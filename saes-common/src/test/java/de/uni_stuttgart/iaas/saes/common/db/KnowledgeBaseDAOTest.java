/*
* This file is part of the Serverless Application Extraction System (SAES)
*
* The Serverless Application Extraction System is licensed under under
* the Apache License, Version 2.0. Please see the included COPYING file
* for license information.
*/
package de.uni_stuttgart.iaas.saes.common.db;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentest4j.TestAbortedException;

import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;
import com.arangodb.ArangoDatabase;
import com.google.gson.Gson;

class KnowledgeBaseDAOTest {

	private static final String DBNAME = "tdb-examples";
	static ArangoDB arangoDB;
	static ArangoDatabase db;
	Gson gson;

	KnowledgeBaseDAO<ExampleEntity> dao;

	ExampleEntity eA;
	ExampleEntity eB;
	ExampleEntity eB1;
	ExampleEntity eB2;
	ExampleEntity eC;
	ExampleEntity eCc;
	ExampleEntity eC1;
	ExampleEntity eC2;

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		arangoDB = new ArangoDB.Builder().build();
		try {
			arangoDB.getEngine();
		} catch (ArangoDBException e) {
			throw new TestAbortedException("db not available", e);
		}

		if (arangoDB.getAccessibleDatabases().contains(DBNAME)) {
			arangoDB.db(DBNAME).drop();
		}
		arangoDB.createDatabase(DBNAME);
		db = arangoDB.db(DBNAME);
	}

	@BeforeEach
	void setUp() throws Exception {
		gson = new Gson();

		dao = new KnowledgeBaseDAO<>(db, ExampleEntity.class);

		dao.put("a", eA = new ExampleEntity("xa", 5, null, List.of()));
		dao.put("b", eB = new ExampleEntity("xb", 5, null, List.of(eB1 = new ExampleEntity("xb-1", 7, null, List.of()),
				eB2 = new ExampleEntity("xb-2", 8, null, List.of()))));
		dao.put("c",
				eC = new ExampleEntity("xc", 6, eCc = new ExampleEntity("xc-c", 99, null, List.of()),
						List.of(eC1 = new ExampleEntity("xc-1", 8, null, List.of()),
								eC2 = new ExampleEntity("xc-2", 7, null, List.of()))));
	}

	@AfterEach
	void tearDown() throws Exception {
		db.collection("examples").drop();
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
		if (db != null)
			db.drop();
	}

	@Test
	void testMapLookForInSingle() throws Exception {
		var lookup5n = dao.mapLookForIn(5, "n");
		assertEquals(2, lookup5n.size());
		assertNotNull(lookup5n.get("a"));
		assertNotNull(lookup5n.get("b"));
		assertEquals(
				ExampleEntity.fromBaseDoc(
						gson.<Map<String, Object>>fromJson("{\"sub\":[],\"exampleProp\":\"xa\",\"n\":5}", Map.class)), //
				lookup5n.get("a"));
		assertEquals(eA, lookup5n.get("a"));
		assertEquals(eB, lookup5n.get("b"));
		assertEquals(eB1, lookup5n.get("b").getSub().get(0));
		assertEquals(eB2, lookup5n.get("b").getSub().get(1));
	}

	@Test
	void testLookForInSingle() throws Exception {
		var lookup6n = dao.lookForIn(6, ExampleEntity.Fields.n);
		assertEquals(1, lookup6n.size());
		assertNotNull(lookup6n.get(0));
		assertEquals(eC, lookup6n.get(0).getBody());
		assertEquals(eCc, lookup6n.get(0).getBody().getChild());
		assertEquals(eC1, lookup6n.get(0).getBody().getSub().get(0));
		assertEquals(eC2, lookup6n.get(0).getBody().getSub().get(1));
	}

	@Test
	void testMapLookForInMultiple() throws Exception {
		var lookup99cn = dao.mapLookForIn(99, ExampleEntity.Fields.child, ExampleEntity.Fields.n);
		assertEquals(eC, lookup99cn.get("c"));

		var lookupArray0 = dao.mapLookForIn(8, ExampleEntity.Fields.sub, 0, ExampleEntity.Fields.n);
		assertEquals(1, lookupArray0.size());
		assertEquals(eC, lookupArray0.values().iterator().next());
	}

	@Test
	void testLookForInMultiple() throws Exception {
		var lookupArray1 = dao.lookForIn(8, ExampleEntity.Fields.sub, 1, ExampleEntity.Fields.n);
		assertEquals(1, lookupArray1.size());
		assertEquals(eB, lookupArray1.get(0).getBody());
	}

	@Test
	void testIterate() {
		var s = new HashSet<String>();
		for (var baseDoc : dao) {
			s.add(baseDoc.getBody().getExampleProp());
		}
		assertEquals(Set.of("xa", "xb", "xc"), s);
	}

}
