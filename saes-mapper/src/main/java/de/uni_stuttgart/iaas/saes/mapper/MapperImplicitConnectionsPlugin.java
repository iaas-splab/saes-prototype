/*
* This file is part of the Serverless Application Extraction System (SAES)
*
* The Serverless Application Extraction System is licensed under under
* the Apache License, Version 2.0. Please see the included COPYING file
* for license information.
*/
package de.uni_stuttgart.iaas.saes.mapper;

import java.util.List;

import com.arangodb.ArangoCollection;
import com.arangodb.ArangoDatabase;
import com.google.gson.JsonElement;

public interface MapperImplicitConnectionsPlugin extends MapperPlugin {

	List<JsonElement> createConnections(ArangoDatabase db, String collectionName);

}
