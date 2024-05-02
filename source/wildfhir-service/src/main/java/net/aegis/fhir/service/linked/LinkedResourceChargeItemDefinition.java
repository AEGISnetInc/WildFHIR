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

import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.ChargeItemDefinition;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;

import net.aegis.fhir.service.ResourceService;

/**
 * @author richard.ettema
 *
 */
public class LinkedResourceChargeItemDefinition extends LinkedResourceProxy {

	private Logger log = Logger.getLogger("LinkedResourceChargeItemDefinition");

	/* (non-Javadoc)
	 * @see net.aegis.fhir.service.linked.LinkedResourceProxy#getLinkedResources(net.aegis.fhir.service.ResourceService, org.hl7.fhir.instance.model.Resource)
	 */
	@Override
	public List<Resource> getLinkedResources(ResourceService resourceService, Resource containerResource) throws Exception {

		log.fine("[START] LinkedResourceChargeItemDefinition.getLinkedResources()");

		List<Resource> linkedResources = new ArrayList<Resource>();

		try {
			// Test for valid container Resource
			if (containerResource != null && containerResource instanceof ChargeItemDefinition) {

				ChargeItemDefinition typedContainerResource = (ChargeItemDefinition) containerResource;

				/*
				 * ChargeItem linked Resource references
				 */
				String ref = null;
				Resource linkedResource = null;

				// partOf (ChargeItemDefinition)
				if (typedContainerResource.hasPartOf()) {

					for (CanonicalType partOf : typedContainerResource.getPartOf()) {
						if (partOf.hasValue()) {

							ref = partOf.getValue();
							linkedResource = this.getLinkedResource(resourceService, ref, "ChargeItemDefinition");

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}

				linkedResource = null;
				// replaces (ChargeItemDefinition)
				if (typedContainerResource.hasReplaces()) {

					for (CanonicalType replaces : typedContainerResource.getReplaces()) {
						if (replaces.hasValue()) {

							ref = replaces.getValue();
							linkedResource = this.getLinkedResource(resourceService, ref, "ChargeItemDefinition");

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}

				linkedResource = null;
				// instance
				if (typedContainerResource.hasInstance()) {

					for (Reference instance : typedContainerResource.getInstance()) {
						if (instance.hasReference()) {

							ref = instance.getReference();
							linkedResource = this.getLinkedResourceAny(resourceService, ref);

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
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
