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

			// Add any passed in tags
			List<Resourcemetadata> tagMetadataList = this.generateResourcemetadataTagList(resource, documentManifest, chainedParameter);
			resourcemetadataList.addAll(tagMetadataList);

			// _id : token
			if (documentManifest.getId() != null) {
				Resourcemetadata _id = generateResourcemetadata(resource, chainedResource, chainedParameter+"_id", documentManifest.getId());
				resourcemetadataList.add(_id);
			}

			// _language : token
			if (documentManifest.getLanguage() != null) {
				Resourcemetadata _language = generateResourcemetadata(resource, chainedResource, chainedParameter+"_language", documentManifest.getLanguage());
				resourcemetadataList.add(_language);
			}

			// _lastUpdated : date
			if (documentManifest.getMeta() != null && documentManifest.getMeta().getLastUpdated() != null) {
				Resourcemetadata _lastUpdated = generateResourcemetadata(resource, chainedResource, chainedParameter+"_lastUpdated", utcDateUtil.formatDate(documentManifest.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(documentManifest.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(_lastUpdated);
			}

			// author : reference
			if (documentManifest.hasAuthor()) {

				Resourcemetadata rAuthor = null;
				List<Resourcemetadata> rAuthorChain = null;
				for (Reference author : documentManifest.getAuthor()) {

					if (author.hasReference()) {
						rAuthor = generateResourcemetadata(resource, chainedResource, chainedParameter+"author", generateFullLocalReference(author.getReference(), baseUrl));
						resourcemetadataList.add(rAuthor);

						if (chainedResource == null) {
							// Add chained parameters for any
							rAuthorChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "author", 0, author.getReference(), null);
							resourcemetadataList.addAll(rAuthorChain);
						}
					}
				}
			}

			// created : date
			if (documentManifest.hasCreated()) {
				Resourcemetadata rCreated = generateResourcemetadata(resource, chainedResource, chainedParameter+"created", utcDateUtil.formatDate(documentManifest.getCreated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(documentManifest.getCreated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rCreated);
			}

			// description : string
			if (documentManifest.hasDescription()) {
				Resourcemetadata rDescription = generateResourcemetadata(resource, chainedResource, chainedParameter+"description", documentManifest.getDescription());
				resourcemetadataList.add(rDescription);
			}

			// identifier : token
			if (documentManifest.hasMasterIdentifier()) {
				Resourcemetadata rIdentifier = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", documentManifest.getMasterIdentifier().getValue(), documentManifest.getMasterIdentifier().getSystem(), null, ServicesUtil.INSTANCE.getTextValue(documentManifest.getMasterIdentifier()));
				resourcemetadataList.add(rIdentifier);
			}

			if (documentManifest.hasIdentifier()) {

				for (Identifier identifier : documentManifest.getIdentifier()) {

					Resourcemetadata rIdentifier = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rIdentifier);
				}
			}

			// item : reference
			if (documentManifest.hasContent()) {

				List<Resourcemetadata> rContentChain = null;
				for (Reference content : documentManifest.getContent()) {

					if (content.hasReference()) {
						Resourcemetadata rContentRef = generateResourcemetadata(resource, chainedResource, chainedParameter+"item", generateFullLocalReference(content.getReference(), baseUrl));
						resourcemetadataList.add(rContentRef);

						if (chainedResource == null) {
							// Add chained parameters for any
							rContentChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "item", 0, content.getReference(), null);
							resourcemetadataList.addAll(rContentChain);
						}
					}
				}
			}

			// recipient : token
			if (documentManifest.hasRecipient()) {

				List<Resourcemetadata> rRecipientChain = null;
				for (Reference recipient : documentManifest.getRecipient()) {

					if (recipient.hasReference()) {
						Resourcemetadata rRecipient = generateResourcemetadata(resource, chainedResource, chainedParameter+"recipient", recipient.getReference());
						resourcemetadataList.add(rRecipient);

						if (chainedResource == null) {
							// Add chained parameters for any
							rRecipientChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "recipient", 0, recipient.getReference(), null);
							resourcemetadataList.addAll(rRecipientChain);
						}
					}
				}
			}

			// source : uri
			if (documentManifest.hasSource()) {
				Resourcemetadata rSource = generateResourcemetadata(resource, chainedResource, chainedParameter+"source", documentManifest.getSource());
				resourcemetadataList.add(rSource);
			}

			// status : token
			if (documentManifest.hasStatus() && documentManifest.getStatus() != null) {
				Resourcemetadata rCoding = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", documentManifest.getStatus().toCode(), documentManifest.getStatus().getSystem());
				resourcemetadataList.add(rCoding);
			}

			// patient : reference
			// subject : reference
			if (documentManifest.hasSubject() && documentManifest.getSubject().hasReference()) {
				String subjectReference = generateFullLocalReference(documentManifest.getSubject().getReference(), baseUrl);

				Resourcemetadata rSubject = generateResourcemetadata(resource, chainedResource, chainedParameter+"subject", subjectReference);
				resourcemetadataList.add(rSubject);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rSubjectChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "subject", 0, documentManifest.getSubject().getReference(), null);
					resourcemetadataList.addAll(rSubjectChain);
				}

				if (subjectReference.indexOf("Patient") >= 0) {
					Resourcemetadata rPatient = generateResourcemetadata(resource, chainedResource, chainedParameter+"patient", subjectReference);
					resourcemetadataList.add(rPatient);

					if (chainedResource == null) {
						// Add chained parameters
						List<Resourcemetadata> rPatientChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "patient", 0, documentManifest.getSubject().getReference(), null);
						resourcemetadataList.addAll(rPatientChain);
					}
				}
			}

			// type : token
			if (documentManifest.hasType() && documentManifest.getType().hasCoding()) {

				Resourcemetadata rType = null;
				for (Coding type : documentManifest.getType().getCoding()) {
					rType = generateResourcemetadata(resource, chainedResource, chainedParameter+"type", type.getCode(), type.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(type));
					resourcemetadataList.add(rType);
				}
			}

			// related
			if (documentManifest.hasRelated()) {

				List<Resourcemetadata> rRelatedChain = null;
				for (DocumentManifestRelatedComponent related : documentManifest.getRelated()) {

					// related-id : token
					if (related.hasIdentifier()) {
						Resourcemetadata rRelatedId = generateResourcemetadata(resource, chainedResource, chainedParameter+"related-id", related.getIdentifier().getValue(), related.getIdentifier().getSystem(), null, ServicesUtil.INSTANCE.getTextValue(related.getIdentifier()));
						resourcemetadataList.add(rRelatedId);
					}

					// related-ref : reference
					if (related.hasRef() && related.getRef().hasReference()) {
						Resourcemetadata rRelatedRef = generateResourcemetadata(resource, chainedResource, chainedParameter+"related-ref", generateFullLocalReference(related.getRef().getReference(), baseUrl));
						resourcemetadataList.add(rRelatedRef);

						if (chainedResource == null) {
							// Add chained parameters for any
							rRelatedChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "related-ref", 0, related.getRef().getReference(), null);
							resourcemetadataList.addAll(rRelatedChain);
						}
					}
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
