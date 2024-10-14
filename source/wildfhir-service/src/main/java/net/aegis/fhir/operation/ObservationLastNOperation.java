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
package net.aegis.fhir.operation;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.hl7.fhir.r4.formats.XmlParser;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.IntegerType;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleEntrySearchComponent;
import org.hl7.fhir.r4.model.Bundle.BundleLinkComponent;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.Bundle.SearchEntryMode;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Parameters.ParametersParameterComponent;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Timing;

import net.aegis.fhir.model.Resource;
import net.aegis.fhir.model.ResourceContainer;
import net.aegis.fhir.model.Resourcemetadata;
import net.aegis.fhir.service.BatchService;
import net.aegis.fhir.service.CodeService;
import net.aegis.fhir.service.ConformanceService;
import net.aegis.fhir.service.ResourceService;
import net.aegis.fhir.service.ResourcemetadataService;
import net.aegis.fhir.service.TransactionService;
import net.aegis.fhir.service.util.ServicesUtil;
import net.aegis.fhir.service.util.SummaryUtil;
import net.aegis.fhir.service.util.UTCDateUtil;
import net.aegis.fhir.service.util.UUIDUtil;

/**
 * @author richard.ettema
 *
 */
public class ObservationLastNOperation extends ResourceOperationProxy {

	private Logger log = Logger.getLogger("ObservationLastNOperation");

	/* (non-Javadoc)
	 * @see net.aegis.fhir.operation.ResourceOperationProxy#executeOperation(javax.ws.rs.core.UriInfo, javax.ws.rs.core.HttpHeaders, net.aegis.fhir.service.ResourceService, net.aegis.fhir.service.ResourcemetadataService, net.aegis.fhir.service.BatchService, net.aegis.fhir.service.TransactionService, net.aegis.fhir.service.CodeService, net.aegis.fhir.service.ConformanceService, java.lang.String, java.lang.String, java.lang.String, org.hl7.fhir.r4.model.Parameters, org.hl7.fhir.r4.model.Resource, java.lang.String, java.lang.String, boolean, java.lang.StringBuffer)
	 */
	@Override
	public Parameters executeOperation(UriInfo context, HttpHeaders headers, ResourceService resourceService, ResourcemetadataService resourcemetadataService, BatchService batchService, TransactionService transactionService, CodeService codeService, ConformanceService conformanceService, String softwareVersion, String resourceType, String resourceId, Parameters inputParameters, org.hl7.fhir.r4.model.Resource inputResource, String inputString, String contentType, boolean isPost, StringBuffer returnedDirective) throws Exception {

		log.fine("[START] ObservationLastNOperation.executeOperation()");

		IntegerType maxValue = null;

		Parameters out = new Parameters();

		ParametersParameterComponent outParameter = new ParametersParameterComponent();
		outParameter.setName("return");
		out.addParameter(outParameter);
		//outParameter.setResource(lastnResource);

		try {
			/*
			 * If inputParameters is null, attempt to extract parameters from context
			 */
			if (inputParameters == null) {
				inputParameters = getParametersFromQueryParams(context);
			}

			// inputParameters is optional; if present, extract max value
			if (inputParameters != null && inputParameters.hasParameter()) {

				for (ParametersParameterComponent parameter : inputParameters.getParameter()) {

					if (parameter.getName() != null && parameter.getName().equals("max")) {

						if (parameter.getValue() instanceof IntegerType) {
							maxValue = (IntegerType) parameter.getValue();
						}
					}
				}
			}

			Bundle observationLastNResults = getObservationLastN(context, resourceService, resourcemetadataService, codeService, maxValue);

			if (observationLastNResults == null) {
				// results came back null; throw exception with error message
				throw new Exception("$lastn operation failed. The results contents were null.");
			}
			else {
				outParameter.setResource(observationLastNResults);
			}
		}
		catch (Exception e) {
			// Throw exceptions back
			throw e;
		}

		return out;
	}

