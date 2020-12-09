/*
* This file is part of the Serverless Application Extraction System (SAES)
*
* The Serverless Application Extraction System is licensed under under
* the Apache License, Version 2.0. Please see the included COPYING file
* for license information.
*/
package de.uni_stuttgart.iaas.saes.api_interaction.plugin.azure;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.Azure.Authenticated;
import com.microsoft.azure.management.appservice.FunctionApp;
import com.microsoft.azure.management.appservice.FunctionEnvelope;

import de.uni_stuttgart.iaas.saes.common.facts.Fact;
import de.uni_stuttgart.iaas.saes.common.facts.OriginArtifact;
import de.uni_stuttgart.iaas.saes.common.util.BaseDocUtil;

public class FunctionappExtractor {
	private Gson gson = new Gson();

	public List<Fact> extract() throws IOException {
		Authenticated auth = Azure
				.authenticate(new File(System.getProperty("user.home") + "/.azure/mgmtapi-auth.properties"));
		Azure azure = auth.withDefaultSubscription();

		List<Fact> result = new ArrayList<>();

		for (FunctionApp app : azure.appServices().functionApps().list()) {
			for (FunctionEnvelope fun : azure.appServices().functionApps().listFunctions(app.resourceGroupName(), app.name())) {
				Map<String, Object> config = BaseDocUtil.toBaseDoc(fun.config(), gson);
				result.add(new Fact(//
						"azure/appservices/function",
						new OriginArtifact("azure", app.resourceGroupName(), app.name() + "/" + fun.inner().name()),//
						config));
			}
		}
		return result;
	}

	public static void main(String[] args) throws IOException {
		List<Fact> extractedFacts = new FunctionappExtractor().extract();
		for (Fact fact : extractedFacts) {
			System.out.println(fact.toPrettyJsonString(new Gson()));
		}
		System.exit(0); // lingering api threads
	}
}
