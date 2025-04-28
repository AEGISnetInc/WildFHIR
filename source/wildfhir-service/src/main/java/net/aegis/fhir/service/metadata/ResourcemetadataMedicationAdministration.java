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
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.MedicationAdministration;
import org.hl7.fhir.r4.model.MedicationAdministration.MedicationAdministrationPerformerComponent;
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
public class ResourcemetadataMedicationAdministration extends ResourcemetadataProxy {

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
        ByteArrayInputStream iMedicationAdministration = null;
        Resourcemetadata rMetadata = null;
        List<Resourcemetadata> rMetadataChain = null;

		try {
            // Extract and convert the resource contents to a MedicationAdministration object
			if (chainedResource != null) {
				iMedicationAdministration = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iMedicationAdministration = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
            MedicationAdministration medicationAdministration = (MedicationAdministration) xmlP.parse(iMedicationAdministration);
            iMedicationAdministration.close();

			/*
             * Create new Resourcemetadata objects for each MedicationAdministration metadata value and add to the resourcemetadataList
			 */

			// Add Resource common parameters
            rMetadataChain = this.generateResourcemetadataTagList(resource, medicationAdministration, chainedParameter);
			resourcemetadataList.addAll(rMetadataChain);

			// code : token
			if (medicationAdministration.hasMedicationCodeableConcept() && medicationAdministration.getMedicationCodeableConcept().hasCoding()) {

				for (Coding code : medicationAdministration.getMedicationCodeableConcept().getCoding()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"code", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rMetadata);
				}
			}

			// context : reference
			if (medicationAdministration.hasContext()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "context", 0, medicationAdministration.getContext(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// device : reference
			if (medicationAdministration.hasDevice()) {

				for (Reference device : medicationAdministration.getDevice()) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "device", 0, device, null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// effective-time : datetime(period)
			if (medicationAdministration.hasEffective()) {

				if (medicationAdministration.hasEffectiveDateTimeType()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"effective-time", utcDateUtil.formatDate(medicationAdministration.getEffectiveDateTimeType().getValue(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(medicationAdministration.getEffectiveDateTimeType().getValue(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
					resourcemetadataList.add(rMetadata);
				}
				if (medicationAdministration.hasEffectivePeriod()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"effective-time", utcDateUtil.formatDate(medicationAdministration.getEffectivePeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(medicationAdministration.getEffectivePeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(medicationAdministration.getEffectivePeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), utcDateUtil.formatDate(medicationAdministration.getEffectivePeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), "PERIOD");
					resourcemetadataList.add(rMetadata);
				}
			}

			// identifier : token
			if (medicationAdministration.hasIdentifier()) {

				for (Identifier identifier : medicationAdministration.getIdentifier()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rMetadata);
				}
			}

			// medication : reference
			if (medicationAdministration.hasMedicationReference()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "medication", 0, medicationAdministration.getMedicationReference(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// subject : reference
			if (medicationAdministration.hasSubject()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "subject", 0, medicationAdministration.getSubject(), null);
				resourcemetadataList.addAll(rMetadataChain);

				// patient : reference
				if ((medicationAdministration.getSubject().hasReference() && medicationAdministration.getSubject().getReference().indexOf("Patient") >= 0)
						|| (medicationAdministration.getSubject().hasType() && medicationAdministration.getSubject().getType().equals("Patient"))) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "patient", 0, medicationAdministration.getSubject(), null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// performer : reference
			if (medicationAdministration.hasPerformer()) {

				for (MedicationAdministrationPerformerComponent performer : medicationAdministration.getPerformer()) {

					if (performer.hasActor()) {
						rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "performer", 0, performer.getActor(), null);
						resourcemetadataList.addAll(rMetadataChain);
					}
				}
			}

			// reason-given : token
			if (medicationAdministration.hasReasonCode()) {
				for (CodeableConcept reasonGiven : medicationAdministration.getReasonCode()) {

					if (reasonGiven.hasCoding()) {
						for (Coding code : reasonGiven.getCoding()) {
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"reason-given", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rMetadata);
						}
					}
				}
			}

			// reason-not-given : token
			if (medicationAdministration.hasStatusReason()) {
				for (CodeableConcept reasonNotGiven : medicationAdministration.getStatusReason()) {

					if (reasonNotGiven.hasCoding()) {
						for (Coding code : reasonNotGiven.getCoding()) {
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"reason-not-given", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rMetadata);
						}
					}
				}
			}

			// request : reference
			if (medicationAdministration.hasRequest()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "request", 0, medicationAdministration.getRequest(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// status : token
			if (medicationAdministration.hasStatus() && medicationAdministration.getStatus() != null) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", medicationAdministration.getStatus().toCode(), medicationAdministration.getStatus().getSystem());
				resourcemetadataList.add(rMetadata);
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		} finally {
	        rMetadata = null;
	        rMetadataChain = null;
            if (iMedicationAdministration != null) {
                try {
                	iMedicationAdministration.close();
                } catch (IOException ioe) {
                	ioe.printStackTrace();
                }
            }
		}

		return resourcemetadataList;
	}

}
