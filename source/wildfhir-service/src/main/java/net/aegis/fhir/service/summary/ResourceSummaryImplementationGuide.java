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

import org.hl7.fhir.r4.model.ImplementationGuide;
import org.hl7.fhir.r4.model.ImplementationGuide.ImplementationGuideDefinitionComponent;
import org.hl7.fhir.r4.model.ImplementationGuide.ImplementationGuideDefinitionGroupingComponent;
import org.hl7.fhir.r4.model.ImplementationGuide.ImplementationGuideDefinitionPageComponent;
import org.hl7.fhir.r4.model.ImplementationGuide.ImplementationGuideDefinitionResourceComponent;
import org.hl7.fhir.r4.model.ImplementationGuide.ImplementationGuideDefinitionTemplateComponent;
import org.hl7.fhir.r4.model.ImplementationGuide.ImplementationGuideDependsOnComponent;
import org.hl7.fhir.r4.model.ImplementationGuide.ImplementationGuideManifestComponent;
import org.hl7.fhir.r4.model.ImplementationGuide.ManifestPageComponent;
import org.hl7.fhir.r4.model.ImplementationGuide.ManifestResourceComponent;
import org.hl7.fhir.r4.model.Resource;

/**
 * @author richard.ettema
 *
 */
public class ResourceSummaryImplementationGuide extends ResourceSummaryProxy {

	/* (non-Javadoc)
	 * @see net.aegis.fhir.service.summary.ResourceSummaryProxy#generateSummary(org.hl7.fhir.instance.model.Resource)
	 */
	@Override
	public Resource generateSummary(Resource resource) throws Exception {

		ImplementationGuide summary = null;

		try {
			// Cast original resource to expected type
			ImplementationGuide original = (ImplementationGuide) resource;

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
			summary.setCopyright(null);
			summary.setDefinition(null);
			if (summary.hasManifest()) {
				for (ManifestResourceComponent resourceComponent : summary.getManifest().getResource()) {
					resourceComponent.setExample(null);
					resourceComponent.setRelativePath(null);
				}
				summary.getManifest().setPage(null);
				summary.getManifest().setImage(null);
				summary.getManifest().setOther(null);
			}
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

		ImplementationGuide summary = null;

		try {
			// Cast original resource to expected type
			ImplementationGuide original = (ImplementationGuide) resource;

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

		ImplementationGuide summary = null;

		try {
			// Cast original resource to expected type
			ImplementationGuide original = (ImplementationGuide) resource;

			// Instantiate summary resource
			summary = new ImplementationGuide();

			// Copy text-only DomainResource elements
			copyTextOnlyDomainResourceElements(original, summary);

			// Set Meta Security Tag SUBSETTED
			setMetaTagSubsetted(summary);

			// Copy mandatory Resource Type elements
			summary.setUrl(original.getUrl());
			summary.setName(original.getName());
			summary.setStatus(original.getStatus());
			summary.setPackageId(original.getPackageId());
			summary.setFhirVersion(original.getFhirVersion());
			ImplementationGuideDependsOnComponent summaryDependsOn;
			for (ImplementationGuideDependsOnComponent dependsOn : original.getDependsOn()) {
				summaryDependsOn = new ImplementationGuideDependsOnComponent();
				summaryDependsOn.setUri(dependsOn.getUri());
				summary.addDependsOn(summaryDependsOn);
			}
			summary.setGlobal(original.getGlobal());
			if (original.hasDefinition()) {
				ImplementationGuideDefinitionComponent summaryDefinition = new ImplementationGuideDefinitionComponent();

				ImplementationGuideDefinitionGroupingComponent summaryGrouping;
				for (ImplementationGuideDefinitionGroupingComponent grouping : original.getDefinition().getGrouping()) {
					summaryGrouping = new ImplementationGuideDefinitionGroupingComponent();
					summaryGrouping.setName(grouping.getName());
					summaryDefinition.addGrouping(summaryGrouping);
				}
				ImplementationGuideDefinitionResourceComponent summaryResourceComponent;
				for (ImplementationGuideDefinitionResourceComponent resourceComponent : original.getDefinition().getResource()) {
					summaryResourceComponent = new ImplementationGuideDefinitionResourceComponent();
					summaryResourceComponent.setReference(resourceComponent.getReference());
					summaryDefinition.addResource(summaryResourceComponent);
				}
				if (original.getDefinition().hasPage()) {
					ImplementationGuideDefinitionPageComponent summaryPageComponent = getMandatoryPageElements(original.getDefinition().getPage());
					summaryDefinition.setPage(summaryPageComponent);
				}
				summaryDefinition.setParameter(original.getDefinition().getParameter());
				ImplementationGuideDefinitionTemplateComponent summaryTemplateComponent;
				for (ImplementationGuideDefinitionTemplateComponent templateComponent : original.getDefinition().getTemplate()) {
					summaryTemplateComponent = new ImplementationGuideDefinitionTemplateComponent();
					summaryTemplateComponent.setCode(templateComponent.getCode());
					summaryTemplateComponent.setSource(templateComponent.getSource());
					summaryDefinition.addTemplate(summaryTemplateComponent);
				}

				summary.setDefinition(summaryDefinition);
			}
			if (original.hasManifest()) {
				ImplementationGuideManifestComponent summaryManifestComponent = new ImplementationGuideManifestComponent();

				ManifestResourceComponent summaryResourceComponent;
				for (ManifestResourceComponent resourceComponent : original.getManifest().getResource()) {
					summaryResourceComponent = new ManifestResourceComponent();
					summaryResourceComponent.setReference(resourceComponent.getReference());
					summaryManifestComponent.addResource(summaryResourceComponent);
				}
				ManifestPageComponent summaryPageComponent;
				for (ManifestPageComponent pageComponent : original.getManifest().getPage()) {
					summaryPageComponent = new ManifestPageComponent();
					summaryPageComponent.setName(pageComponent.getName());
					summaryManifestComponent.addPage(summaryPageComponent);
				}

				summary.setManifest(summaryManifestComponent);
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		}

		return summary;
	}

	private ImplementationGuideDefinitionPageComponent getMandatoryPageElements(ImplementationGuideDefinitionPageComponent originalPageComponent) {
		ImplementationGuideDefinitionPageComponent summaryPageComponent = null;
		if (originalPageComponent != null) {
			summaryPageComponent = new ImplementationGuideDefinitionPageComponent();
			summaryPageComponent.setName(originalPageComponent.getName());
			summaryPageComponent.setTitle(originalPageComponent.getTitle());
			summaryPageComponent.setGeneration(originalPageComponent.getGeneration());
			if (originalPageComponent.hasPage()) {
				ImplementationGuideDefinitionPageComponent summaryPagePageComponent;
				for (ImplementationGuideDefinitionPageComponent originalPagePageComponent : originalPageComponent.getPage()) {
					summaryPagePageComponent = getMandatoryPageElements(originalPagePageComponent);
					if (summaryPagePageComponent != null) {
						summaryPageComponent.addPage(summaryPagePageComponent);
					}
				}
			}
		}
		return summaryPageComponent;
	}

}
