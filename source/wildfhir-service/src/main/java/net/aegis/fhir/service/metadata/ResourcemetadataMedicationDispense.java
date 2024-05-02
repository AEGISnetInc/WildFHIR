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
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.MedicationDispense;
import org.hl7.fhir.r4.model.MedicationDispense.MedicationDispensePerformerComponent;
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
public class ResourcemetadataMedicationDispense extends ResourcemetadataProxy {

	/* (non-Javadoc)
	 * @see net.aegis.fhir.service.metadata.ResourcemetadataProxy#generateAllForResource(net.aegis.fhir.model.Resource, java.lang.String, net.aegis.fhir.service.ResourceService)
	 */
	@Override
	public List<Resourcemetadata> generateAllForResource(Resource resource, String baseUrl, ResourceService resourceService) throws Exception {
		return generateAllForResource(resource, baseUrl, resourceService, null, null, 0);
	}

	/* (non-Javadoc)
	 * @see net.aegis.fhir.service.metadata.ResourcemetadataProxy#generateAllForResource(net.aegis.fhir.model.Resource, java.lang.String, net.aegis.fhir.service.ResourceService, net.aegis.fhir.model.Resource, java.lang.String, int)
	 */
	@Override
	public List<Resourcemetadata> generateAllForResource(Resource resource, String baseUrl, ResourceService resourceService, Resource chainedResource, String chainedParameter, int chainedIndex) throws Exception {

		if (StringUtils.isEmpty(chainedParameter)) {
			chainedParameter = "";
		}

		List<Resourcemetadata> resourcemetadataList = new ArrayList<Resourcemetadata>();
        ByteArrayInputStream iMedicationDispense = null;

		try {
            // Extract and convert the resource contents to a MedicationDispense object
			if (chainedResource != null) {
				iMedicationDispense = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iMedicationDispense = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
            MedicationDispense medicationDispense = (MedicationDispense) xmlP.parse(iMedicationDispense);
            iMedicationDispense.close();

			/*
             * Create new Resourcemetadata objects for each MedicationDispense metadata value and add to the resourcemetadataList
			 */

			// Add any passed in tags
			List<Resourcemetadata> tagMetadataList = this.generateResourcemetadataTagList(resource, medicationDispense, chainedParameter);
			resourcemetadataList.addAll(tagMetadataList);

			// _id : token
			Resourcemetadata _id = generateResourcemetadata(resource, chainedResource, chainedParameter+"_id", medicationDispense.getId());
			resourcemetadataList.add(_id);

			// _language : token
			if (medicationDispense.getLanguage() != null) {
				Resourcemetadata _language = generateResourcemetadata(resource, chainedResource, chainedParameter+"_language", medicationDispense.getLanguage());
				resourcemetadataList.add(_language);
			}

			// _lastUpdated : date
			if (medicationDispense.getMeta() != null && medicationDispense.getMeta().getLastUpdated() != null) {
				Resourcemetadata _lastUpdated = generateResourcemetadata(resource, chainedResource, chainedParameter+"_lastUpdated", utcDateUtil.formatDate(medicationDispense.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(medicationDispense.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(_lastUpdated);
			}

			// code : token
			if (medicationDispense.hasMedicationCodeableConcept() && medicationDispense.getMedicationCodeableConcept().hasCoding()) {

				Resourcemetadata rCode = null;
				for (Coding code : medicationDispense.getMedicationCodeableConcept().getCoding()) {
					rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"code", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rCode);
				}
			}

			// context : reference
			if (medicationDispense.hasContext() && medicationDispense.getContext().hasReference()) {
				String contextString = generateFullLocalReference(medicationDispense.getContext().getReference(), baseUrl);

				Resourcemetadata rContext = generateResourcemetadata(resource, chainedResource, chainedParameter+"context", contextString);
				resourcemetadataList.add(rContext);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rContextChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "context", 0, medicationDispense.getContext().getReference());
					resourcemetadataList.addAll(rContextChain);
				}
			}

			// destination : reference
			if (medicationDispense.hasDestination() && medicationDispense.getDestination().hasReference()) {
				Resourcemetadata rDestination = generateResourcemetadata(resource, chainedResource, chainedParameter+"destination", generateFullLocalReference(medicationDispense.getDestination().getReference(), baseUrl));
				resourcemetadataList.add(rDestination);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rDestinationChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "destination", 0, medicationDispense.getDestination().getReference());
					resourcemetadataList.addAll(rDestinationChain);
				}
			}

			// identifier : token
			if (medicationDispense.hasIdentifier()) {

				for (Identifier identifier : medicationDispense.getIdentifier()) {

					Resourcemetadata rIdentifier = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rIdentifier);
				}
			}

