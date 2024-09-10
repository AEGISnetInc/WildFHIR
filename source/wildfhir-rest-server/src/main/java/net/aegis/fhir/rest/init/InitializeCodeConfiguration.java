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

/**
 * @author richard.ettema
 *
 */
public class InitializeCodeConfiguration extends HttpServlet {

	private static final long serialVersionUID = -1855936600074695102L;

	private static final Logger log = LoggerFactory.getLogger(InitializeCodeConfiguration.class);

	private static final Map<String, String> envCodeMap = new HashMap<String, String>(Map.of(
		"WILDFHIR_CONDITIONALDELETE", "conditionalDelete",
		"WILDFHIR_CONDITIONALREAD", "conditionalRead",
		"WILDFHIR_CONDITIONALCREATE", "conditionalCreate",
		"WILDFHIR_CONDITIONALUPDATE", "conditionalUpdate",
		"WILDFHIR_CREATERESPONSEPAYLOAD", "createResponsePayload",
		"WILDFHIR_SEARCHRESPONSEPAYLOAD", "searchResponsePayload",
		"WILDFHIR_UPDATERESPONSEPAYLOAD", "updateResponsePayload"
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
			envValue = System.getenv(k);

			if (envValue != null && !envValue.isEmpty()) {
				// Get the code configuration record
				Code code = codeService.findCodeByName(v);

				if (code != null) {
					code.setValue(envValue);
					// Check for boolean value
					if (envValue.equals("true")) {
						code.setIntValue(1);
					}
					else if (envValue.equals("false")) {
						code.setIntValue(0);
					}
				}

				// Update code configuration record
				codeService.update(code);

				log.info("InitializeCodeConfiguration - " + v + " = " + envValue);
			}
			else {
				log.info("InitializeCodeConfiguration - " + v + " unchanged");
			}
		} catch (Exception e) {
			log.error("InitializeCodeConfiguration - " + v + " error! " + e.getMessage());
		}
	}

}
