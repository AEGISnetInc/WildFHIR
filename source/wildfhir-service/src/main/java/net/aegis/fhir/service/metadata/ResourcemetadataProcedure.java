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

import net.aegis.fhir.model.Resource;
import net.aegis.fhir.model.Resourcemetadata;
import net.aegis.fhir.service.ResourceService;
import net.aegis.fhir.service.util.ServicesUtil;
import net.aegis.fhir.service.util.UTCDateUtil;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.formats.XmlParser;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Procedure;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.UriType;
import org.hl7.fhir.r4.model.Procedure.ProcedurePerformerComponent;

/**
 * @author richard.ettema
 *
 */
public class ResourcemetadataProcedure extends ResourcemetadataProxy {

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
        ByteArrayInputStream iProcedure = null;

		try {
			// Extract and convert the resource contents to a Procedure object
			if (chainedResource != null) {
				iProcedure = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iProcedure = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
			Procedure procedure = (Procedure) xmlP.parse(iProcedure);
			iProcedure.close();

			/*
			 * Create new Resourcemetadata objects for each Procedure metadata value and add to the resourcemetadataList
			 */

			// Add any passed in tags
			List<Resourcemetadata> tagMetadataList = this.generateResourcemetadataTagList(resource, procedure, chainedParameter);
			resourcemetadataList.addAll(tagMetadataList);

			// _id : token
			if (procedure.getId() != null) {
				Resourcemetadata _id = generateResourcemetadata(resource, chainedResource, chainedParameter+"_id", procedure.getId());
				resourcemetadataList.add(_id);
			}

			// _language : token
			if (procedure.getLanguage() != null) {
				Resourcemetadata _language = generateResourcemetadata(resource, chainedResource, chainedParameter+"_language", procedure.getLanguage());
				resourcemetadataList.add(_language);
			}

			// _lastUpdated : date
			if (procedure.getMeta() != null && procedure.getMeta().getLastUpdated() != null) {
				Resourcemetadata _lastUpdated = generateResourcemetadata(resource, chainedResource, chainedParameter+"_lastUpdated", utcDateUtil.formatDate(procedure.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(procedure.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(_lastUpdated);
			}

			// based-on : reference
			if (procedure.hasBasedOn()) {

				List<Resourcemetadata> rBasedOnChain = null;
				for (Reference basedOn : procedure.getBasedOn()) {

					if (basedOn.hasReference()) {
						Resourcemetadata rBasedOn = generateResourcemetadata(resource, chainedResource, chainedParameter+"based-on", generateFullLocalReference(basedOn.getReference(), baseUrl));
						resourcemetadataList.add(rBasedOn);

						if (chainedResource == null) {
							// Add chained parameters for any
							rBasedOnChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "based-on", 0, basedOn.getReference(), null);
							resourcemetadataList.addAll(rBasedOnChain);
						}
					}
				}
			}

			// category : token
			if (procedure.hasCategory() && procedure.getCategory().hasCoding()) {

				Resourcemetadata rCode = null;
				for (Coding code : procedure.getCategory().getCoding()) {
					rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"category", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rCode);
				}
			}

			// code : token
			if (procedure.hasCode() && procedure.getCode().hasCoding()) {

				Resourcemetadata rCode = null;
				for (Coding code : procedure.getCode().getCoding()) {
					rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"code", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rCode);
				}
			}

			// date : datetime
			// date : period
			if (procedure.hasPerformed()) {

				if (procedure.getPerformed() instanceof DateTimeType) {
					Resourcemetadata rPerformedDateTimeType = generateResourcemetadata(resource, chainedResource, chainedParameter+"date", utcDateUtil.formatDate(procedure.getPerformedDateTimeType().getValue(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(procedure.getPerformedDateTimeType().getValue(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
					resourcemetadataList.add(rPerformedDateTimeType);
				}
				else if (procedure.getPerformed() instanceof Period) {
					Resourcemetadata rPerformedPeriod = generateResourcemetadata(resource, chainedResource, chainedParameter+"date", utcDateUtil.formatDate(procedure.getPerformedPeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(procedure.getPerformedPeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(procedure.getPerformedPeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), utcDateUtil.formatDate(procedure.getPerformedPeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), "PERIOD");
					resourcemetadataList.add(rPerformedPeriod);
				}
			}

			// encounter : reference
			if (procedure.hasEncounter() && procedure.getEncounter().hasReference()) {
				String contextString = generateFullLocalReference(procedure.getEncounter().getReference(), baseUrl);

				Resourcemetadata rEncounter = generateResourcemetadata(resource, chainedResource, chainedParameter+"encounter", contextString);
				resourcemetadataList.add(rEncounter);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rEncounterChain =  this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "encounter", 0, procedure.getEncounter().getReference(), null);
					resourcemetadataList.addAll(rEncounterChain);
				}
			}

			// identifier : token
			if (procedure.hasIdentifier()) {

				for (Identifier identifier : procedure.getIdentifier()) {

					Resourcemetadata rIdentifier = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rIdentifier);
				}
			}

			// instantiates-canonical : reference
			if (procedure.hasInstantiatesCanonical()) {

				for (CanonicalType instantiates : procedure.getInstantiatesCanonical()) {
					String objectReference = generateFullLocalReference(instantiates.asStringValue(), baseUrl);

					List<Resourcemetadata> rInstantiatesChain = null;
					Resourcemetadata rReference = generateResourcemetadata(resource, chainedResource, chainedParameter+"instantiates-canonical", objectReference);
					resourcemetadataList.add(rReference);

					if (chainedResource == null) {
						// Add chained parameters
						rInstantiatesChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "instantiates-canonical", 0, instantiates.asStringValue(), null);
						resourcemetadataList.addAll(rInstantiatesChain);
					}
				}
			}

			// instantiates-uri : uri
			if (procedure.hasInstantiatesUri()) {

				for (UriType instantiates : procedure.getInstantiatesUri()) {

					Resourcemetadata rInstantiates = generateResourcemetadata(resource, chainedResource, chainedParameter+"instantiates-uri", instantiates.asStringValue());
					resourcemetadataList.add(rInstantiates);
				}
			}

			// location : reference
			if (procedure.hasLocation() && procedure.getLocation().hasReference()) {
				Resourcemetadata rLocation = generateResourcemetadata(resource, chainedResource, chainedParameter+"location", generateFullLocalReference(procedure.getLocation().getReference(), baseUrl));
				resourcemetadataList.add(rLocation);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rLocationChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "location", 0, procedure.getLocation().getReference(), null);
					resourcemetadataList.addAll(rLocationChain);
				}
			}

			// part-of : reference
			if (procedure.hasPartOf()) {

				List<Resourcemetadata> rPartOfChain = null;
				for (Reference partOf : procedure.getPartOf()) {

					if (partOf.hasReference()) {
						Resourcemetadata rPartOf = generateResourcemetadata(resource, chainedResource, chainedParameter+"part-of", generateFullLocalReference(partOf.getReference(), baseUrl));
						resourcemetadataList.add(rPartOf);

						if (chainedResource == null) {
							// Add chained parameters for any
							rPartOfChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "part-of", 0, partOf.getReference(), null);
							resourcemetadataList.addAll(rPartOfChain);
						}
					}
				}
			}

			// patient : reference
			// subject : reference
			if (procedure.hasSubject() && procedure.getSubject().hasReference()) {
				String subjectReference = generateFullLocalReference(procedure.getSubject().getReference(), baseUrl);

				Resourcemetadata rSubject = generateResourcemetadata(resource, chainedResource, chainedParameter+"subject", subjectReference);
				resourcemetadataList.add(rSubject);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rSubjectChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "subject", 0, procedure.getSubject().getReference(), null);
					resourcemetadataList.addAll(rSubjectChain);
				}

				if (subjectReference.indexOf("Patient") >= 0) {
					Resourcemetadata rPatient = generateResourcemetadata(resource, chainedResource, chainedParameter+"patient", subjectReference);
					resourcemetadataList.add(rPatient);

					if (chainedResource == null) {
						// Add chained parameters
						List<Resourcemetadata> rPatientChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "patient", 0, procedure.getSubject().getReference(), null);
						resourcemetadataList.addAll(rPatientChain);
					}
				}
			}

			// performer : reference
			if (procedure.hasPerformer()) {

				List<Resourcemetadata> rPerformerChain = null;
				for (ProcedurePerformerComponent performer : procedure.getPerformer()) {

					if (performer.hasActor() && performer.getActor().hasReference()) {
						Resourcemetadata rPerformer = generateResourcemetadata(resource, chainedResource, chainedParameter+"performer", generateFullLocalReference(performer.getActor().getReference(), baseUrl));
						resourcemetadataList.add(rPerformer);

						if (chainedResource == null) {
							// Add chained parameters for any
							rPerformerChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "performer", 0, performer.getActor().getReference(), null);
							resourcemetadataList.addAll(rPerformerChain);
						}
					}
				}
			}

			// reason-code : token
			if (procedure.hasReasonCode()) {

				Resourcemetadata rCode = null;
				for (CodeableConcept reasonCode : procedure.getReasonCode()) {

					if (reasonCode.hasCoding()) {
						for (Coding code : reasonCode.getCoding()) {
							rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"reason-code", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rCode);
						}
					}
				}
			}

			// reason-reference : reference
			if (procedure.hasReasonReference()) {

				Resourcemetadata rReasonReference = null;
				List<Resourcemetadata> rReasonReferenceChain = null;
				for (Reference reasonReference : procedure.getReasonReference()) {

					if (reasonReference.hasReference()) {
						rReasonReference = generateResourcemetadata(resource, chainedResource, chainedParameter+"reason-reference", generateFullLocalReference(reasonReference.getReference(), baseUrl));
						resourcemetadataList.add(rReasonReference);

						if (chainedResource == null) {
							// Add chained parameters for any
							rReasonReferenceChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "reason-reference", 0, reasonReference.getReference(), null);
							resourcemetadataList.addAll(rReasonReferenceChain);
						}
					}
				}
			}

			// status : token
			if (procedure.hasStatus() && procedure.getStatus() != null) {
				Resourcemetadata rStatus = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", procedure.getStatus().toCode(), procedure.getStatus().getSystem());
				resourcemetadataList.add(rStatus);
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		}

		return resourcemetadataList;
	}

}
