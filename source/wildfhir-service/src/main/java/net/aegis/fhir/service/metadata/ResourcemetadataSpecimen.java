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
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Specimen;
import org.hl7.fhir.r4.model.Specimen.SpecimenContainerComponent;

/**
 * @author richard.ettema
 *
 */
public class ResourcemetadataSpecimen extends ResourcemetadataProxy {

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
        ByteArrayInputStream iSpecimen = null;

		try {
			// Extract and convert the resource contents to a Specimen object
			if (chainedResource != null) {
				iSpecimen = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iSpecimen = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
			Specimen specimen = (Specimen) xmlP.parse(iSpecimen);
			iSpecimen.close();

			/*
			 * Create new Resourcemetadata objects for each Specimen metadata value and add to the resourcemetadataList
			 */

			// Add any passed in tags
			List<Resourcemetadata> tagMetadataList = this.generateResourcemetadataTagList(resource, specimen, chainedParameter);
			resourcemetadataList.addAll(tagMetadataList);

			// _id : token
			if (specimen.getId() != null) {
				Resourcemetadata _id = generateResourcemetadata(resource, chainedResource, chainedParameter+"_id", specimen.getId());
				resourcemetadataList.add(_id);
			}

			// _language : token
			if (specimen.getLanguage() != null) {
				Resourcemetadata _language = generateResourcemetadata(resource, chainedResource, chainedParameter+"_language", specimen.getLanguage());
				resourcemetadataList.add(_language);
			}

			// _lastUpdated : date
			if (specimen.getMeta() != null && specimen.getMeta().getLastUpdated() != null) {
				Resourcemetadata _lastUpdated = generateResourcemetadata(resource, chainedResource, chainedParameter+"_lastUpdated", utcDateUtil.formatDate(specimen.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(specimen.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(_lastUpdated);
			}

			// accession : token
			if (specimen.hasAccessionIdentifier()) {
				Resourcemetadata rAccessionIdentifier = generateResourcemetadata(resource, chainedResource, chainedParameter+"accession", specimen.getAccessionIdentifier().getValue(), specimen.getAccessionIdentifier().getSystem(), null, ServicesUtil.INSTANCE.getTextValue(specimen.getAccessionIdentifier()));
				resourcemetadataList.add(rAccessionIdentifier);
			}

			// identifier : token
			if (specimen.hasIdentifier()) {

				for (Identifier identifier : specimen.getIdentifier()) {

					Resourcemetadata rIdentifier = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rIdentifier);
				}
			}

			// parent : token
			if (specimen.hasParent()) {

				List<Resourcemetadata> rParentChain = null;
				for (Reference parent : specimen.getParent()) {

					if (parent.hasReference()) {
						Resourcemetadata rParent = generateResourcemetadata(resource, chainedResource, chainedParameter+"parent", parent.getReference());
						resourcemetadataList.add(rParent);

						if (chainedResource == null) {
							// Add chained parameters
							rParentChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "parent", 0, parent.getReference(), null);
							resourcemetadataList.addAll(rParentChain);
						}
					}
				}
			}

			// patient : reference
			// subject : reference
			if (specimen.hasSubject() && specimen.getSubject().hasReference()) {
				String subjectReference = generateFullLocalReference(specimen.getSubject().getReference(), baseUrl);

				Resourcemetadata rSubject = generateResourcemetadata(resource, chainedResource, chainedParameter+"subject", subjectReference);
				resourcemetadataList.add(rSubject);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rSubjectChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "subject", 0, specimen.getSubject().getReference(), null);
					resourcemetadataList.addAll(rSubjectChain);
				}

				if (subjectReference.indexOf("Patient") >= 0) {
					Resourcemetadata rPatient = generateResourcemetadata(resource, chainedResource, chainedParameter+"patient", subjectReference);
					resourcemetadataList.add(rPatient);

					if (chainedResource == null) {
						// Add chained parameters
						List<Resourcemetadata> rPatientChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "patient", 0, specimen.getSubject().getReference(), null);
						resourcemetadataList.addAll(rPatientChain);
					}
				}
			}

			// status : token
			if (specimen.hasStatus() && specimen.getStatus() != null) {
				Resourcemetadata rStatus = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", specimen.getStatus().toCode(), specimen.getStatus().getSystem());
				resourcemetadataList.add(rStatus);
			}

			// type : token
			if (specimen.hasType() && specimen.getType().hasCoding()) {

				Resourcemetadata rType = null;
				for (Coding type : specimen.getType().getCoding()) {
					rType = generateResourcemetadata(resource, chainedResource, chainedParameter+"type", type.getCode(), type.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(type));
					resourcemetadataList.add(rType);
				}
			}

			if (specimen.hasCollection()) {

				// bodysite : token
				if (specimen.getCollection().hasBodySite() && specimen.getCollection().getBodySite().hasCoding()) {

					Resourcemetadata rCode = null;
					for (Coding code : specimen.getCollection().getBodySite().getCoding()) {
						rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"bodysite", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
						resourcemetadataList.add(rCode);
					}
				}

				// collected : date(period)
				if (specimen.getCollection().hasCollected()) {

					if (specimen.getCollection().getCollected() instanceof DateTimeType) {
						Resourcemetadata rCollectedDateTime = generateResourcemetadata(resource, chainedResource, chainedParameter+"collected", utcDateUtil.formatDate(specimen.getCollection().getCollectedDateTimeType().getValue(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(specimen.getCollection().getCollectedDateTimeType().getValue(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
						resourcemetadataList.add(rCollectedDateTime);
					}
					else if (specimen.getCollection().getCollected() instanceof Period) {
						Resourcemetadata rCollectedPeriod = generateResourcemetadata(resource, chainedResource, chainedParameter+"collected", utcDateUtil.formatDate(specimen.getCollection().getCollectedPeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(specimen.getCollection().getCollectedPeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(specimen.getCollection().getCollectedPeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), utcDateUtil.formatDate(specimen.getCollection().getCollectedPeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), "PERIOD");
						resourcemetadataList.add(rCollectedPeriod);
					}
				}

				// collector : reference
				if (specimen.getCollection().hasCollector() && specimen.getCollection().getCollector().hasReference()) {
					Resourcemetadata rCollector = generateResourcemetadata(resource, chainedResource, chainedParameter+"collector", specimen.getCollection().getCollector().getReference());
					resourcemetadataList.add(rCollector);

					if (chainedResource == null) {
						// Add chained parameters
						List<Resourcemetadata> rCollectorChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "collector", 0, specimen.getCollection().getCollector().getReference(), null);
						resourcemetadataList.addAll(rCollectorChain);
					}
				}
			}

			if (specimen.hasContainer()) {

				for (SpecimenContainerComponent container : specimen.getContainer()) {

					// container : token
					if (container.hasType()) {

						for (Coding coding : container.getType().getCoding()) {

							Resourcemetadata rSiteCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"container", coding.getCode(), coding.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(coding));
							resourcemetadataList.add(rSiteCode);
						}
					}

					// container-id : token
					if (container.hasIdentifier()) {

						for (Identifier containerid : container.getIdentifier()) {

							Resourcemetadata rIdentifier = generateResourcemetadata(resource, chainedResource, chainedParameter+"container-id", containerid.getValue(), containerid.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(containerid));
							resourcemetadataList.add(rIdentifier);
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
