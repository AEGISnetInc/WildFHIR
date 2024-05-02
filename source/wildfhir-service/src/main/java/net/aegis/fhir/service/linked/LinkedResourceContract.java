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
package net.aegis.fhir.service.linked;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.hl7.fhir.r4.model.Contract;
import org.hl7.fhir.r4.model.Contract.ActionComponent;
import org.hl7.fhir.r4.model.Contract.ActionSubjectComponent;
import org.hl7.fhir.r4.model.Contract.AnswerComponent;
import org.hl7.fhir.r4.model.Contract.AssetContextComponent;
import org.hl7.fhir.r4.model.Contract.ComputableLanguageComponent;
import org.hl7.fhir.r4.model.Contract.ContractAssetComponent;
import org.hl7.fhir.r4.model.Contract.ContractPartyComponent;
import org.hl7.fhir.r4.model.Contract.FriendlyLanguageComponent;
import org.hl7.fhir.r4.model.Contract.LegalLanguageComponent;
import org.hl7.fhir.r4.model.Contract.SignatoryComponent;
import org.hl7.fhir.r4.model.Contract.TermComponent;
import org.hl7.fhir.r4.model.Contract.ValuedItemComponent;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;

import net.aegis.fhir.service.ResourceService;

/**
 * @author richard.ettema
 *
 */
public class LinkedResourceContract extends LinkedResourceProxy {

	private Logger log = Logger.getLogger("LinkedResourceContract");

