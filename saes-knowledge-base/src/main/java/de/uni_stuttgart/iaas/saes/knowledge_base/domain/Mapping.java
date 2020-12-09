/*
* This file is part of the Serverless Application Extraction System (SAES)
*
* The Serverless Application Extraction System is licensed under under
* the Apache License, Version 2.0. Please see the included COPYING file
* for license information.
*/
package de.uni_stuttgart.iaas.saes.knowledge_base.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uni_stuttgart.iaas.saes.common.db.DatabaseStorable;
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
@DatabaseStorable("mappings")
public class Mapping implements KnowledgeBaseObject {
	private static final DeserUtil DESER = new DeserUtil(Mapping.class);

	Map<String, String> match = new HashMap<>();
	String targetType;
	List<TargetProperty> targetProperties = new ArrayList<>();

	@Override
	public List<KnowledgeBaseObject> baseDocChildren() {
		return List.copyOf(targetProperties);
	}

	@Override
	public Map<String, Object> toBaseDoc() {
		return Map.of(//
				Fields.match, new HashMap<String, Object>(match), //
				Fields.targetType, targetType, //
				Fields.targetProperties, KnowledgeBaseObject.convertList(targetProperties));
	}

	@SuppressWarnings("unchecked")
	public static Mapping fromBaseDoc(Map<String, Object> baseDoc) {
		baseDoc = DESER.ensureMap(baseDoc, Fields.match);
		baseDoc = DESER.ensureList(baseDoc, Fields.targetProperties);
		DESER.assertContains(baseDoc, Fields.targetType);
		return new Mapping(//
				(Map<String, String>) baseDoc.get(Fields.match), //
				(String) baseDoc.get(Fields.targetType),
				KnowledgeBaseObject.listFromBaseDoc(baseDoc.get(Fields.targetProperties), TargetProperty::fromBaseDoc));
	}
}
