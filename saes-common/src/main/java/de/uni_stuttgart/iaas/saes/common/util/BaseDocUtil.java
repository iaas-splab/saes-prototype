/*
* This file is part of the Serverless Application Extraction System (SAES)
*
* The Serverless Application Extraction System is licensed under under
* the Apache License, Version 2.0. Please see the included COPYING file
* for license information.
*/
package de.uni_stuttgart.iaas.saes.common.util;

import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

public class BaseDocUtil {

	public static Map<String, Object> toBaseDoc(Object o, Gson gson) {
		JsonElement json = gson.toJsonTree(o);
		return gson.<Map<String, Object>>fromJson(json, Map.class);
	}
}