			// medication : reference
			if (medicationDispense.hasMedicationReference() && medicationDispense.getMedicationReference().hasReference()) {
				Resourcemetadata rMedicationReference = generateResourcemetadata(resource, chainedResource, chainedParameter+"medication", generateFullLocalReference(medicationDispense.getMedicationReference().getReference(), baseUrl));
				resourcemetadataList.add(rMedicationReference);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rMedicationChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "medication", 0, medicationDispense.getMedicationReference().getReference());
					resourcemetadataList.addAll(rMedicationChain);
				}
			}

			// patient : reference
			// subject : reference
			if (medicationDispense.hasSubject() && medicationDispense.getSubject().hasReference()) {
				String subjectReference = generateFullLocalReference(medicationDispense.getSubject().getReference(), baseUrl);

				Resourcemetadata rSubject = generateResourcemetadata(resource, chainedResource, chainedParameter+"subject", subjectReference);
				resourcemetadataList.add(rSubject);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rSubjectChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "subject", 0, medicationDispense.getSubject().getReference());
					resourcemetadataList.addAll(rSubjectChain);
				}

				if (subjectReference.indexOf("Patient") >= 0) {
					Resourcemetadata rPatient = generateResourcemetadata(resource, chainedResource, chainedParameter+"patient", subjectReference);
					resourcemetadataList.add(rPatient);

					if (chainedResource == null) {
						// Add chained parameters
						List<Resourcemetadata> rPatientChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "patient", 0, medicationDispense.getSubject().getReference());
						resourcemetadataList.addAll(rPatientChain);
					}
				}
			}

			// performer : reference
			if (medicationDispense.hasPerformer()) {

				for (MedicationDispensePerformerComponent performer : medicationDispense.getPerformer()) {

					if (performer.hasActor() && performer.getActor().hasReference()) {
						Resourcemetadata rRequestDetail = generateResourcemetadata(resource, chainedResource, chainedParameter+"performer", generateFullLocalReference(performer.getActor().getReference(), baseUrl));
						resourcemetadataList.add(rRequestDetail);

						if (chainedResource == null) {
							// Add chained parameters for any
							List<Resourcemetadata> rPerformerChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "performer", 0, performer.getActor().getReference());
							resourcemetadataList.addAll(rPerformerChain);
						}
					}
				}
			}

			// prescription : reference
			if (medicationDispense.hasAuthorizingPrescription()) {

				List<Resourcemetadata> rPrescriptionChain = null;
				for (Reference prescription : medicationDispense.getAuthorizingPrescription()) {

					if (prescription.hasReference()) {
						Resourcemetadata rPrescription = generateResourcemetadata(resource, chainedResource, chainedParameter+"prescription", generateFullLocalReference(prescription.getReference(), baseUrl));
						resourcemetadataList.add(rPrescription);

						if (chainedResource == null) {
							// Add chained parameters
							rPrescriptionChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "prescription", 0, prescription.getReference());
							resourcemetadataList.addAll(rPrescriptionChain);
						}
					}
				}
			}

			// receiver : reference
			if (medicationDispense.hasReceiver()) {

				List<Resourcemetadata> rReceiverChain = null;
				for (Reference receiver : medicationDispense.getReceiver()) {

					if (receiver.hasReference()) {
						Resourcemetadata rReceiver = generateResourcemetadata(resource, chainedResource, chainedParameter+"receiver", generateFullLocalReference(receiver.getReference(), baseUrl));
						resourcemetadataList.add(rReceiver);

						if (chainedResource == null) {
							// Add chained parameters for any
							rReceiverChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "receiver", 0, receiver.getReference());
							resourcemetadataList.addAll(rReceiverChain);
						}
					}
				}
			}

			// responsibleparty : reference
			if (medicationDispense.hasSubstitution() && medicationDispense.getSubstitution().hasResponsibleParty()) {

				List<Resourcemetadata> rResponsiblePartyChain = null;
				for (Reference responsibleparty : medicationDispense.getSubstitution().getResponsibleParty()) {

					if (responsibleparty.hasReference()) {
						Resourcemetadata rResponsibleParty = generateResourcemetadata(resource, chainedResource, chainedParameter+"responsibleparty", generateFullLocalReference(responsibleparty.getReference(), baseUrl));
						resourcemetadataList.add(rResponsibleParty);

						if (chainedResource == null) {
							// Add chained parameters
							rResponsiblePartyChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "responsibleparty", 0, responsibleparty.getReference());
							resourcemetadataList.addAll(rResponsiblePartyChain);
						}
					}
				}
			}

			// status : token
			if (medicationDispense.hasStatus() && medicationDispense.getStatus() != null) {
				Resourcemetadata rStatus = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", medicationDispense.getStatus().toCode(), medicationDispense.getStatus().getSystem());
				resourcemetadataList.add(rStatus);
			}

			// type : token
			if (medicationDispense.hasType()) {

				for (Coding type : medicationDispense.getType().getCoding()) {

					Resourcemetadata rType = generateResourcemetadata(resource, chainedResource, chainedParameter+"type", type.getCode(), type.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(type));
					resourcemetadataList.add(rType);
				}
			}

			// whenhandedover : datetime
			if (medicationDispense.hasWhenHandedOver()) {
				Resourcemetadata rWhenHandedOver = generateResourcemetadata(resource, chainedResource, chainedParameter+"whenhandedover", utcDateUtil.formatDate(medicationDispense.getWhenHandedOver(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(medicationDispense.getWhenHandedOver(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rWhenHandedOver);
			}

			// whenprepared : datetime
			if (medicationDispense.hasWhenPrepared()) {
				Resourcemetadata rWhenPrepared = generateResourcemetadata(resource, chainedResource, chainedParameter+"whenprepared", utcDateUtil.formatDate(medicationDispense.getWhenPrepared(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(medicationDispense.getWhenPrepared(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rWhenPrepared);
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		}

		return resourcemetadataList;
	}

}
