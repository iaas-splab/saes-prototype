/*
* This file is part of the Serverless Application Extraction System (SAES)
*
* The Serverless Application Extraction System is licensed under under
* the Apache License, Version 2.0. Please see the included COPYING file
* for license information.
*/
package de.uni_stuttgart.iaas.saes.code_analysis.plugin.java;

public class ExampleObject implements ExampleInterface {
	public void exampleObjectVoid(String a, int b, Object c) {
		System.out.println("ExampleStatic.exampleStaticVoid()");
	}
	public int exampleObjectInt(String a, int b, Object c) {
		System.out.println("ExampleStatic.exampleStaticInt()");
		return 1;
	}
	@Override
	public void exampleInterfaceVoid(String a, int b, Object c) {
		System.out.println("ExampleObject.exampleInterfaceVoid()");
	}
	@Override
	public int exampleInterfaceInt(String a, int b, Object c) {
		System.out.println("ExampleObject.exampleInterfaceInt()");
		return 1;
	}
}
