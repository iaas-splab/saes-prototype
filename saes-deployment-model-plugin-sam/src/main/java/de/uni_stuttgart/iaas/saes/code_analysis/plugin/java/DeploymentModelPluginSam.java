/*
* This file is part of the Serverless Application Extraction System (SAES)
*
* The Serverless Application Extraction System is licensed under under
* the Apache License, Version 2.0. Please see the included COPYING file
* for license information.
*/
package de.uni_stuttgart.iaas.saes.code_analysis.plugin.java;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import de.uni_stuttgart.iaas.saes.common.facts.Fact;
import de.uni_stuttgart.iaas.saes.common.util.SaesException;
import de.uni_stuttgart.iaas.saes.common.util.SaesProperties;
import de.uni_stuttgart.iaas.saes.deployment_model.DeploymentModelsPlugin;

public class DeploymentModelPluginSam implements DeploymentModelsPlugin {

	private final Properties pluginProperties;

	public DeploymentModelPluginSam(Properties pluginProperties) {
		this.pluginProperties = pluginProperties;
	}

	public DeploymentModelPluginSam() {
		this(new Properties());
	}

	@Override
	public List<Fact> analyze(Properties analysisProperties) throws SaesException {
		var mprop = new SaesProperties(pluginProperties);
		mprop.putAll(analysisProperties);

		var res = new ArrayList<Fact>();
		try {
			res.addAll(new SAMExtractor().extractSAM(mprop.getProperty("sam-model"), mprop.getProperty("sam-model-namespace", "model1")));
		} catch (SaesException e) {
			throw new SaesException("exception occurred while analyzing", e);
		}
		return res;
	}

}
