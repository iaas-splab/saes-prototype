/*
* This file is part of the Serverless Application Extraction System (SAES)
*
* The Serverless Application Extraction System is licensed under under
* the Apache License, Version 2.0. Please see the included COPYING file
* for license information.
*/
package de.uni_stuttgart.iaas.saes.mapper;

import java.io.InputStreamReader;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;

import de.uni_stuttgart.iaas.saes.common.facts.Fact;
import de.uni_stuttgart.iaas.saes.knowledge_base.domain.Mapping;

/**
 * test cases generated by testing random strings of the alphabet [abc] against
 * random patterns from the alphabet [abc?*] in a <strong>different
 * implementation</strong>, namely the glob matching of GNU bash
 */
class MappingMatcherTest2 {

	private MappingApplier ma;
	private Fact fact1;
	private Mapping mapping1;

	private Gson gson;

	@BeforeEach
	void setUp() {
		gson = new Gson();
		ma = new MappingApplier(s -> null);
		fact1 = Fact
				.fromBaseDoc(
						gson.<Map<String, Object>>fromJson(
								new InputStreamReader(MappingMatcherTest2.class.getClassLoader()
										.getResourceAsStream("saes-test-examples/fact-functiontrigger-aws")),
								Map.class));
		mapping1 = Mapping
				.fromBaseDoc(
						gson.<Map<String, Object>>fromJson(
								new InputStreamReader(MappingMatcherTest2.class.getClassLoader()
										.getResourceAsStream("saes-test-examples/mapping-functiontrigger-aws")),
								Map.class));
	}

	@Test
	void testMatchFactMapping() {
		ma.apply(fact1, mapping1);
	}
}
