/*
* This file is part of the Serverless Application Extraction System (SAES)
*
* The Serverless Application Extraction System is licensed under under
* the Apache License, Version 2.0. Please see the included COPYING file
* for license information.
*/
package de.uni_stuttgart.iaas.saes.api_interaction.plugin.azure;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.microsoft.azure.CloudException;

import de.uni_stuttgart.iaas.saes.api_interaction.ApiInteractionPlugin;
import de.uni_stuttgart.iaas.saes.common.facts.Fact;
import de.uni_stuttgart.iaas.saes.common.util.SaesProperties;
import de.uni_stuttgart.iaas.saes.common.util.SaesException;

public class ApiInteractionPluginAzure implements ApiInteractionPlugin {

	private final Properties pluginProperties;

	public ApiInteractionPluginAzure(Properties pluginProperties) {
		this.pluginProperties = pluginProperties;
	}

	@Override
	public List<Fact> analyze(Properties analysisProperties) throws SaesException {
		var mprop = new SaesProperties(pluginProperties);
		mprop.putAll(analysisProperties);

		var res = new ArrayList<Fact>();
		try {
			res.addAll(new FunctionappExtractor().extract());
		} catch (IOException | CloudException e) {
			throw new SaesException("Could not extract from AWS API", e);
		}
		return res;
	}

}
