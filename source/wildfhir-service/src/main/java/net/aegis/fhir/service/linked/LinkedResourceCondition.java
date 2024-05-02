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
package net.aegis.fhir.service.linked;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import net.aegis.fhir.service.ResourceService;

import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Condition.ConditionEvidenceComponent;
import org.hl7.fhir.r4.model.Condition.ConditionStageComponent;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;

/**
 * @author richard.ettema
 *
 */
public class LinkedResourceCondition extends LinkedResourceProxy {

	private Logger log = Logger.getLogger("LinkedResourceCondition");

	/* (non-Javadoc)
	 * @see net.aegis.fhir.service.linked.LinkedResourceProxy#getLinkedResources(net.aegis.fhir.service.ResourceService, org.hl7.fhir.instance.model.Resource)
	 */
	@Override
	public List<Resource> getLinkedResources(ResourceService resourceService, Resource containerResource) throws Exception {

		log.fine("[START] LinkedResourceCondition.getLinkedResources()");

		List<Resource> linkedResources = new ArrayList<Resource>();

		try {
			// Test for valid container Resource
			if (containerResource != null && containerResource instanceof Condition) {

				Condition typedContainerResource = (Condition) containerResource;

				/*
				 * Condition linked Resource references
				 */
				String ref = null;
				Resource linkedResource = null;

				// subject
				if (typedContainerResource.hasSubject() && typedContainerResource.getSubject().hasReference()) {

					ref = typedContainerResource.getSubject().getReference();
					linkedResource = this.getLinkedResourceAny(resourceService, ref);

					if (linkedResource != null) {
						linkedResources.add(linkedResource);
					}
				}

				linkedResource = null;
				// encounter (Encounter)
				if (typedContainerResource.hasEncounter() && typedContainerResource.getEncounter().hasReference()) {

					ref = typedContainerResource.getEncounter().getReference();
					linkedResource = this.getLinkedResource(resourceService, ref, "Encounter");

					if (linkedResource != null) {
						linkedResources.add(linkedResource);
					}
				}

				linkedResource = null;
				// recorder
				if (typedContainerResource.hasRecorder() && typedContainerResource.getRecorder().hasReference()) {

					ref = typedContainerResource.getRecorder().getReference();
					linkedResource = this.getLinkedResourceAny(resourceService, ref);

					if (linkedResource != null) {
						linkedResources.add(linkedResource);
					}
				}

				linkedResource = null;
				// asserter
				if (typedContainerResource.hasAsserter() && typedContainerResource.getAsserter().hasReference()) {

					ref = typedContainerResource.getAsserter().getReference();
					linkedResource = this.getLinkedResourceAny(resourceService, ref);

					if (linkedResource != null) {
						linkedResources.add(linkedResource);
					}
				}

				linkedResource = null;
				// stage.assessment
				if (typedContainerResource.hasStage()) {

					for (ConditionStageComponent stage : typedContainerResource.getStage()) {
						if (stage.hasAssessment()) {

							for (Reference assessment : stage.getAssessment()) {
								if (assessment.hasReference()) {

									ref = assessment.getReference();
									linkedResource = this.getLinkedResourceAny(resourceService, ref);

									if (linkedResource != null) {
										linkedResources.add(linkedResource);
									}
								}
							}
						}
					}
				}

				linkedResource = null;
				// evidence
				if (typedContainerResource.hasEvidence()) {

					for (ConditionEvidenceComponent evidence : typedContainerResource.getEvidence()) {
						// evidence.detail
						if (evidence.hasDetail()) {

							for (Reference detail : evidence.getDetail()) {
								if (detail.hasReference()) {

									ref = detail.getReference();
									linkedResource = this.getLinkedResourceAny(resourceService, ref);

									if (linkedResource != null) {
										linkedResources.add(linkedResource);
									}
								}
							}
						}
					}
				}
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
        }

		return linkedResources;
	}

}
