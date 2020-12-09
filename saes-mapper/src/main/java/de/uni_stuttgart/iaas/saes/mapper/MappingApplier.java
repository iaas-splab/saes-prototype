/*
* This file is part of the Serverless Application Extraction System (SAES)
*
* The Serverless Application Extraction System is licensed under under
* the Apache License, Version 2.0. Please see the included COPYING file
* for license information.
*/
package de.uni_stuttgart.iaas.saes.mapper;

import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.jayway.jsonpath.DocumentContext;

import de.uni_stuttgart.iaas.saes.common.facts.Fact;
import de.uni_stuttgart.iaas.saes.common.util.SaesException;
import de.uni_stuttgart.iaas.saes.knowledge_base.domain.JSONPathAccessor;
import de.uni_stuttgart.iaas.saes.knowledge_base.domain.LookupFilter;
import de.uni_stuttgart.iaas.saes.knowledge_base.domain.Mapping;
import de.uni_stuttgart.iaas.saes.knowledge_base.domain.PropAssignment;
import de.uni_stuttgart.iaas.saes.knowledge_base.domain.PropertyConstant;
import de.uni_stuttgart.iaas.saes.knowledge_base.domain.RegexFilter;
import de.uni_stuttgart.iaas.saes.knowledge_base.domain.Taxonomy;

/** The MappingApplier transforms facts into model elements using mappings */
public class MappingApplier {
	private final Map<String, String> missingTaxonomy = Collections.unmodifiableMap(new HashMap<>());
	private final Function<String, Taxonomy> taxonomyGetter;
	private final Gson gson = new Gson();
	private final Map<RegexFilter, Pattern> compliedRegexCache = new IdentityHashMap<>();
	private final Map<String, Map<String, String>> taxonomyCache = new HashMap<>();

	public MappingApplier(Function<String, Taxonomy> taxonomyGetter) {
		this.taxonomyGetter = taxonomyGetter;
	}

	public JsonElement apply(Fact fact, Mapping mapping) {
		var factDoc = JPUtil.documentContext(gson, fact);
		var res = new JsonObject();
		res.addProperty("type", mapping.getTargetType());
		for (var prop : mapping.getTargetProperties()) {
			res.add(prop.getName(), resolvePropAssignment(factDoc, prop.getValue()));
		}
		return res;
	}

	private JsonElement resolvePropAssignment(DocumentContext fact, PropAssignment assn) {
		if (assn instanceof PropertyConstant) {
			var casted = (PropertyConstant) assn;
			return new JsonPrimitive(casted.getValue());
		} else if (assn instanceof JSONPathAccessor) {
			var casted = (JSONPathAccessor) assn;
			return fact.read(casted.getJsonPath());
		} else if (assn instanceof RegexFilter) {
			var casted = (RegexFilter) assn;
			var pattern = compileCachedRegex(casted);
			var wrapped = resolvePropAssignment(fact, casted.getWrapped());
			var transformed = pattern.matcher(wrapped.getAsString()).replaceFirst(casted.getReplacement());
			return new JsonPrimitive(transformed);
		} else if (assn instanceof LookupFilter) {
			var casted = (LookupFilter) assn;
			var taxonomy = compileCachedTaxonomy(casted.getIn());
			var wrapped = resolvePropAssignment(fact, casted.getWrapped());
			var transformed = taxonomy.get(wrapped.getAsString());
			if (transformed != null) {
				return new JsonPrimitive(transformed);
			} else {
				return new JsonPrimitive("(unmatched) " + wrapped.getAsString());
			}
		} else {
			throw new SaesException("Unknown prop assignment type");
		}
	}

	private Pattern compileCachedRegex(RegexFilter filter) {
		Pattern p = compliedRegexCache.get(filter);
		if (p == null) {
			p = Pattern.compile(filter.getRegex());
			compliedRegexCache.put(filter, p);
		}
		return p;
	}

	private Map<String, String> compileCachedTaxonomy(String name) {
		Map<String, String> m = taxonomyCache.get(name);
		if (m == null) {
			Taxonomy taxonomy = taxonomyGetter.apply(name);
			if (taxonomy == null) {
				m = missingTaxonomy;
			} else {
				m = taxonomy.getEntries().stream()
						.collect(Collectors.toMap(e -> e.getFromValue(), e -> e.getToValue(), (v1, v2) -> v1));
			}
			taxonomyCache.put(name, m);
		}
		return m;
	}
}
