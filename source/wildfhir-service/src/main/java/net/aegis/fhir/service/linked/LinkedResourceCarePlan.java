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
import org.hl7.fhir.r4.model.CarePlan;
import org.hl7.fhir.r4.model.CarePlan.CarePlanActivityComponent;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;

import net.aegis.fhir.service.ResourceService;

/**
 * @author richard.ettema
 *
 */
public class LinkedResourceCarePlan extends LinkedResourceProxy {

	private Logger log = Logger.getLogger("LinkedResourceCarePlan");

	/* (non-Javadoc)
	 * @see net.aegis.fhir.service.linked.LinkedResourceProxy#getLinkedResources(net.aegis.fhir.service.ResourceService, org.hl7.fhir.instance.model.Resource)
	 */
	@Override
	public List<Resource> getLinkedResources(ResourceService resourceService, Resource containerResource) throws Exception {

		log.fine("[START] LinkedResourceCarePlan.getLinkedResources()");

		List<Resource> linkedResources = new ArrayList<Resource>();

		try {
			// Test for valid container Resource
			if (containerResource != null && containerResource instanceof CarePlan) {

				CarePlan typedContainerResource = (CarePlan) containerResource;

				/*
				 * CarePlan linked Resource references
				 */
				String ref = null;
				Resource linkedResource = null;

				// instantiatesCanonical
				if (typedContainerResource.hasInstantiatesCanonical()) {

					for (CanonicalType instantiatesCanonical : typedContainerResource.getInstantiatesCanonical()) {
						if (instantiatesCanonical.hasValue()) {

							ref = instantiatesCanonical.getValue();
							linkedResource = this.getLinkedResourceAny(resourceService, ref);

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}

				linkedResource = null;
				// basedOn (CarePlan)
				if (typedContainerResource.hasBasedOn()) {

					for (Reference basedOn : typedContainerResource.getBasedOn()) {
						if (basedOn.hasReference()) {

							ref = basedOn.getReference();
							linkedResource = this.getLinkedResource(resourceService, ref, "CarePlan");

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}

				linkedResource = null;
				// replaces (CarePlan)
				if (typedContainerResource.hasReplaces()) {

					for (Reference replaces : typedContainerResource.getReplaces()) {
						if (replaces.hasReference()) {

							ref = replaces.getReference();
							linkedResource = this.getLinkedResource(resourceService, ref, "CarePlan");

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}

				linkedResource = null;
				// partOf (CarePlan)
				if (typedContainerResource.hasPartOf()) {

					for (Reference partOf : typedContainerResource.getPartOf()) {
						if (partOf.hasReference()) {

							ref = partOf.getReference();
							linkedResource = this.getLinkedResource(resourceService, ref, "CarePlan");

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
				// author
				if (typedContainerResource.hasAuthor() && typedContainerResource.getAuthor().hasReference()) {

					ref = typedContainerResource.getAuthor().getReference();
					linkedResource = this.getLinkedResourceAny(resourceService, ref);

					if (linkedResource != null) {
						linkedResources.add(linkedResource);
					}
				}

				linkedResource = null;
				// contributor
				if (typedContainerResource.hasContributor()) {

					for (Reference contributor : typedContainerResource.getContributor()) {
						if (contributor.hasReference()) {

							ref = contributor.getReference();
							linkedResource = this.getLinkedResourceAny(resourceService, ref);

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}

				linkedResource = null;
				// careTeam (CareTeam)
				if (typedContainerResource.hasCareTeam()) {

					for (Reference careTeam : typedContainerResource.getCareTeam()) {
						if (careTeam.hasReference()) {

							ref = careTeam.getReference();
							linkedResource = this.getLinkedResource(resourceService, ref, "CareTeam");

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}

				linkedResource = null;
				// addresses (Condition)
				if (typedContainerResource.hasAddresses()) {

					for (Reference addresses : typedContainerResource.getAddresses()) {
						if (addresses.hasReference()) {

							ref = addresses.getReference();
							linkedResource = this.getLinkedResource(resourceService, ref, "Condition");

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}

				linkedResource = null;
				// supportingInfo
				if (typedContainerResource.hasSupportingInfo()) {

					for (Reference supportingInfo : typedContainerResource.getSupportingInfo()) {
						if (supportingInfo.hasReference()) {

							ref = supportingInfo.getReference();
							linkedResource = this.getLinkedResourceAny(resourceService, ref);

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}

				linkedResource = null;
				// goal (Goal)
				if (typedContainerResource.hasGoal()) {

					for (Reference goal : typedContainerResource.getGoal()) {
						if (goal.hasReference()) {

							ref = goal.getReference();
							linkedResource = this.getLinkedResource(resourceService, ref, "Goal");

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}

				linkedResource = null;
				// activity
				if (typedContainerResource.hasActivity()) {

					for (CarePlanActivityComponent activity : typedContainerResource.getActivity()) {

						// activity.outcomeReference
						if (activity.hasOutcomeReference()) {

							for (Reference outcomeReference : activity.getOutcomeReference()) {
								if (outcomeReference.hasReference()) {

									ref = outcomeReference.getReference();
									linkedResource = this.getLinkedResourceAny(resourceService, ref);

									if (linkedResource != null) {
										linkedResources.add(linkedResource);
									}
								}
							}
						}

						// activity.reference
						if (activity.hasReference() && activity.getReference().hasReference()) {

							ref = activity.getReference().getReference();
							linkedResource = this.getLinkedResourceAny(resourceService, ref);

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}

						// activity.detail
						if (activity.hasDetail()) {

							// activity.detail.instantiatesCanonical
							if (activity.getDetail().hasInstantiatesCanonical()) {

								for (CanonicalType instantiatesCanonical : activity.getDetail().getInstantiatesCanonical()) {
									if (instantiatesCanonical.hasValue()) {

										ref = instantiatesCanonical.getValue();
										linkedResource = this.getLinkedResourceAny(resourceService, ref);

										if (linkedResource != null) {
											linkedResources.add(linkedResource);
										}
									}
								}
							}

							// activity.detail.reasonReference
							if (activity.getDetail().hasReasonReference()) {

								for (Reference reasonReference : activity.getDetail().getReasonReference()) {
									if (reasonReference.hasReference()) {

										ref = reasonReference.getReference();
										linkedResource = this.getLinkedResourceAny(resourceService, ref);

										if (linkedResource != null) {
											linkedResources.add(linkedResource);
										}
									}
								}
							}

							// activity.detail.goal
							if (activity.getDetail().hasGoal()) {

								for (Reference goal : activity.getDetail().getGoal()) {
									if (goal.hasReference()) {

										ref = goal.getReference();
										linkedResource = this.getLinkedResource(resourceService, ref, "Goal");

										if (linkedResource != null) {
											linkedResources.add(linkedResource);
										}
									}
								}
							}

							// activity.detail.location
							if (activity.getDetail().hasLocation() && activity.getDetail().getLocation().hasReference()) {

								ref = activity.getDetail().getLocation().getReference();
								linkedResource = this.getLinkedResource(resourceService, ref, "Location");

								if (linkedResource != null) {
									linkedResources.add(linkedResource);
								}
							}

							// activity.detail.performer
							if (activity.getDetail().hasPerformer()) {

								for (Reference performer : activity.getDetail().getPerformer()) {
									if (performer.hasReference()) {

										ref = performer.getReference();
										linkedResource = this.getLinkedResourceAny(resourceService, ref);

										if (linkedResource != null) {
											linkedResources.add(linkedResource);
										}
									}
								}
							}

							// activity.detail.productReference
							if (activity.getDetail().hasProductReference() && activity.getDetail().getProductReference().hasReference()) {

								ref = activity.getDetail().getProductReference().getReference();
								linkedResource = this.getLinkedResourceAny(resourceService, ref);

								if (linkedResource != null) {
									linkedResources.add(linkedResource);
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
