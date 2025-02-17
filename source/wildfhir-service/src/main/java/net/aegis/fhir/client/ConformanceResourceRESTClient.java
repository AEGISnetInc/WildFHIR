/*
 * #%L
 * WildFHIR - wildfhir-service
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
package net.aegis.fhir.client;

import java.io.Serializable;
import java.util.logging.Logger;

import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import net.aegis.fhir.model.Constants;
import net.aegis.fhir.service.CodeService;
import net.aegis.fhir.service.util.WebClientHelper;

/**
 * @author richard.ettema
 * @author rizwan.tanoli
 *
 */
public class ConformanceResourceRESTClient implements Serializable {

	private static final long serialVersionUID = -6115544042011096017L;

	private Logger log = Logger.getLogger("ConformanceResourceRESTClient");

    private String fhirVersion = "; fhirVersion=4.0";

	/**
	 * Initialize codeService
	 */
	public ConformanceResourceRESTClient(CodeService codeService) {
		super();
		if (codeService != null) {
			try {
				fhirVersion = "; fhirVersion=" + codeService.getCodeValue("supportedVersions");
			} catch (Exception e) {
				fhirVersion = "";
				e.printStackTrace();
			}
		}
	}

	/**
	 * Return conformance resource using GET metadata
	 *
	 * @param baseUrl
	 * @param contentType
	 * @return {@link Response}
	 * @throws Exception
	 */
	public Response metadata(String baseUrl, String contentType)
			throws Exception {

		log.fine("[START] ConformanceResourceRESTClient.metadata()");

		Response conformanceResponse = null;

		try {

			// Conformance metadata read
			String sMetadata = formatBaseUrl(baseUrl) + "/metadata";
			ResteasyClient client = WebClientHelper.createClientWihtoutHostVerification();
			//ResteasyClient client = new ResteasyClientBuilder().build();
			ResteasyWebTarget webTarget = client.target(sMetadata);

			Builder targetBuilder = webTarget.request();

			if (contentType != null && contentType.toLowerCase().indexOf("json") >= 0) {
				targetBuilder = targetBuilder.accept("application/fhir+json" + Constants.CHARSET_UTF8_EXT + fhirVersion);
			}
			else {
				targetBuilder = targetBuilder.accept("application/fhir+xml" + Constants.CHARSET_UTF8_EXT + fhirVersion);
			}

			log.info("Conformance metadata request uri: " + webTarget.getUri());

			conformanceResponse = targetBuilder.get();

			// Expensive - only use for debugging
			// log.info("Conformance object returned: " +
			// conformanceResponse.readEntity(String.class));

		} catch (Exception e) {
			// Exception caught
			log.severe(e.getMessage());
			e.printStackTrace();
			throw e;
		}

		return conformanceResponse;
	}


	/**
	 *
	 * @param baseUrl
	 * @return formatted baseUrl
	 */
	private String formatBaseUrl(String baseUrl) {
		return ((baseUrl.endsWith("/")) ? baseUrl.substring(0,
				baseUrl.length() - 1) : baseUrl);
	}

}
