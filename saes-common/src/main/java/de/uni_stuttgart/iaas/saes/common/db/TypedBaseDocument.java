/*
* This file is part of the Serverless Application Extraction System (SAES)
*
* The Serverless Application Extraction System is licensed under under
* the Apache License, Version 2.0. Please see the included COPYING file
* for license information.
*/
package de.uni_stuttgart.iaas.saes.common.db;

import lombok.Value;

@Value
public class TypedBaseDocument<T> {

	String id;
	String key;
	String revision;
	T body;
}
