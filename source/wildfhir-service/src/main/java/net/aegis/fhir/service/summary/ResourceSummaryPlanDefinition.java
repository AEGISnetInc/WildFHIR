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

import org.hl7.fhir.r4.model.PlanDefinition;
import org.hl7.fhir.r4.model.PlanDefinition.PlanDefinitionActionComponent;
import org.hl7.fhir.r4.model.PlanDefinition.PlanDefinitionActionConditionComponent;
import org.hl7.fhir.r4.model.PlanDefinition.PlanDefinitionActionParticipantComponent;
import org.hl7.fhir.r4.model.PlanDefinition.PlanDefinitionActionRelatedActionComponent;
import org.hl7.fhir.r4.model.PlanDefinition.PlanDefinitionGoalComponent;
import org.hl7.fhir.r4.model.Resource;

/**
 * @author richard.ettema
 *
 */
public class ResourceSummaryPlanDefinition extends ResourceSummaryProxy {

	/* (non-Javadoc)
	 * @see net.aegis.fhir.service.summary.ResourceSummaryProxy#generateSummary(org.hl7.fhir.instance.model.Resource)
	 */
	@Override
	public Resource generateSummary(Resource resource) throws Exception {

		PlanDefinition summary = null;

		try {
			// Cast original resource to expected type
			PlanDefinition original = (PlanDefinition) resource;

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
			summary.setSubtitle(null);
			summary.setSubject(null);
			summary.setPurpose(null);
			summary.setUsage(null);
			summary.setCopyright(null);
			summary.setApprovalDate(null);
			summary.setLastReviewDate(null);
			summary.setTopic(null);
			summary.setAuthor(null);
			summary.setEditor(null);
			summary.setReviewer(null);
			summary.setEndorser(null);
			summary.setRelatedArtifact(null);
			summary.setLibrary(null);
			summary.setGoal(null);
			summary.setAction(null);
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

		PlanDefinition summary = null;

		try {
			// Cast original resource to expected type
			PlanDefinition original = (PlanDefinition) resource;

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

		PlanDefinition summary = null;

		try {
			// Cast original resource to expected type
			PlanDefinition original = (PlanDefinition) resource;

			// Instantiate summary resource
			summary = new PlanDefinition();

			// Copy text-only DomainResource elements
			copyTextOnlyDomainResourceElements(original, summary);

			// Set Meta Security Tag SUBSETTED
			setMetaTagSubsetted(summary);

			// Copy mandatory Resource Type elements
			summary.setStatus(original.getStatus());
			PlanDefinitionGoalComponent summaryGoal;
			for (PlanDefinitionGoalComponent goal : original.getGoal()) {
				summaryGoal = new PlanDefinitionGoalComponent();
				summaryGoal.setDescription(goal.getDescription());
				summary.addGoal(summaryGoal);
			}
			PlanDefinitionActionComponent summaryAction;
			for (PlanDefinitionActionComponent action : original.getAction()) {
				summaryAction = getSummaryPlanDefinitionActionComponent(action);
				if (summaryAction != null) {
					summary.addAction(summaryAction);
				}
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		}

		return summary;
	}

	private PlanDefinitionActionComponent getSummaryPlanDefinitionActionComponent(PlanDefinitionActionComponent action) {
		PlanDefinitionActionComponent summaryAction = null;

		if (action.hasCondition() || action.hasRelatedAction() || action.hasParticipant() || action.hasAction()) {
			summaryAction = new PlanDefinitionActionComponent();

			if (action.hasCondition()) {
				PlanDefinitionActionConditionComponent summaryActionCondition;
				for (PlanDefinitionActionConditionComponent condition : action.getCondition()) {
					summaryActionCondition = new PlanDefinitionActionConditionComponent();
					summaryActionCondition.setKind(condition.getKind());
					summaryAction.addCondition(summaryActionCondition);
				}
			}
			if (action.hasRelatedAction()) {
				PlanDefinitionActionRelatedActionComponent summaryRelatedAction;
				for (PlanDefinitionActionRelatedActionComponent relatedAction : action.getRelatedAction()) {
					summaryRelatedAction = new PlanDefinitionActionRelatedActionComponent();
					summaryRelatedAction.setActionId(relatedAction.getActionId());
					summaryRelatedAction.setRelationship(relatedAction.getRelationship());
					summaryAction.addRelatedAction(summaryRelatedAction);
				}
			}
			if (action.hasParticipant()) {
				PlanDefinitionActionParticipantComponent summaryActionParticipant;
				for (PlanDefinitionActionParticipantComponent participant : action.getParticipant()) {
					summaryActionParticipant = new PlanDefinitionActionParticipantComponent();
					summaryActionParticipant.setType(participant.getType());
					summaryAction.addParticipant(summaryActionParticipant);
				}
			}
			if (action.hasAction()) {
				PlanDefinitionActionComponent summaryActionAction = null;
				for (PlanDefinitionActionComponent actionAction : summaryAction.getAction()) {
					summaryActionAction = getSummaryPlanDefinitionActionComponent(actionAction);
					if (summaryActionAction != null) {
						summaryAction.addAction(summaryActionAction);
					}
				}
			}
		}

		return summaryAction;
	}
}
