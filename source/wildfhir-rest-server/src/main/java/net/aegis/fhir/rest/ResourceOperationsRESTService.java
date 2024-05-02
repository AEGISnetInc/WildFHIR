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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.Encoded;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import net.aegis.fhir.model.Constants;
import net.aegis.fhir.model.ResourceType;
import net.aegis.fhir.operation.ResourceOperationProxy;
import net.aegis.fhir.operation.ResourceOperationProxyObjectFactory;
import net.aegis.fhir.service.BatchService;
import net.aegis.fhir.service.CodeService;
import net.aegis.fhir.service.ConformanceService;
import net.aegis.fhir.service.ResourceService;
import net.aegis.fhir.service.ResourcemetadataService;
import net.aegis.fhir.service.TransactionService;
import net.aegis.fhir.service.util.ServicesUtil;
import net.aegis.fhir.service.util.UTCDateUtil;

import org.apache.commons.io.IOUtils;
import org.hl7.fhir.r4.formats.IParser.OutputStyle;
import org.hl7.fhir.r4.formats.JsonParser;
import org.hl7.fhir.r4.formats.XmlParser;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity;
import org.hl7.fhir.r4.model.OperationOutcome.IssueType;
import org.hl7.fhir.r4.model.OperationOutcome.OperationOutcomeIssueComponent;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Resource;

/**
 * JAX-RS Resource Operations Service
 * <p/>
 * This class produces the RESTful services for the HL7 FHIR Resource Operations.
 *
 * @author richard.ettema
 *
 */
@Path("/")
@ApplicationScoped
@Encoded
public class ResourceOperationsRESTService {

	@Inject
	private Logger log;

	@Inject
	BatchService batchService;

	@Inject
    CodeService codeService;

	@Inject
    ResourceService resourceService;

	@Inject
	ResourcemetadataService resourcemetadataService;

	@Inject
	TransactionService transactionService;

	@Inject
	UTCDateUtil utcDateUtil;

	@Context
	private UriInfo context;

	@Inject
	ConformanceService conformanceService;

