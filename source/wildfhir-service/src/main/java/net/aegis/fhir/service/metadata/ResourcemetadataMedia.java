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
import org.hl7.fhir.r4.model.Media;
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
public class ResourcemetadataMedia extends ResourcemetadataProxy {

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
        ByteArrayInputStream iMedia = null;

		try {
            // Extract and convert the resource contents to a Media object
			if (chainedResource != null) {
				iMedia = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iMedia = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
            Media media = (Media) xmlP.parse(iMedia);
            iMedia.close();

			/*
             * Create new Resourcemetadata objects for each Media metadata value and add to the resourcemetadataList
			 */

			// Add any passed in tags
			List<Resourcemetadata> tagMetadataList = this.generateResourcemetadataTagList(resource, media, chainedParameter);
			resourcemetadataList.addAll(tagMetadataList);

			// _id : token
			if (media.getId() != null) {
				Resourcemetadata _id = generateResourcemetadata(resource, chainedResource, chainedParameter+"_id", media.getId());
				resourcemetadataList.add(_id);
			}

			// _language : token
			if (media.getLanguage() != null) {
				Resourcemetadata _language = generateResourcemetadata(resource, chainedResource, chainedParameter+"_language", media.getLanguage());
				resourcemetadataList.add(_language);
			}

			// _lastUpdated : date
			if (media.getMeta() != null && media.getMeta().getLastUpdated() != null) {
				Resourcemetadata _lastUpdated = generateResourcemetadata(resource, chainedResource, chainedParameter+"_lastUpdated", utcDateUtil.formatDate(media.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(media.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(_lastUpdated);
			}

			// based-on : reference
			if (media.hasBasedOn()) {

				List<Resourcemetadata> rBasedOnChain = null;
				for (Reference basedOn : media.getBasedOn()) {

					if (basedOn.hasReference()) {
						Resourcemetadata rBasedOn = generateResourcemetadata(resource, chainedResource, chainedParameter+"based-on", generateFullLocalReference(basedOn.getReference(), baseUrl));
						resourcemetadataList.add(rBasedOn);

						if (chainedResource == null) {
							// Add chained parameters
							rBasedOnChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "based-on", 0, basedOn.getReference(), null);
							resourcemetadataList.addAll(rBasedOnChain);
						}
					}
				}
			}

			// created : datetime(period)
			if (media.hasCreatedDateTimeType()) {
				Resourcemetadata rDate = generateResourcemetadata(resource, chainedResource, chainedParameter+"created", utcDateUtil.formatDate(media.getCreatedDateTimeType().getValue(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(media.getCreatedDateTimeType().getValue(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rDate);
			}
			else if (media.hasCreatedPeriod()) {
				Resourcemetadata rDate = generateResourcemetadata(resource, chainedResource, chainedParameter+"created", utcDateUtil.formatDate(media.getCreatedPeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(media.getCreatedPeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(media.getCreatedPeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), utcDateUtil.formatDate(media.getCreatedPeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), "PERIOD");
				resourcemetadataList.add(rDate);
			}

			// device : reference
			if (media.hasDevice() && media.getDevice().hasReference()) {
				Resourcemetadata rDevice = generateResourcemetadata(resource, chainedResource, chainedParameter+"device", generateFullLocalReference(media.getDevice().getReference(), baseUrl));
				resourcemetadataList.add(rDevice);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rDeviceChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "device", 0, media.getDevice().getReference(), null);
					resourcemetadataList.addAll(rDeviceChain);
				}
			}

			// encounter : reference
			if (media.hasEncounter() && media.getEncounter().hasReference()) {
				String contextString = generateFullLocalReference(media.getEncounter().getReference(), baseUrl);

				Resourcemetadata rEncounter = generateResourcemetadata(resource, chainedResource, chainedParameter+"encounter", contextString);
				resourcemetadataList.add(rEncounter);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rEncounterChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "encounter", 0, media.getEncounter().getReference(), null);
					resourcemetadataList.addAll(rEncounterChain);
				}
			}

			// identifier : reference
			if (media.hasIdentifier()) {

				for (Identifier identifier : media.getIdentifier()) {

					Resourcemetadata rIdentifier = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rIdentifier);
				}
			}

			// modality : token
			if (media.hasModality() && media.getModality().hasCoding()) {

				Resourcemetadata rCode = null;
				for (Coding code : media.getModality().getCoding()) {
					rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"modality", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rCode);
				}
			}

			// operator : reference
			if (media.hasOperator() && media.getOperator().hasReference()) {
				Resourcemetadata rOperator = generateResourcemetadata(resource, chainedResource, chainedParameter+"operator", generateFullLocalReference(media.getOperator().getReference(), baseUrl));
				resourcemetadataList.add(rOperator);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rOperatorChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "operator", 0, media.getOperator().getReference(), null);
					resourcemetadataList.addAll(rOperatorChain);
				}
			}

			// patient : reference
			// subject : reference
			if (media.hasSubject() && media.getSubject().hasReference()) {
				String subjectReference = generateFullLocalReference(media.getSubject().getReference(), baseUrl);

				Resourcemetadata rSubject = generateResourcemetadata(resource, chainedResource, chainedParameter+"subject", subjectReference);
				resourcemetadataList.add(rSubject);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rSubjectChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "subject", 0, media.getSubject().getReference(), null);
					resourcemetadataList.addAll(rSubjectChain);
				}

				if (subjectReference.indexOf("Patient") >= 0) {
					Resourcemetadata rPatient = generateResourcemetadata(resource, chainedResource, chainedParameter+"patient", subjectReference);
					resourcemetadataList.add(rPatient);

					if (chainedResource == null) {
						// Add chained parameters
						List<Resourcemetadata> rPatientChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "patient", 0, media.getSubject().getReference(), null);
						resourcemetadataList.addAll(rPatientChain);
					}
				}
			}

			// site : token
			if (media.hasBodySite() && media.getBodySite().hasCoding()) {

				Resourcemetadata rCode = null;
				for (Coding code : media.getBodySite().getCoding()) {
					rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"site", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rCode);
				}
			}

			// status : token
			if (media.hasStatus() && media.getStatus() != null) {
				Resourcemetadata rStatus = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", media.getStatus().toCode(), media.getStatus().getSystem());
				resourcemetadataList.add(rStatus);
			}

			// type : token
			if (media.hasType() && media.getType().hasCoding()) {

				Resourcemetadata rType = null;
				for (Coding type : media.getType().getCoding()) {
					rType = generateResourcemetadata(resource, chainedResource, chainedParameter+"type", type.getCode(), type.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(type));
					resourcemetadataList.add(rType);
				}
			}

			// view : token
			if (media.hasView() && media.getView().hasCoding()) {

				Resourcemetadata rCode = null;
				for (Coding code : media.getView().getCoding()) {
					rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"view", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rCode);

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
