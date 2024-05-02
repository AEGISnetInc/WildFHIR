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

import org.hl7.fhir.r4.model.RequestGroup;
import org.hl7.fhir.r4.model.RequestGroup.RequestGroupActionComponent;
import org.hl7.fhir.r4.model.RequestGroup.RequestGroupActionConditionComponent;
import org.hl7.fhir.r4.model.RequestGroup.RequestGroupActionRelatedActionComponent;
import org.hl7.fhir.r4.model.Resource;

/**
 * @author richard.ettema
 *
 */
public class ResourceSummaryRequestGroup extends ResourceSummaryProxy {

	/* (non-Javadoc)
	 * @see net.aegis.fhir.service.summary.ResourceSummaryProxy#generateSummary(org.hl7.fhir.instance.model.Resource)
	 */
	@Override
	public Resource generateSummary(Resource resource) throws Exception {

		RequestGroup summary = null;

		try {
			// Cast original resource to expected type
			RequestGroup original = (RequestGroup) resource;

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
			summary.setBasedOn(null);
			summary.setReplaces(null);
			summary.setSubject(null);
			summary.setEncounter(null);
			summary.setAuthoredOn(null);
			summary.setAuthor(null);
			summary.setReasonCode(null);
			summary.setReasonReference(null);
			summary.setNote(null);
			for (RequestGroupActionComponent action : summary.getAction()) {
				setRequestGroupActionComponent(action);
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

		RequestGroup summary = null;

		try {
			// Cast original resource to expected type
			RequestGroup original = (RequestGroup) resource;

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

		RequestGroup summary = null;

		try {
			// Cast original resource to expected type
			RequestGroup original = (RequestGroup) resource;

			// Instantiate summary resource
			summary = new RequestGroup();

			// Copy text-only DomainResource elements
			copyTextOnlyDomainResourceElements(original, summary);

			// Set Meta Security Tag SUBSETTED
			setMetaTagSubsetted(summary);

			// Copy mandatory Resource Type elements
			summary.setStatus(original.getStatus());
			summary.setIntent(original.getIntent());
			RequestGroupActionComponent summaryAction;
			for (RequestGroupActionComponent action : original.getAction()) {
				summaryAction = getRequestGroupActionComponent(action);
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

	private void setRequestGroupActionComponent(RequestGroupActionComponent action) {

		if (action != null) {
			action.setPrefix(null);
			action.setTitle(null);
			action.setPriority(null);
			action.setCode(null);
			action.setDocumentation(null);
			action.setCondition(null);
			action.setRelatedAction(null);
			action.setTiming(null);
			action.setParticipant(null);
			action.setType(null);
			action.setGroupingBehavior(null);
			action.setSelectionBehavior(null);
			action.setRequiredBehavior(null);
			action.setPrecheckBehavior(null);
			action.setCardinalityBehavior(null);
			action.setResource(null);

			for (RequestGroupActionComponent actionAction : action.getAction()) {
				setRequestGroupActionComponent(actionAction);
			}
		}
	}

	private RequestGroupActionComponent getRequestGroupActionComponent(RequestGroupActionComponent action) {
		RequestGroupActionComponent summaryAction = null;

		if (action.hasCondition() || action.hasRelatedAction()) {
			summaryAction = new RequestGroupActionComponent();

			RequestGroupActionConditionComponent summaryActionCondition;
			for (RequestGroupActionConditionComponent condition : action.getCondition()) {
				summaryActionCondition = new RequestGroupActionConditionComponent();
				summaryActionCondition.setKind(condition.getKind());
				summaryAction.addCondition(summaryActionCondition);
			}

			RequestGroupActionRelatedActionComponent summaryActionRelatedAction;
			for (RequestGroupActionRelatedActionComponent relatedAction : action.getRelatedAction()) {
				summaryActionRelatedAction = new RequestGroupActionRelatedActionComponent();
				summaryActionRelatedAction.setActionId(relatedAction.getActionId());
				summaryActionRelatedAction.setRelationship(relatedAction.getRelationship());
				summaryAction.addRelatedAction(summaryActionRelatedAction);
			}

			RequestGroupActionComponent summaryActionAction;
			for (RequestGroupActionComponent actionAction : action.getAction()) {
				summaryActionAction = getRequestGroupActionComponent(actionAction);
				if (summaryActionAction != null) {
					summaryAction.addAction(summaryActionAction);
				}
			}
		}

		return summaryAction;
	}

}
