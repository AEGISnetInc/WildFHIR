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
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Task;

/**
 * @author richard.ettema
 *
 */
public class ResourcemetadataTask extends ResourcemetadataProxy {

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
        ByteArrayInputStream iTask = null;
        Resourcemetadata rMetadata = null;
        List<Resourcemetadata> rMetadataChain = null;

		try {
            // Extract and convert the resource contents to a Task object
			if (chainedResource != null) {
				iTask = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iTask = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
            Task task = (Task) xmlP.parse(iTask);
            iTask.close();

			/*
             * Create new Resourcemetadata objects for each Task metadata value and add to the resourcemetadataList
			 */

			// Add Resource common parameters
            rMetadataChain = this.generateResourcemetadataTagList(resource, task, chainedParameter);
			resourcemetadataList.addAll(rMetadataChain);

			// authored-on : date
			if (task.hasAuthoredOn()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"authored-on", utcDateUtil.formatDate(task.getAuthoredOn(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(task.getAuthoredOn(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rMetadata);
			}

			// based-on : reference
			if (task.hasBasedOn()) {

				for (Reference basedOn : task.getBasedOn()) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "based-on", 0, basedOn, null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// business-status : token
			if (task.hasBusinessStatus() && task.getBusinessStatus().hasCoding()) {

				for (Coding code : task.getBusinessStatus().getCoding()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"business-status", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rMetadata);
				}
			}

			// code : token
			if (task.hasCode() && task.getCode().hasCoding()) {

				for (Coding code : task.getCode().getCoding()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"code", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rMetadata);
				}
			}

			// encounter : reference
			if (task.hasEncounter()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "encounter", 0, task.getEncounter(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// focus : reference
			if (task.hasFocus()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "focus", 0, task.getFocus(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// group-identifier : token
			if (task.hasGroupIdentifier()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"group-identifier", task.getGroupIdentifier().getValue(), task.getGroupIdentifier().getSystem(), null, ServicesUtil.INSTANCE.getTextValue(task.getGroupIdentifier()));
				resourcemetadataList.add(rMetadata);
			}

			// identifier : token
			if (task.hasIdentifier()) {

				for (Identifier identifier : task.getIdentifier()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rMetadata);
				}
			}

			// intent : token
			if (task.hasIntent() && task.getIntent() != null) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"intent", task.getIntent().toCode(), task.getIntent().getSystem());
				resourcemetadataList.add(rMetadata);
			}

			// modified : date
			if (task.hasLastModified()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"modified", utcDateUtil.formatDate(task.getLastModified(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(task.getLastModified(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rMetadata);
			}

			// owner : reference
			if (task.hasOwner()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "owner", 0, task.getOwner(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// part-of : reference
			if (task.hasPartOf()) {

				for (Reference partOf : task.getPartOf()) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "part-of", 0, partOf, null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// subject : reference
			if (task.hasFor() && task.getFor().hasReference()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "subject", 0, task.getFor(), null);
				resourcemetadataList.addAll(rMetadataChain);

				// patient : reference
				if ((task.getFor().hasReference() && task.getFor().getReference().indexOf("Patient") >= 0)
						|| (task.getFor().hasType() && task.getFor().getType().equals("Patient"))) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "patient", 0, task.getFor(), null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// performer : token
			if (task.hasPerformerType()) {
				for (CodeableConcept performer : task.getPerformerType()) {

					if (performer.hasCoding()) {
						for (Coding code : performer.getCoding()) {
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"performer", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rMetadata);
						}
					}
				}
			}

			// period : date(period)
			if (task.hasExecutionPeriod()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"period", utcDateUtil.formatDate(task.getExecutionPeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(task.getExecutionPeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT), null, null, "PERIOD");
				resourcemetadataList.add(rMetadata);
			}

			// priority : token
			if (task.hasPriority() && task.getPriority() != null) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"priority", task.getPriority().toCode(), task.getPriority().getSystem());
				resourcemetadataList.add(rMetadata);
			}

			// requester : reference
			if (task.hasRequester()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "requester", 0, task.getRequester(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// status : token
			if (task.hasStatus() && task.getStatus() != null) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", task.getStatus().toCode(), task.getStatus().getSystem());
				resourcemetadataList.add(rMetadata);
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		} finally {
	        rMetadata = null;
	        rMetadataChain = null;
            if (iTask != null) {
                try {
                	iTask.close();
                } catch (IOException ioe) {
                	ioe.printStackTrace();
                }
            }
		}

		return resourcemetadataList;
	}

}
