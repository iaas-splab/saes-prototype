/*
* This file is part of the Serverless Application Extraction System (SAES)
*
* The Serverless Application Extraction System is licensed under under
* the Apache License, Version 2.0. Please see the included COPYING file
* for license information.
*/
package de.uni_stuttgart.iaas.saes.knowledge_base.domain;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import de.uni_stuttgart.iaas.saes.common.db.KnowledgeBaseObject;
import de.uni_stuttgart.iaas.saes.common.util.DeserUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldNameConstants;

@Data
@EqualsAndHashCode(callSuper = true)
@FieldNameConstants
public class JSONPathAccessor extends PropAssignment implements KnowledgeBaseObject {
	private static final DeserUtil DESER = new DeserUtil(JSONPathAccessor.class);

	public static final String TYPEID = "jsonPath";

	String jsonPath;
	boolean expandList = false;

	public JSONPathAccessor() {
		super(TYPEID);
	}

	public JSONPathAccessor(String value) {
		this();
		this.jsonPath = value;
	}

	public JSONPathAccessor(String value, boolean expandList) {
		this(value);
		this.expandList = expandList;
	}

	@Override
	public List<KnowledgeBaseObject> baseDocChildren() {
		return Collections.emptyList();
	}

	@Override
	public Map<String, Object> toBaseDoc() {
		return Map.of(//
				PropAssignment.Fields.type, type, //
				Fields.expandList, expandList, //
				Fields.jsonPath, jsonPath);
	}

	public static JSONPathAccessor fromBaseDoc(Map<String, Object> baseDoc) {
		DESER.assertContains(baseDoc, Fields.jsonPath);
		boolean expandList = false;
		if (baseDoc.get(Fields.expandList) != null) {
			expandList = (Boolean) baseDoc.get(Fields.expandList);
		}
		return new JSONPathAccessor(//
				(String) baseDoc.get(Fields.jsonPath),//
				expandList);
	}
}
