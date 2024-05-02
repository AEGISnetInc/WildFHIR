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

import org.hl7.fhir.r4.model.Contract;
import org.hl7.fhir.r4.model.Contract.ActionComponent;
import org.hl7.fhir.r4.model.Contract.ActionSubjectComponent;
import org.hl7.fhir.r4.model.Contract.ContentDefinitionComponent;
import org.hl7.fhir.r4.model.Contract.ContractOfferComponent;
import org.hl7.fhir.r4.model.Contract.SecurityLabelComponent;
import org.hl7.fhir.r4.model.Contract.TermComponent;
import org.hl7.fhir.r4.model.Resource;

/**
 * @author richard.ettema
 *
 */
public class ResourceSummaryContract extends ResourceSummaryProxy {

	/* (non-Javadoc)
	 * @see net.aegis.fhir.service.summary.ResourceSummaryProxy#generateSummary(org.hl7.fhir.instance.model.Resource)
	 */
	@Override
	public Resource generateSummary(Resource resource) throws Exception {

		Contract summary = null;

		try {
			// Cast original resource to expected type
			Contract original = (Contract) resource;

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
			summary.setUrl(null);
			summary.setLegalState(null);
			summary.setInstantiatesCanonical(null);
			summary.setInstantiatesUri(null);
			summary.setContentDerivative(null);
			summary.setExpirationType(null);
			summary.setAuthority(null);
			summary.setDomain(null);
			summary.setSite(null);
			summary.setSubtitle(null);
			summary.setAlias(null);
			summary.setAuthor(null);
			summary.setScope(null);
			summary.setTopic(null);
			summary.setContentDefinition(null);
			for (TermComponent term : summary.getTerm()) {
				setSummaryTermComponent(term);
			}
			summary.setSupportingInfo(null);
			summary.setRelevantHistory(null);
			summary.setSigner(null);
			summary.setFriendly(null);
			summary.setLegal(null);
			summary.setRule(null);
			summary.setLegallyBinding(null);
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

		Contract summary = null;

		try {
			// Cast original resource to expected type
			Contract original = (Contract) resource;

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

		Contract summary = null;

		try {
			// Cast original resource to expected type
			Contract original = (Contract) resource;

			// Instantiate summary resource
			summary = new Contract();

			// Copy text-only DomainResource elements
			copyTextOnlyDomainResourceElements(original, summary);

			// Set Meta Security Tag SUBSETTED
			setMetaTagSubsetted(summary);

			// Copy mandatory Resource Type elements
			if (original.hasContentDefinition()) {
				ContentDefinitionComponent summaryContentDefintion = new ContentDefinitionComponent();
				summaryContentDefintion.setType(original.getContentDefinition().getType());
				summaryContentDefintion.setPublicationStatus(original.getContentDefinition().getPublicationStatus());
				summary.setContentDefinition(summaryContentDefintion);
			}
			for (TermComponent term : original.getTerm()) {
				TermComponent summaryTerm = getSummaryTermComponent(term);
				if (summaryTerm != null) {
					summary.addTerm(summaryTerm);
				}
			}
			summary.setSigner(original.getSigner());
			summary.setFriendly(original.getFriendly());
			summary.setLegal(original.getLegal());
			summary.setRule(original.getRule());

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		}

		return summary;
	}

	private void setSummaryTermComponent(TermComponent term) {
		if (term != null) {
			term.setTopic(null);
			term.setType(null);
			term.setSubType(null);
			term.setSecurityLabel(null);
			if (term.hasOffer()) {
				term.getOffer().setIdentifier(null);
				term.getOffer().setParty(null);
				term.getOffer().setType(null);
				term.getOffer().setDecision(null);
				term.getOffer().setDecisionMode(null);
				term.getOffer().setAnswer(null);
				term.getOffer().setText(null);
				term.getOffer().setLinkId(null);
				term.getOffer().setSecurityLabelNumber(null);
			}
			term.setAsset(null);
			term.setAction(null);
			for (TermComponent group : term.getGroup()) {
				setSummaryTermComponent(group);
			}
		}
	}

	private TermComponent getSummaryTermComponent(TermComponent term) {
		TermComponent summaryTerm = null;

		if (term != null) {

			if (term.hasSecurityLabel()) {
				if (summaryTerm == null) {
					summaryTerm = new TermComponent();
				}
				SecurityLabelComponent summaryTermSecurityLabel = null;
				for (SecurityLabelComponent securityLabel : term.getSecurityLabel()) {
					summaryTermSecurityLabel = new SecurityLabelComponent();
					summaryTermSecurityLabel.setClassification(securityLabel.getClassification());
					summaryTerm.addSecurityLabel(summaryTermSecurityLabel);
				}
			}

			if (term.hasOffer()) {
				if (summaryTerm == null) {
					summaryTerm = new TermComponent();
				}
				ContractOfferComponent summaryTermOffer = new ContractOfferComponent();
				summaryTermOffer.setParty(term.getOffer().getParty());
				summaryTermOffer.setAnswer(term.getOffer().getAnswer());
				summaryTermOffer.setTopic(term.getOffer().getTopic());
				summaryTerm.setOffer(summaryTermOffer);
			}

			if (term.hasAction()) {
				if (summaryTerm == null) {
					summaryTerm = new TermComponent();
				}
				ActionComponent summaryTermAction = null;
				for (ActionComponent action : term.getAction()) {
					summaryTermAction = new ActionComponent();
					summaryTermAction.setType(action.getType());
					ActionSubjectComponent summaryTermActionSubject = null;
					for (ActionSubjectComponent subject : action.getSubject()) {
						summaryTermActionSubject = new ActionSubjectComponent();
						summaryTermActionSubject.setReference(subject.getReference());
						summaryTermAction.addSubject(summaryTermActionSubject);
					}
					summaryTermAction.setIntent(action.getIntent());
					summaryTermAction.setStatus(action.getStatus());
					summaryTerm.addAction(summaryTermAction);
				}
			}

			if (term.hasGroup()) {
				if (summaryTerm == null) {
					summaryTerm = new TermComponent();
				}
				for (TermComponent group : term.getGroup()) {
					TermComponent summaryGroup = getSummaryTermComponent(group);
					if (summaryGroup != null) {
						summaryTerm.addGroup(summaryGroup);
					}
				}
			}
		}

		return summaryTerm;
	}

}
