/*
* This file is part of the Serverless Application Extraction System (SAES)
*
* The Serverless Application Extraction System is licensed under under
* the Apache License, Version 2.0. Please see the included COPYING file
* for license information.
*/
package de.uni_stuttgart.iaas.saes.code_analysis.plugin.java;

public interface ExampleInterface {
	public default void exampleDefaultInterfaceVoid(String a, int b, Object c) {
		System.out.println("ExampleStatic.exampleStaticVoid()");
	}
	public default int exampleDefaultInterfaceInt(String a, int b, Object c) {
		System.out.println("ExampleStatic.exampleStaticInt()");
		return 1;
	}
	public void exampleInterfaceVoid(String a, int b, Object c);
	public int exampleInterfaceInt(String a, int b, Object c);
	
	public static ExampleInterface getInstance() {
		return new ExampleObject();
	}
}
