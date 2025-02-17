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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import net.aegis.fhir.service.CodeService;
import net.aegis.fhir.service.RESTBatchTransactionOps;
import net.aegis.fhir.service.RESTResourceOps;
import net.aegis.fhir.service.util.ServicesUtil;

import org.apache.commons.io.IOUtils;

/**
 * JAX-RS Resource Service
 * <p/>
 * This class produces the RESTful services for the HL7 FHIR operations of any Resource.
 *
 * @author richard.ettema
 *
 */
@Path("/")
@ApplicationScoped
public class ResourceRESTService {

	private Logger log = Logger.getLogger("ResourceRESTService");

	@Inject
	CodeService codeService;

	@Inject
	RESTBatchTransactionOps batchTransactionOps;

	@Inject
	RESTResourceOps resourceOps;

	@Context
	private UriInfo context;

	/**
	 * This method supports when the base path is invoked and handles two use cases:
	 * <ul>
	 * <li>A request is made to the base path with search parameters. This will result in a global search of all
	 * resource instances.</li>
	 * <li>A request is made to just the base path. This will result in a re-direct to the client web application.</li>
	 * </ul>
	 *
	 * @param request
	 * @param headers
	 * @param ui
	 * @return <code>Response</code>
	 */
	@GET
	@Path("/")
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, "application/fhir+xml", "application/fhir+json", "application/xml+fhir", "application/json+fhir", "text/xml", "text/json" })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, "application/fhir+xml", "application/fhir+json", "application/xml+fhir", "application/json+fhir", "text/xml", "text/json" })
	public Response baseUrl(@Context HttpServletRequest request, @Context HttpHeaders headers, @Context UriInfo ui) {

		log.fine("[START] ResourceRESTService.baseUrl()");

		Response response = null;

		debugRequest(request, headers, null);

		// Get the query parameters that represent the search criteria
		MultivaluedMap<String, String> queryParams = ui.getQueryParameters();

		if (queryParams != null && queryParams.size() > 0) {
			/*
			 * Global Search
			 */
			try {
				// Validate request fhir version with supported fhir version
				if (!ServicesUtil.INSTANCE.fhirVersionMatched(request, headers, codeService.findCodeValueByName("supportedVersions"))) {
					response = ServicesUtil.INSTANCE.fhirVersioMismatchedResponse(headers, codeService.findCodeValueByName("supportedVersions"), context);
				} else {
					response = resourceOps.search(headers, ui, null, null);
				}
			}
			catch (Exception e) {
				log.severe(e.getMessage());
			}
		}
		else {
			/*
			 * Re-direct to conformance metadata
			 */
			String locationPath = ui.getAbsolutePath().toString();
			log.info("Absolute Path is " + locationPath);

			String lastChar = locationPath.substring(locationPath.length() - 1, locationPath.length());
			if (lastChar.equals("/")) {
				locationPath = locationPath.substring(0, locationPath.length() - 1);
				log.info("Absolute Path is " + locationPath);
			}

			Response.ResponseBuilder builder;

			try {
				builder = Response.seeOther(new URI(locationPath + "/metadata"));
				response = builder.build();
			}
			catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}

		return response;
	}

	/**
	 * This returns a single instance with the content specified for the resource type.
	 *
	 * @param request
	 * @param headers
	 * @param ui
	 * @param resourceType
	 * @param resourceId
	 * @return <code>Response</code>
	 */
	@GET
	@Path("{resourceType}/{id}")
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, "application/fhir+xml", "application/fhir+json", "application/xml+fhir", "application/json+fhir", "text/xml", "text/json" })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, "application/fhir+xml", "application/fhir+json", "application/xml+fhir", "application/json+fhir", "text/xml", "text/json" })
	public Response read(@Context HttpServletRequest request, @Context HttpHeaders headers, @Context UriInfo ui, @PathParam("resourceType") String resourceType, @PathParam("id") String resourceId) {

		log.fine("[START] ResourceRESTService.read(" + resourceType + ", " + resourceId + ")");

		Response response = null;

		debugRequest(request, headers, null);

		/*
		 * Check for GET search using "_search" - INVALID ACCORDING TO THE SPEC! BUT IT SEEMS SOME CLIENTS ARE SENDING THIS
		 */
		if (resourceId.equals("_search")) {
			try {
				// Validate request fhir version with supported fhir version
				if (!ServicesUtil.INSTANCE.fhirVersionMatched(request, headers, codeService.findCodeValueByName("supportedVersions"))) {
					response = ServicesUtil.INSTANCE.fhirVersioMismatchedResponse(headers, codeService.findCodeValueByName("supportedVersions"), context);
				} else {
					response = resourceOps.search(headers, ui, null, resourceType);
				}
			}
			catch (Exception e) {
				response = null;
				log.severe(e.getMessage());
			}
		}
		else {
			try {
				// Validate request fhir version with supported fhir version
				if(!ServicesUtil.INSTANCE.fhirVersionMatched(request, headers, codeService.findCodeValueByName("supportedVersions"))) {
					response = ServicesUtil.INSTANCE.fhirVersioMismatchedResponse(headers, codeService.findCodeValueByName("supportedVersions"), context);
				} else {
					response = resourceOps.resourceTypeRead(context, headers, null, null, resourceId, resourceType);
				}
			}
			catch (Exception e) {
				log.severe(e.getMessage());
			}
		}

		return response;
	}

	/**
	 * This returns a single instance with the content specified for the resource type.
	 *
	 * @param request
	 * @param headers
	 * @param ui
	 * @param compartment
	 * @param resourceId
	 * @param resourceType
	 * @return <code>Response</code>
	 */
	@GET
	@Path("{compartment}/{id}/{resourceType}")
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, "application/fhir+xml", "application/fhir+json", "application/xml+fhir", "application/json+fhir", "text/xml", "text/json" })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, "application/fhir+xml", "application/fhir+json", "application/xml+fhir", "application/json+fhir", "text/xml", "text/json" })
	public Response compartment(@Context HttpServletRequest request, @Context HttpHeaders headers, @Context UriInfo ui, @PathParam("compartment") String compartment, @PathParam("id") String resourceId, @PathParam("resourceType") String resourceType) {

		log.fine("[START] ResourceRESTService.compartment(" + compartment + ", " + resourceId + ", " + resourceType + ")");

		Response response = null;

		debugRequest(request, headers, null);

		try {
			// Validate request fhir version with supported fhir version
			if (!ServicesUtil.INSTANCE.fhirVersionMatched(request, headers, codeService.findCodeValueByName("supportedVersions"))) {
				response = ServicesUtil.INSTANCE.fhirVersioMismatchedResponse(headers, codeService.findCodeValueByName("supportedVersions"), context);
			} else {
				response = resourceOps.compartment(request, headers, ui, compartment, resourceId, resourceType);
			}
		}
		catch (Exception e) {
			log.severe(e.getMessage());
		}

		return response;
	}

	/**
	 * This returns a single instance with the content specified for the resource type for that version of the resource.
	 *
	 * @param request
	 * @param headers
	 * @param resourceType
	 * @param resourceId
	 * @param versionId
	 * @return <code>Response</code>
	 */
	@GET
	@Path("{resourceType}/{id}/_history/{vid}")
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, "application/fhir+xml", "application/fhir+json", "application/xml+fhir", "application/json+fhir", "text/xml", "text/json" })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, "application/fhir+xml", "application/fhir+json", "application/xml+fhir", "application/json+fhir", "text/xml", "text/json" })
	public Response vread(@Context HttpServletRequest request, @Context HttpHeaders headers, @PathParam("resourceType") String resourceType, @PathParam("id") String resourceId, @PathParam("vid") String versionId) {

		log.fine("[START] ResourceRESTService.vread(" + resourceType + ", " + resourceId + ", " + versionId + ")");

		Response response = null;

		debugRequest(request, headers, null);

		try {
			// Validate request fhir version with supported fhir version
			if (!ServicesUtil.INSTANCE.fhirVersionMatched(request, headers, codeService.findCodeValueByName("supportedVersions"))) {
				response = ServicesUtil.INSTANCE.fhirVersioMismatchedResponse(headers, codeService.findCodeValueByName("supportedVersions"), context);
			} else {
				response = resourceOps.resourceTypeVRead(context, headers, resourceId, versionId, resourceType);
			}
		}
		catch (Exception e) {
			log.severe(e.getMessage());
		}

		return response;
	}

	/**
	 * The update interaction creates a new current version for an existing resource or creates a new resource if no
	 * resource already exists for the given id.
	 *
	 * Consumes check for MediaType.APPLICATION_OCTET_STREAM to support condition of undefined request Content-Type. JBoss RESTEasy
	 * framework defaults empty Content-Type to MediaType.APPLICATION_OCTET_STREAM.
	 *
	 * @param request
	 * @param headers
	 * @param resourceType
	 * @param resourceId
	 * @param resourceInputStream
	 * @return <code>Response</code>
	 */
	@PUT
	@Path("{resourceType}/{id}")
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, "application/fhir+xml", "application/fhir+json", "application/xml+fhir", "application/json+fhir", "text/xml", "text/json", MediaType.APPLICATION_OCTET_STREAM })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, "application/fhir+xml", "application/fhir+json", "application/xml+fhir", "application/json+fhir", "text/xml", "text/json" })
	public Response update(@Context HttpServletRequest request, @Context HttpHeaders headers, @PathParam("resourceType") String resourceType, @PathParam("id") String resourceId, InputStream resourceInputStream) {

		log.fine("[START] ResourceRESTService.update(" + resourceType + ", " + resourceId + ")");

		Response response = null;

		String payload = debugRequest(request, headers, resourceInputStream);

		try {
			// Validate request fhir version with supported fhir version
			if (!ServicesUtil.INSTANCE.fhirVersionMatched(request, headers, codeService.findCodeValueByName("supportedVersions"))) {
				response = ServicesUtil.INSTANCE.fhirVersioMismatchedResponse(headers, codeService.findCodeValueByName("supportedVersions"), context);
			} else {
				response = resourceOps.update(context, headers, null, null, resourceId, payload, resourceType);
			}
		}
		catch (Exception e) {
			log.severe(e.getMessage());
		}

		return response;
	}

	/**
	 * The patch interaction creates a new current version for an existing resource for the given id using a partial
	 * update mechanism.
	 *
	 * Consumes check for MediaType.APPLICATION_OCTET_STREAM to support condition of undefined request Content-Type. JBoss RESTEasy
	 * framework defaults empty Content-Type to MediaType.APPLICATION_OCTET_STREAM.
	 *
	 * @param request
	 * @param headers
	 * @param resourceType
	 * @param resourceId
	 * @param resourceInputStream
	 * @return <code>Response</code>
	 */
	@PATCH
	@Path("{resourceType}/{id}")
	@Consumes({ MediaType.APPLICATION_JSON_PATCH_JSON, "application/xml-patch+xml", MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_OCTET_STREAM })
	@Produces({ "application/fhir+xml", "application/fhir+json", "application/xml+fhir", "application/json+fhir", MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "text/xml", "text/json" })
	public Response patch(@Context HttpServletRequest request, @Context HttpHeaders headers, @PathParam("resourceType") String resourceType, @PathParam("id") String resourceId, InputStream resourceInputStream) {

		log.fine("[START] ResourceRESTService.patch(" + resourceType + ", " + resourceId + ")");

		Response response = null;

		debugRequest(request, headers, null);

		try {
			// Validate request fhir version with supported fhir version
			if (!ServicesUtil.INSTANCE.fhirVersionMatched(request, headers, codeService.findCodeValueByName("supportedVersions"))) {
				response = ServicesUtil.INSTANCE.fhirVersioMismatchedResponse(headers, codeService.findCodeValueByName("supportedVersions"), context);
			} else {
				response = resourceOps.patch(context, headers, resourceId, resourceInputStream, resourceType);
			}
		}
		catch (Exception e) {
			log.severe(e.getMessage());
		}

		return response;
	}

	/**
	 * The conditional update interaction creates a new current version for an existing resource or creates a new
	 * resource if no resource already exists for the given id based on search criteria.
	 *
	 * Consumes check for MediaType.APPLICATION_OCTET_STREAM to support condition of undefined request Content-Type. JBoss RESTEasy
	 * framework defaults empty Content-Type to MediaType.APPLICATION_OCTET_STREAM.
	 *
	 * @param request
	 * @param headers
	 * @param resourceType
	 * @param resourceInputStream
	 * @return <code>Response</code>
	 */
	@PUT
	@Path("{resourceType}")
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, "application/fhir+xml", "application/fhir+json", "application/xml+fhir", "application/json+fhir", "text/xml", "text/json", MediaType.APPLICATION_OCTET_STREAM })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, "application/fhir+xml", "application/fhir+json", "application/xml+fhir", "application/json+fhir", "text/xml", "text/json" })
	public Response conditionalUpdate(@Context HttpServletRequest request, @Context HttpHeaders headers, @PathParam("resourceType") String resourceType, InputStream resourceInputStream) {

		log.fine("[START] ResourceRESTService.conditionalUpdate(" + resourceType + ")");

		Response response = null;

		String payload = debugRequest(request, headers, resourceInputStream);

		try {
			// Validate request fhir version with supported fhir version
			if (!ServicesUtil.INSTANCE.fhirVersionMatched(request, headers, codeService.findCodeValueByName("supportedVersions"))) {
				response = ServicesUtil.INSTANCE.fhirVersioMismatchedResponse(headers, codeService.findCodeValueByName("supportedVersions"), context);
			} else {
				response = resourceOps.update(context, headers, null, null, null, payload, resourceType);
			}
		}
		catch (Exception e) {
			log.severe(e.getMessage());
		}

		return response;
	}

	/**
	 * The delete interaction removes an existing resource.
	 *
	 * @param request
	 * @param headers
	 * @param resourceType
	 * @param resourceId
	 * @return <code>Response</code>
	 */
	@DELETE
	@Path("{resourceType}/{id}")
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, "application/fhir+xml", "application/fhir+json", "application/xml+fhir", "application/json+fhir", "text/xml", "text/json" })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, "application/fhir+xml", "application/fhir+json", "application/xml+fhir", "application/json+fhir", "text/xml", "text/json" })
	public Response delete(@Context HttpServletRequest request, @Context HttpHeaders headers, @PathParam("resourceType") String resourceType, @PathParam("id") String resourceId) {

		log.fine("[START] ResourceRESTService.delete(" + resourceType + ", " + resourceId + ")");

		Response response = null;

		debugRequest(request, headers, null);

		try {
			// Validate request fhir version with supported fhir version
			if (!ServicesUtil.INSTANCE.fhirVersionMatched(request, headers, codeService.findCodeValueByName("supportedVersions"))) {
				response = ServicesUtil.INSTANCE.fhirVersioMismatchedResponse(headers, codeService.findCodeValueByName("supportedVersions"), context);
			} else {
				response = resourceOps.delete(context, headers, null, resourceId, resourceType);
			}
		}
		catch (Exception e) {
			log.severe(e.getMessage());
		}

		return response;
	}

	/**
	 * The conditional delete interaction removes an existing resource based on search criteria.
	 *
	 * @param request
	 * @param headers
	 * @param resourceType
	 * @return <code>Response</code>
	 */
	@DELETE
	@Path("{resourceType}")
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, "application/fhir+xml", "application/fhir+json", "application/xml+fhir", "application/json+fhir", "text/xml", "text/json" })
	public Response conditionalDelete(@Context HttpServletRequest request, @Context HttpHeaders headers, @PathParam("resourceType") String resourceType) {

		log.fine("[START] ResourceRESTService.conditionalDelete(" + resourceType + ")");

		Response response = null;

		debugRequest(request, headers, null);

		try {
			// Validate request fhir version with supported fhir version
			if (!ServicesUtil.INSTANCE.fhirVersionMatched(request, headers, codeService.findCodeValueByName("supportedVersions"))) {
				response = ServicesUtil.INSTANCE.fhirVersioMismatchedResponse(headers, codeService.findCodeValueByName("supportedVersions"), context);
			} else {
				response = resourceOps.delete(context, headers, null, null, resourceType);
			}
		}
		catch (Exception e) {
			log.severe(e.getMessage());
		}

		return response;
	}

	/**
	 * The history instance interaction returns all version instances for a specific resource.
	 *
	 * @param request
	 * @param headers
	 * @param resourceType
	 * @param resourceId
	 * @return <code>Response</code>
	 */
	@GET
	@Path("{resourceType}/{id}/_history")
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, "application/fhir+xml", "application/fhir+json", "application/xml+fhir", "application/json+fhir", "text/xml", "text/json" })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, "application/fhir+xml", "application/fhir+json", "application/xml+fhir", "application/json+fhir", "text/xml", "text/json" })
	public Response historyInstance(@Context HttpServletRequest request, @Context HttpHeaders headers, @PathParam("resourceType") String resourceType, @PathParam("id") String resourceId) {

		log.fine("[START] ResourceRESTService.historyInstance(" + resourceType + ", " + resourceId + ")");

		Response response = null;

		debugRequest(request, headers, null);

		try {
			// Validate request fhir version with supported fhir version
			if (!ServicesUtil.INSTANCE.fhirVersionMatched(request, headers, codeService.findCodeValueByName("supportedVersions"))) {
				response = ServicesUtil.INSTANCE.fhirVersioMismatchedResponse(headers, codeService.findCodeValueByName("supportedVersions"), context);
			} else {
				response = resourceOps.history(context, headers, null, resourceId, resourceType);
			}
		}
		catch (Exception e) {
			log.severe(e.getMessage());
		}

		return response;
	}

	/**
	 * The history resource interaction returns all version instances for a specific resource type.
	 *
	 * @param request
	 * @param headers
	 * @param resourceType
	 * @return <code>Response</code>
	 */
	@GET
	@Path("{resourceType}/_history")
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, "application/fhir+xml", "application/fhir+json", "application/xml+fhir", "application/json+fhir", "text/xml", "text/json" })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, "application/fhir+xml", "application/fhir+json", "application/xml+fhir", "application/json+fhir", "text/xml", "text/json" })
	public Response historyResource(@Context HttpServletRequest request, @Context HttpHeaders headers, @PathParam("resourceType") String resourceType) {

		log.fine("[START] ResourceRESTService.historyResource(" + resourceType + ")");

		Response response = null;

		debugRequest(request, headers, null);

		try {
			// Validate request fhir version with supported fhir version
			if (!ServicesUtil.INSTANCE.fhirVersionMatched(request, headers, codeService.findCodeValueByName("supportedVersions"))) {
				response = ServicesUtil.INSTANCE.fhirVersioMismatchedResponse(headers, codeService.findCodeValueByName("supportedVersions"), context);
			} else {
				response = resourceOps.history(context, headers, null, null, resourceType);
			}
		}
		catch (Exception e) {
			log.severe(e.getMessage());
		}

		return response;
	}

	/**
	 * The history global interaction returns all version instances for all supported resource types.
	 *
	 * @param request
	 * @param headers
	 * @return <code>Response</code>
	 */
	@GET
	@Path("_history")
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, "application/fhir+xml", "application/fhir+json", "application/xml+fhir", "application/json+fhir", "text/xml", "text/json" })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, "application/fhir+xml", "application/fhir+json", "application/xml+fhir", "application/json+fhir", "text/xml", "text/json" })
	public Response historyGlobal(@Context HttpServletRequest request, @Context HttpHeaders headers) {

		log.fine("[START] ResourceRESTService.historyGlobal()");

		Response response = null;

		debugRequest(request, headers, null);

		try {
			// Validate request fhir version with supported fhir version
			if (!ServicesUtil.INSTANCE.fhirVersionMatched(request, headers, codeService.findCodeValueByName("supportedVersions"))) {
				response = ServicesUtil.INSTANCE.fhirVersioMismatchedResponse(headers, codeService.findCodeValueByName("supportedVersions"), context);
			} else {
				response = resourceOps.history(context, headers, null, null, null);
			}
		}
		catch (Exception e) {
			log.severe(e.getMessage());
		}

		return response;
	}

	/**
	 * The batch and transaction interaction accepts a Bundle resource type only of type 'batch' or 'transaction'. Processing of the
	 * Bundle entries will either treat them as independent (batch) or a single transaction (transaction).
	 *
	 * Consumes check for MediaType.APPLICATION_OCTET_STREAM to support condition of undefined request Content-Type. JBoss RESTEasy
	 * framework defaults empty Content-Type to MediaType.APPLICATION_OCTET_STREAM.
	 *
	 * @param request
	 * @param headers
	 * @param resourceInputStream
	 * @return <code>Response</code>
	 */
	@POST
	@Path("/")
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, "application/fhir+xml", "application/fhir+json", "application/xml+fhir", "application/json+fhir", "text/xml", "text/json", MediaType.APPLICATION_OCTET_STREAM })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, "application/fhir+xml", "application/fhir+json", "application/xml+fhir", "application/json+fhir", "text/xml", "text/json" })
	public Response batchTransaction(@Context HttpServletRequest request, @Context HttpHeaders headers, InputStream resourceInputStream) {

		log.fine("[START] ResourceRESTService.batchTransaction()");

		Response response = null;

		String payload = debugRequest(request, headers, resourceInputStream, null, true);

		try {
			// Validate request fhir version with supported fhir version
			if (!ServicesUtil.INSTANCE.fhirVersionMatched(request, headers, codeService.findCodeValueByName("supportedVersions"))) {
				response = ServicesUtil.INSTANCE.fhirVersioMismatchedResponse(headers, codeService.findCodeValueByName("supportedVersions"), context);
			} else {
				response = batchTransactionOps.batchTransaction(context, headers, payload);
			}
		}
		catch (Exception e) {
			log.severe(e.getMessage());
		}

		return response;
	}

	/**
	 * The create interaction creates a new resource in a server assigned location. If the client wishes to have control
	 * over the id of a newly submitted resource, it should use the update interaction instead.
	 *
	 * Consumes check for MediaType.APPLICATION_OCTET_STREAM to support condition of undefined request Content-Type. JBoss RESTEasy
	 * framework defaults empty Content-Type to MediaType.APPLICATION_OCTET_STREAM.
	 *
	 * @param request
	 * @param headers
	 * @param resourceType
	 * @param resourceInputStream
	 * @return <code>Response</code>
	 */
	@POST
	@Path("{resourceType}")
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, "application/fhir+xml", "application/fhir+json", "application/xml+fhir", "application/json+fhir", "text/xml", "text/json", MediaType.APPLICATION_OCTET_STREAM })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, "application/fhir+xml", "application/fhir+json", "application/xml+fhir", "application/json+fhir", "text/xml", "text/json" })
	public Response create(@Context HttpServletRequest request, @Context HttpHeaders headers, @PathParam("resourceType") String resourceType, InputStream resourceInputStream) {

		log.fine("[START] ResourceRESTService.create(" + resourceType + ")");

		Response response = null;

		String payload = debugRequest(request, headers, resourceInputStream);

		try {
			// Validate request fhir version with supported fhir version
			if (!ServicesUtil.INSTANCE.fhirVersionMatched(request, headers, codeService.findCodeValueByName("supportedVersions"))) {
				response = ServicesUtil.INSTANCE.fhirVersioMismatchedResponse(headers, codeService.findCodeValueByName("supportedVersions"), context);
			} else {
				response = resourceOps.create(context, headers, null, payload, resourceType, null);
			}
		}
		catch (Exception e) {
			log.severe(e.getMessage());
		}

		return response;
	}

	/**
	 * This returns a <code>Bundle</code> with the found search contents for the resource type.
	 *
	 * @param request
	 * @param headers
	 * @param ui
	 * @param resourceType
	 * @return <code>Response</code>
	 */
	@GET
	@Path("{resourceType}")
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, "application/fhir+xml", "application/fhir+json", "application/xml+fhir", "application/json+fhir", "text/xml", "text/json" })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, "application/fhir+xml", "application/fhir+json", "application/xml+fhir", "application/json+fhir", "text/xml", "text/json" })
	public Response searchGet(@Context HttpServletRequest request, @Context HttpHeaders headers, @Context UriInfo ui, @PathParam("resourceType") String resourceType) {

		log.fine("[START] ResourceRESTService.searchGet(" + resourceType + ")");

		Response response = null;

		debugRequest(request, headers, null);

		try {
			// Validate request fhir version with supported fhir version
			if (!ServicesUtil.INSTANCE.fhirVersionMatched(request, headers, codeService.findCodeValueByName("supportedVersions"))) {
				response = ServicesUtil.INSTANCE.fhirVersioMismatchedResponse(headers, codeService.findCodeValueByName("supportedVersions"), context);
			} else {
				response = resourceOps.search(headers, ui, null, resourceType);
			}
		}
		catch (Exception e) {
			log.severe(e.getMessage());
		}

		return response;
	}

	/**
	 * This returns a <code>Bundle</code> with the found search contents for the resource type.
	 *
	 * Consumes check for MediaType.APPLICATION_FORM_URLENCODED to support submission of search parameters via an HTML Form.
	 *
	 * Consumes check for MediaType.APPLICATION_OCTET_STREAM to support condition of undefined request Content-Type. JBoss RESTEasy
	 * framework defaults empty Content-Type to MediaType.APPLICATION_OCTET_STREAM.
	 *
	 * @param request
	 * @param headers
	 * @param ui
	 * @param resourceType
	 * @param form
	 * @return <code>Response</code>
	 */
	@POST
	@Path("{resourceType}/_search")
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, "application/fhir+xml", "application/fhir+json", "application/xml+fhir", "application/json+fhir", "text/xml", "text/json", MediaType.APPLICATION_FORM_URLENCODED, MediaType.APPLICATION_OCTET_STREAM })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, "application/fhir+xml", "application/fhir+json", "application/xml+fhir", "application/json+fhir", "text/xml", "text/json" })
	public Response searchPost(@Context HttpServletRequest request, @Context HttpHeaders headers, @Context UriInfo ui, @PathParam("resourceType") String resourceType, MultivaluedMap<String, String> form) {

		log.fine("[START] ResourceRESTService.searchPost(" + resourceType + ")");

		Response response = null;

		debugRequest(request, headers, null, form);

		try {
			// Get the content type based on the request Content-Type
			String contentType = ServicesUtil.INSTANCE.getHttpHeader(headers, HttpHeaders.CONTENT_TYPE);

			// Validate request fhir version with supported fhir version
			if (!ServicesUtil.INSTANCE.fhirVersionMatched(request, headers, codeService.findCodeValueByName("supportedVersions"))) {
				response = ServicesUtil.INSTANCE.fhirVersioMismatchedResponse(headers, codeService.findCodeValueByName("supportedVersions"), context);
			} else {
				if (contentType != null && contentType.equals(MediaType.APPLICATION_FORM_URLENCODED)) {
					// Process form payload if present
					response = resourceOps.search(headers, ui, null, resourceType, form);
				}
				else {
					// Allow POST search without a form payload
					response = resourceOps.search(headers, ui, null, resourceType);
				}
			}
		}
		catch (Exception e) {
			log.severe(e.getMessage());
		}

		return response;
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
		return debugRequest(request, headers, resourceInputStream, null);
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
	 * @param form
	 */
	private String debugRequest(HttpServletRequest request, HttpHeaders headers, InputStream resourceInputStream, MultivaluedMap<String, String> form) {
		return debugRequest(request, headers, resourceInputStream, form, false);
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
	 * @param form
	 * @param snipped
	 */
	private String debugRequest(HttpServletRequest request, HttpHeaders headers, InputStream resourceInputStream, MultivaluedMap<String, String> form, boolean snipped) {

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

		log.info("Absolute Path: " + sbRequestUrl.toString());
		log.info("Request URL: " + context.getRequestUri().toString());

		log.info("----- FORM INPUT PARAMS -----");
		if (form != null && !form.isEmpty()) {
			sbRequestUrl.append("?");
			boolean first = true;
			for (String key : form.keySet()) {
				if (!first) {
					sbRequestUrl.append("&");
				}
				log.info("input(" + key + ") is " + form.get(key).toString());
				sbRequestUrl.append(key).append("=").append(form.get(key).toString());
			}
		}

		log.info("----- PAYLOAD ----- [snipped; use fine logging]");
		if (resourceInputStream != null) {
			try {
				StringWriter writer = new StringWriter();
				String encoding = "UTF-8";
				IOUtils.copy(resourceInputStream, writer, encoding);
				payload = writer.toString();

				if (snipped == false) {
					log.info(payload);
				}
				else {
					log.fine(">> SNIPPED <<");
				}

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
