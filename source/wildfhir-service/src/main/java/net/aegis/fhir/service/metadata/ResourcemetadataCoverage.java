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
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Coverage;
import org.hl7.fhir.r4.model.Coverage.ClassComponent;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;

/**
 * @author richard.ettema
 *
 */
public class ResourcemetadataCoverage extends ResourcemetadataProxy {

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
        ByteArrayInputStream iCoverage = null;
		Resourcemetadata rCode = null;

		try {
            // Extract and convert the resource contents to a Coverage object
			if (chainedResource != null) {
				iCoverage = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iCoverage = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
            Coverage coverage = (Coverage) xmlP.parse(iCoverage);
            iCoverage.close();

			/*
             * Create new Resourcemetadata objects for each Coverage metadata value and add to the resourcemetadataList
			 */

			// Add any passed in tags
			List<Resourcemetadata> tagMetadataList = this.generateResourcemetadataTagList(resource, coverage, chainedParameter);
			resourcemetadataList.addAll(tagMetadataList);

			// _id : token
			if (coverage.getId() != null) {
				Resourcemetadata _id = generateResourcemetadata(resource, chainedResource, chainedParameter+"_id", coverage.getId());
				resourcemetadataList.add(_id);
			}

			// _language : token
			if (coverage.getLanguage() != null) {
				Resourcemetadata _language = generateResourcemetadata(resource, chainedResource, chainedParameter+"_language", coverage.getLanguage());
				resourcemetadataList.add(_language);
			}

			// _lastUpdated : date
			if (coverage.getMeta() != null && coverage.getMeta().getLastUpdated() != null) {
				Resourcemetadata _lastUpdated = generateResourcemetadata(resource, chainedResource, chainedParameter+"_lastUpdated", utcDateUtil.formatDate(coverage.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(coverage.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(_lastUpdated);
			}

			// beneficiary : reference
			if (coverage.hasBeneficiary()) {
				Resourcemetadata rBeneficiaryReference = generateResourcemetadata(resource, chainedResource, chainedParameter+"beneficiary", generateFullLocalReference(coverage.getBeneficiary().getReference(), baseUrl));
				resourcemetadataList.add(rBeneficiaryReference);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rBeneficiaryChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "beneficiary", 0, coverage.getBeneficiary().getReference(), null);
					resourcemetadataList.addAll(rBeneficiaryChain);
				}
			}

			// class-type : token
			// class-value : string
			if (coverage.hasClass_()) {

				for (ClassComponent class_ : coverage.getClass_()) {

					if (class_.hasType() && class_.getType().hasCoding()) {

						for (Coding code : class_.getType().getCoding()) {
							rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"class-type", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rCode);
						}
					}

					if (class_.hasValue()) {
						rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"class-value", class_.getValue());
						resourcemetadataList.add(rCode);
					}
				}
			}

			// dependent : string
			if (coverage.hasDependent()) {
				Resourcemetadata rDependent = generateResourcemetadata(resource, chainedResource, chainedParameter+"dependent", coverage.getDependentElement().asStringValue());
				resourcemetadataList.add(rDependent);
			}

			// identifier : token
			if (coverage.hasIdentifier()) {

				for (Identifier identifier : coverage.getIdentifier()) {

					Resourcemetadata rIdentifier = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rIdentifier);
				}
			}

			// patient : reference
			if (coverage.hasBeneficiary() && coverage.getBeneficiary().hasReference()) {
				Resourcemetadata rPatient = generateResourcemetadata(resource, chainedResource, chainedParameter+"patient", generateFullLocalReference(coverage.getBeneficiary().getReference(), baseUrl));
				resourcemetadataList.add(rPatient);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rPatientChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "patient", 0, coverage.getBeneficiary().getReference(), null);
					resourcemetadataList.addAll(rPatientChain);
				}
			}

			// payor : reference
			if (coverage.hasPayor()) {

				String payorReference = null;
				List<Resourcemetadata> rPayorChain = null;
				for (Reference payor : coverage.getPayor()) {

					if (payor.hasReference()) {
						payorReference = generateFullLocalReference(payor.getReference(), baseUrl);

						Resourcemetadata rPayor = generateResourcemetadata(resource, chainedResource, chainedParameter+"payor", payorReference);
						resourcemetadataList.add(rPayor);

						if (chainedResource == null) {
							// Add chained parameters for any
							rPayorChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "payor", 0, payor.getReference(), null);
							resourcemetadataList.addAll(rPayorChain);
						}
					}
				}
			}

			// period : date(period)
			if (coverage.hasPeriod()) {
				Resourcemetadata rPeriod = generateResourcemetadata(resource, chainedResource, chainedParameter+"period", utcDateUtil.formatDate(coverage.getPeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(coverage.getPeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(coverage.getPeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), utcDateUtil.formatDate(coverage.getPeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), "PERIOD");
				resourcemetadataList.add(rPeriod);
			}

			// policy-holder : reference
			if (coverage.hasPolicyHolder()) {
				Resourcemetadata rPolicyHolder = generateResourcemetadata(resource, chainedResource, chainedParameter+"policy-holder", generateFullLocalReference(coverage.getPolicyHolder().getReference(), baseUrl));
				resourcemetadataList.add(rPolicyHolder);

				if (chainedResource == null) {
					// Add chained parameters for any
					List<Resourcemetadata> rPolicyHolderChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "policyholder", 0, coverage.getPolicyHolder().getReference(), null);
					resourcemetadataList.addAll(rPolicyHolderChain);
				}
			}

			// status : token
			if (coverage.hasStatus() && coverage.getStatus() != null) {
				Resourcemetadata rStatus = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", coverage.getStatus().toCode(), coverage.getStatus().getSystem());
				resourcemetadataList.add(rStatus);
			}

			// subscriber : reference
			if (coverage.hasSubscriber()) {
				Resourcemetadata rSubscriber = generateResourcemetadata(resource, chainedResource, chainedParameter+"subscriber", generateFullLocalReference(coverage.getSubscriber().getReference(), baseUrl));
				resourcemetadataList.add(rSubscriber);

				if (chainedResource == null) {
					// Add chained parameters for any
					List<Resourcemetadata> rSubscriberChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "subscriber", 0, coverage.getSubscriber().getReference(), null);
					resourcemetadataList.addAll(rSubscriberChain);
				}
			}

			// type : token
			if (coverage.hasType() && coverage.getType().hasCoding()) {

				for (Coding code : coverage.getType().getCoding()) {
					rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"type", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
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
