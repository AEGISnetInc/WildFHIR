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

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.hl7.fhir.r4.formats.JsonParser;
import org.hl7.fhir.r4.formats.XmlParser;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity;
import org.hl7.fhir.r4.model.OperationOutcome.IssueType;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.utilities.xhtml.XhtmlNode;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleEntryResponseComponent;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.Bundle.HTTPVerb;

import net.aegis.fhir.model.ResourceContainer;
import net.aegis.fhir.model.ResourceType;
import net.aegis.fhir.service.util.ServicesUtil;
import net.aegis.fhir.service.util.UTCDateUtil;
import net.aegis.fhir.service.util.UUIDUtil;

/**
 * Transaction services for performing a set of dependent actions. Multiple actions on multiple
 * resources of the same or different types may be submitted, and they may be a mix of basic
 * interactions (e.g. read, search, create, update, delete, etc.), or those defined in the
 * operations framework.
 *
 * The @Stateless annotation eliminates the need for manual transaction demarcation
 *
 * @author richard.ettema
 *
 */
@Stateless
public class TransactionService {

	private Logger log = Logger.getLogger("TransactionService");

	@Inject
	RESTResourceOps resourceOps;

	@Inject
    UTCDateUtil utcDateUtil;

	private XmlParser xmlP = new XmlParser();
	private JsonParser jsonP = new JsonParser();

