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
import org.hl7.fhir.r4.model.Account;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Identifier;
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
public class ResourcemetadataAccount extends ResourcemetadataProxy {

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
        ByteArrayInputStream iAccount = null;

		try {
			// Extract and convert the resource contents to a Account object
			if (chainedResource != null) {
				iAccount = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iAccount = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
			Account account = (Account) xmlP.parse(iAccount);
			iAccount.close();

			/*
			 * Create new Resourcemetadata objects for each Account metadata value and add to the resourcemetadataList
			 */

			// Add any passed in tags
			List<Resourcemetadata> tagMetadataList = this.generateResourcemetadataTagList(resource, account, chainedParameter);
			resourcemetadataList.addAll(tagMetadataList);

			// _id : token
			Resourcemetadata _id = generateResourcemetadata(resource, chainedResource, chainedParameter+"_id", account.getId());
			resourcemetadataList.add(_id);

			// _language : token
			if (account.getLanguage() != null) {
				Resourcemetadata _language = generateResourcemetadata(resource, chainedResource, chainedParameter+"_language", account.getLanguage());
				resourcemetadataList.add(_language);
			}

			// _lastUpdated : date
			if (account.getMeta() != null && account.getMeta().getLastUpdated() != null) {
				Resourcemetadata _lastUpdated = generateResourcemetadata(resource, chainedResource, chainedParameter+"_lastUpdated", utcDateUtil.formatDate(account.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(account.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(_lastUpdated);
			}

			// identifier : token
			if (account.hasIdentifier()) {

				for (Identifier identifier : account.getIdentifier()) {

					Resourcemetadata rIdentifier = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rIdentifier);
				}
			}

			// name : string
			if (account.hasName()) {
				Resourcemetadata rName = generateResourcemetadata(resource, chainedResource, chainedParameter+"name", account.getName());
				resourcemetadataList.add(rName);
			}

			// owner : reference
			if (account.hasOwner() && account.getOwner().hasReference()) {
				String ownerReference = generateFullLocalReference(account.getOwner().getReference(), baseUrl);

				Resourcemetadata rOwner = generateResourcemetadata(resource, chainedResource, chainedParameter+"owner", ownerReference);
				resourcemetadataList.add(rOwner);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rOwnerChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "owner", 0, account.getOwner().getReference());
					resourcemetadataList.addAll(rOwnerChain);
				}
			}

			// period : date(period)
			if (account.hasServicePeriod()) {
				Resourcemetadata rPeriod = generateResourcemetadata(resource, chainedResource, chainedParameter+"period", utcDateUtil.formatDate(account.getServicePeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(account.getServicePeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(account.getServicePeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), utcDateUtil.formatDate(account.getServicePeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), "PERIOD");
				resourcemetadataList.add(rPeriod);
			}

			// status : token
			if (account.hasStatus() && account.getStatus() != null) {
				Resourcemetadata rStatus = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", account.getStatus().toCode(), account.getStatus().getSystem());
				resourcemetadataList.add(rStatus);
			}

			// patient : reference
			// subject : reference
			if (account.hasSubject()) {

				String subjectReference = null;
				List<Resourcemetadata> rSubjectChain = null;
				for (Reference subject : account.getSubject()) {

					if (subject.hasReference()) {
						subjectReference = generateFullLocalReference(subject.getReference(), baseUrl);

						Resourcemetadata rSubject = generateResourcemetadata(resource, chainedResource, chainedParameter+"subject", subjectReference);
						resourcemetadataList.add(rSubject);

						if (chainedResource == null) {
							// Add chained parameters for any
							rSubjectChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "subject", 0, subject.getReference());
							resourcemetadataList.addAll(rSubjectChain);
						}

						// patient : reference
						if (subjectReference.indexOf("Patient") >= 0) {
							Resourcemetadata rPatient = generateResourcemetadata(resource, chainedResource, chainedParameter+"patient", subjectReference);
							resourcemetadataList.add(rPatient);

							if (chainedResource == null) {
								// Add chained parameters
								rSubjectChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "patient", 0, subject.getReference());
								resourcemetadataList.addAll(rSubjectChain);
							}
						}
					}
				}
			}

			// type : token
			if (account.hasType() && account.getType().hasCoding()) {

				Resourcemetadata rType = null;
				for (Coding type : account.getType().getCoding()) {
					rType = generateResourcemetadata(resource, chainedResource, chainedParameter+"type", type.getCode(), type.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(type));
					resourcemetadataList.add(rType);
				}
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
        } finally {
            if (iAccount != null) {
                try {
                	iAccount.close();
                } catch (IOException ioe) {
                	ioe.printStackTrace();
                }
            }
		}

		return resourcemetadataList;
	}

}