	/**
	 * Global operations with expected search parameters
	 *
	 * @param request
	 * @param headers
	 * @param operationName
	 * @param resourceInputStream
	 * @return <code>Response</code>
	 */
	@GET
	@Path("{dollarSign : (\\$|%24)}{operationName}")
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, "application/fhir+xml", "application/fhir+json", "application/xml+fhir", "application/json+fhir", "text/xml", "text/json" })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, "application/fhir+xml", "application/fhir+json", "application/xml+fhir", "application/json+fhir", "text/xml", "text/json" })
	public Response globalGetOperation(@Context HttpServletRequest request, @Context HttpHeaders headers, @PathParam("dollarSign") String dollarSign, @PathParam("operationName") String operationName) {

		log.fine("[START] ResourceOperationsRESTService.globalGetOperation(" + operationName + ")");

		Response response = null;

		try {
			// Validate request fhir version with supported fhir version
			if (!ServicesUtil.INSTANCE.fhirVersionMatched(request, headers, codeService.findCodeValueByName("supportedVersions"))) {
				response = ServicesUtil.INSTANCE.fhirVersioMismatchedResponse(headers, codeService.findCodeValueByName("supportedVersions"), context);
			} else {
				response = resourceOperation(request, headers, "GET", null, null, operationName, null, false);
			}
		}
		catch (Exception e) {
			log.severe(e.getMessage());
		}

		return response;
	}

	/**
	 * Resource type operations with expected search parameters
	 *
	 * @param request
	 * @param headers
	 * @param resourceType
	 * @param operationName
	 * @param resourceInputStream
	 * @return <code>Response</code>
	 */
	@GET
	@Path("{resourceType}/{dollarSign : (\\$|%24)}{operationName}")
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, "application/fhir+xml", "application/fhir+json", "application/xml+fhir", "application/json+fhir", "text/xml", "text/json" })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, "application/fhir+xml", "application/fhir+json", "application/xml+fhir", "application/json+fhir", "text/xml", "text/json" })
	public Response resourceTypeGetOperation(@Context HttpServletRequest request, @Context HttpHeaders headers, @PathParam("resourceType") String resourceType, @PathParam("dollarSign") String dollarSign, @PathParam("operationName") String operationName) {

		log.fine("[START] ResourceOperationsRESTService.resourceTypeGetOperation(" + resourceType + ", " + operationName + ")");

		Response response = null;

		try {
			// Validate request fhir version with supported fhir version
			if (!ServicesUtil.INSTANCE.fhirVersionMatched(request, headers, codeService.findCodeValueByName("supportedVersions"))) {
				response = ServicesUtil.INSTANCE.fhirVersioMismatchedResponse(headers, codeService.findCodeValueByName("supportedVersions"), context);
			} else {
				response = resourceOperation(request, headers, "GET", resourceType, null, operationName, null, false);
			}
		}
		catch (Exception e) {
			log.severe(e.getMessage());
		}

		return response;
	}

	/**
	 * Resource instance operations with expected search parameters
	 *
	 * @param request
	 * @param headers
	 * @param resourceType
	 * @param resourceId
	 * @param operationName
	 * @param resourceInputStream
	 * @return <code>Response</code>
	 */
	@GET
	@Path("{resourceType}/{id}/{dollarSign : (\\$|%24)}{operationName}")
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, "application/fhir+xml", "application/fhir+json", "application/xml+fhir", "application/json+fhir", "text/xml", "text/json" })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, "application/fhir+xml", "application/fhir+json", "application/xml+fhir", "application/json+fhir", "text/xml", "text/json" })
	public Response resourceInstanceGetOperation(@Context HttpServletRequest request, @Context HttpHeaders headers, @PathParam("resourceType") String resourceType, @PathParam("id") String resourceId, @PathParam("dollarSign") String dollarSign, @PathParam("operationName") String operationName) {

		log.fine("[START] ResourceOperationsRESTService.resourceInstanceGetOperation(" + resourceType + ", " + resourceId + ", " + operationName + ")");

		Response response = null;

		try {
			// Validate request fhir version with supported fhir version
			if (!ServicesUtil.INSTANCE.fhirVersionMatched(request, headers, codeService.findCodeValueByName("supportedVersions"))) {
				response = ServicesUtil.INSTANCE.fhirVersioMismatchedResponse(headers, codeService.findCodeValueByName("supportedVersions"), context);
			} else {
				response = resourceOperation(request, headers, "GET", resourceType, resourceId, operationName, null, false);
			}
		}
		catch (Exception e) {
			log.severe(e.getMessage());
		}

		return response;
	}

	/**
	 * Global operations with expected Parameters payload
	 *
	 * Consumes check for MediaType.APPLICATION_OCTET_STREAM to support condition of undefined request Content-Type. JBoss RESTEasy
	 * framework defaults empty Content-Type to MediaType.APPLICATION_OCTET_STREAM.
	 *
	 * @param request
	 * @param headers
	 * @param operationName
	 * @param resourceInputStream
	 * @return <code>Response</code>
	 */
	@POST
	@Path("{dollarSign : (\\$|%24)}{operationName}")
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, "application/fhir+xml", "application/fhir+json", "application/xml+fhir", "application/json+fhir", "text/xml", "text/json", MediaType.APPLICATION_OCTET_STREAM })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, "application/fhir+xml", "application/fhir+json", "application/xml+fhir", "application/json+fhir", "text/xml", "text/json" })
	public Response globalPostOperation(@Context HttpServletRequest request, @Context HttpHeaders headers, @PathParam("dollarSign") String dollarSign, @PathParam("operationName") String operationName, InputStream resourceInputStream) {

		log.fine("[START] ResourceOperationsRESTService.globalPostOperation(" + operationName + ")");

		Response response = null;

		try {
			// Validate request fhir version with supported fhir version
			if (!ServicesUtil.INSTANCE.fhirVersionMatched(request, headers, codeService.findCodeValueByName("supportedVersions"))) {
				response = ServicesUtil.INSTANCE.fhirVersioMismatchedResponse(headers, codeService.findCodeValueByName("supportedVersions"), context);
			} else {
				response = resourceOperation(request, headers, "POST", null, null, operationName, resourceInputStream, true);
			}
		}
		catch (Exception e) {
			log.severe(e.getMessage());
		}

		return response;
	}

	/**
	 * Resource type operations with expected Parameters payload
	 *
	 * Consumes check for MediaType.APPLICATION_OCTET_STREAM to support condition of undefined request Content-Type. JBoss RESTEasy
	 * framework defaults empty Content-Type to MediaType.APPLICATION_OCTET_STREAM.
	 *
	 * @param request
	 * @param headers
	 * @param resourceType
	 * @param operationName
	 * @param resourceInputStream
	 * @return <code>Response</code>
	 */
	@POST
	@Path("{resourceType}/{dollarSign : (\\$|%24)}{operationName}")
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, "application/fhir+xml", "application/fhir+json", "application/xml+fhir", "application/json+fhir", "text/xml", "text/json", MediaType.APPLICATION_OCTET_STREAM })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, "application/fhir+xml", "application/fhir+json", "application/xml+fhir", "application/json+fhir", "text/xml", "text/json" })
	public Response resourceTypePostOperation(@Context HttpServletRequest request, @Context HttpHeaders headers, @PathParam("resourceType") String resourceType, @PathParam("dollarSign") String dollarSign, @PathParam("operationName") String operationName, InputStream resourceInputStream) {

		log.fine("[START] ResourceOperationsRESTService.resourceTypePostOperation(" + resourceType + ", " + operationName + ")");

		Response response = null;

		try {
			// Validate request fhir version with supported fhir version
			if (!ServicesUtil.INSTANCE.fhirVersionMatched(request, headers, codeService.findCodeValueByName("supportedVersions"))) {
				response = ServicesUtil.INSTANCE.fhirVersioMismatchedResponse(headers, codeService.findCodeValueByName("supportedVersions"), context);
			} else {
				response = resourceOperation(request, headers, "POST", resourceType, null, operationName, resourceInputStream, true);
			}
		}
		catch (Exception e) {
			log.severe(e.getMessage());
		}

		return response;
	}

	/**
	 * Resource instance operations with expected Parameters payload
	 *
	 * Consumes check for MediaType.APPLICATION_OCTET_STREAM to support condition of undefined request Content-Type. JBoss RESTEasy
	 * framework defaults empty Content-Type to MediaType.APPLICATION_OCTET_STREAM.
	 *
	 * @param request
	 * @param headers
	 * @param resourceType
	 * @param resourceId
	 * @param operationName
	 * @param resourceInputStream
	 * @return <code>Response</code>
	 */
	@POST
	@Path("{resourceType}/{id}/{dollarSign : (\\$|%24)}{operationName}")
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, "application/fhir+xml", "application/fhir+json", "application/xml+fhir", "application/json+fhir", "text/xml", "text/json", MediaType.APPLICATION_OCTET_STREAM })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, "application/fhir+xml", "application/fhir+json", "application/xml+fhir", "application/json+fhir", "text/xml", "text/json" })
	public Response resourceInstancePostOperation(@Context HttpServletRequest request, @Context HttpHeaders headers, @PathParam("resourceType") String resourceType, @PathParam("id") String resourceId, @PathParam("dollarSign") String dollarSign, @PathParam("operationName") String operationName, InputStream resourceInputStream) {

		log.fine("[START] ResourceOperationsRESTService.resourceInstancePostOperation(" + resourceType + ", " + resourceId + ", " + operationName + ")");

		Response response = null;

		try {
			// Validate request fhir version with supported fhir version
			if (!ServicesUtil.INSTANCE.fhirVersionMatched(request, headers, codeService.findCodeValueByName("supportedVersions"))) {
				response = ServicesUtil.INSTANCE.fhirVersioMismatchedResponse(headers, codeService.findCodeValueByName("supportedVersions"), context);
			} else {
				response = resourceOperation(request, headers, "POST", resourceType, resourceId, operationName, resourceInputStream, true);
			}
		}
		catch (Exception e) {
			log.severe(e.getMessage());
		}

		return response;
	}

	/**
	 * Operations with expected Parameters payload
	 *
	 * @param request
	 * @param headers
	 * @param resourceType
	 * @param resourceId
	 * @param operationName
	 * @param resourceInputStream
	 * @param isPost
	 * @return <code>Response</code>
	 */
	private Response resourceOperation(HttpServletRequest request, HttpHeaders headers, String httpOperation, String resourceType, String resourceId, String operationName, InputStream resourceInputStream, boolean isPost) {

		log.fine("[START] private ResourceOperationsRESTService.resourceOperation(" + httpOperation + ", " + resourceType + ", " + operationName + ")");

		log.fine("context.path = " + context.getPath());
		log.fine("context.baseuri.path = " + context.getBaseUri().getPath());
		log.fine("context.absolutePath = " + context.getAbsolutePath());

		debugRequest(request, headers, null);

		Response.ResponseBuilder builder = null;
        String contentType = null;
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

			log.fine("producesType = " + producesType);

			// Check for valid and supported ResourceType
			if ((resourceType == null && ResourceType.isSupportedGlobalOperation(operationName)) ||
					(ResourceType.isValidResourceType(resourceType) &&
					ResourceType.isValidOperationResourceType(resourceType) &&
					ResourceType.isSupportedResourceOperation(resourceType, operationName))
				) {

				// Get the content type based on the request Content-Type
				contentType = ServicesUtil.INSTANCE.getHttpHeader(headers, HttpHeaders.CONTENT_TYPE);

				// Validate input format check; instantiate the Parameters
				Parameters inputParameters = null;
				Resource inputResource = null;

				// convert input stream to String
				String payload = null;

				if (isPost && resourceInputStream != null) {
					// POST Operations with expected Parameters payload
					log.fine("POST Operations with expected Parameters payload");
					isPost = true;

					// convert input stream to String
					payload = IOUtils.toString(resourceInputStream, "UTF-8");

					if (payload != null && !payload.isEmpty() && payload.length() > 3) {
						if (contentType != null && contentType.indexOf("xml") >= 0) {
							// Convert XML contents to Resource
							XmlParser xmlP = new XmlParser();
							inputResource = (Resource) xmlP.parse(payload.getBytes());
						} else if (contentType != null && contentType.indexOf("json") >= 0) {
							// Convert JSON contents to Resource
							JsonParser jsonP = new JsonParser();
							inputResource = (Resource) jsonP.parse(payload.getBytes());
						} else {
							// contentType did not contain a valid media type or was null; attempt to determine based on starting character
							int firstValid = payload.indexOf("<"); // check for xml first
							if (firstValid > -1 && firstValid < 5) {
								if (firstValid > 0) {
									payload = payload.substring(firstValid);
								}
								// Convert XML contents to Resource
								contentType = "xml";
								XmlParser xmlP = new XmlParser();
								inputResource = (Resource) xmlP.parse(payload.getBytes());
							} else {
								firstValid = payload.indexOf("{"); // check for json next
								if (firstValid > -1 && firstValid < 5) {
									if (firstValid > 0) {
										payload = payload.substring(firstValid);
									}
									// Convert JSON contents to Resource
									contentType = "json";
									JsonParser jsonP = new JsonParser();
									inputResource = (Resource) jsonP.parse(payload.getBytes());
								}
							}
						}
					}
					else {
						payload = null;
					}

					if (inputResource != null && inputResource.getResourceType().equals(org.hl7.fhir.r4.model.ResourceType.Parameters)) {
						inputParameters = (Parameters) inputResource;
					}
				}

				/*
				 * Use Factory Pattern for execution of Resource Type operation
				 */
				ResourceOperationProxyObjectFactory operationFactory = new ResourceOperationProxyObjectFactory();

				ResourceOperationProxy operationProxy = operationFactory.getResourceOperationProxy(resourceType, operationName);

				// Test proxy
				if (operationProxy != null) {
					log.fine("Calling operationProxy.executeOperation()");

					StringBuffer returnedDirective = new StringBuffer("");

					String softwareVersion = getSoftwareVersion();
					log.fine("softwareVersion: " + softwareVersion);
					Parameters outputParameters = operationProxy.executeOperation(context, headers, resourceService, resourcemetadataService, batchService, transactionService, codeService, conformanceService, softwareVersion, resourceType, resourceId, inputParameters, inputResource, payload, contentType, isPost, returnedDirective);

					String locationPath = context.getPath();

					// Test output parameters
					if (outputParameters != null) {

						// Build response

						// Check number of parameters; if only one resource, return just that resource
						if (!outputParameters.hasParameter()) {
							// Something went wrong
							String outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.WARNING, OperationOutcome.IssueType.INCOMPLETE, "operation returned no parameters", null, null, producesType);

							builder = Response.status(Response.Status.NO_CONTENT).entity(outcome).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);
						}
						else if (outputParameters.hasParameter() && outputParameters.getParameter().size() == 1 && outputParameters.getParameter().get(0).hasResource() &&
								outputParameters.getParameter().get(0).hasName() && outputParameters.getParameter().get(0).getName().equals("return") &&
								!operationName.startsWith("meta")) {

							log.info("outputParameters contains only 1 parameter and operation is not meta* - building response with Resource only");

							builder = buildResource(operationName, locationPath, producesType, outputParameters.getParameter().get(0).getResource(), returnedDirective, responseFhirVersion);
						}
						else {
							log.info("outputParameters contains multiple parameters - building response with Parameters");

							builder = buildResource(operationName, locationPath, producesType, outputParameters, returnedDirective, responseFhirVersion);
						}
					}
					else {
						// Something went wrong
						String outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.TRANSIENT, "operation failure, no response returned", null, null, producesType);

						builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(outcome).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);
					}
				}
				else {
					// Something went wrong
					String outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.TRANSIENT, "operation failure, no service interface defined", null, null, producesType);

					builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(outcome).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);
				}
			}
			else {
				if (resourceType == null) {
					String outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.FATAL, OperationOutcome.IssueType.NOTSUPPORTED, "Server does not support this FHIR global operation.", null, null, producesType);

					builder = Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(outcome).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);
				}
				else {
					builder = responseInvalidResourceType(producesType, resourceType, operationName, responseFhirVersion);
				}
			}
        }
		catch (Exception e) {
			// Handle generic exceptions
			String outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.FATAL, OperationOutcome.IssueType.EXCEPTION, e.getMessage(), null, null, producesType);

			builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(outcome).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);

			e.printStackTrace();
		}

		return builder.build();
	}

	/**
	 *
	 * @param operationName
	 * @param locationPath
	 * @param producesType
	 * @param output
	 * @param returnedDirective
     * @param responseFhirVersion
	 * @return <code>Response.ResponseBuilder</code>
	 * @throws URISyntaxException
	 * @throws Exception
	 */
	private Response.ResponseBuilder buildResource(String operationName, String locationPath, String producesType, Resource output, StringBuffer returnedDirective, String responseFhirVersion) throws URISyntaxException, Exception {

		log.fine("[START] ResourceOperationsRESTService.buildResource()");

		Response.ResponseBuilder builder;
		String outcome = "";

		if (output != null) {
			log.fine("Output resource type is " + output.getResourceType().name());

			if (operationName.equalsIgnoreCase("document")) {
				// Special processing for $document operation
				if (output.getResourceType().name().equals("OperationOutcome")) {
					builder = Response.status(Response.Status.BAD_REQUEST).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);
				}
				else {
					// Check returnedDirective to verify document is persisted; if true, send response Location header
					if (returnedDirective != null && returnedDirective.length() > 1) {
						log.fine("Build Response - returnedDirective is '" + returnedDirective + "'");
						// Extract base url from locationPath for Location HTTP header
						String baseUrl = ServicesUtil.INSTANCE.extractBaseURL(locationPath, "Composition");
						URI locationHeader = new URI(baseUrl + output.fhirType() + "/" + output.getId());

						builder = Response.status(Response.Status.OK).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion).location(locationHeader);
					}
					else {
						log.fine("Build Response - returnedDirective is null");
						builder = Response.status(Response.Status.OK).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);
					}
				}
			}
			else if (operationName.equalsIgnoreCase("everything")) {
				// Special processing for $everything operation
				if (output.getResourceType().name().equals("OperationOutcome")) {
					builder = Response.status(Response.Status.CONFLICT).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);
				}
				else {
					builder = Response.status(Response.Status.OK).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);
				}
			}
			else if (operationName.equalsIgnoreCase("immds-forecast")) {
				// Special processing for $immds-forecast operation
				if (output.getResourceType().name().equals("OperationOutcome")) {
					OperationOutcome opOutcome = (OperationOutcome) output;
					// Check IssueType
					if (opOutcome.hasIssue() && opOutcome.getIssue().get(0).hasCode() && opOutcome.getIssue().get(0).getCode().equals(IssueType.INCOMPLETE)) {
						builder = Response.status(Response.Status.PRECONDITION_FAILED).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);
					}
					else if (opOutcome.hasIssue() && opOutcome.getIssue().get(0).hasCode() && opOutcome.getIssue().get(0).getCode().equals(IssueType.NOTFOUND)) {
						builder = Response.status(Response.Status.NOT_FOUND).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);
					}
					else {
						builder = Response.status(Response.Status.BAD_REQUEST).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);
					}
				}
				else {
					builder = Response.status(Response.Status.OK).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);
				}
			}
			else if (operationName.equalsIgnoreCase("process-message")) {
				// Special processing for $process-message operation
				if (output.getResourceType().name().equals("OperationOutcome")) {
					OperationOutcome opOutcome = (OperationOutcome) output;
					boolean isOk = true;
					// Check IssueSeverity
					if (opOutcome.hasIssue()) {
						for (OperationOutcomeIssueComponent issue : opOutcome.getIssue()) {
							if (issue.hasSeverity() && (issue.getSeverity().equals(IssueSeverity.ERROR) || issue.getSeverity().equals(IssueSeverity.FATAL))) {
								isOk = false;
								break;
							}
						}
					}
					if (isOk == true) {
						builder = Response.status(Response.Status.OK).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);
					}
					else {
						builder = Response.status(Response.Status.BAD_REQUEST).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);
					}
				}
				else {
					builder = Response.status(Response.Status.OK).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);
				}
			}
			else {
				builder = Response.status(Response.Status.OK).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);
			}

			// Define URI location
			URI resourceLocation = new URI(locationPath);
			builder = builder.contentLocation(resourceLocation);

			// Build response
			builder = responseStatus(producesType, output, builder);

		}
		else {
			// Something went wrong
			outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.TRANSIENT, "operation failure, no response", null, null, producesType);

			builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(outcome).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);
		}

		return builder;
	}

	/**
	 * @param producesType
	 * @param output
	 * @param builder
	 * @return <code>Response.ResponseBuilder</code>
	 * @throws Exception
	 */
	private Response.ResponseBuilder responseStatus(String producesType, Resource output, Response.ResponseBuilder builder) throws Exception {

		log.fine("[START] ResourceOperationsRESTService.responseStatus()");

		ByteArrayOutputStream oResource;

		String sParameters = "";

		if (producesType.indexOf("xml") >= 0) {
			// Convert Bundle to XML
			oResource = new ByteArrayOutputStream();
			XmlParser xmlParser = new XmlParser();
			xmlParser.setOutputStyle(OutputStyle.PRETTY);
			xmlParser.compose(oResource, output, true);
			sParameters = oResource.toString();
		}
		else {
			// Convert Bundle to JSON
			oResource = new ByteArrayOutputStream();
			JsonParser jsonParser = new JsonParser();
			jsonParser.setOutputStyle(OutputStyle.PRETTY);
			jsonParser.compose(oResource, output);
			sParameters = oResource.toString();
		}

		builder = builder.entity(sParameters);

		return builder;
	}

	/**
	 * @param resourceType
	 * @param builder
     * @param responseFhirVersion
	 * @return <code>Response.ResponseBuilder</code>
	 * @throws Exception
	 */
	private Response.ResponseBuilder responseInvalidResourceType(String producesType, String resourceType, String operationName, String responseFhirVersion) throws Exception {

		log.fine("[START] ResourceOperationsRESTService.responseInvalidResourceType()");

		String message = "";
		if (!ResourceType.isValidResourceType(resourceType)) {
			message = "Not a valid FHIR resource type!";
		}
		else {
			if (!ResourceType.isValidOperationResourceType(resourceType)) {
				message = "Server does not support this FHIR resource type for operations.";
			}
			else {
				if (!ResourceType.isSupportedResourceOperation(resourceType, operationName)) {
					message = "Server does not support this FHIR resource type and operation.";
				}
				else {
					message = "Server does not support this FHIR resource type.";
				}
			}
		}
		// Unsupported ResourceType
		String outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.FATAL, OperationOutcome.IssueType.NOTSUPPORTED, message, null, null, producesType);

		Response.ResponseBuilder builder = Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(outcome).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);

		return builder;
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

	private String getSoftwareVersion() throws IOException {
        InputStream inputStream = null;
        String softwareVersion = "";
        try {
            Properties properties = new Properties();
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            inputStream = loader.getResourceAsStream("application.properties");
            properties.load(inputStream);

            String versionNumber = (properties.getProperty("version.number") == null)?"":properties.getProperty("version.number");
            String buildNumber = (properties.getProperty("build.number") == null)?"":properties.getProperty("build.number");
            String buildTimestamp = (properties.getProperty("build.timestamp") == null)?"":properties.getProperty("build.timestamp");

            softwareVersion = versionNumber + " Build " + buildNumber + " [" + buildTimestamp + "]";
            log.fine("softwareVersion: " + softwareVersion);

        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return softwareVersion;
	}

}
