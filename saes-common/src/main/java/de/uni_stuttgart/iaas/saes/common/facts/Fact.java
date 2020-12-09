/*
* This file is part of the Serverless Application Extraction System (SAES)
*
* The Serverless Application Extraction System is licensed under under
* the Apache License, Version 2.0. Please see the included COPYING file
* for license information.
*/
package de.uni_stuttgart.iaas.saes.common.facts;

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
@DatabaseStorable("facts")
public class Fact implements KnowledgeBaseObject {
	private static final DeserUtil DESER = new DeserUtil(Fact.class);

	String type;
	OriginArtifact originArtifact;
	Map<String, Object> body;

	@Override
	public List<KnowledgeBaseObject> baseDocChildren() {
		return List.of(originArtifact);
	}

	@Override
	public Map<String, Object> toBaseDoc() {
		return Map.of(//
				Fields.type, type, //
				Fields.originArtifact, originArtifact.toBaseDoc(), //
				Fields.body, body);
	}

	@SuppressWarnings("unchecked")
	public static Fact fromBaseDoc(Map<String, Object> baseDoc) {
		baseDoc = DESER.ensureMap(baseDoc, Fields.originArtifact, Fields.body);
		DESER.assertContains(baseDoc, Fields.type);
		return new Fact(//
				(String) baseDoc.get(Fields.type), //
				OriginArtifact.fromBaseDoc((Map<String, Object>) baseDoc.get(Fields.originArtifact)), //
				(Map<String, Object>) baseDoc.get(Fields.body));
	}

}
