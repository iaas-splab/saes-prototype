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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
public class TaxonomyEntry implements KnowledgeBaseObject {
	private static final DeserUtil DESER = new DeserUtil(TaxonomyEntry.class);
	
	String fromValue;
	String toValue;

	@Override
	public List<KnowledgeBaseObject> baseDocChildren() {
		return Collections.emptyList();
	}

	@Override
	public Map<String, Object> toBaseDoc() {
		return Map.of(//
				Fields.fromValue, fromValue, //
				Fields.toValue, toValue);
	}

	public static TaxonomyEntry fromBaseDoc(Map<String, Object> baseDoc) {
		DESER.assertContains(baseDoc, Fields.fromValue, Fields.toValue);
		return new TaxonomyEntry(//
				(String) baseDoc.get(Fields.fromValue), //
				(String) baseDoc.get(Fields.toValue));
	}
}
