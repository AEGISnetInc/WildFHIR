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
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.FamilyMemberHistory;
import org.hl7.fhir.r4.model.FamilyMemberHistory.FamilyMemberHistoryConditionComponent;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.UriType;

import net.aegis.fhir.model.Resource;
import net.aegis.fhir.model.Resourcemetadata;
import net.aegis.fhir.service.ResourceService;
import net.aegis.fhir.service.util.ServicesUtil;
import net.aegis.fhir.service.util.UTCDateUtil;

/**
 * @author richard.ettema
 *
 */
public class ResourcemetadataFamilyMemberHistory extends ResourcemetadataProxy {

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
        ByteArrayInputStream iFamilyMemberHistory = null;

		try {
			// Extract and convert the resource contents to a FamilyMemberHistory object
			if (chainedResource != null) {
				iFamilyMemberHistory = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iFamilyMemberHistory = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
			FamilyMemberHistory familyMemberHistory = (FamilyMemberHistory) xmlP.parse(iFamilyMemberHistory);
			iFamilyMemberHistory.close();

			/*
			 * Create new Resourcemetadata objects for each FamilyMemberHistory metadata value and add to the resourcemetadataList
			 */

			// Add any passed in tags
			List<Resourcemetadata> tagMetadataList = this.generateResourcemetadataTagList(resource, familyMemberHistory, chainedParameter);
			resourcemetadataList.addAll(tagMetadataList);

			// _id : token
			Resourcemetadata _id = generateResourcemetadata(resource, chainedResource, chainedParameter+"_id", familyMemberHistory.getId());
			resourcemetadataList.add(_id);

			// _language : token
			if (familyMemberHistory.getLanguage() != null) {
				Resourcemetadata _language = generateResourcemetadata(resource, chainedResource, chainedParameter+"_language", familyMemberHistory.getLanguage());
				resourcemetadataList.add(_language);
			}

			// _lastUpdated : date
			if (familyMemberHistory.getMeta() != null && familyMemberHistory.getMeta().getLastUpdated() != null) {
				Resourcemetadata _lastUpdated = generateResourcemetadata(resource, chainedResource, chainedParameter+"_lastUpdated", utcDateUtil.formatDate(familyMemberHistory.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(familyMemberHistory.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(_lastUpdated);
			}

			// code : token
			if (familyMemberHistory.hasCondition()) {

				Resourcemetadata rCode = null;
				for (FamilyMemberHistoryConditionComponent condition : familyMemberHistory.getCondition()) {

					if (condition.hasCode() && condition.getCode().hasCoding()) {
						for (Coding code : condition.getCode().getCoding()) {
							rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"code", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rCode);
						}
					}
				}
			}

			// date : date
			if (familyMemberHistory.hasDate()) {
				Resourcemetadata rDate = generateResourcemetadata(resource, chainedResource, chainedParameter+"date", utcDateUtil.formatDate(familyMemberHistory.getDate(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(familyMemberHistory.getDate(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rDate);
			}

			// identifier : token
			if (familyMemberHistory.hasIdentifier()) {

				for (Identifier identifier : familyMemberHistory.getIdentifier()) {

					Resourcemetadata rIdentifier = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rIdentifier);
				}
			}

			// instantiates-canonical : reference
			if (familyMemberHistory.hasInstantiatesCanonical()) {

				for (CanonicalType instantiates : familyMemberHistory.getInstantiatesCanonical()) {
					String objectReference = generateFullLocalReference(instantiates.asStringValue(), baseUrl);

					List<Resourcemetadata> rInstantiatesChain = null;
					Resourcemetadata rReference = generateResourcemetadata(resource, chainedResource, chainedParameter+"instantiates-canonical", objectReference);
					resourcemetadataList.add(rReference);

					if (chainedResource == null) {
						// Add chained parameters
						rInstantiatesChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "instantiates-canonical", 0, instantiates.asStringValue());
						resourcemetadataList.addAll(rInstantiatesChain);
					}
				}
			}

			// instantiates-uri : uri
			if (familyMemberHistory.hasInstantiatesUri()) {

				for (UriType instantiates : familyMemberHistory.getInstantiatesUri()) {

					Resourcemetadata rInstantiates = generateResourcemetadata(resource, chainedResource, chainedParameter+"instantiates-uri", instantiates.asStringValue());
					resourcemetadataList.add(rInstantiates);
				}
			}

			// patient : reference
			if (familyMemberHistory.hasPatient() && familyMemberHistory.getPatient().hasReference()) {
				Resourcemetadata rPatient = generateResourcemetadata(resource, chainedResource, chainedParameter+"patient", generateFullLocalReference(familyMemberHistory.getPatient().getReference(), baseUrl));
				resourcemetadataList.add(rPatient);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rPatientChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "patient", 0, familyMemberHistory.getPatient().getReference());
					resourcemetadataList.addAll(rPatientChain);
				}
			}

			// relationship : token
			if (familyMemberHistory.hasRelationship() && familyMemberHistory.getRelationship().hasCoding()) {

				Resourcemetadata rCode = null;
				for (Coding code : familyMemberHistory.getRelationship().getCoding()) {
					rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"relationship", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rCode);
				}
			}

			// sex : token
			if (familyMemberHistory.hasSex() && familyMemberHistory.getSex().hasCoding()) {

				Resourcemetadata rCode = null;
				for (Coding code : familyMemberHistory.getSex().getCoding()) {
					rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"sex", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rCode);
				}
			}

			// status : token
			if (familyMemberHistory.hasStatus() && familyMemberHistory.getStatus() != null) {
				Resourcemetadata rStatus = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", familyMemberHistory.getStatus().toCode(), familyMemberHistory.getStatus().getSystem());
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
