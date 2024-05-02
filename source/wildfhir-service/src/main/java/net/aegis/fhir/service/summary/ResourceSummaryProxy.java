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

import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Resource;

/**
 * @author richard.ettema
 *
 */
public abstract class ResourceSummaryProxy {

	/**
	 * Generate general summary of resource (_summary=true)
	 *
	 * @param resource
	 * @return <code>Resource</code> with only general summary information
	 * @throws Exception
	 */
	public abstract Resource generateSummary(Resource resource) throws Exception;

	/**
	 * Generate data only summary of resource (_summary=data)
	 *
	 * @param resource
	 * @return <code>Resource</code> with only data summary information
	 * @throws Exception
	 */
	public abstract Resource generateDataSummary(Resource resource) throws Exception;

	/**
	 * Generate text only summary of resource (_summary=text)
	 *
	 * @param resource
	 * @return <code>Resource</code> with only text summary information
	 * @throws Exception
	 */
	public abstract Resource generateTextSummary(Resource resource) throws Exception;

	/**
	 * Set the Meta Tag SUBSETTED on the summary resource
	 *
	 * @param resource
	 * @throws Exception
	 */
	protected void setMetaTagSubsetted(Resource resource) throws Exception {

		try {
			// First define the new security tag
			Coding subsettedTag = new Coding();
			subsettedTag.setCode("SUBSETTED");
			subsettedTag.setSystem("http://terminology.hl7.org/CodeSystem/v3-ObservationValue");
			subsettedTag.setDisplay("subsetted");

			if (resource.hasMeta()) {
				if (resource.getMeta().hasTag()) {
					boolean hasSubsetted = false;
					for (Coding securityCoding : resource.getMeta().getTag()) {
						if (securityCoding.getCode().equals("SUBSETTED")) {
							hasSubsetted = true;
						}
					}
					if (!hasSubsetted) {
						resource.getMeta().getTag().add(subsettedTag);
					}
				}
				else {
					resource.getMeta().getTag().add(subsettedTag);
				}
			}
			else {
				Meta meta = new Meta();
				meta.getTag().add(subsettedTag);
				resource.setMeta(meta);
			}
		}
		catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		}

	}

	/**
	 * Remove non-summary Resource elements on the summary resource
	 *
	 * @param resource
	 * @throws Exception
	 */
	protected void removeNonSummaryResourceElements(Resource resource) throws Exception {

		try {
			// Remove non-summary Resource elements
			resource.setLanguage(null);
		}
		catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		}

	}

	/**
	 * Remove non-summary DomainResource elements on the summary resource
	 *
	 * @param resource
	 * @throws Exception
	 */
	protected void removeNonSummaryDomainResourceElements(DomainResource domainResource) throws Exception {

		try {
			// Remove non-summary DomainResource elements
			domainResource.setText(null);
			domainResource.setContained(null);
			domainResource.setExtension(null);
			domainResource.setModifierExtension(null);
		}
		catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		}

	}

	/**
	 * Remove non-summary DomainResource elements on the summary resource
	 *
	 * @param resource
	 * @throws Exception
	 */
	protected void copyTextOnlyDomainResourceElements(DomainResource original, DomainResource summary) throws Exception {

		try {
			if (original != null && summary != null) {
				// Copy text-only DomainResource elements
				summary.setText(original.getText().copy());
				summary.setId(original.getId());
				summary.setMeta(original.getMeta());
			}
		}
		catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		}

	}

}
