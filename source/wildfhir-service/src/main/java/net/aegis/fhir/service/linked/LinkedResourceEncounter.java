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

import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Encounter.DiagnosisComponent;
import org.hl7.fhir.r4.model.Encounter.EncounterLocationComponent;
import org.hl7.fhir.r4.model.Encounter.EncounterParticipantComponent;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;

import net.aegis.fhir.service.ResourceService;

/**
 * @author richard.ettema
 *
 */
public class LinkedResourceEncounter extends LinkedResourceProxy {

	private Logger log = Logger.getLogger("LinkedResourceEncounter");

	/* (non-Javadoc)
	 * @see net.aegis.fhir.service.linked.LinkedResourceProxy#getLinkedResources(net.aegis.fhir.service.ResourceService, org.hl7.fhir.instance.model.Resource)
	 */
	@Override
	public List<Resource> getLinkedResources(ResourceService resourceService, Resource containerResource) throws Exception {

		log.fine("[START] LinkedResourceEncounter.getLinkedResources()");

		List<Resource> linkedResources = new ArrayList<Resource>();

		try {
			// Test for valid container Resource
			if (containerResource != null && containerResource instanceof Encounter) {

				Encounter typedContainerResource = (Encounter) containerResource;

				/*
				 * Encounter linked Resource references
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
				// episodeOfCare (EpisodeOfCare)
				if (typedContainerResource.hasEpisodeOfCare()) {

					for (Reference episodeOfCare : typedContainerResource.getEpisodeOfCare()) {
						if (episodeOfCare.hasReference()) {

							ref = episodeOfCare.getReference();
							linkedResource = this.getLinkedResource(resourceService, ref, "EpisodeOfCare");

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}

				linkedResource = null;
				// basedOn (ServiceRequest)
				if (typedContainerResource.hasBasedOn()) {

					for (Reference basedOn : typedContainerResource.getBasedOn()) {
						if (basedOn.hasReference()) {

							ref = basedOn.getReference();
							linkedResource = this.getLinkedResource(resourceService, ref, "ServiceRequest");

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}

				linkedResource = null;
				// participant.individual
				if (typedContainerResource.hasParticipant()) {

					for (EncounterParticipantComponent participant : typedContainerResource.getParticipant()) {
						if (participant.hasIndividual() && participant.getIndividual().hasReference()) {

							ref = participant.getIndividual().getReference();
							linkedResource = this.getLinkedResourceAny(resourceService, ref);

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}

				linkedResource = null;
				// appointment (Appointment)
				if (typedContainerResource.hasAppointment()) {

					for (Reference appointment : typedContainerResource.getAppointment()) {
						if (appointment.hasReference()) {

							ref = appointment.getReference();
							linkedResource = this.getLinkedResource(resourceService, ref, "Appointment");

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
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
				// diagnosis.condition
				if (typedContainerResource.hasDiagnosis()) {

					for (DiagnosisComponent diagnosis : typedContainerResource.getDiagnosis()) {
						if (diagnosis.hasCondition() && diagnosis.getCondition().hasReference()) {

							ref = diagnosis.getCondition().getReference();
							linkedResource = this.getLinkedResourceAny(resourceService, ref);

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}

				linkedResource = null;
				// account (Account)
				if (typedContainerResource.hasAccount()) {

					for (Reference account : typedContainerResource.getAccount()) {
						if (account.hasReference()) {

							ref = account.getReference();
							linkedResource = this.getLinkedResource(resourceService, ref, "Account");

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}

				linkedResource = null;
				// hospitalization
				if (typedContainerResource.hasHospitalization()) {

					// hospitalization.origin
					if (typedContainerResource.getHospitalization().hasOrigin() && typedContainerResource.getHospitalization().getOrigin().hasReference()) {

						ref = typedContainerResource.getHospitalization().getOrigin().getReference();
						linkedResource = this.getLinkedResourceAny(resourceService, ref);

						if (linkedResource != null) {
							linkedResources.add(linkedResource);
						}
					}

					// hospitalization.destination
					if (typedContainerResource.getHospitalization().hasDestination() && typedContainerResource.getHospitalization().getDestination().hasReference()) {

						ref = typedContainerResource.getHospitalization().getDestination().getReference();
						linkedResource = this.getLinkedResourceAny(resourceService, ref);

						if (linkedResource != null) {
							linkedResources.add(linkedResource);
						}
					}
				}

				linkedResource = null;
				// location.location (Location)
				if (typedContainerResource.hasLocation()) {

					for (EncounterLocationComponent location : typedContainerResource.getLocation()) {
						if (location.hasLocation() && location.getLocation().hasReference()) {

							ref = location.getLocation().getReference();
							linkedResource = this.getLinkedResource(resourceService, ref, "Location");

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}

				linkedResource = null;
				// serviceProvider (Organization)
				if (typedContainerResource.hasServiceProvider() && typedContainerResource.getServiceProvider().hasReference()) {

					ref = typedContainerResource.getServiceProvider().getReference();
					linkedResource = this.getLinkedResource(resourceService, ref, "Organization");

					if (linkedResource != null) {
						linkedResources.add(linkedResource);
					}
				}

				linkedResource = null;
				// partOf (Encounter)
				if (typedContainerResource.hasPartOf() && typedContainerResource.getPartOf().hasReference()) {

					ref = typedContainerResource.getPartOf().getReference();
					linkedResource = this.getLinkedResource(resourceService, ref, "Encounter");

					if (linkedResource != null) {
						linkedResources.add(linkedResource);
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
