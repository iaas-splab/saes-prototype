/*
* This file is part of the Serverless Application Extraction System (SAES)
*
* The Serverless Application Extraction System is licensed under under
* the Apache License, Version 2.0. Please see the included COPYING file
* for license information.
*/
package de.uni_stuttgart.iaas.saes.common.db;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uni_stuttgart.iaas.saes.common.util.DeserUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@DatabaseStorable("examples")
public class ExampleEntity implements KnowledgeBaseObject {
	private static final DeserUtil DESER = new DeserUtil(ExampleEntity.class);

	String exampleProp;
	int n;
	ExampleEntity child;
	List<ExampleEntity> sub = new ArrayList<>();

	@Override
	public List<KnowledgeBaseObject> baseDocChildren() {
		var ret = new ArrayList<KnowledgeBaseObject>();
		if (null != child) {
			ret.add(child);
		}
		ret.addAll(sub);
		return Collections.unmodifiableList(ret);
	}

	@Override
	public Map<String, Object> toBaseDoc() {
		var ret = new HashMap<String, Object>();
		ret.put(Fields.n, n);
		ret.put(Fields.exampleProp, exampleProp);
		if (null != child) {
			ret.put(Fields.child, child.toBaseDoc());
		}
		ret.put(Fields.sub, KnowledgeBaseObject.convertList(sub));
		return Collections.unmodifiableMap(ret);
	}

	@SuppressWarnings("unchecked")
	public static ExampleEntity fromBaseDoc(Map<String, Object> baseDoc) {
		if (null == baseDoc) {
			return null;
		}
		baseDoc = DESER.ensureList(baseDoc, Fields.sub);
		DESER.assertContains(baseDoc, Fields.n, Fields.exampleProp, Fields.sub);

		return new ExampleEntity(//
				(String) baseDoc.get(Fields.exampleProp), //
				((Number) baseDoc.get(Fields.n)).intValue(), //
				ExampleEntity.fromBaseDoc((Map<String, Object>) baseDoc.get(Fields.child)), // s
				KnowledgeBaseObject.listFromBaseDoc(baseDoc.get(Fields.sub), ExampleEntity::fromBaseDoc));
	}
}
