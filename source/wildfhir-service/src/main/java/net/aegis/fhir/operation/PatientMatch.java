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

import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.SearchEntryMode;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.DecimalType;
import org.hl7.fhir.r4.model.IntegerType;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Parameters.ParametersParameterComponent;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Resource;

import net.aegis.fhir.model.ResourceContainer;
import net.aegis.fhir.model.Resourcemetadata;
import net.aegis.fhir.service.BatchService;
import net.aegis.fhir.service.CodeService;
import net.aegis.fhir.service.ConformanceService;
import net.aegis.fhir.service.ResourceService;
import net.aegis.fhir.service.ResourcemetadataService;
import net.aegis.fhir.service.TransactionService;
import net.aegis.fhir.service.util.ServicesUtil;
import net.aegis.fhir.service.util.UTCDateUtil;

/**
 * @author richard.ettema
 *
 */
public class PatientMatch extends ResourceOperationProxy {

	private Logger log = Logger.getLogger("PatientMatch");

	private ResourceService resourceService;

	/* (non-Javadoc)
	 * @see net.aegis.fhir.operation.ResourceOperationProxy#executeOperation(javax.ws.rs.core.UriInfo, javax.ws.rs.core.HttpHeaders, net.aegis.fhir.service.ResourceService, net.aegis.fhir.service.ResourcemetadataService, net.aegis.fhir.service.BatchService, net.aegis.fhir.service.TransactionService, net.aegis.fhir.service.CodeService, net.aegis.fhir.service.ConformanceService, java.lang.String, java.lang.String, java.lang.String, org.hl7.fhir.r4.model.Parameters, org.hl7.fhir.r4.model.Resource, java.lang.String, java.lang.String, boolean, java.lang.StringBuffer)
	 */
	@Override
	public Parameters executeOperation(UriInfo context, HttpHeaders headers, ResourceService resourceService, ResourcemetadataService resourcemetadataService, BatchService batchService, TransactionService transactionService, CodeService codeService, ConformanceService conformanceService, String softwareVersion, String resourceType, String resourceId, Parameters inputParameters, org.hl7.fhir.r4.model.Resource inputResource, String inputString, String contentType, boolean isPost, StringBuffer returnedDirective) throws Exception {

        log.fine("[START] PatientMatch.executeOperation()");

        this.resourceService = resourceService;

		Parameters out = new Parameters();

		try {
			/*
			 * If inputParameters is null, throw exception
			 */
			if (inputParameters == null) {
				throw new Exception("$match failed. The input parameters contents were empty or null.");
			}

			/*
			 * Extract the individual expected parameters
			 */
			Patient paramResource = null;
			BooleanType paramOnlyCertainMatches = null; // default is false
			IntegerType paramCount = null; // zero means no input value defined

			if (inputParameters != null && inputParameters.hasParameter()) {

				for (ParametersParameterComponent parameter : inputParameters.getParameter()) {

					if (parameter.getName() != null && parameter.getName().equals("resource")) {

						paramResource = (Patient) parameter.getResource();
					}

					if (parameter.getName() != null && parameter.getName().equals("onlyCertainMatches")) {

						paramOnlyCertainMatches = (BooleanType) parameter.getValue();
					}

					if (parameter.getName() != null && parameter.getName().equals("count")) {

						paramCount = (IntegerType) parameter.getValue();
					}
				}
			}

			// (Patient) resource parameter is required
			if (paramResource == null) {
				throw new Exception("$match failed. The input parameter resource contents were empty or null.");
			}
			// onlyCertainMatches parameter is optional; default to false if null
			if (paramOnlyCertainMatches == null) {
				paramOnlyCertainMatches = new BooleanType(false);
			}
			// count parameter is optional; default to zero if null
			if (paramCount == null) {
				paramCount = new IntegerType(0);
			}

			Resource matchResponse = peformPatientMatch(context, paramResource, paramOnlyCertainMatches, paramCount);

			if (matchResponse != null) {
				// Return matchResponse resource as-is as the return output parameter
				out = new Parameters();

				ParametersParameterComponent parameter = new ParametersParameterComponent();
				parameter.setName("return");
				parameter.setResource(matchResponse);

				out.addParameter(parameter);
			}
			else {
				// matchResponse resource is null; throw exception with error message (this should not happen)
				throw new Exception("The return output parameter resource contents were empty or null!");
			}
		}
		catch (Exception e) {
			// Throw exceptions back
			e.printStackTrace();
			throw new Exception("$match failed! Exception thrown: " + e.getMessage());
		}

		return out;
	}

