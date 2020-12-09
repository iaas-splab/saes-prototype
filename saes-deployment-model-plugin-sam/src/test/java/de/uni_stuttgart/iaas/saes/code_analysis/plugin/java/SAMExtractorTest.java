/*
* This file is part of the Serverless Application Extraction System (SAES)
*
* The Serverless Application Extraction System is licensed under under
* the Apache License, Version 2.0. Please see the included COPYING file
* for license information.
*/
package de.uni_stuttgart.iaas.saes.code_analysis.plugin.java;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;

class SAMExtractorTest {

	private SAMExtractor extractor;

	@BeforeEach
	void setUp() throws Exception {
		extractor = new SAMExtractor();
	}

	@Test
	void testS3JavaSample() {
		var facts = extractor.extractSAM(ExampleTemplates.AWS_SAMPLE_APP_S3_JAVA, "s3-java-example");
		assertEquals(3, facts.size());
	}

	@Test
	void testListManagerSample() {
		var facts = extractor.extractSAM(ExampleTemplates.AWS_SAMPLE_APP_LIST_MANAGER, "s3-java-example");
		System.out.println("SAMExtractorTest.testListManagerSample(): " + facts.size() + " facts found.");
		assertEquals(7, facts.size());
	}

	@Test
	void testErrorProcessorSample() {
		var facts = extractor.extractSAM(ExampleTemplates.AWS_SAMPLE_APP_ERROR_PROCESSOR, "s3-java-example");
		System.out.println("SAMExtractorTest.testErrorProcessorSample(): " + facts.size() + " facts found.");
		assertEquals(9, facts.size());
	}

}
