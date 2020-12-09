/*
* This file is part of the Serverless Application Extraction System (SAES)
*
* The Serverless Application Extraction System is licensed under under
* the Apache License, Version 2.0. Please see the included COPYING file
* for license information.
*/
package de.uni_stuttgart.iaas.saes.common.util;

/** A "business logic" exception */
public class SaesException extends RuntimeException {
	private static final long serialVersionUID = -1682846028590083145L;

	/** @see Exception#Exception() */
	public SaesException() {
	}

	/** @see Exception#Exception() */
	public SaesException(String message) {
		super(message);
	}

	/** @see Exception#Exception(Throwable) */
	public SaesException(Throwable cause) {
		super(cause);
	}

	/** @see Exception#Exception(String, Throwable) */
	public SaesException(String message, Throwable cause) {
		super(message, cause);
	}

}
