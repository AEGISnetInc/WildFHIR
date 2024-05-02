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
import org.hl7.fhir.r4.model.StructureMap;
import org.hl7.fhir.r4.model.StructureMap.StructureMapGroupComponent;
import org.hl7.fhir.r4.model.StructureMap.StructureMapGroupInputComponent;
import org.hl7.fhir.r4.model.StructureMap.StructureMapGroupRuleComponent;
import org.hl7.fhir.r4.model.StructureMap.StructureMapGroupRuleSourceComponent;
import org.hl7.fhir.r4.model.StructureMap.StructureMapGroupRuleTargetComponent;
import org.hl7.fhir.r4.model.StructureMap.StructureMapGroupRuleTargetParameterComponent;
import org.hl7.fhir.r4.model.StructureMap.StructureMapStructureComponent;

/**
 * @author richard.ettema
 *
 */
public class ResourceSummaryStructureMap extends ResourceSummaryProxy {

	/* (non-Javadoc)
	 * @see net.aegis.fhir.service.summary.ResourceSummaryProxy#generateSummary(org.hl7.fhir.instance.model.Resource)
	 */
	@Override
	public Resource generateSummary(Resource resource) throws Exception {

		StructureMap summary = null;

		try {
			// Cast original resource to expected type
			StructureMap original = (StructureMap) resource;

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
			for (StructureMapStructureComponent structure : summary.getStructure()) {
				structure.setDocumentation(null);
			}
			for (StructureMapGroupComponent group : summary.getGroup()) {
				for (StructureMapGroupInputComponent input : group.getInput()) {
					input.setDocumentation(null);
				}
				for (StructureMapGroupRuleComponent rule : group.getRule()) {
					setSummaryGroupRuleComponent(rule);
				}
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

		StructureMap summary = null;

		try {
			// Cast original resource to expected type
			StructureMap original = (StructureMap) resource;

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

		StructureMap summary = null;

		try {
			// Cast original resource to expected type
			StructureMap original = (StructureMap) resource;

			// Instantiate summary resource
			summary = new StructureMap();

			// Copy text-only DomainResource elements
			copyTextOnlyDomainResourceElements(original, summary);

			// Set Meta Security Tag SUBSETTED
			setMetaTagSubsetted(summary);

			// Copy mandatory Resource Type elements
			summary.setUrl(original.getUrl());
			summary.setName(original.getName());
			summary.setStatus(original.getStatus());
			StructureMapStructureComponent summaryStructure;
			for (StructureMapStructureComponent structure : original.getStructure()) {
				summaryStructure = new StructureMapStructureComponent();
				summaryStructure.setUrl(structure.getUrl());
				summaryStructure.setMode(structure.getMode());
				summary.addStructure(summaryStructure);
			}
			StructureMapGroupComponent summaryGroup;
			for (StructureMapGroupComponent group : original.getGroup()) {
				summaryGroup = new StructureMapGroupComponent();
				summaryGroup.setName(group.getName());
				summaryGroup.setTypeMode(group.getTypeMode());
				StructureMapGroupInputComponent summaryGroupInput;
				for (StructureMapGroupInputComponent input : group.getInput()) {
					summaryGroupInput = new StructureMapGroupInputComponent();
					summaryGroupInput.setName(input.getName());
					summaryGroupInput.setMode(input.getMode());
					summaryGroup.addInput(summaryGroupInput);
				}
				StructureMapGroupRuleComponent summaryGroupRule;
				for (StructureMapGroupRuleComponent rule : group.getRule()) {
					summaryGroupRule = getSummaryGroupRuleComponent(rule);
					if (summaryGroupRule != null) {
						summaryGroup.addRule(summaryGroupRule);
					}
				}
				summary.addGroup(summaryGroup);
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		}

		return summary;
	}

	private void setSummaryGroupRuleComponent(StructureMapGroupRuleComponent rule) {
		if (rule != null) {
			rule.setDocumentation(null);
			for (StructureMapGroupRuleComponent ruleRule : rule.getRule()) {
				setSummaryGroupRuleComponent(ruleRule);
			}
		}
	}

	private StructureMapGroupRuleComponent getSummaryGroupRuleComponent(StructureMapGroupRuleComponent rule) {
		StructureMapGroupRuleComponent summaryGroupRule = null;

		if (rule != null) {
			summaryGroupRule = new StructureMapGroupRuleComponent();
			summaryGroupRule.setName(rule.getName());
			StructureMapGroupRuleSourceComponent summaryGroupRuleSource;
			for (StructureMapGroupRuleSourceComponent source : rule.getSource()) {
				summaryGroupRuleSource = new StructureMapGroupRuleSourceComponent();
				summaryGroupRuleSource.setContext(source.getContext());
				summaryGroupRule.addSource(summaryGroupRuleSource);
			}
			StructureMapGroupRuleTargetComponent summaryGroupRuleTarget;
			for (StructureMapGroupRuleTargetComponent target : rule.getTarget()) {
				if (target.hasParameter()) {
					summaryGroupRuleTarget = new StructureMapGroupRuleTargetComponent();
					StructureMapGroupRuleTargetParameterComponent summaryGroupRuleTargetParameter;
					for (StructureMapGroupRuleTargetParameterComponent parameter : target.getParameter()) {
						summaryGroupRuleTargetParameter = new StructureMapGroupRuleTargetParameterComponent();
						summaryGroupRuleTargetParameter.setValue(parameter.getValue());
						summaryGroupRuleTarget.addParameter(summaryGroupRuleTargetParameter);
					}
					summaryGroupRule.addTarget(summaryGroupRuleTarget);
				}
			}
			summaryGroupRule.setDependent(rule.getDependent());
			StructureMapGroupRuleComponent summaryGroupRuleRule;
			for (StructureMapGroupRuleComponent ruleRule : rule.getRule()) {
				summaryGroupRuleRule = getSummaryGroupRuleComponent(ruleRule);
				if (summaryGroupRuleRule != null) {
					summaryGroupRule.addRule(summaryGroupRuleRule);
				}
			}
		}

		return summaryGroupRule;
	}

}
