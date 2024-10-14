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
import org.hl7.fhir.r4.model.Contract;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Contract.SignatoryComponent;
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
public class ResourcemetadataContract extends ResourcemetadataProxy {

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
        ByteArrayInputStream iContract = null;

		try {
            // Extract and convert the resource contents to a Contract object
			if (chainedResource != null) {
				iContract = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iContract = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
            Contract contract = (Contract) xmlP.parse(iContract);
            iContract.close();

			/*
             * Create new Resourcemetadata objects for each Contract metadata value and add to the resourcemetadataList
			 */

			// Add any passed in tags
			List<Resourcemetadata> tagMetadataList = this.generateResourcemetadataTagList(resource, contract, chainedParameter);
			resourcemetadataList.addAll(tagMetadataList);

			// _id : token
			if (contract.getId() != null) {
				Resourcemetadata _id = generateResourcemetadata(resource, chainedResource, chainedParameter+"_id", contract.getId());
				resourcemetadataList.add(_id);
			}

			// _language : token
			if (contract.getLanguage() != null) {
				Resourcemetadata _language = generateResourcemetadata(resource, chainedResource, chainedParameter+"_language", contract.getLanguage());
				resourcemetadataList.add(_language);
			}

			// _lastUpdated : date
			if (contract.getMeta() != null && contract.getMeta().getLastUpdated() != null) {
				Resourcemetadata _lastUpdated = generateResourcemetadata(resource, chainedResource, chainedParameter+"_lastUpdated", utcDateUtil.formatDate(contract.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(contract.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(_lastUpdated);
			}

			// authority : reference
			if (contract.hasAuthority()) {

				for (Reference authority : contract.getAuthority()) {

					if (authority.hasReference()) {
						String authorityReference = generateFullLocalReference(authority.getReference(), baseUrl);

						Resourcemetadata rAuthority = generateResourcemetadata(resource, chainedResource, chainedParameter+"authority", authorityReference);
						resourcemetadataList.add(rAuthority);

						if (chainedResource == null) {
							// Add chained parameters
							List<Resourcemetadata> rAuthorityChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "authority", 0, authority.getReference(), null);
							resourcemetadataList.addAll(rAuthorityChain);
						}
					}
				}
			}

			// domain : reference
			if (contract.hasDomain()) {

				for (Reference domain : contract.getDomain()) {

					if (domain.hasReference()) {
						String authorityReference = generateFullLocalReference(domain.getReference(), baseUrl);

						Resourcemetadata rDomain = generateResourcemetadata(resource, chainedResource, chainedParameter+"domain", authorityReference);
						resourcemetadataList.add(rDomain);

						if (chainedResource == null) {
							// Add chained parameters
							List<Resourcemetadata> rDomainChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "domain", 0, domain.getReference(), null);
							resourcemetadataList.addAll(rDomainChain);
						}
					}
				}
			}

			// identifier : token
			if (contract.hasIdentifier()) {

				for (Identifier identifier : contract.getIdentifier()) {

					Resourcemetadata rIdentifier = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rIdentifier);
				}
			}

			// instantiates : uri
			if (contract.hasInstantiatesUri()) {
				Resourcemetadata rInstantiates = generateResourcemetadata(resource, chainedResource, chainedParameter+"instantiates", contract.getInstantiatesUri());
				resourcemetadataList.add(rInstantiates);
			}

			// issued : date
			if (contract.hasIssued()) {
				Resourcemetadata rIssued = generateResourcemetadata(resource, chainedResource, chainedParameter+"issued", utcDateUtil.formatDate(contract.getIssued(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(contract.getIssued(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rIssued);
			}

			// patient : reference
			// subject : reference
			if (contract.hasSubject()) {

				String subjectReference = null;
				List<Resourcemetadata> rSubjectChain = null;
				for (Reference subject : contract.getSubject()) {

					if (subject.hasReference()) {
						subjectReference = generateFullLocalReference(subject.getReference(), baseUrl);

						Resourcemetadata rSubject = generateResourcemetadata(resource, chainedResource, chainedParameter+"subject", subjectReference);
						resourcemetadataList.add(rSubject);

						if (chainedResource == null) {
							// Add chained parameters for any
							rSubjectChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "subject", 0, subject.getReference(), null);
							resourcemetadataList.addAll(rSubjectChain);
						}

						// patient : reference
						if (subjectReference.indexOf("Patient") >= 0) {
							Resourcemetadata rPatient = generateResourcemetadata(resource, chainedResource, chainedParameter+"patient", subjectReference);
							resourcemetadataList.add(rPatient);

							if (chainedResource == null) {
								// Add chained parameters
								rSubjectChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "patient", 0, subject.getReference(), null);
								resourcemetadataList.addAll(rSubjectChain);
							}
						}
					}
				}
			}

			// signer : reference
			if (contract.hasSigner()) {

				List<Resourcemetadata> rSignerChain = null;
				for (SignatoryComponent signer : contract.getSigner()) {

					if (signer.hasParty() && signer.getParty().hasReference()) {
						Resourcemetadata rSigner = generateResourcemetadata(resource, chainedResource, chainedParameter+"signer", generateFullLocalReference(signer.getParty().getReference(), baseUrl));
						resourcemetadataList.add(rSigner);

						if (chainedResource == null) {
							// Add chained parameters for any
							rSignerChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "signer", 0, signer.getParty().getReference(), null);
							resourcemetadataList.addAll(rSignerChain);
						}
					}
				}
			}

			// status : token
			if (contract.hasStatus() && contract.getStatus() != null) {
				Resourcemetadata rStatus = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", contract.getStatus().toCode(), contract.getStatus().getSystem());
				resourcemetadataList.add(rStatus);
			}

			// url : uri
			if (contract.hasUrl()) {
				Resourcemetadata rUrl = generateResourcemetadata(resource, chainedResource, chainedParameter+"url", contract.getUrl());
				resourcemetadataList.add(rUrl);
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		}

		return resourcemetadataList;
	}

}
