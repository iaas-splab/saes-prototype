/*
* This file is part of the Serverless Application Extraction System (SAES)
*
* The Serverless Application Extraction System is licensed under under
* the Apache License, Version 2.0. Please see the included COPYING file
* for license information.
*/
package de.uni_stuttgart.iaas.saes.code_analysis.plugin.java;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.text.StringEscapeUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.objectweb.asm.tree.analysis.AnalyzerException;

class AnalysisContextTest {

	@TempDir
	Path tempDir;

	private static final List<String> C_TYPES = List.of("<package>/ExampleStatic", "<package>/ExampleStatic",
			"<package>/ExampleInterface", "<package>/ExampleInterface", "<package>/ExampleInterface",
			"<package>/ExampleInterface", "<package>/ExampleObject", "<package>/ExampleObject",
			"<package>/ExampleObject", "<package>/ExampleObject", "<package>/ExampleObject", "<package>/ExampleObject");

	private static final List<String> C_METHODS = List.of("exampleStaticVoid", "exampleStaticInt",
			"exampleDefaultInterfaceVoid", "exampleDefaultInterfaceInt", "exampleInterfaceVoid", "exampleInterfaceInt",
			"exampleObjectVoid", "exampleObjectInt", "exampleDefaultInterfaceVoid", "exampleDefaultInterfaceInt",
			"exampleInterfaceVoid", "exampleInterfaceInt");

	private static final List<String> C_ARG1 = List.of("examplestr", "examplestr", "examplestr", "examplestr",
			"examplestr", "examplestr", "examplestr", "examplestr", "examplestr", "examplestr", "examplestr",
			"examplestr");

	private static final List<Integer> C_ARG2 = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 9, 10, 11);

	@SuppressWarnings("unused")
	@Test
	void test() throws IOException, AnalyzerException {
		var exampleAnalyzedClassName = ExampleAnalyzedClass.class.getName();
		var filename = exampleAnalyzedClassName.replaceAll("\\.", "/") + ".class";
		try (var classIS = ExampleAnalyzedClass.class.getClassLoader().getResourceAsStream(filename)) {
			Files.createDirectories(tempDir.resolve(filename).getParent());
			Files.copy(classIS, tempDir.resolve(filename));
		}
		var ctx = new AnalysisContext(tempDir, exampleAnalyzedClassName, "exampleMethod");
		var list = ctx.analyze();

		var exampleCalls = list.stream().filter(c -> c.getTargetMethodName().startsWith("example"))
				.collect(Collectors.toList());

		var callTypes = exampleCalls.stream().map(c -> c.getTargetClass())
				.map(s -> '"' + StringEscapeUtils.escapeJava(s) + '"').collect(Collectors.toList());
		var callMethods = exampleCalls.stream().map(c -> c.getTargetMethodName())
				.map(s -> '"' + StringEscapeUtils.escapeJava(s) + '"').collect(Collectors.toList());
		var callArg1 = exampleCalls.stream().map(c -> (String) c.getCallArgs().get(0).getValue())
				.map(s -> '"' + StringEscapeUtils.escapeJava(s) + '"').collect(Collectors.toList());
		var callArg2 = exampleCalls.stream().map(c -> (Integer) c.getCallArgs().get(1).getValue())
				.collect(Collectors.toList());

		for (int i = 0; i < exampleCalls.size(); i++) {
			MethodCall call = exampleCalls.get(i);
			String errormsg = " in " + call;
			assertEquals(
					C_TYPES.get(i).replace("<package>", ExampleAnalyzedClass.class.getPackageName().replace('.', '/')),
					call.getTargetClass(), "target class" + errormsg);
			assertEquals(C_METHODS.get(i), call.getTargetMethodName(), "target method" + errormsg);
			assertEquals(C_ARG1.get(i), call.getCallArgs().get(0).getValue(), "arg1" + errormsg);
			assertEquals(C_ARG2.get(i), call.getCallArgs().get(1).getValue(), "arg2" + errormsg);
		}
	}
}
