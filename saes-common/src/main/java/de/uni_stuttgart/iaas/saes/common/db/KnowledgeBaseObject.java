/*
* This file is part of the Serverless Application Extraction System (SAES)
*
* The Serverless Application Extraction System is licensed under under
* the Apache License, Version 2.0. Please see the included COPYING file
* for license information.
*/
package de.uni_stuttgart.iaas.saes.common.db;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

/** Interface for all objects that belong to the knowledge base workflow and need to be JSON-ized or stored in databases. */
public interface KnowledgeBaseObject {
	List<KnowledgeBaseObject> baseDocChildren();
	Map<String, Object> toBaseDoc();
	default String toJsonString(Gson gson) {
		return gson.toJson(this.toBaseDoc());
	}
	default String toPrettyJsonString(Gson gson) {
		Gson tweakedGson = gson.newBuilder().setPrettyPrinting().create();
		return tweakedGson.toJson(this.toBaseDoc());
	}
	default JsonElement toJsonElement(Gson gson) {
		return gson.toJsonTree(this.toBaseDoc());
	}

	public static List<Map<String, Object>> convertList(List<? extends KnowledgeBaseObject> list) {
		return list.stream().map(KnowledgeBaseObject::toBaseDoc).collect(Collectors.toList());
	}

	@SuppressWarnings("unchecked")
	public static <T extends KnowledgeBaseObject> List<T> listFromBaseDoc(Object list, Function<Map<String, Object>, T> fun) {
		var l = (List<Map<String, Object>>) list;
		return l.stream().map(fun).collect(Collectors.toList());
	}
}
