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
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import net.aegis.fhir.model.LabelKeyValueBean;
import net.aegis.fhir.model.Resource;
import net.aegis.fhir.model.ResourceContainer;
import net.aegis.fhir.model.ResourceType;
import net.aegis.fhir.service.BatchService;
import net.aegis.fhir.service.CodeService;
import net.aegis.fhir.service.ConformanceService;
import net.aegis.fhir.service.ResourceService;
import net.aegis.fhir.service.ResourcemetadataService;
import net.aegis.fhir.service.TransactionService;
import net.aegis.fhir.service.linked.LinkedResourceProxy;
import net.aegis.fhir.service.linked.LinkedResourceProxyObjectFactory;
import net.aegis.fhir.service.narrative.FHIRNarrativeGeneratorClient;
import net.aegis.fhir.service.util.ServicesUtil;
import net.aegis.fhir.service.util.UUIDUtil;

import org.hl7.fhir.r4.formats.XmlParser;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.DateType;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleEntrySearchComponent;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.Bundle.SearchEntryMode;
import org.hl7.fhir.r4.model.Parameters.ParametersParameterComponent;
import org.hl7.fhir.r4.model.Reference;

/**
 * @author richard.ettema
 *
 */
public class PatientEverything extends ResourceOperationProxy {

	private Logger log = Logger.getLogger("PatientEverything");

