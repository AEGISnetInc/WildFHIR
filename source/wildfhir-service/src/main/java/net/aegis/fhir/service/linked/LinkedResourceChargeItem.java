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
import org.hl7.fhir.r4.model.ChargeItem;
import org.hl7.fhir.r4.model.ChargeItem.ChargeItemPerformerComponent;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;

import net.aegis.fhir.service.ResourceService;

/**
 * @author richard.ettema
 *
 */
public class LinkedResourceChargeItem extends LinkedResourceProxy {

	private Logger log = Logger.getLogger("LinkedResourceChargeItem");

	/* (non-Javadoc)
	 * @see net.aegis.fhir.service.linked.LinkedResourceProxy#getLinkedResources(net.aegis.fhir.service.ResourceService, org.hl7.fhir.instance.model.Resource)
	 */
	@Override
	public List<Resource> getLinkedResources(ResourceService resourceService, Resource containerResource) throws Exception {

		log.fine("[START] LinkedResourceChargeItem.getLinkedResources()");

		List<Resource> linkedResources = new ArrayList<Resource>();

		try {
			// Test for valid container Resource
			if (containerResource != null && containerResource instanceof ChargeItem) {

				ChargeItem typedContainerResource = (ChargeItem) containerResource;

				/*
				 * ChargeItem linked Resource references
				 */
				String ref = null;
				Resource linkedResource = null;

				// definitionCanonical
				if (typedContainerResource.hasDefinitionCanonical()) {

					for (CanonicalType instantiatesCanonical : typedContainerResource.getDefinitionCanonical()) {
						if (instantiatesCanonical.hasValue()) {

							ref = instantiatesCanonical.getValue();
							linkedResource = this.getLinkedResource(resourceService, ref, "ChargeItemDefinition");

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}

				linkedResource = null;
				// partOf (ChargeItem)
				if (typedContainerResource.hasPartOf()) {

					for (Reference partOf : typedContainerResource.getPartOf()) {
						if (partOf.hasReference()) {

							ref = partOf.getReference();
							linkedResource = this.getLinkedResource(resourceService, ref, "ChargeItem");

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
				// context
				if (typedContainerResource.hasContext() && typedContainerResource.getContext().hasReference()) {

					ref = typedContainerResource.getContext().getReference();
					linkedResource = this.getLinkedResourceAny(resourceService, ref);

					if (linkedResource != null) {
						linkedResources.add(linkedResource);
					}
				}

				linkedResource = null;
				// performer
				if (typedContainerResource.hasPerformer()) {

					for (ChargeItemPerformerComponent participant : typedContainerResource.getPerformer()) {
						// participant.actor
						if (participant.hasActor() && participant.getActor().hasReference()) {

							ref = participant.getActor().getReference();
							linkedResource = this.getLinkedResourceAny(resourceService, ref);

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}

				linkedResource = null;
				// performingOrganization (Organization)
				if (typedContainerResource.hasPerformingOrganization() && typedContainerResource.getPerformingOrganization().hasReference()) {

					ref = typedContainerResource.getPerformingOrganization().getReference();
					linkedResource = this.getLinkedResource(resourceService, ref, "Organization");

					if (linkedResource != null) {
						linkedResources.add(linkedResource);
					}
				}

				linkedResource = null;
				// requestingOrganization (Organization)
				if (typedContainerResource.hasRequestingOrganization() && typedContainerResource.getRequestingOrganization().hasReference()) {

					ref = typedContainerResource.getRequestingOrganization().getReference();
					linkedResource = this.getLinkedResource(resourceService, ref, "Organization");

					if (linkedResource != null) {
						linkedResources.add(linkedResource);
					}
				}

				linkedResource = null;
				// costCenter (Organization)
				if (typedContainerResource.hasCostCenter() && typedContainerResource.getCostCenter().hasReference()) {

					ref = typedContainerResource.getCostCenter().getReference();
					linkedResource = this.getLinkedResource(resourceService, ref, "Organization");

					if (linkedResource != null) {
						linkedResources.add(linkedResource);
					}
				}

				linkedResource = null;
				// enterer
				if (typedContainerResource.hasEnterer() && typedContainerResource.getEnterer().hasReference()) {

					ref = typedContainerResource.getEnterer().getReference();
					linkedResource = this.getLinkedResourceAny(resourceService, ref);

					if (linkedResource != null) {
						linkedResources.add(linkedResource);
					}
				}

				linkedResource = null;
				// service
				if (typedContainerResource.hasService()) {

					for (Reference service : typedContainerResource.getService()) {
						if (service.hasReference()) {

							ref = service.getReference();
							linkedResource = this.getLinkedResourceAny(resourceService, ref);

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}

				linkedResource = null;
				// productReference
				if (typedContainerResource.hasProductReference() && typedContainerResource.getProductReference().hasReference()) {

					ref = typedContainerResource.getProductReference().getReference();
					linkedResource = this.getLinkedResourceAny(resourceService, ref);

					if (linkedResource != null) {
						linkedResources.add(linkedResource);
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
				// supportingInformation
				if (typedContainerResource.hasSupportingInformation()) {

					for (Reference supportingInformation : typedContainerResource.getSupportingInformation()) {
						if (supportingInformation.hasReference()) {

							ref = supportingInformation.getReference();
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
