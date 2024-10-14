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
import org.hl7.fhir.r4.model.Consent;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Consent.provisionActorComponent;
import org.hl7.fhir.r4.model.Consent.provisionDataComponent;
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
public class ResourcemetadataConsent extends ResourcemetadataProxy {

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
        ByteArrayInputStream iConsent = null;
		Resourcemetadata rCode = null;

		try {
			// Extract and convert the resource contents to a Consent object
			if (chainedResource != null) {
				iConsent = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iConsent = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
			Consent consent = (Consent) xmlP.parse(iConsent);
			iConsent.close();

			/*
			 * Create new Resourcemetadata objects for each Consent metadata value and add to the resourcemetadataList
			 */

			// Add any passed in tags
			List<Resourcemetadata> tagMetadataList = this.generateResourcemetadataTagList(resource, consent, chainedParameter);
			resourcemetadataList.addAll(tagMetadataList);

			// _id : token
			if (consent.getId() != null) {
				Resourcemetadata _id = generateResourcemetadata(resource, chainedResource, chainedParameter+"_id", consent.getId());
				resourcemetadataList.add(_id);
			}

			// _language : token
			if (consent.getLanguage() != null) {
				Resourcemetadata _language = generateResourcemetadata(resource, chainedResource, chainedParameter+"_language", consent.getLanguage());
				resourcemetadataList.add(_language);
			}

			// _lastUpdated : date
			if (consent.getMeta() != null && consent.getMeta().getLastUpdated() != null) {
				Resourcemetadata _lastUpdated = generateResourcemetadata(resource, chainedResource, chainedParameter+"_lastUpdated", utcDateUtil.formatDate(consent.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(consent.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(_lastUpdated);
			}

			if (consent.hasProvision()) {

				// action : token
				if (consent.getProvision().hasAction()) {

					for (CodeableConcept action : consent.getProvision().getAction()) {

						if (action.hasCoding()) {
							for (Coding code : action.getCoding()) {
								rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"action", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
								resourcemetadataList.add(rCode);
							}
						}
					}
				}

				// actor : reference
				if (consent.getProvision().hasActor()) {

					String recipientReference = null;
					for (provisionActorComponent actor : consent.getProvision().getActor()) {

						if (actor.hasReference() && actor.getReference().hasReference()) {
							recipientReference = generateFullLocalReference(actor.getReference().getReference(), baseUrl);

							Resourcemetadata rActor = generateResourcemetadata(resource, chainedResource, chainedParameter+"actor", recipientReference);
							resourcemetadataList.add(rActor);

							if (chainedResource == null) {
								// Add chained parameters
								List<Resourcemetadata> rRecipientChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "actor", 0, actor.getReference().getReference(), null);
								resourcemetadataList.addAll(rRecipientChain);
							}
						}
					}
				}

				// data : reference
				if (consent.getProvision().hasData()) {

					for (provisionDataComponent data : consent.getProvision().getData()) {

						if (data.hasReference() && data.getReference().hasReference()) {
							Resourcemetadata rData = generateResourcemetadata(resource, chainedResource, chainedParameter+"data", generateFullLocalReference(data.getReference().getReference(), baseUrl));
							resourcemetadataList.add(rData);

							if (chainedResource == null) {
								// Add chained parameters for any
								List<Resourcemetadata> rDataChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "data", 0, data.getReference().getReference(), null);
								resourcemetadataList.addAll(rDataChain);
							}
						}
					}
				}

				// period : date(period)
				if (consent.getProvision().hasPeriod()) {
					Resourcemetadata rPeriod = generateResourcemetadata(resource, chainedResource, chainedParameter+"period", utcDateUtil.formatDate(consent.getProvision().getPeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(consent.getProvision().getPeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(consent.getProvision().getPeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), utcDateUtil.formatDate(consent.getProvision().getPeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), "PERIOD");
					resourcemetadataList.add(rPeriod);
				}

				// purpose : token
				if (consent.getProvision().hasPurpose()) {

					for (Coding purpose : consent.getProvision().getPurpose()) {

						Resourcemetadata rPurpose = generateResourcemetadata(resource, chainedResource, chainedParameter+"purpose", purpose.getCode(), purpose.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(purpose));
						resourcemetadataList.add(rPurpose);
					}
				}

				// security-label : token
				if (consent.getProvision().hasSecurityLabel()) {

					for (Coding security : consent.getProvision().getSecurityLabel()) {

						Resourcemetadata rSecurity = generateResourcemetadata(resource, chainedResource, chainedParameter+"security-label", security.getCode(), security.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(security));
						resourcemetadataList.add(rSecurity);
					}
				}
			}

			// category : token
			if (consent.hasCategory()) {

				for (CodeableConcept category : consent.getCategory()) {

					if (category.hasCoding()) {
						for (Coding code : category.getCoding()) {
							rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"category", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rCode);
						}
					}
				}
			}

			// consentor : reference
			if (consent.hasPerformer()) {

				String consentorReference = null;
				for (Reference consentor : consent.getPerformer()) {

					if (consentor.hasReference()) {
						consentorReference = generateFullLocalReference(consentor.getReference(), baseUrl);

						Resourcemetadata rRecipient = generateResourcemetadata(resource, chainedResource, chainedParameter+"consentor", consentorReference);
						resourcemetadataList.add(rRecipient);

						if (chainedResource == null) {
							// Add chained parameters
							List<Resourcemetadata> rConsentorChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "consentor", 0, consentor.getReference(), null);
							resourcemetadataList.addAll(rConsentorChain);
						}
					}
				}
			}

			// date : date
			if (consent.hasDateTime()) {
				Resourcemetadata rDate = generateResourcemetadata(resource, chainedResource, chainedParameter+"date", utcDateUtil.formatDate(consent.getDateTime(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(consent.getDateTime(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rDate);
			}

			// identifier : token
			if (consent.hasIdentifier()) {

				for (Identifier identifier : consent.getIdentifier()) {

					Resourcemetadata rIdentifier = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rIdentifier);
				}
			}

			// organization : reference
			if (consent.hasOrganization()) {

				Resourcemetadata rOrganization = null;
				List<Resourcemetadata> rOrganizationChain = null;
				for (Reference organization : consent.getOrganization()) {

					if (organization.hasReference()) {
						rOrganization = generateResourcemetadata(resource, chainedResource, chainedParameter+"organization", generateFullLocalReference(organization.getReference(), baseUrl));
						resourcemetadataList.add(rOrganization);

						if (chainedResource == null) {
							// Add chained parameters
							rOrganizationChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "organization", 0, organization.getReference(), null);
							resourcemetadataList.addAll(rOrganizationChain);
						}
					}
				}
			}

			// patient : reference
			if (consent.hasPatient() && consent.getPatient().hasReference()) {
				Resourcemetadata rPatient = generateResourcemetadata(resource, chainedResource, chainedParameter+"patient", generateFullLocalReference(consent.getPatient().getReference(), baseUrl));
				resourcemetadataList.add(rPatient);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rPatientChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "patient", 0, consent.getPatient().getReference(), null);
					resourcemetadataList.addAll(rPatientChain);
				}
			}

			// scope : token
			if (consent.hasScope() && consent.getScope().hasCoding()) {

				for (Coding code : consent.getScope().getCoding()) {
					rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"scope", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rCode);
				}
			}

			// source-reference : reference
			if (consent.hasSourceReference()) {
				String sourceReference = generateFullLocalReference(consent.getSourceReference().getReference(), baseUrl);

				Resourcemetadata rSource = generateResourcemetadata(resource, chainedResource, chainedParameter+"source-reference", sourceReference);
				resourcemetadataList.add(rSource);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rSourceChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "source-reference", 0, consent.getSourceReference().getReference(), null);
					resourcemetadataList.addAll(rSourceChain);
				}
			}

			// status : token
			if (consent.hasStatus() && consent.getStatus() != null) {
				Resourcemetadata rStatus = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", consent.getStatus().toCode(), consent.getStatus().getSystem());
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
