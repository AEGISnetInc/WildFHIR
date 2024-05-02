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
        ByteArrayInputStream iDocumentReference = null;

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

			// Add any passed in tags
			List<Resourcemetadata> tagMetadataList = this.generateResourcemetadataTagList(resource, documentReference, chainedParameter);
			resourcemetadataList.addAll(tagMetadataList);

			// _id : token
			Resourcemetadata _id = generateResourcemetadata(resource, chainedResource, chainedParameter+"_id", documentReference.getId());
			resourcemetadataList.add(_id);

			// _language : token
			if (documentReference.getLanguage() != null) {
				Resourcemetadata _language = generateResourcemetadata(resource, chainedResource, chainedParameter+"_language", documentReference.getLanguage());
				resourcemetadataList.add(_language);
			}

			// _lastUpdated : date
			if (documentReference.getMeta() != null && documentReference.getMeta().getLastUpdated() != null) {
				Resourcemetadata _lastUpdated = generateResourcemetadata(resource, chainedResource, chainedParameter+"_lastUpdated", utcDateUtil.formatDate(documentReference.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(documentReference.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(_lastUpdated);
			}

			// authenticator : reference
			if (documentReference.hasAuthenticator()) {

				Resourcemetadata rAuthenticator = generateResourcemetadata(resource, chainedResource, chainedParameter + "authenticator", generateFullLocalReference(documentReference.getAuthenticator().getReference(), baseUrl));
				resourcemetadataList.add(rAuthenticator);

				if (chainedResource == null) {
					// Add chained parameters for any
					List<Resourcemetadata> rAuthenticatorChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "authenticator", 0, documentReference.getAuthenticator().getReference());
					resourcemetadataList.addAll(rAuthenticatorChain);
				}
			}

			// author : reference
			if (documentReference.hasAuthor()) {

				Resourcemetadata rAuthor = null;
				List<Resourcemetadata> rAuthorChain = null;
				for (Reference author : documentReference.getAuthor()) {

					if (author.hasReference()) {
						rAuthor = generateResourcemetadata(resource, chainedResource, chainedParameter+"author", generateFullLocalReference(author.getReference(), baseUrl));
						resourcemetadataList.add(rAuthor);

						if (chainedResource == null) {
							// Add chained parameters for any
							rAuthorChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "author", 0, author.getReference());
							resourcemetadataList.addAll(rAuthorChain);
						}
					}
				}
			}

			// category : token
			if (documentReference.hasCategory()) {

				Resourcemetadata rCode = null;
				for (CodeableConcept category : documentReference.getCategory()) {

					if (category.hasCoding()) {
						for (Coding code : category.getCoding()) {
							rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"category", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rCode);
						}
					}
				}
			}

			// custodian : reference
			if (documentReference.hasCustodian() && documentReference.getCustodian().hasReference()) {
				Resourcemetadata rCustodian = generateResourcemetadata(resource, chainedResource, chainedParameter+"custodian", generateFullLocalReference(documentReference.getCustodian().getReference(), baseUrl));
				resourcemetadataList.add(rCustodian);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rCustodianChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "custodian", 0, documentReference.getCustodian().getReference());
					resourcemetadataList.addAll(rCustodianChain);
				}
			}

			// date : date
			if (documentReference.hasDate()) {
				Resourcemetadata rDate = generateResourcemetadata(resource, chainedResource, chainedParameter+"date", utcDateUtil.formatDate(documentReference.getDate(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(documentReference.getDate(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rDate);
			}

			// description : string
			if (documentReference.hasDescription()) {
				Resourcemetadata rDescription = generateResourcemetadata(resource, chainedResource, chainedParameter+"description", documentReference.getDescription());
				resourcemetadataList.add(rDescription);
			}

			if (documentReference.hasContext()) {

				// encounter : reference
				if (documentReference.getContext().hasEncounter()) {

					List<Resourcemetadata> rEncounterChain = null;
					for (Reference encounter : documentReference.getContext().getEncounter()) {
						Resourcemetadata rEncounter = generateResourcemetadata(resource, chainedResource, chainedParameter+"encounter", generateFullLocalReference(encounter.getReference(), baseUrl));
						resourcemetadataList.add(rEncounter);

						if (chainedResource == null) {
							// Add chained parameters for any
							rEncounterChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "encounter", 0, encounter.getReference());
							resourcemetadataList.addAll(rEncounterChain);
						}
					}
				}

				// event : token
				if (documentReference.getContext().hasEvent()) {

					Resourcemetadata rCode = null;
					for (CodeableConcept codeableConcept : documentReference.getContext().getEvent()) {

						if (codeableConcept.hasCoding()) {
							for (Coding code : codeableConcept.getCoding()) {
								rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"event", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
								resourcemetadataList.add(rCode);
							}
						}
					}
				}

				// facility : token
				if (documentReference.getContext().hasFacilityType() && documentReference.getContext().getFacilityType().hasCoding()) {

					Resourcemetadata rCode = null;
					for (Coding code : documentReference.getContext().getFacilityType().getCoding()) {

						rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"facility", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
						resourcemetadataList.add(rCode);
					}
				}

				// period : date(period)
				if (documentReference.getContext().hasPeriod()) {
					Resourcemetadata rPeriod = generateResourcemetadata(resource, chainedResource, chainedParameter+"period", utcDateUtil.formatDate(documentReference.getContext().getPeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(documentReference.getContext().getPeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(documentReference.getContext().getPeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), utcDateUtil.formatDate(documentReference.getContext().getPeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), "PERIOD");
					resourcemetadataList.add(rPeriod);
				}

				// setting : token
				if (documentReference.getContext().hasPracticeSetting() && documentReference.getContext().getPracticeSetting().hasCoding()) {

					Resourcemetadata rCode = null;
					for (Coding code : documentReference.getContext().getPracticeSetting().getCoding()) {

						rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"setting", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
						resourcemetadataList.add(rCode);
					}
				}

				if (documentReference.getContext().hasRelated()) {

					List<Resourcemetadata> rRelatedChain = null;
					for (Reference related : documentReference.getContext().getRelated()) {

						// related : reference
						if (related.hasReference()) {
							Resourcemetadata rRelated = generateResourcemetadata(resource, chainedResource, chainedParameter+"related", generateFullLocalReference(related.getReference(), baseUrl));
							resourcemetadataList.add(rRelated);

							if (chainedResource == null) {
								// Add chained parameters for any
								rRelatedChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "related-", 0, related.getReference());
								resourcemetadataList.addAll(rRelatedChain);
							}
						}
					}
				}
			}

			// identifier : token
			if (documentReference.hasMasterIdentifier()) {
				Resourcemetadata rIdentifier = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", documentReference.getMasterIdentifier().getValue(), documentReference.getMasterIdentifier().getSystem(), null, ServicesUtil.INSTANCE.getTextValue(documentReference.getMasterIdentifier()));
				resourcemetadataList.add(rIdentifier);
			}

			if (documentReference.hasIdentifier()) {

				for (Identifier identifier : documentReference.getIdentifier()) {

					Resourcemetadata rIdentifier = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rIdentifier);
				}
			}

			if (documentReference.hasContent()) {

				for (DocumentReferenceContentComponent content : documentReference.getContent()) {

					if (content.hasAttachment()) {

						// contenttype : token
						if (content.getAttachment().hasContentType()) {
							Resourcemetadata rContentType = generateResourcemetadata(resource, chainedResource, chainedParameter+"contenttype", content.getAttachment().getContentType());
							resourcemetadataList.add(rContentType);
						}

						// language : token
						if (content.getAttachment().hasLanguage()) {
							Resourcemetadata rLanguage = generateResourcemetadata(resource, chainedResource, chainedParameter+"language", content.getAttachment().getLanguage());
							resourcemetadataList.add(rLanguage);
						}

						// location : uri
						if (content.getAttachment().hasUrl()) {
							Resourcemetadata rLocation = generateResourcemetadata(resource, chainedResource, chainedParameter+"location", content.getAttachment().getUrl());
							resourcemetadataList.add(rLocation);
						}
					}

					// format : token
					if (content.hasFormat()) {
						Resourcemetadata rFormat = generateResourcemetadata(resource, chainedResource, chainedParameter+"format", content.getFormat().getCode(), content.getFormat().getSystem(), null, ServicesUtil.INSTANCE.getTextValue(content.getFormat()));
						resourcemetadataList.add(rFormat);
					}

				}
			}

			// patient : reference
			// subject : reference
			if (documentReference.hasSubject() && documentReference.getSubject().hasReference()) {
				String subjectReference = generateFullLocalReference(documentReference.getSubject().getReference(), baseUrl);

				Resourcemetadata rSubject = generateResourcemetadata(resource, chainedResource, chainedParameter+"subject", subjectReference);
				resourcemetadataList.add(rSubject);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rSubjectChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "subject", 0, documentReference.getSubject().getReference());
					resourcemetadataList.addAll(rSubjectChain);
				}

				if (subjectReference.indexOf("Patient") >= 0) {
					Resourcemetadata rPatient = generateResourcemetadata(resource, chainedResource, chainedParameter+"patient", subjectReference);
					resourcemetadataList.add(rPatient);

					if (chainedResource == null) {
						// Add chained parameters
						List<Resourcemetadata> rPatientChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "patient", 0, documentReference.getSubject().getReference());
						resourcemetadataList.addAll(rPatientChain);
					}
				}
			}

			if (documentReference.hasRelatesTo()) {

				List<Resourcemetadata> rRelatesToChain = null;
				for (DocumentReferenceRelatesToComponent relatesTo : documentReference.getRelatesTo()) {

					// relationship : composite
					StringBuilder relationship = new StringBuilder("");

					// relation : token
					if (relatesTo.hasCode() && relatesTo.getCode() != null) {
						Resourcemetadata rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"relation", relatesTo.getCode().toCode(), relatesTo.getCode().getSystem());
						resourcemetadataList.add(rCode);

						if (relatesTo.getCode().getSystem() != null) {
							relationship.append(relatesTo.getCode().getSystem());
						}
						relationship.append("|").append(relatesTo.getCode().toCode());
					}

					relationship.append("$");

					// relatesto : reference
					if (relatesTo.hasTarget() && relatesTo.getTarget().hasReference()) {
						Resourcemetadata rTarget = generateResourcemetadata(resource, chainedResource, chainedParameter+"relatesto", relatesTo.getTarget().getReference());
						resourcemetadataList.add(rTarget);

						relationship.append(relatesTo.getTarget().getReference());

						if (chainedResource == null) {
							// Add chained parameters for any
							rRelatesToChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "relatesto", 0, relatesTo.getTarget().getReference());
							resourcemetadataList.addAll(rRelatesToChain);
						}
					}

					Resourcemetadata rRelationship = generateResourcemetadata(resource, chainedResource, chainedParameter+"relationship", relationship.toString());
					resourcemetadataList.add(rRelationship);
				}
			}

			// security-label : token
			if (documentReference.hasSecurityLabel()) {

				Resourcemetadata rCode = null;
				for (CodeableConcept securityLabel : documentReference.getSecurityLabel()) {

					if (securityLabel.hasCoding()) {
						for (Coding code : securityLabel.getCoding()) {
							rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"security-label", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rCode);
						}
					}
				}
			}

			// status : token
			if (documentReference.hasStatus() && documentReference.getStatus() != null) {
				Resourcemetadata rStatus = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", documentReference.getStatus().toCode(), documentReference.getStatus().getSystem());
				resourcemetadataList.add(rStatus);
			}

			// type : token
			if (documentReference.hasType() && documentReference.getType().hasCoding()) {

				Resourcemetadata rType = null;
				for (Coding type : documentReference.getType().getCoding()) {
					rType = generateResourcemetadata(resource, chainedResource, chainedParameter+"type", type.getCode(), type.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(type));
					resourcemetadataList.add(rType);
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
