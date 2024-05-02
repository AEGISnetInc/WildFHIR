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

import org.hl7.fhir.r4.model.TerminologyCapabilities;
import org.hl7.fhir.r4.model.TerminologyCapabilities.TerminologyCapabilitiesCodeSystemComponent;
import org.hl7.fhir.r4.model.TerminologyCapabilities.TerminologyCapabilitiesCodeSystemVersionComponent;
import org.hl7.fhir.r4.model.TerminologyCapabilities.TerminologyCapabilitiesCodeSystemVersionFilterComponent;
import org.hl7.fhir.r4.model.TerminologyCapabilities.TerminologyCapabilitiesExpansionComponent;
import org.hl7.fhir.r4.model.TerminologyCapabilities.TerminologyCapabilitiesExpansionParameterComponent;
import org.hl7.fhir.r4.model.TerminologyCapabilities.TerminologyCapabilitiesImplementationComponent;
import org.hl7.fhir.r4.model.TerminologyCapabilities.TerminologyCapabilitiesSoftwareComponent;
import org.hl7.fhir.r4.model.TerminologyCapabilities.TerminologyCapabilitiesTranslationComponent;
import org.hl7.fhir.r4.model.TerminologyCapabilities.TerminologyCapabilitiesValidateCodeComponent;
import org.hl7.fhir.r4.model.Resource;

/**
 * @author richard.ettema
 *
 */
public class ResourceSummaryTerminologyCapabilities extends ResourceSummaryProxy {

	/* (non-Javadoc)
	 * @see net.aegis.fhir.service.summary.ResourceSummaryProxy#generateSummary(org.hl7.fhir.instance.model.Resource)
	 */
	@Override
	public Resource generateSummary(Resource resource) throws Exception {

		TerminologyCapabilities summary = null;

		try {
			// Cast original resource to expected type
			TerminologyCapabilities original = (TerminologyCapabilities) resource;

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
			summary.setDescription(null);
			summary.setPurpose(null);
			for (TerminologyCapabilitiesCodeSystemComponent codeSystem : summary.getCodeSystem()) {
				codeSystem.setUri(null);
				for (TerminologyCapabilitiesCodeSystemVersionComponent version : codeSystem.getVersion()) {
					version.setCompositionalElement(null);
					version.setLanguage(null);
					version.setFilter(null);
					version.setProperty(null);
				}
				codeSystem.setSubsumptionElement(null);
			}
			summary.setExpansion(null);
			summary.setCodeSearch(null);
			summary.setValidateCode(null);
			summary.setTranslation(null);
			summary.setClosure(null);
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

		TerminologyCapabilities summary = null;

		try {
			// Cast original resource to expected type
			TerminologyCapabilities original = (TerminologyCapabilities) resource;

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

		TerminologyCapabilities summary = null;

		try {
			// Cast original resource to expected type
			TerminologyCapabilities original = (TerminologyCapabilities) resource;

			// Instantiate summary resource
			summary = new TerminologyCapabilities();

			// Copy text-only DomainResource elements
			copyTextOnlyDomainResourceElements(original, summary);

			// Set Meta Security Tag SUBSETTED
			setMetaTagSubsetted(summary);

			// Copy mandatory Resource Type elements
			summary.setStatus(original.getStatus());
			summary.setDate(original.getDate());
			summary.setKind(original.getKind());

			if (original.hasSoftware()) {
				TerminologyCapabilitiesSoftwareComponent summarySoftware = new TerminologyCapabilitiesSoftwareComponent();
				summarySoftware.setName(original.getSoftware().getName());
				summary.setSoftware(summarySoftware);
			}

			if (original.hasImplementation()) {
				TerminologyCapabilitiesImplementationComponent summaryImplementation = new TerminologyCapabilitiesImplementationComponent();
				summaryImplementation.setDescription(original.getImplementation().getDescription());
				summary.setImplementation(summaryImplementation);
			}

			TerminologyCapabilitiesCodeSystemComponent summaryCodeSystem =  null;
			for (TerminologyCapabilitiesCodeSystemComponent codeSystem : original.getCodeSystem()) {
				summaryCodeSystem =  null;

				TerminologyCapabilitiesCodeSystemVersionComponent summaryVersion = null;
				for (TerminologyCapabilitiesCodeSystemVersionComponent version : codeSystem.getVersion()) {
					summaryVersion = null;

					TerminologyCapabilitiesCodeSystemVersionFilterComponent summaryFilter = null;
					for (TerminologyCapabilitiesCodeSystemVersionFilterComponent filter : version.getFilter()) {
						if (summaryCodeSystem == null) {
							summaryCodeSystem = new TerminologyCapabilitiesCodeSystemComponent();
						}
						if (summaryVersion == null) {
							summaryVersion = new TerminologyCapabilitiesCodeSystemVersionComponent();
						}
						summaryFilter = new TerminologyCapabilitiesCodeSystemVersionFilterComponent();
						summaryFilter.setCode(filter.getCode());
						summaryFilter.setOp(filter.getOp());
						summaryVersion.addFilter(summaryFilter);
					}

					if (summaryCodeSystem != null && summaryVersion != null) {
						summaryCodeSystem.addVersion(summaryVersion);
					}
				}

				if (summaryCodeSystem != null) {
					summary.addCodeSystem(summaryCodeSystem);
				}
			}

			if (original.hasExpansion() && original.getExpansion().hasParameter()) {
				TerminologyCapabilitiesExpansionComponent summaryExpansion = new TerminologyCapabilitiesExpansionComponent();
				TerminologyCapabilitiesExpansionParameterComponent summaryParameter;
				for (TerminologyCapabilitiesExpansionParameterComponent parameter : original.getExpansion().getParameter()) {
					summaryParameter = new TerminologyCapabilitiesExpansionParameterComponent();
					summaryParameter.setName(parameter.getName());
					summaryExpansion.addParameter(summaryParameter);
				}
				summary.setExpansion(summaryExpansion);
			}

			if (original.hasValidateCode()) {
				TerminologyCapabilitiesValidateCodeComponent summaryValidateCode = new TerminologyCapabilitiesValidateCodeComponent();
				summaryValidateCode.setTranslations(original.getValidateCode().getTranslations());
				summary.setValidateCode(summaryValidateCode);
			}

			if (original.hasTranslation()) {
				TerminologyCapabilitiesTranslationComponent summaryTranslation = new TerminologyCapabilitiesTranslationComponent();
				summaryTranslation.setNeedsMap(original.getTranslation().getNeedsMap());
				summary.setTranslation(summaryTranslation);
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		}

		return summary;
	}

}
