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

import de.uni_stuttgart.iaas.saes.knowledge_base.domain.Taxonomy;
import de.uni_stuttgart.iaas.saes.knowledge_base.domain.TaxonomyEntry;

class TaxonomySerializationTest {

	private ArangoDB arangoDB;
	private Taxonomy taxonomy1;

	@BeforeEach
	void setUp() {
		arangoDB = new ArangoDB.Builder().build();
		taxonomy1 = new Taxonomy(//
				"gcloud-events", //
				List.of(//
						new TaxonomyEntry("google.storage.object.finalize", "saes/object-storage/store"), //
						new TaxonomyEntry("google.storage.object.delete", "saes/object-storage/delete"), //
						new TaxonomyEntry("google.storage.object.archive", "saes/object-storage/delete"), //
						new TaxonomyEntry("google.storage.object.metadataUpdate", "saes/object-storage/store")//
				)//
		);
	}

	@Test
	void testBaseDocT1() {
		Taxonomy t_converted = Taxonomy.fromBaseDoc(taxonomy1.toBaseDoc());
		assertTrue(EqualsBuilder.reflectionEquals(taxonomy1, t_converted),
				"Taxonomy 1 not equal after fromBaseDoc/toBaseDoc");
	}

	@Test
	void testBaseDocGsonSerializabilityM1() {
		Taxonomy degson_basedoc_taxonomy = gsonAndBack(taxonomy1);
		assertTrue(EqualsBuilder.reflectionEquals(taxonomy1, degson_basedoc_taxonomy),
				"taxonomy 1 not equal after gsonAndBack");
	}

	private Taxonomy gsonAndBack(Taxonomy taxonomy) {
		Gson gson = new Gson();

		String t_gson = taxonomy.toJsonString(gson);
		JsonElement degson_element = com.google.gson.JsonParser.parseString(t_gson);
		Map<String, Object> degson_basedoc = gson.<Map<String, Object>>fromJson(degson_element, Map.class);
		Taxonomy degson_basedoc_taxonomy = Taxonomy.fromBaseDoc(degson_basedoc);
		return degson_basedoc_taxonomy;
	}

	@Test
	void testVPackT1() {
		Taxonomy dejson_depack_Taxonomy = vpackAndBack(taxonomy1);
		assertTrue(EqualsBuilder.reflectionEquals(taxonomy1, dejson_depack_Taxonomy),
				"Taxonomy 1 not equal after vpackAndBack");
	}

	private Taxonomy vpackAndBack(Taxonomy taxonomy) {
		Map<String, Object> t_basedoc = taxonomy.toBaseDoc();
		VPackSlice t_basedoc_slice = arangoDB.util().serialize(t_basedoc);
		String t_basedoc_slice_json = t_basedoc_slice.toString();
		VPackSlice dejson = arangoDB.util().serialize(t_basedoc_slice_json);
		Map<String, Object> dejson_depack = arangoDB.util().deserialize(dejson, Map.class);
		Taxonomy dejson_depack_taxonomy = Taxonomy.fromBaseDoc(dejson_depack);
		return dejson_depack_taxonomy;
	}
}
