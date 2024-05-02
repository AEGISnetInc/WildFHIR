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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.util.Date;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import net.aegis.fhir.model.Constants;
import net.aegis.fhir.model.ResourceContainer;
import net.aegis.fhir.service.CodeService;
import net.aegis.fhir.service.ConformanceService;
import net.aegis.fhir.service.util.ServicesUtil;
import net.aegis.fhir.service.util.UTCDateUtil;

import org.apache.commons.io.IOUtils;
import org.hl7.fhir.r4.formats.IParser.OutputStyle;
import org.hl7.fhir.r4.formats.JsonParser;
import org.hl7.fhir.r4.formats.XmlParser;
import org.hl7.fhir.r4.model.CapabilityStatement;
import org.hl7.fhir.r4.model.OperationOutcome;

/**
 * JAX-RS capabilities Service
 * <p/>
 * This class produces a RESTful service to return the contents of the CapabilityStatement Resource.
 */
@Path("")
@ApplicationScoped
public class ConformanceResourceRESTService {

	@Inject
	private Logger log;

	@Inject
	ConformanceService conformanceService;

	@Inject
	UTCDateUtil utcDateUtil;

	@Context
	private UriInfo context;

	@Inject
	CodeService codeService;

	/**
	 * The metadata capabilities interaction retrieves this server's FHIR CapabilityStatement.
	 *
	 * @param request
	 * @param headers
	 * @return <code>Response</code>
	 */
	@GET
	@Path("/metadata")
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, "application/fhir+xml", "application/fhir+json", "application/xml+fhir", "application/json+fhir", "text/xml", "text/json" })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, "application/fhir+xml", "application/fhir+json", "application/xml+fhir", "application/json+fhir", "text/xml", "text/json" })
	public Response fhirMetadata(@Context HttpServletRequest request, @Context HttpHeaders headers) {

		log.fine("[START] FHIR capabilities[metadata]");

		Response response = null;

		try {
			// Validate request fhir version with supported fhir version
			if (!ServicesUtil.INSTANCE.fhirVersionMatched(request, headers, codeService.findCodeValueByName("supportedVersions"))) {
				response = ServicesUtil.INSTANCE.fhirVersioMismatchedResponse(headers, codeService.findCodeValueByName("supportedVersions"), context);
			} else {
				response = getConformance(request, headers, true, "0");
			}
		}
		catch (Exception e) {
			log.severe(e.getMessage());
		}

		return response;
	}

	/*
	 * Private methods
	 */

	/**
	 * Get the CapabilityStatement resource for this server.
	 *
	 * @param headers
	 * @return <code>Response</code>
	 */
	private Response getConformance(HttpServletRequest request, HttpHeaders headers, boolean isMetadata, String resourceId) {

		log.fine("[START] ConformanceResourceRESTService.getConformance()");

		debugRequest(request, headers, null);

		Response.ResponseBuilder builder = null;
		ByteArrayInputStream iConformance = null;
		ByteArrayOutputStream oConformance = null;
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
			// Get the produces type based on the request Content-Type
			String producesType = ServicesUtil.INSTANCE.getProducesType(headers, context);

			ResourceContainer resourceContainer = conformanceService.read();

			if (resourceContainer != null) {
				String eTagVersion = resourceContainer.getConformance().getVersionId().toString();
				EntityTag eTag = new EntityTag(eTagVersion, true);

				builder = Response.status(resourceContainer.getResponseStatus()).tag(eTag).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);

				if (resourceContainer.getConformance() != null) {
					// Define URI location
					String locationPath = context.getBaseUri().getPath();
					if (isMetadata) {
						locationPath += "/metadata";
					}

					URI patientLocation = new URI(locationPath);
					builder = builder.contentLocation(patientLocation);

					// Get last update date
					Date lastUpdate = resourceContainer.getConformance().getLastUpdate();
					log.info("Last Update Date: " + lastUpdate);
					if (lastUpdate != null) {
						String sLastUpdate = utcDateUtil.formatUTCDateOffset(lastUpdate);
						log.info("Last Update UTC Date: " + sLastUpdate);
						builder = builder.header("Last-Modified", sLastUpdate);
					}

					if (resourceContainer.getResponseStatus().equals(Response.Status.OK)) {

						byte[] resourceContents = resourceContainer.getConformance().getResourceContents();

						if (resourceContents != null && resourceContents.length > 0) {

							if (producesType.contains("xml")) {
								builder = builder.entity(new String(resourceContents));
							}
							else {
								// Convert XML contents to JSON
								iConformance = new ByteArrayInputStream(resourceContents);
								XmlParser xmlP = new XmlParser();
								CapabilityStatement conformance = (CapabilityStatement) xmlP.parse(iConformance);

								oConformance = new ByteArrayOutputStream();
								JsonParser jsonParser = new JsonParser();
								jsonParser.setOutputStyle(OutputStyle.PRETTY);
								jsonParser.compose(oConformance, conformance);
								String sConformance = oConformance.toString();

								builder = builder.entity(sConformance);
							}
						}
						else {
							// Something went wrong
							String outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.EXCEPTION, "read failure, CapabilityStatement content not defined", null, null, producesType);
							builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(outcome).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);
						}
					}
					else {
						// Something went wrong
						String outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.EXCEPTION, "read failure, response status not ok", null, null, producesType);
						builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(outcome).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);
					}
				}
				else {
					// Something went wrong
					String outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.EXCEPTION, "read failure, conformance not found", null, null, producesType);
					builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(outcome).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);
				}
			}
			else {
				// Something went wrong
				String outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.EXCEPTION, "read failure, no response", null, null, producesType);
				builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(outcome).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);
			}
		}
		catch (Exception e) {
			// Handle generic exceptions
			e.printStackTrace();
			String outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.FATAL, OperationOutcome.IssueType.EXCEPTION, e.getMessage());
			builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(outcome).type("application/fhir+xml" + Constants.CHARSET_UTF8_EXT + responseFhirVersion);
		}
		finally {
			if (iConformance != null) {
				try {
					iConformance.close();
				}
				catch (IOException ioe) {
					log.warning("Exception closing ByteArrayInputStream: " + ioe.getMessage());
				}
			}
			if (oConformance != null) {
				try {
					oConformance.close();
				}
				catch (IOException ioe) {
					log.warning("Exception closing ByteArrayOutputStream: " + ioe.getMessage());
				}
			}
		}

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
	 * @param response
	 */
	private String debugRequest(HttpServletRequest request, HttpHeaders headers, InputStream resourceInputStream) {

		String payload = null;

		if (request != null) {
			log.info("----- HTTP REQUEST -----");

			log.info("Remote host is '" + (request.getRemoteHost() == null ? "NOT FOUND" : request.getRemoteHost()) + "'");
		}

		if (headers != null) {
			log.info("----- HTTP HEADERS (REQUEST) -----");

			MultivaluedMap<String, String> requestHeaders = headers.getRequestHeaders();

			if (requestHeaders != null) {

				for (String key : requestHeaders.keySet()) {

					for (String keyValue : requestHeaders.get(key)) {
						log.info("header(" + key + ") is " + keyValue);
					}
				}
			}
		}

		log.info("----- REQUEST URL -----");
		StringBuilder sbRequestUrl = new StringBuilder(context.getAbsolutePath().getPath());
		MultivaluedMap<String, String> queryParams = context.getQueryParameters();
		if (queryParams != null && !queryParams.isEmpty()) {
			sbRequestUrl.append("?");
			boolean first = true;
			for (String key : queryParams.keySet()) {
				if (!first) {
					sbRequestUrl.append("&");
				}
				log.info("header(" + key + ") is " + queryParams.get(key).toString());
				sbRequestUrl.append(key).append("=").append(queryParams.get(key).toString());
			}
		}
		log.info(sbRequestUrl.toString());

		log.info("----- PAYLOAD ----- [snipped; use fine logging]");
        if (resourceInputStream != null) {
			try {
				StringWriter writer = new StringWriter();
				String encoding = "UTF-8";
				IOUtils.copy(resourceInputStream, writer, encoding);
				payload = writer.toString();

				log.fine(payload);

			}
			catch (Exception e) {
				log.severe("Exception parsing payload! " + e.getMessage());
				e.printStackTrace();
			}
		}
		else {
			log.info(">> NO PAYLOAD <<");
		}

		return payload;
	}

}
