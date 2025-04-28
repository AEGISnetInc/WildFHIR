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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.formats.XmlParser;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DocumentManifest;
import org.hl7.fhir.r4.model.DocumentManifest.DocumentManifestRelatedComponent;
import org.hl7.fhir.r4.model.Identifier;
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
public class ResourcemetadataDocumentManifest extends ResourcemetadataProxy {

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
        ByteArrayInputStream iDocumentManifest = null;
        Resourcemetadata rMetadata = null;
        List<Resourcemetadata> rMetadataChain = null;

		try {
			// Extract and convert the resource contents to a DocumentManifest object
			if (chainedResource != null) {
				iDocumentManifest = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iDocumentManifest = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
			DocumentManifest documentManifest = (DocumentManifest) xmlP.parse(iDocumentManifest);
			iDocumentManifest.close();

			/*
			 * Create new Resourcemetadata objects for each DocumentManifest metadata value and add to the resourcemetadataList
			 */

			// Add Resource common parameters
            rMetadataChain = this.generateResourcemetadataTagList(resource, documentManifest, chainedParameter);
			resourcemetadataList.addAll(rMetadataChain);

			// author : reference
			if (documentManifest.hasAuthor()) {

				for (Reference author : documentManifest.getAuthor()) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "author", 0, author, null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// created : date
			if (documentManifest.hasCreated()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"created", utcDateUtil.formatDate(documentManifest.getCreated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(documentManifest.getCreated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rMetadata);
			}

			// description : string
			if (documentManifest.hasDescription()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"description", documentManifest.getDescription());
				resourcemetadataList.add(rMetadata);
			}

			// identifier : token
			if (documentManifest.hasMasterIdentifier()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", documentManifest.getMasterIdentifier().getValue(), documentManifest.getMasterIdentifier().getSystem(), null, ServicesUtil.INSTANCE.getTextValue(documentManifest.getMasterIdentifier()));
				resourcemetadataList.add(rMetadata);
			}

			if (documentManifest.hasIdentifier()) {

				for (Identifier identifier : documentManifest.getIdentifier()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rMetadata);
				}
			}

			// item : reference
			if (documentManifest.hasContent()) {

				for (Reference content : documentManifest.getContent()) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "item", 0, content, null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// recipient : reference
			if (documentManifest.hasRecipient()) {

				for (Reference recipient : documentManifest.getRecipient()) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "recipient", 0, recipient, null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// source : uri
			if (documentManifest.hasSource()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"source", documentManifest.getSource());
				resourcemetadataList.add(rMetadata);
			}

			// status : token
			if (documentManifest.hasStatus() && documentManifest.getStatus() != null) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", documentManifest.getStatus().toCode(), documentManifest.getStatus().getSystem());
				resourcemetadataList.add(rMetadata);
			}

			// subject : reference
			if (documentManifest.hasSubject()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "subject", 0, documentManifest.getSubject(), null);
				resourcemetadataList.addAll(rMetadataChain);

				// patient : reference
				if ((documentManifest.getSubject().hasReference() && documentManifest.getSubject().getReference().indexOf("Patient") >= 0)
						|| (documentManifest.getSubject().hasType() && documentManifest.getSubject().getType().equals("Patient"))) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "patient", 0, documentManifest.getSubject(), null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// type : token
			if (documentManifest.hasType() && documentManifest.getType().hasCoding()) {

				for (Coding type : documentManifest.getType().getCoding()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"type", type.getCode(), type.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(type));
					resourcemetadataList.add(rMetadata);
				}
			}

			// related
			if (documentManifest.hasRelated()) {

				for (DocumentManifestRelatedComponent related : documentManifest.getRelated()) {

					// related-id : token
					if (related.hasIdentifier()) {
						rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"related-id", related.getIdentifier().getValue(), related.getIdentifier().getSystem(), null, ServicesUtil.INSTANCE.getTextValue(related.getIdentifier()));
						resourcemetadataList.add(rMetadata);
					}

					// related-ref : reference
					if (related.hasRef()) {
						rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "related-ref", 0, related.getRef(), null);
						resourcemetadataList.addAll(rMetadataChain);
					}
				}
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		} finally {
	        rMetadata = null;
	        rMetadataChain = null;
            if (iDocumentManifest != null) {
                try {
                	iDocumentManifest.close();
                } catch (IOException ioe) {
                	ioe.printStackTrace();
                }
            }
		}

		return resourcemetadataList;
	}

}