	/**
	 *
	 * @param context
	 * @param resourceService
	 * @param maxValue
	 * @return Constructed Bundle response
	 * @throws Exception
	 */
	private Bundle getObservationLastN(UriInfo context, ResourceService resourceService, ResourcemetadataService resourcemetadataService, CodeService codeService, IntegerType maxValue) throws Exception {

		log.fine("[START] ObservationLastNOperation.getObservationLastN()");

		UTCDateUtil utcDateUtil = new UTCDateUtil();
		Integer countInteger = null;
		String countString = null;
		String summaryString = null;

		String locationPath = context.getRequestUri().toString();

		// Extract base url from locationPath for use in Bundle.entry.fullUrl element
		String baseUrl = ServicesUtil.INSTANCE.extractBaseURL(locationPath, "/$lastn");
		String includeBaseUrl = ServicesUtil.INSTANCE.extractBaseURL(locationPath, "Observation");

		MultivaluedMap<String, String> authPatientParams = null;

		// If max value null (not set), per FHIR spec $lastn operation default to one (1)
		if (maxValue == null) {
			maxValue = new IntegerType(1);
		}

		// Get the query parameters that represent the search criteria
		MultivaluedMap<String, String> queryParams = context.getQueryParameters();

		// Define lastn parameters with initial _sort by code:asc and date:desc
		MultivaluedMap<String, String> lastnParams = new MultivaluedHashMap<String, String>();
		lastnParams.add("_sort", "code");
		lastnParams.add("_sort", "-date"); // FHIR-261 Change sort to use effective[x]

		// Add queryParams if present
		if (queryParams != null && !queryParams.isEmpty()) {
			lastnParams.putAll(queryParams);
		}

		// Get the _count parameter if present
		countString = ServicesUtil.INSTANCE.getUriParameter("_count", queryParams);

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

		List<String> _matchedId = new ArrayList<String>();
		List<String[]> _include = new ArrayList<String[]>();
		List<String> _includedId = new ArrayList<String>();
		List<String[]> _includeRecurse = new ArrayList<String[]>();
		List<String[]> _revinclude = new ArrayList<String[]>();
		List<String> _revincludedId = new ArrayList<String>();
		List<String[]> validParams = new ArrayList<String[]>();
		List<String[]> invalidParams = new ArrayList<String[]>();

		// Execute search query with lastn parameters
		List<Resource> resources = resourceService.searchQuery(lastnParams, null, authPatientParams, "Observation", false, _include, _includeRecurse, _revinclude, validParams, invalidParams);

		log.info("ObservationLastNOperation - resources.size() = " + resources.size());

		String queryString = context.getRequestUri().getQuery();
		List<NameValuePair> orderedParams = URLEncodedUtils.parse(queryString, Charset.forName("UTF-8"));
		for(NameValuePair param : orderedParams) {
			log.fine("  param.name = '" + param.getName() + "'; param.value = '" + param.getValue() + "'");
		}

		// Extract base url from locationPath for use in Bundle.entry.fullUrl element
		String baseSelfUrl = ServicesUtil.INSTANCE.extractBaseURL(locationPath, "?");
		// FHIR-159 - Check for trailing forward slash and remove if present
		if (baseSelfUrl.endsWith("/")){
			baseSelfUrl = baseSelfUrl.substring(0, baseSelfUrl.length() - 1);
		}
		StringBuffer selfUrl = new StringBuffer(baseSelfUrl);
		selfUrl.append("?");
		int validCount = 0;
		if (orderedParams != null) {
			for (NameValuePair param : orderedParams) {
				log.fine("  param.name = '" + param.getName() + "'; param.value = '" + param.getValue() + "'");

				// Add orderedParam to selfUrl only if param.name in validParams
				for (String[] validParam : validParams) {
					if (validParam[0].equals(param.getName()) && validParam[1].contains(param.getValue())) {
						if (validCount > 0) {
							selfUrl.append("&");
						}
						selfUrl.append(param.getName()).append("=").append(URLEncoder.encode(validParam[1], StandardCharsets.UTF_8.toString()));
						validCount++;

						log.fine("      --> Adding " + validParam[0] + " = '" + validParam[1] + "'");
						break; // Only include first validParam match
					}
				}
			}
		}

		Date effectiveDefault = null;
		String effectiveDefaultException = null;
		if (codeService.isSupported("lastnProcessEmptyDate")) {
			// Assign default 'early' date value so Observations without an effective value get processed
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			// Use code setting 'lastnEmptyDateValue' if defined, else 1900-01-01
			String lastnEmptyDateValue = codeService.getCodeValue("lastnEmptyDateValue");
			try {
				effectiveDefault = sdf.parse(lastnEmptyDateValue);
			}
			catch (Exception e) {
				effectiveDefaultException = e.getMessage();
				effectiveDefault = sdf.parse("1900-01-01");
			}
		}

		// Process any invalidParams or undefined lastnEmptyDateValue into a Bundle.entry.resource OperationOutcome
		BundleEntryComponent bundleEntryOutcome = null;
		if (!invalidParams.isEmpty() || effectiveDefaultException != null) {
			bundleEntryOutcome = new BundleEntryComponent();
			OperationOutcome outcome = null;
			OperationOutcome.OperationOutcomeIssueComponent issue = null;
			List<OperationOutcome.OperationOutcomeIssueComponent> issues = new ArrayList<OperationOutcome.OperationOutcomeIssueComponent>();

			if (!invalidParams.isEmpty()) {
				for (String[] invalidParam : invalidParams) {
					issue = ServicesUtil.INSTANCE.getOperationOutcomeIssueComponent(OperationOutcome.IssueSeverity.WARNING, OperationOutcome.IssueType.INVALID, "Invalid search parameter '" + invalidParam[0] + "' included in search criteria." + (invalidParam.length > 1 && invalidParam[1] != null ? " " + invalidParam[1] : ""), null, invalidParam[0]);
					if (issue != null) {
						issues.add(issue);
					}
				}
			}
			if (effectiveDefaultException != null) {
				issue = ServicesUtil.INSTANCE.getOperationOutcomeIssueComponent(OperationOutcome.IssueSeverity.WARNING, OperationOutcome.IssueType.INVALID, "Process of empty Observation.effective is true but default effective date value is invalid.", null, effectiveDefaultException);
				if (issue != null) {
					issues.add(issue);
				}
			}

			outcome = ServicesUtil.INSTANCE.getOperationOutcomeResource(issues);

			bundleEntryOutcome.setResource(outcome);

			BundleEntrySearchComponent bundleEntryOutcomeSearch = new BundleEntrySearchComponent();
			bundleEntryOutcomeSearch.setMode(SearchEntryMode.OUTCOME);
			bundleEntryOutcome.setSearch(bundleEntryOutcomeSearch);
		}

		Bundle bundle = new Bundle();

		bundle.setId(UUIDUtil.getUUID());
		Meta bundleMeta = new Meta();
		bundleMeta.setVersionId("1");
		bundleMeta.setLastUpdated(new Date());
		bundle.setMeta(bundleMeta);
		bundle.setType(BundleType.SEARCHSET);
		bundle.setTotal(0);
		BundleLinkComponent selfLink = new BundleLinkComponent();
		selfLink.setRelation("self");
		selfLink.setUrl(selfUrl.toString());
		bundle.getLink().add(selfLink);

		if (resources != null && resources.size() > 0) {

			// First, populate the _matchedId list for use in _include and _revInclude duplicate check
			for (net.aegis.fhir.model.Resource resourceEntry : resources) {
				_matchedId.add(resourceEntry.getResourceType() + "/" + resourceEntry.getResourceId());
			}

			/*
			 * Evaluate found Observation resources for inclusion
			 */
			String codeValue = "";
			String previousCode = "";
			Date effectiveValue = null;
			Period effectivePeriod = null;
			Date previousEffectiveValue = null;
			int codeCount = 0;
			int resourceCount = 0;
			int maxPlusCount = 0; // Used when effectiveValue equal to previousEffectiveValue

			XmlParser xmlP = new XmlParser();

			ByteArrayInputStream iResource = null;

			String fullUrl = "";

			BundleEntryComponent bundleEntry = null;

			boolean addToBundle = false;

			for (Resource resourceEntry : resources) {
				log.fine("$lastn - processing " + resourceEntry.getResourceType() + " resource [" + resourceEntry.getResourceId() + "]");

				addToBundle = false;

				// Convert XML contents to Resource object
				iResource = new ByteArrayInputStream(resourceEntry.getResourceContents());
				org.hl7.fhir.r4.model.Resource resourceObject = xmlP.parse(iResource);

				// Only add Observation resource types
				if (resourceObject.getResourceType().equals(org.hl7.fhir.r4.model.ResourceType.Observation)) {
					// Get current code value for comparison
					codeValue = ((Observation)resourceObject).getCode().getCodingFirstRep().getCode();

					effectiveValue = null;
					if (((Observation)resourceObject).hasEffectiveDateTimeType()) {
						effectiveValue = ((Observation)resourceObject).getEffectiveDateTimeType().getValue();
					}
					else if (((Observation)resourceObject).hasEffectiveInstantType()) {
						effectiveValue = ((Observation)resourceObject).getEffectiveInstantType().getValue();
					}
					else if (((Observation)resourceObject).hasEffectivePeriod()) {
						effectivePeriod = ((Observation)resourceObject).getEffectivePeriod();
						if (effectivePeriod.hasEnd()) {
							effectiveValue = effectivePeriod.getEnd();
						}
						else if (effectivePeriod.hasStart()) {
							effectiveValue = effectivePeriod.getStart();
						}
					}
					else if (((Observation)resourceObject).hasEffectiveTiming()) {
						// Special logic for Timing data type - set effectiveValue to the latest event value
						Timing effectiveTiming = ((Observation)resourceObject).getEffectiveTiming();

						Date current = null;
						if (effectiveTiming.hasEvent()) {
							effectiveValue = effectiveTiming.getEvent().get(0).getValue();
							for (DateTimeType event : effectiveTiming.getEvent()) {
								current = event.getValue();
								if (current.after(effectiveValue)) {
									effectiveValue = current;
								}
							}
						}
					}

					if (effectiveValue == null && codeService.isSupported("lastnProcessEmptyDate")) {
						// Assign default 'early' date value so Observations without an effective value get processed
						effectiveValue = effectiveDefault;
					}

					// Skip Observations without a code value and without an effective[x] value
					if (codeValue != null && effectiveValue != null) {
						log.fine("  --> Observation code[0] = '" + codeValue + "' effectiveValue = '" + utcDateUtil.formatDate(effectiveValue, UTCDateUtil.DATE_PARAMETER_FORMAT, null) + "'");

						// Increment code count
						codeCount++;

						if (previousCode.equals("")) {
							log.fine("	--> previous values are empty, first one gets added");
							// This is the first one, add to lastn bundle
							addToBundle = true;
						}
						else if (codeValue.equals(previousCode)) {
							log.fine("	--> codeValue = '" + codeValue + "'; previousCode = '" + codeValue + "'; match, check max value");
							log.fine("	  --> maxValue = " + maxValue.getValue() + "; codeCount = " + codeCount);

							// Special check for effectiveValue equal to previousEffectiveValue
							if (previousEffectiveValue != null && effectiveValue.equals(previousEffectiveValue)) {
								// Increment maxPlusCount to allow for additional duplicate Observation code and effective
								maxPlusCount++;
								log.fine("	  --> maxPlusCount = " + maxPlusCount + "; previousEffectiveValue = '" + utcDateUtil.formatDate(previousEffectiveValue, UTCDateUtil.DATE_PARAMETER_FORMAT, null) + "'");
							}

							// This is another Observation with the same code value, check number against max value
							if (maxValue.getValue() < 0 || codeCount <= (maxValue.getValue() + maxPlusCount)) {
								log.fine("	  --> maxValue check true - add");
								// If less than or equal to max value, add to lastn bundle
								addToBundle = true;
							}
						}
						else if (!codeValue.equals(previousCode)) {
							log.fine("	--> codeValue = '" + codeValue + "'; previousCode = '" + codeValue + "'; new code, add and reset codeCount");
							// This Observation is the first one for a new code value, add to lastn bundle and reset code count = 1
							addToBundle = true;
							codeCount = 1;
							maxPlusCount = 0;
						}

						if (addToBundle) {
							resourceCount++;

							/*
							 *  Check for count=0 or _summary=count parameter setting; if set, then only return total
							 *  without any page links and without any entries
							 */
							if ((countInteger != null && countInteger.intValue() == 0) || (summaryString != null && summaryString.equals("count"))) {
								log.fine("	--> _summary=count; increment Observation count");
							}
							else {
								log.fine("	--> adding Observation");

								bundleEntry = new BundleEntryComponent();

								bundleEntry.setResource(resourceObject);

								// Build and set Bundle.entry.fullUrl
								fullUrl = baseUrl + "/" + resourceEntry.getResourceId();
								bundleEntry.setFullUrl(fullUrl);

								BundleEntrySearchComponent bundleEntrySearch = new BundleEntrySearchComponent();
								bundleEntrySearch.setMode(SearchEntryMode.MATCH);
								bundleEntrySearch.setScore(new BigDecimal(1));
								bundleEntry.setSearch(bundleEntrySearch);

								bundle.getEntry().add(bundleEntry);

								// Process _include
								if (_include != null && _include.size() > 0) {
									log.fine("Processing _include...");

									String source = null;
									String parameter = null;
									String type = null;

									for (String[] include : _include) {
										// Extract include parameter parts
										source = include[0];
										parameter = include[1];
										if (include.length > 2) {
											type = include[2];
										}
										else {
											type = null;
										}

										log.fine("--> _include is '" + source + ":" + parameter + ":" + (type != null ? type : "null") + "'");

										// Proceed only if current resource type matches include source
										if (resourceEntry.getResourceType().equals(source)) {
											log.fine("-->--> _include resource type match (" + source + "); _include parameter is reference (" + parameter + ")");

											boolean isParamRef = false;
											String resolvedParameter = null;
											int resolvedParamEnd = -1;

											List<Resourcemetadata> paramMetaData = null;

											// Check for wild card parameter
											if (parameter.equals("*")) {
												paramMetaData = resourcemetadataService.findMetadataByResourceIdTypeLevel1Param(resourceEntry.getResourceId(), source);
											}
											else {
												// Query the resourcemetadata for the current resource parameter
												paramMetaData = resourcemetadataService.findMetadataByResourceIdTypeParam(resourceEntry.getResourceId(), source, parameter);
											}

											if (paramMetaData != null && paramMetaData.size() > 0) {
												log.fine("-->-->-->--> _include parameter meta data found");

												for (Resourcemetadata metadata : paramMetaData) {
													if (parameter.equals("*")) {
														resolvedParamEnd = metadata.getParamName().indexOf("[");
														if (resolvedParamEnd < 0) {
															resolvedParamEnd = metadata.getParamName().length();
														}
														resolvedParameter = metadata.getParamName().substring(0, resolvedParamEnd);
													}
													else {
														resolvedParameter = parameter;
													}
													isParamRef = (net.aegis.fhir.model.ResourceType.findResourceTypeResourceCriteriaType(source, resolvedParameter).equalsIgnoreCase("REFERENCE") ? true : false);

													if (isParamRef == true && !metadata.getParamValue().isEmpty()) {
														log.fine("-->-->-->--> _include parameter (" + resolvedParameter + ") meta data reference found (" + metadata.getParamValue() + ")");

														// Extract resource type and id
														String[] refParts = metadata.getParamValue().split("/");

														int refPartsLength = refParts.length;
														if (refPartsLength > 1) {
															String refResourceType = refParts[refPartsLength - 2];
															String refResourceId = refParts[refPartsLength - 1];

															// Check already _includedId and _matchedId lists for this included resource; if found, skip
															String refResourceCheckId = refResourceType + "/" + refResourceId;
															if (!_includedId.contains(refResourceCheckId) && !_matchedId.contains(refResourceCheckId)) {
																log.fine("-->-->-->-->--> _include resource (" + refResourceCheckId + ")");

																// If type defined, check for match
																if (type == null || refResourceType.equals(type)) {
																	if (type != null) {
																		log.fine("-->-->-->-->--> _include type match (" + type + ")");
																	}
																	ResourceContainer refResource = resourceService.read(refResourceType, refResourceId, summaryString);

																	if (refResource.getResponseStatus().equals(Response.Status.OK)) {
																		log.fine("-->-->-->-->--> _include resource read OK (" + refResourceId + ")");

																		// Create and add bundle entry for included resource
																		bundleEntry = new BundleEntryComponent();

																		// Set Bundle.entry.fullUrl
																		fullUrl = includeBaseUrl + refResourceType + "/" + refResourceId;
																		bundleEntry.setFullUrl(fullUrl);

																		// Convert XML contents to Resource object
																		iResource = new ByteArrayInputStream(refResource.getResource().getResourceContents());

																		resourceObject = xmlP.parse(iResource);

																		bundleEntry.setResource(resourceObject);

																		bundleEntrySearch = new BundleEntrySearchComponent();
																		bundleEntrySearch.setMode(SearchEntryMode.INCLUDE);
																		bundleEntry.setSearch(bundleEntrySearch);

																		bundle.getEntry().add(bundleEntry);

																		// Add to _includedId
																		_includedId.add(refResourceCheckId);
																	}
																	else {
																		log.fine("-->-->-->-->--> _include resource read NOT OK (" + refResourceId + ") --> " + refResource.getResponseStatus().name());
																	}
																}
																else {
																	log.fine("-->-->-->-->--> _type mismatch! refResourceType = '" + refResourceType + "', type = '" + (type != null ? type : "null") + "'");
																}
															}
															else {
																log.fine("-->-->-->-->--> _include resource (" + refResourceCheckId + ") - already included!");
															}
														}
													}
												}
											}
										}
									}
								}

								// Process _include:recurse
								if (_includeRecurse != null && _includeRecurse.size() > 0) {
									log.fine("Processing _include:recurse...");

									String source = null;
									String parameter = null;
									String type = null;

									for (String[] includeRecurse : _includeRecurse) {
										// Extract include parameter parts
										source = includeRecurse[0];
										parameter = includeRecurse[1];
										if (includeRecurse.length > 2) {
											type = includeRecurse[2];
										}
										else {
											type = null;
										}

										log.fine("--> _include:recurse is '" + source + ":" + parameter + ":" + (type != null ? type : "null") + "'");

										// Proceed only if current resource type matches include source
										if (resourceEntry.getResourceType().equals(source)) {
											log.fine("-->--> _include:recurse resource type match (" + source + "); _include:recurse parameter is reference (" + parameter + ")");

											boolean isParamRef = false;
											String resolvedParameter = null;
											int resolvedParamEnd = -1;

											List<Resourcemetadata> paramMetaData = null;

											// Check for wild card parameter
											if (parameter.equals("*")) {
												paramMetaData = resourcemetadataService.findMetadataByResourceIdTypeLevel1Param(resourceEntry.getResourceId(), source);
											}
											else {
												// Query the resourcemetadata for the current resource parameter
												paramMetaData = resourcemetadataService.findMetadataByResourceIdTypeParam(resourceEntry.getResourceId(), source, parameter);
											}

											if (paramMetaData != null && paramMetaData.size() > 0) {
												log.fine("-->-->-->--> _include:recurse parameter meta data found");

												for (Resourcemetadata metadata : paramMetaData) {
													if (parameter.equals("*")) {
														resolvedParamEnd = metadata.getParamName().indexOf("[");
														if (resolvedParamEnd < 0) {
															resolvedParamEnd = metadata.getParamName().length();
														}
														resolvedParameter = metadata.getParamName().substring(0, resolvedParamEnd);
													}
													else {
														resolvedParameter = parameter;
													}
													isParamRef = (net.aegis.fhir.model.ResourceType.findResourceTypeResourceCriteriaType(source, resolvedParameter).equalsIgnoreCase("REFERENCE") ? true : false);

													if (isParamRef == true && !metadata.getParamValue().isEmpty()) {
														log.fine("-->-->-->--> _include:recurse parameter (" + resolvedParameter + ") meta data reference found (" + metadata.getParamValue() + ")");

														// Extract resource type and id
														String[] refParts = metadata.getParamValue().split("/");

														int refPartsLength = refParts.length;
														if (refPartsLength > 1) {
															String refResourceType = refParts[refPartsLength - 2];
															String refResourceId = refParts[refPartsLength - 1];

															// Check already _includedId and _matchedId lists for this included resource; if found, skip
															String refResourceCheckId = refResourceType + "/" + refResourceId;
															if (!_includedId.contains(refResourceCheckId) && !_matchedId.contains(refResourceCheckId)) {
																log.fine("-->-->-->-->--> _include:recurse resource (" + refResourceCheckId + ")");

																// If type defined, check for match
																if (type == null || refResourceType.equals(type)) {
																	if (type != null) {
																		log.fine("-->-->-->-->--> _include:recurse type match (" + type + ")");
																	}
																	ResourceContainer refResource = resourceService.read(refResourceType, refResourceId, summaryString);

																	if (refResource.getResponseStatus().equals(Response.Status.OK)) {
																		log.fine("-->-->-->-->--> _include:recurse resource read OK (" + refResourceId + ")");

																		// Create and add bundle entry for included resource
																		bundleEntry = new BundleEntryComponent();

																		// Set Bundle.entry.fullUrl
																		fullUrl = includeBaseUrl + refResourceType + "/" + refResourceId;
																		bundleEntry.setFullUrl(fullUrl);

																		// Convert XML contents to Resource object
																		iResource = new ByteArrayInputStream(refResource.getResource().getResourceContents());

																		resourceObject = xmlP.parse(iResource);

																		bundleEntry.setResource(resourceObject);

																		bundleEntrySearch = new BundleEntrySearchComponent();
																		bundleEntrySearch.setMode(SearchEntryMode.INCLUDE);
																		bundleEntry.setSearch(bundleEntrySearch);

																		bundle.getEntry().add(bundleEntry);

																		// Add to _includedId
																		_includedId.add(refResourceCheckId);

																		// Call includeRecurse for this resource instance
																		includeRecurse(resourceService, resourcemetadataService, bundle, _includedId, refResourceId, source, parameter, type, summaryString, baseUrl, xmlP);
																	}
																	else {
																		log.fine("-->-->-->-->--> _include:recurse resource read NOT OK (" + refResourceId + ") --> " + refResource.getResponseStatus().name());
																	}
																}
																else {
																	log.fine("-->-->-->-->--> _include:recurse _type mismatch! refResourceType = '" + refResourceType + "', type = '" + (type != null ? type : "null") + "'");
																}
															}
															else {
																log.fine("-->-->-->-->--> _include:recurse resource (" + refResourceCheckId + ") - already included!");
															}
														}
													}
												}
											}
										}
									}
								}

								// Process _revinclude
								if (_revinclude != null && _revinclude.size() > 0) {
									log.fine("Processing _revinclude...");

									String source = null;
									String parameter = null;
									String type = null;

									for (String[] revinclude : _revinclude) {
										log.fine("--> _revinclude is '" + revinclude[0] + ":" + revinclude[1] + "'");

										// Extract revinclude parameter parts
										source = revinclude[0];
										parameter = revinclude[1];
										if (revinclude.length > 2) {
											type = revinclude[2];
										}
										else {
											type = null;
										}

										// Proceed based on revinclude source and current resource type
										log.fine("-->--> _revinclude resource type (" + source + "); current resource type (" + resourceEntry.getResourceType() + ")");

										boolean isParamRef = (net.aegis.fhir.model.ResourceType.findResourceTypeResourceCriteriaType(source, parameter).equalsIgnoreCase("REFERENCE") ? true : false);

										if (isParamRef) {
											log.fine("-->-->--> _revinclude parameter is reference (" + parameter + "); current resource id entry (" + resourceEntry.getResourceId() + ")");

											// If type defined, check for current resource type match
											if (type == null || resourceEntry.getResourceType().equals(type)) {
												if (type != null) {
													log.fine("-->-->-->-->--> _revinclude type match (" + type + ")");
												}

												// Build reverse search parameter
												String revSearchParameter = parameter + "=" + resourceEntry.getResourceType() + "/" + resourceEntry.getResourceId();

												// Convert search parameter string into queryParams map
												List<NameValuePair> params = URLEncodedUtils.parse(revSearchParameter, Charset.defaultCharset());
												MultivaluedMap<String, String> revQueryParams = ServicesUtil.INSTANCE.listNameValuePairToMultivaluedMapString(params);

												// Search for resources with reverse search
												List<Resource> revSearch = resourceService.searchQuery(revQueryParams, null, null, source, false, null, null, null, null, null);

												if (revSearch != null && revSearch.size() > 0) {
													log.fine("-->-->-->--> _revinclude reverse search found matches (" + revSearch.size() + ")");

													for (Resource revResource : revSearch) {
														String revResourceCheckId = revResource.getResourceType() + "/" + revResource.getResourceId();
														log.fine("-->-->-->-->--> _revinclude resource (" + revResourceCheckId + ")");

														// Check already _revincludedId and _matchedId lists for this revincluded resource; if found, skip
														if (!_revincludedId.contains(revResourceCheckId) && !_matchedId.contains(revResourceCheckId)) {

															// Create and add bundle entry for included resource
															bundleEntry = new BundleEntryComponent();

															// Set Bundle.entry.fullUrl
															bundleEntry.setFullUrl(includeBaseUrl + revResourceCheckId);

															// Check for _summary
															if (!StringUtils.isEmpty(summaryString)) {
																// Summary requested, modify copy of found resource
																Resource foundRevResource = revResource.copy();

																SummaryUtil.INSTANCE.generateResourceSummary(foundRevResource, summaryString);

																// Convert XML contents of copy to Resource object
																iResource = new ByteArrayInputStream(foundRevResource.getResourceContents());
															}
															else {
																// Convert XML contents to Resource object
																iResource = new ByteArrayInputStream(revResource.getResourceContents());
															}

															resourceObject = xmlP.parse(iResource);

															bundleEntry.setResource(resourceObject);

															bundleEntrySearch = new BundleEntrySearchComponent();
															bundleEntrySearch.setMode(SearchEntryMode.INCLUDE);
															bundleEntry.setSearch(bundleEntrySearch);

															bundle.getEntry().add(bundleEntry);

															// Add to _revincludedId
															_revincludedId.add(revResourceCheckId);
														}
														else {
															log.fine("-->-->-->-->--> _revinclude resource (" + revResourceCheckId + ") - already included!");
														}
													}
												}
											}
										}
									}
								}
							}
						}

						// Set previous values for next Observation comparison
						previousCode = codeValue;
						previousEffectiveValue = effectiveValue;
					}
					else {
						log.fine("  --> SKIP - Observation.code[0] is null or Observation.effective is empty");
					}
				}
			}

			if (bundleEntryOutcome != null) {
				bundle.getEntry().add(bundleEntryOutcome);
			}

			bundle.setTotal(resourceCount);
		}

		return bundle;
	}

