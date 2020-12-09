/*
* This file is part of the Serverless Application Extraction System (SAES)
*
* The Serverless Application Extraction System is licensed under under
* the Apache License, Version 2.0. Please see the included COPYING file
* for license information.
*/
package de.uni_stuttgart.iaas.saes.code_analysis.plugin.java;

public class ExampleAnalyzedClass {
	public void exampleMethod() {
		String a = "examplestr";
		int b = -1;
		Object c = 42.0;
		
		ExampleInterface iface = ExampleInterface.getInstance();
		ExampleObject object = new ExampleObject();
		
		b = 1;
		ExampleStatic.exampleStaticVoid(a, b, c);
		b = 2;
		ExampleStatic.exampleStaticInt(a, b, c);
		b = 3;
		iface.exampleDefaultInterfaceVoid(a, b, c);
		b = 4;
		iface.exampleDefaultInterfaceInt(a, b, c);
		b = 5;
		iface.exampleInterfaceVoid(a, b, c);
		b = 6;
		iface.exampleInterfaceInt(a, b, c);
		b = 7;
		object.exampleObjectVoid(a, b, c);
		b = 8;
		object.exampleObjectInt(a, b, c);
		b = 9;
		object.exampleDefaultInterfaceVoid(a, b, c);
		b = 9;
		object.exampleDefaultInterfaceInt(a, b, c);
		b = 10;
		object.exampleInterfaceVoid(a, b, c);
		b = 11;
		object.exampleInterfaceInt(a, b, c);
		b = 12;
	}
}
