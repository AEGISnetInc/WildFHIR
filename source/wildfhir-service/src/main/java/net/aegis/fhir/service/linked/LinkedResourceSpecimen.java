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

import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.Specimen;
import org.hl7.fhir.r4.model.Specimen.SpecimenContainerComponent;
import org.hl7.fhir.r4.model.Specimen.SpecimenProcessingComponent;

import net.aegis.fhir.service.ResourceService;

/**
 * @author richard.ettema
 *
 */
public class LinkedResourceSpecimen extends LinkedResourceProxy {

	private Logger log = Logger.getLogger("LinkedResourceSpecimen");

	/* (non-Javadoc)
	 * @see net.aegis.fhir.service.linked.LinkedResourceProxy#getLinkedResources(net.aegis.fhir.service.ResourceService, org.hl7.fhir.instance.model.Resource)
	 */
	@Override
	public List<Resource> getLinkedResources(ResourceService resourceService, Resource containerResource) throws Exception {

		log.fine("[START] LinkedResourceSpecimen.getLinkedResources()");

		List<Resource> linkedResources = new ArrayList<Resource>();

		try {
			// Test for valid container Resource
			if (containerResource != null && containerResource instanceof Specimen) {

				Specimen typedContainerResource = (Specimen) containerResource;

				/*
				 * Specimen linked Resource references
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
				// parent (Specimen)
				if (typedContainerResource.hasParent()) {

					for (Reference parent : typedContainerResource.getParent()) {
						if (parent.hasReference()) {

							ref = parent.getReference();
							linkedResource = this.getLinkedResource(resourceService, ref, "Specimen");

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}

				linkedResource = null;
				// request (ServiceRequest)
				if (typedContainerResource.hasRequest()) {

					for (Reference request : typedContainerResource.getRequest()) {
						if (request.hasReference()) {

							ref = request.getReference();
							linkedResource = this.getLinkedResource(resourceService, ref, "ServiceRequest");

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}

				linkedResource = null;
				// collection.collector
				if (typedContainerResource.hasCollection() && typedContainerResource.getCollection().hasCollector() && typedContainerResource.getCollection().getCollector().hasReference()) {

					ref = typedContainerResource.getCollection().getCollector().getReference();
					linkedResource = this.getLinkedResourceAny(resourceService, ref);

					if (linkedResource != null) {
						linkedResources.add(linkedResource);
					}
				}

				linkedResource = null;
				// processing.additive (Substance)
				if (typedContainerResource.hasProcessing()) {

					for (SpecimenProcessingComponent processing : typedContainerResource.getProcessing()) {
						if (processing.hasAdditive()) {

							for (Reference additive : processing.getAdditive()) {
								if (additive.hasReference()) {

									ref = additive.getReference();
									linkedResource = this.getLinkedResource(resourceService, ref, "Substance");

									if (linkedResource != null) {
										linkedResources.add(linkedResource);
									}
								}
							}
						}
					}
				}

				linkedResource = null;
				// container.additiveReference (Substance)
				if (typedContainerResource.hasContainer()) {

					for (SpecimenContainerComponent additive : typedContainerResource.getContainer()) {
						if (additive.hasAdditiveReference() && additive.getAdditiveReference().hasReference()) {

							ref = additive.getAdditiveReference().getReference();
							linkedResource = this.getLinkedResource(resourceService, ref, "Substance");

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
