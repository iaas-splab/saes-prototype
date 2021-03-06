/*
* This file is part of the Serverless Application Extraction System (SAES)
*
* The Serverless Application Extraction System is licensed under under
* the Apache License, Version 2.0. Please see the included COPYING file
* for license information.
*/
package de.uni_stuttgart.iaas.saes.mapper;

import com.google.gson.Gson;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ParseContext;
import com.jayway.jsonpath.spi.json.GsonJsonProvider;

import de.uni_stuttgart.iaas.saes.common.db.KnowledgeBaseObject;

public class JPUtil {
	public static final DocumentContext documentContext(Gson gson, KnowledgeBaseObject kbo) {
		return jsonPath().parse(kbo.toJsonElement(gson));
	}
	
	public static final ParseContext jsonPath() {
		return JsonPath.using(config());
	}
	
	private static final Configuration config() {
		return Configuration.defaultConfiguration().jsonProvider(new GsonJsonProvider());
	}

}
