/*
* This file is part of the Serverless Application Extraction System (SAES)
*
* The Serverless Application Extraction System is licensed under under
* the Apache License, Version 2.0. Please see the included COPYING file
* for license information.
*/
package de.uni_stuttgart.iaas.saes.common.db;

import static org.junit.jupiter.api.Assertions.*;
//import static org.junit.jupiter.api.Assumptions.*;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentest4j.TestAbortedException;

import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;
import com.arangodb.ArangoDatabase;
import com.arangodb.entity.BaseDocument;

/** this is not supposed to be an actual useful test. It's example code for using the ArangoDB binding. */
class ArangoDBExampleCode {

	@BeforeEach
	void setUp() throws Exception {
	}

	@Test
	void test() {

		ArangoDB arangoDB = new ArangoDB.Builder().build();
		try {
			arangoDB.getEngine();
		} catch (ArangoDBException e) {
			throw new TestAbortedException("db not available", e);
		}
		if (arangoDB.getAccessibleDatabases().contains("tdb-common")) {
			assertTrue(arangoDB.db("tdb-common").drop());
		}
		assertTrue(arangoDB.createDatabase("tdb-common"));
		
		ArangoDatabase db = arangoDB.db("tdb-common");
		assertFalse(db.getCollections().stream().filter(c -> "testcollection".equals(c.getName())).findAny().isPresent());
		assertNotNull(db.createCollection("testcollection"));
		assertTrue(db.getCollections().stream().filter(c -> "testcollection".equals(c.getName())).findAny().isPresent());
		
		for (int i = 0; i < 10; i++) {
			BaseDocument bd = new BaseDocument("docckey" + i);
			bd.addAttribute("x", Map.of("y", String.valueOf(i)));
			db.collection("testcollection").insertDocument(bd);
		}
		
		var l = db.query("FOR t IN @@col FILTER t.x.y == @p RETURN t", Map.of("@col", "testcollection", "p", "4"), BaseDocument.class).asListRemaining();
		assertEquals(1, l.size(), "incorrect number of results");
		assertEquals("docckey4", l.get(0).getKey(), "incorrect key");
		
		assertTrue(arangoDB.db("tdb-common").drop());
	}

}
