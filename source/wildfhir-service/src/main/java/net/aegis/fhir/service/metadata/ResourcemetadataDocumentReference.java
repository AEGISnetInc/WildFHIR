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
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.DocumentReference.DocumentReferenceContentComponent;
import org.hl7.fhir.r4.model.DocumentReference.DocumentReferenceRelatesToComponent;
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
public class ResourcemetadataDocumentReference extends ResourcemetadataProxy {

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
        ByteArrayInputStream iDocumentReference = null;
        Resourcemetadata rMetadata = null;
        List<Resourcemetadata> rMetadataChain = null;

		try {
			// Extract and convert the resource contents to a DocumentReference object
			if (chainedResource != null) {
				iDocumentReference = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iDocumentReference = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
			DocumentReference documentReference = (DocumentReference) xmlP.parse(iDocumentReference);
			iDocumentReference.close();

			/*
			 * Create new Resourcemetadata objects for each DocumentReference metadata value and add to the resourcemetadataList
			 */

			// Add Resource common parameters
            rMetadataChain = this.generateResourcemetadataTagList(resource, documentReference, chainedParameter);
			resourcemetadataList.addAll(rMetadataChain);

			// authenticator : reference
			if (documentReference.hasAuthenticator()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "authenticator", 0, documentReference.getAuthenticator(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// author : reference
			if (documentReference.hasAuthor()) {

				for (Reference author : documentReference.getAuthor()) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "author", 0, author, null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// category : token
			if (documentReference.hasCategory()) {

				for (CodeableConcept category : documentReference.getCategory()) {

					if (category.hasCoding()) {
						for (Coding code : category.getCoding()) {
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"category", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rMetadata);
						}
					}
				}
			}

			// custodian : reference
			if (documentReference.hasCustodian()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "custodian", 0, documentReference.getCustodian(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// date : date
			if (documentReference.hasDate()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"date", utcDateUtil.formatDate(documentReference.getDate(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(documentReference.getDate(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rMetadata);
			}

			// description : string
			if (documentReference.hasDescription()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"description", documentReference.getDescription());
				resourcemetadataList.add(rMetadata);
			}

			if (documentReference.hasContext()) {

				// encounter : reference
				if (documentReference.getContext().hasEncounter()) {

					for (Reference encounter : documentReference.getContext().getEncounter()) {
						rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "encounter", 0, encounter, null);
						resourcemetadataList.addAll(rMetadataChain);
					}
				}

				// event : token
				if (documentReference.getContext().hasEvent()) {

					for (CodeableConcept codeableConcept : documentReference.getContext().getEvent()) {

						if (codeableConcept.hasCoding()) {
							for (Coding code : codeableConcept.getCoding()) {
								rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"event", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
								resourcemetadataList.add(rMetadata);
							}
						}
					}
				}

				// facility : token
				if (documentReference.getContext().hasFacilityType() && documentReference.getContext().getFacilityType().hasCoding()) {

					for (Coding code : documentReference.getContext().getFacilityType().getCoding()) {
						rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"facility", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
						resourcemetadataList.add(rMetadata);
					}
				}

				// period : date(period)
				if (documentReference.getContext().hasPeriod()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"period", utcDateUtil.formatDate(documentReference.getContext().getPeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(documentReference.getContext().getPeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(documentReference.getContext().getPeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), utcDateUtil.formatDate(documentReference.getContext().getPeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), "PERIOD");
					resourcemetadataList.add(rMetadata);
				}

				// setting : token
				if (documentReference.getContext().hasPracticeSetting() && documentReference.getContext().getPracticeSetting().hasCoding()) {

					for (Coding code : documentReference.getContext().getPracticeSetting().getCoding()) {
						rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"setting", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
						resourcemetadataList.add(rMetadata);
					}
				}

				if (documentReference.getContext().hasRelated()) {

					// related : reference
					for (Reference related : documentReference.getContext().getRelated()) {
						rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "related", 0, related, null);
						resourcemetadataList.addAll(rMetadataChain);
					}
				}
			}

			// identifier : token
			if (documentReference.hasMasterIdentifier()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", documentReference.getMasterIdentifier().getValue(), documentReference.getMasterIdentifier().getSystem(), null, ServicesUtil.INSTANCE.getTextValue(documentReference.getMasterIdentifier()));
				resourcemetadataList.add(rMetadata);
			}

			if (documentReference.hasIdentifier()) {

				for (Identifier identifier : documentReference.getIdentifier()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rMetadata);
				}
			}

			if (documentReference.hasContent()) {

				for (DocumentReferenceContentComponent content : documentReference.getContent()) {

					if (content.hasAttachment()) {

						// contenttype : token
						if (content.getAttachment().hasContentType()) {
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"contenttype", content.getAttachment().getContentType());
							resourcemetadataList.add(rMetadata);
						}

						// language : token
						if (content.getAttachment().hasLanguage()) {
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"language", content.getAttachment().getLanguage());
							resourcemetadataList.add(rMetadata);
						}

						// location : uri
						if (content.getAttachment().hasUrl()) {
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"location", content.getAttachment().getUrl());
							resourcemetadataList.add(rMetadata);
						}
					}

					// format : token
					if (content.hasFormat()) {
						rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"format", content.getFormat().getCode(), content.getFormat().getSystem(), null, ServicesUtil.INSTANCE.getTextValue(content.getFormat()));
						resourcemetadataList.add(rMetadata);
					}

				}
			}

			// subject : reference
			if (documentReference.hasSubject()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "subject", 0, documentReference.getSubject(), null);
				resourcemetadataList.addAll(rMetadataChain);

				// patient : reference
				if ((documentReference.getSubject().hasReference() && documentReference.getSubject().getReference().indexOf("Patient") >= 0)
						|| (documentReference.getSubject().hasType() && documentReference.getSubject().getType().equals("Patient"))) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "patient", 0, documentReference.getSubject(), null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			if (documentReference.hasRelatesTo()) {

				for (DocumentReferenceRelatesToComponent relatesTo : documentReference.getRelatesTo()) {

					// relationship : composite
					StringBuilder relationship = new StringBuilder("");

					// relation : token
					if (relatesTo.hasCode() && relatesTo.getCode() != null) {
						rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"relation", relatesTo.getCode().toCode(), relatesTo.getCode().getSystem());
						resourcemetadataList.add(rMetadata);

						if (relatesTo.getCode().getSystem() != null) {
							relationship.append(relatesTo.getCode().getSystem());
						}
						relationship.append("|").append(relatesTo.getCode().toCode());
					}

					relationship.append("$");

					// relatesto : reference
					if (relatesTo.hasTarget()) {
						rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "relatesto", 0, relatesTo.getTarget(), null);
						resourcemetadataList.addAll(rMetadataChain);
					}

					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"relationship", relationship.toString(), null, null, null, "COMPOSITE");
					resourcemetadataList.add(rMetadata);
				}
			}

			// security-label : token
			if (documentReference.hasSecurityLabel()) {

				for (CodeableConcept securityLabel : documentReference.getSecurityLabel()) {

					if (securityLabel.hasCoding()) {
						for (Coding code : securityLabel.getCoding()) {
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"security-label", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rMetadata);
						}
					}
				}
			}

			// status : token
			if (documentReference.hasStatus() && documentReference.getStatus() != null) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", documentReference.getStatus().toCode(), documentReference.getStatus().getSystem());
				resourcemetadataList.add(rMetadata);
			}

			// type : token
			if (documentReference.hasType() && documentReference.getType().hasCoding()) {

				for (Coding type : documentReference.getType().getCoding()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"type", type.getCode(), type.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(type));
					resourcemetadataList.add(rMetadata);
				}
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		} finally {
	        rMetadata = null;
	        rMetadataChain = null;
            if (iDocumentReference != null) {
                try {
                	iDocumentReference.close();
                } catch (IOException ioe) {
                	ioe.printStackTrace();
                }
            }
		}

		return resourcemetadataList;
	}

}
