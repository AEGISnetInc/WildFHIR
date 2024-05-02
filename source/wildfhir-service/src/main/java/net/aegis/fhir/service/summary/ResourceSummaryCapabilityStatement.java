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

import org.hl7.fhir.r4.model.CapabilityStatement;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementDocumentComponent;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementImplementationComponent;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementMessagingComponent;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementMessagingEndpointComponent;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementMessagingSupportedMessageComponent;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementRestComponent;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementRestResourceComponent;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementRestResourceOperationComponent;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementRestResourceSearchParamComponent;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementSoftwareComponent;
import org.hl7.fhir.r4.model.CapabilityStatement.ResourceInteractionComponent;
import org.hl7.fhir.r4.model.CapabilityStatement.SystemInteractionComponent;
import org.hl7.fhir.r4.model.Resource;

/**
 * @author richard.ettema
 *
 */
public class ResourceSummaryCapabilityStatement extends ResourceSummaryProxy {

	/* (non-Javadoc)
	 * @see net.aegis.fhir.service.summary.ResourceSummaryProxy#generateSummary(org.hl7.fhir.instance.model.Resource)
	 */
	@Override
	public Resource generateSummary(Resource resource) throws Exception {

		CapabilityStatement summary = null;

		try {
			// Cast original resource to expected type
			CapabilityStatement original = (CapabilityStatement) resource;

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
			for (CapabilityStatementRestComponent rest : summary.getRest()) {
				rest.setDocumentation(null);
				if (rest.hasSecurity()) {
					rest.getSecurity().setDescription(null);
				}
				for (CapabilityStatementRestResourceComponent resourceComponent : rest.getResource()) {
					resourceComponent.setDocumentation(null);
					resourceComponent.setInteraction(null);
					resourceComponent.setVersioning(null);
					resourceComponent.setReadHistoryElement(null);
					resourceComponent.setUpdateCreateElement(null);
					resourceComponent.setConditionalCreateElement(null);
					resourceComponent.setConditionalRead(null);
					resourceComponent.setConditionalUpdateElement(null);
					resourceComponent.setConditionalDelete(null);
					resourceComponent.setReferencePolicy(null);
					resourceComponent.setSearchInclude(null);
					resourceComponent.setSearchRevInclude(null);
					resourceComponent.setSearchParam(null);
					for (CapabilityStatementRestResourceOperationComponent operation : resourceComponent.getOperation()) {
						operation.setDocumentation(null);
					}
				}
				rest.setInteraction(null);
				rest.setSearchParam(null);
				for (CapabilityStatementRestResourceOperationComponent operation : rest.getOperation()) {
					operation.setDocumentation(null);
				}
				rest.setCompartment(null);
			}
			for (CapabilityStatementMessagingComponent messaging : summary.getMessaging()) {
				messaging.setEndpoint(null);
				messaging.setReliableCacheElement(null);
				messaging.setDocumentation(null);
			}
			for (CapabilityStatementDocumentComponent document : summary.getDocument()) {
				document.setDocumentation(null);
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

		CapabilityStatement summary = null;

		try {
			// Cast original resource to expected type
			CapabilityStatement original = (CapabilityStatement) resource;

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

		CapabilityStatement summary = null;

		try {
			// Cast original resource to expected type
			CapabilityStatement original = (CapabilityStatement) resource;

			// Instantiate summary resource
			summary = new CapabilityStatement();

			// Copy text-only DomainResource elements
			copyTextOnlyDomainResourceElements(original, summary);

			// Set Meta Security Tag SUBSETTED
			setMetaTagSubsetted(summary);

			// Copy mandatory Resource Type elements
			summary.setStatus(original.getStatus());
			summary.setDate(original.getDate());
			summary.setKind(original.getKind());
			if (original.hasSoftware()) {
				CapabilityStatementSoftwareComponent summarySoftware = new CapabilityStatementSoftwareComponent();
				summarySoftware.setName(original.getSoftware().getName());
				summary.setSoftware(summarySoftware);
			}
			if (original.hasImplementation()) {
				CapabilityStatementImplementationComponent summaryImplementation = new CapabilityStatementImplementationComponent();
				summaryImplementation.setDescription(original.getImplementation().getDescription());
				summary.setImplementation(summaryImplementation);
			}
			summary.setFhirVersion(original.getFhirVersion());
			summary.setFormat(original.getFormat());
			CapabilityStatementRestComponent summaryRest;
			for (CapabilityStatementRestComponent rest : original.getRest()) {
				summaryRest = new CapabilityStatementRestComponent();
				summaryRest.setMode(rest.getMode());
				CapabilityStatementRestResourceComponent summaryRestResourceComponent;
				for (CapabilityStatementRestResourceComponent resourceComponent : rest.getResource()) {
					summaryRestResourceComponent = new CapabilityStatementRestResourceComponent();
					summaryRestResourceComponent.setType(resourceComponent.getType());
					ResourceInteractionComponent summaryRestResourceComponentInteraction;
					for (ResourceInteractionComponent interaction : resourceComponent.getInteraction()) {
						summaryRestResourceComponentInteraction = new ResourceInteractionComponent();
						summaryRestResourceComponentInteraction.setCode(interaction.getCode());
						summaryRestResourceComponent.addInteraction(summaryRestResourceComponentInteraction);
					}
					CapabilityStatementRestResourceSearchParamComponent summaryRestResourceComponentSearchParam;
					for (CapabilityStatementRestResourceSearchParamComponent searchParam : resourceComponent.getSearchParam()) {
						summaryRestResourceComponentSearchParam = new CapabilityStatementRestResourceSearchParamComponent();
						summaryRestResourceComponentSearchParam.setName(searchParam.getName());
						summaryRestResourceComponentSearchParam.setType(searchParam.getType());
						summaryRestResourceComponent.addSearchParam(summaryRestResourceComponentSearchParam);
					}
					rest.addResource(summaryRestResourceComponent);
				}
				SystemInteractionComponent summaryRestInteraction;
				for (SystemInteractionComponent interaction : rest.getInteraction()) {
					summaryRestInteraction = new SystemInteractionComponent();
					summaryRestInteraction.setCode(interaction.getCode());
					summaryRest.addInteraction(summaryRestInteraction);
				}
				CapabilityStatementRestResourceSearchParamComponent summaryRestSearchParam;
				for (CapabilityStatementRestResourceSearchParamComponent searchParam : rest.getSearchParam()) {
					summaryRestSearchParam = new CapabilityStatementRestResourceSearchParamComponent();
					summaryRestSearchParam.setName(searchParam.getName());
					summaryRestSearchParam.setType(searchParam.getType());
					summaryRest.addSearchParam(summaryRestSearchParam);
				}
				CapabilityStatementRestResourceOperationComponent summaryRestOperation;
				for (CapabilityStatementRestResourceOperationComponent operation : rest.getOperation()) {
					summaryRestOperation = new CapabilityStatementRestResourceOperationComponent();
					summaryRestOperation.setName(operation.getName());
					summaryRestOperation.setDefinition(operation.getDefinition());
					summaryRest.addOperation(summaryRestOperation);
				}
				summary.addRest(summaryRest);
			}
			CapabilityStatementMessagingComponent summaryMessaging;
			for (CapabilityStatementMessagingComponent messaging : original.getMessaging()) {
				summaryMessaging = new CapabilityStatementMessagingComponent();
				CapabilityStatementMessagingEndpointComponent summaryMessagingEndpoint;
				for (CapabilityStatementMessagingEndpointComponent endpoint : messaging.getEndpoint()) {
					summaryMessagingEndpoint = new CapabilityStatementMessagingEndpointComponent();
					summaryMessagingEndpoint.setProtocol(endpoint.getProtocol());
					summaryMessagingEndpoint.setAddress(endpoint.getAddress());
					summaryMessaging.addEndpoint(summaryMessagingEndpoint);
				}
				CapabilityStatementMessagingSupportedMessageComponent summaryMessagingSupportedMessage;
				for (CapabilityStatementMessagingSupportedMessageComponent supportedMessage : messaging.getSupportedMessage()) {
					summaryMessagingSupportedMessage = new CapabilityStatementMessagingSupportedMessageComponent();
					summaryMessagingSupportedMessage.setMode(supportedMessage.getMode());
					summaryMessagingSupportedMessage.setDefinition(supportedMessage.getDefinition());
					summaryMessaging.addSupportedMessage(summaryMessagingSupportedMessage);
				}
				CapabilityStatementMessagingSupportedMessageComponent supportedMessageComponent;
				for (CapabilityStatementMessagingSupportedMessageComponent supportedMessage : messaging.getSupportedMessage())  {
					supportedMessageComponent = new CapabilityStatementMessagingSupportedMessageComponent();
					supportedMessageComponent.setMode(supportedMessage.getMode());
					supportedMessageComponent.setDefinition(supportedMessage.getDefinition());
					summaryMessaging.addSupportedMessage(supportedMessageComponent);
				}
				summary.addMessaging(summaryMessaging);
			}
			CapabilityStatementDocumentComponent summaryDocument;
			for (CapabilityStatementDocumentComponent document : original.getDocument()) {
				summaryDocument = new CapabilityStatementDocumentComponent();
				summaryDocument.setMode(document.getMode());
				summaryDocument.setProfile(document.getProfile());
				summary.addDocument(summaryDocument);
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		}

		return summary;
	}

}
