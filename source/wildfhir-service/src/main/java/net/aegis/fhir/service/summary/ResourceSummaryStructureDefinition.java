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

import org.hl7.fhir.r4.model.ElementDefinition;
import org.hl7.fhir.r4.model.ElementDefinition.ElementDefinitionBaseComponent;
import org.hl7.fhir.r4.model.ElementDefinition.ElementDefinitionBindingComponent;
import org.hl7.fhir.r4.model.ElementDefinition.ElementDefinitionConstraintComponent;
import org.hl7.fhir.r4.model.ElementDefinition.ElementDefinitionMappingComponent;
import org.hl7.fhir.r4.model.ElementDefinition.ElementDefinitionSlicingComponent;
import org.hl7.fhir.r4.model.ElementDefinition.TypeRefComponent;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.StructureDefinition.StructureDefinitionDifferentialComponent;
import org.hl7.fhir.r4.model.StructureDefinition.StructureDefinitionMappingComponent;
import org.hl7.fhir.r4.model.StructureDefinition.StructureDefinitionSnapshotComponent;

/**
 * @author richard.ettema
 *
 */
public class ResourceSummaryStructureDefinition extends ResourceSummaryProxy {

	/* (non-Javadoc)
	 * @see net.aegis.fhir.service.summary.ResourceSummaryProxy#generateSummary(org.hl7.fhir.instance.model.Resource)
	 */
	@Override
	public Resource generateSummary(Resource resource) throws Exception {

		StructureDefinition summary = null;

		try {
			// Cast original resource to expected type
			StructureDefinition original = (StructureDefinition) resource;

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
			summary.setMapping(null);
			summary.setSnapshot(null);
			summary.setDifferential(null);
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

		StructureDefinition summary = null;

		try {
			// Cast original resource to expected type
			StructureDefinition original = (StructureDefinition) resource;

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

		StructureDefinition summary = null;

		try {
			// Cast original resource to expected type
			StructureDefinition original = (StructureDefinition) resource;

			// Instantiate summary resource
			summary = new StructureDefinition();

			// Copy text-only DomainResource elements
			copyTextOnlyDomainResourceElements(original, summary);

			// Set Meta Security Tag SUBSETTED
			setMetaTagSubsetted(summary);

			// Copy mandatory Resource Type elements
			summary.setUrl(original.getUrl());
			summary.setName(original.getName());
			summary.setStatus(original.getStatus());
			StructureDefinitionMappingComponent summaryMapping;
			for (StructureDefinitionMappingComponent mapping : original.getMapping()) {
				summaryMapping = new StructureDefinitionMappingComponent();
				summaryMapping.setIdentity(mapping.getIdentity());
				summary.addMapping(summaryMapping);
			}
			summary.setKind(original.getKind());
			summary.setAbstract(original.getAbstract());
			summary.setContext(original.getContext());
			summary.setType(original.getType());
			if (original.hasSnapshot()) {
				StructureDefinitionSnapshotComponent summarySnapshot = new StructureDefinitionSnapshotComponent();
				for (ElementDefinition element : original.getSnapshot().getElement()) {
					ElementDefinition summaryElement = getSummaryElementDefintion(element);
					if (summaryElement != null) {
						summarySnapshot.addElement(summaryElement);
					}
				}
				summary.setSnapshot(summarySnapshot);
			}
			if (original.hasDifferential()) {
				StructureDefinitionDifferentialComponent summaryDifferential = new StructureDefinitionDifferentialComponent();
				for (ElementDefinition element : original.getDifferential().getElement()) {
					ElementDefinition summaryElement = getSummaryElementDefintion(element);
					if (summaryElement != null) {
						summaryDifferential.addElement(summaryElement);
					}
				}
				summary.setDifferential(summaryDifferential);
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		}

		return summary;
	}

	private ElementDefinition getSummaryElementDefintion(ElementDefinition element) {
		ElementDefinition summaryElement = new ElementDefinition();

		summaryElement.setPath(element.getPath());
		if (element.hasSlicing()) {
			ElementDefinitionSlicingComponent summaryElementSlicing = new ElementDefinitionSlicingComponent();
			summaryElementSlicing.setDiscriminator(element.getSlicing().getDiscriminator());
			summaryElementSlicing.setRules(element.getSlicing().getRules());
			summaryElement.setSlicing(summaryElementSlicing);
		}
		if (element.hasBase()) {
			ElementDefinitionBaseComponent summaryElementBase = new ElementDefinitionBaseComponent();
			summaryElementBase.setPath(element.getBase().getPath());
			summaryElementBase.setMin(element.getBase().getMin());
			summaryElementBase.setMax(element.getBase().getMax());
			summaryElement.setBase(summaryElementBase);
		}
		TypeRefComponent summaryElementType;
		for (TypeRefComponent type : element.getType()) {
			summaryElementType = new TypeRefComponent();
			summaryElementType.setCode(type.getCode());
			summaryElement.addType(summaryElementType);
		}
		summaryElement.setExample(element.getExample());
		ElementDefinitionConstraintComponent summaryElementConstraint;
		for (ElementDefinitionConstraintComponent constraint : element.getConstraint()) {
			summaryElementConstraint = new ElementDefinitionConstraintComponent();
			summaryElementConstraint.setKey(constraint.getKey());
			summaryElementConstraint.setSeverity(constraint.getSeverity());
			summaryElementConstraint.setHuman(constraint.getHuman());
			summaryElement.addConstraint(summaryElementConstraint);
		}
		if (element.hasBinding()) {
			ElementDefinitionBindingComponent summaryElementBinding = new ElementDefinitionBindingComponent();
			summaryElementBinding.setStrength(element.getBinding().getStrength());
			summaryElement.setBinding(summaryElementBinding);
		}
		ElementDefinitionMappingComponent summaryElementMapping;
		for (ElementDefinitionMappingComponent mapping : element.getMapping()) {
			summaryElementMapping = new ElementDefinitionMappingComponent();
			summaryElementMapping.setIdentity(mapping.getIdentity());
			summaryElementMapping.setMap(mapping.getMap());
			summaryElement.addMapping(summaryElementMapping);
		}

		return summaryElement;
	}

}
