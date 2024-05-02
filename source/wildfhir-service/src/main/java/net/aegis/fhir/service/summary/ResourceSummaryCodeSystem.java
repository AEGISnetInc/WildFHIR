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

import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.CodeSystem.CodeSystemFilterComponent;
import org.hl7.fhir.r4.model.CodeSystem.ConceptDefinitionComponent;
import org.hl7.fhir.r4.model.CodeSystem.ConceptDefinitionDesignationComponent;
import org.hl7.fhir.r4.model.CodeSystem.ConceptPropertyComponent;
import org.hl7.fhir.r4.model.CodeSystem.PropertyComponent;
import org.hl7.fhir.r4.model.Resource;

/**
 * @author richard.ettema
 *
 */
public class ResourceSummaryCodeSystem extends ResourceSummaryProxy {

	/* (non-Javadoc)
	 * @see net.aegis.fhir.service.summary.ResourceSummaryProxy#generateSummary(org.hl7.fhir.instance.model.Resource)
	 */
	@Override
	public Resource generateSummary(Resource resource) throws Exception {

		CodeSystem summary = null;

		try {
			// Cast original resource to expected type
			CodeSystem original = (CodeSystem) resource;

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
			summary.setCopyright(null);
			summary.setConcept(null);
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

		CodeSystem summary = null;

		try {
			// Cast original resource to expected type
			CodeSystem original = (CodeSystem) resource;

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

		CodeSystem summary = null;

		try {
			// Cast original resource to expected type
			CodeSystem original = (CodeSystem) resource;

			// Instantiate summary resource
			summary = new CodeSystem();

			// Copy text-only DomainResource elements
			copyTextOnlyDomainResourceElements(original, summary);

			// Set Meta Security Tag SUBSETTED
			setMetaTagSubsetted(summary);

			// Copy mandatory Resource Type elements
			summary.setStatus(original.getStatus());
			summary.setContent(original.getContent());
			CodeSystemFilterComponent summaryFilter;
			for (CodeSystemFilterComponent filter : original.getFilter()) {
				summaryFilter = new CodeSystemFilterComponent();
				summaryFilter.setCode(filter.getCode());
				summaryFilter.setOperator(filter.getOperator());
				summaryFilter.setValue(filter.getValue());
				summary.addFilter(summaryFilter);
			}
			PropertyComponent summaryProperty;
			for (PropertyComponent property : original.getProperty()) {
				summaryProperty = new PropertyComponent();
				summaryProperty.setCode(property.getCode());
				summaryProperty.setType(property.getType());
				summary.addProperty(summaryProperty);
			}
			ConceptDefinitionComponent summaryConcept;
			for (ConceptDefinitionComponent concept : original.getConcept()) {
				summaryConcept = getSummaryConceptDefinitionComponent(concept);
				if (summaryConcept != null) {
					summary.addConcept(summaryConcept);
				}
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		}

		return summary;
	}

	private ConceptDefinitionComponent getSummaryConceptDefinitionComponent(ConceptDefinitionComponent originalConcept) {
		ConceptDefinitionComponent summaryConcept = null;

		if (originalConcept != null) {
			summaryConcept = new ConceptDefinitionComponent();
			summaryConcept.setCode(originalConcept.getCode());
			ConceptDefinitionDesignationComponent summaryConceptDesignation;
			for (ConceptDefinitionDesignationComponent designation : originalConcept.getDesignation()) {
				summaryConceptDesignation = new ConceptDefinitionDesignationComponent();
				summaryConceptDesignation.setValue(designation.getValue());
				summaryConcept.addDesignation(summaryConceptDesignation);
			}
			ConceptPropertyComponent summaryConceptProperty;
			for (ConceptPropertyComponent property : originalConcept.getProperty()) {
				summaryConceptProperty = new ConceptPropertyComponent();
				summaryConceptProperty.setCode(property.getCode());
				summaryConceptProperty.setValue(property.getValue());
				summaryConcept.addProperty(summaryConceptProperty);
			}
			ConceptDefinitionComponent summaryConceptConcept = null;
			for (ConceptDefinitionComponent concept : originalConcept.getConcept()) {
				summaryConceptConcept = getSummaryConceptDefinitionComponent(concept);
				if (summaryConceptConcept != null) {
					summaryConcept.addConcept(summaryConceptConcept);
				}
			}
		}

		return summaryConcept;
	}
}
