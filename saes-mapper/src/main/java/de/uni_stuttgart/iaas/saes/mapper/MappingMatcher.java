/*
* This file is part of the Serverless Application Extraction System (SAES)
*
* The Serverless Application Extraction System is licensed under under
* the Apache License, Version 2.0. Please see the included COPYING file
* for license information.
*/
package de.uni_stuttgart.iaas.saes.mapper;

import java.util.Map;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.jayway.jsonpath.DocumentContext;

import de.uni_stuttgart.iaas.saes.common.facts.Fact;
import de.uni_stuttgart.iaas.saes.knowledge_base.domain.Mapping;

/**
 * Match mappings to facts
 * 
 * The wildcard-to-regex conversion logic implemented in this class is inspired
 * by Mark R. Hoffman's WildcardMatcher.java as committed to JaCoCo in 2009. The
 * concept has been reused, but the code has been re-implemented to be safe for
 * Unicode characters outside the basic multilingual plane (i.e. unicode
 * surrogate pairs are processed together.) Reference:
 * https://github.com/jacoco/jacoco/commit/b8ee4efe
 */
public class MappingMatcher {

	private static final int CODEP_ASTERISK = "*".codePointAt(0);
	private static final int CODEP_QUESTION = "?".codePointAt(0);
	private final String[] convCache = new String[1024];

	private Gson gson = new Gson();

	public boolean match(Fact fact, Mapping mapping) {
		DocumentContext factDoc = JPUtil.documentContext(gson, fact);
		for (Map.Entry<String, String> entry : mapping.getMatch().entrySet()) {
			String matchKey = entry.getKey();
			Object factValue;
			if (matchKey.startsWith("$")) {
				factValue = factDoc.read(matchKey);
			} else {
				factValue = fact.toBaseDoc().get(matchKey);
			}
			String matchValue = entry.getValue();

			if (!match(matchValue, factValue)) {
				return false; // mismatch
			}
		}
		return true; // no mismatches
	}

	/* package */ Pattern wildcardToPattern(String wildcard) {
		StringBuilder sb = new StringBuilder();
		wildcard.codePoints().mapToObj(this::globToRegex).forEach(sb::append);
		return Pattern.compile(sb.toString());
	}

	private boolean match(String wildcard, Object o) {
		if (o instanceof Iterable<?>) {
			System.out.println("matching array");
			var iter = (Iterable<?>) o;
			boolean match = false;
			for (Object var : iter) {
				if (var instanceof JsonElement) {
					if (matchWildcard(wildcard, jsonElementToString((JsonElement) var))) {
						match = true; // one array element matches
					}
				} else {
					if (matchWildcard(wildcard, String.valueOf(var))) {
						match = true; // one array element matches
					}
				}
			}
			return match;
		} else {
			if (o instanceof JsonElement) {
				return matchWildcard(wildcard, jsonElementToString((JsonElement) o));
			} else {
				return matchWildcard(wildcard, String.valueOf(o));
			}
		}
	}

	private String jsonElementToString(JsonElement e) {
		if (e.isJsonPrimitive()) {
			JsonPrimitive primitive = e.getAsJsonPrimitive();
			if (primitive.isNumber()) {
				return String.valueOf(primitive.getAsNumber());
			}
			if (primitive.isBoolean()) {
				return String.valueOf(primitive.getAsBoolean());
			}
			if (primitive.isString()) {
				return primitive.getAsString();
			}
		}
		return String.valueOf(e);
	}

	/* package */ boolean matchWildcard(String wildcard, String haystack) {
		return wildcardToPattern(wildcard).matcher(haystack).matches();
	}

	private String globToRegex(int codepoint) {
		if (codepoint < convCache.length) {
			String cacheEntry = convCache[codepoint];
			if (cacheEntry == null) {
				cacheEntry = convCache[codepoint] = globToRegexInner(codepoint);
			} else {
			}
			return cacheEntry;
		}
		return globToRegexInner(codepoint);
	}

	private String globToRegexInner(int codepoint) {
		if (codepoint == CODEP_ASTERISK) {
			return ".*";
		} else if (codepoint == CODEP_QUESTION) {
			return ".";
		} else {
			return Pattern.quote(new String(Character.toChars(codepoint), 0, 1));
		}
	}
}
