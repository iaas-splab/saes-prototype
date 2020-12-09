/*
* This file is part of the Serverless Application Extraction System (SAES)
*
* The Serverless Application Extraction System is licensed under under
* the Apache License, Version 2.0. Please see the included COPYING file
* for license information.
*/
package de.uni_stuttgart.iaas.saes.common.util;

import java.util.Properties;

public class SaesProperties extends Properties {
	private static final long serialVersionUID = -4169011247423098322L;

	public SaesProperties() {
		super();
	}

	public SaesProperties(int initialCapacity) {
		super(initialCapacity);
	}

	public SaesProperties(Properties defaults) {
		super(defaults);
	}
	
	public String requireProperty(String key) throws SaesException {
		var stored = getProperty(key);
		if (stored == null) {
			throw new SaesException("Required property '" + key + "' not specified.");
		}
		return stored;
	}

}
