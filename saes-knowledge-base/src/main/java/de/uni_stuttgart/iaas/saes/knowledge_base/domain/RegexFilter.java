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
public class RegexFilter extends PropAssignment implements KnowledgeBaseObject {
	private static final DeserUtil DESER = new DeserUtil(RegexFilter.class);
	public static final String TYPEID = "regex";

	String regex;
	String replacement;
	PropAssignment wrapped;

	public RegexFilter() {
		super(TYPEID);
	}

	public RegexFilter(String regex, String replacement, PropAssignment wrappedValue) {
		this();
		this.regex = regex;
		this.replacement = replacement;
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
				Fields.regex, regex, //
				Fields.replacement, replacement, //
				Fields.wrapped, wrapped.toBaseDoc());
	}

	@SuppressWarnings("unchecked")
	public static RegexFilter fromBaseDoc(Map<String, Object> baseDoc) {
		baseDoc = DESER.ensureMap(baseDoc, Fields.wrapped);
		DESER.assertContains(baseDoc, Fields.regex, Fields.replacement);
		return new RegexFilter(//
				(String) baseDoc.get(Fields.regex), //
				(String) baseDoc.get(Fields.replacement), //
				PropAssignment.fromBaseDoc((Map<String, Object>) baseDoc.get(Fields.wrapped)));
	}
}
