/*
* This file is part of the Serverless Application Extraction System (SAES)
*
* The Serverless Application Extraction System is licensed under under
* the Apache License, Version 2.0. Please see the included COPYING file
* for license information.
*/
package de.uni_stuttgart.iaas.saes.api_interaction;

import java.util.Properties;

import com.arangodb.ArangoDB;

import de.uni_stuttgart.iaas.saes.common.extr.ExtractionPlugin;
import de.uni_stuttgart.iaas.saes.common.extr.ExtractionRunner;

/**
 * Use this class to initiate extraction from cloud provider APIs
 * 
 * <strong>NOTE: you must register plugins using
 * {@link ExtractionRunner#registerPlugin(ExtractionPlugin)}</strong>
 */
public class ApiInteraction extends ExtractionRunner<ApiInteractionPlugin> {

	public ApiInteraction(ArangoDB arangoDB, Properties properties) {
		super(arangoDB, properties);
	}

}
