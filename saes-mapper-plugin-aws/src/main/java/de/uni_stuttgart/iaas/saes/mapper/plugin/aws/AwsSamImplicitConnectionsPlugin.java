/*
* This file is part of the Serverless Application Extraction System (SAES)
*
* The Serverless Application Extraction System is licensed under under
* the Apache License, Version 2.0. Please see the included COPYING file
* for license information.
*/
package de.uni_stuttgart.iaas.saes.mapper.plugin.aws;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arangodb.ArangoDatabase;
import com.arangodb.entity.BaseDocument;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import de.uni_stuttgart.iaas.saes.mapper.MapperImplicitConnectionsPlugin;

/**
 * Plugin to create implicit connections on model elements extracted from a AWS
 * SAM model
 */
public class AwsSamImplicitConnectionsPlugin implements MapperImplicitConnectionsPlugin {
	private static final Logger LOG = LoggerFactory.getLogger(AwsSamImplicitConnectionsPlugin.class);

	private Gson gson = new Gson();

	@SuppressWarnings("unchecked")
	@Override
	public List<JsonElement> createConnections(ArangoDatabase db, String collectionName) {
		var res = new ArrayList<JsonElement>();

		var list = db.query("FOR t in @@col RETURN t", Map.of("@col", collectionName), BaseDocument.class)
				.asListRemaining();
		LOG.info("creating implicit connections");
		for (var elementBaseDoc : list) {
			var function = elementBaseDoc.getProperties();
			LOG.info("element of type {}", function.get("type"));
			LOG.info(" {}", function);
			try {
				var events = (Map<String, Object>) function.get("_events");
				if (events == null || !events.containsKey("s3Notification")) {
					continue;
				}
				var s3Notification = (Map<String, Object>) events.get("s3Notification");
				var notifProperties = (Map<String, Object>) s3Notification.get("Properties");
				var propBucket = (Map<String, String>) notifProperties.get("Bucket");
				var yamlRef = propBucket.get("yaml-ref");
				if (yamlRef == null) {
					continue;
				}
				var otherList = db.query("FOR t in @@col FILTER t[\"_yamlrefkey\"] == @ref RETURN t",
						Map.of("@col", collectionName, "ref", yamlRef), BaseDocument.class).asListRemaining();
				if (otherList.size() == 0) {
					LOG.info("none found");
					continue;
				}
				LOG.info("found {}", otherList.size());
				var referenced = otherList.get(0);
				res.add(gson.toJsonTree(//
						Map.of(//
								"source", referenced.getId(), //
								"target", elementBaseDoc.getId())//
				));
			} catch (ClassCastException | NullPointerException e) {
				e.printStackTrace();
				continue;
			}
		}
		return res;
	}

}
