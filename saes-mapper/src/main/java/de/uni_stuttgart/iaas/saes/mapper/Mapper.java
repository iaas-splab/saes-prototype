/*
* This file is part of the Serverless Application Extraction System (SAES)
*
* The Serverless Application Extraction System is licensed under under
* the Apache License, Version 2.0. Please see the included COPYING file
* for license information.
*/
package de.uni_stuttgart.iaas.saes.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.arangodb.ArangoCollection;
import com.arangodb.ArangoDB;
import com.arangodb.ArangoDatabase;
import com.arangodb.entity.BaseDocument;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import de.uni_stuttgart.iaas.saes.common.db.KnowledgeBaseDAO;
import de.uni_stuttgart.iaas.saes.common.db.TypedBaseDocument;
import de.uni_stuttgart.iaas.saes.common.facts.Fact;
import de.uni_stuttgart.iaas.saes.common.util.SaesException;
import de.uni_stuttgart.iaas.saes.knowledge_base.domain.Mapping;
import de.uni_stuttgart.iaas.saes.knowledge_base.domain.Taxonomy;

public class Mapper {

	private final KnowledgeBaseDAO<Fact> factDAO;
	private final KnowledgeBaseDAO<Mapping> mappingDAO;
	private final KnowledgeBaseDAO<Taxonomy> taxonomyDAO;

	private final MappingApplier applier;
	private final MappingMatcher matcher;

	private final ArangoDatabase modelDb;
	private final String collectionNameModel;
	private final Gson gson = new Gson();
	
	private final List<MapperImplicitConnectionsPlugin> implicitConnectionPlugins = new ArrayList<>();

	public Mapper(ArangoDB arangoDB, Properties properties) {
		var dbNameExtr = properties.getProperty("extraction-db-name", "extr");
		var dbNameKB = properties.getProperty("knowledge-base-db-name", "kb");
		var dbNameModel = properties.getProperty("model-db-name", "model");
		collectionNameModel = properties.getProperty("model-collection-name", "elements");
		factDAO = new KnowledgeBaseDAO<>(arangoDB.db(dbNameExtr), Fact.class);
		mappingDAO = new KnowledgeBaseDAO<>(arangoDB.db(dbNameKB), Mapping.class);
		taxonomyDAO = new KnowledgeBaseDAO<>(arangoDB.db(dbNameKB), Taxonomy.class);
		modelDb = arangoDB.db(dbNameModel);

		applier = new MappingApplier(this::getTaxonomy);
		matcher = new MappingMatcher();
	}

	@SuppressWarnings("unchecked")
	public void run() {
		ArangoCollection collection = modelDb.collection(collectionNameModel);
		if (!collection.exists()) {
			collection.create();
		}
		for (TypedBaseDocument<Fact> factBaseDoc : factDAO) {
			var fact = factBaseDoc.getBody();
			Mapping matched = null;
			System.out.println(" Fact of type " + fact.getType());
			for (TypedBaseDocument<Mapping> mappingBaseDoc : mappingDAO.lookForIn(fact.getType(),
					Mapping.Fields.match, "type")) {
				var mapping = mappingBaseDoc.getBody();
				if (matcher.match(fact, mapping)) {
					matched = mapping;
				}
				break;
			}
			if (matched == null) {
				// no mapping found
				continue;
			}
			JsonElement e = applier.apply(fact, matched);
			var modelElement = new BaseDocument(factBaseDoc.getKey() + "-mapped");
			modelElement.setProperties(gson.fromJson(e, Map.class));
			collection.insertDocument(modelElement);
		}
	}

	private Taxonomy getTaxonomy(String name) {
		if (name == null) {
			throw new SaesException("null taxonomy requested");
		}
		var res = taxonomyDAO.lookForIn(name, Taxonomy.Fields.name);
		if (res.size() < 1) {
			throw new SaesException("taxonomy " + name + " not found");
		}
		if (res.size() > 1) {
			throw new SaesException("taxonomy " + name + " not unique");
		}
		return res.get(0).getBody();
	}
	
	public List<JsonElement> implicitConnections() {
		var res = new ArrayList<JsonElement>();
		for (var plugin : implicitConnectionPlugins) {
			res.addAll(plugin.createConnections(modelDb, collectionNameModel));
		}
		return res;
	}
	public List<JsonElement> explicitConnections() {
		return List.of(); // TOOD
	}

	public void registerPlugin(MapperPlugin plugin) {
		if (plugin instanceof MapperImplicitConnectionsPlugin) {
			implicitConnectionPlugins.add((MapperImplicitConnectionsPlugin) plugin);
		}
	}

}
