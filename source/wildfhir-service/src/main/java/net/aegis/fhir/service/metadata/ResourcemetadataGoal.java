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
import org.hl7.fhir.r4.model.Goal;
import org.hl7.fhir.r4.model.Goal.GoalTargetComponent;
import org.hl7.fhir.r4.model.Identifier;

/**
 * @author richard.ettema
 *
 */
public class ResourcemetadataGoal extends ResourcemetadataProxy {

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
        ByteArrayInputStream iGoal = null;
        Resourcemetadata rMetadata = null;
        List<Resourcemetadata> rMetadataChain = null;

		try {
            // Extract and convert the resource contents to a Goal object
			if (chainedResource != null) {
				iGoal = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iGoal = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
            Goal goal = (Goal) xmlP.parse(iGoal);
            iGoal.close();

			/*
             * Create new Resourcemetadata objects for each Goal metadata value and add to the resourcemetadataList
			 */

			// Add Resource common parameters
            rMetadataChain = this.generateResourcemetadataTagList(resource, goal, chainedParameter);
			resourcemetadataList.addAll(rMetadataChain);

			// achievement-status : token
			if (goal.hasAchievementStatus() && goal.getAchievementStatus().hasCoding()) {

				for (Coding code : goal.getAchievementStatus().getCoding()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"achievement-status", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rMetadata);
				}
			}

			// category : token
			if (goal.hasCategory()) {
				for (CodeableConcept category : goal.getCategory()) {

					if (category.hasCoding()) {
						for (Coding code : category.getCoding()) {
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"category", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rMetadata);
						}
					}
				}
			}

			// identifier : token
			if (goal.hasIdentifier()) {

				for (Identifier identifier : goal.getIdentifier()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rMetadata);
				}
			}

			// lifecycle-status : token
			if (goal.hasLifecycleStatus() && goal.getLifecycleStatus() != null) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"lifecycle-status", goal.getLifecycleStatus().toCode(), goal.getLifecycleStatus().getSystem());
				resourcemetadataList.add(rMetadata);
			}

			// subject : reference
			if (goal.hasSubject()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "subject", 0, goal.getSubject(), null);
				resourcemetadataList.addAll(rMetadataChain);

				// patient : reference
				if ((goal.getSubject().hasReference() && goal.getSubject().getReference().indexOf("Patient") >= 0)
						|| (goal.getSubject().hasType() && goal.getSubject().getType().equals("Patient"))) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "patient", 0, goal.getSubject(), null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// start-date : date
			if (goal.hasStartDateType()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"start-date", utcDateUtil.formatDate(goal.getStartDateType().getValue(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(goal.getStartDateType().getValue(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rMetadata);
			}

			// target-date : date
			if (goal.hasTarget()) {
				for (GoalTargetComponent target : goal.getTarget()) {

					if (target.hasDueDateType()) {
						rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"target-date", utcDateUtil.formatDate(target.getDueDateType().getValue(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(target.getDueDateType().getValue(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
						resourcemetadataList.add(rMetadata);
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
            if (iGoal != null) {
                try {
                	iGoal.close();
                } catch (IOException ioe) {
                	ioe.printStackTrace();
                }
            }
		}

		return resourcemetadataList;
	}

}