	/**
	 * Process the transaction bundle where each bundle entry is dependently executed based on the
	 * corresponding entry request. The returned transaction-response bundle will contain a bundle
	 * entry for each request with the entry response populated.
	 *
	 * @param context
	 * @param headers
	 * @param contentType
	 * @param producesType
	 * @param bundleToProcess
	 * @param locationPath
	 * @param authMapPatient
	 * @return <code>ResourceContainer</code>
	 * @throws Exception
	 */
	public ResourceContainer transaction(UriInfo context, HttpHeaders headers, String contentType, String producesType, Bundle bundleToProcess, String locationPath, List<String> authMapPatient) throws Exception {

		log.fine("[START] TransactionService.transaction()");

		ResourceContainer resourceContainer = new ResourceContainer();
		Bundle bundleResponse = null;
		Boolean txOutcomeAck = Boolean.valueOf(false);
		String txOutcomeAckText = null;

		try {
			resourceContainer.setBundle(bundleResponse);
			resourceContainer.setResponseStatus(Response.Status.NOT_IMPLEMENTED);
			resourceContainer.setMessage("Transaction processing not supported");

			// 01 - Create and pre-populate transaction response bundle
			log.fine("===== TransactionService -  01 - Create and pre-populate transaction response bundle");
			bundleResponse = new Bundle();

			bundleResponse.setId(UUIDUtil.getGUID());
			Meta bundleMeta = new Meta();
			bundleMeta.setVersionId("1");
			bundleMeta.setLastUpdated(new Date());
			bundleResponse.setMeta(bundleMeta);
			bundleResponse.setType(BundleType.TRANSACTIONRESPONSE);

			// Check for transaction entries to process
			if (bundleToProcess != null && bundleToProcess.hasEntry()) {
				// 02 - Pre-populate transaction response bundle entries
				log.fine("===== TransactionService -  02 - Pre-populate transaction response bundle entries; total entries to process [" + bundleToProcess.getEntry().size() + "]");

				Map<String,String> postFullUrlMap = new HashMap<String,String>();
				BundleEntryComponent bundleResponseEntry = null;
				List<BundleEntryComponent> bundleResponseEntries = new ArrayList<BundleEntryComponent>();

				for (BundleEntryComponent bundleToProcessEntry : bundleToProcess.getEntry()) {
					bundleResponseEntry = new BundleEntryComponent();
					bundleResponseEntry.setFullUrl(bundleToProcessEntry.getFullUrl()); // Copy request entry fullUrl; to be replaced

					if (bundleToProcessEntry.hasFullUrl() && bundleToProcessEntry.getFullUrl().contains("urn:")) {
						// Initialize fullUrl UUID mapping
						postFullUrlMap.put(bundleToProcessEntry.getFullUrl(), "");
						log.fine("     ----- TransactionService - postFullUrlMap - Adding [" + bundleToProcessEntry.getFullUrl() + "]");
					}

					bundleResponseEntry.setResource(bundleToProcessEntry.getResource()); // Copy request entry resource; to be replaced/removed
					bundleResponseEntry.setRequest(bundleToProcessEntry.getRequest()); // Copy request entry request; to be removed

					BundleEntryResponseComponent bundleToProcessEntryResponse = new BundleEntryResponseComponent(); // Create new response with unknown status
					bundleToProcessEntryResponse.setStatus("UNKNOWN");
					bundleResponseEntry.setResponse(bundleToProcessEntryResponse);

					bundleResponseEntries.add(bundleResponseEntry);
				}

				MultivaluedMap<String, String> requestHeaderParams = null;
				MultivaluedMap<String, String> urlPathParams = null;

				// 03 - Process DELETE
				int entryCount = 0;
				for (BundleEntryComponent bundleDeleteEntry : bundleResponseEntries) {
					/*
					 * Only process delete requests
					 */
					if (bundleDeleteEntry.hasRequest() && bundleDeleteEntry.getRequest().hasMethod() && bundleDeleteEntry.getRequest().getMethod().equals(HTTPVerb.DELETE)) {
						log.fine("===== TransactionService - PROCESS DELETE - Bundle.entry[" + entryCount + "]");
						try {
							// Delete is based solely on the url value
							if (bundleDeleteEntry.getRequest().hasUrl()) {
								String urlValue = bundleDeleteEntry.getRequest().getUrl();

								// extract resource id from url; should be found; if not found, treat as conditional delete
								String resourceId = ServicesUtil.INSTANCE.extractResourceIdFromURL(urlValue);
								if (resourceId == null || resourceId.isEmpty()) {
									resourceId = null;
								}

								String baseUrl = ServicesUtil.INSTANCE.extractBaseURL(urlValue);
								String resourceType = ResourceType.findValidResourceType(baseUrl);

								// ignore request header params from request elements
								//requestHeaderParams = getRequestHeaderParams(bundleDeleteEntry);

								// build url params from url parameters
								String urlParams = ServicesUtil.INSTANCE.extractURLParams(urlValue);
								List<NameValuePair> params = URLEncodedUtils.parse(urlParams, Charset.defaultCharset());
								urlPathParams = ServicesUtil.INSTANCE.listNameValuePairToMultivaluedMapString(params);

								Response deleteResponse = resourceOps.delete(context, headers, urlPathParams, resourceId, resourceType);

								setResponseParams(deleteResponse, bundleDeleteEntry, producesType, entryCount, null);
							}
							else {
								// url is missing; report an error in the outcome
								this.setErrorResponseParams(bundleDeleteEntry, "400", OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.INCOMPLETE, "request url is missing", null, "Bundle.entry[" + entryCount + "].request.url");
							}
						} catch (Throwable e) {
							// record exception in outcome
							this.setErrorResponseParams(bundleDeleteEntry, "500", OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.EXCEPTION, "exception caught processing delete request", e.getMessage(), "Bundle.entry[" + entryCount + "].request");
							log.severe(e.getMessage());
						}
					}

					entryCount++;
				}

				/*
				 *  04,05 - Wrap POST and PUT processing in a cycle in order to resolve variable references
				 *
				 *  Once max cycles reached or all variable references have been resolved, drop out of cycles
				 */
				int cycleCount = 0;
				int cycleMax = 4;
				while (cycleCount < cycleMax) {
					cycleCount++;
					log.fine("===== TransactionService - PROCESS POST,PUT Cycle [" + cycleCount + "]");

					// 04 - Process POST (Create/Search/Extended Operations)
					entryCount = 0;
					for (BundleEntryComponent bundlePostEntry : bundleResponseEntries) {
						/*
						 * Only process post requests - only supporting create
						 */
						if (bundlePostEntry.hasRequest() && bundlePostEntry.getRequest().hasMethod() && bundlePostEntry.getRequest().getMethod().equals(HTTPVerb.POST)) {
							log.fine("===== TransactionService - PROCESS POST - Bundle.entry[" + entryCount + "]");
							try {
								// Post is based on the resource, request.url and request header values
								if (bundlePostEntry.getRequest().hasUrl()) {
									String urlValue = bundlePostEntry.getRequest().getUrl();

									// extract resource id from url
									String resourceId = ServicesUtil.INSTANCE.extractResourceIdFromURL(urlValue);

									if (resourceId == null || resourceId.isEmpty()) {
										resourceId = null;
										String baseUrl = ServicesUtil.INSTANCE.extractBaseURL(urlValue);
										String resourceType = ResourceType.findValidResourceType(baseUrl);

										// build request header params from request elements
										requestHeaderParams = getRequestHeaderParams(bundlePostEntry);

										// ignore url parameters
										//String urlParams = ServicesUtil.INSTANCE.extractURLParams(urlValue);
										//List<NameValuePair> params = URLEncodedUtils.parse(urlParams, Charset.defaultCharset());
										//urlPathParams = ServicesUtil.INSTANCE.listNameValuePairToMultivaluedMapString(params);

										// Check for resource
										if (bundlePostEntry.hasResource()) {
											String resourceString = null;
											if (contentType.indexOf("xml") >= 0) {
												resourceString = xmlP.composeString(bundlePostEntry.getResource());
											}
											else {
												resourceString = jsonP.composeString(bundlePostEntry.getResource());
											}

											/*
											 *  Process resource contents to locate any variable references, urn:
											 *  If all replacements are successful, continue with create; otherwise, skip
											 *  and try again in the next cycle
											 */
											StringBuilder newResourceString = new StringBuilder("");
											boolean processResult = processVariableReferences(resourceString, postFullUrlMap, newResourceString);

											if (processResult) {
												Response createResponse = resourceOps.create(context, headers, requestHeaderParams, newResourceString.toString(), resourceType, resourceId);

												setResponseParams(createResponse, bundlePostEntry, producesType, entryCount, postFullUrlMap);
											}
											else {
												// variable reference map not found; report a temporary error in the outcome as a placeholder; re-try in the next cycle
												this.setTempErrorResponseParams(bundlePostEntry, "400", OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.NOTFOUND, "variable resource reference '" + newResourceString.toString() + "' was not resolved in prior Bundle entry operation; required for post operation", null, "Bundle.entry[" + entryCount + "]");
											}
										}
										else {
											// resource not found; report an error in the outcome
											this.setErrorResponseParams(bundlePostEntry, "400", OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.INCOMPLETE, "resource not found in Bundle entry; required for post operation", null, "Bundle.entry[" + entryCount + "]");
										}
									}
									else {
										// resource id found in url; report an error in the outcome
										this.setErrorResponseParams(bundlePostEntry, "400", OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.INVALID, "resource id found in url; not allowed for create", null, "Bundle.entry[" + entryCount + "].request.url");
									}
								}
								else {
									// url is missing; report an error in the outcome
									this.setErrorResponseParams(bundlePostEntry, "400", OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.INCOMPLETE, "request url is missing", null, "Bundle.entry[" + entryCount + "].request.url");
								}
							} catch (Throwable e) {
								// record exception in outcome
								this.setErrorResponseParams(bundlePostEntry, "500", OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.EXCEPTION, "exception caught processing post request", e.getMessage(), "Bundle.entry[" + entryCount + "].request");
								log.severe(e.getMessage());
							}
						}

						entryCount++;
					}

					// 05 - Process PUT (Update/Create)
					entryCount = 0;
					for (BundleEntryComponent bundlePutEntry : bundleResponseEntries) {
						/*
						 * Only process put requests
						 */
						if (bundlePutEntry.hasRequest() && bundlePutEntry.getRequest().hasMethod() && bundlePutEntry.getRequest().getMethod().equals(HTTPVerb.PUT)) {
							log.fine("===== TransactionService - PROCESS PUT - Bundle.entry[" + entryCount + "]");
							try {
								// Put is based on the resource, request.url and request header values
								if (bundlePutEntry.getRequest().hasUrl()) {
									String urlValue = bundlePutEntry.getRequest().getUrl();

									// extract resource id from url; should be found; if not found, then treat as conditional update
									String resourceId = ServicesUtil.INSTANCE.extractResourceIdFromURL(urlValue);
									if (resourceId == null || resourceId.isEmpty()) {
										resourceId = null;
									}

									String baseUrl = ServicesUtil.INSTANCE.extractBaseURL(urlValue);
									String resourceType = ResourceType.findValidResourceType(baseUrl);

									// build request header params from request elements
									requestHeaderParams = getRequestHeaderParams(bundlePutEntry);

									// build url parameters
									String urlParams = ServicesUtil.INSTANCE.extractURLParams(urlValue);
									List<NameValuePair> params = URLEncodedUtils.parse(urlParams, Charset.defaultCharset());
									urlPathParams = ServicesUtil.INSTANCE.listNameValuePairToMultivaluedMapString(params);

									// Check for resource
									if (bundlePutEntry.hasResource()) {
										String resourceString = null;
										if (contentType.indexOf("xml") >= 0) {
											resourceString = xmlP.composeString(bundlePutEntry.getResource());
										}
										else {
											resourceString = jsonP.composeString(bundlePutEntry.getResource());
										}

										/*
										 *  Process resource contents to locate any variable references, urn:
										 *  If all replacements are successful, continue with update; otherwise, skip
										 *  and try again in the next cycle
										 */
										StringBuilder newResourceString = new StringBuilder("");
										boolean processResult = processVariableReferences(resourceString, postFullUrlMap, newResourceString);

										if (processResult) {
											Response updateResponse = resourceOps.update(context, headers, requestHeaderParams, urlPathParams, resourceId, newResourceString.toString(), resourceType);

											setResponseParams(updateResponse, bundlePutEntry, producesType, entryCount, postFullUrlMap);
										}
										else {
											// variable reference map not found; report an error in the outcome
											this.setTempErrorResponseParams(bundlePutEntry, "400", OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.NOTFOUND, "variable resource reference '" + newResourceString.toString() + "' was not resolved in prior Bundle entry operation; required for put operation", null, "Bundle.entry[" + entryCount + "]");
										}
									}
									else {
										// resource not found; report an error in the outcome
										this.setErrorResponseParams(bundlePutEntry, "400", OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.INCOMPLETE, "resource not found in Bundle entry; required for put operation", null, "Bundle.entry[" + entryCount + "]");
									}
								}
								else {
									// url is missing; report an error in the outcome
									this.setErrorResponseParams(bundlePutEntry, "400", OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.INCOMPLETE, "request url is missing", null, "Bundle.entry[" + entryCount + "].request.url");
								}
							} catch (Throwable e) {
								// record exception in outcome
								this.setErrorResponseParams(bundlePutEntry, "500", OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.EXCEPTION, "exception caught processing put request", e.getMessage(), "Bundle.entry[" + entryCount + "].request");
								log.severe(e.getMessage());
							}
						}

						entryCount++;
					}

				} // 04,05 END CYCLE

				// 06 - Process PATCH (Update)

				// 07 - Process GET (History/Read/Search/VRead Operations)
				entryCount = 0;
				for (BundleEntryComponent bundleGetEntry : bundleResponseEntries) {
					/*
					 * Only process get requests
					 */
					if (bundleGetEntry.hasRequest() && bundleGetEntry.getRequest().hasMethod() && bundleGetEntry.getRequest().getMethod().equals(HTTPVerb.GET)) {
						log.fine("===== TransactionService - PROCESS GET - Bundle.entry[" + entryCount + "]");
						try {
							// Put is based on the resource, request.url and request header values
							if (bundleGetEntry.getRequest().hasUrl()) {
								String urlValue = bundleGetEntry.getRequest().getUrl();

								// extract resource id from url; should be found for a read; if not found, treat as a search
								String resourceId = ServicesUtil.INSTANCE.extractResourceIdFromURL(urlValue);
								if (resourceId == null || resourceId.isEmpty()) {
									resourceId = null;
								}

								String baseUrl = ServicesUtil.INSTANCE.extractBaseURL(urlValue);
								String txLocationPath = urlValue;
								String txBaseUrl = null;
								if (!baseUrl.contains("http")) {
									txBaseUrl = ServicesUtil.INSTANCE.extractBaseURL(locationPath, baseUrl);
									txLocationPath = txBaseUrl + urlValue;
								}
								log.fine("  txLocationPath = '" + txLocationPath + "'");

								String resourceType = ResourceType.findValidResourceType(baseUrl);

								boolean hasHistoryInPath = baseUrl.contains("_history");
								String versionId = null;
								if (hasHistoryInPath == true) {
									versionId = ServicesUtil.INSTANCE.extractVersionIdFromURL(urlValue);
								}

								// build request header params from request elements
								requestHeaderParams = getRequestHeaderParams(bundleGetEntry);

								// build url parameters
								String urlParams = ServicesUtil.INSTANCE.extractURLParams(urlValue);
								List<NameValuePair> params = URLEncodedUtils.parse(urlParams, Charset.defaultCharset());
								urlPathParams = ServicesUtil.INSTANCE.listNameValuePairToMultivaluedMapString(params);

								if (bundleGetEntry.hasResource()) {
									// ignore resource (should this be an error?)
								}

								/*
								 * Determine appropriate FHIR action: history, read, search or vread
								 */
								Response getResponse = null;

								if (resourceType != null && resourceId != null && hasHistoryInPath == false) {
									// Check for read operation
									getResponse = resourceOps.resourceTypeRead(context, headers, requestHeaderParams, urlPathParams, resourceId, resourceType);
								}
								else if (resourceType != null && resourceId != null && hasHistoryInPath == true && versionId != null) {
									// Check for vread operation
									getResponse = resourceOps.resourceTypeVRead(context, headers, resourceId, versionId, resourceType);
								}
								else if (resourceType != null && hasHistoryInPath == true && versionId == null) {
									// Check for history operation
									getResponse = resourceOps.history(context, headers, urlPathParams, resourceId, resourceType);
								}
								else if (resourceId == null && hasHistoryInPath == false && versionId == null) {
									// Check for search operation
									// Use txLocationPath from transaction bundle entry request
									getResponse = resourceOps.search(headers, context, urlPathParams, resourceType, null, txLocationPath);
								}

								if (getResponse != null) {
									setResponseParams(getResponse, bundleGetEntry, producesType, entryCount, null);
								}
								else {
									// could not determine FHIR action; record in outcome
									this.setErrorResponseParams(bundleGetEntry, "400", OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.UNKNOWN, "could not determine FHIR get action based on request definition", null, "Bundle.entry[" + entryCount + "].request");
								}
							}
							else {
								// url is missing; report an error in the outcome
								this.setErrorResponseParams(bundleGetEntry, "400", OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.INCOMPLETE, "request url is missing", null, "Bundle.entry[" + entryCount + "].request.url");
							}
						} catch (Throwable e) {
							// record exception in outcome
							this.setErrorResponseParams(bundleGetEntry, "500", OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.EXCEPTION, "exception caught processing get request", e.getMessage(), "Bundle.entry[" + entryCount + "].request");
							log.severe(e.getMessage());
						}
					}

					entryCount++;
				}

				// 08 - Process unknown
				entryCount = 0;
				for (BundleEntryComponent bundleUnknownEntry : bundleResponseEntries) {
					/*
					 * Remove any remaining entry.request elements
					 */
					if (bundleUnknownEntry.hasRequest()) {
						log.fine("===== TransactionService - PROCESS REMAINING REQUESTS - Bundle.entry[" + entryCount + "]");

						// Response not already set, create new response
						if (!bundleUnknownEntry.hasResponse()) {
							try {
								// set outcome with information from original request
								StringBuilder requestInfo = new StringBuilder();
								if (bundleUnknownEntry.getRequest().hasMethod()) {
									requestInfo.append("request.method[").append(bundleUnknownEntry.getRequest().getMethod().toCode()).append("]; ");
								}
								if (bundleUnknownEntry.getRequest().hasIfNoneMatch()) {
									requestInfo.append("request.ifNoneMatch[").append(bundleUnknownEntry.getRequest().getIfNoneMatch()).append("]; ");
								}
								if (bundleUnknownEntry.getRequest().hasIfModifiedSince()) {
									requestInfo.append("request.ifNoneMatch[").append(utcDateUtil.formatHTTPDate(bundleUnknownEntry.getRequest().getIfModifiedSince())).append("]; ");
								}
								if (bundleUnknownEntry.getRequest().hasIfMatch()) {
									requestInfo.append("request.ifMatch[").append(bundleUnknownEntry.getRequest().getIfMatch()).append("]; ");
								}
								if (bundleUnknownEntry.getRequest().hasIfNoneExist()) {
									requestInfo.append("request.ifNoneExist[").append(bundleUnknownEntry.getRequest().getIfNoneExist()).append("]; ");
								}

								this.setErrorResponseParams(bundleUnknownEntry, "400", OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.NOTSUPPORTED, "request not supported", requestInfo.toString(), "Bundle.entry[" + entryCount + "]");
							} catch (Throwable e) {
								// record exception in outcome
								this.setErrorResponseParams(bundleUnknownEntry, "500", OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.EXCEPTION, "exception caught processing unknown request", e.getMessage(), "Bundle.entry[" + entryCount + "].request");
								log.severe(e.getMessage());
							}
						}

						/*
						 * Final clean up - delete the Bundle.entry.resource and Bundle.entry.request
						 */
						bundleUnknownEntry.setResource(null);
						bundleUnknownEntry.setRequest(null);
					}

					entryCount++;
				}

				/*
				 * Check for Nictiz txOutcomeAck to add final OperationOutcome with information issue
				 * containing txOutcomeAckText text.
				 */
				if (txOutcomeAck.booleanValue() == true) {
					log.fine("===== TransactionService - PROCESS TX OUTCOME ACK = TRUE");
					bundleResponseEntry = new BundleEntryComponent();
					String ooResourceId = UUIDUtil.getUUID(false);
					bundleResponseEntry.setFullUrl("urn:uuid:" + ooResourceId);

					OperationOutcome finalInfoOO = ServicesUtil.INSTANCE.getOperationOutcomeResource(IssueSeverity.INFORMATION, IssueType.INFORMATIONAL, txOutcomeAckText);
					finalInfoOO.setId(ooResourceId);
					finalInfoOO.setLanguage("nl-NL");
					XhtmlNode ooDiv = finalInfoOO.getText().getDiv();
					ooDiv.setAttribute("lang", "nl-NL");
					ooDiv.setAttribute("xml:lang", "nl-NL");
					bundleResponseEntry.setResource(finalInfoOO);

					BundleEntryResponseComponent bundleEntryResponse = new BundleEntryResponseComponent();
					bundleEntryResponse.setStatus("200");
					bundleResponseEntry.setResponse(bundleEntryResponse);

					bundleResponseEntries.add(bundleResponseEntry);
				}
				else {
					log.fine("===== TransactionService - SKIP TX OUTCOME ACK = FALSE");
				}

				bundleResponse.setEntry(bundleResponseEntries);

			}

			resourceContainer.setBundle(bundleResponse);
			resourceContainer.setResponseStatus(Response.Status.OK);
			resourceContainer.setMessage("Transaction processing completed. Check transaction response for entry outcomes.");

		} catch (Exception e) {
			// Exception caught
			resourceContainer.setResource(null);
			resourceContainer.setResponseStatus(Response.Status.INTERNAL_SERVER_ERROR);
			resourceContainer.setMessage(e.getMessage());

			log.severe(e.getMessage());
			// Exception not thrown to allow operation to complete
		}

		log.fine("[END] TransactionService.transaction()");

		return resourceContainer;

	}

