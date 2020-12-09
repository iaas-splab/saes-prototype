/*
* This file is part of the Serverless Application Extraction System (SAES)
*
* The Serverless Application Extraction System is licensed under under
* the Apache License, Version 2.0. Please see the included COPYING file
* for license information.
*/
package de.uni_stuttgart.iaas.saes.common.extr;

import java.util.List;
import java.util.Properties;

import de.uni_stuttgart.iaas.saes.common.facts.Fact;
import de.uni_stuttgart.iaas.saes.common.util.SaesException;

public interface ExtractionPlugin {
	List<Fact> analyze(Properties analysisProperties) throws SaesException;
}