	/* (non-Javadoc)
	 * @see net.aegis.fhir.service.linked.LinkedResourceProxy#getLinkedResources(net.aegis.fhir.service.ResourceService, org.hl7.fhir.instance.model.Resource)
	 */
	@Override
	public List<Resource> getLinkedResources(ResourceService resourceService, Resource containerResource) throws Exception {

		log.fine("[START] LinkedResourceContract.getLinkedResources()");

		List<Resource> linkedResources = new ArrayList<Resource>();

		try {
			// Test for valid container Resource
			if (containerResource != null && containerResource instanceof Contract) {

				Contract typedContainerResource = (Contract) containerResource;

				/*
				 * Contract linked Resource references
				 */
				String ref = null;
				Resource linkedResource = null;

				// instantiatesCanonical
				if (typedContainerResource.hasInstantiatesCanonical() && typedContainerResource.getInstantiatesCanonical().hasReference()) {

					ref = typedContainerResource.getInstantiatesCanonical().getReference();
					linkedResource = this.getLinkedResourceAny(resourceService, ref);

					if (linkedResource != null) {
						linkedResources.add(linkedResource);
					}
				}

				// subject
				if (typedContainerResource.hasSubject()) {

					for (Reference subject : typedContainerResource.getSubject()) {
						if (subject.hasReference()) {

							ref = subject.getReference();
							linkedResource = this.getLinkedResourceAny(resourceService, ref);

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}

				linkedResource = null;
				// authority (Organization)
				if (typedContainerResource.hasAuthority()) {

					for (Reference authority : typedContainerResource.getAuthority()) {
						if (authority.hasReference()) {

							ref = authority.getReference();
							linkedResource = this.getLinkedResource(resourceService, ref, "Organization");

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}

				linkedResource = null;
				// domain (Location)
				if (typedContainerResource.hasDomain()) {

					for (Reference domain : typedContainerResource.getDomain()) {
						if (domain.hasReference()) {

							ref = domain.getReference();
							linkedResource = this.getLinkedResource(resourceService, ref, "Location");

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}

				linkedResource = null;
				// site (Location)
				if (typedContainerResource.hasSite()) {

					for (Reference site : typedContainerResource.getSite()) {
						if (site.hasReference()) {

							ref = site.getReference();
							linkedResource = this.getLinkedResource(resourceService, ref, "Location");

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}

				linkedResource = null;
				// topicReference
				if (typedContainerResource.hasTopicReference() && typedContainerResource.getTopicReference().hasReference()) {

					ref = typedContainerResource.getTopicReference().getReference();
					linkedResource = this.getLinkedResourceAny(resourceService, ref);

					if (linkedResource != null) {
						linkedResources.add(linkedResource);
					}
				}

				linkedResource = null;
				// contentDefinition.publisher
				if (typedContainerResource.hasContentDefinition() && typedContainerResource.getContentDefinition().hasPublisher()) {

					ref = typedContainerResource.getContentDefinition().getPublisher().getReference();
					linkedResource = this.getLinkedResourceAny(resourceService, ref);

					if (linkedResource != null) {
						linkedResources.add(linkedResource);
					}
				}

				linkedResource = null;
				// term (group)
				// Call getTerm method to handle recursion into sub-groups
				if (typedContainerResource.hasTerm()) {
					this.getTerm(resourceService, typedContainerResource.getTerm(), linkedResources);
				}

				linkedResource = null;
				// supportingInfo
				if (typedContainerResource.hasSupportingInfo()) {

					for (Reference supportingInfo : typedContainerResource.getSupportingInfo()) {
						if (supportingInfo.hasReference()) {

							ref = supportingInfo.getReference();
							linkedResource = this.getLinkedResourceAny(resourceService, ref);

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}

				linkedResource = null;
				// relevantHistory (Provenance)
				if (typedContainerResource.hasRelevantHistory()) {

					for (Reference relevantHistory : typedContainerResource.getRelevantHistory()) {
						if (relevantHistory.hasReference()) {

							ref = relevantHistory.getReference();
							linkedResource = this.getLinkedResource(resourceService, ref, "Provenance");

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}

				linkedResource = null;
				// signer.party
				if (typedContainerResource.hasSigner()) {

					for (SignatoryComponent signer : typedContainerResource.getSigner()) {

						if (signer.hasParty() && signer.getParty().hasReference()) {

							ref = signer.getParty().getReference();
							linkedResource = this.getLinkedResourceAny(resourceService, ref);

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}

				linkedResource = null;
				// friendly.contentReference
				if (typedContainerResource.hasFriendly()) {

					for (FriendlyLanguageComponent friendly : typedContainerResource.getFriendly()) {
						if (friendly.hasContentReference() && friendly.getContentReference().hasReference()) {

							ref = friendly.getContentReference().getReference();
							linkedResource = this.getLinkedResourceAny(resourceService, ref);

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}

				linkedResource = null;
				// legal.contentReference
				if (typedContainerResource.hasLegal()) {

					for (LegalLanguageComponent legal : typedContainerResource.getLegal()) {
						if (legal.hasContentReference() && legal.getContentReference().hasReference()) {

							ref = legal.getContentReference().getReference();
							linkedResource = this.getLinkedResourceAny(resourceService, ref);

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}

				linkedResource = null;
				// rule.contentReference
				if (typedContainerResource.hasRule()) {

					for (ComputableLanguageComponent rule : typedContainerResource.getRule()) {
						if (rule.hasContentReference() && rule.getContentReference().hasReference()) {

							ref = rule.getContentReference().getReference();
							linkedResource = this.getLinkedResourceAny(resourceService, ref);

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}
					}
				}
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
        }

		return linkedResources;
	}

	private void getTerm(ResourceService resourceService, List<TermComponent> terms, List<Resource> linkedResources) throws Exception {

		log.fine("[START] LinkedResourceContract.getTerm()");

		String ref = null;
		Resource linkedResource = null;

		if (terms != null) {

			for (TermComponent term : terms) {
				// term.topicReference
				if (term.hasTopicReference() && term.getTopicReference().hasReference()) {

					ref = term.getTopicReference().getReference();
					linkedResource = this.getLinkedResourceAny(resourceService, ref);

					if (linkedResource != null) {
						linkedResources.add(linkedResource);
					}
				}

				linkedResource = null;
				// term.offer
				if (term.hasOffer()) {

					// term.offer.party.reference
					if (term.getOffer().hasParty()) {

						for (ContractPartyComponent party : term.getOffer().getParty()) {
							if (party.hasReference()) {

								for (Reference partyRef : party.getReference()) {
									if (partyRef.hasReference()) {
										ref = partyRef.getReference();
										linkedResource = this.getLinkedResourceAny(resourceService, ref);

										if (linkedResource != null) {
											linkedResources.add(linkedResource);
										}
									}
								}
							}
						}
					}

					linkedResource = null;
					// term.offer.topic
					if (term.getOffer().hasTopic() && term.getOffer().getTopic().hasReference()) {

						ref = term.getOffer().getTopic().getReference();
						linkedResource = this.getLinkedResourceAny(resourceService, ref);

						if (linkedResource != null) {
							linkedResources.add(linkedResource);
						}
					}

					linkedResource = null;
					// term.offer.answer.valueReference
					if (term.getOffer().hasAnswer()) {

						for (AnswerComponent answer : term.getOffer().getAnswer()) {

							if (answer.hasValueReference() && answer.getValueReference().hasReference()) {

								ref = answer.getValueReference().getReference();
								linkedResource = this.getLinkedResourceAny(resourceService, ref);

								if (linkedResource != null) {
									linkedResources.add(linkedResource);
								}
							}
						}
					}
				}

				// term.asset
				if (term.hasAsset()) {

					for (ContractAssetComponent asset : term.getAsset()) {

						linkedResource = null;
						// term.asset.typeReference
						if (asset.hasTypeReference()) {

							for (Reference typeReference : asset.getTypeReference()) {
								if (typeReference.hasReference()) {

									ref = typeReference.getReference();
									linkedResource = this.getLinkedResourceAny(resourceService, ref);

									if (linkedResource != null) {
										linkedResources.add(linkedResource);
									}
								}
							}
						}

						linkedResource = null;
						// term.asset.context.reference
						if (asset.hasContext()) {

							for (AssetContextComponent context : asset.getContext()) {

								if (context.hasReference() && context.getReference().hasReference()) {

									ref = context.getReference().getReference();
									linkedResource = this.getLinkedResourceAny(resourceService, ref);

									if (linkedResource != null) {
										linkedResources.add(linkedResource);
									}
								}
							}
						}

						linkedResource = null;
						// term.asset.answer.valueReference
						if (asset.hasAnswer()) {

							for (AnswerComponent answer : asset.getAnswer()) {

								if (answer.hasValueReference() && answer.getValueReference().hasReference()) {

									ref = answer.getValueReference().getReference();
									linkedResource = this.getLinkedResourceAny(resourceService, ref);

									if (linkedResource != null) {
										linkedResources.add(linkedResource);
									}
								}
							}
						}

						linkedResource = null;
						// term.asset.valuedItem
						if (asset.hasValuedItem()) {

							for (ValuedItemComponent valuedItem : asset.getValuedItem()) {
								// term.asset.valuedItem.entityReference
								if (valuedItem.hasEntityReference() && valuedItem.getEntityReference().hasReference()) {

									ref = valuedItem.getEntityReference().getReference();
									linkedResource = this.getLinkedResourceAny(resourceService, ref);

									if (linkedResource != null) {
										linkedResources.add(linkedResource);
									}
								}

								linkedResource = null;
								// term.asset.valuedItem.responsible
								if (valuedItem.hasResponsible() && valuedItem.getResponsible().hasReference()) {

									ref = valuedItem.getResponsible().getReference();
									linkedResource = this.getLinkedResourceAny(resourceService, ref);

									if (linkedResource != null) {
										linkedResources.add(linkedResource);
									}
								}

								linkedResource = null;
								// term.asset.valuedItem.recipient
								if (valuedItem.hasRecipient() && valuedItem.getRecipient().hasReference()) {

									ref = valuedItem.getRecipient().getReference();
									linkedResource = this.getLinkedResourceAny(resourceService, ref);

									if (linkedResource != null) {
										linkedResources.add(linkedResource);
									}
								}
							}
						}
					}
				}


				// term.action
				if (term.hasAction()) {

					for (ActionComponent action : term.getAction()) {

						// term.action.subject.reference
						if (action.hasSubject()) {

							for (ActionSubjectComponent subject : action.getSubject()) {
								if (subject.hasReference()) {

									for (Reference subjectRef : subject.getReference()) {
										if (subjectRef.hasReference()) {
											ref = subjectRef.getReference();
											linkedResource = this.getLinkedResourceAny(resourceService, ref);

											if (linkedResource != null) {
												linkedResources.add(linkedResource);
											}
										}
									}
								}
							}
						}

						linkedResource = null;
						// term.action.context
						if (action.hasContext() && action.getContext().hasReference()) {

							ref = action.getContext().getReference();
							linkedResource = this.getLinkedResourceAny(resourceService, ref);

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}

						linkedResource = null;
						// term.action.requester
						if (action.hasRequester()) {

							for (Reference requester : action.getRequester()) {
								if (requester.hasReference()) {
									ref = requester.getReference();
									linkedResource = this.getLinkedResourceAny(resourceService, ref);

									if (linkedResource != null) {
										linkedResources.add(linkedResource);
									}
								}
							}
						}

						linkedResource = null;
						// term.action.performer
						if (action.hasPerformer() && action.getPerformer().hasReference()) {

							ref = action.getPerformer().getReference();
							linkedResource = this.getLinkedResourceAny(resourceService, ref);

							if (linkedResource != null) {
								linkedResources.add(linkedResource);
							}
						}

						linkedResource = null;
						// term.action.reasonReference
						if (action.hasReasonReference()) {

							for (Reference reasonReference : action.getReasonReference()) {
								if (reasonReference.hasReference()) {
									ref = reasonReference.getReference();
									linkedResource = this.getLinkedResourceAny(resourceService, ref);

									if (linkedResource != null) {
										linkedResources.add(linkedResource);
									}
								}
							}
						}
					}
				}

				// term.group
				// Call getTerm method to handle recursion into sub-groups
				if (term.hasGroup()) {
					this.getTerm(resourceService, term.getGroup(), linkedResources);
				}
			}
		}
	}

}
