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

import net.aegis.fhir.model.Resource;
import net.aegis.fhir.model.Resourcemetadata;
import net.aegis.fhir.service.ResourceService;
import net.aegis.fhir.service.util.ServicesUtil;
import net.aegis.fhir.service.util.UTCDateUtil;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.formats.XmlParser;
import org.hl7.fhir.r4.model.ClinicalImpression;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.ClinicalImpression.ClinicalImpressionFindingComponent;
import org.hl7.fhir.r4.model.ClinicalImpression.ClinicalImpressionInvestigationComponent;
import org.hl7.fhir.r4.model.Reference;

/**
 * @author richard.ettema
 *
 */
public class ResourcemetadataClinicalImpression extends ResourcemetadataProxy {

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
        ByteArrayInputStream iClinicalImpression = null;

		try {
            // Extract and convert the resource contents to a ClinicalImpression object
			if (chainedResource != null) {
				iClinicalImpression = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iClinicalImpression = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
            ClinicalImpression clinicalImpression = (ClinicalImpression) xmlP.parse(iClinicalImpression);
            iClinicalImpression.close();

			/*
             * Create new Resourcemetadata objects for each ClinicalImpression metadata value and add to the resourcemetadataList
			 */

			// Add any passed in tags
			List<Resourcemetadata> tagMetadataList = this.generateResourcemetadataTagList(resource, clinicalImpression, chainedParameter);
			resourcemetadataList.addAll(tagMetadataList);

			// _id : token
			if (clinicalImpression.getId() != null) {
				Resourcemetadata _id = generateResourcemetadata(resource, chainedResource, chainedParameter+"_id", clinicalImpression.getId());
				resourcemetadataList.add(_id);
			}

			// _language : token
			if (clinicalImpression.getLanguage() != null) {
				Resourcemetadata _language = generateResourcemetadata(resource, chainedResource, chainedParameter+"_language", clinicalImpression.getLanguage());
				resourcemetadataList.add(_language);
			}

			// _lastUpdated : date
			if (clinicalImpression.getMeta() != null && clinicalImpression.getMeta().getLastUpdated() != null) {
				Resourcemetadata _lastUpdated = generateResourcemetadata(resource, chainedResource, chainedParameter+"_lastUpdated", utcDateUtil.formatDate(clinicalImpression.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(clinicalImpression.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(_lastUpdated);
			}

			// assessor : reference
			if (clinicalImpression.hasAssessor() && clinicalImpression.getAssessor().hasReference()) {
				Resourcemetadata rAssessor = generateResourcemetadata(resource, chainedResource, chainedParameter+"assessor", generateFullLocalReference(clinicalImpression.getAssessor().getReference(), baseUrl));
				resourcemetadataList.add(rAssessor);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rAssessorChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "assessor", 0, clinicalImpression.getAssessor().getReference(), null);
					resourcemetadataList.addAll(rAssessorChain);
				}
			}

			// date : date
			if (clinicalImpression.hasDate()) {
				Resourcemetadata rDate = generateResourcemetadata(resource, chainedResource, chainedParameter+"date", utcDateUtil.formatDate(clinicalImpression.getDate(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(clinicalImpression.getDate(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rDate);
			}

			// encounter : reference
			if (clinicalImpression.hasEncounter() && clinicalImpression.getEncounter().hasReference()) {
				String encounterReference = generateFullLocalReference(clinicalImpression.getEncounter().getReference(), baseUrl);

				Resourcemetadata rEncounter = generateResourcemetadata(resource, chainedResource, chainedParameter+"encounter", encounterReference);
				resourcemetadataList.add(rEncounter);

				if (chainedResource == null) {
					// Add chained parameters for any
					List<Resourcemetadata> rEncounterChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "encounter", 0, clinicalImpression.getEncounter().getReference(), null);
					resourcemetadataList.addAll(rEncounterChain);
				}
			}

			// finding : token
			if (clinicalImpression.hasFinding()) {

				Resourcemetadata rCode = null;
				String findingReference = null;
				for (ClinicalImpressionFindingComponent finding : clinicalImpression.getFinding()) {

					if (finding.hasItemCodeableConcept() && finding.getItemCodeableConcept().hasCoding()) {

						for (Coding code : finding.getItemCodeableConcept().getCoding()) {
							rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"finding-code", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rCode);
						}
					}
					else if (finding.hasItemReference() && finding.getItemReference().hasReference()) {
						findingReference = generateFullLocalReference(finding.getItemReference().getReference(), baseUrl);

						Resourcemetadata rFindingRef = generateResourcemetadata(resource, chainedResource, chainedParameter+"finding-ref", findingReference);
						resourcemetadataList.add(rFindingRef);

						if (chainedResource == null) {
							// Add chained parameters for any
							List<Resourcemetadata> rFindingRefChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "finding-ref", 02, finding.getItemReference().getReference(), null);
							resourcemetadataList.addAll(rFindingRefChain);
						}
					}
				}
			}

			// identifier : token
			if (clinicalImpression.hasIdentifier()) {

				for (Identifier identifier : clinicalImpression.getIdentifier()) {

					Resourcemetadata rIdentifier = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rIdentifier);
				}
			}

			// investigation : reference
			if (clinicalImpression.hasInvestigation()) {

				String investigationReference = null;
				for (ClinicalImpressionInvestigationComponent investigation : clinicalImpression.getInvestigation()) {

					if (investigation.hasItem()) {

						for (Reference item : investigation.getItem()) {

							if (item.hasReference()) {
								investigationReference = generateFullLocalReference(item.getReference(), baseUrl);

								Resourcemetadata rInvestigation = generateResourcemetadata(resource, chainedResource, chainedParameter+"investigation", investigationReference);
								resourcemetadataList.add(rInvestigation);

								if (chainedResource == null) {
									// Add chained parameters for any
									List<Resourcemetadata> rInvestigationChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "investigation", 0, item.getReference(), null);
									resourcemetadataList.addAll(rInvestigationChain);
								}
							}
						}
					}
				}
			}

			// patient : reference
			// subject : reference
			if (clinicalImpression.hasSubject() && clinicalImpression.getSubject().hasReference()) {
				String subjectReference = generateFullLocalReference(clinicalImpression.getSubject().getReference(), baseUrl);

				Resourcemetadata rSubject = generateResourcemetadata(resource, chainedResource, chainedParameter+"subject", subjectReference);
				resourcemetadataList.add(rSubject);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rSubjectChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "subject", 0, clinicalImpression.getSubject().getReference(), null);
					resourcemetadataList.addAll(rSubjectChain);
				}

				if (subjectReference.indexOf("Patient") >= 0) {
					Resourcemetadata rPatient = generateResourcemetadata(resource, chainedResource, chainedParameter+"patient", subjectReference);
					resourcemetadataList.add(rPatient);

					if (chainedResource == null) {
						// Add chained parameters
						List<Resourcemetadata> rPatientChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "patient", 0, clinicalImpression.getSubject().getReference(), null);
						resourcemetadataList.addAll(rPatientChain);
					}
				}
			}

			// previous : reference
			if (clinicalImpression.hasPrevious() && clinicalImpression.getPrevious().hasReference()) {
				Resourcemetadata rPrevious = generateResourcemetadata(resource, chainedResource, chainedParameter+"previous", generateFullLocalReference(clinicalImpression.getPrevious().getReference(), baseUrl));
				resourcemetadataList.add(rPrevious);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rPreviousChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "previous", 0, clinicalImpression.getPrevious().getReference(), null);
					resourcemetadataList.addAll(rPreviousChain);
				}
			}

			// problem : reference
			if (clinicalImpression.hasProblem()) {

				String problemReference = null;
				for (Reference problem : clinicalImpression.getProblem()) {

					if (problem.hasReference()) {
						problemReference = generateFullLocalReference(problem.getReference(), baseUrl);

						Resourcemetadata rProblem = generateResourcemetadata(resource, chainedResource, chainedParameter+"problem", problemReference);
						resourcemetadataList.add(rProblem);

						if (chainedResource == null) {
							// Add chained parameters for any
							List<Resourcemetadata> rProblemChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "problem", 0, problem.getReference(), null);
							resourcemetadataList.addAll(rProblemChain);
						}
					}
				}
			}

			// status : token
			if (clinicalImpression.hasStatus() && clinicalImpression.getStatus() != null) {
				Resourcemetadata rStatus = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", clinicalImpression.getStatus().toCode(), clinicalImpression.getStatus().getSystem());
				resourcemetadataList.add(rStatus);
			}

			// supporting-info : reference
			if (clinicalImpression.hasSupportingInfo()) {

				String supportingInfoReference = null;
				Resourcemetadata rSupportingInfo = null;
				List<Resourcemetadata> rSupportingInfoChain = null;
				for (Reference supportingInfo : clinicalImpression.getSupportingInfo()) {

					if (supportingInfo.hasReference()) {
						supportingInfoReference = generateFullLocalReference(supportingInfo.getReference(), baseUrl);

						rSupportingInfo = generateResourcemetadata(resource, chainedResource, chainedParameter+"supporting-info", supportingInfoReference);
						resourcemetadataList.add(rSupportingInfo);

						if (chainedResource == null) {
							// Add chained parameters for any
							rSupportingInfoChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "supporting-info", 0, supportingInfo.getReference(), null);
							resourcemetadataList.addAll(rSupportingInfoChain);
						}
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
