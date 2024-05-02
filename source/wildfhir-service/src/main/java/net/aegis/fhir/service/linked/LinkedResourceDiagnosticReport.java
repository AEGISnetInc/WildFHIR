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

import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.r4.model.DiagnosticReport.DiagnosticReportMediaComponent;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;

import net.aegis.fhir.service.ResourceService;

/**
 * @author richard.ettema
 *
 */
public class LinkedResourceDiagnosticReport extends LinkedResourceProxy {

	private Logger log = Logger.getLogger("LinkedResourceDiagnosticReport");

	/* (non-Javadoc)
	 * @see net.aegis.fhir.service.linked.LinkedResourceProxy#getLinkedResources(net.aegis.fhir.service.ResourceService, org.hl7.fhir.instance.model.Resource)
	 */
	@Override
	public List<Resource> getLinkedResources(ResourceService resourceService, Resource containerResource) throws Exception {

		log.fine("[START] LinkedResourceDiagnosticReport.getLinkedResources()");

		List<Resource> linkedResources = new ArrayList<Resource>();

		try {
			// Test for valid container Resource
			if (containerResource != null && containerResource instanceof DiagnosticReport) {

				DiagnosticReport typedContainerResource = (DiagnosticReport) containerResource;

				/*
				 * DiagnosticReport linked Resource references
				 */
				String ref = null;
				Resource linkedResource = null;

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
				// performer
				if (typedContainerResource.hasPerformer()) {

					for (Reference performer : typedContainerResource.getPerformer()) {
						if (performer.hasReference()) {

							ref = performer.getReference();
							linkedResource = this.getLinkedResourceAny(resourceService, ref);

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}

				linkedResource = null;
				// resultsInterpreter
				if (typedContainerResource.hasResultsInterpreter()) {

					for (Reference resultsInterpreter : typedContainerResource.getResultsInterpreter()) {
						if (resultsInterpreter.hasReference()) {

							ref = resultsInterpreter.getReference();
							linkedResource = this.getLinkedResourceAny(resourceService, ref);

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}

				linkedResource = null;
				// specimen (Specimen)
				if (typedContainerResource.hasSpecimen()) {

					for (Reference specimen : typedContainerResource.getSpecimen()) {
						if (specimen.hasReference()) {

							ref = specimen.getReference();
							linkedResource = this.getLinkedResource(resourceService, ref, "Specimen");

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}

				linkedResource = null;
				// result (Observation)
				if (typedContainerResource.hasResult()) {

					for (Reference result : typedContainerResource.getResult()) {
						if (result.hasReference()) {

							ref = result.getReference();
							linkedResource = this.getLinkedResource(resourceService, ref, "Observation");

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}

				linkedResource = null;
				// imagingStudy (ImagingStudy)
				if (typedContainerResource.hasImagingStudy()) {

					for (Reference imagingStudy : typedContainerResource.getImagingStudy()) {
						if (imagingStudy.hasReference()) {

							ref = imagingStudy.getReference();
							linkedResource = this.getLinkedResource(resourceService, ref, "ImagingStudy");

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}

				linkedResource = null;
				// media.link
				if (typedContainerResource.hasMedia()) {

					for (DiagnosticReportMediaComponent media : typedContainerResource.getMedia()) {
						if (media.hasLink() && media.getLink().hasReference()) {

							ref = media.getLink().getReference();
							linkedResource = this.getLinkedResource(resourceService, ref, "Media");

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
