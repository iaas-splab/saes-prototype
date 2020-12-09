/*
* This file is part of the Serverless Application Extraction System (SAES)
*
* The Serverless Application Extraction System is licensed under under
* the Apache License, Version 2.0. Please see the included COPYING file
* for license information.
*/
package de.uni_stuttgart.iaas.saes.common.db;

import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDatabase;
import com.arangodb.entity.BaseDocument;

public class KnowledgeBaseDAO<T extends KnowledgeBaseObject> implements Iterable<TypedBaseDocument<T>> {

	private final ArangoDatabase db;
	private final String collection;
	// private final Function<Map<String, Object>, T> fromBaseDoc;
	private final MethodHandle fromBaseDocInvoker;

	public KnowledgeBaseDAO(ArangoDatabase db, Class<T> clazz) {
		try {
			this.db = db;
			DatabaseStorable ds = clazz.getAnnotation(DatabaseStorable.class);
			if (ds == null) {
				throw new IllegalArgumentException(clazz + " is not annotated with DatabaseStorable");
			}
			collection = Objects.requireNonNull(ds.value(), "collection annotation value");

			CallSite cs = new ConstantCallSite(
					MethodHandles.lookup().findStatic(clazz, "fromBaseDoc", MethodType.methodType(clazz, Map.class)));
			fromBaseDocInvoker = cs.dynamicInvoker();
//			fromBaseDoc = m -> {
//				try {
//					return (T) invoker.invoke(m);
//				} catch (Throwable e) {
//					if (e instanceof RuntimeException) {
//						throw (RuntimeException) e;
//					}
//					throw new RuntimeException(e);
//				}
//			};

			if (db.getCollections().stream().filter(c -> collection.equals(c.getName())).findAny().isEmpty()) {
				db.createCollection(collection);
			}
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException(clazz + " has no fromBaseDoc(Map) method");
		} catch (SecurityException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	public void deleteAll() {
		var keys = new ArrayList<String>();
		for (TypedBaseDocument<T> tbd : this) {
			keys.add(tbd.getKey());
		}
		db.collection(collection).deleteDocuments(keys);
	}

	public void put(String key, T val) {
		var baseDoc = new BaseDocument(key);
		baseDoc.setProperties(val.toBaseDoc());
		db.collection(collection).insertDocument(baseDoc);
	}

	private T fromBaseDoc(Map<String, Object> map) {
		try {
			return (T) fromBaseDocInvoker.invoke(map);
		} catch (Throwable e) {
			if (e instanceof RuntimeException) {
				throw (RuntimeException) e;
			}
			throw new RuntimeException(e);
		}
	}

	public List<TypedBaseDocument<T>> lookForIn(Object value, String key) {
		var queryResult = innerLookForIn(value, key);
		var ret = new ArrayList<TypedBaseDocument<T>>();
		for (var baseDoc : queryResult) {
			ret.add(new TypedBaseDocument<T>(baseDoc.getId(), baseDoc.getKey(), baseDoc.getRevision(),
					fromBaseDoc(baseDoc.getProperties())));
		}
		return ret;
	}

	public Map<String, T> mapLookForIn(Object value, String key) {
		var queryResult = innerLookForIn(value, key);
		var ret = new HashMap<String, T>();
		for (var baseDoc : queryResult) {
			ret.put(baseDoc.getKey(), fromBaseDoc(baseDoc.getProperties()));
		}
		return ret;
	}

	public List<TypedBaseDocument<T>> lookForIn(Object value, Object key1, Object key2, Object... keyRest) {
		var queryResult = innerLookForIn(value, key1, key2, keyRest);
		var ret = new ArrayList<TypedBaseDocument<T>>();
		for (var baseDoc : queryResult) {
			ret.add(new TypedBaseDocument<T>(baseDoc.getId(), baseDoc.getKey(), baseDoc.getRevision(),
					fromBaseDoc(baseDoc.getProperties())));
		}
		return ret;
	}

	public Map<String, T> mapLookForIn(Object value, Object key1, Object key2, Object... keyRest) {
		var queryResult = innerLookForIn(value, key1, key2, keyRest);
		var ret = new HashMap<String, T>();
		for (var baseDoc : queryResult) {
			ret.put(baseDoc.getKey(), fromBaseDoc(baseDoc.getProperties()));
		}
		return ret;
	}

	public Iterator<TypedBaseDocument<T>> iterator() {
		var innerIterator = db.query("FOR t in @@col RETURN t", Map.of("@col", collection), BaseDocument.class)
				.iterator();
		return new Iterator<TypedBaseDocument<T>>() {

			@Override
			public boolean hasNext() {
				return innerIterator.hasNext();
			}

			@Override
			public TypedBaseDocument<T> next() {
				var baseDoc = innerIterator.next();
				return new TypedBaseDocument<T>(baseDoc.getId(), baseDoc.getKey(), baseDoc.getRevision(),
						fromBaseDoc(baseDoc.getProperties()));
			}
		};
	}

	private ArangoCursor<BaseDocument> innerLookForIn(Object value, String key) {
		var queryResult = db.query("FOR t IN @@col FILTER t[@key] == @val RETURN t", //
				Map.of(//
						"@col", collection, //
						"key", key, //
						"val", value//
				), BaseDocument.class);
		return queryResult;
	}

	private ArangoCursor<BaseDocument> innerLookForIn(Object value, Object key1, Object key2, Object... keyRest) {
		var queryBuilder = new StringBuilder("FOR t IN @@col FILTER t");
		var criteria = new HashMap<String, Object>(Map.of(//
				"@col", collection, //
				"val", value//
		));
		queryBuilder.append("[@key1]");
		criteria.put("key1", key1);
		queryBuilder.append("[@key2]");
		criteria.put("key2", key2);
		for (int i = 0; i < keyRest.length; i++) {
			queryBuilder.append("[@key" + (i + 3) + "]");
			criteria.put("key" + (i + 3), keyRest[i]);
		}
		queryBuilder.append(" == @val RETURN t");

		var queryResult = db.query(queryBuilder.toString(), criteria, BaseDocument.class);
		return queryResult;
	}

}
