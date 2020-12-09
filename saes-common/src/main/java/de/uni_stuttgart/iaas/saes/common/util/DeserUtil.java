/*
* This file is part of the Serverless Application Extraction System (SAES)
*
* The Serverless Application Extraction System is licensed under under
* the Apache License, Version 2.0. Please see the included COPYING file
* for license information.
*/
package de.uni_stuttgart.iaas.saes.common.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uni_stuttgart.iaas.saes.common.db.KnowledgeBaseObject;

public class DeserUtil {

	private String callerName;

	public <T extends KnowledgeBaseObject> DeserUtil(Class<T> caller) {
		callerName = caller.getSimpleName();
	}

	public <V> void assertContains(Map<String, V> map, String... keys) {
		for (String key : keys) {
			if (!map.containsKey(key)) {
				throw new SaesException(reason("Missing value", key));
			}
			if (null == map.get(key)) {
				throw new SaesException(reason("Null value", key));
			}
		}
	}
	
	public Map<String, Object> ensureList(Map<String, Object> map, String... keys) {
		Map<String, Object> copy = new HashMap<>(map);
		for (String key : keys) {
			Object val = copy.get(key);
			if (null == val) {
				copy.put(key, new ArrayList<Object>());
			} else if (!(val instanceof List<?>)) {
				throw new SaesException(reason("Expected list value (json array)", key));
			}
		}
		return Collections.unmodifiableMap(copy);
	}
	
	public Map<String, Object> ensureMap(Map<String, Object> map, String... keys) {
		Map<String, Object> copy = new HashMap<>(map);
		for (String key : keys) {
			Object val = copy.get(key);
			if (null == val) {
				copy.put(key, new HashMap<String, Object>());
			} else if (!(val instanceof Map<?,?>)) {
				throw new SaesException(reason("Expected map value (json object)", key));
			}
		}
		return Collections.unmodifiableMap(copy);
	}
	
	private String reason(String detail, String key) {
		return "Error when deserializing " + callerName + ": " + detail + " for key '" + key + "'";
	}

}
