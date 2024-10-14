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
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Dosage;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.MedicationRequest;

import net.aegis.fhir.model.Resource;
import net.aegis.fhir.model.Resourcemetadata;
import net.aegis.fhir.service.ResourceService;
import net.aegis.fhir.service.util.ServicesUtil;
import net.aegis.fhir.service.util.UTCDateUtil;

/**
 * @author richard.ettema
 *
 */
public class ResourcemetadataMedicationRequest extends ResourcemetadataProxy {

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
        ByteArrayInputStream iMedicationRequest = null;

		try {
            // Extract and convert the resource contents to a MedicationRequest object
			if (chainedResource != null) {
				iMedicationRequest = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iMedicationRequest = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
            MedicationRequest medicationRequest = (MedicationRequest) xmlP.parse(iMedicationRequest);
            iMedicationRequest.close();

			/*
             * Create new Resourcemetadata objects for each MedicationRequest metadata value and add to the resourcemetadataList
			 */

			// Add any passed in tags
			List<Resourcemetadata> tagMetadataList = this.generateResourcemetadataTagList(resource, medicationRequest, chainedParameter);
			resourcemetadataList.addAll(tagMetadataList);

			// _id : token
			if (medicationRequest.getId() != null) {
				Resourcemetadata _id = generateResourcemetadata(resource, chainedResource, chainedParameter+"_id", medicationRequest.getId());
				resourcemetadataList.add(_id);
			}

			// _language : token
			if (medicationRequest.getLanguage() != null) {
				Resourcemetadata _language = generateResourcemetadata(resource, chainedResource, chainedParameter+"_language", medicationRequest.getLanguage());
				resourcemetadataList.add(_language);
			}

			// _lastUpdated : date
			if (medicationRequest.getMeta() != null && medicationRequest.getMeta().getLastUpdated() != null) {
				Resourcemetadata _lastUpdated = generateResourcemetadata(resource, chainedResource, chainedParameter+"_lastUpdated", utcDateUtil.formatDate(medicationRequest.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(medicationRequest.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(_lastUpdated);
			}

			// authoredon : date
			if (medicationRequest.hasAuthoredOn()) {
				Resourcemetadata rAuthoredOn = generateResourcemetadata(resource, chainedResource, chainedParameter+"authoredon", utcDateUtil.formatDate(medicationRequest.getAuthoredOn(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(medicationRequest.getAuthoredOn(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rAuthoredOn);
			}

			// category : token
			if (medicationRequest.hasCategory()) {

				Resourcemetadata rCode = null;
				for (CodeableConcept category : medicationRequest.getCategory()) {

					if (category.hasCoding()) {
						for (Coding code : category.getCoding()) {
							rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"category", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rCode);
						}
					}
				}
			}

			// code : token
			if (medicationRequest.hasMedicationCodeableConcept() && medicationRequest.getMedicationCodeableConcept().hasCoding()) {

				Resourcemetadata rCode = null;
				for (Coding code : medicationRequest.getMedicationCodeableConcept().getCoding()) {
					rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"code", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rCode);
				}
			}

			// date : date
			if (medicationRequest.hasDosageInstruction()) {

				Resourcemetadata rDate = null;
				for (Dosage dosageInstruction : medicationRequest.getDosageInstruction()) {

					if (dosageInstruction.hasTiming() && dosageInstruction.getTiming().hasEvent()) {

						for (DateTimeType event : dosageInstruction.getTiming().getEvent()) {

							rDate = generateResourcemetadata(resource, chainedResource, chainedParameter+"date", utcDateUtil.formatDate(event.getValue(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(event.getValue(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
							resourcemetadataList.add(rDate);
						}
					}
				}
			}

			// encounter : reference
			if (medicationRequest.hasEncounter() && medicationRequest.getEncounter().hasReference()) {
				String contextString = generateFullLocalReference(medicationRequest.getEncounter().getReference(), baseUrl);

				Resourcemetadata rEncounter = generateResourcemetadata(resource, chainedResource, chainedParameter+"encounter", contextString);
				resourcemetadataList.add(rEncounter);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rEncounterChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "encounter", 0, medicationRequest.getEncounter().getReference(), null);
					resourcemetadataList.addAll(rEncounterChain);
				}
			}

			// identifier : token
			if (medicationRequest.hasIdentifier()) {

				for (Identifier identifier : medicationRequest.getIdentifier()) {

					Resourcemetadata rIdentifier = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rIdentifier);
				}
			}

			// intended-dispenser : reference
			if (medicationRequest.hasDispenseRequest() && medicationRequest.getDispenseRequest().hasPerformer() && medicationRequest.getDispenseRequest().getPerformer().hasReference()) {
				Resourcemetadata rIntendedDispenser = generateResourcemetadata(resource, chainedResource, chainedParameter+"intended-dispenser", generateFullLocalReference(medicationRequest.getDispenseRequest().getPerformer().getReference(), baseUrl));
				resourcemetadataList.add(rIntendedDispenser);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rIntendedDispenserChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "intended-dispenser", 0, medicationRequest.getDispenseRequest().getPerformer().getReference(), null);
					resourcemetadataList.addAll(rIntendedDispenserChain);
				}
			}

			// intended-performer : reference
			if (medicationRequest.hasPerformer() && medicationRequest.getPerformer().hasReference()) {
				String performerString = generateFullLocalReference(medicationRequest.getPerformer().getReference(), baseUrl);

				Resourcemetadata rPerformer = generateResourcemetadata(resource, chainedResource, chainedParameter+"intended-performer", performerString);
				resourcemetadataList.add(rPerformer);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rPerformerChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "intended-performer.", 0, medicationRequest.getPerformer().getReference(), null);
					resourcemetadataList.addAll(rPerformerChain);
				}
			}

			// intended-performertype : token
			if (medicationRequest.hasPerformerType() && medicationRequest.getPerformerType().hasCoding()) {

				Resourcemetadata rCode = null;
				for (Coding code : medicationRequest.getPerformerType().getCoding()) {
					rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"intended-performertype", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rCode);
				}
			}

			// intent : token
			if (medicationRequest.hasIntent() && medicationRequest.getIntent() != null) {
				Resourcemetadata rIntent = generateResourcemetadata(resource, chainedResource, chainedParameter+"intent", medicationRequest.getIntent().toCode(), medicationRequest.getIntent().getSystem());
				resourcemetadataList.add(rIntent);
			}

			// medication : reference
			if (medicationRequest.hasMedicationReference() && medicationRequest.getMedicationReference().hasReference()) {
				Resourcemetadata rMedicationReference = generateResourcemetadata(resource, chainedResource, chainedParameter+"medication", generateFullLocalReference(medicationRequest.getMedicationReference().getReference(), baseUrl));
				resourcemetadataList.add(rMedicationReference);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rMedicationChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "medication", 0, medicationRequest.getMedicationReference().getReference(), null);
					resourcemetadataList.addAll(rMedicationChain);
				}
			}

			// patient : reference
			// subject : reference
			if (medicationRequest.hasSubject() && medicationRequest.getSubject().hasReference()) {
				String subjectReference = generateFullLocalReference(medicationRequest.getSubject().getReference(), baseUrl);

				Resourcemetadata rSubject = generateResourcemetadata(resource, chainedResource, chainedParameter+"subject", subjectReference);
				resourcemetadataList.add(rSubject);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rSubjectChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "subject", 0, medicationRequest.getSubject().getReference(), null);
					resourcemetadataList.addAll(rSubjectChain);
				}

				if (subjectReference.indexOf("Patient") >= 0) {
					Resourcemetadata rPatient = generateResourcemetadata(resource, chainedResource, chainedParameter+"patient", subjectReference);
					resourcemetadataList.add(rPatient);

					if (chainedResource == null) {
						// Add chained parameters
						List<Resourcemetadata> rPatientChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "patient", 0, medicationRequest.getSubject().getReference(), null);
						resourcemetadataList.addAll(rPatientChain);
					}
				}
			}

			// priority : token
			if (medicationRequest.hasPriority() && medicationRequest.getPriority() != null) {
				Resourcemetadata rPriority = generateResourcemetadata(resource, chainedResource, chainedParameter+"priority", medicationRequest.getPriority().toCode(), medicationRequest.getPriority().getSystem());
				resourcemetadataList.add(rPriority);
			}

			// requester : reference
			if (medicationRequest.hasRequester() && medicationRequest.getRequester().hasReference()) {
				Resourcemetadata rRequester = generateResourcemetadata(resource, chainedResource, chainedParameter+"requester", generateFullLocalReference(medicationRequest.getRequester().getReference(), baseUrl));
				resourcemetadataList.add(rRequester);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rRequesterChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "requester", 0, medicationRequest.getRequester().getReference(), null);
					resourcemetadataList.addAll(rRequesterChain);
				}
			}

			// status : token
			if (medicationRequest.hasStatus() && medicationRequest.getStatus() != null) {
				Resourcemetadata rStatus = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", medicationRequest.getStatus().toCode(), medicationRequest.getStatus().getSystem());
				resourcemetadataList.add(rStatus);
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		}

		return resourcemetadataList;
	}

}
