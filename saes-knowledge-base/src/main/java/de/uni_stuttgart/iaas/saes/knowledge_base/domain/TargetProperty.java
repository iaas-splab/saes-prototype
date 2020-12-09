/*
* This file is part of the Serverless Application Extraction System (SAES)
*
* The Serverless Application Extraction System is licensed under under
* the Apache License, Version 2.0. Please see the included COPYING file
* for license information.
*/
package de.uni_stuttgart.iaas.saes.knowledge_base.domain;

import java.util.List;
import java.util.Map;

import de.uni_stuttgart.iaas.saes.common.db.KnowledgeBaseObject;
import de.uni_stuttgart.iaas.saes.common.util.DeserUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
public class TargetProperty implements KnowledgeBaseObject {
	private static final DeserUtil DESER = new DeserUtil(TargetProperty.class);

	String name;
	PropAssignment value;

	@Override
	public List<KnowledgeBaseObject> baseDocChildren() {
		return List.of(value);
	}

	@Override
	public Map<String, Object> toBaseDoc() {
		return Map.of(//
				Fields.name, name, //
				Fields.value, value.toBaseDoc());
	}

	@SuppressWarnings("unchecked")
	public static TargetProperty fromBaseDoc(Map<String, Object> baseDoc) {
		baseDoc = DESER.ensureMap(baseDoc, Fields.value);
		DESER.assertContains(baseDoc, Fields.name);
		return new TargetProperty(//
				(String) baseDoc.get(Fields.name), //
				PropAssignment.fromBaseDoc((Map<String, Object>) baseDoc.get(Fields.value)));
	}
}
