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

import org.hl7.fhir.r4.model.BiologicallyDerivedProduct;
import org.hl7.fhir.r4.model.BiologicallyDerivedProduct.BiologicallyDerivedProductProcessingComponent;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;

import net.aegis.fhir.service.ResourceService;

/**
 * @author richard.ettema
 *
 */
public class LinkedResourceBiologicallyDerivedProduct extends LinkedResourceProxy {

	private Logger log = Logger.getLogger("LinkedResourceBiologicallyDerivedProduct");

	/* (non-Javadoc)
	 * @see net.aegis.fhir.service.linked.LinkedResourceProxy#getLinkedResources(net.aegis.fhir.service.ResourceService, org.hl7.fhir.instance.model.Resource)
	 */
	@Override
	public List<Resource> getLinkedResources(ResourceService resourceService, Resource containerResource) throws Exception {

		log.fine("[START] LinkedResourceBiologicallyDerivedProduct.getLinkedResources()");

		List<Resource> linkedResources = new ArrayList<Resource>();

		try {
			// Test for valid container Resource
			if (containerResource != null && containerResource instanceof BiologicallyDerivedProduct) {

				BiologicallyDerivedProduct typedContainerResource = (BiologicallyDerivedProduct) containerResource;

				/*
				 * BiologicallyDerivedProduct linked Resource references
				 */
				String ref = null;
				Resource linkedResource = null;

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
				// parent (BiologicallyDerivedProduct)
				if (typedContainerResource.hasParent()) {

					for (Reference parent : typedContainerResource.getParent()) {
						if (parent.hasReference()) {

							ref = parent.getReference();
							linkedResource = this.getLinkedResource(resourceService, ref, "BiologicallyDerivedProduct");

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}

				linkedResource = null;
				// collection
				if (typedContainerResource.hasCollection()) {

					// collection.collector
					if (typedContainerResource.getCollection().hasCollector() && typedContainerResource.getCollection().getCollector().hasReference()) {

						ref = typedContainerResource.getCollection().getCollector().getReference();
						linkedResource = this.getLinkedResourceAny(resourceService, ref);

						if (linkedResource != null) {
							linkedResources.add(linkedResource);
						}
					}

					// collection.source
					if (typedContainerResource.getCollection().hasSource() && typedContainerResource.getCollection().getSource().hasReference()) {

						ref = typedContainerResource.getCollection().getSource().getReference();
						linkedResource = this.getLinkedResourceAny(resourceService, ref);

						if (linkedResource != null) {
							linkedResources.add(linkedResource);
						}
					}
				}

				linkedResource = null;
				// processing.additive (Substance)
				if (typedContainerResource.hasProcessing()) {

					for (BiologicallyDerivedProductProcessingComponent processing : typedContainerResource.getProcessing()) {
						if (processing.hasAdditive() && processing.getAdditive().hasReference()) {

							ref = processing.getAdditive().getReference();
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
