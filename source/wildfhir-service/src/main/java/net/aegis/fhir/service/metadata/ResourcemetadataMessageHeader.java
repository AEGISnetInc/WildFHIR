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
package net.aegis.fhir.service.metadata;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.formats.XmlParser;
import org.hl7.fhir.r4.formats.IParser.OutputStyle;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.MessageHeader;
import org.hl7.fhir.r4.model.MessageHeader.MessageDestinationComponent;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ResourceType;

import net.aegis.fhir.model.Resource;
import net.aegis.fhir.model.Resourcemetadata;
import net.aegis.fhir.service.ResourceService;
import net.aegis.fhir.service.util.ServicesUtil;
import net.aegis.fhir.service.util.UTCDateUtil;

/**
 * @author richard.ettema
 *
 */
public class ResourcemetadataMessageHeader extends ResourcemetadataProxy {

	/* (non-Javadoc)
	 * @see net.aegis.fhir.service.metadata.ResourcemetadataProxy#generateAllForResource(net.aegis.fhir.model.Resource, java.lang.String, net.aegis.fhir.service.ResourceService)
	 */
	@Override
	public List<Resourcemetadata> generateAllForResource(Resource resource, String baseUrl, ResourceService resourceService) throws Exception {
		return generateAllForResource(resource, baseUrl, resourceService, null, null, 0, null);
	}

