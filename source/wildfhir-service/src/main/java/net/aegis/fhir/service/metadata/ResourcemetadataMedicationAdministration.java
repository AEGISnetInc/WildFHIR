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
        ByteArrayInputStream iMedicationAdministration = null;

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

			// Add any passed in tags
			List<Resourcemetadata> tagMetadataList = this.generateResourcemetadataTagList(resource, medicationAdministration, chainedParameter);
			resourcemetadataList.addAll(tagMetadataList);

			// _id : token
			Resourcemetadata _id = generateResourcemetadata(resource, chainedResource, chainedParameter+"_id", medicationAdministration.getId());
			resourcemetadataList.add(_id);

			// _language : token
			if (medicationAdministration.getLanguage() != null) {
				Resourcemetadata _language = generateResourcemetadata(resource, chainedResource, chainedParameter+"_language", medicationAdministration.getLanguage());
				resourcemetadataList.add(_language);
			}

			// _lastUpdated : date
			if (medicationAdministration.getMeta() != null && medicationAdministration.getMeta().getLastUpdated() != null) {
				Resourcemetadata _lastUpdated = generateResourcemetadata(resource, chainedResource, chainedParameter+"_lastUpdated", utcDateUtil.formatDate(medicationAdministration.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(medicationAdministration.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(_lastUpdated);
			}

			// code : token
			if (medicationAdministration.hasMedicationCodeableConcept() && medicationAdministration.getMedicationCodeableConcept().hasCoding()) {

				Resourcemetadata rCode = null;
				for (Coding code : medicationAdministration.getMedicationCodeableConcept().getCoding()) {
					rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"code", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rCode);
				}
			}

			// context : reference
			if (medicationAdministration.hasContext() && medicationAdministration.getContext().hasReference()) {
				String contextString = generateFullLocalReference(medicationAdministration.getContext().getReference(), baseUrl);

				Resourcemetadata rContext = generateResourcemetadata(resource, chainedResource, chainedParameter+"context", contextString);
				resourcemetadataList.add(rContext);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rContextChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "context", 0, medicationAdministration.getContext().getReference());
					resourcemetadataList.addAll(rContextChain);
				}
			}

			// device : reference
			if (medicationAdministration.hasDevice()) {

				List<Resourcemetadata> rDeviceChain = null;
				for (Reference device : medicationAdministration.getDevice()) {

					if (device.hasReference()) {
						Resourcemetadata rDevice = generateResourcemetadata(resource, chainedResource, chainedParameter+"device", generateFullLocalReference(device.getReference(), baseUrl));
						resourcemetadataList.add(rDevice);

						if (chainedResource == null) {
							// Add chained parameters
							rDeviceChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "device", 0, device.getReference());
							resourcemetadataList.addAll(rDeviceChain);
						}
					}
				}
			}

			// effective-time : datetime(period)
			if (medicationAdministration.hasEffective()) {

				if (medicationAdministration.hasEffectiveDateTimeType()) {
					Resourcemetadata rEffectiveTime = generateResourcemetadata(resource, chainedResource, chainedParameter+"effective-time", utcDateUtil.formatDate(medicationAdministration.getEffectiveDateTimeType().getValue(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(medicationAdministration.getEffectiveDateTimeType().getValue(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
					resourcemetadataList.add(rEffectiveTime);
				}
				if (medicationAdministration.hasEffectivePeriod()) {
					Resourcemetadata rEffectiveTime = generateResourcemetadata(resource, chainedResource, chainedParameter+"effective-time", utcDateUtil.formatDate(medicationAdministration.getEffectivePeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(medicationAdministration.getEffectivePeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(medicationAdministration.getEffectivePeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), utcDateUtil.formatDate(medicationAdministration.getEffectivePeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), "PERIOD");
					resourcemetadataList.add(rEffectiveTime);
				}
			}

			// identifier : token
			if (medicationAdministration.hasIdentifier()) {

				for (Identifier identifier : medicationAdministration.getIdentifier()) {

					Resourcemetadata rIdentifier = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rIdentifier);
				}
			}

			// medication : reference
			if (medicationAdministration.hasMedicationReference() && medicationAdministration.getMedicationReference().hasReference()) {
				Resourcemetadata rMedicationReference = generateResourcemetadata(resource, chainedResource, chainedParameter+"medication", generateFullLocalReference(medicationAdministration.getMedicationReference().getReference(), baseUrl));
				resourcemetadataList.add(rMedicationReference);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rMedicationChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "medication", 0, medicationAdministration.getMedicationReference().getReference());
					resourcemetadataList.addAll(rMedicationChain);
				}
			}

			// patient : reference
			// subject : reference
			if (medicationAdministration.hasSubject() && medicationAdministration.getSubject().hasReference()) {
				String subjectReference = generateFullLocalReference(medicationAdministration.getSubject().getReference(), baseUrl);

				Resourcemetadata rSubject = generateResourcemetadata(resource, chainedResource, chainedParameter+"subject", subjectReference);
				resourcemetadataList.add(rSubject);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rSubjectChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "subject", 0, medicationAdministration.getSubject().getReference());
					resourcemetadataList.addAll(rSubjectChain);
				}

				if (subjectReference.indexOf("Patient") >= 0) {
					Resourcemetadata rPatient = generateResourcemetadata(resource, chainedResource, chainedParameter+"patient", subjectReference);
					resourcemetadataList.add(rPatient);

					if (chainedResource == null) {
						// Add chained parameters
						List<Resourcemetadata> rPatientChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "patient", 0, medicationAdministration.getSubject().getReference());
						resourcemetadataList.addAll(rPatientChain);
					}
				}
			}

			// performer : reference
			if (medicationAdministration.hasPerformer()) {

				for (MedicationAdministrationPerformerComponent performer : medicationAdministration.getPerformer()) {

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

			// reason-given : token
			if (medicationAdministration.hasReasonCode()) {

				Resourcemetadata rCode = null;
				for (CodeableConcept reasonGiven : medicationAdministration.getReasonCode()) {

					if (reasonGiven.hasCoding()) {
						for (Coding code : reasonGiven.getCoding()) {
							rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"reason-given", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rCode);
						}
					}
				}
			}

			// reason-not-given : token
			if (medicationAdministration.hasStatusReason()) {

				Resourcemetadata rCode = null;
				for (CodeableConcept reasonNotGiven : medicationAdministration.getStatusReason()) {

					if (reasonNotGiven.hasCoding()) {
						for (Coding code : reasonNotGiven.getCoding()) {
							rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"reason-not-given", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rCode);
						}
					}
				}
			}

			// request : reference
			if (medicationAdministration.hasRequest() && medicationAdministration.getRequest().hasReference()) {
				Resourcemetadata rRequest = generateResourcemetadata(resource, chainedResource, chainedParameter+"request", generateFullLocalReference(medicationAdministration.getRequest().getReference(), baseUrl));
				resourcemetadataList.add(rRequest);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rRequestChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "request", 0, medicationAdministration.getRequest().getReference());
					resourcemetadataList.addAll(rRequestChain);
				}
			}

			// status : token
			if (medicationAdministration.hasStatus() && medicationAdministration.getStatus() != null) {
				Resourcemetadata rStatus = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", medicationAdministration.getStatus().toCode(), medicationAdministration.getStatus().getSystem());
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