	/* (non-Javadoc)
	 * @see net.aegis.fhir.operation.ResourceOperationProxy#executeOperation(javax.ws.rs.core.UriInfo, javax.ws.rs.core.HttpHeaders, net.aegis.fhir.service.ResourceService, net.aegis.fhir.service.ResourcemetadataService, net.aegis.fhir.service.BatchService, net.aegis.fhir.service.TransactionService, net.aegis.fhir.service.CodeService, net.aegis.fhir.service.ConformanceService, java.lang.String, java.lang.String, java.lang.String, org.hl7.fhir.r4.model.Parameters, org.hl7.fhir.r4.model.Resource, java.lang.String, java.lang.String, boolean, java.lang.StringBuffer)
	 */
	@Override
	public Parameters executeOperation(UriInfo context, HttpHeaders headers, ResourceService resourceService, ResourcemetadataService resourcemetadataService, BatchService batchService, TransactionService transactionService, CodeService codeService, ConformanceService conformanceService, String softwareVersion, String resourceType, String resourceId, Parameters inputParameters, org.hl7.fhir.r4.model.Resource inputResource, String inputString, String contentType, boolean isPost, StringBuffer returnedDirective) throws Exception {

		log.fine("[START] PatientEverything.executeOperation()");

		Parameters out = null;
		Bundle everythingSearchSet = null;
		OperationOutcome rOutcome = null;
		Patient patient = null;

		try {
			/*
			 * If inputParameters is null, attempt to extract parameters from context
			 */
			if (inputParameters == null) {
				inputParameters = getParametersFromQueryParams(context);
			}

			/*
			 * Extract the individual expected parameters
			 */
			DateType startDate = null;
			DateType endDate = null;

			if (inputParameters != null && inputParameters.hasParameter()) {

				for (ParametersParameterComponent parameter : inputParameters.getParameter()) {

					if (parameter.getName() != null && parameter.getName().equals("start")) {

						startDate = (DateType) parameter.getValue();
					}

					if (parameter.getName() != null && parameter.getName().equals("end")) {

						endDate = (DateType) parameter.getValue();
					}
				}
			}

			/*
			 * patient everything check
			 * - $everything must be executed against an existing resource instance; i.e. the url path must contain the id [base]/[resource]/[id]/$everything or,
			 *   an authorized patient has been sent.
			 * - insure resource content is not null or empty
			 * - insure resource instance for the url [id] value exists and is not deleted
			 */
			ResourceContainer resourceContainer = null;
			if (resourceId != null) {
				resourceContainer = resourceService.read(resourceType, resourceId, null);

				if (resourceContainer == null || resourceContainer.getResource() == null || !resourceContainer.getResponseStatus().equals(Response.Status.OK)) {
					rOutcome = new OperationOutcome();
					OperationOutcome.OperationOutcomeIssueComponent issue =
							ServicesUtil.INSTANCE.getOperationOutcomeIssueComponent(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.CONFLICT,
									"Patient $everything failed. The referenced patient does not resolve to a valid resource instance.", null, null);

					if (issue != null) {
						rOutcome.setText(null);
						rOutcome.getIssue().add(issue);

						// Use RI NarrativeGenerator
						FHIRNarrativeGeneratorClient.instance().generate(rOutcome);
					}
				}
				else {
					// Process everything on the found Patient

					// Convert XML contents to Resource object and set id and meta
					ByteArrayInputStream iResource = new ByteArrayInputStream(resourceContainer.getResource().getResourceContents());
					XmlParser xmlP = new XmlParser();
					patient = (Patient) xmlP.parse(iResource);

					/*
					 * Perform a search operation against all Patient Compartment resource types and combine all results into
					 * a single searchset Bundle.
					 */
					everythingSearchSet = getPatientEverything(context, resourceService, patient, startDate, endDate);
				}
			}
			else {
				rOutcome = new OperationOutcome();
				OperationOutcome.OperationOutcomeIssueComponent issue =
						ServicesUtil.INSTANCE.getOperationOutcomeIssueComponent(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.INCOMPLETE,
								"Patient $everything failed. No referenced patient id value found. The $everything interaction is performed by an HTTP GET or POST command with a path of [base]/Patient/[id]/$everything where [id] references a known, valid Patient resource.", null, null);

				if (issue != null) {
					rOutcome.setText(null);
					rOutcome.getIssue().add(issue);

					// Use RI NarrativeGenerator
					FHIRNarrativeGeneratorClient.instance().generate(rOutcome);
				}
			}

			out = new Parameters();

			ParametersParameterComponent parameter = new ParametersParameterComponent();
			parameter.setName("return");

			if (everythingSearchSet != null) {
				parameter.setResource(everythingSearchSet);
			}
			else {
				parameter.setResource(rOutcome);
			}

			out.addParameter(parameter);

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
	 * @param patient
	 * @param startDate
	 * @param endDate
	 * @return Constructed Bundle response
	 * @throws Exception
	 */
	private Bundle getPatientEverything(UriInfo context, ResourceService resourceService, Patient patient, DateType startDate, DateType endDate) throws Exception {

		log.fine("[START] PatientEverything.getPatientEverything()");

		String locationPath = context.getRequestUri().toString();

		// Extract base url from locationPath for use in Bundle.entry.fullUrl element
		String baseUrl = ServicesUtil.INSTANCE.extractBaseURL(locationPath, "/Patient");

		String everythingKey = "";

		Map<String, org.hl7.fhir.r4.model.Resource> everythingResources = new LinkedHashMap<String, org.hl7.fhir.r4.model.Resource>();

		LinkedResourceProxyObjectFactory linkedProxyFactory = new LinkedResourceProxyObjectFactory();

		/*
		 * Add the Patient for minimum response
		 */
		log.info("Adding Patient");

		everythingKey = patient.getId();
		everythingResources.put(everythingKey, patient);

		/*
		 * Add Patient linked resources: Organization, Practitioner, etc.
		 */
		ByteArrayInputStream iResource = null;
		XmlParser xmlP = new XmlParser();
		String resourceType = "";
		ResourceContainer resourceContainer = null;

		// GeneralPractitioner
		if (patient.hasGeneralPractitioner()) {

			log.info("Processing Patient General Practitioners");

			for (Reference generalPractitionerReference : patient.getGeneralPractitioner()) {
				String generalPractitionerRef = generalPractitionerReference.getReference();
				String generalPractitionerResourceId = ServicesUtil.INSTANCE.extractResourceIdFromURL(generalPractitionerRef);
				if (generalPractitionerRef.contains("Organization")) {
					resourceType = "Organization";
				}
				else if (generalPractitionerRef.contains("Practitioner")) {
					resourceType = "Practitioner";
				}

				if (!resourceType.isEmpty()) {

					log.info("Processing resource type " + resourceType);
					log.info("Processing resource id " + generalPractitionerResourceId);

					resourceContainer = resourceService.read(resourceType, generalPractitionerResourceId, null);

					log.info("Resource read status " + resourceContainer.getResponseStatus().name());

					if (resourceContainer.getResponseStatus().equals(Response.Status.OK)) {

						log.info("Adding resource type " + resourceType + " to everything resources");

						// Convert XML contents to Resource object
						iResource = new ByteArrayInputStream(resourceContainer.getResource().getResourceContents());
						org.hl7.fhir.r4.model.Resource resourceObject = xmlP.parse(iResource);

						everythingKey = resourceObject.getId();
						everythingResources.put(everythingKey, resourceObject);
					}
				}
			}
		}

		// ManagingOrganization
		resourceType = "";
		if (patient.hasManagingOrganization()) {

			log.info("Processing Patient Managing Organization");

			String managingOrgRef = patient.getManagingOrganization().getReference();
			String managingOrgResourceId = ServicesUtil.INSTANCE.extractResourceIdFromURL(managingOrgRef);
			if (managingOrgRef.contains("Organization")) {
				resourceType = "Organization";
			}

			if (!resourceType.isEmpty()) {

				log.info("Processing resource type " + resourceType);
				log.info("Processing resource id " + managingOrgResourceId);

				resourceContainer = resourceService.read(resourceType, managingOrgResourceId, null);

				log.info("Resource read status " + resourceContainer.getResponseStatus().name());

				if (resourceContainer.getResponseStatus().equals(Response.Status.OK)) {

					log.info("Adding resource type " + resourceType + " to everything resources");

					// Convert XML contents to Resource object
					iResource = new ByteArrayInputStream(resourceContainer.getResource().getResourceContents());
					org.hl7.fhir.r4.model.Resource resourceObject = xmlP.parse(iResource);

					everythingKey = resourceObject.getId();
					everythingResources.put(everythingKey, resourceObject);
				}
			}
		}

		/*
		 * Parse startDate and endDate criteria if defined
		 */
		String startDateCriteria = null;
		if (startDate != null) {
			log.info("startDate = " + startDate.getValueAsString());
			startDateCriteria = "ge" + startDate.getValueAsString();
			log.info("startDateCriteria = " + startDateCriteria);
		}
		String endDateCriteria = null;
		if (endDate != null) {
			log.info("endDate = " + endDate.getValueAsString());
			endDateCriteria = "le" + endDate.getValueAsString();
			log.info("endDateCriteria = " + endDateCriteria);
		}

		/*
		 * Perform a search operation against all Patient Compartment resource types and combine all results into
		 * a single searchset Bundle.
		 * Exclusions: AuditEvent, Provenance
		 */
		String patientCriteria = "Patient/" + patient.getId();
		MultivaluedMap<String, String> queryParams = null;
		List<Resource> resources = null;

		List<LabelKeyValueBean> compartmentResourceTypeCriteriaList = ResourceType.getCompartmentResourceTypeCriteria(patient.getResourceType().name());

		for (LabelKeyValueBean lkvb : compartmentResourceTypeCriteriaList) {

			// Exclude: AuditEvent, Provenance
			if (!lkvb.getKey().equals("AuditEvent") && !lkvb.getKey().equals("Provenance")) {

				log.info("Processing resource type " + lkvb.getKey());

				// Set patient criteria
				queryParams = new MultivaluedHashMap<String, String>();
				queryParams.add(lkvb.getValue(), patientCriteria);

				// Set date criteria (if defined)
				if (startDate != null || endDate != null) {

					LabelKeyValueBean dateCriteria = ResourceType.getEverythingDateCriteria(lkvb.getKey());
					if (dateCriteria != null) {

						// Set startDate criteria if defined
						if (startDateCriteria != null) {
							queryParams.add(dateCriteria.getValue(), startDateCriteria);
						}
						// Set endDate criteria if defined
						if (endDateCriteria != null) {
							queryParams.add(dateCriteria.getValue(), endDateCriteria);
						}
					}
				}

				resources = resourceService.searchQuery(queryParams, null, null, lkvb.getKey(), false, null, null, null, null, null);

				if (resources != null && resources.size() > 0) {
					/*
					 * Add found resources to bundle
					 */
					for (Resource resourceEntry : resources) {

						// Convert XML contents to Resource object
						iResource = new ByteArrayInputStream(resourceEntry.getResourceContents());
						org.hl7.fhir.r4.model.Resource resourceObject = xmlP.parse(iResource);

						everythingKey = resourceObject.getId();

						// Test for existing resource in everythingResources
						if (!everythingResources.containsKey(everythingKey)) {

							log.info("[1]Adding resource type " + resourceObject.getResourceType().getPath() + "; resource id " + everythingKey);

							everythingResources.put(everythingKey, resourceObject);

							// Check for linked resources
							LinkedResourceProxy linkedProxy = linkedProxyFactory.getLinkedResourceProxy(resourceObject.getResourceType().getPath());

							if (linkedProxy != null) {

								log.info("[2]Found Linked Resource Proxy " + linkedProxy.getClass().getName());

								List<org.hl7.fhir.r4.model.Resource> linkedResources = linkedProxy.getLinkedResources(resourceService, resourceObject);

								if (linkedResources != null && !linkedResources.isEmpty()) {

									for (org.hl7.fhir.r4.model.Resource linkedResource : linkedResources) {

										everythingKey = linkedResource.getId();

										log.info("[2]Adding linked resource type " + linkedResource.getResourceType().getPath() + "; resource id " + everythingKey);

										// Test for existing resource in everythingResources
										if (!everythingResources.containsKey(everythingKey)) {
											everythingResources.put(everythingKey, linkedResource);
										}
									}
								}
								else {

									log.info("[2]No Linked Resources found");

								}
							}
							else {

								log.info("[2]Linked Resource Proxy NOT FOUND");

							}
						}
						else {

							log.info("[1]Already in map - resource type " + resourceObject.getResourceType().getPath() + "; resource id " + everythingKey);

						}
					}
				}
			}
			else {
				log.info("Skipping resource type " + lkvb.getKey());
			}
		}

		Bundle bundle = new Bundle();

		bundle.setId(UUIDUtil.getUUID());
		Meta bundleMeta = new Meta();
		bundleMeta.setVersionId("1");
		bundleMeta.setLastUpdated(new Date());
		bundle.setMeta(bundleMeta);
		bundle.setType(BundleType.SEARCHSET);

		int bundleTotal = 0;

		/*
		 * Add the everything resources
		 */
		BundleEntryComponent bundleEntry = new BundleEntryComponent();
		String fullUrl = "";
		BundleEntrySearchComponent bundleEntrySearch = null;

		// Iterate over the everything resources map and add all as Bundle entries
		for (Map.Entry<String, org.hl7.fhir.r4.model.Resource> entry : everythingResources.entrySet()) {
			bundleEntry = new BundleEntryComponent();
			// Build and set Bundle.entry.fullUrl
			fullUrl = baseUrl + "/" + entry.getValue().getResourceType() + "/" + entry.getValue().getId();
			bundleEntry.setFullUrl(fullUrl);
			// Set resource contents
			bundleEntry.setResource(entry.getValue());
			// Set search match mode
			bundleEntrySearch = new BundleEntrySearchComponent();
			bundleEntrySearch.setMode(SearchEntryMode.MATCH);
			bundleEntry.setSearch(bundleEntrySearch);
			bundle.getEntry().add(bundleEntry);
			bundleTotal++;
		}

		bundle.setTotal(bundleTotal);

		return bundle;
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
				DateType startDate = null;
				DateType endDate = null;

				// Get the query parameters that represent the search criteria
				MultivaluedMap<String, String> queryParams = context.getQueryParameters();

				if (queryParams != null && queryParams.size() > 0) {
					Set<Entry<String, List<String>>> paramSet = queryParams.entrySet();

					for (Entry<String, List<String>> entry : paramSet) {

						String key = entry.getKey();
						String value = entry.getValue().get(0);

						if (key.equals("start")) {
							ParametersParameterComponent parameter = new ParametersParameterComponent();
							parameter.setName(key);
							startDate = new DateType();
							startDate.setValueAsString(value);
							parameter.setValue(startDate);
							queryParameters.addParameter(parameter);
						}
						else if (key.equals("end")) {
							ParametersParameterComponent parameter = new ParametersParameterComponent();
							parameter.setName(key);
							endDate = new DateType();
							endDate.setValueAsString(value);
							parameter.setValue(endDate);
							queryParameters.addParameter(parameter);
						}
					}
				}
			}
		}
		catch (Exception e) {
			// Handle generic exceptions
			log.severe(e.getMessage());
			e.printStackTrace();
			throw e;
		}

		return queryParameters;
	}

}
