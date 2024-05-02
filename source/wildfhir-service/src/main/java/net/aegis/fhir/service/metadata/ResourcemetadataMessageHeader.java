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
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.formats.XmlParser;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.MessageHeader;
import org.hl7.fhir.r4.model.MessageHeader.MessageDestinationComponent;
import org.hl7.fhir.r4.model.Reference;

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
		return generateAllForResource(resource, baseUrl, resourceService, null, null, 0);
	}

	/* (non-Javadoc)
	 * @see net.aegis.fhir.service.metadata.ResourcemetadataProxy#generateAllForResource(net.aegis.fhir.model.Resource, java.lang.String, net.aegis.fhir.service.ResourceService, net.aegis.fhir.model.Resource, java.lang.String, int)
	 */
	@Override
	public List<Resourcemetadata> generateAllForResource(Resource resource, String baseUrl, ResourceService resourceService, Resource chainedResource, String chainedParameter, int chainedIndex) throws Exception {

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

			Bundle bundle = null;
			if (iBundle != null) {
				bundle = (Bundle) xmlP.parse(iBundle);
				iBundle.close();
			}

			/*
             * Create new Resourcemetadata objects for each MessageHeader metadata value and add to the resourcemetadataList
			 */

			// Add any passed in tags
			List<Resourcemetadata> tagMetadataList = this.generateResourcemetadataTagList(resource, messageHeader, chainedParameter);
			resourcemetadataList.addAll(tagMetadataList);

			// _id : token
			Resourcemetadata _id = generateResourcemetadata(resource, chainedResource, chainedParameter+"_id", messageHeader.getId());
			resourcemetadataList.add(_id);

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
				Resourcemetadata rAuthor = generateResourcemetadata(resource, chainedResource, chainedParameter+"author", messageHeader.getAuthor().getReference());
				resourcemetadataList.add(rAuthor);

				List<Resourcemetadata> rAuthorChain = null;
				if (chainedResource == null) {
					// Add chained parameters
					rAuthorChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "author", 0, messageHeader.getAuthor().getReference());
					resourcemetadataList.addAll(rAuthorChain);
				}
				else {
					if (bundle != null) {
						// Extract message.author resource from containing bundle
						org.hl7.fhir.r4.model.Resource authorEntry = this.getReferencedBundleEntryResource(bundle, messageHeader.getAuthor().getReference());

						if (authorEntry != null) {
							// Add chained parameters for message.author
							rAuthorChain = this.generateChainedResourcemetadata(resource, baseUrl, resourceService, "message.author.", authorEntry.fhirType(), authorEntry);
							resourcemetadataList.addAll(rAuthorChain);
						}
					}
				}
			}

			// enterer : reference
			if (messageHeader.hasEnterer() && messageHeader.getEnterer().hasReference()) {
				Resourcemetadata rEnterer = generateResourcemetadata(resource, chainedResource, chainedParameter+"enterer", generateFullLocalReference(messageHeader.getEnterer().getReference(), baseUrl));
				resourcemetadataList.add(rEnterer);

				List<Resourcemetadata> rEntererChain = null;
				if (chainedResource == null) {
					// Add chained parameters
					rEntererChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "enterer", 0, messageHeader.getEnterer().getReference());
					resourcemetadataList.addAll(rEntererChain);
				}
				else {
					if (bundle != null) {
						// Extract message.enterer resource from containing bundle
						org.hl7.fhir.r4.model.Resource entererEntry = this.getReferencedBundleEntryResource(bundle, messageHeader.getEnterer().getReference());

						if (entererEntry != null) {
							// Add chained parameters for message.enterer
							rEntererChain = this.generateChainedResourcemetadata(resource, baseUrl, resourceService, "message.enterer.", entererEntry.fhirType(), entererEntry);
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

				List<Resourcemetadata> rFocusChain = null;
				for (Reference focus : messageHeader.getFocus()) {

					if (focus.hasReference()) {
						Resourcemetadata rFocus = generateResourcemetadata(resource, chainedResource, chainedParameter+"focus", generateFullLocalReference(focus.getReference(), baseUrl));
						resourcemetadataList.add(rFocus);

						if (chainedResource == null) {
							// Add chained parameters for any
							rFocusChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "focus", 0, focus.getReference());
							resourcemetadataList.addAll(rFocusChain);
						}
						else {
							if (bundle != null) {
								// Extract message.focus resource from containing bundle
								org.hl7.fhir.r4.model.Resource focusEntry = this.getReferencedBundleEntryResource(bundle, focus.getReference());

								if (focusEntry != null) {
									// Add chained parameters for message.focus
									rFocusChain = this.generateChainedResourcemetadata(resource, baseUrl, resourceService, "message.focus.", focusEntry.fhirType(), focusEntry);
									resourcemetadataList.addAll(rFocusChain);
								}
							}
						}
					}
				}
			}

			// responsible : reference
			if (messageHeader.hasResponsible() && messageHeader.getResponsible().hasReference()) {
				Resourcemetadata rResponsible = generateResourcemetadata(resource, chainedResource, chainedParameter+"responsible", generateFullLocalReference(messageHeader.getResponsible().getReference(), baseUrl));
				resourcemetadataList.add(rResponsible);

				List<Resourcemetadata> rResponsibleChain = null;
				if (chainedResource == null) {
					// Add chained parameters for any
					rResponsibleChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "responsible", 0, messageHeader.getResponsible().getReference());
					resourcemetadataList.addAll(rResponsibleChain);
				}
				else {
					if (bundle != null) {
						// Extract message.responsible resource from containing bundle
						org.hl7.fhir.r4.model.Resource responsibleEntry = this.getReferencedBundleEntryResource(bundle, messageHeader.getResponsible().getReference());

						if (responsibleEntry != null) {
							// Add chained parameters for message.responsible
							rResponsibleChain = this.generateChainedResourcemetadata(resource, baseUrl, resourceService, "message.responsible.", responsibleEntry.fhirType(), responsibleEntry);
							resourcemetadataList.addAll(rResponsibleChain);
						}
					}
				}
			}

			// sender : reference
			if (messageHeader.hasSender() && messageHeader.getSender().hasReference()) {
				Resourcemetadata rSender = generateResourcemetadata(resource, chainedResource, chainedParameter+"sender", generateFullLocalReference(messageHeader.getSender().getReference(), baseUrl));
				resourcemetadataList.add(rSender);

				List<Resourcemetadata> rSenderChain = null;
				if (chainedResource == null) {
					// Add chained parameters for any
					rSenderChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "sender", 0, messageHeader.getSender().getReference());
					resourcemetadataList.addAll(rSenderChain);
				}
				else {
					if (bundle != null) {
						// Extract message.sender resource from containing bundle
						org.hl7.fhir.r4.model.Resource senderEntry = this.getReferencedBundleEntryResource(bundle, messageHeader.getSender().getReference());

						if (senderEntry != null) {
							// Add chained parameters for message.sender
							rSenderChain = this.generateChainedResourcemetadata(resource, baseUrl, resourceService, "message.sender.", senderEntry.fhirType(), senderEntry);
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
						Resourcemetadata rReceiver = generateResourcemetadata(resource, chainedResource, chainedParameter+"receiver", generateFullLocalReference(destination.getReceiver().getReference(), baseUrl));
						resourcemetadataList.add(rReceiver);

						if (chainedResource == null) {
							// Add chained parameters for any
							rDestinationChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "receiver", 0, destination.getReceiver().getReference());
							resourcemetadataList.addAll(rDestinationChain);
						}
						else {
							if (bundle != null) {
								// Extract message.receiver resource from containing bundle
								org.hl7.fhir.r4.model.Resource receiverEntry = this.getReferencedBundleEntryResource(bundle, destination.getReceiver().getReference());

								if (receiverEntry != null) {
									// Add chained parameters for message.receiver
									rDestinationChain = this.generateChainedResourcemetadata(resource, baseUrl, resourceService, "message.receiver.", receiverEntry.fhirType(), receiverEntry);
									resourcemetadataList.addAll(rDestinationChain);
								}
							}
						}
					}

					// target : reference
					if (destination.hasTarget() && destination.getTarget().hasReference()) {
						Resourcemetadata rTarget = generateResourcemetadata(resource, chainedResource, chainedParameter+"target", generateFullLocalReference(destination.getTarget().getReference(), baseUrl));
						resourcemetadataList.add(rTarget);

						if (chainedResource == null) {
							// Add chained parameters
							List<Resourcemetadata> rTargetChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "target", 0, destination.getTarget().getReference());
							resourcemetadataList.addAll(rTargetChain);
						}
						else {
							if (bundle != null) {
								// Extract message.target resource from containing bundle
								org.hl7.fhir.r4.model.Resource targetEntry = this.getReferencedBundleEntryResource(bundle, destination.getTarget().getReference());

								if (targetEntry != null) {
									// Add chained parameters for message.target
									rDestinationChain = this.generateChainedResourcemetadata(resource, baseUrl, resourceService, "message.target.", targetEntry.fhirType(), targetEntry);
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
