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

import org.hl7.fhir.r4.model.Account;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Account.CoverageComponent;
import org.hl7.fhir.r4.model.Account.GuarantorComponent;
import org.hl7.fhir.r4.model.Resource;

/**
 * @author richard.ettema
 *
 */
public class LinkedResourceAccount extends LinkedResourceProxy {

	private Logger log = Logger.getLogger("LinkedResourceAccount");

	/* (non-Javadoc)
	 * @see net.aegis.fhir.service.linked.LinkedResourceProxy#getLinkedResources(net.aegis.fhir.service.ResourceService, org.hl7.fhir.instance.model.Resource)
	 */
	@Override
	public List<Resource> getLinkedResources(ResourceService resourceService, Resource containerResource) throws Exception {

		log.fine("[START] LinkedResourceAccount.getLinkedResources()");

		List<Resource> linkedResources = new ArrayList<Resource>();

		try {
			// Test for valid container Resource
			if (containerResource != null && containerResource instanceof Account) {

				Account typedContainerResource = (Account) containerResource;

				/*
				 * Account linked Resource references
				 */
				String ref = null;
				Resource linkedResource = null;

				// subject
				if (typedContainerResource.hasSubject()) {

					for (Reference subject : typedContainerResource.getSubject()) {
						if (subject.hasReference()) {

							ref = subject.getReference();
							linkedResource = this.getLinkedResourceAny(resourceService, ref);

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}

				linkedResource = null;
				// coverage.coverage (Coverage)
				if (typedContainerResource.hasCoverage()) {

					for (CoverageComponent coverage : typedContainerResource.getCoverage()) {
						if (coverage.hasCoverage() && coverage.getCoverage().hasReference()) {

							ref = coverage.getCoverage().getReference();
							linkedResource = this.getLinkedResource(resourceService, ref, "Coverage");

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}

				linkedResource = null;
				// owner (Organization)
				if (typedContainerResource.hasOwner() && typedContainerResource.getOwner().hasReference()) {

					ref = typedContainerResource.getOwner().getReference();
					linkedResource = this.getLinkedResource(resourceService, ref, "Organization");

					if (linkedResource != null) {
						linkedResources.add(linkedResource);
					}
				}

				linkedResource = null;
				// guarantor.party
				if (typedContainerResource.hasGuarantor()) {

					for (GuarantorComponent guarantor : typedContainerResource.getGuarantor()) {
						if (guarantor.hasParty() && guarantor.getParty().hasReference()) {

							ref = guarantor.getParty().getReference();
							linkedResource = this.getLinkedResourceAny(resourceService, ref);

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}

				linkedResource = null;
				// partOf (Account)
				if (typedContainerResource.hasPartOf() && typedContainerResource.getPartOf().hasReference()) {

					ref = typedContainerResource.getPartOf().getReference();
					linkedResource = this.getLinkedResource(resourceService, ref, "Account");

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
