/*
* This file is part of the Serverless Application Extraction System (SAES)
*
* The Serverless Application Extraction System is licensed under under
* the Apache License, Version 2.0. Please see the included COPYING file
* for license information.
*/
package de.uni_stuttgart.iaas.saes.common.extr;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.arangodb.ArangoDB;

import de.uni_stuttgart.iaas.saes.common.db.KnowledgeBaseDAO;
import de.uni_stuttgart.iaas.saes.common.facts.Fact;

/** Superclass for the different extraction entry point classes */
public abstract class ExtractionRunner<T extends ExtractionPlugin> {

	private final List<T> plugins = new ArrayList<>();
	private final KnowledgeBaseDAO<Fact> factDAO;
	private final Properties properties;

	public ExtractionRunner(ArangoDB arangoDB, Properties properties) {
		var dbName = properties.getProperty("extraction-db-name", "extr");
		factDAO = new KnowledgeBaseDAO<Fact>(arangoDB.db(dbName), Fact.class);
		this.properties = properties;
	}
	
	public void registerPlugin(T plugin) {
		plugins.add(plugin);
	}
	
	/** run the extraction and insert facts into the database */
	public void run() {
		for (var plugin : plugins) {
			for (var fact : plugin.analyze(properties)) {
				factDAO.put("fact" + fact.hashCode(), fact);
			}
		}
	}

}
