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
package net.aegis.fhir.service.metadata;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.formats.XmlParser;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.r4.model.DiagnosticReport.DiagnosticReportMediaComponent;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;

import net.aegis.fhir.model.Resource;
import net.aegis.fhir.model.Resourcemetadata;
import net.aegis.fhir.service.ResourceService;
import net.aegis.fhir.service.util.ServicesUtil;
import net.aegis.fhir.service.util.UTCDateUtil;

/**
 * @author richard.ettema
 *
 */
public class ResourcemetadataDiagnosticReport extends ResourcemetadataProxy {

	/* (non-Javadoc)
	 * @see net.aegis.fhir.service.metadata.ResourcemetadataProxy#generateAllForResource(net.aegis.fhir.model.Resource, java.lang.String, net.aegis.fhir.service.ResourceService)
	 */
	@Override
	public List<Resourcemetadata> generateAllForResource(Resource resource, String baseUrl, ResourceService resourceService) throws Exception {
		return generateAllForResource(resource, baseUrl, resourceService, null, null, 0, null);
	}

	/* (non-Javadoc)
	 * @see net.aegis.fhir.service.metadata.ResourcemetadataProxy#generateAllForResource(net.aegis.fhir.model.Resource, java.lang.String, net.aegis.fhir.service.ResourceService, net.aegis.fhir.model.Resource, java.lang.String, int, org.hl7.fhir.r4.model.Resource)
	 */
	@Override
	public List<Resourcemetadata> generateAllForResource(Resource resource, String baseUrl, ResourceService resourceService, Resource chainedResource, String chainedParameter, int chainedIndex, org.hl7.fhir.r4.model.Resource fhirResource) throws Exception {

		if (StringUtils.isEmpty(chainedParameter)) {
			chainedParameter = "";
		}

		List<Resourcemetadata> resourcemetadataList = new ArrayList<Resourcemetadata>();
        ByteArrayInputStream iDiagnosticReport = null;

		try {
			// Extract and convert the resource contents to a DiagnosticReport object
			if (chainedResource != null) {
				iDiagnosticReport = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iDiagnosticReport = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
			DiagnosticReport diagnosticReport = (DiagnosticReport) xmlP.parse(iDiagnosticReport);
			iDiagnosticReport.close();

			/*
			 * Create new Resourcemetadata objects for each DiagnosticReport metadata value and add to the resourcemetadataList
			 */

			// Add any passed in tags
			List<Resourcemetadata> tagMetadataList = this.generateResourcemetadataTagList(resource, diagnosticReport, chainedParameter);
			resourcemetadataList.addAll(tagMetadataList);

			// _id : token
			if (diagnosticReport.getId() != null) {
				Resourcemetadata _id = generateResourcemetadata(resource, chainedResource, chainedParameter+"_id", diagnosticReport.getId());
				resourcemetadataList.add(_id);
			}

			// _language : token
			if (diagnosticReport.getLanguage() != null) {
				Resourcemetadata _language = generateResourcemetadata(resource, chainedResource, chainedParameter+"_language", diagnosticReport.getLanguage());
				resourcemetadataList.add(_language);
			}

			// _lastUpdated : date
			if (diagnosticReport.getMeta() != null && diagnosticReport.getMeta().getLastUpdated() != null) {
				Resourcemetadata _lastUpdated = generateResourcemetadata(resource, chainedResource, chainedParameter+"_lastUpdated", utcDateUtil.formatDate(diagnosticReport.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(diagnosticReport.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(_lastUpdated);
			}

			// based-on : reference
			if (diagnosticReport.hasBasedOn()) {

				List<Resourcemetadata> rBasedOnChain = null;
				for (Reference basedOn : diagnosticReport.getBasedOn()) {

					if (basedOn.hasReference()) {
						Resourcemetadata rBasedOn = generateResourcemetadata(resource, chainedResource, chainedParameter+"based-on", generateFullLocalReference(basedOn.getReference(), baseUrl));
						resourcemetadataList.add(rBasedOn);

						if (chainedResource == null) {
							// Add chained parameters for any
							rBasedOnChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "based-on", 0, basedOn.getReference(), null);
							resourcemetadataList.addAll(rBasedOnChain);
						}
					}
				}
			}

			// category : token
			if (diagnosticReport.hasCategory()) {

				for (CodeableConcept category : diagnosticReport.getCategory()) {

					for (Coding coding : category.getCoding()) {

						Resourcemetadata rCoding = generateResourcemetadata(resource, chainedResource, chainedParameter+"category", coding.getCode(), coding.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(coding));
						resourcemetadataList.add(rCoding);
					}
				}
			}

			// code : token
			if (diagnosticReport.hasCode()) {

				for (Coding coding : diagnosticReport.getCode().getCoding()) {

					Resourcemetadata rCoding = generateResourcemetadata(resource, chainedResource, chainedParameter+"code", coding.getCode(), coding.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(coding));
					resourcemetadataList.add(rCoding);
				}
			}

			// encounter : reference
			if (diagnosticReport.hasEncounter() && diagnosticReport.getEncounter().hasReference()) {
				String encounterReference = generateFullLocalReference(diagnosticReport.getEncounter().getReference(), baseUrl);

				Resourcemetadata rEncounter = generateResourcemetadata(resource, chainedResource, chainedParameter+"encounter", encounterReference);
				resourcemetadataList.add(rEncounter);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rEncounterChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "encounter", 0, diagnosticReport.getEncounter().getReference(), null);
					resourcemetadataList.addAll(rEncounterChain);
				}
			}

			// date : date(period)
			if (diagnosticReport.hasEffective()) {

				if (diagnosticReport.hasEffectiveDateTimeType()) {
					Resourcemetadata rDateDateTime = generateResourcemetadata(resource, chainedResource, chainedParameter+"date", utcDateUtil.formatDate(diagnosticReport.getEffectiveDateTimeType().getValue(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(diagnosticReport.getEffectiveDateTimeType().getValue(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
					resourcemetadataList.add(rDateDateTime);
				}
				else if (diagnosticReport.hasEffectivePeriod()) {
					Resourcemetadata rDatePeriod = generateResourcemetadata(resource, chainedResource, chainedParameter+"date", utcDateUtil.formatDate(diagnosticReport.getEffectivePeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(diagnosticReport.getEffectivePeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(diagnosticReport.getEffectivePeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), utcDateUtil.formatDate(diagnosticReport.getEffectivePeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), "PERIOD");
					resourcemetadataList.add(rDatePeriod);
				}
			}

			// identifier : token
			if (diagnosticReport.hasIdentifier()) {

				for (Identifier identifier : diagnosticReport.getIdentifier()) {

					Resourcemetadata rIdentifier = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rIdentifier);
				}
			}

			// issued : date
			if (diagnosticReport.hasIssued()) {
				Resourcemetadata rIssued = generateResourcemetadata(resource, chainedResource, chainedParameter+"issued", utcDateUtil.formatDate(diagnosticReport.getIssued(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(diagnosticReport.getIssued(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rIssued);
			}

			// media : token
			if (diagnosticReport.hasMedia()) {

				for (DiagnosticReportMediaComponent media : diagnosticReport.getMedia()) {

					if (media.hasLink() && media.getLink().hasReference()) {
						Resourcemetadata rMedia = generateResourcemetadata(resource, chainedResource, chainedParameter+"media", media.getLink().getReference());
						resourcemetadataList.add(rMedia);
					}
				}
			}

			// performer : reference
			if (diagnosticReport.hasPerformer()) {

				for (Reference performer : diagnosticReport.getPerformer()) {

					if (performer.hasReference()) {
						Resourcemetadata rRequestDetail = generateResourcemetadata(resource, chainedResource, chainedParameter+"performer", generateFullLocalReference(performer.getReference(), baseUrl));
						resourcemetadataList.add(rRequestDetail);

						if (chainedResource == null) {
							// Add chained parameters for any
							List<Resourcemetadata> rPerformerChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "performer", 0, performer.getReference(), null);
							resourcemetadataList.addAll(rPerformerChain);
						}
					}
				}
			}

			// result : reference
			if (diagnosticReport.hasResult()) {

				for (Reference result : diagnosticReport.getResult()) {

					if (result.hasReference()) {
						Resourcemetadata rResult = generateResourcemetadata(resource, chainedResource, chainedParameter+"result", generateFullLocalReference(result.getReference(), baseUrl));
						resourcemetadataList.add(rResult);

						if (chainedResource == null) {
							// Add chained parameters
							List<Resourcemetadata> rResultChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "result", 0, result.getReference(), null);
							resourcemetadataList.addAll(rResultChain);
						}
					}
				}
			}

			// results-interpreter : reference
			if (diagnosticReport.hasResultsInterpreter()) {

				for (Reference resultsInterpreter : diagnosticReport.getResultsInterpreter()) {

					if (resultsInterpreter.hasReference()) {
						Resourcemetadata rResultsInterpreter = generateResourcemetadata(resource, chainedResource, chainedParameter+"results-interpreter", generateFullLocalReference(resultsInterpreter.getReference(), baseUrl));
						resourcemetadataList.add(rResultsInterpreter);

						if (chainedResource == null) {
							// Add chained parameters for any
							List<Resourcemetadata> rResultsInterpreterChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "results-interpreter", 0, resultsInterpreter.getReference(), null);
							resourcemetadataList.addAll(rResultsInterpreterChain);
						}
					}
				}
			}

			// specimen : reference
			if (diagnosticReport.hasSpecimen()) {

				for (Reference specimen : diagnosticReport.getSpecimen()) {

					if (specimen.hasReference()) {
						Resourcemetadata rSpecimen = generateResourcemetadata(resource, chainedResource, chainedParameter+"specimen", generateFullLocalReference(specimen.getReference(), baseUrl));
						resourcemetadataList.add(rSpecimen);

						if (chainedResource == null) {
							// Add chained parameters
							List<Resourcemetadata> rSpecimenChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "specimen", 0, specimen.getReference(), null);
							resourcemetadataList.addAll(rSpecimenChain);
						}
					}
				}
			}

			// status : token
			if (diagnosticReport.hasStatus() && diagnosticReport.getStatus() != null) {
				Resourcemetadata rStatus = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", diagnosticReport.getStatus().toCode(), diagnosticReport.getStatus().getSystem());
				resourcemetadataList.add(rStatus);
			}

			// patient : reference
			// subject : reference
			if (diagnosticReport.hasSubject() && diagnosticReport.getSubject().hasReference()) {
				String subjectReference = generateFullLocalReference(diagnosticReport.getSubject().getReference(), baseUrl);

				Resourcemetadata rSubject = generateResourcemetadata(resource, chainedResource, chainedParameter+"subject", subjectReference);
				resourcemetadataList.add(rSubject);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rSubjectChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "subject", 0, diagnosticReport.getSubject().getReference(), null);
					resourcemetadataList.addAll(rSubjectChain);
				}

				if (subjectReference.indexOf("Patient") >= 0) {
					Resourcemetadata rPatient = generateResourcemetadata(resource, chainedResource, chainedParameter+"patient", subjectReference);
					resourcemetadataList.add(rPatient);

					if (chainedResource == null) {
						// Add chained parameters
						List<Resourcemetadata> rPatientChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "patient", 0, diagnosticReport.getSubject().getReference(), null);
						resourcemetadataList.addAll(rPatientChain);
					}
				}
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		}

		return resourcemetadataList;
	}

}
