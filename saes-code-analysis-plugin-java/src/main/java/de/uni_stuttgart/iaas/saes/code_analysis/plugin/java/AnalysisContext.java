/*
* This file is part of the Serverless Application Extraction System (SAES)
*
* The Serverless Application Extraction System is licensed under under
* the Apache License, Version 2.0. Please see the included COPYING file
* for license information.
*/
package de.uni_stuttgart.iaas.saes.code_analysis.plugin.java;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

import de.uni_stuttgart.iaas.saes.code_analysis.plugin.java.ConstantTracker.ConstantValue;

public class AnalysisContext {
	private final Path basePath;
	private final String className;
	private final String methodName;

	/**
	 * Construct a new analysis context
	 * @param path Classpath element - can be a folder or a JAR file
	 * @param className class to examine
	 * @param methodName method to examine
	 */
	public AnalysisContext(Path path, String className, String methodName) {
		if (Files.isDirectory(path)) {
			basePath = path;
		} else if (Files.isReadable(path)) {
			try {
				var fs = FileSystems.newFileSystem(path, null);
				basePath = fs.getPath("/");
			} catch (IOException e) {
				throw new RuntimeException("Cannot open jar file", e);
			}
		} else {
			throw new RuntimeException("Input file does not exist");
		}
		this.className = className.replaceAll("\\.class", "").replaceAll("\\.", "/").concat(".class");
		this.methodName = methodName;
	}

	public Optional<byte[]> loadResource(String subpath) {
		try {
			return Optional.of(Files.readAllBytes(basePath.resolve(subpath)));
		} catch (IOException e) {
			return Optional.empty();
		}
	}

	public List<MethodCall> analyze() throws AnalyzerException {
		var result = new ArrayList<MethodCall>();

		var reader = new ClassReader(loadResource(className).get());
		var clazzNode = new ClassNode();
		reader.accept(clazzNode, ClassReader.EXPAND_FRAMES);

		var methodNode = clazzNode.methods.stream().filter(mn -> methodName.equals(mn.name)).findAny()
				.orElseThrow(() -> new RuntimeException("Input method does not exist"));

		var textifier = new Textifier();
		methodNode.accept(new TraceMethodVisitor(textifier));

		var writer = new PrintWriter(System.out);
		textifier.print(writer);
		writer.flush();
		System.out.println("=====");
		
		var analyzer = new Analyzer<>(new ConstantTracker());

		analyzer.analyze(clazzNode.name, methodNode);
		Frame<ConstantValue>[] frames = analyzer.getFrames();
		System.out.println(frames.length);
		for (int i = 0; i < frames.length; i++) {
			var frame = frames[i];
			if (frame == null)
				continue;
			AbstractInsnNode insnNode = methodNode.instructions.get(i);
			if (!(insnNode instanceof MethodInsnNode)) {
				continue;
			}
			var mInsnNode = (MethodInsnNode) insnNode;
			Type methodType = Type.getMethodType(mInsnNode.desc);
			
			int numArgs = methodType.getArgumentTypes().length;
			int stackFrom;
			int stackTo;
			
			if (mInsnNode.getOpcode() == Opcodes.INVOKESTATIC) {
				// INVOKESTATIC has no "this"
				stackFrom = 0;
				stackTo = numArgs - 1;
			} else {
				stackFrom = 1;
				stackTo = numArgs;
			}
			
			var callArgs = IntStream.rangeClosed(stackFrom, stackTo).mapToObj(frame::getStack).collect(Collectors.toList());
			result.add(new MethodCall(mInsnNode.owner, mInsnNode.name, mInsnNode.desc, callArgs));
		}
		return Collections.unmodifiableList(result);
	}
}



