/*
* This file is part of the Serverless Application Extraction System (SAES)
*
* The Serverless Application Extraction System is licensed under under
* the Apache License, Version 2.0. Please see the included COPYING file
* for license information.
*/
package de.uni_stuttgart.iaas.saes.code_analysis.plugin.java;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.objectweb.asm.tree.analysis.AnalyzerException;

import de.uni_stuttgart.iaas.saes.code_analysis.CodeAnalysisPlugin;
import de.uni_stuttgart.iaas.saes.common.facts.Fact;
import de.uni_stuttgart.iaas.saes.common.facts.OriginArtifact;
import de.uni_stuttgart.iaas.saes.common.util.SaesProperties;
import de.uni_stuttgart.iaas.saes.common.util.SaesException;

public class CodeAnalysisPluginJava implements CodeAnalysisPlugin {

	private final Properties pluginProperties;

	public CodeAnalysisPluginJava(Properties pluginProperties) {
		this.pluginProperties = pluginProperties;
	}

	@Override
	public List<Fact> analyze(Properties analysisProperties) throws SaesException {
		var mprop = new SaesProperties(pluginProperties);
		mprop.putAll(analysisProperties);

		var ctx = new AnalysisContext(Paths.get(mprop.requireProperty("source-code-analysis-input")),
				mprop.requireProperty("source-code-analysis-class"),
				mprop.requireProperty("source-code-analysis-method"));
		var res = new ArrayList<Fact>();
		try {
			for (var call : ctx.analyze()) {
				res.add(new Fact("saes/code-analysis/java",
						new OriginArtifact(mprop.getProperty("source-code-analysis-input-provider"),
								mprop.getProperty("source-code-analysis-input-namespace"),
								mprop.getProperty("source-code-analysis-input-resource")),
						Map.of(//
								"targetClass", call.getTargetClass(), //
								"targetMethodDesc", call.getTargetMethodDesc(), //
								"targetMethodName", call.getTargetMethodName(), //
								"callArts", call.getCallArgs()//
						)));
			}
		} catch (AnalyzerException e) {
			throw new SaesException("exception occurred while analyzing", e);
		}
		return res;
	}

}