	/* (non-Javadoc)
	 * @see net.aegis.fhir.service.metadata.ResourcemetadataProxy#generateAllForResource(net.aegis.fhir.model.Resource, java.lang.String, net.aegis.fhir.service.ResourceService, net.aegis.fhir.model.Resource, java.lang.String, int, org.hl7.fhir.r4.model.Resource)
	 */
	@Override
	public List<Resourcemetadata> generateAllForResource(Resource resource, String baseUrl, ResourceService resourceService, Resource chainedResource, String chainedParameter, int chainedIndex, org.hl7.fhir.r4.model.Resource fhirResource) throws Exception {

		if (StringUtils.isEmpty(chainedParameter)) {
			chainedParameter = "";
		}

		List<Resourcemetadata> resourcemetadataList = new ArrayList<Resourcemetadata>();
        ByteArrayInputStream iBundle = null;
        ByteArrayInputStream iMessageHeader = null;

		try {
            // Extract and convert the resource contents to a MessageHeader object
			if (chainedResource != null) {
				iMessageHeader = new ByteArrayInputStream(chainedResource.getResourceContents());

				// Extract and convert the original resource contents to a Bundle object ONLY IF it is a Bundle resource type
				if (resource.getResourceType().equals("Bundle")) {
					iBundle = new ByteArrayInputStream(resource.getResourceContents());
				}
			}
			else {
				iMessageHeader = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
            MessageHeader messageHeader = (MessageHeader) xmlP.parse(iMessageHeader);
            iMessageHeader.close();

			Resource messageHeaderResource = null;

			Bundle bundle = null;
			if (iBundle != null) {
				bundle = (Bundle) xmlP.parse(iBundle);
				iBundle.close();

				// Use provided resource and build the required WildFHIR Resource for the Composition
				messageHeaderResource = new Resource();
				messageHeaderResource.setResourceId(messageHeader.getId());

				// Convert the Resource to XML byte[]
				ByteArrayOutputStream oResource = new ByteArrayOutputStream();
				XmlParser xmlParser = new XmlParser();
				xmlParser.setOutputStyle(OutputStyle.PRETTY);
				xmlParser.compose(oResource, messageHeader, true);
				byte[] bResource = oResource.toByteArray();

				messageHeaderResource.setResourceContents(bResource);
				messageHeaderResource.setResourceType(messageHeader.fhirType());
			}

			/*
             * Create new Resourcemetadata objects for each MessageHeader metadata value and add to the resourcemetadataList
			 */

			// Add any passed in tags
			List<Resourcemetadata> tagMetadataList = this.generateResourcemetadataTagList(resource, messageHeader, chainedParameter);
			resourcemetadataList.addAll(tagMetadataList);

			// _id : token
			if (messageHeader.getId() != null) {
				Resourcemetadata _id = generateResourcemetadata(resource, chainedResource, chainedParameter+"_id", messageHeader.getId());
				resourcemetadataList.add(_id);
			}

			// _language : token
			if (messageHeader.getLanguage() != null) {
				Resourcemetadata _language = generateResourcemetadata(resource, chainedResource, chainedParameter+"_language", messageHeader.getLanguage());
				resourcemetadataList.add(_language);
			}

			// _lastUpdated : date
			if (messageHeader.getMeta() != null && messageHeader.getMeta().getLastUpdated() != null) {
				Resourcemetadata _lastUpdated = generateResourcemetadata(resource, chainedResource, chainedParameter+"_lastUpdated", utcDateUtil.formatDate(messageHeader.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(messageHeader.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(_lastUpdated);
			}

			// author : reference
			if (messageHeader.hasAuthor() && messageHeader.getAuthor().hasReference()) {
				String authorReference = null;
				if (bundle != null) {
					authorReference = messageHeader.getAuthor().getReference();
				}
				else {
					authorReference = generateFullLocalReference(messageHeader.getAuthor().getReference(), baseUrl);
				}

				Resourcemetadata rAuthor = generateResourcemetadata(resource, chainedResource, chainedParameter+"author", authorReference);
				resourcemetadataList.add(rAuthor);

				List<Resourcemetadata> rAuthorChain = null;
				if (chainedResource == null) {
					// Add chained parameters
					rAuthorChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "author", 0, messageHeader.getAuthor().getReference(), null);
					resourcemetadataList.addAll(rAuthorChain);
				}
				else {
					if (bundle != null) {
						// Extract message.author resource from containing bundle
						org.hl7.fhir.r4.model.Resource authorEntry = this.getReferencedBundleEntryResource(bundle, messageHeader.getAuthor().getReference());

						if (messageHeaderResource != null && authorEntry != null) {
							// Add chained parameters for message.author; do not send baseUrl so references get stored as-is
							rAuthorChain = this.generateChainedResourcemetadataAny(messageHeaderResource, "", resourceService, "message.author", 0, messageHeader.getAuthor().getReference(), authorEntry);
							resourcemetadataList.addAll(rAuthorChain);
						}
					}
				}
			}

			// enterer : reference
			if (messageHeader.hasEnterer() && messageHeader.getEnterer().hasReference()) {
				String entererReference = null;
				if (bundle != null) {
					entererReference = messageHeader.getEnterer().getReference();
				}
				else {
					entererReference = generateFullLocalReference(messageHeader.getAuthor().getReference(), baseUrl);
				}

				Resourcemetadata rEnterer = generateResourcemetadata(resource, chainedResource, chainedParameter+"enterer", generateFullLocalReference(entererReference, baseUrl));
				resourcemetadataList.add(rEnterer);

				List<Resourcemetadata> rEntererChain = null;
				if (chainedResource == null) {
					// Add chained parameters
					rEntererChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "enterer", 0, messageHeader.getEnterer().getReference(), null);
					resourcemetadataList.addAll(rEntererChain);
				}
				else {
					if (bundle != null) {
						// Extract message.enterer resource from containing bundle
						org.hl7.fhir.r4.model.Resource entererEntry = this.getReferencedBundleEntryResource(bundle, messageHeader.getEnterer().getReference());

						if (messageHeaderResource != null && entererEntry != null) {
							// Add chained parameters for message.enterer; do not send baseUrl so references get stored as-is
							rEntererChain = this.generateChainedResourcemetadataAny(messageHeaderResource, "", resourceService, "message.enterer", 0, messageHeader.getEnterer().getReference(), entererEntry);
							resourcemetadataList.addAll(rEntererChain);
						}
					}
				}
			}

			// event : token
			if (messageHeader.hasEventUriType()) {
				Resourcemetadata rEvent = generateResourcemetadata(resource, chainedResource, chainedParameter+"event", messageHeader.getEventUriType().getValue());
				resourcemetadataList.add(rEvent);
			}
			if (messageHeader.hasEventCoding()) {
				Resourcemetadata rEvent = generateResourcemetadata(resource, chainedResource, chainedParameter+"event", messageHeader.getEventCoding().getCode(), messageHeader.getEventCoding().getSystem(), null, ServicesUtil.INSTANCE.getTextValue(messageHeader.getEventCoding()));
				resourcemetadataList.add(rEvent);
			}

			// focus : reference
			if (messageHeader.hasFocus()) {

				String focusReference = null;
				List<Resourcemetadata> rFocusChain = null;
				for (Reference focus : messageHeader.getFocus()) {

					if (focus.hasReference()) {
						if (bundle != null) {
							focusReference = focus.getReference();
						}
						else {
							focusReference = generateFullLocalReference(focus.getReference(), baseUrl);
						}

						Resourcemetadata rFocus = generateResourcemetadata(resource, chainedResource, chainedParameter+"focus", generateFullLocalReference(focusReference, baseUrl));
						resourcemetadataList.add(rFocus);

						if (chainedResource == null) {
							// Add chained parameters for any
							rFocusChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "focus", 0, focus.getReference(), null);
							resourcemetadataList.addAll(rFocusChain);
						}
						else {
							if (bundle != null) {
								// Extract message.focus resource from containing bundle
								org.hl7.fhir.r4.model.Resource focusEntry = this.getReferencedBundleEntryResource(bundle, focus.getReference());

								if (messageHeaderResource != null && focusEntry != null) {
									// Add chained parameters for message.focus; do not send baseUrl so references get stored as-is
									rFocusChain = this.generateChainedResourcemetadataAny(messageHeaderResource, "", resourceService, "message.focus", 0, focus.getReference(), focusEntry);
									resourcemetadataList.addAll(rFocusChain);
								}
							}
						}
					}
				}
			}

			// responsible : reference
			if (messageHeader.hasResponsible() && messageHeader.getResponsible().hasReference()) {
				String responsibleReference = null;
				if (bundle != null) {
					responsibleReference = messageHeader.getResponsible().getReference();
				}
				else {
					responsibleReference = generateFullLocalReference(messageHeader.getResponsible().getReference(), baseUrl);
				}

				Resourcemetadata rResponsible = generateResourcemetadata(resource, chainedResource, chainedParameter+"responsible", generateFullLocalReference(responsibleReference, baseUrl));
				resourcemetadataList.add(rResponsible);

				List<Resourcemetadata> rResponsibleChain = null;
				if (chainedResource == null) {
					// Add chained parameters for any
					rResponsibleChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "responsible", 0, messageHeader.getResponsible().getReference(), null);
					resourcemetadataList.addAll(rResponsibleChain);
				}
				else {
					if (bundle != null) {
						// Extract message.responsible resource from containing bundle
						org.hl7.fhir.r4.model.Resource responsibleEntry = this.getReferencedBundleEntryResource(bundle, messageHeader.getResponsible().getReference());

						if (messageHeaderResource != null && responsibleEntry != null) {
							// Add chained parameters for message.responsible; do not send baseUrl so references get stored as-is
							rResponsibleChain = this.generateChainedResourcemetadataAny(messageHeaderResource, "", resourceService, "message.responsible", 0, messageHeader.getResponsible().getReference(), responsibleEntry);
							resourcemetadataList.addAll(rResponsibleChain);
						}
					}
				}
			}

			// sender : reference
			if (messageHeader.hasSender() && messageHeader.getSender().hasReference()) {
				String senderReference = null;
				if (bundle != null) {
					senderReference = messageHeader.getSender().getReference();
				}
				else {
					senderReference = generateFullLocalReference(messageHeader.getSender().getReference(), baseUrl);
				}

				Resourcemetadata rSender = generateResourcemetadata(resource, chainedResource, chainedParameter+"sender", generateFullLocalReference(senderReference, baseUrl));
				resourcemetadataList.add(rSender);

				List<Resourcemetadata> rSenderChain = null;
				if (chainedResource == null) {
					// Add chained parameters for any
					rSenderChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "sender", 0, messageHeader.getSender().getReference(), null);
					resourcemetadataList.addAll(rSenderChain);
				}
				else {
					if (bundle != null) {
						// Extract message.sender resource from containing bundle
						org.hl7.fhir.r4.model.Resource senderEntry = this.getReferencedBundleEntryResource(bundle, messageHeader.getSender().getReference());

						if (messageHeaderResource != null && senderEntry != null) {
							// Add chained parameters for message.sender; do not send baseUrl so references get stored as-is
							rSenderChain = this.generateChainedResourcemetadataAny(messageHeaderResource, "", resourceService, "message.sender", 0, messageHeader.getSender().getReference(), senderEntry);
							resourcemetadataList.addAll(rSenderChain);
						}
					}
				}
			}

			if (messageHeader.hasDestination()) {

				List<Resourcemetadata> rDestinationChain = null;
				for (MessageDestinationComponent destination : messageHeader.getDestination()) {

					// destination : string
					if (destination.hasName()) {
						Resourcemetadata rDestination = generateResourcemetadata(resource, chainedResource, chainedParameter+"destination", destination.getName());
						resourcemetadataList.add(rDestination);
					}

					// destination-uri : uri
					if (destination.hasEndpoint()) {
						Resourcemetadata rDestinationUri = generateResourcemetadata(resource, chainedResource, chainedParameter+"destination-uri", destination.getEndpoint());
						resourcemetadataList.add(rDestinationUri);
					}

					// receiver : reference
					if (destination.hasReceiver() && destination.getReceiver().hasReference()) {
						String receiverReference = null;
						if (bundle != null) {
							receiverReference = destination.getReceiver().getReference();
						}
						else {
							receiverReference = generateFullLocalReference(destination.getReceiver().getReference(), baseUrl);
						}

						Resourcemetadata rReceiver = generateResourcemetadata(resource, chainedResource, chainedParameter+"receiver", generateFullLocalReference(receiverReference, baseUrl));
						resourcemetadataList.add(rReceiver);

						if (chainedResource == null) {
							// Add chained parameters for any
							rDestinationChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "receiver", 0, destination.getReceiver().getReference(), null);
							resourcemetadataList.addAll(rDestinationChain);
						}
						else {
							if (bundle != null) {
								// Extract message.receiver resource from containing bundle
								org.hl7.fhir.r4.model.Resource receiverEntry = this.getReferencedBundleEntryResource(bundle, destination.getReceiver().getReference());

								if (messageHeaderResource != null && receiverEntry != null) {
									// Add chained parameters for message.receiver; do not send baseUrl so references get stored as-is
									rDestinationChain = this.generateChainedResourcemetadataAny(messageHeaderResource, "", resourceService, "message.receiver", 0, destination.getReceiver().getReference(), receiverEntry);
									resourcemetadataList.addAll(rDestinationChain);
								}
							}
						}
					}

					// target : reference
					if (destination.hasTarget() && destination.getTarget().hasReference()) {
						String targetReference = null;
						if (bundle != null) {
							targetReference = destination.getTarget().getReference();
						}
						else {
							targetReference = generateFullLocalReference(destination.getTarget().getReference(), baseUrl);
						}

						Resourcemetadata rTarget = generateResourcemetadata(resource, chainedResource, chainedParameter+"target", generateFullLocalReference(targetReference, baseUrl));
						resourcemetadataList.add(rTarget);

						if (chainedResource == null) {
							// Add chained parameters
							List<Resourcemetadata> rTargetChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "target", 0, destination.getTarget().getReference(), null);
							resourcemetadataList.addAll(rTargetChain);
						}
						else {
							if (bundle != null) {
								// Extract message.target resource from containing bundle
								org.hl7.fhir.r4.model.Resource targetEntry = this.getReferencedBundleEntryResource(bundle, destination.getTarget().getReference());

								if (messageHeaderResource != null && targetEntry != null) {
									// Add chained parameters for message.target; do not send baseUrl so references get stored as-is
									rDestinationChain = this.generateChainedResourcemetadata(messageHeaderResource, "", resourceService, "message.target.", ResourceType.Device.name(), targetEntry, targetEntry);
									resourcemetadataList.addAll(rDestinationChain);
								}
							}
						}
					}
				}
			}

			if (messageHeader.hasResponse()) {

				// code : token
				if (messageHeader.getResponse().hasCode() && messageHeader.getResponse().getCode() != null) {
					Resourcemetadata rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"code", messageHeader.getResponse().getCode().toCode(), messageHeader.getResponse().getCode().getSystem());
					resourcemetadataList.add(rCode);
				}

				// response-id : token
				if (messageHeader.getResponse().hasIdentifier()) {
					Resourcemetadata rIdentifier = generateResourcemetadata(resource, chainedResource, chainedParameter+"response-id", messageHeader.getResponse().getIdentifier());
					resourcemetadataList.add(rIdentifier);
				}
			}

			if (messageHeader.hasSource()) {

				// source : string
				if (messageHeader.getSource().hasName()) {
					Resourcemetadata rSource = generateResourcemetadata(resource, chainedResource, chainedParameter+"source", messageHeader.getSource().getName());
					resourcemetadataList.add(rSource);
				}

				// source-uri : uri
				if (messageHeader.getSource().hasEndpoint()) {
					Resourcemetadata rEndpoint = generateResourcemetadata(resource, chainedResource, chainedParameter+"source-uri", messageHeader.getSource().getEndpoint());
					resourcemetadataList.add(rEndpoint);
				}
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		}

		return resourcemetadataList;
	}

}
