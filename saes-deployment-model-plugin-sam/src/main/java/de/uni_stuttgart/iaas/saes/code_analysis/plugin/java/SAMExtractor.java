/*
* This file is part of the Serverless Application Extraction System (SAES)
*
* The Serverless Application Extraction System is licensed under under
* the Apache License, Version 2.0. Please see the included COPYING file
* for license information.
*/
package de.uni_stuttgart.iaas.saes.code_analysis.plugin.java;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.AbstractConstruct;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.Tag;

import de.uni_stuttgart.iaas.saes.common.facts.Fact;
import de.uni_stuttgart.iaas.saes.common.facts.OriginArtifact;
import de.uni_stuttgart.iaas.saes.common.util.SaesException;

public class SAMExtractor {

	private final Yaml yaml = new Yaml(new TagConstructor());

	public List<Fact> extractSAM(String model, String modelNamespace) {
		var res = new ArrayList<Fact>();

		@SuppressWarnings("unchecked")
		Map<String, Object> modelMap = yaml.loadAs(model, Map.class);
		if (!(modelMap.get("Resources") instanceof Map)) {
			throw new SaesException("Malformed SAM model");
		}
		@SuppressWarnings("unchecked")
		var resources = (Map<String, Map<String, Object>>) modelMap.get("Resources");
		for (var resourceName : resources.keySet()) {
			var resourceBody = resources.get(resourceName);
			var resourceType = (String) resourceBody.get("Type");
			res.add(new Fact("awssam/" + resourceType.replaceAll("^AWS::", "").replaceAll("::", "/").toLowerCase(),
					new OriginArtifact("AWS", "sam/model/" + modelNamespace, resourceName), resourceBody));
			System.out.println(resourceType + " " + res.get(res.size()-1).getType());
		}
		return res;
	}

	public static class TagConstructor extends Constructor {
		public TagConstructor() {
			yamlConstructors.put(new Tag("!Ref"), new TagConstruct("ref"));
			yamlConstructors.put(new Tag("!Sub"), new TagConstruct("sub"));
			yamlConstructors.put(new Tag("!GetAtt"), new TagConstruct("getatt"));
			yamlConstructors.put(new Tag("!Join"), new TagConstruct("join"));
		}

		private class TagConstruct extends AbstractConstruct {
			private final String tagName;
			public TagConstruct(String tagName) {
				this.tagName = tagName;
			}
			public Object construct(Node node) {
				if (!(node instanceof ScalarNode)) {
					return null;
				}
				var scalarNode = (ScalarNode) node;
				return Map.of("yaml-" + tagName, scalarNode.getValue());
			}
		}
	}
}
