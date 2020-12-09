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
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldNameConstants;

@Data
@EqualsAndHashCode(callSuper = true)
@FieldNameConstants
public class LookupFilter extends PropAssignment implements KnowledgeBaseObject {
	private static final DeserUtil DESER = new DeserUtil(LookupFilter.class);

	public static final String TYPEID = "lookup";

	String in;
	PropAssignment wrapped;

	public LookupFilter() {
		super(TYPEID);
	}

	public LookupFilter(String in, PropAssignment wrappedValue) {
		this();
		this.in = in;
		this.wrapped = wrappedValue;
	}

	@Override
	public List<KnowledgeBaseObject> baseDocChildren() {
		return List.of(wrapped);
	}

	@Override
	public Map<String, Object> toBaseDoc() {
		return Map.of(//
				PropAssignment.Fields.type, type, //
				Fields.in, in, //
				Fields.wrapped, wrapped.toBaseDoc());
	}

	@SuppressWarnings("unchecked")
	public static LookupFilter fromBaseDoc(Map<String, Object> baseDoc) {
		baseDoc = DESER.ensureMap(baseDoc, Fields.wrapped);
		DESER.assertContains(baseDoc, Fields.in);
		return new LookupFilter(//
				(String) baseDoc.get(Fields.in), //
				PropAssignment.fromBaseDoc((Map<String, Object>) baseDoc.get(Fields.wrapped)));
	}
}
