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
import org.hl7.fhir.r4.model.AdverseEvent;
import org.hl7.fhir.r4.model.AdverseEvent.AdverseEventSuspectEntityComponent;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
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
public class ResourcemetadataAdverseEvent extends ResourcemetadataProxy {

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
        ByteArrayInputStream iAdverseEvent = null;
        Resourcemetadata rMetadata = null;
        List<Resourcemetadata> rMetadataChain = null;

		try {
			// Extract and convert the resource contents to a AdverseEvent object
			if (chainedResource != null) {
				iAdverseEvent = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iAdverseEvent = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
			AdverseEvent adverseEvent = (AdverseEvent) xmlP.parse(iAdverseEvent);
			iAdverseEvent.close();

			/*
			 * Create new Resourcemetadata objects for each AdverseEvent metadata value and add to the resourcemetadataList
			 */

			// Add Resource common parameters
            rMetadataChain = this.generateResourcemetadataTagList(resource, adverseEvent, chainedParameter);
			resourcemetadataList.addAll(rMetadataChain);

			// actuality : token
			if (adverseEvent.hasActuality() && adverseEvent.getActuality() != null) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"actuality", adverseEvent.getActuality().toCode(), adverseEvent.getActuality().getSystem());
				resourcemetadataList.add(rMetadata);
			}

			// category : token
			if (adverseEvent.hasCategory()) {

				for (CodeableConcept category : adverseEvent.getCategory()) {
					if (category.hasCoding()) {
						for (Coding code : category.getCoding()) {
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"category", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rMetadata);
						}
					}
				}
			}

			// date : date
			if (adverseEvent.hasDate()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"date", utcDateUtil.formatDate(adverseEvent.getDate(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(adverseEvent.getDate(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rMetadata);
			}

			// event : token
			if (adverseEvent.hasEvent() && adverseEvent.getEvent().hasCoding()) {

				for (Coding code : adverseEvent.getEvent().getCoding()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"event", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rMetadata);
				}
			}

			// identifier : token
			if (adverseEvent.hasIdentifier()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", adverseEvent.getIdentifier().getValue(), adverseEvent.getIdentifier().getSystem(), null, ServicesUtil.INSTANCE.getTextValue(adverseEvent.getIdentifier()));
				resourcemetadataList.add(rMetadata);
			}

			// location : reference
			if (adverseEvent.hasLocation()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "location", 0, adverseEvent.getLocation(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// recorder : reference
			if (adverseEvent.hasRecorder()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "recorder", 0, adverseEvent.getRecorder(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// resultingcondition : reference
			if (adverseEvent.hasResultingCondition()) {

				for (Reference resultingcondition : adverseEvent.getResultingCondition()) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "resultingcondition", 0, resultingcondition, null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// seriousness : token
			if (adverseEvent.hasSeriousness() && adverseEvent.getSeriousness().hasCoding()) {

				for (Coding code : adverseEvent.getSeriousness().getCoding()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"seriousness", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rMetadata);
				}
			}

			// severity : token
			if (adverseEvent.hasSeverity() && adverseEvent.getSeverity().hasCoding()) {

				for (Coding code : adverseEvent.getSeverity().getCoding()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"severity", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rMetadata);
				}
			}

			// study : reference
			if (adverseEvent.hasStudy()) {

				for (Reference study : adverseEvent.getStudy()) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "study", 0, study, null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// subject : reference
			if (adverseEvent.hasSubject()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "subject", 0, adverseEvent.getSubject(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// substance : reference
			if (adverseEvent.hasSuspectEntity()) {

				for (AdverseEventSuspectEntityComponent suspectEntity : adverseEvent.getSuspectEntity()) {

					if (suspectEntity.hasInstance()) {
						rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "substance", 0, suspectEntity.getInstance(), null);
						resourcemetadataList.addAll(rMetadataChain);
					}
				}
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
        } finally {
	        rMetadata = null;
	        rMetadataChain = null;
            if (iAdverseEvent != null) {
                try {
                	iAdverseEvent.close();
                } catch (IOException ioe) {
                	ioe.printStackTrace();
                }
            }
		}

		return resourcemetadataList;
	}

}
