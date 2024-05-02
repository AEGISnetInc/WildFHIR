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

import org.hl7.fhir.r4.model.PaymentReconciliation;
import org.hl7.fhir.r4.model.PaymentReconciliation.DetailsComponent;
import org.hl7.fhir.r4.model.Resource;

import net.aegis.fhir.service.ResourceService;

/**
 * @author richard.ettema
 *
 */
public class LinkedResourcePaymentReconciliation extends LinkedResourceProxy {

	private Logger log = Logger.getLogger("LinkedResourcePaymentReconciliation");

	/* (non-Javadoc)
	 * @see net.aegis.fhir.service.linked.LinkedResourceProxy#getLinkedResources(net.aegis.fhir.service.ResourceService, org.hl7.fhir.instance.model.Resource)
	 */
	@Override
	public List<Resource> getLinkedResources(ResourceService resourceService, Resource containerResource) throws Exception {

		log.fine("[START] LinkedResourcePaymentReconciliation.getLinkedResources()");

		List<Resource> linkedResources = new ArrayList<Resource>();

		try {
			// Test for valid container Resource
			if (containerResource != null && containerResource instanceof PaymentReconciliation) {

				PaymentReconciliation typedContainerResource = (PaymentReconciliation) containerResource;

				/*
				 * PaymentReconciliation linked Resource references
				 */
				String ref = null;
				Resource linkedResource = null;

				// paymentIssuer (Organization)
				if (typedContainerResource.hasPaymentIssuer() && typedContainerResource.getPaymentIssuer().hasReference()) {

					ref = typedContainerResource.getPaymentIssuer().getReference();
					linkedResource = this.getLinkedResource(resourceService, ref, "Organization");

					if (linkedResource != null) {
						linkedResources.add(linkedResource);
					}
				}

				linkedResource = null;
				// request (Task)
				if (typedContainerResource.hasRequest() && typedContainerResource.getRequest().hasReference()) {

					ref = typedContainerResource.getRequest().getReference();
					linkedResource = this.getLinkedResource(resourceService, ref, "Task");

					if (linkedResource != null) {
						linkedResources.add(linkedResource);
					}
				}

				linkedResource = null;
				// requestor
				if (typedContainerResource.hasRequestor() && typedContainerResource.getRequestor().hasReference()) {

					ref = typedContainerResource.getRequestor().getReference();
					linkedResource = this.getLinkedResourceAny(resourceService, ref);

					if (linkedResource != null) {
						linkedResources.add(linkedResource);
					}
				}

				linkedResource = null;
				// detail
				if (typedContainerResource.hasDetail()) {
					for (DetailsComponent detail : typedContainerResource.getDetail()) {
						// detail.request
						if (detail.hasRequest() && detail.getRequest().hasReference()) {

							ref = detail.getRequest().getReference();
							linkedResource = this.getLinkedResourceAny(resourceService, ref);

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}

						// detail.submitter
						if (detail.hasSubmitter() && detail.getSubmitter().hasReference()) {

							ref = detail.getSubmitter().getReference();
							linkedResource = this.getLinkedResourceAny(resourceService, ref);

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}

						// detail.response
						if (detail.hasResponse() && detail.getResponse().hasReference()) {

							ref = detail.getResponse().getReference();
							linkedResource = this.getLinkedResourceAny(resourceService, ref);

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}

						// detail.responsible (PractitionerRole)
						if (detail.hasResponsible() && detail.getResponsible().hasReference()) {

							ref = detail.getResponsible().getReference();
							linkedResource = this.getLinkedResource(resourceService, ref, "PractitionerRole");

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}

						// detail.payee
						if (detail.hasPayee() && detail.getPayee().hasReference()) {

							ref = detail.getPayee().getReference();
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
