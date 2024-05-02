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
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.EpisodeOfCare;
import org.hl7.fhir.r4.model.EpisodeOfCare.DiagnosisComponent;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;

/**
 * @author richard.ettema
 *
 */
public class ResourcemetadataEpisodeOfCare extends ResourcemetadataProxy {

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
        ByteArrayInputStream iEpisodeOfCare = null;

		try {
            // Extract and convert the resource contents to a EpisodeOfCare object
			if (chainedResource != null) {
				iEpisodeOfCare = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iEpisodeOfCare = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
            EpisodeOfCare episodeOfCare = (EpisodeOfCare) xmlP.parse(iEpisodeOfCare);
            iEpisodeOfCare.close();

			/*
             * Create new Resourcemetadata objects for each EpisodeOfCare metadata value and add to the resourcemetadataList
			 */

			// Add any passed in tags
			List<Resourcemetadata> tagMetadataList = this.generateResourcemetadataTagList(resource, episodeOfCare, chainedParameter);
			resourcemetadataList.addAll(tagMetadataList);

			// _id : token
			Resourcemetadata _id = generateResourcemetadata(resource, chainedResource, chainedParameter+"_id", episodeOfCare.getId());
			resourcemetadataList.add(_id);

			// _language : token
			if (episodeOfCare.getLanguage() != null) {
				Resourcemetadata _language = generateResourcemetadata(resource, chainedResource, chainedParameter+"_language", episodeOfCare.getLanguage());
				resourcemetadataList.add(_language);
			}

			// _lastUpdated : date
			if (episodeOfCare.getMeta() != null && episodeOfCare.getMeta().getLastUpdated() != null) {
				Resourcemetadata _lastUpdated = generateResourcemetadata(resource, chainedResource, chainedParameter+"_lastUpdated", utcDateUtil.formatDate(episodeOfCare.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(episodeOfCare.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(_lastUpdated);
			}

			// care-manager : reference
			if (episodeOfCare.hasCareManager() && episodeOfCare.getCareManager().hasReference()) {
				Resourcemetadata rCareManager = generateResourcemetadata(resource, chainedResource, chainedParameter+"care-manager", generateFullLocalReference(episodeOfCare.getCareManager().getReference(), baseUrl));
				resourcemetadataList.add(rCareManager);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rCareManagerChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "care-manager", 0, episodeOfCare.getCareManager().getReference());
					resourcemetadataList.addAll(rCareManagerChain);
				}
			}

			// condition : reference
			if (episodeOfCare.hasDiagnosis()) {

				Resourcemetadata rDiagnosis = null;
				List<Resourcemetadata> rDiagnosisChain = null;
				for (DiagnosisComponent diagnosis : episodeOfCare.getDiagnosis()) {

					if (diagnosis.hasCondition() && diagnosis.getCondition().hasReference()) {
						rDiagnosis = generateResourcemetadata(resource, chainedResource, chainedParameter+"condition", generateFullLocalReference(diagnosis.getCondition().getReference(), baseUrl));
						resourcemetadataList.add(rDiagnosis);

						if (chainedResource == null) {
							// Add chained parameters
							rDiagnosisChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "condition", 0, diagnosis.getCondition().getReference());
							resourcemetadataList.addAll(rDiagnosisChain);
						}
					}
				}
			}

			// date : date(period)
			if (episodeOfCare.hasPeriod()) {
				Resourcemetadata rDate = generateResourcemetadata(resource, chainedResource, chainedParameter+"date", utcDateUtil.formatDate(episodeOfCare.getPeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(episodeOfCare.getPeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(episodeOfCare.getPeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), utcDateUtil.formatDate(episodeOfCare.getPeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), "PERIOD");
				resourcemetadataList.add(rDate);
			}

			// identifier : token
			if (episodeOfCare.hasIdentifier()) {

				for (Identifier identifier : episodeOfCare.getIdentifier()) {

					Resourcemetadata rIdentifier = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rIdentifier);
				}
			}

			// incoming-referral : reference
			if (episodeOfCare.hasReferralRequest()) {

				Resourcemetadata rReferralRequest = null;
				List<Resourcemetadata> rReferralRequestChain = null;
				for (Reference incomingreferral : episodeOfCare.getReferralRequest()) {

					if (incomingreferral.hasReference()) {
						rReferralRequest = generateResourcemetadata(resource, chainedResource, chainedParameter+"incoming-referral", generateFullLocalReference(incomingreferral.getReference(), baseUrl));
						resourcemetadataList.add(rReferralRequest);

						if (chainedResource == null) {
							// Add chained parameters
							rReferralRequestChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "incoming-referral", 0, incomingreferral.getReference());
							resourcemetadataList.addAll(rReferralRequestChain);
						}
					}
				}
			}

			// organization : reference
			if (episodeOfCare.hasManagingOrganization() && episodeOfCare.getManagingOrganization().hasReference()) {
				Resourcemetadata rOrganization = generateResourcemetadata(resource, chainedResource, chainedParameter+"organization", generateFullLocalReference(episodeOfCare.getManagingOrganization().getReference(), baseUrl));
				resourcemetadataList.add(rOrganization);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rOrganizationChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "organization", 0, episodeOfCare.getManagingOrganization().getReference());
					resourcemetadataList.addAll(rOrganizationChain);
				}
			}

			// patient : reference
			if (episodeOfCare.hasPatient() && episodeOfCare.getPatient().hasReference()) {
				Resourcemetadata rPatient = generateResourcemetadata(resource, chainedResource, chainedParameter+"patient", generateFullLocalReference(episodeOfCare.getPatient().getReference(), baseUrl));
				resourcemetadataList.add(rPatient);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rPatientChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "patient", 0, episodeOfCare.getPatient().getReference());
					resourcemetadataList.addAll(rPatientChain);
				}
			}

			// status : token
			if (episodeOfCare.hasStatus() && episodeOfCare.getStatus() != null) {
				Resourcemetadata rStatus = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", episodeOfCare.getStatus().toCode(), episodeOfCare.getStatus().getSystem());
				resourcemetadataList.add(rStatus);
			}

			// type : token
			if (episodeOfCare.hasType()) {

				Resourcemetadata rType = null;
				for (CodeableConcept episodeOfCareType : episodeOfCare.getType()) {

					if (episodeOfCareType.hasCoding()) {
						for (Coding type : episodeOfCareType.getCoding()) {
							rType = generateResourcemetadata(resource, chainedResource, chainedParameter+"type", type.getCode(), type.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(type));
							resourcemetadataList.add(rType);
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