	/**
	 * @param bundleEntry
	 * @return
	 */
	private MultivaluedMap<String, String> getRequestHeaderParams(BundleEntryComponent bundleEntry) {
		MultivaluedHashMap<String, String> requestParams = null;

		if (bundleEntry.hasRequest()) {
			if (bundleEntry.getRequest().hasIfNoneMatch()) {
				if (requestParams == null) {
					requestParams = new MultivaluedHashMap<String, String>();
				}
				requestParams.add("If-None-Match", bundleEntry.getRequest().getIfNoneMatch());
			}
			if (bundleEntry.getRequest().hasIfModifiedSince()) {
				if (requestParams == null) {
					requestParams = new MultivaluedHashMap<String, String>();
				}
				requestParams.add("If-Modified-Since", utcDateUtil.formatHTTPDate(bundleEntry.getRequest().getIfModifiedSince()));
			}
			if (bundleEntry.getRequest().hasIfMatch()) {
				if (requestParams == null) {
					requestParams = new MultivaluedHashMap<String, String>();
				}
				requestParams.add("If-Match", bundleEntry.getRequest().getIfMatch());
			}
			if (bundleEntry.getRequest().hasIfNoneExist()) {
				if (requestParams == null) {
					requestParams = new MultivaluedHashMap<String, String>();
				}
				requestParams.add("If-None-Exist", bundleEntry.getRequest().getIfNoneExist());
			}
		}

		return requestParams;
	}

