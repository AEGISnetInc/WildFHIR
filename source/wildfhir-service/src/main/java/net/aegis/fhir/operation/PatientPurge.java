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

import java.util.List;
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
import net.aegis.fhir.service.narrative.FHIRNarrativeGeneratorClient;
import net.aegis.fhir.service.util.ServicesUtil;

import org.hl7.fhir.r4.model.DateType;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Parameters.ParametersParameterComponent;

/**
 * @author richard.ettema
 *
 */
public class PatientPurge extends ResourceOperationProxy {

	private Logger log = Logger.getLogger("PatientPurge");

	/* (non-Javadoc)
	 * @see net.aegis.fhir.operation.ResourceOperationProxy#executeOperation(javax.ws.rs.core.UriInfo, javax.ws.rs.core.HttpHeaders, net.aegis.fhir.service.ResourceService, net.aegis.fhir.service.ResourcemetadataService, net.aegis.fhir.service.BatchService, net.aegis.fhir.service.TransactionService, net.aegis.fhir.service.CodeService, net.aegis.fhir.service.ConformanceService, java.lang.String, java.lang.String, java.lang.String, org.hl7.fhir.r4.model.Parameters, org.hl7.fhir.r4.model.Resource, java.lang.String, java.lang.String, boolean, java.lang.StringBuffer)
	 */
	@Override
	public Parameters executeOperation(UriInfo context, HttpHeaders headers, ResourceService resourceService, ResourcemetadataService resourcemetadataService, BatchService batchService, TransactionService transactionService, CodeService codeService, ConformanceService conformanceService, String softwareVersion, String resourceType, String resourceId, Parameters inputParameters, org.hl7.fhir.r4.model.Resource inputResource, String inputString, String contentType, boolean isPost, StringBuffer returnedDirective) throws Exception {

		log.fine("[START] PatientPurge.executeOperation()");

		Parameters out = null;
		OperationOutcome rOutcome = null;

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

						if (parameter.getValue() instanceof DateType) {
							startDate = (DateType) parameter.getValue();
						}
					}

					if (parameter.getName() != null && parameter.getName().equals("end")) {

						if (parameter.getValue() instanceof DateType) {
							endDate = (DateType) parameter.getValue();
						}
					}
				}
			}

			/*
			 * patient purge check
			 * - $purge must be executed against an existing resource instance; i.e. the url path must contain the id [base]/[resource]/[id]/$purge
			 *   or an authorized patient has been sent.
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
									"Patient $purge failed. The [id] url value does not reference a valid resource instance.", null, null);

					if (issue != null) {
						rOutcome.setText(null);
						rOutcome.getIssue().add(issue);

						// Use RI NarrativeGenerator
						FHIRNarrativeGeneratorClient.instance().generate(rOutcome);
					}
				}
				else {
					// Process purge on the found Patient

					/*
					 * Perform a search operation against all Patient Compartment resource types and permanently delete all
					 * database entries. The response is an OperationOutcome containing any issues that occurred.
					 */
					rOutcome = executePatientPurge(resourceService, resourceContainer.getResource(), startDate, endDate);
				}
			}
			else {
				rOutcome = new OperationOutcome();
				OperationOutcome.OperationOutcomeIssueComponent issue =
						ServicesUtil.INSTANCE.getOperationOutcomeIssueComponent(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.INCOMPLETE,
								"Patient $purge failed. No [id] value found in the $purge path. The interaction is performed by an HTTP GET or POST command with a path of [base]/Patient/[id]/$purge where [id] references a known, valid Patient resource.", null, null);

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
			parameter.setResource(rOutcome);

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
	 * @param resourceService
	 * @param patient
	 * @param startDate
	 * @param endDate
	 * @return Constructed OperationOutcome response
	 * @throws Exception
	 */
	private OperationOutcome executePatientPurge(ResourceService resourceService, Resource patient, DateType startDate, DateType endDate) throws Exception {

		log.fine("[START] PatientPurge.getPatientPurge()");

		int total = 0;
		OperationOutcome rOutcome = new OperationOutcome();
		OperationOutcome.OperationOutcomeIssueComponent issue = null;

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
		 * a single OperationOutcome.
		 */
		String patientCriteria = "Patient/" + patient.getResourceId();
		MultivaluedMap<String, String> queryParams = null;
		List<Resource> resources = null;

		List<LabelKeyValueBean> compartmentResourceTypeCriteriaList = ResourceType.getCompartmentResourceTypeCriteria(patient.getResourceType());

		for (LabelKeyValueBean lkvb : compartmentResourceTypeCriteriaList) {

			log.info("========================================================================");
			log.info("===== Processing resource type " + lkvb.getKey());
			log.info("========================================================================");

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
				 * Purge found resources
				 */
				for (Resource resourceEntry : resources) {
					log.info("     ----- Purging resource " + resourceEntry.getResourceType() + "/" + resourceEntry.getResourceId());

					resourceService.purge(resourceEntry.getId());
				}

				total += resources.size();

				issue = ServicesUtil.INSTANCE.getOperationOutcomeIssueComponent(OperationOutcome.IssueSeverity.INFORMATION, OperationOutcome.IssueType.INFORMATIONAL,
					"Patient $purge - " + resources.size() + " referenced " + lkvb.getKey() + " have been deleted.", null, null);

				rOutcome.getIssue().add(issue);
			}
		}

		/*
		 * Finally, purge the Patient if no date parameters were sent
		 */
		if (startDateCriteria == null && endDateCriteria == null) {
			log.info("Purging Patient");

			resourceService.purge(patient.getId());
			total++;

			issue = ServicesUtil.INSTANCE.getOperationOutcomeIssueComponent(OperationOutcome.IssueSeverity.INFORMATION, OperationOutcome.IssueType.INFORMATIONAL,
				"Patient $purge - Patient deleted.", null, null);

			rOutcome.getIssue().add(issue);
		}
		else {
			log.info("Start or End date parameters sent; skipping purge of Patient");

			issue = ServicesUtil.INSTANCE.getOperationOutcomeIssueComponent(OperationOutcome.IssueSeverity.INFORMATION, OperationOutcome.IssueType.INFORMATIONAL,
				"Patient $purge - Patient purge skipped; start or end date parameter sent.", null, null);

			rOutcome.getIssue().add(issue);
		}

		issue = ServicesUtil.INSTANCE.getOperationOutcomeIssueComponent(OperationOutcome.IssueSeverity.INFORMATION, OperationOutcome.IssueType.INFORMATIONAL,
			"Patient $purge - Total purged resource(s) " + total + ".", null, null);

		rOutcome.getIssue().add(issue);

		rOutcome.setText(null);

		// Use RI NarrativeGenerator
		FHIRNarrativeGeneratorClient.instance().generate(rOutcome);

		return rOutcome;
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
