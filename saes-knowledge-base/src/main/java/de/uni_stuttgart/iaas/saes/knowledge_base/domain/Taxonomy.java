/*
* This file is part of the Serverless Application Extraction System (SAES)
*
* The Serverless Application Extraction System is licensed under under
* the Apache License, Version 2.0. Please see the included COPYING file
* for license information.
*/
package de.uni_stuttgart.iaas.saes.knowledge_base.domain;

import java.util.ArrayList;
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
@DatabaseStorable("taxonomies")
public class Taxonomy implements KnowledgeBaseObject {
	private static final DeserUtil DESER = new DeserUtil(Taxonomy.class);

	String name;
	List<TaxonomyEntry> entries = new ArrayList<>();

	@Override
	public List<KnowledgeBaseObject> baseDocChildren() {
		return List.copyOf(entries);
	}

	@Override
	public Map<String, Object> toBaseDoc() {
		return Map.of(//
				Fields.name, name, //
				Fields.entries, KnowledgeBaseObject.convertList(entries));
	}

	public static Taxonomy fromBaseDoc(Map<String, Object> baseDoc) {
		baseDoc = DESER.ensureList(baseDoc, Fields.entries);
		DESER.assertContains(baseDoc, Fields.name);
		return new Taxonomy(//
				(String) baseDoc.get(Fields.name), //
				KnowledgeBaseObject.listFromBaseDoc(baseDoc.get(Fields.entries), TaxonomyEntry::fromBaseDoc));
	}
}