	/**
	 * @param resourceString
	 * @param postFullUrlMap
	 * @param newResourceString
	 * @return
	 */
	private boolean processVariableReferences(String resourceString, Map<String,String> postFullUrlMap, StringBuilder newResourceString) {
		log.fine("===== TransactionService - processVariableReferences - START");
		boolean result = true;
		String missingKey = "??";

		for (Entry<String, String> e : postFullUrlMap.entrySet()) {

			// Check for resource containing map key
			if (resourceString.contains(e.getKey())) {

				log.fine("     ----- TransactionService - processVariableReferences - found map key [" + e.getKey() + "]");

				if (e.getValue() != null && !e.getValue().isEmpty()) {
					log.fine("     ----- TransactionService - processVariableReferences - found map value [" + e.getValue() + "]");
					resourceString = resourceString.replace(e.getKey(), e.getValue());
					log.fine(resourceString);
				}
				else {
					log.fine("     ----- BatchService - processVariableReferences - DID NOT FIND map value for key [" + e.getKey() + "]");
					result = false;
					missingKey = e.getKey();
					break;
				}
			}
		}

		if (result) {
			newResourceString.append(resourceString);
		}
		else {
			newResourceString.append(missingKey);
		}

		return result;
	}

	/**
	 * @param response
	 * @param entryResponse
	 * @param producesType
	 * @param entryCount
	 * @param postFullUrlMap
	 */
	private void setResponseParams(Response response, BundleEntryComponent bundleEntry, String producesType, int entryCount, Map<String,String> postFullUrlMap) {

		BundleEntryResponseComponent entryResponse = null;
		if (!bundleEntry.hasResponse()) {
			entryResponse = new BundleEntryResponseComponent();
			bundleEntry.setResponse(entryResponse);
		}
		else {
			entryResponse = bundleEntry.getResponse();
		}

		if (response != null) {
			OperationOutcome.OperationOutcomeIssueComponent issue = null;
			try {
				entryResponse.setStatus(Integer.toString(response.getStatus()));
				if (response.getLocation() != null) {
					String location = response.getLocation().toString();
					entryResponse.setLocation(location);

					// Look for variable reference in the map
					if (postFullUrlMap != null) {
						log.fine("     ----- TransactionService - setResponseParams - get map for [" + bundleEntry.getFullUrl() + "]");
						String postFullUrlVarRef = postFullUrlMap.get(bundleEntry.getFullUrl());
						if (postFullUrlVarRef != null) {
							// Found, assign mapped reference
							String relativeReference = ServicesUtil.INSTANCE.extractRelativeReferenceFromURL(response.getLocation().toString());
							postFullUrlMap.put(bundleEntry.getFullUrl(), relativeReference);
							log.fine("     ----- TransactionService - setResponseParams - put map for [" + bundleEntry.getFullUrl() + "] with value [" + relativeReference + "]");
						}
					}

					// FHIR-154 - If location contains _history, remove
					if (location.contains("/_history")) {
						bundleEntry.setFullUrl(ServicesUtil.INSTANCE.extractBaseURL(location, "/_history"));
					}
					else {
						bundleEntry.setFullUrl(location);
					}
				}
				if (response.getHeaderString(HttpHeaders.ETAG) != null) {
					entryResponse.setEtag(response.getHeaderString(HttpHeaders.ETAG));
				}
				if (response.getHeaderString(HttpHeaders.LAST_MODIFIED) != null) {
					try {
						// Get last modified date directly from HTTP header in order to
						// process formatted date with local date utility due to JBoss
						// Resteasy ASCII time formatting issue
						entryResponse.setLastModified(utcDateUtil.parseHTTPDate(response.getHeaderString(HttpHeaders.LAST_MODIFIED)));
					}
					catch (Throwable e) {
						// generate outcome issue to add to response.outcome
						issue = ServicesUtil.INSTANCE.getOperationOutcomeIssueComponent(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.EXCEPTION, "exception caught processing last_modified from response '" + response.getHeaderString(HttpHeaders.LAST_MODIFIED) + "'", e.getMessage(), "Bundle.entry[" + entryCount + "].response");
					}
				}

				if (response.hasEntity()) {
					String entity = (String) response.getEntity();
					Resource resource = null;

					try {
						if (producesType.indexOf("xml") >= 0) {
							resource = xmlP.parse(entity.getBytes());
						}
						else {
							resource = jsonP.parse(entity.getBytes());
						}

						// Check resource type: if OperationOutcome, set outcome; else, set resource
						if (resource instanceof OperationOutcome) {
							if (issue != null) {
								((OperationOutcome) resource).addIssue(issue);
							}
							entryResponse.setOutcome(resource);
							bundleEntry.setResource(null);
						}
						else {
							bundleEntry.setResource(resource);
							if (issue != null) {
								OperationOutcome outcome = new OperationOutcome();
								outcome.addIssue(issue);
								entryResponse.setOutcome(outcome);
							}
							else {
								// No issue(s) found, make sure outcome is empty
								entryResponse.setOutcome(null);
							}
						}
					}
					catch (Throwable e) {
						// record exception in outcome
						OperationOutcome outcome = ServicesUtil.INSTANCE.getOperationOutcomeResource(OperationOutcome.IssueSeverity.FATAL, OperationOutcome.IssueType.PROCESSING, "response from request is not a valid FHIR resource", null, "Bundle.entry[" + entryCount + "].request");
						entryResponse.setOutcome(outcome);
						log.severe(e.getMessage());
					}
				}
			}
			catch (Throwable e) {
				// record exception in outcome
				this.setErrorResponseParams(bundleEntry, "500", OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.EXCEPTION, "exception caught processing setting response for request", e.getMessage(), "Bundle.entry[" + entryCount + "].request");
				log.severe(e.getMessage());
			}
		}
		else {
			// record response empty or missing
			this.setErrorResponseParams(bundleEntry, "500", OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.PROCESSING, "response from request is empty or missing", null, "Bundle.entry[" + entryCount + "].request");
		}

		/*
		 * Final clean up - delete the Bundle.entry.request
		 */
		bundleEntry.setRequest(null);
	}

