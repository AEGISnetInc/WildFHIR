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
import java.io.IOException;
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
        Resourcemetadata rMetadata = null;
        List<Resourcemetadata> rMetadataChain = null;

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

			// Add Resource common parameters
            rMetadataChain = this.generateResourcemetadataTagList(resource, medicationRequest, chainedParameter);
			resourcemetadataList.addAll(rMetadataChain);

			// authoredon : date
			if (medicationRequest.hasAuthoredOn()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"authoredon", utcDateUtil.formatDate(medicationRequest.getAuthoredOn(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(medicationRequest.getAuthoredOn(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rMetadata);
			}

			// category : token
			if (medicationRequest.hasCategory()) {
				for (CodeableConcept category : medicationRequest.getCategory()) {

					if (category.hasCoding()) {
						for (Coding code : category.getCoding()) {
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"category", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rMetadata);
						}
					}
				}
			}

			// code : token
			if (medicationRequest.hasMedicationCodeableConcept() && medicationRequest.getMedicationCodeableConcept().hasCoding()) {

				for (Coding code : medicationRequest.getMedicationCodeableConcept().getCoding()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"code", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rMetadata);
				}
			}

			// date : date
			if (medicationRequest.hasDosageInstruction()) {
				for (Dosage dosageInstruction : medicationRequest.getDosageInstruction()) {

					if (dosageInstruction.hasTiming() && dosageInstruction.getTiming().hasEvent()) {
						for (DateTimeType event : dosageInstruction.getTiming().getEvent()) {

							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"date", utcDateUtil.formatDate(event.getValue(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(event.getValue(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
							resourcemetadataList.add(rMetadata);
						}
					}
				}
			}

			// encounter : reference
			if (medicationRequest.hasEncounter()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "encounter", 0, medicationRequest.getEncounter(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// identifier : token
			if (medicationRequest.hasIdentifier()) {

				for (Identifier identifier : medicationRequest.getIdentifier()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rMetadata);
				}
			}

			// intended-dispenser : reference
			if (medicationRequest.hasDispenseRequest() && medicationRequest.getDispenseRequest().hasPerformer()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "intended-dispenser", 0, medicationRequest.getDispenseRequest().getPerformer(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// intended-performer : reference
			if (medicationRequest.hasPerformer()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "intended-performer", 0, medicationRequest.getPerformer(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// intended-performertype : token
			if (medicationRequest.hasPerformerType() && medicationRequest.getPerformerType().hasCoding()) {

				for (Coding code : medicationRequest.getPerformerType().getCoding()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"intended-performertype", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rMetadata);
				}
			}

			// intent : token
			if (medicationRequest.hasIntent() && medicationRequest.getIntent() != null) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"intent", medicationRequest.getIntent().toCode(), medicationRequest.getIntent().getSystem());
				resourcemetadataList.add(rMetadata);
			}

			// medication : reference
			if (medicationRequest.hasMedicationReference()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "medication", 0, medicationRequest.getMedicationReference(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// subject : reference
			if (medicationRequest.hasSubject()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "subject", 0, medicationRequest.getSubject(), null);
				resourcemetadataList.addAll(rMetadataChain);

				// patient : reference
				if ((medicationRequest.getSubject().hasReference() && medicationRequest.getSubject().getReference().indexOf("Patient") >= 0)
						|| (medicationRequest.getSubject().hasType() && medicationRequest.getSubject().getType().equals("Patient"))) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "patient", 0, medicationRequest.getSubject(), null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// priority : token
			if (medicationRequest.hasPriority() && medicationRequest.getPriority() != null) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"priority", medicationRequest.getPriority().toCode(), medicationRequest.getPriority().getSystem());
				resourcemetadataList.add(rMetadata);
			}

			// requester : reference
			if (medicationRequest.hasRequester()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "requester", 0, medicationRequest.getRequester(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// status : token
			if (medicationRequest.hasStatus() && medicationRequest.getStatus() != null) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", medicationRequest.getStatus().toCode(), medicationRequest.getStatus().getSystem());
				resourcemetadataList.add(rMetadata);
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		} finally {
	        rMetadata = null;
	        rMetadataChain = null;
            if (iMedicationRequest != null) {
                try {
                	iMedicationRequest.close();
                } catch (IOException ioe) {
                	ioe.printStackTrace();
                }
            }
		}

		return resourcemetadataList;
	}

}
