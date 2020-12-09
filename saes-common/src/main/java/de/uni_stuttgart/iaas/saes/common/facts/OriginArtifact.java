/*
* This file is part of the Serverless Application Extraction System (SAES)
*
* The Serverless Application Extraction System is licensed under under
* the Apache License, Version 2.0. Please see the included COPYING file
* for license information.
*/
package de.uni_stuttgart.iaas.saes.common.facts;

import java.util.Collections;
import java.util.HashMap;
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
public class OriginArtifact implements KnowledgeBaseObject {
	private static final DeserUtil DESER = new DeserUtil(OriginArtifact.class);

	String provider;
	/** optional! */
	String namespace;
	String resource;

	@Override
	public List<KnowledgeBaseObject> baseDocChildren() {
		return Collections.emptyList();
	}

	@Override
	public Map<String, Object> toBaseDoc() {
		/*
		 * namespace is optional, so we can't use Map.of which requires all objects to
		 * be non-null
		 */
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(Fields.provider, provider);
		map.put(Fields.namespace, namespace);
		map.put(Fields.resource, resource);
		return Collections.unmodifiableMap(map);
	}

	public static OriginArtifact fromBaseDoc(Map<String, Object> baseDoc) {
		DESER.assertContains(baseDoc, Fields.provider, Fields.resource);

		return new OriginArtifact(//
				(String) baseDoc.get(Fields.provider), //
				(String) baseDoc.get(Fields.namespace), //
				(String) baseDoc.get(Fields.resource));
	}
	
	
}
