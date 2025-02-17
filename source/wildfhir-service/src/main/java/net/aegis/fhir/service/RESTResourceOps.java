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
package net.aegis.fhir.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.io.IOUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.hl7.fhir.r4.formats.IParser.OutputStyle;
import org.hl7.fhir.r4.formats.JsonParser;
import org.hl7.fhir.r4.formats.XmlParser;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Resource;
import org.xmlpull.v1.XmlPullParserException;

import com.google.gson.JsonSyntaxException;

import net.aegis.fhir.model.Constants;
import net.aegis.fhir.model.ResourceContainer;
import net.aegis.fhir.model.ResourceType;
import net.aegis.fhir.service.audit.AuditEventActionEnum;
import net.aegis.fhir.service.audit.AuditEventService;
import net.aegis.fhir.service.narrative.FHIRNarrativeGeneratorClient;
import net.aegis.fhir.service.provenance.ProvenanceService;
import net.aegis.fhir.service.util.ServicesUtil;
import net.aegis.fhir.service.util.StringUtils;
import net.aegis.fhir.service.util.UTCDateUtil;

/**
 *
 * @author richard.ettema
 * @author rizwan.tanoli
 */
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class RESTResourceOps {

    private Logger log = Logger.getLogger("RESTResourceOps");

	@Inject
	AuditEventService auditEventService;

    @Inject
    CodeService codeService;

	@Inject
	ProvenanceService provenanceService;

    @Inject
    ResourceService resourceService;

    @Inject
    UTCDateUtil utcDateUtil;

    /**
     * This returns a single instance with the content specified for the resource type for the current version of the resource.
     *
     * @param context
     * @param headers
     * @param requestHeaderParams
     * @param contextQueryParams
     * @param id
     * @param resourceType
     * @return <code>Response</code>
     */
    public Response resourceTypeRead(UriInfo context, HttpHeaders headers, MultivaluedMap<String,String> requestHeaderParams, MultivaluedMap<String,String> contextQueryParams, String id, String resourceType) {

        log.fine("[START] RESTResourceOps.resourceTypeRead()");

        Response.ResponseBuilder builder = null;
        String producesType = null;
        String _summary = null;
        String ifModifiedSince = null;
        String ifNoneMatch = null;
        String outcome = null;
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

			// Check for valid and supported ResourceType
			if (ResourceType.isValidResourceType(resourceType) && ResourceType.isSupportedResourceType(resourceType)) {

				// Check for valid FHIR resource id data type compliance
				if (StringUtils.isValidFhirId(id)) {

					// Get the _summary parameter, if present
					if (contextQueryParams != null) {
						_summary = ServicesUtil.INSTANCE.getUriParameter("_summary", contextQueryParams);
					}
					if (_summary == null) {
						_summary = ServicesUtil.INSTANCE.getUriParameter("_summary", context);
					}

					log.info("Resource id: " + id);

					ResourceContainer resourceContainer = resourceService.read(resourceType, id, _summary);

					log.info("Resource status: " + resourceContainer.getResponseStatus().name());

					String locationPath = context.getRequestUri().toString();

					/*
					 * Check for missing resource type if called from batch or transaction
					 */
					if (!locationPath.contains(resourceType)) {
						locationPath += "/" + resourceType + "/" + id;
					}

					if (resourceContainer != null && resourceContainer.getResource() != null) {
						locationPath += "/_history/" + resourceContainer.getResource().getVersionId();
					}

					// Check for valid returned resource instance; i.e. response status of OK
					if (resourceContainer.getResponseStatus().equals(Status.OK)) {

						/*
						 * Check conditional read based on HTTP Headers If-Modified-Since (date-time) and If-None-Match
						 * (ETag version id)
						 */
						if (requestHeaderParams != null) {
							ifModifiedSince = ServicesUtil.INSTANCE.getUriParameter(HttpHeaders.IF_MODIFIED_SINCE, requestHeaderParams);
						}
						if (ifModifiedSince == null) {
							ifModifiedSince = ServicesUtil.INSTANCE.getHttpHeader(headers, HttpHeaders.IF_MODIFIED_SINCE);
						}
						if (requestHeaderParams != null) {
							ifNoneMatch = ServicesUtil.INSTANCE.getUriParameter(HttpHeaders.IF_NONE_MATCH, requestHeaderParams);
						}
						if (ifNoneMatch == null) {
							ifNoneMatch = ServicesUtil.INSTANCE.getHttpHeader(headers, HttpHeaders.IF_NONE_MATCH);
						}

						if (ifModifiedSince != null || ifNoneMatch != null) {
							log.info("Conditional Read requested - check support level");

							if (codeService.isValueSupported("conditionalRead", "not-supported")) {
								outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.NOTSUPPORTED, "Unsupported operation - conditional read not implemented.", null, null, producesType);

								builder = Response.status(Response.Status.BAD_REQUEST).entity(outcome).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);
							}
							else if (codeService.isValueSupported("conditionalRead", "not-match") || codeService.isValueSupported("conditionalRead", "modified-since") || codeService.isValueSupported("conditionalRead", "full-support")) {

								if (ifModifiedSince != null && codeService.isValueSupported("conditionalRead", "not-match")) {
									outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.NOTSUPPORTED, "Unsupported conditional read criteria - If-Modified-Since.", null, null, producesType);

									builder = Response.status(Response.Status.BAD_REQUEST).entity(outcome).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);
								}
								else if (ifNoneMatch != null && codeService.isValueSupported("conditionalRead", "modified-since")) {
									outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.NOTSUPPORTED, "Unsupported conditional read criteria - If-None-Match.", null, null, producesType);

									builder = Response.status(Response.Status.BAD_REQUEST).entity(outcome).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);
								}
								else {
									boolean notModifiedorMatched = false;

									Date modifiedSince = null;
									if (ifModifiedSince != null) {
										try {
											try {
												// First try expected HTTP date format
												modifiedSince = utcDateUtil.parseHTTPDate(ifModifiedSince);
											}
											catch (Exception e) {
												log.severe("Exception parsing If-Modified-Since as HTTP Date. " + e.getMessage());
											}
											if (modifiedSince == null) {
												try {
													// Next try XML date format; not expected but we'll allow
													modifiedSince = utcDateUtil.parseXMLDate(ifModifiedSince);
												}
												catch (Exception e) {
													log.severe("Exception parsing If-Modified-Since as XML Date. " + e.getMessage());
												}
											}
											if (modifiedSince == null) {
												// If neither formats worked, default to current date and time
												modifiedSince = new Date();
											}

											Date lastUpdate = resourceContainer.getResource().getLastUpdate();

											if (lastUpdate.before(modifiedSince)) {
												notModifiedorMatched = true;
											}
										}
										catch (Exception e) {
											log.severe("Exception processing If-Modified-Since date value " + ifModifiedSince + "'. " + e.getMessage());
										}
									}

									if (!notModifiedorMatched) {

										if (ifNoneMatch != null) {
											try {
												String versionId = resourceContainer.getResource().getVersionId().toString();

												// First check for versionId only
												if (ifNoneMatch.equals(versionId)) {
													notModifiedorMatched = true;
												}
												if (!notModifiedorMatched) {
													// Next check for weak ETag format W/"vid"
													versionId = "W/\"" + versionId + "\"";
													if (ifNoneMatch.equals(versionId)) {
														notModifiedorMatched = true;
													}
												}
											}
											catch (Exception e) {
												log.severe("Exception parsing If-None-Match. " + e.getMessage());
											}
										}
									}

									if (notModifiedorMatched) {
										// Conditional read found no modifications or a match; return 304 NOT MODIFIED with no
										// response contents
										builder = Response.status(Response.Status.NOT_MODIFIED);

									}
									else {
										builder = buildResource(locationPath, producesType, resourceContainer, Ops.READ, responseFhirVersion);
									}
								}
							}
							else {
								// Support for conditional read not correctly defined so report error in OperationOutcome
								outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.PROCESSING,
										"Conditional read failed due to internal configuration error - support not defined properly.", null, null, producesType);

								builder = Response.status(Response.Status.BAD_REQUEST).entity(outcome).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);
							}

						}
						else {
							builder = buildResource(locationPath, producesType, resourceContainer, Ops.READ, responseFhirVersion);
						}

					}
					else {
						builder = buildResource(locationPath, producesType, resourceContainer, Ops.READ, responseFhirVersion);
					}

				}
				else {
					builder = responseInvalidResourceId(producesType, resourceType, id, responseFhirVersion);
				}

			}
			else {
				builder = responseInvalidResourceType(producesType, resourceType, responseFhirVersion);
			}

        } catch (Exception e) {
            // Handle generic exceptions
            outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.FATAL, OperationOutcome.IssueType.EXCEPTION, e.getMessage(), null, null, producesType);

            builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(outcome).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);

            e.printStackTrace();
        }

        Response resp = builder.build();
        return resp;
    }

    /**
     * This returns a single instance with the content specified for the resource type for that version of the resource.
     *
     * @param context
     * @param headers
     * @param id
     * @param versionId
     * @param resourceType
     * @return <code>Response</code>
     */
    public Response resourceTypeVRead(UriInfo context, HttpHeaders headers, String id, String versionId, String resourceType) {

        log.fine("[START] RESTResourceOps.resourceTypeVRead()");

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

			// Check for valid and supported ResourceType
			if (ResourceType.isValidResourceType(resourceType) && ResourceType.isSupportedResourceType(resourceType)) {

				// Check for valid FHIR resource id data type compliance
				if (StringUtils.isValidFhirId(id)) {

					log.info("Resource id: " + id);

					Integer iVersionId = Integer.valueOf(versionId);
					log.info("Converted version id: " + iVersionId);

					ResourceContainer resourceContainer = resourceService.vread(resourceType, id, iVersionId);
					String locationPath = context.getRequestUri().toString();

					/*
					 * Check for missing resource type if called from batch or transaction
					 */
					if (!locationPath.contains(resourceType)) {
						locationPath += "/" + resourceType + "/" + id + "/_history/" + versionId;
					}

					builder = buildResource(locationPath, producesType, resourceContainer, Ops.READ, responseFhirVersion);
				}
				else {
					builder = responseInvalidResourceId(producesType, resourceType, id, responseFhirVersion);
				}
			}
			else {
				builder = responseInvalidResourceType(producesType, resourceType, responseFhirVersion);
			}

        } catch (Exception e) {
            // Handle generic exceptions
            String outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.FATAL, OperationOutcome.IssueType.EXCEPTION, e.getMessage(), null, null, producesType);

            builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(outcome).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);

            e.printStackTrace();
        }

        return builder.build();
    }

    /**
     * The create interaction creates a new resource in a server assigned location. If the client wishes to
     * have control over the id of a newly submitted resource, it should use the update interaction instead.
     *
     * @param context
     * @param headers
     * @param requestHeaderParams
     * @param resourceInputStream
     * @param resourceType
     * @param resourceId
     * @return <code>Response</code>
     */
    public Response create(UriInfo context, HttpHeaders headers, MultivaluedMap<String,String> requestHeaderParams, String payload, String resourceType, String resourceId) {

        log.fine("[START] RESTResourceOps.create()");

		Response response = null;
        Response.ResponseBuilder builder = null;
        String contentType = null;
        String producesType = null;
        String ifNoneExist = null;
        String prefer = null;
		boolean okToCreate = true;
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

			// Get the content type based on the request Content-Type
			contentType = ServicesUtil.INSTANCE.getHttpHeader(headers, HttpHeaders.CONTENT_TYPE);

			if (contentType != null && !contentType.equals(MediaType.APPLICATION_OCTET_STREAM)) {
				String contextPath = context.getRequestUri().toString();

				/*
				 * Check for missing resource type if called from batch or transaction
				 */
				if (!contextPath.contains(resourceType)) {
					contextPath += "/" + resourceType;
				}
				/*
				 * Remove resource id if called from update
				 */
				contextPath = ServicesUtil.INSTANCE.extractBaseURL(contextPath, resourceType) + resourceType;

				StringBuilder sbLocationPath = new StringBuilder(contextPath);

				// Check for valid and supported ResourceType
				if (ResourceType.isValidResourceType(resourceType) && ResourceType.isSupportedResourceType(resourceType)) {

					// Instantiate the Resource; this is the first, simple validation of the resource
					Resource resource = null;

					try {
						if (contentType.indexOf("xml") >= 0) {
							// Convert XML contents to Resource
							XmlParser xmlP = new XmlParser();
							resource = xmlP.parse(payload.getBytes());
						}
						else if (contentType.indexOf("json") >= 0) {
							// Convert JSON contents to Resource
							JsonParser jsonP = new JsonParser();
							resource = jsonP.parse(payload.getBytes());
						}
						else {
							// contentType did not contain a valid media type or was null; attempt to determine based on starting character
							int firstValid = payload.indexOf("<"); // check for xml first
							if (firstValid > -1 && firstValid < 5) {
								if (firstValid > 0) {
									payload = payload.substring(firstValid);
								}
								// Convert XML contents to Resource
								contentType = "xml";
								XmlParser xmlP = new XmlParser();
								resource = xmlP.parse(payload.getBytes());
							}
							else {
								firstValid = payload.indexOf("{"); // check for json next
								if (firstValid > -1 && firstValid < 5) {
									if (firstValid > 0) {
										payload = payload.substring(firstValid);
									}
									// Convert JSON contents to Resource
									contentType = "json";
									JsonParser jsonP = new JsonParser();
									resource = jsonP.parse(payload.getBytes());
								}
							}
						}
					}
					catch (Exception e) {
						// Log original exception
						e.printStackTrace();
						// JSON or XML FHIR parsing failed, content is not a valid FHIR resource; throw appropriate exception to catch below
						if (contentType.indexOf("json") >= 0) {
							throw new JsonSyntaxException(e.getMessage());
						}
						else {
							// Default to XML exception
							throw new XmlPullParserException(e.getMessage());
						}
					}

					// Check okToCreate; if false, then create validation failed
					if (okToCreate) {
						/*
						 * Conditional Create based on HTTP Header If-None-Exist
						 */
						if (requestHeaderParams != null) {
							ifNoneExist = ServicesUtil.INSTANCE.getUriParameter("If-None-Exist", requestHeaderParams);
						}
						if (ifNoneExist == null) {
							ifNoneExist = ServicesUtil.INSTANCE.getHttpHeader(headers, "If-None-Exist");
						}

						if (ifNoneExist != null) {

							if (codeService.isSupported("conditionalCreate")) {
								log.info("Conditional Create requested and supported - start search");

								// Convert If-None-Exist into queryParams map
								List<NameValuePair> params = URLEncodedUtils.parse(ifNoneExist, Charset.defaultCharset());
								MultivaluedMap<String, String> queryParams = ServicesUtil.INSTANCE.listNameValuePairToMultivaluedMapString(params);

								// Execute search as defined in If-None-Exist header
								String locationPath = context.getRequestUri().toString();

								ResourceContainer searchContainer = resourceService.search(queryParams, null, null, null, resourceType, locationPath, null, null, null, false);

								if (searchContainer != null && searchContainer.getResponseStatus().equals(Status.OK) && searchContainer.getBundle() != null) {

									Bundle searchBundle = searchContainer.getBundle();
									if (!searchBundle.hasEntry()) {
										// If search returns zero records, process create
										okToCreate = true;
									}
									else if (searchBundle.getEntry().size() == 1) {
										// Check for OperationOutcome; if found, ok to create
										if (searchBundle.getEntryFirstRep().getResource().getResourceType().equals(org.hl7.fhir.r4.model.ResourceType.OperationOutcome)) {
											okToCreate = true;
										}
										else {
											// Else if search returns one record (match), return OK (200) with informational outcome
											String outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.INFORMATION, OperationOutcome.IssueType.INFORMATIONAL,
													"Conditional create If-None-Exist criteria found matching resource; no operation performed.", null, null, producesType);

											sbLocationPath.append("/").append(searchBundle.getEntry().get(0).getResource().getId());
											sbLocationPath.append("/_history/").append(searchBundle.getEntry().get(0).getResource().getMeta().getVersionId());
											URI resourceLocation = new URI(sbLocationPath.toString());

											// Get last update date
											Date lastUpdate = searchBundle.getEntry().get(0).getResource().getMeta().getLastUpdated();
											log.info("Last Update Date: " + lastUpdate);

											String sLastUpdate = null;
											if (lastUpdate != null) {
												sLastUpdate = utcDateUtil.formatUTCDateOffset(lastUpdate);
												log.info("Last Update UTC Date: " + sLastUpdate);
											}

											// ETag to hold the resource version id String eTagVersion = "W/\"" + searchBundle.getEntry().get(0).getResource().getMeta().getVersionId() + "\"";
											String eTagVersion = searchBundle.getEntry().get(0).getResource().getMeta().getVersionId();
											EntityTag eTag = new EntityTag(eTagVersion, true);

											builder = Response.status(Response.Status.OK).entity(outcome).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);

											builder = builder.tag(eTag).contentLocation(resourceLocation).location(resourceLocation).header("Last-Modified", sLastUpdate);

											okToCreate = false;
										}
									}
									else {
										// Else (more than one record found - multiple matches) return Precondition Failed (412)
										String outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.PROCESSING,
												"Conditional create could not be exected. If-None-Exist criteria found multiple matching resources; no operation performed.", null, null, producesType);

										builder = Response.status(Response.Status.PRECONDITION_FAILED).entity(outcome).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);

										okToCreate = false;
									}
								}
								else {
									// Search has failed so report error in OperationOutcome
									String outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.PROCESSING, "Conditional create failed due to processing errors with If-None-Exist.",
											ifNoneExist, null, producesType);

									builder = Response.status(Response.Status.BAD_REQUEST).entity(outcome).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);

									okToCreate = false;
								}

							}
							else {
								log.info("Conditional Create requested and not supported - return bad request");

								// Conditional Create is not supported; report error in OperationOutcome
								String outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.PROCESSING, "Conditional create of If-None-Exist is not supported.", ifNoneExist, null, producesType);

								builder = Response.status(Response.Status.BAD_REQUEST).entity(outcome).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);

								okToCreate = false;
							}

						}

						if (okToCreate) {

							// Check for no existing text narrative
							if (resource instanceof DomainResource) {
								DomainResource dResource = (DomainResource) resource;

								if (!dResource.hasText()) {
									// Use RI NarrativeGenerator
									FHIRNarrativeGeneratorClient.instance().generate(dResource);
								}
							}

							// Convert the Resource to XML byte[]
							ByteArrayOutputStream oResource = new ByteArrayOutputStream();
							XmlParser xmlParser = new XmlParser();
							xmlParser.setOutputStyle(OutputStyle.PRETTY);
							xmlParser.compose(oResource, resource, true);
							byte[] bResource = oResource.toByteArray();

							// Initialize a Resource to be created
							net.aegis.fhir.model.Resource newResource = new net.aegis.fhir.model.Resource();
							newResource.setResourceType(resourceType);
							newResource.setResourceContents(bResource);

							ResourceContainer resourceContainer = resourceService.create(newResource, resourceId, context.getAbsolutePath().toString());

							if (resourceId == null) {
								sbLocationPath.append("/").append(resourceContainer.getResource().getResourceId());
							}
							else {
								sbLocationPath.append("/").append(resourceId);
							}
							sbLocationPath.append("/_history/").append(resourceContainer.getResource().getVersionId());

							/*
							 * Honor Prefer HTTP Header first. If not defined, use configuration setting createResponsePayload
							 * Return preference minimal, representation (WildFHIR default) or OperationOutcome
							 */
							prefer = ServicesUtil.INSTANCE.getHttpHeader(headers, "Prefer");
							if (prefer == null) {
								prefer = codeService.getCodeValue("createResponsePayload");
							}

							if (prefer != null && prefer.indexOf("minimal") >= 0) {
								// Return content preference set to minimal; remove resource contents
								resourceContainer.getResource().setResourceContents(null);
							}
							else if ((prefer != null && prefer.indexOf("OperationOutcome") >= 0)) {
								// Return content preference set to OperationOutcome; generate XML OperationOutcome resource contents
								String outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.INFORMATION, OperationOutcome.IssueType.INFORMATIONAL, resourceContainer.getResource().getResourceType()
										+ " resource created with resource id " + resourceContainer.getResource().getResourceId() + ".", null, null);

								resourceContainer.getResource().setResourceContents(outcome.getBytes());
							}

							builder = buildResource(sbLocationPath.toString(), producesType, resourceContainer, Ops.CREATE, responseFhirVersion);
						}

					}

				}
				else {
					builder = responseInvalidResourceType(producesType, resourceType, responseFhirVersion);
				}

			}
			else {
				// Request Content-Type was empty or set to MediaType.APPLICATION_OCTET_STREAM, report error "415 (Unsupported Media Type)"
				String outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.INVALID, "The required request Content-Type mime type is not defined or not supported.", "HTTP Header Content-Type", null, producesType);

				builder = Response.status(Response.Status.UNSUPPORTED_MEDIA_TYPE).entity(outcome).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);
			}

        } catch (JsonSyntaxException jse) {
            String outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.EXCEPTION, "Resource syntax or data is incorrect or invalid, and cannot be used to create a new resource. " + jse.getMessage(), null, null, producesType);

            builder = Response.status(Response.Status.BAD_REQUEST).entity(outcome).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);

            jse.printStackTrace();

        } catch (XmlPullParserException xppe) {
            String outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.EXCEPTION, "Resource syntax or data is incorrect or invalid, and cannot be used to create a new resource. " + xppe.getMessage(), null, null, producesType);

            builder = Response.status(Response.Status.BAD_REQUEST).entity(outcome).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);

            xppe.printStackTrace();

        } catch (Exception e) {
            // Handle generic exceptions
            String outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.FATAL, OperationOutcome.IssueType.EXCEPTION, e.getMessage(), null, null, producesType);

            builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(outcome).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);

            e.printStackTrace();
        }

        response = builder.build();

		// Audit the create operation
		auditEventService.createAuditEvent(context, headers, payload, resourceType, response , null, AuditEventActionEnum.CREATE.getCode());

        return response;

    }

    /**
     * The update interaction creates a new current version for an existing resource or creates a new resource if no resource already exists for the given id.
     *
     * @param context
     * @param headers
     * @param requestHeaderParams
     * @param contextQueryParams
     * @param id
     * @param resourceInputStream
     * @param resourceType
     * @return <code>Response</code>
     */
    public Response update(UriInfo context, HttpHeaders headers, MultivaluedMap<String,String> requestHeaderParams, MultivaluedMap<String,String> contextQueryParams, String id, String payload, String resourceType) {

        log.fine("[START] RESTResourceOps.update()");

        ResourceContainer resourceContainer = null;
		Response response = null;
        Response.ResponseBuilder builder = null;
        String contentType = null;
        String producesType = null;
        String prefer = null;
        String ifMatch = null;
        ByteArrayOutputStream oResource;
		XmlParser xmlP = new XmlParser();
		JsonParser jsonP = new JsonParser();
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
        	// Get the content type based on the request Content-Type
			contentType = ServicesUtil.INSTANCE.getHttpHeader(headers, HttpHeaders.CONTENT_TYPE);

			if (contentType != null && !contentType.equals(MediaType.APPLICATION_OCTET_STREAM)) {
				// Get the produces type based on the request Accept
				producesType = ServicesUtil.INSTANCE.getProducesType(headers, context);

				// Check for valid and supported ResourceType
				if (ResourceType.isValidResourceType(resourceType) && ResourceType.isSupportedResourceType(resourceType)) {

					// Get the query parameters that represent the search criteria if any
					// FHIR-293 - Code correction to create new MultivaluedHashMap to hold all params
					MultivaluedMap<String, String> queryParams = new MultivaluedHashMap<String, String>();
					queryParams.putAll(context.getQueryParameters());
					if (contextQueryParams != null) {
						queryParams.putAll(contextQueryParams);
					}

					boolean isConditional = false;
					boolean okToCreate = false;
					boolean okToUpdate = true;

					/*
					 * Conditional Update based on no resource id and URI parameters
					 */
					if (id == null && queryParams != null && !queryParams.isEmpty()) {

						// If one query parameter present, check for '_format'
						if (queryParams.size() == 1) {

							// If not '_format', then perform conditional update logic
							if (!queryParams.containsKey("_format")) {
								isConditional = true;
							}
						}
						else {
							isConditional = true;
						}
					}

					if (isConditional) {

						if (codeService.isSupported("conditionalUpdate")) {
							log.info("Conditional Update requested and supported - start search");

							// Execute search as defined in the request uri parameters
							String locationPath = context.getRequestUri().toString();

							ResourceContainer searchContainer = resourceService.search(queryParams, null, null, null, resourceType, locationPath, null, null, null, false);

							if (searchContainer != null && searchContainer.getResponseStatus().equals(Status.OK) && searchContainer.getBundle() != null) {

								Bundle searchBundle = searchContainer.getBundle();
								if (!searchBundle.hasEntry()) {
									// If no matches, execute create by setting okToCreate = true and okToUpdate = true
									id = null;
									okToCreate = true;
									okToUpdate = true;
								}
								else if (searchBundle.getEntry().size() == 1) {

									// Check for OperationOutcome; if found, ok to create, update
									if (searchBundle.getEntryFirstRep().getResource().getResourceType().equals(org.hl7.fhir.r4.model.ResourceType.OperationOutcome)) {
										id = null;
										okToCreate = true;
										okToUpdate = true;
									}
									else {
										// If one match, perform update against matched resource by setting id to matched resource id
										id = searchBundle.getEntry().get(0).getResource().getId();
										okToUpdate = true;
									}

								}
								else {
									// If multiple matches, return precondition failed error criteria was not selective enough
									String outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.PROCESSING, "Conditional update failed due to query parameters not selective enough.", null,
											null, producesType);

									builder = Response.status(Response.Status.PRECONDITION_FAILED).entity(outcome).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);

									okToUpdate = false;
								}
							}
							else {
								// Search has failed so report error in OperationOutcome
								String outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.PROCESSING, "Conditional update failed due to processing errors with query parameters.", null,
										null, producesType);

								builder = Response.status(Response.Status.BAD_REQUEST).entity(outcome).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);

								okToUpdate = false;
							}
						}
						else {
							log.info("Conditional Update requested and not supported - return bad request");

							// Conditional Update is not supported; report error in OperationOutcome
							String outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.PROCESSING, "Conditional update based on query parameters is not supported.",
									"Conditional update based on query parameters is not supported.", null, producesType);

							builder = Response.status(Response.Status.BAD_REQUEST).entity(outcome).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);

							okToCreate = false;
							okToUpdate = false;
						}
					}

					if (okToUpdate) {
						log.info("Resource id: " + id);

						// Check okToCreate based on conditional update
						if (okToCreate) {

							// Call create logic and RETURN
							return this.create(context, headers, null, payload, resourceType, id);

						}
						// Else, check for valid FHIR resource id data type compliance
						else if (id != null && StringUtils.isValidFhirId(id)) {

							// Parse inputStream into a FHIR resource type
							Resource resource = null;

							try {
								if (contentType.indexOf("xml") >= 0) {
									// Convert XML contents to Resource
									resource = xmlP.parse(payload.getBytes());
								}
								else if (contentType.indexOf("json") >= 0) {
									// Convert JSON contents to Resource
									resource = jsonP.parse(payload.getBytes());
								}
								else {
									// contentType did not contain a valid media type or was null; attempt to determine based on starting character
									int firstValid = payload.indexOf("<"); // check for xml first
									if (firstValid > -1 && firstValid < 5) {
										if (firstValid > 0) {
											payload = payload.substring(firstValid);
										}
										// Convert XML contents to Resource
										contentType = "xml";
										resource = xmlP.parse(payload.getBytes());
									}
									else {
										firstValid = payload.indexOf("{"); // check for json next
										if (firstValid > -1 && firstValid < 5) {
											if (firstValid > 0) {
												payload = payload.substring(firstValid);
											}
											// Convert JSON contents to Resource
											contentType = "json";
											resource = jsonP.parse(payload.getBytes());
										}
									}
								}
							}
							catch (Exception e) {
								// Log original exception
								e.printStackTrace();
								// JSON or XML FHIR parsing failed, content is not a valid FHIR resource; throw appropriate exception to catch below
								if (contentType.indexOf("json") >= 0) {
									throw new JsonSyntaxException(e.getMessage());
								} else {
									// Default to XML exception
									throw new XmlPullParserException(e.getMessage());
								}
							}

							// Attempt to read current version of the resource
							resourceContainer = resourceService.read(resourceType, id, null);

							/*
							 * If current version of the resource does not exist, perform create using the provided id value
							 */
							if (resourceContainer == null || resourceContainer.getResource() == null || !resourceContainer.getResponseStatus().equals(Status.OK)) {

								// Verify that the resource payload contains an id element
								if (resource.hasId() && resource.getId().equals(id)) {

									// Remove resource id for create logic
									resource.setIdBase(null);

									String resourcePayload = null;

									if (contentType.indexOf("xml") >= 0) {
										// Convert Resource to XML string
										oResource = new ByteArrayOutputStream();
										xmlP.compose(oResource, resource);
										resourcePayload = oResource.toString();
									}
									else {
										// Convert Resource to JSON string
										oResource = new ByteArrayOutputStream();
										jsonP.compose(oResource, resource);
										resourcePayload = oResource.toString();
									}

									// Call create logic and RETURN
									return this.create(context, headers, null, resourcePayload, resourceType, id);
								}
								else {
									// resource id not found or ids do not match, return 400 (Bad Request) with OperationOutcome
									String outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.CONFLICT,
											"Resource contents invalid! The request body SHALL be a Resource with an id element that has an identical value to the [id] in the URL.", null, null, producesType);

									builder = Response.status(Response.Status.BAD_REQUEST).entity(outcome).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);
								}

							}
							else {
								/*
								 * Manage resource contention based on HTTP Header If-Match
								 */
								/*
								 * Conditional Create based on HTTP Header If-None-Exist
								 */
								if (requestHeaderParams != null) {
									ifMatch = ServicesUtil.INSTANCE.getUriParameter(HttpHeaders.IF_MATCH, requestHeaderParams);
								}
								if (ifMatch == null) {
									ifMatch = ServicesUtil.INSTANCE.getHttpHeader(headers, HttpHeaders.IF_MATCH);
								}
								boolean matched = true;

								if (ifMatch != null) {
									try {
										if (resourceContainer != null && resourceContainer.getResource() != null) {
											String versionId = resourceContainer.getResource().getVersionId().toString();
											String matchVersionId = "";

											// First check for versionId only
											if (!ifMatch.equals(versionId)) {
												matched = false;
											}
											if (!matched) {
												// Next check for "vid" (extra quotes to allow for clients that do not send correct weak ETag format)
												matchVersionId = "\"" + versionId + "\"";
												if (ifMatch.equals(matchVersionId)) {
													matched = true;
												}
											}
											if (!matched) {
												// Next check for valid weak ETag format W/"vid"
												matchVersionId = "W/\"" + versionId + "\"";
												log.info("ifMatch: " + ifMatch + "; Generated Weak ETag to match: " + matchVersionId);
												if (ifMatch.equals(matchVersionId)) {
													matched = true;
												}
											}
											if (!matched) {
												// Next check for weak ETag format "W/"vid"" (extra quotes to allow for clients that do not send correct weak ETag format)
												matchVersionId = "\"W/\"" + versionId + "\"\"";
												log.info("ifMatch: " + ifMatch + "; Generated Weak ETag to match: " + matchVersionId);
												if (ifMatch.equals(matchVersionId)) {
													matched = true;
												}
											}
										}
										else {
											matched = false;
										}
									}
									catch (Exception e) {
										log.severe("Exception parsing If-Match. " + e.getMessage());
									}
								}

								if (matched) {
									// Check for conditional update
									if (isConditional) {
										// Set resource id
										resource.setId(id);
									}

									// Check for resource.id
									if (resource.hasId() && resource.getId().equals(id)) {
										// Check for no existing text narrative
										if (resource instanceof DomainResource) {
											DomainResource dResource = (DomainResource) resource;

											if (!dResource.hasText()) {
												// Use Cached NarrativeGeneratorClient
												FHIRNarrativeGeneratorClient.instance().generate(dResource);
											}
										}

										// Convert the Resource to XML byte[]
										oResource = new ByteArrayOutputStream();
										xmlP.setOutputStyle(OutputStyle.PRETTY);
										xmlP.compose(oResource, resource, true);
										byte[] bResource = oResource.toByteArray();

										// Initialize a Resource to be updated
										net.aegis.fhir.model.Resource updateResource = new net.aegis.fhir.model.Resource();
										updateResource.setResourceType(resourceType);
										updateResource.setResourceContents(bResource);

										String absolutePath = context.getAbsolutePath().toString();

										/*
										 * Check for missing resource type if called from batch or transaction
										 */
										if (!absolutePath.contains(resourceType)) {
											absolutePath += "/" + resourceType + "/" + id;
										}

										resourceContainer = resourceService.update(id, updateResource, absolutePath);

										String locationPath = context.getRequestUri().toString();

										/*
										 * Check for missing resource type if called from batch or transaction
										 */
										if (!locationPath.contains(resourceType)) {
											locationPath += "/" + resourceType + "/" + id;
										}

										if (resourceContainer != null && resourceContainer.getResource() != null) {
											locationPath += "/_history/" + resourceContainer.getResource().getVersionId();
										}

										/*
										 * Honor Prefer HTTP Header if defined
										 * Return preference minimal, representation (WildFHIR default) or OperationOutcome
										 */
										prefer = ServicesUtil.INSTANCE.getHttpHeader(headers, "Prefer");

										if (prefer != null && prefer.indexOf("minimal") >= 0) {
											// Return content preference set to minimal; remove resource contents
											resourceContainer.getResource().setResourceContents(null);
										}
										else if ((prefer != null && prefer.indexOf("OperationOutcome") >= 0)) {
											// Return content preference set to OperationOutcome; generate XML OperationOutcome resource contents
											String outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.INFORMATION, OperationOutcome.IssueType.INFORMATIONAL,
													resourceContainer.getResource().getResourceType() + " resource updated with resource id " + resourceContainer.getResource().getResourceId() + ".", null, null);

											resourceContainer.getResource().setResourceContents(outcome.getBytes());
										}

										builder = buildResource(locationPath, producesType, resourceContainer, Ops.UPDATE, responseFhirVersion);
									}
									else {
										// resource id not found or ids do not match, return 400 (Bad Request) with OperationOutcome
										String outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.CONFLICT,
												"Resource contents invalid! The request body SHALL be a Resource with an id element that has an identical value to the [id] in the URL.", null, null, producesType);

										builder = Response.status(Response.Status.BAD_REQUEST).entity(outcome).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);
									}
								}
								else {
									String outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.CONFLICT, "Resource contention detected! Resource version mis-match.", null, null, producesType);

									builder = Response.status(Response.Status.PRECONDITION_FAILED).entity(outcome).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);
								}
							}
						}
						else {
							builder = responseInvalidResourceId(producesType, resourceType, id, responseFhirVersion);
						}
					} //if (okToUpdate)
					else {
						String outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.CONFLICT, "Resource contention detected! Conditional update criteria was not selective enough.", null, null, producesType);

						builder = Response.status(Response.Status.PRECONDITION_FAILED).entity(outcome).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);
					}
				} // if valid and supported ResourceType
				else {
					builder = responseInvalidResourceType(producesType, resourceType, responseFhirVersion);
				}
			}
			else {
				// Request Content-Type was empty or set to MediaType.APPLICATION_OCTET_STREAM, report error "415 (Unsupported Media Type)"
				String outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.INVALID, "The required request Content-Type mime type is not defined or not supported.", null, "HTTP Header Content-Type", producesType);

				builder = Response.status(Response.Status.UNSUPPORTED_MEDIA_TYPE).entity(outcome).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);
			}

        } catch (JsonSyntaxException jse) {
            String outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.EXCEPTION, "Resource syntax or data is incorrect or invalid, and cannot be used to create a new resource. " + jse.getMessage(), null, null, producesType);

            builder = Response.status(Response.Status.BAD_REQUEST).entity(outcome).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);

            jse.printStackTrace();

        } catch (XmlPullParserException xppe) {
            String outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.EXCEPTION, "Resource syntax or data is incorrect or invalid, and cannot be used to create a new resource. " + xppe.getMessage(), null, null, producesType);

            builder = Response.status(Response.Status.BAD_REQUEST).entity(outcome).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);

            xppe.printStackTrace();

        } catch (Exception e) {
            // Handle generic exceptions
            String outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.FATAL, OperationOutcome.IssueType.EXCEPTION, e.getMessage(), null, null, producesType);

            builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(outcome).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);

            e.printStackTrace();
        }

        response = builder.build();

		// Audit the update operation
		auditEventService.createAuditEvent(context, headers, payload, resourceType, response, id, AuditEventActionEnum.UPDATE.getCode());

        return response;

    }

    /**
	 * The patch interaction creates a new current version for an existing resource for the given id using a partial
	 * update mechanism.
     *
     * @param context
     * @param headers
     * @param id
     * @param resourceInputStream
     * @param resourceType
     * @return <code>Response</code>
     */
    public Response patch(UriInfo context, HttpHeaders headers, String id, InputStream resourceInputStream, String resourceType) {

        log.fine("[START] RESTResourceOps.patch()");

        ResourceContainer resourceContainer = null;
        Response.ResponseBuilder builder = null;
        String contentType = null;
        String producesType = null;
        String prefer = null;
        String ifMatch = null;
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
			// Get the content type based on the request Content-Type
			contentType = ServicesUtil.INSTANCE.getHttpHeader(headers, HttpHeaders.CONTENT_TYPE);

			if (contentType != null && !contentType.equals(MediaType.APPLICATION_OCTET_STREAM)) {
				// Get the produces type based on the request Accept
				producesType = ServicesUtil.INSTANCE.getProducesType(headers, context);

				// Check for valid and supported ResourceType
				if (ResourceType.isValidResourceType(resourceType) && ResourceType.isSupportedResourceType(resourceType)) {

					// Additional special check for unsupported resource type Binary
					if (!resourceType.equals("Binary")) {

						// Get the query parameters that represent the search criteria if any
						MultivaluedMap<String, String> queryParams = context.getQueryParameters();

						boolean isConditional = false;
						boolean okToPatch = true;

						/*
						 * Conditional Update based on no resource id and URI parameters
						 */
						if (id == null && queryParams != null && !queryParams.isEmpty()) {

							// If one query parameter present, check for '_format'
							if (queryParams.size() == 1) {

								// If not '_format', then perform conditional update logic
								if (!queryParams.containsKey("_format")) {
									isConditional = true;
								}
							}
							else {
								isConditional = true;
							}
						}

						if (isConditional == true) {

							if (codeService.isSupported("conditionalUpdate")) {
								log.info("Conditional Patch Update requested and supported - start search");

								// Execute search as defined in the request uri parameters
								String locationPath = context.getRequestUri().toString();

								ResourceContainer searchContainer = resourceService.search(queryParams, null, null, null, resourceType, locationPath, null, null, null, false);

								if (searchContainer != null && searchContainer.getResponseStatus().equals(Status.OK) && searchContainer.getBundle() != null) {

									Bundle searchBundle = searchContainer.getBundle();
									if (!searchBundle.hasEntry()) {
										// If no matches, execute create by setting okToCreate = true and okToUpdate = true
										String outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.INFORMATION, OperationOutcome.IssueType.NOTFOUND, "Conditional patch update failed due to esource to patch does not exist.", null, context.getRequestUri().toString(), producesType);

										builder = Response.status(Response.Status.NOT_FOUND).entity(outcome).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);

										okToPatch = false;
									}
									else if (searchBundle.getEntry().size() == 1) {

										// If one match, perform update against matched resource by setting id to matched resource id
										id = searchBundle.getEntry().get(0).getResource().getId();
										okToPatch = true;

									}
									else {
										// If multiple matches, return precondition failed error criteria was not selective enough
										String outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.PROCESSING, "Conditional patch update failed due to query parameters not selective enough.", null,
												null, producesType);

										builder = Response.status(Response.Status.PRECONDITION_FAILED).entity(outcome).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);

										okToPatch = false;
									}
								}
								else {
									// Search has failed so report error in OperationOutcome
									String outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.PROCESSING, "Conditional update failed due to processing errors with query parameters.", null,
											null, producesType);

									builder = Response.status(Response.Status.BAD_REQUEST).entity(outcome).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);

									okToPatch = false;
								}
							}
							else {
								log.info("Conditional Update requested and not supported - return bad request");

								// Conditional Update is not supported; report error in OperationOutcome
								String outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.PROCESSING, "Conditional update based on query parameters is not supported.",
										"Conditional update based on query parameters is not supported.", null, producesType);

								builder = Response.status(Response.Status.BAD_REQUEST).entity(outcome).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);

								okToPatch = false;
							}
						}

						if (okToPatch) {
							log.info("Patch Resource id: " + id);

							// Check for valid FHIR resource id data type compliance
							if (id != null && StringUtils.isValidFhirId(id)) {

								// Attempt to read current version of the resource
								resourceContainer = resourceService.read(resourceType, id, null);

								/*
								 * If current version of the resource does not exist, return not found
								 */
								if (resourceContainer == null || resourceContainer.getResource() == null || !resourceContainer.getResponseStatus().equals(Status.OK)) {

									// OperationOutcome for resource contents
									String outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.INFORMATION, OperationOutcome.IssueType.NOTFOUND, "Resource to patch does not exist.", null, context.getRequestUri().toString(), producesType);

									builder = Response.status(Response.Status.NOT_FOUND).entity(outcome).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);
								}
								else {
									/*
									 * Check for Version Aware Update based on HTTP Header If-Match
									 */
									boolean matched = true;

									ifMatch = ServicesUtil.INSTANCE.getHttpHeader(headers, HttpHeaders.IF_MATCH);

									if (ifMatch != null) {
										try {
											if (resourceContainer != null && resourceContainer.getResource() != null) {
												String versionId = resourceContainer.getResource().getVersionId().toString();
												String matchVersionId = "";

												// First check for versionId only
												if (!ifMatch.equals(versionId)) {
													matched = false;
												}
												if (!matched) {
													// Next check for "vid" (extra quotes to allow for clients that do not send correct weak ETag format)
													matchVersionId = "\"" + versionId + "\"";
													log.info("ifMatch: " + ifMatch + "; vid to match: " + matchVersionId);
													if (ifMatch.equals(matchVersionId)) {
														matched = true;
													}
												}
												if (!matched) {
													// Next check for valid weak ETag format W/"vid"
													matchVersionId = "W/\"" + versionId + "\"";
													log.info("ifMatch: " + ifMatch + "; Generated Weak ETag to match: " + matchVersionId);
													if (ifMatch.equals(matchVersionId)) {
														matched = true;
													}
												}
												if (!matched) {
													// Next check for weak ETag format "W/"vid"" (extra quotes to allow for clients that do not send correct weak ETag format)
													matchVersionId = "\"W/\"" + versionId + "\"\"";
													log.info("ifMatch: " + ifMatch + "; Generated Weak ETag to match: " + matchVersionId);
													if (ifMatch.equals(matchVersionId)) {
														matched = true;
													}
												}
											}
											else {
												matched = false;
											}
										}
										catch (Exception e) {
											log.severe("Exception parsing If-Match. " + e.getMessage());
										}
									}

									if (matched) {

										// Convert input stream to String - should be a JSON Patch formatted string
										String payload = IOUtils.toString(resourceInputStream, "UTF-8");

										// Call JSON or XML patch logic based on the Content-Type
										if (contentType == null || contentType.indexOf("xml") >= 0) {
											log.info("Calling XML Patch based on Content-Type of " + contentType);
											resourceContainer = resourceService.xmlPatch(payload, resourceContainer, context.getAbsolutePath().toString());
										}
										else {
											log.info("Calling JSON Patch based on Content-Type of " + contentType);
											resourceContainer = resourceService.jsonPatch(payload, resourceContainer, context.getAbsolutePath().toString());
										}

										// Check for error or exception
										if (resourceContainer == null || resourceContainer.getResource() == null || !resourceContainer.getResponseStatus().equals(Status.OK)) {

											// OperationOutcome for resource contents
											String outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.EXCEPTION, resourceContainer.getMessage(), null, context.getRequestUri().toString(), producesType);

											builder = Response.status(resourceContainer.getResponseStatus()).entity(outcome).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);
										}
										else {
											// Generate location path with new reference url to the updated resource
											String locationPath = context.getRequestUri().toString();
											if (resourceContainer != null && resourceContainer.getResource() != null) {
												locationPath += "/_history/" + resourceContainer.getResource().getVersionId();
											}

											/*
											 * Return preference minimal or representation
											 */
											prefer = ServicesUtil.INSTANCE.getHttpHeader(headers, "Prefer");

											if (prefer != null && prefer.indexOf("minimal") >= 0) {
												// Return content preference set to minimal; return information OperationOutcome
												String outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.INFORMATION, OperationOutcome.IssueType.INFORMATIONAL, resourceContainer.getResource().getResourceType()
														+ " resource patched with resource id " + resourceContainer.getResource().getResourceId() + ".", null, producesType);

												resourceContainer.getResource().setResourceContents(outcome.getBytes());
											}

											builder = buildResource(locationPath, producesType, resourceContainer, Ops.UPDATE, responseFhirVersion);
										}

									}
									else {
										String outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.CONFLICT, "Resource contention detected! Resource version mis-match.", null, null, producesType);

										builder = Response.status(Response.Status.PRECONDITION_FAILED).entity(outcome).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);
									}
								}
							}
							else {
								builder = responseInvalidResourceId(producesType, resourceType, id, responseFhirVersion);
							}
						}
						else {
							String outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.CONFLICT, "Resource contention detected! Conditional patch update criteria was not selective enough.", null, null, producesType);

							builder = Response.status(Response.Status.PRECONDITION_FAILED).entity(outcome).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);
						}
					}
					else {
				        String outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.TRANSIENT, "Invalid resource type! Binary patch operation not supported.", null, null, producesType);

				        builder = Response.status(Response.Status.NOT_FOUND).entity(outcome).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);
					}
				}
				else {
					builder = responseInvalidResourceType(producesType, resourceType, responseFhirVersion);
				}
			}
			else {
				// Request Content-Type was empty or set to MediaType.APPLICATION_OCTET_STREAM, report error "415 (Unsupported Media Type)"
				String outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.INVALID, "The required request Content-Type mime type is not defined or not supported.", null, "HTTP Header Content-Type", producesType);

				builder = Response.status(Response.Status.UNSUPPORTED_MEDIA_TYPE).entity(outcome).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);
			}

        } catch (JsonSyntaxException jse) {
            String outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.EXCEPTION, jse.getMessage(), null, null, producesType);

            builder = Response.status(Response.Status.BAD_REQUEST).entity(outcome).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);

            jse.printStackTrace();

        } catch (XmlPullParserException xppe) {
            String outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.EXCEPTION, xppe.getMessage(), null, null, producesType);

            builder = Response.status(Response.Status.BAD_REQUEST).entity(outcome).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);

            xppe.printStackTrace();

        } catch (Exception e) {
            // Handle generic exceptions
            String outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.FATAL, OperationOutcome.IssueType.EXCEPTION, e.getMessage(), null, null, producesType);

            builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(outcome).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);

            e.printStackTrace();
        }

        return builder.build();

    }

	/**
	 * The delete interaction removes an existing resource.
	 *
	 * @param context
	 * @param headers
	 * @param contextQueryParams
	 * @param id
	 * @param resourceType
	 * @return <code>Response</code>
	 */
	public Response delete(UriInfo context, HttpHeaders headers, MultivaluedMap<String,String> contextQueryParams, String id, String resourceType) {

		log.fine("[START] RESTResourceOps.delete()");

		Response.ResponseBuilder builder = null;
		String producesType = null;
		String outcome = null;
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

			// Check for valid and supported ResourceType
			if (ResourceType.isValidResourceType(resourceType) && ResourceType.isSupportedResourceType(resourceType)) {

				// Get the query parameters that represent the search criteria if any
				// FHIR-293 - Code correction to create new MultivaluedHashMap to hold all params
				MultivaluedMap<String, String> queryParams = new MultivaluedHashMap<String, String>();
				queryParams.putAll(context.getQueryParameters());
				if (contextQueryParams != null) {
					queryParams.putAll(contextQueryParams);
				}

				boolean isConditional = false;
				boolean okToDelete = true;
				boolean multipleDelete = false;
				Bundle searchBundle = null;

				/*
				 * Conditional Delete based on no resource id and URI parameters
				 */
				if (id == null && queryParams != null && !queryParams.isEmpty()) {

					// If one query parameter present, check for '_format'
					if (queryParams.size() == 1) {

						// If not '_format', then perform conditional update logic
						if (!queryParams.containsKey("_format")) {
							isConditional = true;
						}
					}
					else {
						isConditional = true;
					}
				}

				if (isConditional) {
					log.info("Conditional Delete requested - start search");

					if (codeService.isValueSupported("conditionalDelete", "not-supported")) {
						log.info("Conditional Delete not supported!");

						outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.NOTSUPPORTED, "Unsupported operation - conditional delete not implemented.", null, null, producesType);

						builder = Response.status(Response.Status.BAD_REQUEST).entity(outcome).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);

						okToDelete = false;
					}
					else if (codeService.isValueSupported("conditionalDelete", "single") || codeService.isValueSupported("conditionalDelete", "multiple")) {
						log.info("Conditional Delete is supported!");

						// Execute search as defined in the request uri parameters
						String locationPath = context.getRequestUri().toString();

						ResourceContainer searchContainer = resourceService.search(queryParams, null, null, null, resourceType, locationPath, null, null, null, false);

						if (searchContainer != null && searchContainer.getResponseStatus().equals(Status.OK) && searchContainer.getBundle() != null) {
							log.info("Conditional Delete search successful.");

							searchBundle = searchContainer.getBundle();
							if (!searchBundle.hasEntry()) {
								log.info("Conditional Delete search returned no matches!");

								// If no matches, return not found failed error criteria did not match any resources
								outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.WARNING, OperationOutcome.IssueType.PROCESSING, "Conditional delete failed due to no match based on the query parameters.", null,
										null, producesType);

								builder = Response.status(Response.Status.OK).entity(outcome).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);

								okToDelete = false;
							}
							else if (searchBundle.getEntry().size() == 1) {
								log.info("Conditional Delete search returned one match.");

								// If one match, perform delete against matched resource by setting id to matched
								// resource id
								id = searchBundle.getEntry().get(0).getResource().getId();
								okToDelete = true;
							}
							else {
								log.info("Conditional Delete search returned multiple matches.");

								if (codeService.isValueSupported("conditionalDelete", "multiple")) {
									multipleDelete = true;
								}
								else {
									// If multiple matches, return precondition failed error criteria was not selective
									// enough
									outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.PROCESSING,
											"Conditional delete failed due to query parameters not selective enough. Deletion of multiple resources not supported.", null, null, producesType);

									builder = Response.status(Response.Status.PRECONDITION_FAILED).entity(outcome).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);

									okToDelete = false;
								}
							}
						}
						else {
							// Search has failed so report error in OperationOutcome
							outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.PROCESSING, "Conditional delete failed due to processing errors with query parameters.", null,
									null, producesType);

							builder = Response.status(Response.Status.BAD_REQUEST).entity(outcome).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);

							okToDelete = false;
						}
					}
					else {
						// Support for conditional delete not correctly defined so report error in OperationOutcome
						outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.PROCESSING,
								"Conditional delete failed due to internal configuration error - support not defined properly.", null, null, producesType);

						builder = Response.status(Response.Status.BAD_REQUEST).entity(outcome).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);

						okToDelete = false;
					}
				}

				if (okToDelete) {
					if (multipleDelete) {
						log.info("Ok to Delete - mutiple delete");

						// All the searchBundle entries must be deleted successfully. If not, report an error in OperationOutcome

						List<String> resourceIds = new ArrayList<String>();
						for (BundleEntryComponent entry : searchBundle.getEntry()) {
							resourceIds.add(entry.getResource().getId());
						}

						ResourceContainer resourceContainer = resourceService.deleteMultiple(resourceType, resourceIds);

						if (resourceContainer != null) {
							// if resourceContainer is not null, delete happened, finish processing
							if (resourceContainer.getResponseStatus().equals(Response.Status.OK)) {
								outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.INFORMATION, OperationOutcome.IssueType.INFORMATIONAL, "Conditional delete successfully deleted all matching " + resourceType
										+ " resources based on the query parameters.", null, null, producesType);

								builder = Response.status(Response.Status.OK).entity(outcome).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);
							}
							else {
								outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.PROCESSING, "Conditional delete failure! " + resourceContainer.getMessage(), null, null, producesType);

								builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(outcome).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);
							}
						}
					}
					else {
						log.info("Ok to Delete - single delete; Resource id: " + id);

						// Check for valid FHIR resource id data type compliance
						if (id != null && StringUtils.isValidFhirId(id)) {

							ResourceContainer resourceContainer = resourceService.delete(resourceType, id);

							if (resourceContainer != null) {
								// if resourceContainer is not null, delete happened, finish processing
								String locationPath = context.getRequestUri().toString();
								if (resourceContainer != null && resourceContainer.getResource() != null) {
									locationPath += "/_history/" + resourceContainer.getResource().getVersionId();
								}

								builder = buildResource(locationPath, producesType, resourceContainer, Ops.DELETE, responseFhirVersion);
							}
						}
						else {
							builder = responseInvalidResourceId(producesType, resourceType, id, responseFhirVersion);
						}
					}
				}
				// else not needed as responses for okToDelete = false already built
			}
			else {
				builder = responseInvalidResourceType(producesType, resourceType, responseFhirVersion);
			}
		}
		catch (Exception e) {
			// Handle generic exceptions
			outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.FATAL, OperationOutcome.IssueType.EXCEPTION, e.getMessage(), null, null, producesType);

			builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(outcome).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);

			e.printStackTrace();
		}

		return builder.build();
	}

    /**
     * <p>Performs a compartment search using any query parameters passed via {@link UriInfo}.If no<br/>
     * query parameters are present search is done using {@code resourceType}.<br/>
     * This returns a {@link Response} with an {@link Bundle} containing the found search contents.</p>
     * @param request
     * @param headers
     * @param context
     * @param compartment
     * @param id
     * @param resourceType
     * @return Response
     */
    public Response compartment(HttpServletRequest request, HttpHeaders headers, UriInfo context, String compartment, String id, String resourceType) {

        log.fine("[START] RESTResourceOps.compartment()");

        ResourceContainer resourceContainer = null;
        Response.ResponseBuilder builder = null;
        String producesType = null;
        Integer countInteger = null;
        String countString = null;
        Integer pageInteger = null;
        String pageString = null;
        String summaryString = null;
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

			// Check for valid and supported Compartment
        	boolean isCompartmentValid = false;

        	if (compartment == null
					|| (ResourceType.isValidCompartment(compartment)
							&& ResourceType.isSupportedCompartment(compartment))) {

        		isCompartmentValid = true;
        	}

        	// Check for valid and supported ResourceType
        	boolean isResourceTypeValid = false;

        	if (resourceType == null
					|| (ResourceType.isValidResourceType(resourceType)
							&& ResourceType.isSupportedResourceType(resourceType))) {

        		isResourceTypeValid = true;
        	}

        	if (isCompartmentValid) {

        		if (isResourceTypeValid) {

					// Check for valid FHIR resource id data type compliance
					if (StringUtils.isValidFhirId(id)) {

						// Get the query parameters that represent the search criteria
						MultivaluedMap<String, String> queryParams = context.getQueryParameters();

						// Add compartment reference to search criteria query parameter
						List<String> criteriaNames = ResourceType.findCompartmentResourceTypeCriteria(compartment, resourceType);
						if (criteriaNames != null && !criteriaNames.isEmpty()) {
							for (String criteriaName : criteriaNames) {
								queryParams.add("COMPARTMENT-" + criteriaName, compartment + "/" + id);
							}

							// Get the count parameter if present
							countString = ServicesUtil.INSTANCE.getUriParameter("_count", context);

							if (countString != null) {
								try {
									countInteger = Integer.valueOf(countString);
									log.info("history count = " + countString);
								}
								catch (Exception e) {
									log.severe("Exception parsing _count parameter to Integer! " + e.getMessage());
									countInteger = null;
								}
							}

							// Get the _summary parameter if present
							summaryString = ServicesUtil.INSTANCE.getUriParameter("_summary", context);

							// Get the page parameter if present
							pageString = ServicesUtil.INSTANCE.getUriParameter("page", context);

							if (pageString != null) {
								try {
									pageInteger = Integer.valueOf(pageString);
									log.info("page number = " + pageString);
								}
								catch (Exception e) {
									log.severe("Exception parsing page parameter to Integer! " + e.getMessage());
									pageInteger = null;
								}
							}

							String queryString = context.getRequestUri().getQuery();
							List<NameValuePair> orderedParams = URLEncodedUtils.parse(queryString, Charset.forName("UTF-8"));
							for(NameValuePair param : orderedParams) {
								log.info("  param.name = '" + param.getName() + "'; param.value = '" + param.getValue() + "'");
							}

				            String locationPath = request.getRequestURL().toString();

							resourceContainer = resourceService.search(queryParams, null, null, orderedParams, resourceType, locationPath, countInteger, pageInteger, summaryString, true);

							builder = responseBundle(producesType, resourceContainer, locationPath, responseFhirVersion);
						}
						else {
					   		String outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.INVALID, "No search criteria name mapping found for Compartment and Resource Type!", null, null, producesType);

					   		builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(outcome).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);
						}
					}
					else {
						builder = responseInvalidResourceId(producesType, resourceType, id, responseFhirVersion);
					}
				}
				else {
					builder = responseInvalidResourceType(producesType, resourceType, responseFhirVersion);
				}
			}
			else {
				builder = responseInvalidCompartment(producesType, resourceType, responseFhirVersion);
			}

        } catch (Exception e) {
            // Handle generic exceptions
            String outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.FATAL, OperationOutcome.IssueType.EXCEPTION, e.getMessage(), null, null, producesType);

            builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(outcome).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);

            e.printStackTrace();
        }

        return builder.build();
    }

    /**
     * <p>Performs a search using any query parameters passed via {@link UriInfo}.If no<br/>
     * query parameters are present search is done using {@code resourceType}.<br/>
     * This returns a {@link Response} with an {@link Bundle} containing the found search contents.</p>
     * @param headers
     * @param context
     * @param contextQueryParams
     * @param resourceType
     * @return Response
     */
    public Response search(HttpHeaders headers, UriInfo context, MultivaluedMap<String,String> contextQueryParams, String resourceType) {
    	return search(headers, context, contextQueryParams, resourceType, null);
    }

    /**
     * <p>Performs a search using any query parameters passed via {@link UriInfo}.If no<br/>
     * query parameters are present search is done using {@code resourceType}.<br/>
     * This returns a {@link Response} with an {@link Bundle} containing the found search contents.</p>
     * @param headers
     * @param context
     * @param contextQueryParams
     * @param resourceType
     * @param formParams
     * @return Response
     */
    public Response search(HttpHeaders headers, UriInfo context, MultivaluedMap<String,String> contextQueryParams, String resourceType, MultivaluedMap<String, String> formParams) {
    	return search(headers, context, contextQueryParams, resourceType, formParams, null);
    }

    /**
     * <p>Performs a search using any query parameters passed via {@link UriInfo}.If no<br/>
     * query parameters are present search is done using {@code resourceType}.<br/>
     * This returns a {@link Response} with an {@link Bundle} containing the found search contents.</p>
     * @param headers
     * @param context
     * @param contextQueryParams
     * @param resourceType
     * @param formParams
     * @param overrideLocationPath
     * @return Response
     */
    public Response search(HttpHeaders headers, UriInfo context, MultivaluedMap<String,String> contextQueryParams, String resourceType, MultivaluedMap<String, String> formParams, String overrideLocationPath) {

        log.fine("[START] RESTResourceOps.search()");

        Response.ResponseBuilder builder = null;
        String producesType = null;
        Integer countInteger = null;
        String countString = null;
        Integer pageInteger = null;
        String pageString = null;
        String summaryString = null;
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
			//String queryString = context.getRequestUri().getQuery();
			String queryString = null;
			if (overrideLocationPath != null) {
				queryString = ServicesUtil.INSTANCE.extractURLParams(overrideLocationPath);
			}
			else {
				queryString = context.getRequestUri().getQuery();
			}
			log.fine("  queryString = '" + queryString + "'");
			List<NameValuePair> orderedParams = URLEncodedUtils.parse(queryString, Charset.forName("UTF-8"));
			for(NameValuePair param : orderedParams) {
				log.info("  param.name = '" + param.getName() + "'; param.value = '" + param.getValue() + "'");
			}

        	// Get the produces type based on the request Accept or _format parameter
			producesType = ServicesUtil.INSTANCE.getProducesType(headers, context, formParams);

    		// Check for valid and supported ResourceType
        	boolean isResourceTypeValid = false;

        	if (resourceType == null
        			|| (ResourceType.isValidResourceType(resourceType)
					&& ResourceType.isSupportedResourceType(resourceType))) {

        		isResourceTypeValid = true;
        	}

    		if (isResourceTypeValid) {

				// Get the query parameters that represent the search criteria
				// FHIR-293 - Code correction to create new MultivaluedHashMap to hold all params
				MultivaluedMap<String, String> queryParams = new MultivaluedHashMap<String, String>();
				queryParams.putAll(context.getQueryParameters());
				if (contextQueryParams != null) {
					queryParams.putAll(contextQueryParams);
				}

				// Get the _count parameter if present
				countString = ServicesUtil.INSTANCE.getUriParameter("_count", queryParams);
				if (countString == null && formParams != null) {
					countString = ServicesUtil.INSTANCE.getUriParameter("_count", formParams);
				}

				if (countString != null) {
					try {
						countInteger = Integer.valueOf(countString);
						log.info("search count = " + countString);
					}
					catch (Exception e) {
						log.severe("Exception parsing _count parameter to Integer! " + e.getMessage());
						countInteger = null;
					}
				}

				// Get the _summary parameter if present
				summaryString = ServicesUtil.INSTANCE.getUriParameter("_summary", queryParams);
				if (summaryString == null && formParams != null) {
					summaryString = ServicesUtil.INSTANCE.getUriParameter("_summary", formParams);
				}

				// Get the page parameter if present
				pageString = ServicesUtil.INSTANCE.getUriParameter("page", queryParams);
				if (pageString == null && formParams != null) {
					pageString = ServicesUtil.INSTANCE.getUriParameter("page", formParams);
				}

				if (pageString != null) {
					try {
						pageInteger = Integer.valueOf(pageString);
						log.info("page number = " + pageString);
					}
					catch (Exception e) {
						log.severe("Exception parsing page parameter to Integer! " + e.getMessage());
						pageInteger = null;
					}
				}

				String locationPath = null;
				if (overrideLocationPath != null) {
					locationPath = overrideLocationPath;
				}
				else {
					locationPath = context.getRequestUri().toString();
				}

				ResourceContainer resourceContainer = resourceService.search(queryParams, formParams, null, orderedParams, resourceType, locationPath, countInteger, pageInteger, summaryString, false);

				builder = responseBundle(producesType, resourceContainer, locationPath, responseFhirVersion);
			}
    		else {
				builder = responseInvalidResourceType(producesType, resourceType, responseFhirVersion);
			}

        } catch (Exception e) {
            // Handle generic exceptions
            String outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.FATAL, OperationOutcome.IssueType.EXCEPTION, e.getMessage(), null, null, producesType);

            builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(outcome).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);

            e.printStackTrace();
        }

        return builder.build();
    }

    /**
     * This returns a <code>Bundle</code> with the found history contents for the resource type.
     *
     * @param context
     * @param headers
     * @param contextQueryParams
     * @param id
     * @param resourceType
     * @return <code>Response</code>
     */
    public Response history(UriInfo context, HttpHeaders headers, MultivaluedMap<String,String> contextQueryParams, String id, String resourceType) {

        log.fine("[START] RESTResourceOps.history()");

        Response.ResponseBuilder builder = null;
        String producesType = null;
        Integer countInteger = null;
        String countString = null;
        Date sinceDate = null;
        String sinceString = null;
        Integer pageInteger = null;
        String pageString = null;
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

			// Check for valid and supported ResourceType
			boolean isResourceTypeValid = false;

			if (resourceType == null || (ResourceType.isValidResourceType(resourceType) && ResourceType.isSupportedResourceType(resourceType))) {

				isResourceTypeValid = true;
			}

			if (isResourceTypeValid) {

				// Check for valid FHIR resource id data type compliance
				if (id == null || (id != null && StringUtils.isValidFhirId(id))) {

					StringBuffer sbLogMsg = new StringBuffer("History Type is ");
					if (resourceType != null && id != null) {
						sbLogMsg.append(" Instance - Resource Type: ").append(resourceType).append("; Resource Id: ").append(id);
					}
					else if (resourceType != null && id == null) {
						sbLogMsg.append(" Resource - Resource Type: ").append(resourceType);
					}
					else {
						sbLogMsg.append(" Global");
					}
					log.info(sbLogMsg.toString());

					// Get the count and since parameters if present
					if (contextQueryParams != null) {
						countString = ServicesUtil.INSTANCE.getUriParameter("_count", contextQueryParams);
					}
					if (countString == null) {
						countString = ServicesUtil.INSTANCE.getUriParameter("_count", context);
					}
					if (contextQueryParams != null) {
						sinceString = ServicesUtil.INSTANCE.getUriParameter("_since", contextQueryParams);
					}
					if (sinceString == null) {
						sinceString = ServicesUtil.INSTANCE.getUriParameter("_since", context);
					}

					if (countString != null) {
						try {
							countInteger = Integer.valueOf(countString);
							log.info("history count = " + countString);
						}
						catch (Exception e) {
							log.severe("Exception parsing _count parameter to Integer! " + e.getMessage());
							countInteger = null;
						}
					}
					if (sinceString != null) {
						try {
							sinceDate = utcDateUtil.parseXMLDate(sinceString);
							log.info("history since = " + sinceString);
						}
						catch (Exception e) {
							log.severe("Exception parsing _since parameter to UTC Date! " + e.getMessage());
							sinceDate = null;
						}
					}

					// Get the page parameter if present
					if (contextQueryParams != null) {
						pageString = ServicesUtil.INSTANCE.getUriParameter("page", contextQueryParams);
					}
					if (pageString == null) {
						pageString = ServicesUtil.INSTANCE.getUriParameter("page", context);
					}

					if (pageString != null) {
						try {
							pageInteger = Integer.valueOf(pageString);
							log.info("page number = " + pageString);
						}
						catch (Exception e) {
							log.severe("Exception parsing page parameter to Integer! " + e.getMessage());
							pageInteger = null;
						}
					}

					String locationPath = context.getRequestUri().toString();

					ResourceContainer resourceContainer = resourceService.history(id, countInteger, sinceDate, pageInteger, locationPath, resourceType, null);

					if (resourceContainer != null && resourceContainer.getResponseStatus().equals(Response.Status.OK)) {

						builder = responseBundle(producesType, resourceContainer, locationPath, responseFhirVersion);
					}
					else {
						// No resource found; build OperationOutcome response resource
						String outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.INFORMATION, OperationOutcome.IssueType.NOTFOUND, "No resource found.", null, null, producesType);

						builder = Response.status(Response.Status.NOT_FOUND).entity(outcome).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);
					}
				}
				else {
					builder = responseInvalidResourceId(producesType, resourceType, id, responseFhirVersion);
				}
			}
			else {
				builder = responseInvalidResourceType(producesType, resourceType, responseFhirVersion);
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
     * @param resourcePath
     * @param producesType
     * @param resourceContainer
     * @param context
     * @return <code>Response.ResponseBuilder</code>
     * @throws URISyntaxException
     * @throws Exception
     */
    private Response.ResponseBuilder responseBundle(String producesType, ResourceContainer resourceContainer, String locationPath, String responseFhirVersion)
            throws URISyntaxException, Exception {

        log.fine("[START] RESTResourceOps.responseBundle()");

    	Response.ResponseBuilder builder;
        ByteArrayOutputStream oResourceSearch;

        if (resourceContainer != null) {
            builder = Response.status(resourceContainer.getResponseStatus()).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);

            if (resourceContainer.getResponseStatus().equals(Response.Status.OK)) {
                // Define URI location
                URI resourceLocation = new URI(URLEncoder.encode(locationPath, StandardCharsets.UTF_8.toString()));
                builder = builder.contentLocation(resourceLocation);

                if (resourceContainer.getBundle() != null) {

                    String sResourceSearch = "";

                    if (producesType.indexOf("xml") >= 0) {
                        // Convert Bundle to XML
                        oResourceSearch = new ByteArrayOutputStream();
        				XmlParser xmlParser = new XmlParser();
        				xmlParser.setOutputStyle(OutputStyle.PRETTY);
        				xmlParser.compose(oResourceSearch, resourceContainer.getBundle(), true);
                        sResourceSearch = oResourceSearch.toString();
                    } else {
                        // Convert Bundle to JSON
                        oResourceSearch = new ByteArrayOutputStream();
                        JsonParser jsonParser = new JsonParser();
                        jsonParser.setOutputStyle(OutputStyle.PRETTY);
                        jsonParser.compose(oResourceSearch, resourceContainer.getBundle());
                        sResourceSearch = oResourceSearch.toString();
                    }

                    builder = builder.entity(sResourceSearch);

                } else {
                	// Response status is not OK;; build OperationOutcome response resource
                	String message = "No response data found. Bundle is empty.";
                	if (!StringUtils.isNullOrEmpty(resourceContainer.getMessage())) {
                		message = resourceContainer.getMessage();
                	}

                	// Default outcome severity to ERROR and Type to TRANSIENT
                	OperationOutcome.IssueSeverity outcomeIssueSeverity = OperationOutcome.IssueSeverity.ERROR;
                	OperationOutcome.IssueType outcomeIssueType = OperationOutcome.IssueType.TRANSIENT;

                	// If returned response status is NOT_IMPLEMENTED, set severity to WARNING and type to NOTSUPPORTED
                	if (resourceContainer.getResponseStatus() != null && resourceContainer.getResponseStatus().equals(Response.Status.NOT_IMPLEMENTED)) {
                		outcomeIssueSeverity = OperationOutcome.IssueSeverity.WARNING;
                		outcomeIssueType = OperationOutcome.IssueType.NOTSUPPORTED;
                	}

                	String outcome = ServicesUtil.INSTANCE.getOperationOutcome(outcomeIssueSeverity, outcomeIssueType, message, null, null, producesType);

                    builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(outcome).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);
                }
            } else {
                // Something went wrong
            	String message = "Failure processing search.";
            	if (!StringUtils.isNullOrEmpty(resourceContainer.getMessage())) {
            		message = resourceContainer.getMessage();
            	}
                String outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.TRANSIENT, message, null, null, producesType);

                builder = Response.status(resourceContainer.getResponseStatus()).entity(outcome).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);
            }
        } else {
            // Something went wrong
            String outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.TRANSIENT, "No response container returned.", null, null, producesType);

            builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(outcome).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);
        }

        return builder;
    }

	/**
	 *
	 * @param locationPath
	 * @param producesType
	 * @param resourceContainer
	 * @param opsCode
	 * @param context
     * @param responseFhirVersion
	 * @return <code>Response.ResponseBuilder</code>
	 * @throws URISyntaxException
	 * @throws Exception
	 */
	private Response.ResponseBuilder buildResource(String locationPath, String producesType, ResourceContainer resourceContainer, Enum<Ops> opsCode, String responseFhirVersion) throws URISyntaxException, Exception {

		log.fine("[START] RESTResourceOps.buildResource()");

		Response.ResponseBuilder builder;
		String outcome = "";
		String sLastUpdate = null;
		String eTagVersion = "";
		EntityTag eTag = null;

		if (resourceContainer != null) {
			builder = Response.status(resourceContainer.getResponseStatus());

			if (resourceContainer.getResource() != null || opsCode.equals(Ops.DELETE)) {
				// Define URI location
                URI resourceLocation = new URI(URLEncoder.encode(locationPath, StandardCharsets.UTF_8.toString()));

				if (resourceContainer.getResource() != null) {
					// Get last update date
					Date lastUpdate = resourceContainer.getResource().getLastUpdate();
					log.info("Last Update Date: " + lastUpdate);


					if (lastUpdate != null) {
						sLastUpdate = utcDateUtil.formatUTCDateOffset(lastUpdate);
						log.info("Last Update UTC Date: " + sLastUpdate);
					}

					// ETag to hold the resource version id; format "W/##"
					eTagVersion = resourceContainer.getResource().getVersionId().toString();
					eTag = new EntityTag(eTagVersion, true);
				}

				// Build response based on the operation code
				if (opsCode.equals(Ops.READ)) {
					builder = responseStatus(producesType, eTag, resourceLocation, sLastUpdate, resourceContainer, builder, responseFhirVersion);

				}
				else if (opsCode.equals(Ops.CREATE)) {
					builder = responseStatus(producesType, eTag, resourceLocation, sLastUpdate, resourceContainer, builder, responseFhirVersion);

				}
				else if (opsCode.equals(Ops.UPDATE)) {
					builder = responseStatus(producesType, eTag, resourceLocation, sLastUpdate, resourceContainer, builder, responseFhirVersion);

				}
				else if (opsCode.equals(Ops.DELETE)) {
					if (resourceContainer.getResource() != null) {
						// if resource successfully deleted or already deleted; set ETag
						builder = builder.tag(eTag);
					}
					if (resourceContainer.getResponseStatus().equals(Response.Status.OK)) {
						// if resource already deleted; return OperationOutcome
						outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.INFORMATION, OperationOutcome.IssueType.INFORMATIONAL, resourceContainer.getResource().getResourceType() + " resource with resource id "
								+ resourceContainer.getResource().getResourceId() + " is already deleted.", null, null, producesType);
						builder = builder.entity(outcome).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);
					}

				}
				else if (opsCode.equals(Ops.VALIDATE)) {
					outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.INFORMATION, OperationOutcome.IssueType.INFORMATIONAL, resourceContainer.getResource().getResourceType() + " resource type validated.", null,
							null, producesType);
					builder = builder.entity(outcome).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);
				}

			}
			else {
				// No resource found; build OperationOutcome response resource
				outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.INFORMATION, OperationOutcome.IssueType.NOTFOUND, "No resource found.", null, null, producesType);

				builder = Response.status(Response.Status.NOT_FOUND).entity(outcome).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);
			}
		}
		else {
			// Something went wrong
			outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.TRANSIENT, "read failure, no response", null, null, producesType);

			builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(outcome).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);
		}

		return builder;
	}

    /**
     * @param producesType
     * @param eTag
     * @param resourceLocation
     * @param sLastUpdate
     * @param resourceContainer
     * @param builder
     * @param responseFhirVersion
     * @return <code>Response.ResponseBuilder</code>
     * @throws Exception
     */
    private Response.ResponseBuilder responseStatus(String producesType, EntityTag eTag, URI resourceLocation, String sLastUpdate, ResourceContainer resourceContainer,
            Response.ResponseBuilder builder, String responseFhirVersion) throws Exception {

        log.fine("[START] RESTResourceOps.responseStatus()");

    	ByteArrayInputStream iResource;
        ByteArrayOutputStream oResource;

        if (resourceContainer.getResponseStatus().equals(Response.Status.OK)
                || resourceContainer.getResponseStatus().equals(Response.Status.CREATED)
                || resourceContainer.getResponseStatus().equals(Response.Status.BAD_REQUEST)) {

        	if (resourceContainer.getResource().getResourceContents() != null) {
	            if (producesType.indexOf("xml") >= 0) {
	            	String out = new String(resourceContainer.getResource().getResourceContents());
	                builder = builder.entity(out).tag(eTag).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);

	            } else {
	                // Convert XML contents to JSON
	                iResource = new ByteArrayInputStream(resourceContainer.getResource().getResourceContents());
	                XmlParser xmlP = new XmlParser();
	                Resource resource = xmlP.parse(iResource);
	                oResource = new ByteArrayOutputStream();
	                JsonParser jsonParser = new JsonParser();
	                jsonParser.setOutputStyle(OutputStyle.PRETTY);
	                jsonParser.compose(oResource, resource);
	                String sResource = oResource.toString();

	                builder = builder.entity(sResource).tag(eTag).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);
	            }
        	}
        	else {
        		// resourceContents are set to null for create with Prefer=minimal
        		 builder = builder.tag(eTag);
        	}

        	if (!resourceContainer.getResponseStatus().equals(Response.Status.BAD_REQUEST)) {
	            builder = builder.contentLocation(resourceLocation);
	            builder = builder.location(resourceLocation);
	            builder = builder.header("Last-Modified", sLastUpdate);
        	}
        }
        else if (resourceContainer.getResponseStatus().equals(Response.Status.GONE)) {
        	// No resource found; build OperationOutcome response resource
            String outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.INFORMATION, OperationOutcome.IssueType.PROCESSING, "The requested resource is gone.", null, null, producesType);

            builder = Response.status(Response.Status.GONE).entity(outcome).tag(eTag).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);
        }

        return builder;
    }

    /**
     * @param resourceType
     * @param builder
     * @param id
     * @param responseFhirVersion
     * @return <code>Response.ResponseBuilder</code>
     * @throws Exception
     */
    private Response.ResponseBuilder responseInvalidResourceId(String producesType, String resourceType, String id, String responseFhirVersion) throws Exception {

        log.fine("[START] RESTResourceOps.responseInvalidResourceId()");

    	String message = "Invalid FHIR resource id. The value '" + id + "' does not conform to the FHIR id data type regex definition [A-Za-z0-9\\-\\.]{1,64}";

    	// Invalid FHIR resource id data type
        String outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.TRANSIENT, message, null, null, producesType);

        Response.ResponseBuilder builder = Response.status(Response.Status.BAD_REQUEST).entity(outcome).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);

        return builder;
    }

    /**
     * @param producesType
     * @param resourceType
     * @param responseFhirVersion
     * @return <code>Response.ResponseBuilder</code>
     * @throws Exception
     */
    private Response.ResponseBuilder responseInvalidResourceType(String producesType, String resourceType, String responseFhirVersion) throws Exception {

        log.fine("[START] RESTResourceOps.responseInvalidResourceType()");

    	String message = "";
    	if (!ResourceType.isValidResourceType(resourceType)) {
    		message = "Not a valid FHIR resource type!";
    	} else {
    		message = "Server does not support this FHIR resource type.";
    	}
    	// Unsupported ResourceType
        String outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.TRANSIENT, message, null, null, producesType);

        Response.ResponseBuilder builder = Response.status(Response.Status.NOT_FOUND).entity(outcome).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);

        return builder;
    }

    /**
     * @param producesType
     * @param compartment
     * @param responseFhirVersion
     * @return <code>Response.ResponseBuilder</code>
     * @throws Exception
     */
    private Response.ResponseBuilder responseInvalidCompartment(String producesType, String compartment, String responseFhirVersion) throws Exception {

        log.fine("[START] RESTResourceOps.responseInvalidCompartment()");

    	String message = "";
    	if (!ResourceType.isValidCompartment(compartment)) {
    		message = "Not a valid FHIR compartment!";
    	} else {
    		message = "Server does not support this FHIR compartment.";
    	}
    	// Unsupported ResourceType
        String outcome = ServicesUtil.INSTANCE.getOperationOutcome(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.TRANSIENT, message, null, null, producesType);

        Response.ResponseBuilder builder = Response.status(Response.Status.NOT_FOUND).entity(outcome).type(producesType + Constants.CHARSET_UTF8_EXT + responseFhirVersion);

        return builder;
    }

    private enum Ops {

        READ, VREAD, UPDATE, DELETE, CREATE, VALIDATE, HISTORY, SEARCH, SEARCHALL;

    }

}