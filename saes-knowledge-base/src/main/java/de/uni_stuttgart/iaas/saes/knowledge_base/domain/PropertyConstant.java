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
public class PropertyConstant extends PropAssignment implements KnowledgeBaseObject {
	private static final DeserUtil DESER = new DeserUtil(PropertyConstant.class);
	public static final String TYPEID = "constant";

	String value;

	public PropertyConstant() {
		super(TYPEID);
	}

	public PropertyConstant(String value) {
		this();
		this.value = value;
	}

	@Override
	public List<KnowledgeBaseObject> baseDocChildren() {
		return Collections.emptyList();
	}

	@Override
	public Map<String, Object> toBaseDoc() {
		return Map.of(//
				PropAssignment.Fields.type, type, //
				Fields.value, value);
	}

	public static PropertyConstant fromBaseDoc(Map<String, Object> baseDoc) {
		DESER.assertContains(baseDoc, Fields.value);
		return new PropertyConstant(//
				(String) baseDoc.get(Fields.value));
	}
}
