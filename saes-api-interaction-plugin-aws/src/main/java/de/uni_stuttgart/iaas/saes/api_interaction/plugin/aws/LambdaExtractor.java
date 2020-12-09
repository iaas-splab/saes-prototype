/*
* This file is part of the Serverless Application Extraction System (SAES)
*
* The Serverless Application Extraction System is licensed under under
* the Apache License, Version 2.0. Please see the included COPYING file
* for license information.
*/
package de.uni_stuttgart.iaas.saes.api_interaction.plugin.aws;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.FunctionConfiguration;
import com.amazonaws.services.lambda.model.GetPolicyRequest;
import com.amazonaws.services.lambda.model.ListFunctionsResult;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.BucketNotificationConfiguration;
import com.amazonaws.services.s3.model.NotificationConfiguration;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import de.uni_stuttgart.iaas.saes.common.facts.Fact;
import de.uni_stuttgart.iaas.saes.common.facts.OriginArtifact;
import de.uni_stuttgart.iaas.saes.common.util.BaseDocUtil;

public class LambdaExtractor {
	private Gson gson = new Gson();

	/**
	 * Search for facts in a region
	 * @param credentials AWS login credentials
	 * @param inRegion AWS region to examine
	 * @return the extracted facts
	 */
	public List<Fact> extract(AWSCredentialsProvider credentials, String inRegion) {
		var lambdaBuilder = AWSLambdaClientBuilder.standard();
		var s3Builder = AmazonS3ClientBuilder.standard();
		if (credentials != null) {
			lambdaBuilder.setCredentials(credentials);
			s3Builder.setCredentials(credentials);
		}
		String region = inRegion != null ? inRegion : "us-east-1";
		lambdaBuilder.setRegion(region);
		s3Builder.setRegion(region);
		var lambda = lambdaBuilder.build();
		var s3 = s3Builder.build();

		List<Fact> result = new ArrayList<>();

		ListFunctionsResult allFunctions = lambda.listFunctions();
		for (FunctionConfiguration func : allFunctions.getFunctions()) {
			Map<String, Object> factProps = BaseDocUtil.toBaseDoc(func, gson);
			result.add(new Fact(//
					"aws/lambda/function", //
					new OriginArtifact("aws", region, func.getFunctionName()), //
					factProps));

			var polRequest = new GetPolicyRequest();
			polRequest.setFunctionName(func.getFunctionName());
			var polResult = lambda.getPolicy(polRequest);
			JsonObject policy = JsonParser.parseString(polResult.getPolicy()).getAsJsonObject();

			JsonElement policyStatements = policy.remove("Statement");
			if (policyStatements != null && policyStatements.isJsonArray()) {
				for (JsonElement statement : policyStatements.getAsJsonArray()) {
					Map<String, Object> statementFactProps = new HashMap<>();
					statementFactProps.putAll(BaseDocUtil.toBaseDoc(policy, gson));
					statementFactProps.put("statement", statement);
					result.add(new Fact(//
							"aws/lambda/function/policystatement", //
							new OriginArtifact("aws", region, func.getFunctionName()), //
							statementFactProps));
				}
			}
		}

		for (Bucket bucket : s3.listBuckets()) {
			result.add(new Fact(//
					"aws/s3/bucket", //
					new OriginArtifact("aws", null, bucket.getName()), //
					BaseDocUtil.toBaseDoc(bucket, gson)));
			BucketNotificationConfiguration bnc = s3.getBucketNotificationConfiguration(bucket.getName());
			for (Map.Entry<String, NotificationConfiguration> entry : bnc.getConfigurations().entrySet()) {
				result.add(new Fact(//
						"aws/s3/bucket/notification", //
						new OriginArtifact("aws", null, bucket.getName()), //
						Map.of(//
								"bucket", bucket.getName(), //
								"notificationId", entry.getKey(), //
								"notification", BaseDocUtil.toBaseDoc(entry.getValue(), gson))));
			}
			System.out.println(bnc);
			System.out.println("..");
		}
		return result;
	}

//	public static void main(String[] args) {
//		List<Fact> extractedFacts = new LambdaExtractor().extract(null, null);
//		for (Fact fact : extractedFacts) {
//			System.out.println(fact.toPrettyJsonString(new Gson()));
//		}
//		System.exit(0); // lingering api threads
//	}
}
