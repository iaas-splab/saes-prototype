/*
* This file is part of the Serverless Application Extraction System (SAES)
*
* The Serverless Application Extraction System is licensed under under
* the Apache License, Version 2.0. Please see the included COPYING file
* for license information.
*/
package de.uni_stuttgart.iaas.saes.knowledge_base.ser;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.arangodb.ArangoDB;
import com.arangodb.velocypack.VPackSlice;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import de.uni_stuttgart.iaas.saes.knowledge_base.domain.PropertyConstant;
import de.uni_stuttgart.iaas.saes.knowledge_base.domain.JSONPathAccessor;
import de.uni_stuttgart.iaas.saes.knowledge_base.domain.LookupFilter;
import de.uni_stuttgart.iaas.saes.knowledge_base.domain.Mapping;
import de.uni_stuttgart.iaas.saes.knowledge_base.domain.RegexFilter;
import de.uni_stuttgart.iaas.saes.knowledge_base.domain.TargetProperty;

class MappingSerializationTest {

	private ArangoDB arangoDB;
	private Mapping mapping1;
	private Mapping mapping2;

	@BeforeEach
	void setUp() {
		arangoDB = new ArangoDB.Builder().build();
		mapping1 = new Mapping(//
				Map.of("type", "gcloud/functions/eventtrigger"), //
				"case/eventsource", //
				List.of(//
						new TargetProperty(//
								"event", //
								new LookupFilter(//
										"gcloud-events", //
										new JSONPathAccessor("$.eventTrigger.event")//
								)//
						)//
				)//
		);
		mapping2 = new Mapping(//
				Map.of("type", "some/example/type"), //
				"case/directcall", //
				List.of(//
						new TargetProperty(//
								"key", //
								new RegexFilter(//
										"a", //
										"b", //
										new PropertyConstant("a")//
								)//
						)//
				)//
		);
	}

	@Test
	void testBaseDocM1() {
		Mapping m_converted = Mapping.fromBaseDoc(mapping1.toBaseDoc());
		assertTrue(EqualsBuilder.reflectionEquals(mapping1, m_converted), "mapping 1 not equal after fromBaseDoc/toBaseDoc");
	}
	
	@Test
	void testBaseDocM2() {
		Mapping m_converted = Mapping.fromBaseDoc(mapping2.toBaseDoc());
		assertTrue(EqualsBuilder.reflectionEquals(mapping2, m_converted), "mapping 2 not equal after fromBaseDoc/toBaseDoc");
	}
	
	@Test
	void testBaseDocGsonSerializabilityM1() {
		Mapping degson_basedoc_mapping = gsonAndBack(mapping1);
		assertTrue(EqualsBuilder.reflectionEquals(mapping1, degson_basedoc_mapping), "mapping 1 not equal after gsonAndBack");
	}
	
	@Test
	void testBaseDocGsonSerializabilityM2() {
		Mapping degson_basedoc_mapping = gsonAndBack(mapping2);
		assertTrue(EqualsBuilder.reflectionEquals(mapping2, degson_basedoc_mapping), "mapping 2 not equal after gsonAndBack");
	}

	private Mapping gsonAndBack(Mapping mapping) {
		Gson gson = new Gson();

		String m_gson = mapping.toJsonString(gson);
		JsonElement degson_element = com.google.gson.JsonParser.parseString(m_gson);
		Map<String, Object> degson_basedoc = gson.<Map<String, Object>>fromJson(degson_element, Map.class);
		Mapping degson_basedoc_mapping = Mapping.fromBaseDoc(degson_basedoc);
		return degson_basedoc_mapping;
	}

	@Test
	void testVPackM1() {
		Mapping dearangojson_depack_mapping = vpackAndBack(mapping1);
		assertTrue(EqualsBuilder.reflectionEquals(mapping1, dearangojson_depack_mapping), "mapping 1 not equal after vpackAndBack");
	}

	@Test
	void testVPackM2() {
		Mapping dearangojson_depack_mapping = vpackAndBack(mapping2);
		assertTrue(EqualsBuilder.reflectionEquals(mapping2, dearangojson_depack_mapping), "mapping 2 not equal after vpackAndBack");
	}

	private Mapping vpackAndBack(Mapping mapping) {
		Map<String, Object> m_basedoc = mapping.toBaseDoc();
		VPackSlice m_basedoc_slice = arangoDB.util().serialize(m_basedoc);
		String m_basedoc_slice_arangojson = m_basedoc_slice.toString();
		VPackSlice dearangojson = arangoDB.util().serialize(m_basedoc_slice_arangojson);
		Map<String, Object> dearangojson_depack = arangoDB.util().deserialize(dearangojson, Map.class);
		Mapping dearangojson_depack_mapping = Mapping.fromBaseDoc(dearangojson_depack);
		return dearangojson_depack_mapping;
	}
}
