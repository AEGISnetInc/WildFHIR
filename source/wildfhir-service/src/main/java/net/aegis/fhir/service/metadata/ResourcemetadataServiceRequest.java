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
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.hl7.fhir.r4.model.UriType;

/**
 * @author richard.ettema
 *
 */
public class ResourcemetadataServiceRequest extends ResourcemetadataProxy {

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
        ByteArrayInputStream iServiceRequest = null;

		try {
            // Extract and convert the resource contents to a ServiceRequest object
			if (chainedResource != null) {
				iServiceRequest = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iServiceRequest = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
            ServiceRequest serviceRequest = (ServiceRequest) xmlP.parse(iServiceRequest);
            iServiceRequest.close();

			/*
             * Create new Resourcemetadata objects for each ServiceRequest metadata value and add to the resourcemetadataList
			 */

			// Add any passed in tags
			List<Resourcemetadata> tagMetadataList = this.generateResourcemetadataTagList(resource, serviceRequest, chainedParameter);
			resourcemetadataList.addAll(tagMetadataList);

			// _id : token
			if (serviceRequest.getId() != null) {
				Resourcemetadata _id = generateResourcemetadata(resource, chainedResource, chainedParameter+"_id", serviceRequest.getId());
				resourcemetadataList.add(_id);
			}

			// _language : token
			if (serviceRequest.getLanguage() != null) {
				Resourcemetadata _language = generateResourcemetadata(resource, chainedResource, chainedParameter+"_language", serviceRequest.getLanguage());
				resourcemetadataList.add(_language);
			}

			// _lastUpdated : date
			if (serviceRequest.getMeta() != null && serviceRequest.getMeta().getLastUpdated() != null) {
				Resourcemetadata _lastUpdated = generateResourcemetadata(resource, chainedResource, chainedParameter+"_lastUpdated", utcDateUtil.formatDate(serviceRequest.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(serviceRequest.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(_lastUpdated);
			}

			// authored : date
			if (serviceRequest.hasAuthoredOn()) {
				Resourcemetadata rAuthoredOn = generateResourcemetadata(resource, chainedResource, chainedParameter+"authored", utcDateUtil.formatDate(serviceRequest.getAuthoredOn(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(serviceRequest.getAuthoredOn(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rAuthoredOn);
			}

			// based-on : reference
			if (serviceRequest.hasBasedOn()) {

				List<Resourcemetadata> rBasedOnChain = null;
				String basedOnString = null;
				for (Reference basedOn : serviceRequest.getBasedOn()) {

					if (basedOn.hasReference()) {
						basedOnString = generateFullLocalReference(basedOn.getReference(), baseUrl);
						Resourcemetadata rRecipient = generateResourcemetadata(resource, chainedResource, chainedParameter+"based-on", basedOnString);
						resourcemetadataList.add(rRecipient);

						if (chainedResource == null) {
							// Add chained parameters for any
							rBasedOnChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "based-on", 0, basedOn.getReference(), null);
							resourcemetadataList.addAll(rBasedOnChain);
						}
					}
				}
			}

			// body-site : token
			if (serviceRequest.hasBodySite()) {

				Resourcemetadata rCode = null;
				for (CodeableConcept bodySite : serviceRequest.getBodySite()) {

					if (bodySite.hasCoding()) {
						for (Coding code : bodySite.getCoding()) {
							rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"body-site", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rCode);
						}
					}
				}
			}

			// category : token
			if (serviceRequest.hasCategory()) {

				Resourcemetadata rCode = null;
				for (CodeableConcept category : serviceRequest.getCategory()) {

					if (category.hasCoding()) {
						for (Coding code : category.getCoding()) {
							rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"category", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rCode);
						}
					}
				}
			}

			// code : token
			if (serviceRequest.hasCode() && serviceRequest.getCode().hasCoding()) {

				Resourcemetadata rCode = null;
				for (Coding code : serviceRequest.getCode().getCoding()) {
					rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"code", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rCode);
				}
			}

			// encounter : reference
			if (serviceRequest.hasEncounter() && serviceRequest.getEncounter().hasReference()) {
				String encounterString = generateFullLocalReference(serviceRequest.getEncounter().getReference(), baseUrl);

				Resourcemetadata rEncounter = generateResourcemetadata(resource, chainedResource, chainedParameter+"encounter", encounterString);
				resourcemetadataList.add(rEncounter);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rEncounterChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "encounter", 0, serviceRequest.getEncounter().getReference(), null);
					resourcemetadataList.addAll(rEncounterChain);
				}
			}

			// identifier : token
			if (serviceRequest.hasIdentifier()) {

				for (Identifier identifier : serviceRequest.getIdentifier()) {

					Resourcemetadata rIdentifier = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rIdentifier);
				}
			}

			// instantiates-canonical : reference
			if (serviceRequest.hasInstantiatesCanonical()) {

				for (CanonicalType instantiates : serviceRequest.getInstantiatesCanonical()) {
					String objectReference = generateFullLocalReference(instantiates.asStringValue(), baseUrl);

					List<Resourcemetadata> rInstantiatesChain = null;
					Resourcemetadata rReference = generateResourcemetadata(resource, chainedResource, chainedParameter+"instantiates-canonical", objectReference);
					resourcemetadataList.add(rReference);

					if (chainedResource == null) {
						// Add chained parameters
						rInstantiatesChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "instantiates-canonical", 0, instantiates.asStringValue(), null);
						resourcemetadataList.addAll(rInstantiatesChain);
					}
				}
			}

			// instantiates-uri : uri
			if (serviceRequest.hasInstantiatesUri()) {

				for (UriType instantiates : serviceRequest.getInstantiatesUri()) {

					Resourcemetadata rInstantiates = generateResourcemetadata(resource, chainedResource, chainedParameter+"instantiates-uri", instantiates.asStringValue());
					resourcemetadataList.add(rInstantiates);
				}
			}

			// intent : token
			if (serviceRequest.hasIntent() && serviceRequest.getIntent() != null) {
				Resourcemetadata rIntent = generateResourcemetadata(resource, chainedResource, chainedParameter+"intent", serviceRequest.getIntent().toCode(), serviceRequest.getIntent().getSystem());
				resourcemetadataList.add(rIntent);
			}

			// occurrence : date(period)
			if (serviceRequest.hasOccurrenceDateTimeType()) {
				Resourcemetadata rOccurrence = generateResourcemetadata(resource, chainedResource, chainedParameter+"occurrence", utcDateUtil.formatDate(serviceRequest.getOccurrenceDateTimeType().getValue(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(serviceRequest.getOccurrenceDateTimeType().getValue(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rOccurrence);
			}
			else if (serviceRequest.hasOccurrencePeriod()) {
				Resourcemetadata rOccurrence = generateResourcemetadata(resource, chainedResource, chainedParameter+"occurrence", utcDateUtil.formatDate(serviceRequest.getOccurrencePeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(serviceRequest.getOccurrencePeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(serviceRequest.getOccurrencePeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), utcDateUtil.formatDate(serviceRequest.getOccurrencePeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), "PERIOD");
				resourcemetadataList.add(rOccurrence);
			}

			// performer : reference
			if (serviceRequest.hasPerformer()) {

				Resourcemetadata rPerformer = null;
				List<Resourcemetadata> rPerformerChain = null;
				for (Reference performer : serviceRequest.getPerformer()) {

					rPerformer = generateResourcemetadata(resource, chainedResource, chainedParameter+"performer", generateFullLocalReference(performer.getReference(), baseUrl));
					resourcemetadataList.add(rPerformer);

					if (chainedResource == null) {
						// Add chained parameters for any
						rPerformerChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "performer", 0, performer.getReference(), null);
						resourcemetadataList.addAll(rPerformerChain);
					}
				}
			}

			// performer-type : token
			if (serviceRequest.hasPerformerType() && serviceRequest.getPerformerType().hasCoding()) {

				Resourcemetadata rCode = null;
				for (Coding code : serviceRequest.getPerformerType().getCoding()) {
					rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"performer-type", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rCode);
				}
			}

			// priority : token
			if (serviceRequest.hasPriority() && serviceRequest.getPriority() != null) {
				Resourcemetadata rPriority = generateResourcemetadata(resource, chainedResource, chainedParameter+"priority", serviceRequest.getPriority().toCode(), serviceRequest.getPriority().getSystem());
				resourcemetadataList.add(rPriority);
			}

			// replaces : reference
			if (serviceRequest.hasReplaces()) {

				List<Resourcemetadata> rReplacesChain = null;
				for (Reference replaces : serviceRequest.getReplaces()) {

					if (replaces.hasReference()) {
						Resourcemetadata rReplaces = generateResourcemetadata(resource, chainedResource, chainedParameter+"replaces", generateFullLocalReference(replaces.getReference(), baseUrl));
						resourcemetadataList.add(rReplaces);

						if (chainedResource == null) {
							// Add chained parameters for any
							rReplacesChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "replaces", 0, replaces.getReference(), null);
							resourcemetadataList.addAll(rReplacesChain);
						}
					}
				}
			}

			// requester : reference
			if (serviceRequest.hasRequester() && serviceRequest.getRequester().hasReference()) {
				Resourcemetadata rRequester = generateResourcemetadata(resource, chainedResource, chainedParameter+"requester", generateFullLocalReference(serviceRequest.getRequester().getReference(), baseUrl));
				resourcemetadataList.add(rRequester);

				if (chainedResource == null) {
					// Add chained parameters for any
					List<Resourcemetadata> rRequesterChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "requester", 0, serviceRequest.getRequester().getReference(), null);
					resourcemetadataList.addAll(rRequesterChain);
				}
			}

			// requisition : identifier
			if (serviceRequest.hasRequisition()) {
				Resourcemetadata rRequisition = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", serviceRequest.getRequisition().getValue(), serviceRequest.getRequisition().getSystem());
				resourcemetadataList.add(rRequisition);
			}

			// specimen : reference
			if (serviceRequest.hasSpecimen()) {

				Resourcemetadata rSpecimen = null;
				List<Resourcemetadata> rSpecimenChain = null;
				for (Reference specimen : serviceRequest.getSpecimen()) {

					if (specimen.hasReference()) {
						rSpecimen = generateResourcemetadata(resource, chainedResource, chainedParameter+"specimen", generateFullLocalReference(specimen.getReference(), baseUrl));
						resourcemetadataList.add(rSpecimen);

						if (chainedResource == null) {
							// Add chained parameters for any
							rSpecimenChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "specimen", 0, specimen.getReference(), null);
							resourcemetadataList.addAll(rSpecimenChain);
						}
					}
				}
			}

			// status : token
			if (serviceRequest.hasStatus() && serviceRequest.getStatus() != null) {
				Resourcemetadata rStatus = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", serviceRequest.getStatus().toCode(), serviceRequest.getStatus().getSystem());
				resourcemetadataList.add(rStatus);
			}

			// patient : reference
			// subject : reference
			if (serviceRequest.hasSubject() && serviceRequest.getSubject().hasReference()) {
				String subjectReference = generateFullLocalReference(serviceRequest.getSubject().getReference(), baseUrl);

				Resourcemetadata rSubject = generateResourcemetadata(resource, chainedResource, chainedParameter+"subject", subjectReference);
				resourcemetadataList.add(rSubject);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rSubjectChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "subject", 0, serviceRequest.getSubject().getReference(), null);
					resourcemetadataList.addAll(rSubjectChain);
				}

				if (subjectReference.indexOf("Patient") >= 0) {
					Resourcemetadata rPatient = generateResourcemetadata(resource, chainedResource, chainedParameter+"patient", subjectReference);
					resourcemetadataList.add(rPatient);

					if (chainedResource == null) {
						// Add chained parameters
						List<Resourcemetadata> rPatientChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "patient", 0, serviceRequest.getSubject().getReference(), null);
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
