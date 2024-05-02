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
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.MeasureReport;
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
public class ResourcemetadataMeasureReport extends ResourcemetadataProxy {

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
        ByteArrayInputStream iMeasureReport = null;

		try {
			// Extract and convert the resource contents to a MeasureReport object
			if (chainedResource != null) {
				iMeasureReport = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iMeasureReport = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
			MeasureReport measureReport = (MeasureReport) xmlP.parse(iMeasureReport);
			iMeasureReport.close();

			/*
			 * Create new Resourcemetadata objects for each MeasureReport metadata value and add to the resourcemetadataList
			 */

			// Add any passed in tags
			List<Resourcemetadata> tagMetadataList = this.generateResourcemetadataTagList(resource, measureReport, chainedParameter);
			resourcemetadataList.addAll(tagMetadataList);

			// _id : token
			Resourcemetadata _id = generateResourcemetadata(resource, chainedResource, chainedParameter+"_id", measureReport.getId());
			resourcemetadataList.add(_id);

			// _language : token
			if (measureReport.getLanguage() != null) {
				Resourcemetadata _language = generateResourcemetadata(resource, chainedResource, chainedParameter+"_language", measureReport.getLanguage());
				resourcemetadataList.add(_language);
			}

			// _lastUpdated : date
			if (measureReport.getMeta() != null && measureReport.getMeta().getLastUpdated() != null) {
				Resourcemetadata _lastUpdated = generateResourcemetadata(resource, chainedResource, chainedParameter+"_lastUpdated", utcDateUtil.formatDate(measureReport.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(measureReport.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(_lastUpdated);
			}

			// date : date
			if (measureReport.hasDate()) {
				Resourcemetadata rDate = generateResourcemetadata(resource, chainedResource, chainedParameter+"date", utcDateUtil.formatDate(measureReport.getDate(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(measureReport.getDate(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rDate);
			}

			// evaluated-resource : reference
			if (measureReport.hasEvaluatedResource()) {

				String evaluatedResourceReference = null;
				Resourcemetadata rEvaluatedResource = null;
				List<Resourcemetadata> rEvaluatedResourceChain = null;
				for (Reference evaluatedResource : measureReport.getEvaluatedResource()) {

					if (evaluatedResource.hasReference()) {
						evaluatedResourceReference = generateFullLocalReference(evaluatedResource.getReference(), baseUrl);

						rEvaluatedResource = generateResourcemetadata(resource, chainedResource, chainedParameter+"evaluated-resource", evaluatedResourceReference);
						resourcemetadataList.add(rEvaluatedResource);

						if (chainedResource == null) {
							// Add chained parameters for any
							rEvaluatedResourceChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "evaluated-resource", 0, evaluatedResource.getReference());
							resourcemetadataList.addAll(rEvaluatedResourceChain);
						}
					}
				}
			}

			// identifier : token
			if (measureReport.hasIdentifier()) {

				for (Identifier identifier : measureReport.getIdentifier()) {

					Resourcemetadata rIdentifier = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rIdentifier);
				}
			}

			// measure : reference
			if (measureReport.hasMeasure()) {
				Resourcemetadata rMeasure = generateResourcemetadata(resource, chainedResource, chainedParameter+"measure", generateFullLocalReference(measureReport.getMeasure(), baseUrl));
				resourcemetadataList.add(rMeasure);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rMeasureChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "measure", 0, measureReport.getMeasure());
					resourcemetadataList.addAll(rMeasureChain);
				}
			}

			// patient : reference
			// subject : reference
			if (measureReport.hasSubject() && measureReport.getSubject().hasReference()) {
				String subjectReference = generateFullLocalReference(measureReport.getSubject().getReference(), baseUrl);

				Resourcemetadata rSubject = generateResourcemetadata(resource, chainedResource, chainedParameter+"subject", subjectReference);
				resourcemetadataList.add(rSubject);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rSubjectChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "subject", 0, measureReport.getSubject().getReference());
					resourcemetadataList.addAll(rSubjectChain);
				}

				if (subjectReference.indexOf("Patient") >= 0) {
					Resourcemetadata rPatient = generateResourcemetadata(resource, chainedResource, chainedParameter+"patient", subjectReference);
					resourcemetadataList.add(rPatient);

					if (chainedResource == null) {
						// Add chained parameters
						List<Resourcemetadata> rPatientChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "patient", 0, measureReport.getSubject().getReference());
						resourcemetadataList.addAll(rPatientChain);
					}
				}
			}

			// period : date(period)
			if (measureReport.hasPeriod()) {
				Resourcemetadata rPeriod = generateResourcemetadata(resource, chainedResource, chainedParameter+"period", utcDateUtil.formatDate(measureReport.getPeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(measureReport.getPeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(measureReport.getPeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), utcDateUtil.formatDate(measureReport.getPeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), "PERIOD");
				resourcemetadataList.add(rPeriod);
			}

			// reporter : reference
			if (measureReport.hasReporter() && measureReport.getReporter().hasReference()) {
				Resourcemetadata rReporter = generateResourcemetadata(resource, chainedResource, chainedParameter+"reporter", generateFullLocalReference(measureReport.getReporter().getReference(), baseUrl));
				resourcemetadataList.add(rReporter);

				if (chainedResource == null) {
					// Add chained parameters for any
					List<Resourcemetadata> rReporterChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "reporter", 0, measureReport.getReporter().getReference());
					resourcemetadataList.addAll(rReporterChain);
				}
			}

			// status : token
			if (measureReport.hasStatus() && measureReport.getStatus() != null) {
				Resourcemetadata rStatus = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", measureReport.getStatus().toCode(), measureReport.getStatus().getSystem());
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
