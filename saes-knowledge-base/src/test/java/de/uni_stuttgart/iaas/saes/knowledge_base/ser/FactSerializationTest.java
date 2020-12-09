/*
* This file is part of the Serverless Application Extraction System (SAES)
*
* The Serverless Application Extraction System is licensed under under
* the Apache License, Version 2.0. Please see the included COPYING file
* for license information.
*/
package de.uni_stuttgart.iaas.saes.knowledge_base.ser;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.arangodb.ArangoDB;
import com.arangodb.velocypack.VPackSlice;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import de.uni_stuttgart.iaas.saes.common.facts.Fact;
import de.uni_stuttgart.iaas.saes.common.facts.OriginArtifact;

class FactSerializationTest {

	private ArangoDB arangoDB;
	private Fact fact1;

	@BeforeEach
	void setUp() {
		arangoDB = new ArangoDB.Builder().build();
		fact1 = new Fact(//
				"gcloud/functions/eventtrigger", //
				new OriginArtifact(//
						"gcloud", //
						null, //
						"function1"//
				), //
				Map.of(//
						"resource", "A", //
						"event", "storage.object.bucket.finalize"//
				)//
		);
	}

	@Test
	void testBaseDocT1() {
		Fact f_converted = Fact.fromBaseDoc(fact1.toBaseDoc());
		assertTrue(EqualsBuilder.reflectionEquals(fact1, f_converted), "Fact 1 not equal after fromBaseDoc/toBaseDoc");
	}

	@Test
	void testBaseDocGsonSerializabilityM1() {
		Fact degson_basedoc_fact = gsonAndBack(fact1);
		assertTrue(EqualsBuilder.reflectionEquals(fact1, degson_basedoc_fact), "fact 1 not equal after gsonAndBack");
	}

	private Fact gsonAndBack(Fact fact) {
		Gson gson = new Gson();

		String f_gson = fact.toJsonString(gson);
		JsonElement degson_element = com.google.gson.JsonParser.parseString(f_gson);
		Map<String, Object> degson_basedoc = gson.<Map<String, Object>>fromJson(degson_element, Map.class);
		Fact degson_basedoc_fact = Fact.fromBaseDoc(degson_basedoc);
		return degson_basedoc_fact;
	}

	@Test
	void testVPackT1() {
		Fact dejson_depack_Fact = vpackAndBack(fact1);
		assertTrue(EqualsBuilder.reflectionEquals(fact1, dejson_depack_Fact), "Fact 1 not equal after vpackAndBack");
	}

	private Fact vpackAndBack(Fact fact) {
		Map<String, Object> f_basedoc = fact.toBaseDoc();
		VPackSlice f_basedoc_slice = arangoDB.util().serialize(f_basedoc);
		String f_basedoc_slice_json = f_basedoc_slice.toString();
		VPackSlice dejson = arangoDB.util().serialize(f_basedoc_slice_json);
		Map<String, Object> dejson_depack = arangoDB.util().deserialize(dejson, Map.class);
		Fact dejson_depack_fact = Fact.fromBaseDoc(dejson_depack);
		return dejson_depack_fact;
	}
}
