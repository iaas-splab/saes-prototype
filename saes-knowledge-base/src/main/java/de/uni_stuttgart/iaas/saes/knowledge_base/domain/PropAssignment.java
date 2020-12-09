/*
* This file is part of the Serverless Application Extraction System (SAES)
*
* The Serverless Application Extraction System is licensed under under
* the Apache License, Version 2.0. Please see the included COPYING file
* for license information.
*/
package de.uni_stuttgart.iaas.saes.knowledge_base.domain;

import java.util.Map;

import de.uni_stuttgart.iaas.saes.common.db.KnowledgeBaseObject;
import de.uni_stuttgart.iaas.saes.common.util.DeserUtil;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

@Data
@FieldNameConstants
public abstract class PropAssignment implements KnowledgeBaseObject {
	private static final DeserUtil DESER = new DeserUtil(PropAssignment.class);

	final String type;

	public static PropAssignment fromBaseDoc(Map<String, Object> baseDoc) {
		DESER.assertContains(baseDoc, Fields.type);
		Object type = baseDoc.get(PropAssignment.Fields.type);
		if (PropertyConstant.TYPEID.equals(type)) {
			return PropertyConstant.fromBaseDoc(baseDoc);
		} else if (JSONPathAccessor.TYPEID.equals(type)) {
			return JSONPathAccessor.fromBaseDoc(baseDoc);
		} else if (RegexFilter.TYPEID.equals(type)) {
			return RegexFilter.fromBaseDoc(baseDoc);
		} else if (LookupFilter.TYPEID.equals(type)) {
			return LookupFilter.fromBaseDoc(baseDoc);
		} else {
			throw new RuntimeException("Could not determine type of PropValue, unknown value " + String.valueOf(type));
		}
	}
}
