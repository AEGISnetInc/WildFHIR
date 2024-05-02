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

import org.hl7.fhir.r4.model.CoverageEligibilityRequest;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.CoverageEligibilityRequest.DetailsComponent;
import org.hl7.fhir.r4.model.CoverageEligibilityRequest.DiagnosisComponent;
import org.hl7.fhir.r4.model.CoverageEligibilityRequest.InsuranceComponent;
import org.hl7.fhir.r4.model.CoverageEligibilityRequest.SupportingInformationComponent;
import org.hl7.fhir.r4.model.Resource;

import net.aegis.fhir.service.ResourceService;

/**
 * @author richard.ettema
 *
 */
public class LinkedResourceCoverageEligibilityRequest extends LinkedResourceProxy {

	private Logger log = Logger.getLogger("LinkedResourceCoverageEligibilityRequest");

	/* (non-Javadoc)
	 * @see net.aegis.fhir.service.linked.LinkedResourceProxy#getLinkedResources(net.aegis.fhir.service.ResourceService, org.hl7.fhir.instance.model.Resource)
	 */
	@Override
	public List<Resource> getLinkedResources(ResourceService resourceService, Resource containerResource) throws Exception {

		log.fine("[START] LinkedResourceCoverageEligibilityRequest.getLinkedResources()");

		List<Resource> linkedResources = new ArrayList<Resource>();

		try {
			// Test for valid container Resource
			if (containerResource != null && containerResource instanceof CoverageEligibilityRequest) {

				CoverageEligibilityRequest typedContainerResource = (CoverageEligibilityRequest) containerResource;

				/*
				 * CoverageEligibilityRequest linked Resource references
				 */
				String ref = null;
				Resource linkedResource = null;

				// patient (Patient)
				if (typedContainerResource.hasPatient() && typedContainerResource.getPatient().hasReference()) {

					ref = typedContainerResource.getPatient().getReference();
					linkedResource = this.getLinkedResource(resourceService, ref, "Patient");

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
				// provider
				if (typedContainerResource.hasProvider() && typedContainerResource.getProvider().hasReference()) {

					ref = typedContainerResource.getProvider().getReference();
					linkedResource = this.getLinkedResourceAny(resourceService, ref);

					if (linkedResource != null) {
						linkedResources.add(linkedResource);
					}
				}

				linkedResource = null;
				// insurer (Organization)
				if (typedContainerResource.hasInsurer() && typedContainerResource.getInsurer().hasReference()) {

					ref = typedContainerResource.getInsurer().getReference();
					linkedResource = this.getLinkedResource(resourceService, ref, "Organization");

					if (linkedResource != null) {
						linkedResources.add(linkedResource);
					}
				}

				linkedResource = null;
				// facility (Location)
				if (typedContainerResource.hasFacility() && typedContainerResource.getFacility().hasReference()) {

					ref = typedContainerResource.getFacility().getReference();
					linkedResource = this.getLinkedResource(resourceService, ref, "Location");

					if (linkedResource != null) {
						linkedResources.add(linkedResource);
					}
				}

				linkedResource = null;
				// supportingInfo.information
				if (typedContainerResource.hasSupportingInfo()) {

					for (SupportingInformationComponent supportingInfo : typedContainerResource.getSupportingInfo()) {
						if (supportingInfo.hasInformation() && supportingInfo.getInformation().hasReference()) {

							ref = supportingInfo.getInformation().getReference();
							linkedResource = this.getLinkedResourceAny(resourceService, ref);

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}

				linkedResource = null;
				// insurance.coverage (Coverage)
				if (typedContainerResource.hasInsurance()) {

					for (InsuranceComponent insurance : typedContainerResource.getInsurance()) {
						if (insurance.hasCoverage() && insurance.getCoverage().hasReference()) {

							ref = insurance.getCoverage().getReference();
							linkedResource = this.getLinkedResource(resourceService, ref, "Coverage");

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}

				linkedResource = null;
				// item
				if (typedContainerResource.hasItem()) {

					for (DetailsComponent item : typedContainerResource.getItem()) {
						// item.provider
						if (item.hasProvider() && item.getProvider().hasReference()) {

							ref = item.getProvider().getReference();
							linkedResource = this.getLinkedResourceAny(resourceService, ref);

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}

						// item.facility
						if (item.hasFacility() && item.getFacility().hasReference()) {

							ref = item.getFacility().getReference();
							linkedResource = this.getLinkedResourceAny(resourceService, ref);

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}

						// item.diagnosis.diagnosisReference (Condition)
						if (item.hasDiagnosis()) {

							for (DiagnosisComponent diagnosis : item.getDiagnosis()) {
								if (diagnosis.hasDiagnosisReference() && diagnosis.getDiagnosisReference().hasReference()) {

									ref = diagnosis.getDiagnosisReference().getReference();
									linkedResource = this.getLinkedResource(resourceService, ref, "Condition");

									if (linkedResource != null) {
										linkedResources.add(linkedResource);
									}
								}
							}
						}

						// item.detail
						if (item.hasDetail()) {

							for (Reference detail : item.getDetail()) {
								if (detail.hasReference()) {

									ref = detail.getReference();
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
