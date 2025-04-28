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
        ByteArrayInputStream iMedicationDispense = null;
        Resourcemetadata rMetadata = null;
        List<Resourcemetadata> rMetadataChain = null;

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

			// Add Resource common parameters
            rMetadataChain = this.generateResourcemetadataTagList(resource, medicationDispense, chainedParameter);
			resourcemetadataList.addAll(rMetadataChain);

			// Nictiz Search Parameter
			// category : token
			if (medicationDispense.hasCategory() && medicationDispense.getCategory().hasCoding()) {

				for (Coding code : medicationDispense.getCategory().getCoding()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"category", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rMetadata);
				}
			}

			// code : token
			if (medicationDispense.hasMedicationCodeableConcept() && medicationDispense.getMedicationCodeableConcept().hasCoding()) {

				for (Coding code : medicationDispense.getMedicationCodeableConcept().getCoding()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"code", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rMetadata);
				}
			}

			// context : reference
			if (medicationDispense.hasContext()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "context", 0, medicationDispense.getContext(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// destination : reference
			if (medicationDispense.hasDestination()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "destination", 0, medicationDispense.getDestination(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// identifier : token
			if (medicationDispense.hasIdentifier()) {

				for (Identifier identifier : medicationDispense.getIdentifier()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rMetadata);
				}
			}

			// medication : reference
			if (medicationDispense.hasMedicationReference()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "medication", 0, medicationDispense.getMedicationReference(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// subject : reference
			if (medicationDispense.hasSubject()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "subject", 0, medicationDispense.getSubject(), null);
				resourcemetadataList.addAll(rMetadataChain);

				// patient : reference
				if ((medicationDispense.getSubject().hasReference() && medicationDispense.getSubject().getReference().indexOf("Patient") >= 0)
						|| (medicationDispense.getSubject().hasType() && medicationDispense.getSubject().getType().equals("Patient"))) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "patient", 0, medicationDispense.getSubject(), null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// performer : reference
			if (medicationDispense.hasPerformer()) {
				for (MedicationDispensePerformerComponent performer : medicationDispense.getPerformer()) {

					if (performer.hasActor()) {
						rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "performer", 0, performer.getActor(), null);
						resourcemetadataList.addAll(rMetadataChain);
					}
				}
			}

			// prescription : reference
			if (medicationDispense.hasAuthorizingPrescription()) {

				for (Reference prescription : medicationDispense.getAuthorizingPrescription()) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "prescription", 0, prescription, null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// receiver : reference
			if (medicationDispense.hasReceiver()) {

				for (Reference receiver : medicationDispense.getReceiver()) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "receiver", 0, receiver, null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// responsibleparty : reference
			if (medicationDispense.hasSubstitution() && medicationDispense.getSubstitution().hasResponsibleParty()) {

				for (Reference responsibleparty : medicationDispense.getSubstitution().getResponsibleParty()) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "responsibleparty", 0, responsibleparty, null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// status : token
			if (medicationDispense.hasStatus() && medicationDispense.getStatus() != null) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", medicationDispense.getStatus().toCode(), medicationDispense.getStatus().getSystem());
				resourcemetadataList.add(rMetadata);
			}

			// type : token
			if (medicationDispense.hasType()) {

				for (Coding type : medicationDispense.getType().getCoding()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"type", type.getCode(), type.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(type));
					resourcemetadataList.add(rMetadata);
				}
			}

			// whenhandedover : datetime
			if (medicationDispense.hasWhenHandedOver()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"whenhandedover", utcDateUtil.formatDate(medicationDispense.getWhenHandedOver(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(medicationDispense.getWhenHandedOver(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rMetadata);
			}

			// whenprepared : datetime
			if (medicationDispense.hasWhenPrepared()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"whenprepared", utcDateUtil.formatDate(medicationDispense.getWhenPrepared(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(medicationDispense.getWhenPrepared(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rMetadata);
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		} finally {
	        rMetadata = null;
	        rMetadataChain = null;
            if (iMedicationDispense != null) {
                try {
                	iMedicationDispense.close();
                } catch (IOException ioe) {
                	ioe.printStackTrace();
                }
            }
		}

		return resourcemetadataList;
	}

}
