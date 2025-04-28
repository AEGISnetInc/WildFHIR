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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import net.aegis.fhir.model.Resource;
import net.aegis.fhir.model.Resourcemetadata;
import net.aegis.fhir.service.ResourceService;
import net.aegis.fhir.service.util.ServicesUtil;
import net.aegis.fhir.service.util.UTCDateUtil;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.formats.IParser.OutputStyle;
import org.hl7.fhir.r4.formats.XmlParser;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.Composition.CompositionAttesterComponent;
import org.hl7.fhir.r4.model.Composition.CompositionEventComponent;
import org.hl7.fhir.r4.model.Composition.CompositionRelatesToComponent;
import org.hl7.fhir.r4.model.Composition.SectionComponent;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ResourceType;

/**
 * @author richard.ettema
 *
 */
public class ResourcemetadataComposition extends ResourcemetadataProxy {

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
        ByteArrayInputStream iComposition = null;
        Resourcemetadata rMetadata = null;
        List<Resourcemetadata> rMetadataChain = null;

		try {
			// Extract and convert the resource contents to a Composition object
			if (chainedResource != null) {
				iComposition = new ByteArrayInputStream(chainedResource.getResourceContents());

				// Extract and convert the original resource contents to a Bundle object ONLY IF it is a Bundle resource type
				if (resource.getResourceType().equals("Bundle")) {
					iBundle = new ByteArrayInputStream(resource.getResourceContents());
				}
			}
			else {
				iComposition = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
			Composition composition = (Composition) xmlP.parse(iComposition);
			iComposition.close();

			Resource compositionResource = null;

			Bundle bundle = null;
			if (iBundle != null) {
				bundle = (Bundle) xmlP.parse(iBundle);
				iBundle.close();

				// Use provided resource and build the required WildFHIR Resource for the Composition
				compositionResource = new Resource();
				compositionResource.setResourceId(composition.getId());

				// Convert the Resource to XML byte[]
				ByteArrayOutputStream oResource = new ByteArrayOutputStream();
				XmlParser xmlParser = new XmlParser();
				xmlParser.setOutputStyle(OutputStyle.PRETTY);
				xmlParser.compose(oResource, composition, true);
				byte[] bResource = oResource.toByteArray();

				compositionResource.setResourceContents(bResource);
				compositionResource.setResourceType(composition.fhirType());
			}

			/*
			 * Create new Resourcemetadata objects for each Composition metadata value and add to the resourcemetadataList
			 */

			// Add Resource common parameters
            rMetadataChain = this.generateResourcemetadataTagList(resource, composition, chainedParameter);
			resourcemetadataList.addAll(rMetadataChain);

			// attester : reference
			if (composition.hasAttester()) {

				String attesterReference = null;
				for (CompositionAttesterComponent attester : composition.getAttester()) {

					if (attester.hasParty() && attester.getParty().hasReference()) {
						if (bundle != null) {
							attesterReference = attester.getParty().getReference();
						}
						else {
							attesterReference = generateFullLocalReference(attester.getParty().getReference(), baseUrl);
						}

						rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"attester", attesterReference);
						resourcemetadataList.add(rMetadata);

						if (chainedResource == null) {
							// Add chained parameters
							rMetadataChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "attester", 0, attester.getParty().getReference(), null);
							resourcemetadataList.addAll(rMetadataChain);
						}
						else if (bundle != null) {
							// Extract composition.subject resource from containing bundle
							org.hl7.fhir.r4.model.Resource attesterEntry = this.getReferencedBundleEntryResource(bundle, attester.getParty().getReference());

							if (attesterEntry != null) {
								// Add chained parameters for composition.attester
								rMetadataChain = this.generateChainedResourcemetadataAny(compositionResource, "", resourceService, "composition.attester", 0, attester.getParty().getReference(), attesterEntry);
								resourcemetadataList.addAll(rMetadataChain);
							}
						}
					}
					// Manually handle Reference.identifier due to Bundle logic complexities
					if (attester.hasParty() && attester.getParty().hasIdentifier()) {
						rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"attester:identifier", attester.getParty().getIdentifier().getValue(), attester.getParty().getIdentifier().getSystem(), null, ServicesUtil.INSTANCE.getTextValue(attester.getParty().getIdentifier()));
						resourcemetadataList.add(rMetadata);
					}
				}
			}

			// author : reference
			if (composition.hasAuthor()) {

				String authorReference = null;
				for (Reference author : composition.getAuthor()) {

					if (author.hasReference()) {
						if (bundle != null) {
							authorReference = author.getReference();
						}
						else {
							authorReference = generateFullLocalReference(author.getReference(), baseUrl);
						}

						rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"author", authorReference);
						resourcemetadataList.add(rMetadata);

						if (chainedResource == null) {
							// Add chained parameters
							rMetadataChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "author", 0, author.getReference(), null);
							resourcemetadataList.addAll(rMetadataChain);
						}
						else if (bundle != null) {
							// Extract composition.author resource from containing bundle
							org.hl7.fhir.r4.model.Resource authorEntry = this.getReferencedBundleEntryResource(bundle, author.getReference());

							if (authorEntry != null) {
								// Add chained parameters for composition.author
								rMetadataChain = this.generateChainedResourcemetadataAny(compositionResource, "", resourceService, "composition.author", 0, author.getReference(), authorEntry);
								resourcemetadataList.addAll(rMetadataChain);
							}
						}
					}
					// Manually handle Reference.identifier due to Bundle logic complexities
					if (author.hasIdentifier()) {
						rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"author:identifier", author.getIdentifier().getValue(), author.getIdentifier().getSystem(), null, ServicesUtil.INSTANCE.getTextValue(author.getIdentifier()));
						resourcemetadataList.add(rMetadata);
					}
				}
			}

			// category : token
			if (composition.hasCategory()) {

				for (CodeableConcept category : composition.getCategory()) {
					if (category.hasCoding()) {

						for (Coding code : category.getCoding()) {
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"category", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rMetadata);
						}
					}
				}
			}

			// confidentiality : token
			if (composition.hasConfidentiality() && composition.getConfidentiality() != null) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"confidentiality", composition.getConfidentiality().toCode(), composition.getConfidentiality().getSystem());
				resourcemetadataList.add(rMetadata);
			}

			if (composition.hasEvent()) {

				for (CompositionEventComponent event : composition.getEvent()) {
					// context : token
					if (event.hasCode()) {

						for (CodeableConcept eventCode : event.getCode()) {
							if (eventCode.hasCoding()) {

								for (Coding code : eventCode.getCoding()) {
									rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"context", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
									resourcemetadataList.add(rMetadata);
								}
							}
						}
					}

					// period : date(period)
					if (event.hasPeriod()) {
						rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"period", utcDateUtil.formatDate(event.getPeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(event.getPeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(event.getPeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), utcDateUtil.formatDate(event.getPeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), "PERIOD");
						resourcemetadataList.add(rMetadata);
					}
				}
			}

			// date : datetime
			if (composition.hasDate()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"date", utcDateUtil.formatDate(composition.getDate(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(composition.getDate(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rMetadata);
			}

			// encounter : reference
			if (composition.hasEncounter() && composition.getEncounter().hasReference()) {
				String encounterReference = null;
				if (bundle != null) {
					encounterReference = composition.getEncounter().getReference();
				}
				else {
					encounterReference = generateFullLocalReference(composition.getEncounter().getReference(), baseUrl);
				}

				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"encounter", encounterReference);
				resourcemetadataList.add(rMetadata);

				if (chainedResource == null) {
					// Add chained parameters
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "encounter", 0, composition.getEncounter().getReference(), null);
					resourcemetadataList.addAll(rMetadataChain);
				}
				else if (bundle != null) {
					// Extract composition.encounter resource from containing bundle
					org.hl7.fhir.r4.model.Resource encounterEntry = this.getReferencedBundleEntryResource(bundle, composition.getEncounter().getReference());

					if (encounterEntry != null) {
						// Add chained parameters for composition.encounter
						rMetadataChain = this.generateChainedResourcemetadata(compositionResource, "", resourceService, "composition.encounter.", ResourceType.Encounter.name(), encounterEntry, encounterEntry);
						resourcemetadataList.addAll(rMetadataChain);
					}
				}
			}
			// Manually handle Reference.identifier due to Bundle logic complexities
			if (composition.hasEncounter() && composition.getEncounter().hasIdentifier()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"encounter:identifier", composition.getEncounter().getIdentifier().getValue(), composition.getEncounter().getIdentifier().getSystem(), null, ServicesUtil.INSTANCE.getTextValue(composition.getEncounter().getIdentifier()));
				resourcemetadataList.add(rMetadata);
			}

			// section
			if (composition.hasSection()) {

				for (SectionComponent section : composition.getSection()) {

					// entry : reference
					if (section.hasEntry()) {

						for (Reference entry : section.getEntry()) {

							if (entry.hasReference()) {
								String entryReference = null;
								if (bundle != null) {
									entryReference = entry.getReference();
								}
								else {
									entryReference = generateFullLocalReference(entry.getReference(), baseUrl);
								}

								rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"entry", entryReference);
								resourcemetadataList.add(rMetadata);

								if (chainedResource == null) {
									// Add chained parameters for any
									rMetadataChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "entry", 0, entry.getReference(), null);
									resourcemetadataList.addAll(rMetadataChain);
								}
								else if (bundle != null) {
									// Extract composition.entry resource from containing bundle
									org.hl7.fhir.r4.model.Resource entryEntry = this.getReferencedBundleEntryResource(bundle, entry.getReference());

									if (entryEntry != null) {
										// Add chained parameters for composition.entry
										rMetadataChain = this.generateChainedResourcemetadataAny(compositionResource, "", resourceService, "composition.entry", 0, entry.getReference(), entryEntry);
										resourcemetadataList.addAll(rMetadataChain);
									}
								}
							}
							// Manually handle Reference.identifier due to Bundle logic complexities
							if (entry.hasIdentifier()) {
								rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"entry:identifier", entry.getIdentifier().getValue(), entry.getIdentifier().getSystem(), null, ServicesUtil.INSTANCE.getTextValue(entry.getIdentifier()));
								resourcemetadataList.add(rMetadata);
							}
						}
					}

					// section : token
					if (section.hasCode() && section.getCode().hasCoding()) {
						for (Coding code : section.getCode().getCoding()) {
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"section", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rMetadata);
						}
					}
				}
			}

			// identifier : token
			if (composition.hasIdentifier()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", composition.getIdentifier().getValue(), composition.getIdentifier().getSystem(), null, ServicesUtil.INSTANCE.getTextValue(composition.getIdentifier()));
				resourcemetadataList.add(rMetadata);
			}

			// relatesTo
			if (composition.hasRelatesTo()) {

				for (CompositionRelatesToComponent relatesTo : composition.getRelatesTo()) {

					// related-id : token
					if (relatesTo.hasTargetIdentifier()) {
						rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"related-id", relatesTo.getTargetIdentifier().getValue(), relatesTo.getTargetIdentifier().getSystem(), null, ServicesUtil.INSTANCE.getTextValue(relatesTo.getTargetIdentifier()));
						resourcemetadataList.add(rMetadata);
					}

					// related-ref : reference
					if (relatesTo.hasTargetReference() && relatesTo.getTargetReference().hasReference()) {
						String relatesToReference = null;
						if (bundle != null) {
							relatesToReference = relatesTo.getTargetReference().getReference();
						}
						else {
							relatesToReference = generateFullLocalReference(relatesTo.getTargetReference().getReference(), baseUrl);
						}

						rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"related-ref", relatesToReference);
						resourcemetadataList.add(rMetadata);

						if (chainedResource == null) {
							// Add chained parameters
							rMetadataChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "related-ref", 0, relatesTo.getTargetReference().getReference(), null);
							resourcemetadataList.addAll(rMetadataChain);
						}
						else if (bundle != null) {
							// Extract composition.relatesTo resource from containing bundle
							org.hl7.fhir.r4.model.Resource relatesToEntry = this.getReferencedBundleEntryResource(bundle, relatesTo.getTargetReference().getReference());

							if (relatesToEntry != null) {
								// Add chained parameters for composition.relatesTo
								rMetadataChain = this.generateChainedResourcemetadataAny(compositionResource, "", resourceService, "composition.related-ref", 0, relatesTo.getTargetReference().getReference(), relatesToEntry);
								resourcemetadataList.addAll(rMetadataChain);
							}
						}
					}
					// Manually handle Reference.identifier due to Bundle logic complexities
					if (relatesTo.hasTargetReference() && relatesTo.getTargetReference().hasIdentifier()) {
						rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"related-ref:identifier", relatesTo.getTargetReference().getIdentifier().getValue(), relatesTo.getTargetReference().getIdentifier().getSystem(), null, ServicesUtil.INSTANCE.getTextValue(relatesTo.getTargetReference().getIdentifier()));
						resourcemetadataList.add(rMetadata);
					}
				}
			}

			// status : token
			if (composition.hasStatus() && composition.getStatus() != null) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", composition.getStatus().toCode(), composition.getStatus().getSystem());
				resourcemetadataList.add(rMetadata);
			}

			// patient : reference
			// subject : reference
			if (composition.hasSubject() && composition.getSubject().hasReference()) {
				String subjectReference = generateFullLocalReference(composition.getSubject().getReference(), baseUrl);
				if (bundle != null) {
					subjectReference = composition.getSubject().getReference();
				}
				else {
					subjectReference = generateFullLocalReference(composition.getSubject().getReference(), baseUrl);
				}

				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"subject", subjectReference);
				resourcemetadataList.add(rMetadata);

				if (chainedResource == null) {
					// Add chained parameters for any
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "subject", 0, composition.getSubject().getReference(), null);
					resourcemetadataList.addAll(rMetadataChain);
				}
				else if (bundle != null) {
					// Extract composition.subject resource from containing bundle
					org.hl7.fhir.r4.model.Resource subjectEntry = this.getReferencedBundleEntryResource(bundle, composition.getSubject().getReference());

					if (compositionResource != null && subjectEntry != null) {
						// Add chained parameters for composition.subject; do not send baseUrl so references get stored as-is
						rMetadataChain = this.generateChainedResourcemetadataAny(compositionResource, "", resourceService, "composition.subject", 0, composition.getSubject().getReference(), subjectEntry);
						resourcemetadataList.addAll(rMetadataChain);
					}
				}

				if (subjectReference.indexOf("Patient") >= 0) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"patient", subjectReference);
					resourcemetadataList.add(rMetadata);

					if (chainedResource == null) {
						// Add chained parameters
						rMetadataChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "patient", 0, composition.getSubject().getReference(), null);
						resourcemetadataList.addAll(rMetadataChain);
					}
					else if (bundle != null) {
						// Extract composition.subject resource from containing bundle
						org.hl7.fhir.r4.model.Resource subjectEntry = this.getReferencedBundleEntryResource(bundle, composition.getSubject().getReference());

						if (compositionResource != null && subjectEntry != null) {
							// Add chained parameters for composition.patient; do not send baseUrl so references get stored as-is
							rMetadataChain = this.generateChainedResourcemetadata(compositionResource, "", resourceService, "composition.patient.", ResourceType.Patient.name(), subjectEntry, subjectEntry);
							resourcemetadataList.addAll(rMetadataChain);
						}
					}
				}
			}
			// Manually handle Reference.identifier due to Bundle logic complexities
			if (composition.hasSubject() && composition.getSubject().hasIdentifier()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"subject:identifier", composition.getSubject().getIdentifier().getValue(), composition.getSubject().getIdentifier().getSystem(), null, ServicesUtil.INSTANCE.getTextValue(composition.getSubject().getIdentifier()));
				resourcemetadataList.add(rMetadata);

				if (composition.getSubject().hasType("Patient")) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"patient:identifier", composition.getSubject().getIdentifier().getValue(), composition.getSubject().getIdentifier().getSystem(), null, ServicesUtil.INSTANCE.getTextValue(composition.getSubject().getIdentifier()));
					resourcemetadataList.add(rMetadata);
				}
			}

			// title : string
			if (composition.hasTitle()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"title", composition.getTitle());
				resourcemetadataList.add(rMetadata);
			}

			// type : token
			if (composition.hasType() && composition.getType().hasCoding()) {

				for (Coding code : composition.getType().getCoding()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"type", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
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
            if (iBundle != null) {
                try {
                	iBundle.close();
                } catch (IOException ioe) {
                	ioe.printStackTrace();
                }
            }
            if (iComposition != null) {
                try {
                	iComposition.close();
                } catch (IOException ioe) {
                	ioe.printStackTrace();
                }
            }
		}

		return resourcemetadataList;
	}

}
