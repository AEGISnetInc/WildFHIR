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
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.formats.XmlParser;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.MedicationStatement;
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
public class ResourcemetadataMedicationStatement extends ResourcemetadataProxy {

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
        ByteArrayInputStream iMedicationStatement = null;

		try {
            // Extract and convert the resource contents to a MedicationStatement object
			if (chainedResource != null) {
				iMedicationStatement = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iMedicationStatement = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
            MedicationStatement medicationStatement = (MedicationStatement) xmlP.parse(iMedicationStatement);
            iMedicationStatement.close();

			/*
             * Create new Resourcemetadata objects for each MedicationStatement metadata value and add to the resourcemetadataList
			 */

			// Add any passed in tags
			List<Resourcemetadata> tagMetadataList = this.generateResourcemetadataTagList(resource, medicationStatement, chainedParameter);
			resourcemetadataList.addAll(tagMetadataList);

			// _id : token
			Resourcemetadata _id = generateResourcemetadata(resource, chainedResource, chainedParameter+"_id", medicationStatement.getId());
			resourcemetadataList.add(_id);

			// _language : token
			if (medicationStatement.getLanguage() != null) {
				Resourcemetadata _language = generateResourcemetadata(resource, chainedResource, chainedParameter+"_language", medicationStatement.getLanguage());
				resourcemetadataList.add(_language);
			}

			// _lastUpdated : date
			if (medicationStatement.getMeta() != null && medicationStatement.getMeta().getLastUpdated() != null) {
				Resourcemetadata _lastUpdated = generateResourcemetadata(resource, chainedResource, chainedParameter+"_lastUpdated", utcDateUtil.formatDate(medicationStatement.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(medicationStatement.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(_lastUpdated);
			}

			// category : token
			if (medicationStatement.hasCategory() && medicationStatement.getCategory().hasCoding()) {

				Resourcemetadata rCode = null;
				for (Coding code : medicationStatement.getCategory().getCoding()) {
					rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"category", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rCode);
				}
			}

			// code : token
			if (medicationStatement.hasMedicationCodeableConcept() && medicationStatement.getMedicationCodeableConcept().hasCoding()) {

				Resourcemetadata rCode = null;
				for (Coding code : medicationStatement.getMedicationCodeableConcept().getCoding()) {
					rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"code", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rCode);
				}
			}

			// context : reference
			if (medicationStatement.hasContext() && medicationStatement.getContext().hasReference()) {
				String contextString = generateFullLocalReference(medicationStatement.getContext().getReference(), baseUrl);

				Resourcemetadata rContext = generateResourcemetadata(resource, chainedResource, chainedParameter+"context", contextString);
				resourcemetadataList.add(rContext);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rContextChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "context.", 0, medicationStatement.getContext().getReference());
					resourcemetadataList.addAll(rContextChain);
				}
			}

			// effective
			if (medicationStatement.hasEffective()) {

				// effective : date(datetime)
				if (medicationStatement.hasEffectiveDateTimeType()) {
					Resourcemetadata rEffective = generateResourcemetadata(resource, chainedResource, chainedParameter+"effective", utcDateUtil.formatDate(medicationStatement.getEffectiveDateTimeType().getValue(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(medicationStatement.getEffectiveDateTimeType().getValue(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
					resourcemetadataList.add(rEffective);
				}
				// effective : date(period)
				if (medicationStatement.hasEffectivePeriod()) {

					Date periodStart = medicationStatement.getEffectivePeriod().getStart();
					Date periodEnd = medicationStatement.getEffectivePeriod().getEnd();

					Resourcemetadata rEffective = generateResourcemetadata(resource, chainedResource, chainedParameter+"effective", utcDateUtil.formatDate(periodStart, UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(periodEnd, UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(periodStart, UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), utcDateUtil.formatDate(periodEnd, UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), "PERIOD");
					resourcemetadataList.add(rEffective);
				}
			}

			// identifier : token
			if (medicationStatement.hasIdentifier()) {

				for (Identifier identifier : medicationStatement.getIdentifier()) {

					Resourcemetadata rIdentifier = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rIdentifier);
				}
			}

			// medication : reference
			if (medicationStatement.hasMedicationReference() && medicationStatement.getMedicationReference().hasReference()) {
				Resourcemetadata rMedicationReference = generateResourcemetadata(resource, chainedResource, chainedParameter+"medication", generateFullLocalReference(medicationStatement.getMedicationReference().getReference(), baseUrl));
				resourcemetadataList.add(rMedicationReference);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rMedicationChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "medication", 0, medicationStatement.getMedicationReference().getReference());
					resourcemetadataList.addAll(rMedicationChain);
				}
			}

			// part-of : reference
			if (medicationStatement.hasPartOf()) {

				for (Reference partOf : medicationStatement.getPartOf()) {

					if (partOf.hasReference()) {
						Resourcemetadata rPartOf = generateResourcemetadata(resource, chainedResource, chainedParameter+"part-of", generateFullLocalReference(partOf.getReference(), baseUrl));
						resourcemetadataList.add(rPartOf);

						if (chainedResource == null) {
							// Add chained parameters for any
							List<Resourcemetadata> rPartOfChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "part-of", 0, partOf.getReference());
							resourcemetadataList.addAll(rPartOfChain);
						}
					}
				}
			}

			// patient : reference
			// subject : reference
			if (medicationStatement.hasSubject() && medicationStatement.getSubject().hasReference()) {
				String subjectReference = generateFullLocalReference(medicationStatement.getSubject().getReference(), baseUrl);

				Resourcemetadata rSubject = generateResourcemetadata(resource, chainedResource, chainedParameter+"subject", subjectReference);
				resourcemetadataList.add(rSubject);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rSubjectChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "subject", 0, medicationStatement.getSubject().getReference());
					resourcemetadataList.addAll(rSubjectChain);
				}

				if (subjectReference.indexOf("Patient") >= 0) {
					Resourcemetadata rPatient = generateResourcemetadata(resource, chainedResource, chainedParameter+"patient", subjectReference);
					resourcemetadataList.add(rPatient);

					if (chainedResource == null) {
						// Add chained parameters
						List<Resourcemetadata> rPatientChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "patient", 0, medicationStatement.getSubject().getReference());
						resourcemetadataList.addAll(rPatientChain);
					}
				}
			}

			// source : reference
			if (medicationStatement.hasInformationSource() && medicationStatement.getInformationSource().hasReference()) {
				Resourcemetadata rInformationSource = generateResourcemetadata(resource, chainedResource, chainedParameter+"source",generateFullLocalReference(medicationStatement.getInformationSource().getReference(), baseUrl));
				resourcemetadataList.add(rInformationSource);

				if (chainedResource == null) {
					// Add chained parameters for any
					List<Resourcemetadata> rSourceChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "source", 0, medicationStatement.getInformationSource().getReference());
					resourcemetadataList.addAll(rSourceChain);
				}
			}

			// status : token
			if (medicationStatement.hasStatus() && medicationStatement.getStatus() != null) {
				Resourcemetadata rStatus = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", medicationStatement.getStatus().toCode(), medicationStatement.getStatus().getSystem());
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
