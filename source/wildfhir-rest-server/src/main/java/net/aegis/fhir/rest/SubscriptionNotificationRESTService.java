/*
 * #%L
 * WildFHIR - wildfhir-rest-server
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
package net.aegis.fhir.rest;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Encoded;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import org.apache.commons.io.IOUtils;
import org.hl7.fhir.r4.model.OperationOutcome;

import net.aegis.fhir.model.Constants;
import net.aegis.fhir.service.CodeService;
import net.aegis.fhir.service.util.ServicesUtil;

/**
 * JAX-RS Subscription Notification Service
 * <p/>
 * This class produces the RESTful services for the Subscription Notification Service Operations.
 *
 * @author richard.ettema
 *
 */
@Path("/")
@ApplicationScoped
@Encoded
public class SubscriptionNotificationRESTService {

	private static final Logger log = Logger.getLogger("SubscriptionNotificationRESTService");

	@Inject
    CodeService codeService;

	@Context
	private UriInfo context;

	/**
	 * Subscription Notification POST operation where the expected payload is a FHIR searchset Bundle
	 * containing the query results of the Subscription criteria.
	 *
	 * @param request
	 * @param headers
	 * @param ui
	 * @param validateInputStream
	 * @return <code>Response</code> containing a FHIR FHIR searchset Bundle payload
	 */
	@Path("/notification-r5backport")
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, "application/fhir+xml", "application/fhir+json", "application/xml+fhir", "application/json+fhir", "text/xml", "text/json" })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, "application/fhir+xml", "application/fhir+json", "application/xml+fhir", "application/json+fhir", "text/xml", "text/json" })
	public Response notificationR5BackportPost(@Context HttpServletRequest request, @Context HttpHeaders headers, @Context UriInfo ui, InputStream notifyInputStream) {

		log.info("[START] SubscriptionNotificationRESTService.notificationR5BackportPost()");

		debugRequest(request, headers, notifyInputStream);

		Response.ResponseBuilder builder = null;
		String producesType = null;
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
			// Get the produces type based on the request Accept
			producesType = ServicesUtil.INSTANCE.getProducesType(headers, context);

			/*
			 * TODO Expect searchset Bundle based on Subscription criteria
			 */

			// Default success (for now)
			String outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.INFORMATION, OperationOutcome.IssueType.INFORMATIONAL,
					"default success", null, null, producesType);

			builder = Response.status(Response.Status.OK).entity(outcome).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);
		}
		catch (Exception e) {
			// Write exception to server log first
			e.printStackTrace();

			// Handle generic exceptions
			String outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.FATAL, OperationOutcome.IssueType.EXCEPTION, e.getMessage());

			builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(outcome).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);
		}

		log.info("[END] SubscriptionNotificationRESTService.notificationR5BackportPost()");

		return builder.build();
	}

	/**
	 * <p>
	 * Prints the contents of the received request.<br/>
	 * Useful for debugging purposes.
	 * </p>
	 *
	 * @param request
	 * @param headers
	 * @param validateInputStream
	 * @return payload
	 */
	private String debugRequest(HttpServletRequest request, HttpHeaders headers, InputStream notifyInputStream) {

		StringBuilder debugString = new StringBuilder();
		String payload = null;

		if (request != null) {
			debugString.append("----- HTTP REQUEST -----\n");

			debugString.append("Remote host is '" + (request.getRemoteHost() == null ? "NOT FOUND" : request.getRemoteHost()) + "'\n");
		}

		if (headers != null) {
			debugString.append("----- HTTP HEADERS (REQUEST) -----\n");

			MultivaluedMap<String, String> requestHeaders = headers.getRequestHeaders();

			if (requestHeaders != null) {

				for (String key : requestHeaders.keySet()) {

					for (String keyValue : requestHeaders.get(key)) {
						debugString.append("header(" + key + ") is " + keyValue + "\n");
					}
				}
			}
		}

		debugString.append("----- REQUEST URL -----\n");
		StringBuilder sbRequestUrl = new StringBuilder(context.getAbsolutePath().getPath());
		MultivaluedMap<String, String> queryParams = context.getQueryParameters();
		if (queryParams != null && !queryParams.isEmpty()) {
			sbRequestUrl.append("?");
			boolean first = true;
			for (String key : queryParams.keySet()) {
				if (!first) {
					sbRequestUrl.append("&");
				}
				debugString.append("param(" + key + ") is " + queryParams.get(key).toString() + "\n");
				sbRequestUrl.append(key).append("=").append(queryParams.get(key).toString());
			}
		}
		debugString.append("Absolute Path: " + sbRequestUrl.toString() + "\n");
		debugString.append("Request URL: " + context.getRequestUri().toString() + "\n");

		debugString.append("----- PAYLOAD -----\n");
		if (notifyInputStream != null) {
			try {
				StringWriter writer = new StringWriter();
				String encoding = "UTF-8";
				IOUtils.copy(notifyInputStream, writer, encoding);
				payload = writer.toString();

				debugString.append(payload);
				debugString.append("\n");

			}
			catch (Exception e) {
				log.severe("Exception parsing payload! " + e.getMessage());
				e.printStackTrace();
			}
		}
		else {
			debugString.append(">> NO PAYLOAD OR SNIPPED <<\n");
		}

		// log level FINE used for debug
		log.info(debugString.toString());

		return payload;
	}

}