	private void setErrorResponseParams(BundleEntryComponent bundleEntry, String responseStatus, OperationOutcome.IssueSeverity severity, OperationOutcome.IssueType type, String details, String diagnostics, String location) {

		BundleEntryResponseComponent entryResponse = null;
		if (!bundleEntry.hasResponse()) {
			entryResponse = new BundleEntryResponseComponent();
			bundleEntry.setResponse(entryResponse);
		}
		else {
			entryResponse = bundleEntry.getResponse();
		}

		entryResponse.setStatus(responseStatus);
		OperationOutcome outcome = ServicesUtil.INSTANCE.getOperationOutcomeResource(severity, type, details, diagnostics, location);
		entryResponse.setOutcome(outcome);

		// Remove entry.resource on error
		bundleEntry.setResource(null);

		/*
		 * Final clean up - delete the Bundle.entry.request
		 */
		bundleEntry.setRequest(null);
	}

	private void setTempErrorResponseParams(BundleEntryComponent bundleEntry, String responseStatus, OperationOutcome.IssueSeverity severity, OperationOutcome.IssueType type, String details, String diagnostics, String location) {

		BundleEntryResponseComponent entryResponse = null;
		if (!bundleEntry.hasResponse()) {
			entryResponse = new BundleEntryResponseComponent();
			bundleEntry.setResponse(entryResponse);
		}
		else {
			entryResponse = bundleEntry.getResponse();
		}

		// Only set response status and outcome to allow for subsequent cycles to try operation again
		entryResponse.setStatus(responseStatus);
		OperationOutcome outcome = ServicesUtil.INSTANCE.getOperationOutcomeResource(severity, type, details, diagnostics, location);
		entryResponse.setOutcome(outcome);
	}

}
