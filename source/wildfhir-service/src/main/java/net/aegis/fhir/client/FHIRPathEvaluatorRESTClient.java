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

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.hl7.fhir.r4.formats.IParser.OutputStyle;
import org.hl7.fhir.r4.formats.JsonParser;
import org.hl7.fhir.r4.formats.XmlParser;
import org.hl7.fhir.r4.model.Parameters;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import net.aegis.fhir.model.Constants;
import net.aegis.fhir.service.CodeService;
import net.aegis.fhir.service.util.WebClientHelper;

/**
 * @author richard.ettema
 *
 */
@Stateless
public class FHIRPathEvaluatorRESTClient implements Serializable {

	private static final long serialVersionUID = 3054269412004514778L;

	private Logger log = Logger.getLogger("FHIRPathEvaluatorRESTClient");

	private @Inject
	CodeService codeService;

	/**
	 *
	 * @param inputParameters
	 * @param baseUrl
	 * @param contentType
	 * @return {@link Response}
	 * @throws Exception
	 */
	public Response evaluate(Parameters inputParameters, String baseUrl, String contentType, List<String> headers) throws Exception {

		log.fine("[START] FHIRPathEvaluatorRESTClient.evaluate() - contentType: " + contentType);

		Response operationResponse = null;

		ByteArrayOutputStream oResponse = new ByteArrayOutputStream();
		String sInputParameters = null;
        String responseFhirVersion = "";
        try {
        	responseFhirVersion = codeService.getCodeValue("supportedVersions");
			if (responseFhirVersion != null) {
				responseFhirVersion = "; fhirVersion=" + responseFhirVersion;
			}
        }
        catch (Exception e) {
			responseFhirVersion = "";
        }

		try {

			// Build Operation web target reference
			StringBuilder sbOperation = new StringBuilder(buildURL(baseUrl, "fhirpath/evaluate"));

			ResteasyClient client = WebClientHelper.createClientWihtoutHostVerification();
			//ResteasyClient client = new ResteasyClientBuilder().build();
			ResteasyWebTarget webTarget = client.target(sbOperation.toString());

			Builder targetBuilder = webTarget.request();

			if (contentType != null && contentType.toLowerCase().indexOf("json") >= 0) {
				targetBuilder = targetBuilder.accept("application/fhir+json" + Constants.CHARSET_UTF8_EXT + responseFhirVersion);
			}
			else {
				targetBuilder = targetBuilder.accept("application/fhir+xml" + Constants.CHARSET_UTF8_EXT + responseFhirVersion);
			}

			// Add any additional headers
			targetBuilder = addHeaders(targetBuilder, headers);

			log.info("evaluate operation uri: " + webTarget.getUri());

			if (inputParameters != null) {
				if (contentType != null && contentType.toLowerCase().indexOf("json") >= 0) {

					targetBuilder = targetBuilder.header(HttpHeaders.CONTENT_TYPE, "application/fhir+json" + Constants.CHARSET_UTF8_EXT + responseFhirVersion);

					JsonParser jsonParser = new JsonParser();
					jsonParser.setOutputStyle(OutputStyle.PRETTY);
					jsonParser.compose(oResponse, inputParameters);
					sInputParameters = oResponse.toString();

					log.info(sInputParameters);

					operationResponse = targetBuilder.post(Entity.entity(sInputParameters, "application/fhir+json" + Constants.CHARSET_UTF8_EXT + responseFhirVersion));

				}
				else {

					targetBuilder = targetBuilder.header(HttpHeaders.CONTENT_TYPE, "application/fhir+xml" + Constants.CHARSET_UTF8_EXT + responseFhirVersion);

					XmlParser xmlParser = new XmlParser();
					xmlParser.setOutputStyle(OutputStyle.PRETTY);
					xmlParser.compose(oResponse, inputParameters, true);
					sInputParameters = oResponse.toString();

					log.info(sInputParameters);

					operationResponse = targetBuilder.post(Entity.entity(sInputParameters, "application/fhir+xml" + Constants.CHARSET_UTF8_EXT + responseFhirVersion));
				}
			}
			else {
				throw new Exception("Missing input parameters!");
			}

			if (operationResponse.hasEntity()) {
				operationResponse.bufferEntity();
			}

			debugResponse(operationResponse);

		}
		catch (Exception e) {
			// Exception caught
			log.severe(e.getMessage());
			e.printStackTrace();
			throw e;
		}

		return operationResponse;
	}

	/**
	 * <p>
	 * Build a URL by joining the baseURL and resourceType together.<br/>
	 * Check baseURL for trailing {@code /}.
	 * </p>
	 *
	 * @param baseUrl
	 * @param resourceType
	 * @return url
	 */
	private String buildURL(String baseUrl, String resourceType) {
		String url = "";

		if (baseUrl.endsWith("/")) {
			url = baseUrl.substring(0, baseUrl.length() - 1);
		}
		else {
			url = baseUrl;
		}

		if (resourceType != null && !resourceType.isEmpty()) {
			url += "/" + resourceType;
		}

		return url;
	}

	/**
	 * @param targetBuilder
	 * @param headers
	 * @return Builder targetBuilder
	 * @throws Exception
	 */
	public Builder addHeaders(Builder targetBuilder, List<String> headers) throws Exception {

		log.fine("[START] ResourceRESTClient.addHeaders()");

		if (headers != null && !headers.isEmpty()) {
			int separator = -1;
			String headerName = null;
			String headerValue = null;

			for (String header : headers) {
				separator = header.indexOf(":");
				if (separator > -1) {
					headerName = header.substring(0, separator).trim();
					headerValue = header.substring(separator + 1).trim();
					log.info("  ++ Adding Header - " + headerName + ":" + headerValue);

					targetBuilder = targetBuilder.header(headerName, headerValue);
				}
			}
		}
		else {
			log.warning("ResourceRESTClient.addHeaders() - HEADERS EMPTY OR NULL");
		}

		return targetBuilder;
	}

	/**
	 * <p>
	 * Prints the contents of the supplied {@link Response}.<br/>
	 * Useful for debugging purposes.
	 * </p>
	 *
	 * @param response
	 */
	private void debugResponse(Response response) {

		if (response != null) {
			if (response.getHeaders() != null) {

				log.info("----- HTTP HEADERS (RESPONSE) -----");

				for (String key : response.getHeaders().keySet()) {
					log.info("header(" + key + ") is " + response.getHeaders().get(key).toString());
				}
			}

			log.info("----- RESPONSE STATUS -----");
			log.info(Integer.toString(response.getStatus()));

			log.info("----- PAYLOAD (ENTITY) -----");
			String entity = null;
			if (response.getStatus() == Response.Status.NOT_MODIFIED.getStatusCode()) {
				entity = Response.Status.NOT_MODIFIED.getReasonPhrase();
			} else {
				if (response.hasEntity()) {
					entity = response.readEntity(String.class);
				} else {
					entity = ">> NO ENTITY PAYLOAD <<";
				}
			}
			log.info(entity);

		}
		else {
			log.info("Response is NULL.");
		}
	}

}