	private Resource peformPatientMatch(UriInfo context, Resource patientCriteria, BooleanType onlyCertainMatches, IntegerType count) throws Exception {

		Resource matchResponse = null;

		UTCDateUtil utcDateUtil = new UTCDateUtil();

		String baseUrl = ServicesUtil.INSTANCE.extractBaseURL(context.getAbsolutePath().toString(), "/Patient");

		// Extract Patient search parameter values from patientCriteria and build search parameter string
		List<Resourcemetadata> resourcemetadataList = this.resourceService.getResourcemetadataMatch(patientCriteria, baseUrl);

		StringBuilder metaParams = new StringBuilder("");

		for (Resourcemetadata meta : resourcemetadataList) {

			if (metaParams.length() > 3) {
				metaParams.append("&");
			}

			if (meta.getParamValue() != null && !meta.getParamValue().isEmpty()) {
				if (meta.getParamName().contains("birthdate")) {
					Date birthDate = utcDateUtil.parseDate(meta.getParamValue(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getTimeZone(UTCDateUtil.TIME_ZONE_UTC));
					String sBirthDate = utcDateUtil.formatDate(birthDate, UTCDateUtil.DATE_ONLY_PARAMETER_FORMAT, TimeZone.getTimeZone(UTCDateUtil.TIME_ZONE_UTC));

					if (!sBirthDate.isEmpty()) {
						metaParams.append(meta.getParamName()).append("=").append(sBirthDate);
					}
				}
				else if (meta.getParamName().contains("_lastUpdated")) {
					Date lastUpdated = utcDateUtil.parseDate(meta.getParamValue(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getTimeZone(UTCDateUtil.TIME_ZONE_UTC));
					String sLastUpdated = utcDateUtil.formatDate(lastUpdated, UTCDateUtil.DATE_PARAMETER_FORMAT, TimeZone.getTimeZone(UTCDateUtil.TIME_ZONE_UTC));

					if (!sLastUpdated.isEmpty()) {
						metaParams.append(meta.getParamName()).append("=").append(sLastUpdated);
					}
				}
				else {
					metaParams.append(meta.getParamName()).append("=");

					if (meta.getSystemValue() != null && !meta.getSystemValue().isEmpty()) {
						metaParams.append(meta.getSystemValue()).append("|");
					}
					if (meta.getParamValue() != null && !meta.getParamValue().isEmpty()) {
						metaParams.append(meta.getParamValue());
					}
					if (meta.getCodeValue() != null && !meta.getCodeValue().isEmpty()) {
						metaParams.append("|").append(meta.getCodeValue());
					}
				}
			}
		}

		// Convert search parameter string into queryParams map
		List<NameValuePair> params = URLEncodedUtils.parse(metaParams.toString(), Charset.defaultCharset());
		MultivaluedMap<String, String> queryParams = ServicesUtil.INSTANCE.listNameValuePairToMultivaluedMapString(params);

		// Initialize match-grade=certain; score=1.0
		CodeType matchGrade = new CodeType("certain");
		DecimalType searchScore = new DecimalType(1.0);

		// 3 - Perform search with extracted parameters
		ResourceContainer rcPatientMatch = this.resourceService.search(queryParams, null, null, null, patientCriteria.getResourceType().name(), context.getRequestUri().toString(), null, null, null, false);

		// Check for matched Patient resources
		if (rcPatientMatch != null && rcPatientMatch.getBundle() != null) {

			// If single match, grade=certain; score=1.0, stop
			if (!rcPatientMatch.getBundle().getEntry().isEmpty() && rcPatientMatch.getBundle().getEntry().size() == 1) {
				rcPatientMatch.getBundle().getEntry().get(0).getSearch().addExtension("http://hl7.org/fhir/StructureDefinition/match-grade", matchGrade);
				rcPatientMatch.getBundle().getEntry().get(0).getSearch().setMode(SearchEntryMode.MATCH);
				rcPatientMatch.getBundle().getEntry().get(0).getSearch().setScoreElement(searchScore);
			}
			// Else If multiple matches, grade=probable; score=0.7
			else if (!rcPatientMatch.getBundle().getEntry().isEmpty() && rcPatientMatch.getBundle().getEntry().size() > 1) {
				matchGrade = new CodeType("probable");
				searchScore = new DecimalType(0.7);

				for (BundleEntryComponent matchEntry : rcPatientMatch.getBundle().getEntry()) {
					matchEntry.getSearch().addExtension("http://hl7.org/fhir/StructureDefinition/match-grade", matchGrade);
					matchEntry.getSearch().setMode(SearchEntryMode.MATCH);
					matchEntry.getSearch().setScoreElement(searchScore);
				}
			}
			// Else no matches, return empty searchset

			matchResponse = rcPatientMatch.getBundle();
		}

		return matchResponse;
	}

}
