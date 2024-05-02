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

import org.hl7.fhir.r4.model.ImagingStudy;
import org.hl7.fhir.r4.model.ImagingStudy.ImagingStudySeriesComponent;
import org.hl7.fhir.r4.model.ImagingStudy.ImagingStudySeriesPerformerComponent;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;

import net.aegis.fhir.service.ResourceService;

/**
 * @author richard.ettema
 *
 */
public class LinkedResourceImagingStudy extends LinkedResourceProxy {

	private Logger log = Logger.getLogger("LinkedResourceImagingStudy");

	/* (non-Javadoc)
	 * @see net.aegis.fhir.service.linked.LinkedResourceProxy#getLinkedResources(net.aegis.fhir.service.ResourceService, org.hl7.fhir.instance.model.Resource)
	 */
	@Override
	public List<Resource> getLinkedResources(ResourceService resourceService, Resource containerResource) throws Exception {

		log.fine("[START] LinkedResourceImagingStudy.getLinkedResources()");

		List<Resource> linkedResources = new ArrayList<Resource>();

		try {
			// Test for valid container Resource
			if (containerResource != null && containerResource instanceof ImagingStudy) {

				ImagingStudy typedContainerResource = (ImagingStudy) containerResource;

				/*
				 * ImagingStudy linked Resource references
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
				// basedOn
				if (typedContainerResource.hasBasedOn()) {

					for (Reference basedOn : typedContainerResource.getBasedOn()) {
						if (basedOn.hasReference()) {

							ref = basedOn.getReference();
							linkedResource = this.getLinkedResourceAny(resourceService, ref);

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}

				linkedResource = null;
				// referrer
				if (typedContainerResource.hasReferrer() && typedContainerResource.getReferrer().hasReference()) {

					ref = typedContainerResource.getReferrer().getReference();
					linkedResource = this.getLinkedResourceAny(resourceService, ref);

					if (linkedResource != null) {
						linkedResources.add(linkedResource);
					}
				}

				linkedResource = null;
				// interpreter
				if (typedContainerResource.hasInterpreter()) {

					for (Reference interpreter : typedContainerResource.getInterpreter()) {
						if (interpreter.hasReference()) {

							ref = interpreter.getReference();
							linkedResource = this.getLinkedResourceAny(resourceService, ref);

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}

				linkedResource = null;
				// endpoint (Endpoint)
				if (typedContainerResource.hasEndpoint()) {

					for (Reference endpoint : typedContainerResource.getEndpoint()) {
						if (endpoint.hasReference()) {

							ref = endpoint.getReference();
							linkedResource = this.getLinkedResource(resourceService, ref, "Endpoint");

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}

				linkedResource = null;
				// procedureReference (Procedure)
				if (typedContainerResource.hasProcedureReference() && typedContainerResource.getProcedureReference().hasReference()) {

					ref = typedContainerResource.getProcedureReference().getReference();
					linkedResource = this.getLinkedResource(resourceService, ref, "Procedure");

					if (linkedResource != null) {
						linkedResources.add(linkedResource);
					}
				}

				linkedResource = null;
				// location (Location)
				if (typedContainerResource.hasLocation() && typedContainerResource.getLocation().hasReference()) {

					ref = typedContainerResource.getLocation().getReference();
					linkedResource = this.getLinkedResource(resourceService, ref, "Location");

					if (linkedResource != null) {
						linkedResources.add(linkedResource);
					}
				}

				linkedResource = null;
				// reasonReference
				if (typedContainerResource.hasReasonReference()) {

					for (Reference reasonReference : typedContainerResource.getReasonReference()) {
						if (reasonReference.hasReference()) {

							ref = reasonReference.getReference();
							linkedResource = this.getLinkedResourceAny(resourceService, ref);

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}

				linkedResource = null;
				// series
				if (typedContainerResource.hasSeries()) {

					for (ImagingStudySeriesComponent series : typedContainerResource.getSeries()) {
						// series.endpoint (Endpoint)
						if (series.hasEndpoint()) {

							for (Reference endpoint : series.getEndpoint()) {
								if (endpoint.hasReference()) {

									ref = endpoint.getReference();
									linkedResource = this.getLinkedResource(resourceService, ref, "Endpoint");

									if (linkedResource != null) {
										linkedResources.add(linkedResource);
									}
								}
							}
						}

						// series.performer.actor
						if (series.hasPerformer()) {

							for (ImagingStudySeriesPerformerComponent performer : series.getPerformer()) {
								if (performer.hasActor() && performer.getActor().hasReference()) {

									ref = performer.getActor().getReference();
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
