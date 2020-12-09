/*
* This file is part of the Serverless Application Extraction System (SAES)
*
* The Serverless Application Extraction System is licensed under under
* the Apache License, Version 2.0. Please see the included COPYING file
* for license information.
*/
package de.uni_stuttgart.iaas.saes.code_analysis.plugin.java;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.text.StringEscapeUtils;
import org.objectweb.asm.Type;

import de.uni_stuttgart.iaas.saes.code_analysis.plugin.java.ConstantTracker.ConstantValue;

/** Holds information about a method call found in the bytecode */
public class MethodCall {
	private String targetClass;
	private String targetMethodName;
	private String targetMethodDesc;
	private List<Type> targetMethodArgs;
	private List<ConstantValue> callArgs;

	public MethodCall(String targetClass, String targetMethodName, String targetMethodDesc,
			List<ConstantValue> callArgs) {
		this.targetClass = targetClass;
		this.targetMethodName = targetMethodName;
		this.targetMethodDesc = targetMethodDesc;
		this.targetMethodArgs = Arrays.asList(Type.getArgumentTypes(targetMethodDesc));
		this.callArgs = callArgs;
	}

	@Override
	public String toString() {
		var sb = new StringBuilder();
		sb.append(getTargetClass().replaceAll("/", "."));
		sb.append(".");
		sb.append(getTargetMethodName());
		sb.append("(");
		sb.append(getCallArgs().stream().map(MethodCall::argToString).collect(Collectors.joining(", ")));
		sb.append(")");
		return sb.toString();
	}

	private static String argToString(ConstantValue value) {
		if (value.getValue() instanceof String) {
			return "\"" + StringEscapeUtils.escapeJava((String)value.getValue()) + "\"";
		} else if (value == ConstantTracker.NULL) {
			return "null";
		} else if (value.getValue() != null) {
			return String.valueOf(value);
		} else {
			return "?";
		}
	}

	/** @return class holding the target method */
	public String getTargetClass() {
		return targetClass;
	}

	/** @return name of target method */
	public String getTargetMethodName() {
		return targetMethodName;
	}

	/** @return the method descriptor (i.e. machine-readable return and parameter types) */
	public String getTargetMethodDesc() {
		return targetMethodDesc;
	}

	/** @return the list of parameters of the target method */
	public List<Type> getTargetMethodArgs() {
		return targetMethodArgs;
	}

	/** @return inferred actual arguments */
	public List<ConstantValue> getCallArgs() {
		return callArgs;
	}

}
