/*
* This file is part of the Serverless Application Extraction System (SAES)
*
* The Serverless Application Extraction System is licensed under under
* the Apache License, Version 2.0. Please see the included COPYING file
* for license information.
*/
package de.uni_stuttgart.iaas.saes.code_analysis;

import java.util.Properties;

import com.arangodb.ArangoDB;

import de.uni_stuttgart.iaas.saes.common.extr.ExtractionRunner;

/**
 * Use this class to initiate extraction via static code analysis
 * 
 * <strong>NOTE: you must register plugins using
 * {@link ExtractionRunner#registerPlugin}</strong>
 */
public class CodeAnalysis extends ExtractionRunner<CodeAnalysisPlugin> {

	public CodeAnalysis(ArangoDB arangoDB, Properties properties) {
		super(arangoDB, properties);
	}

}
