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
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.r4.model.DiagnosticReport.DiagnosticReportMediaComponent;
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
public class ResourcemetadataDiagnosticReport extends ResourcemetadataProxy {

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
        ByteArrayInputStream iDiagnosticReport = null;
        Resourcemetadata rMetadata = null;
        List<Resourcemetadata> rMetadataChain = null;

		try {
			// Extract and convert the resource contents to a DiagnosticReport object
			if (chainedResource != null) {
				iDiagnosticReport = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iDiagnosticReport = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
			DiagnosticReport diagnosticReport = (DiagnosticReport) xmlP.parse(iDiagnosticReport);
			iDiagnosticReport.close();

			/*
			 * Create new Resourcemetadata objects for each DiagnosticReport metadata value and add to the resourcemetadataList
			 */

			// Add Resource common parameters
            rMetadataChain = this.generateResourcemetadataTagList(resource, diagnosticReport, chainedParameter);
			resourcemetadataList.addAll(rMetadataChain);

			// based-on : reference
			if (diagnosticReport.hasBasedOn()) {

				for (Reference basedOn : diagnosticReport.getBasedOn()) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "based-on", 0, basedOn, null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// category : token
			if (diagnosticReport.hasCategory()) {

				for (CodeableConcept category : diagnosticReport.getCategory()) {

					for (Coding coding : category.getCoding()) {
						rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"category", coding.getCode(), coding.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(coding));
						resourcemetadataList.add(rMetadata);
					}
				}
			}

			// code : token
			if (diagnosticReport.hasCode()) {

				for (Coding coding : diagnosticReport.getCode().getCoding()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"code", coding.getCode(), coding.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(coding));
					resourcemetadataList.add(rMetadata);
				}
			}

			// encounter : reference
			if (diagnosticReport.hasEncounter()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "encounter", 0, diagnosticReport.getEncounter(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// date : date(period)
			if (diagnosticReport.hasEffective()) {

				if (diagnosticReport.hasEffectiveDateTimeType()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"date", utcDateUtil.formatDate(diagnosticReport.getEffectiveDateTimeType().getValue(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(diagnosticReport.getEffectiveDateTimeType().getValue(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
					resourcemetadataList.add(rMetadata);
				}
				else if (diagnosticReport.hasEffectivePeriod()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"date", utcDateUtil.formatDate(diagnosticReport.getEffectivePeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(diagnosticReport.getEffectivePeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(diagnosticReport.getEffectivePeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), utcDateUtil.formatDate(diagnosticReport.getEffectivePeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), "PERIOD");
					resourcemetadataList.add(rMetadata);
				}
			}

			// identifier : token
			if (diagnosticReport.hasIdentifier()) {

				for (Identifier identifier : diagnosticReport.getIdentifier()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rMetadata);
				}
			}

			// issued : date
			if (diagnosticReport.hasIssued()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"issued", utcDateUtil.formatDate(diagnosticReport.getIssued(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(diagnosticReport.getIssued(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rMetadata);
			}

			// media : token
			if (diagnosticReport.hasMedia()) {

				for (DiagnosticReportMediaComponent media : diagnosticReport.getMedia()) {
					if (media.hasLink() && media.getLink().hasReference()) {
						rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"media", media.getLink().getReference());
						resourcemetadataList.add(rMetadata);
					}
				}
			}

			// performer : reference
			if (diagnosticReport.hasPerformer()) {

				for (Reference performer : diagnosticReport.getPerformer()) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "performer", 0, performer, null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// result : reference
			if (diagnosticReport.hasResult()) {

				for (Reference result : diagnosticReport.getResult()) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "result", 0, result, null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// results-interpreter : reference
			if (diagnosticReport.hasResultsInterpreter()) {

				for (Reference resultsInterpreter : diagnosticReport.getResultsInterpreter()) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "results-interpreter", 0, resultsInterpreter, null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// specimen : reference
			if (diagnosticReport.hasSpecimen()) {

				for (Reference specimen : diagnosticReport.getSpecimen()) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "specimen", 0, specimen, null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// status : token
			if (diagnosticReport.hasStatus() && diagnosticReport.getStatus() != null) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", diagnosticReport.getStatus().toCode(), diagnosticReport.getStatus().getSystem());
				resourcemetadataList.add(rMetadata);
			}

			// subject : reference
			if (diagnosticReport.hasSubject()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "subject", 0, diagnosticReport.getSubject(), null);
				resourcemetadataList.addAll(rMetadataChain);

				// patient : reference
				if ((diagnosticReport.getSubject().hasReference() && diagnosticReport.getSubject().getReference().indexOf("Patient") >= 0)
						|| (diagnosticReport.getSubject().hasType() && diagnosticReport.getSubject().getType().equals("Patient"))) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "patient", 0, diagnosticReport.getSubject(), null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		} finally {
	        rMetadata = null;
	        rMetadataChain = null;
            if (iDiagnosticReport != null) {
                try {
                	iDiagnosticReport.close();
                } catch (IOException ioe) {
                	ioe.printStackTrace();
                }
            }
		}

		return resourcemetadataList;
	}

}
