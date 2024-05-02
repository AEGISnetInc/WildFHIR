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

import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.ValueSet;
import org.hl7.fhir.r4.model.ValueSet.ConceptReferenceComponent;
import org.hl7.fhir.r4.model.ValueSet.ConceptReferenceDesignationComponent;
import org.hl7.fhir.r4.model.ValueSet.ConceptSetComponent;
import org.hl7.fhir.r4.model.ValueSet.ValueSetComposeComponent;
import org.hl7.fhir.r4.model.ValueSet.ValueSetExpansionComponent;
import org.hl7.fhir.r4.model.ValueSet.ValueSetExpansionContainsComponent;
import org.hl7.fhir.r4.model.ValueSet.ValueSetExpansionParameterComponent;

/**
 * @author richard.ettema
 *
 */
public class ResourceSummaryValueSet extends ResourceSummaryProxy {

	/* (non-Javadoc)
	 * @see net.aegis.fhir.service.summary.ResourceSummaryProxy#generateSummary(org.hl7.fhir.instance.model.Resource)
	 */
	@Override
	public Resource generateSummary(Resource resource) throws Exception {

		ValueSet summary = null;

		try {
			// Cast original resource to expected type
			ValueSet original = (ValueSet) resource;

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
			if (summary.hasCompose()) {
				for (ConceptSetComponent include : summary.getCompose().getInclude()) {
					include.setConcept(null);
				}
				for (ConceptSetComponent exclude : summary.getCompose().getExclude()) {
					exclude.setConcept(null);
				}
			}
			summary.setExpansion(null);
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

		ValueSet summary = null;

		try {
			// Cast original resource to expected type
			ValueSet original = (ValueSet) resource;

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

		ValueSet summary = null;

		try {
			// Cast original resource to expected type
			ValueSet original = (ValueSet) resource;

			// Instantiate summary resource
			summary = new ValueSet();

			// Copy text-only DomainResource elements
			copyTextOnlyDomainResourceElements(original, summary);

			// Set Meta Security Tag SUBSETTED
			setMetaTagSubsetted(summary);

			// Copy mandatory Resource Type elements
			summary.setStatus(original.getStatus());
			if (original.hasCompose()) {
				ValueSetComposeComponent summaryCompose = new ValueSetComposeComponent();
				ConceptSetComponent summaryComposeInclude;
				for (ConceptSetComponent include : original.getCompose().getInclude()) {
					summaryComposeInclude = new ConceptSetComponent();
					ConceptReferenceComponent summaryComposeIncludeConcept;
					for (ConceptReferenceComponent concept : include.getConcept()) {
						summaryComposeIncludeConcept = new ConceptReferenceComponent();
						summaryComposeIncludeConcept.setCode(concept.getCode());
						ConceptReferenceDesignationComponent summaryComposeIncludeConceptDesignation;
						for (ConceptReferenceDesignationComponent designation : concept.getDesignation()) {
							summaryComposeIncludeConceptDesignation = new ConceptReferenceDesignationComponent();
							summaryComposeIncludeConceptDesignation.setValue(designation.getValue());
							summaryComposeIncludeConcept.addDesignation(summaryComposeIncludeConceptDesignation);
						}
						summaryComposeInclude.addConcept(summaryComposeIncludeConcept);
					}
					summaryComposeInclude.setFilter(include.getFilter());
					summaryCompose.addInclude(summaryComposeInclude);
				}
				ConceptSetComponent summaryComposeExclude;
				for (ConceptSetComponent exclude : original.getCompose().getExclude()) {
					summaryComposeExclude = new ConceptSetComponent();
					ConceptReferenceComponent summaryComposeExcludeConcept;
					for (ConceptReferenceComponent concept : exclude.getConcept()) {
						summaryComposeExcludeConcept = new ConceptReferenceComponent();
						summaryComposeExcludeConcept.setCode(concept.getCode());
						ConceptReferenceDesignationComponent summaryComposeExcludeConceptDesignation;
						for (ConceptReferenceDesignationComponent designation : concept.getDesignation()) {
							summaryComposeExcludeConceptDesignation = new ConceptReferenceDesignationComponent();
							summaryComposeExcludeConceptDesignation.setValue(designation.getValue());
							summaryComposeExcludeConcept.addDesignation(summaryComposeExcludeConceptDesignation);
						}
						summaryComposeExclude.addConcept(summaryComposeExcludeConcept);
					}
					summaryComposeExclude.setFilter(exclude.getFilter());
					summaryCompose.addExclude(summaryComposeExclude);
				}
				summary.setCompose(summaryCompose);
			}
			if (original.hasExpansion()) {
				ValueSetExpansionComponent summaryExpansion = new ValueSetExpansionComponent();
				summaryExpansion.setTimestamp(original.getExpansion().getTimestamp());
				ValueSetExpansionParameterComponent summaryExpansionParameter;
				for (ValueSetExpansionParameterComponent parameter : original.getExpansion().getParameter()) {
					summaryExpansionParameter = new ValueSetExpansionParameterComponent();
					summaryExpansionParameter.setName(parameter.getName());
					summaryExpansion.addParameter(summaryExpansionParameter);
				}
				ValueSetExpansionContainsComponent summaryContains = null;
				for (ValueSetExpansionContainsComponent contains : original.getExpansion().getContains()) {
					summaryContains = getMandatoryContainsElements(contains);
					if (summaryContains != null) {
						summaryExpansion.addContains(summaryContains);
					}
				}
				summary.setExpansion(summaryExpansion);
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		}

		return summary;
	}

	private ValueSetExpansionContainsComponent getMandatoryContainsElements(ValueSetExpansionContainsComponent contains) {
		ValueSetExpansionContainsComponent summaryContains = null;
		if (contains != null && contains.hasDesignation()) {
			summaryContains = new ValueSetExpansionContainsComponent();

			ConceptReferenceDesignationComponent summaryDesignation = null;
			for (ConceptReferenceDesignationComponent designation : contains.getDesignation()) {
				summaryDesignation = new ConceptReferenceDesignationComponent();
				summaryDesignation.setValue(designation.getValue());
				summaryContains.addDesignation(summaryDesignation);
			}

			ValueSetExpansionContainsComponent summaryContainsContains = null;
			for (ValueSetExpansionContainsComponent containsContains : contains.getContains()) {
				summaryContainsContains = getMandatoryContainsElements(containsContains);
				if (summaryContainsContains != null) {
					summaryContains.addContains(summaryContainsContains);
				}
			}
		}
		return summaryContains;
	}

}