	private void includeRecurse(ResourceService resourceService, ResourcemetadataService resourcemetadataService, Bundle bundle, List<String> _includedId, String resourceId, String source, String parameter, String type, String summary_, String baseUrl, XmlParser xmlP) throws Exception {

		log.fine("[START] ObservationLastNOperation.includeRecurse(bundle, _includedId, '" + resourceId + "', '" + source + "', '" + parameter + "', '" + (type == null ? "null" : type) + "', '");

		boolean isParamRef = false;
		String resolvedParameter = null;
		int resolvedParamEnd = -1;
		BundleEntryComponent bundleEntry = null;
		String fullUrl = "";
		ByteArrayInputStream iResource = null;
		org.hl7.fhir.r4.model.Resource resourceObject = null;
		BundleEntrySearchComponent bundleEntrySearch = null;

		List<Resourcemetadata> paramMetaData = null;

		// Check for wild card parameter
		if (parameter.equals("*")) {
			paramMetaData = resourcemetadataService.findMetadataByResourceIdTypeLevel1Param(resourceId, source);
		}
		else {
			// Query the resourcemetadata for the current resource parameter
			paramMetaData = resourcemetadataService.findMetadataByResourceIdTypeParam(resourceId, source, parameter);
		}

		if (paramMetaData != null && paramMetaData.size() > 0) {
			log.fine("-->-->-->--> includeRecurse parameter meta data found");

			for (Resourcemetadata metadata : paramMetaData) {
				if (parameter.equals("*")) {
					resolvedParamEnd = metadata.getParamName().indexOf("[");
					if (resolvedParamEnd < 0) {
						resolvedParamEnd = metadata.getParamName().length();
					}
					resolvedParameter = metadata.getParamName().substring(0, resolvedParamEnd);
				}
				else {
					resolvedParameter = parameter;
				}
				isParamRef = (net.aegis.fhir.model.ResourceType.findResourceTypeResourceCriteriaType(source, resolvedParameter).equalsIgnoreCase("REFERENCE") ? true : false);

				if (isParamRef == true && !metadata.getParamValue().isEmpty()) {
					log.fine("-->-->-->--> includeRecurse parameter (" + resolvedParameter + ") meta data reference found (" + metadata.getParamValue() + ")");

					// Extract resource type and id
					String[] refParts = metadata.getParamValue().split("/");

					int refPartsLength = refParts.length;
					if (refPartsLength > 1) {
						String refResourceType = refParts[refPartsLength - 2];
						String refResourceId = refParts[refPartsLength - 1];

						// Check already _includedId list for this included resource; if found, skip
						String refResourceCheckId = refResourceType + "/" + refResourceId;
						if (!_includedId.contains(refResourceCheckId)) {
							log.fine("-->-->-->-->--> includeRecurse resource (" + refResourceCheckId + ")");

							// If type defined, check for match
							if (type == null || refResourceType.equals(type)) {
								if (type != null) {
									log.fine("-->-->-->-->--> includeRecurse type match (" + type + ")");
								}
								ResourceContainer refResource = resourceService.read(refResourceType, refResourceId, summary_);

								if (refResource.getResponseStatus().equals(Response.Status.OK)) {
									log.fine("-->-->-->-->--> includeRecurse resource read OK (" + refResourceId + ")");

									// Create and add bundle entry for included resource
									bundleEntry = new BundleEntryComponent();

									// Set Bundle.entry.fullUrl
									fullUrl = baseUrl + refResourceType + "/" + refResourceId;
									bundleEntry.setFullUrl(fullUrl);

									// Convert XML contents to Resource object
									iResource = new ByteArrayInputStream(refResource.getResource().getResourceContents());

									resourceObject = xmlP.parse(iResource);

									bundleEntry.setResource(resourceObject);

									bundleEntrySearch = new BundleEntrySearchComponent();
									bundleEntrySearch.setMode(SearchEntryMode.INCLUDE);
									bundleEntry.setSearch(bundleEntrySearch);

									bundle.getEntry().add(bundleEntry);

									// Add to _includedId
									_includedId.add(refResourceCheckId);

									// Call includeRecurse for this resource instance
									includeRecurse(resourceService, resourcemetadataService, bundle, _includedId, refResourceId, source, parameter, type, summary_, baseUrl, xmlP);
								}
								else {
									log.fine("-->-->-->-->--> includeRecurse resource read NOT OK (" + refResourceId + ") --> " + refResource.getResponseStatus().name());
								}
							}
							else {
								log.fine("-->-->-->-->--> includeRecurse _type mismatch! refResourceType = '" + refResourceType + "', type = '" + (type != null ? type : "null") + "'");
							}
						}
						else {
							log.fine("-->-->-->-->--> includeRecurse resource (" + refResourceCheckId + ") - already included!");
						}
					}
				}
			}
		}

	}

