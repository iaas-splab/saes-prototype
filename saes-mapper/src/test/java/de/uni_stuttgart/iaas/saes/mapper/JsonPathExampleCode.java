/*
* This file is part of the Serverless Application Extraction System (SAES)
*
* The Serverless Application Extraction System is licensed under under
* the Apache License, Version 2.0. Please see the included COPYING file
* for license information.
*/
package de.uni_stuttgart.iaas.saes.mapper;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import de.uni_stuttgart.iaas.saes.common.facts.Fact;
import de.uni_stuttgart.iaas.saes.common.facts.OriginArtifact;

class JsonPathExampleCode {

	private Gson gson;
	private Fact fact1;

	@BeforeEach
	void setUp() {
		gson = new Gson();
		fact1 = new Fact(//
				"gcloud/functions/eventtrigger", //
				new OriginArtifact(//
						"gcloud", //
						null, //
						"function1"//
				), //
				Map.of(//
						"resource", "A", //
						"event", "storage.object.bucket.finalize", //
						"examplelist", List.of(1, 2, 3)//
				)//
		);
	}

	@Test
	void exampleCode() {
		JsonElement jsone = fact1.toJsonElement(gson);
		JsonElement provider = JPUtil.jsonPath().parse(jsone).read("$.originArtifact.provider");
		assertTrue(gson.fromJson(provider, Object.class) instanceof String);
		assertEquals("gcloud", provider.getAsString());
		JsonElement listEl1 = JPUtil.jsonPath().parse(jsone).read("$.body.examplelist[1]");
		assertTrue(gson.fromJson(listEl1, Object.class) instanceof Number);
		assertEquals("2", listEl1.getAsString());
		assertEquals(2, listEl1.getAsNumber());
		JsonElement list = JPUtil.jsonPath().parse(jsone).read("$.body.examplelist");
		assertEquals("1", list.getAsJsonArray().get(0).getAsString());
	}
}
