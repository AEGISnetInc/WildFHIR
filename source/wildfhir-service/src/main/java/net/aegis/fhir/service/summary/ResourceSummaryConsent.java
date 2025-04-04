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
package net.aegis.fhir.service.summary;

import org.hl7.fhir.r4.model.Consent;
import org.hl7.fhir.r4.model.Consent.ConsentVerificationComponent;
import org.hl7.fhir.r4.model.Consent.ProvisionComponent;
import org.hl7.fhir.r4.model.Resource;

/**
 * @author richard.ettema
 *
 */
public class ResourceSummaryConsent extends ResourceSummaryProxy {

	/* (non-Javadoc)
	 * @see net.aegis.fhir.service.summary.ResourceSummaryProxy#generateSummary(org.hl7.fhir.instance.model.Resource)
	 */
	@Override
	public Resource generateSummary(Resource resource) throws Exception {

		Consent summary = null;

		try {
			// Cast original resource to expected type
			Consent original = (Consent) resource;

			// Copy original resource and remove text
			summary = original.copy();
			((Resource)original).copyValues(summary);

			// Remove non-summary Resource elements
			removeNonSummaryResourceElements(summary);

			// Remove non-summary DomainResource elements
			removeNonSummaryDomainResourceElements(summary);

			// Set Meta Security Tag SUBSETTED
			setMetaTagSubsetted(summary);

			// Remove Resource Type non-summary data elements
			summary.setPolicy(null);
			for (ConsentVerificationComponent verification : summary.getVerification()) {
				verification.setVerifiedWith(null);
				verification.setVerificationDate(null);
			}
			setSummaryConsentProvision(summary.getProvision());
		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		}

		return summary;
	}

	/* (non-Javadoc)
	 * @see net.aegis.fhir.service.summary.ResourceSummaryProxy#generateDataSummary(org.hl7.fhir.instance.model.Resource)
	 */
	@Override
	public Resource generateDataSummary(Resource resource) throws Exception {

		Consent summary = null;

		try {
			// Cast original resource to expected type
			Consent original = (Consent) resource;

			// Copy original resource and remove text
			summary = original.copy();
			((Resource)original).copyValues(summary);

			// Remove text element
			summary.setText(null);

			// Set Meta Security Tag SUBSETTED
			setMetaTagSubsetted(summary);
		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		}

		return summary;
	}

	/* (non-Javadoc)
	 * @see net.aegis.fhir.service.summary.ResourceSummaryProxy#generateTextSummary(org.hl7.fhir.instance.model.Resource)
	 */
	@Override
	public Resource generateTextSummary(Resource resource) throws Exception {

		Consent summary = null;

		try {
			// Cast original resource to expected type
			Consent original = (Consent) resource;

			// Instantiate summary resource
			summary = new Consent();

			// Copy text-only DomainResource elements
			copyTextOnlyDomainResourceElements(original, summary);

			// Set Meta Security Tag SUBSETTED
			setMetaTagSubsetted(summary);

			// Copy mandatory Resource Type elements
			summary.setStatus(original.getStatus());
			summary.setScope(original.getScope());
			summary.setCategory(original.getCategory());
			ConsentVerificationComponent summaryVerification;
			for (ConsentVerificationComponent verification : original.getVerification()) {
				summaryVerification = new ConsentVerificationComponent();
				summaryVerification.setVerified(verification.getVerified());
				summary.addVerification(summaryVerification);
			}
			summary.setProvision(getSummaryProvision(original.getProvision()));

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		}

		return summary;
	}

	private void setSummaryConsentProvision(ProvisionComponent provision) {
		if (provision != null) {
			provision.setActor(null);
			for (ProvisionComponent provisionProvision : provision.getProvision()) {
				setSummaryConsentProvision(provisionProvision);
			}
		}
	}


	private ProvisionComponent getSummaryProvision(ProvisionComponent provision) {
		ProvisionComponent summaryProvision = null;

		if (provision != null) {
			summaryProvision = new ProvisionComponent();
			summaryProvision.setActor(provision.getActor());
			summaryProvision.setData(provision.getData());

			if (provision.hasProvision()) {
				for (ProvisionComponent provisionProvision : provision.getProvision()) {
					summaryProvision.addProvision(getSummaryProvision(provisionProvision));
				}
			}
		}

		return summaryProvision;
	}
}