	/**
	 *
	 * @param context
	 * @return <code>Parameters</code>
	 * @throws Exception
	 */
	private Parameters getParametersFromQueryParams(UriInfo context) throws Exception {

		log.fine("[START] ResourceOperationsRESTService.getParametersFromQueryParams()");

		// Default empty Parameters
		Parameters queryParameters = new Parameters();

		try {
			if (context != null) {
				log.info("Checking for search parameters...");

				/*
				 * Extract the individual expected parameters
				 */
				IntegerType max = null;

				// Get the query parameters that represent the search criteria
				MultivaluedMap<String, String> queryParams = context.getQueryParameters();

				if (queryParams != null && queryParams.size() > 0) {
					Set<Entry<String, List<String>>> paramSet = queryParams.entrySet();

					for (Entry<String, List<String>> entry : paramSet) {

						String key = entry.getKey();

						if (key.equals("max")) {
							Integer value = Integer.valueOf(entry.getValue().get(0));

							ParametersParameterComponent parameter = new ParametersParameterComponent();
							parameter.setName(key);
							max = new IntegerType();
							max.setValue(value);
							parameter.setValue(max);
							queryParameters.addParameter(parameter);
						}
					}
				}
			}
		}
		catch (Exception e) {
			// Handle generic exceptions
			log.severe(e.getMessage());
			throw e;
		}

		return queryParameters;
	}

}
