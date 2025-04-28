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
        Resourcemetadata rMetadata = null;
        List<Resourcemetadata> rMetadataChain = null;

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

			// Add Resource common parameters
            rMetadataChain = this.generateResourcemetadataTagList(resource, serviceRequest, chainedParameter);
			resourcemetadataList.addAll(rMetadataChain);

			// authored : date
			if (serviceRequest.hasAuthoredOn()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"authored", utcDateUtil.formatDate(serviceRequest.getAuthoredOn(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(serviceRequest.getAuthoredOn(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rMetadata);
			}

			// based-on : reference
			if (serviceRequest.hasBasedOn()) {

				for (Reference basedOn : serviceRequest.getBasedOn()) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "based-on", 0, basedOn, null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// body-site : token
			if (serviceRequest.hasBodySite()) {
				for (CodeableConcept bodySite : serviceRequest.getBodySite()) {

					if (bodySite.hasCoding()) {
						for (Coding code : bodySite.getCoding()) {
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"body-site", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rMetadata);
						}
					}
				}
			}

			// category : token
			if (serviceRequest.hasCategory()) {
				for (CodeableConcept category : serviceRequest.getCategory()) {

					if (category.hasCoding()) {
						for (Coding code : category.getCoding()) {
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"category", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rMetadata);
						}
					}
				}
			}

			// code : token
			if (serviceRequest.hasCode() && serviceRequest.getCode().hasCoding()) {

				for (Coding code : serviceRequest.getCode().getCoding()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"code", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rMetadata);
				}
			}

			// encounter : reference
			if (serviceRequest.hasEncounter()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "encounter", 0, serviceRequest.getEncounter(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// identifier : token
			if (serviceRequest.hasIdentifier()) {

				for (Identifier identifier : serviceRequest.getIdentifier()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rMetadata);
				}
			}

			// instantiates-canonical : reference - instantiates is a Canonical, no Reference.identifier
			if (serviceRequest.hasInstantiatesCanonical()) {

				for (CanonicalType instantiates : serviceRequest.getInstantiatesCanonical()) {
					String objectReference = generateFullLocalReference(instantiates.asStringValue(), baseUrl);

					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"instantiates-canonical", objectReference);
					resourcemetadataList.add(rMetadata);

					if (chainedResource == null) {
						// Add chained parameters
						rMetadataChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "instantiates-canonical", 0, instantiates.asStringValue(), null);
						resourcemetadataList.addAll(rMetadataChain);
					}
				}
			}

			// instantiates-uri : uri
			if (serviceRequest.hasInstantiatesUri()) {

				for (UriType instantiates : serviceRequest.getInstantiatesUri()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"instantiates-uri", instantiates.asStringValue());
					resourcemetadataList.add(rMetadata);
				}
			}

			// intent : token
			if (serviceRequest.hasIntent() && serviceRequest.getIntent() != null) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"intent", serviceRequest.getIntent().toCode(), serviceRequest.getIntent().getSystem());
				resourcemetadataList.add(rMetadata);
			}

			// occurrence : date(period)
			if (serviceRequest.hasOccurrenceDateTimeType()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"occurrence", utcDateUtil.formatDate(serviceRequest.getOccurrenceDateTimeType().getValue(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(serviceRequest.getOccurrenceDateTimeType().getValue(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rMetadata);
			}
			else if (serviceRequest.hasOccurrencePeriod()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"occurrence", utcDateUtil.formatDate(serviceRequest.getOccurrencePeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(serviceRequest.getOccurrencePeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(serviceRequest.getOccurrencePeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), utcDateUtil.formatDate(serviceRequest.getOccurrencePeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), "PERIOD");
				resourcemetadataList.add(rMetadata);
			}

			// performer : reference
			if (serviceRequest.hasPerformer()) {

				for (Reference performer : serviceRequest.getPerformer()) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "performer", 0, performer, null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// performer-type : token
			if (serviceRequest.hasPerformerType() && serviceRequest.getPerformerType().hasCoding()) {

				for (Coding code : serviceRequest.getPerformerType().getCoding()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"performer-type", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rMetadata);
				}
			}

			// priority : token
			if (serviceRequest.hasPriority() && serviceRequest.getPriority() != null) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"priority", serviceRequest.getPriority().toCode(), serviceRequest.getPriority().getSystem());
				resourcemetadataList.add(rMetadata);
			}

			// replaces : reference
			if (serviceRequest.hasReplaces()) {

				for (Reference replaces : serviceRequest.getReplaces()) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "replaces", 0, replaces, null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// requester : reference
			if (serviceRequest.hasRequester()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "requester", 0, serviceRequest.getRequester(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// requisition : identifier
			if (serviceRequest.hasRequisition()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", serviceRequest.getRequisition().getValue(), serviceRequest.getRequisition().getSystem());
				resourcemetadataList.add(rMetadata);
			}

			// specimen : reference
			if (serviceRequest.hasSpecimen()) {

				for (Reference specimen : serviceRequest.getSpecimen()) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "specimen", 0, specimen, null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// status : token
			if (serviceRequest.hasStatus() && serviceRequest.getStatus() != null) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", serviceRequest.getStatus().toCode(), serviceRequest.getStatus().getSystem());
				resourcemetadataList.add(rMetadata);
			}

			// subject : reference
			if (serviceRequest.hasSubject()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "subject", 0, serviceRequest.getSubject(), null);
				resourcemetadataList.addAll(rMetadataChain);

				// patient : reference
				if ((serviceRequest.getSubject().hasReference() && serviceRequest.getSubject().getReference().indexOf("Patient") >= 0)
						|| (serviceRequest.getSubject().hasType() && serviceRequest.getSubject().getType().equals("Patient"))) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "patient", 0, serviceRequest.getSubject(), null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		} finally {
	        rMetadata = null;
	        rMetadataChain = null;
            if (iServiceRequest != null) {
                try {
                	iServiceRequest.close();
                } catch (IOException ioe) {
                	ioe.printStackTrace();
                }
            }
		}

		return resourcemetadataList;
	}

}
