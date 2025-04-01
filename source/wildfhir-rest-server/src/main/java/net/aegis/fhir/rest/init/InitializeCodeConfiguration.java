/*
 * #%L
 * WildFHIR - wildfhir-model
 * %%
 * Copyright (C) 2024 AEGIS.net, Inc.
 * All rights reserved.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  - Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *  - Neither the name of AEGIS nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without specific
 *    prior written permission.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package net.aegis.fhir.rest.init;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.aegis.fhir.model.Code;
import net.aegis.fhir.service.CodeService;
import net.aegis.fhir.service.util.StringUtils;

/**
 * @author richard.ettema
 *
 */
public class InitializeCodeConfiguration extends HttpServlet {

	private static final long serialVersionUID = -1855936600074695102L;

	private static final Logger log = LoggerFactory.getLogger(InitializeCodeConfiguration.class);

	private static final Map<String, String> envCodeMap = new HashMap<String, String>(Map.ofEntries(
		Map.entry("WILDFHIR_BASEURL", "baseUrl"),
		Map.entry("WILDFHIR_CONDITIONALDELETE", "conditionalDelete"),
		Map.entry("WILDFHIR_CONDITIONALREAD", "conditionalRead"),
		Map.entry("WILDFHIR_CONDITIONALCREATE", "conditionalCreate"),
		Map.entry("WILDFHIR_CONDITIONALUPDATE", "conditionalUpdate"),
		Map.entry("WILDFHIR_CREATERESPONSEPAYLOAD", "createResponsePayload"),
		Map.entry("WILDFHIR_SEARCHRESPONSEPAYLOAD", "searchResponsePayload"),
		Map.entry("WILDFHIR_UPDATERESPONSEPAYLOAD", "updateResponsePayload"),
		Map.entry("WILDFHIR_RESOURCEPURGEALLENABLED", "resourcePurgeAllEnabled"),
		Map.entry("WILDFHIR_LASTNPROCESSEMPTYDATE", "lastnProcessEmptyDate"),
		Map.entry("WILDFHIR_LASTNEMPTYDATEVALUE", "lastnEmptyDateValue"),
		Map.entry("WILDFHIR_AUDITEVENTSERVICEENABLED", "auditEventServiceEnabled"),
		Map.entry("WILDFHIR_PROVENANCESERVICEENABLED", "provenanceServiceEnabled"),
		Map.entry("WILDFHIR_SUBSCRIPTIONSERVICEENABLED", "subscriptionServiceEnabled"),
		Map.entry("WILDFHIR_TXCONCURRENTLIMIT", "txConcurrentLimit")
	));

	@Inject
    CodeService codeService;

	public void init() throws ServletException {
		log.info("InitializeCodeConfiguration.init() - START");

		initCodeConfiguration();

		log.info("InitializeCodeConfiguration.init() - END");
	}

	public void initCodeConfiguration() {

		try {
			// Iterate through environment variable code configuration map
			envCodeMap.forEach((k, v) -> updateEnvCode(k, v));
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void updateEnvCode(String k, String v) {

		// Check for environment variables defining code configuration settings
		String envValue;

		try {
			// Get the code configuration record
			Code code = codeService.findCodeByName(v);

			envValue = System.getenv(k);

			if (code != null) {

				if (envValue != null && !envValue.isEmpty()) {

					// Check for boolean value
					if (envValue.equals("true")) {
						code.setValue(envValue);
						code.setIntValue(1);
					}
					else if (envValue.equals("false")) {
						code.setValue(envValue);
						code.setIntValue(0);
					}
					else if (StringUtils.isNumeric(envValue)) {
						Integer intValue = Integer.getInteger(envValue);
						code.setIntValue(intValue);
						if (intValue > 0) {
							code.setValue("true");
						}
						else {
							code.setValue("false");
						}
					}
					else {
						code.setValue(envValue);
						code.setIntValue(0);
					}

					// Update code configuration record
					codeService.update(code);

					log.info("Init Configuration - " + v + " = " + code.getValue() + " int = " + code.getIntValue());
				}
				else {
					log.info("Init Configuration - " + v + " = " + code.getValue() + " int = " + code.getIntValue() + " unchanged");
				}
			}
			else {
				log.warn("Init Configuration - " + v + " = " + " code not defined!");
			}
		} catch (Exception e) {
			log.error("InitializeCodeConfiguration - " + v + " error! " + e.getMessage());
		}
	}

}
