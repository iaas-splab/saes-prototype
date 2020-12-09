/*
* This file is part of the Serverless Application Extraction System (SAES)
*
* The Serverless Application Extraction System is licensed under under
* the Apache License, Version 2.0. Please see the included COPYING file
* for license information.
*/
//package de.uni_stuttgart.iaas.saes.code_analysis.plugin.java;
//
//import java.nio.file.Paths;
//
//import org.apache.commons.cli.DefaultParser;
//import org.apache.commons.cli.HelpFormatter;
//import org.apache.commons.cli.Option;
//import org.apache.commons.cli.Options;
//import org.apache.commons.cli.ParseException;
//
//public class BytecodeAnalyzer {
//	public static void main(String[] args) {
//		var options = new Options();
//		var optIn = new Option("i", "input", true, "Input classpath element (JAR, folders are NYI)");
//		var optClass = new Option("c", "class", true, "Class name");
//		var optMethod = new Option("m", "method", true, "Method name");
//
//		for (var opt : new Option[] { optIn, optClass, optMethod }) {
//			opt.setRequired(true);
//			options.addOption(opt);
//		}
//
//		var parser = new DefaultParser();
//		var help = new HelpFormatter();
//
//		try {
//			var cmd = parser.parse(options, args);
//			System.out.println("=====");
//			System.out.println(cmd);
//			System.out.println("=====");
//			
//			var ctx = new AnalysisContext(Paths.get(cmd.getOptionValue("input")), cmd.getOptionValue("class"),
//					cmd.getOptionValue("method"));
//			ctx.analyze();
//		} catch (ParseException e) {
//			help.printHelp("BytecodeAnalyzer -i JAR -c CLASS -m METHOD", options);
//			System.exit(1);
//		}
//	}
//}
