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
        Resourcemetadata rMetadata = null;
        List<Resourcemetadata> rMetadataChain = null;

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

			// Add Resource common parameters
            rMetadataChain = this.generateResourcemetadataTagList(resource, procedure, chainedParameter);
			resourcemetadataList.addAll(rMetadataChain);

			// based-on : reference
			if (procedure.hasBasedOn()) {

				for (Reference basedOn : procedure.getBasedOn()) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "based-on", 0, basedOn, null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// category : token
			if (procedure.hasCategory() && procedure.getCategory().hasCoding()) {

				for (Coding code : procedure.getCategory().getCoding()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"category", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rMetadata);
				}
			}

			// code : token
			if (procedure.hasCode() && procedure.getCode().hasCoding()) {

				for (Coding code : procedure.getCode().getCoding()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"code", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
					resourcemetadataList.add(rMetadata);
				}
			}

			// date : datetime
			// date : period
			if (procedure.hasPerformed()) {

				if (procedure.getPerformed() instanceof DateTimeType) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"date", utcDateUtil.formatDate(procedure.getPerformedDateTimeType().getValue(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(procedure.getPerformedDateTimeType().getValue(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
					resourcemetadataList.add(rMetadata);
				}
				else if (procedure.getPerformed() instanceof Period) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"date", utcDateUtil.formatDate(procedure.getPerformedPeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(procedure.getPerformedPeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(procedure.getPerformedPeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), utcDateUtil.formatDate(procedure.getPerformedPeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), "PERIOD");
					resourcemetadataList.add(rMetadata);
				}
			}

			// encounter : reference
			if (procedure.hasEncounter()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "encounter", 0, procedure.getEncounter(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// identifier : token
			if (procedure.hasIdentifier()) {

				for (Identifier identifier : procedure.getIdentifier()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rMetadata);
				}
			}

			// instantiates-canonical : reference - instantiates is a Canonical, no Reference.identifier
			if (procedure.hasInstantiatesCanonical()) {

				for (CanonicalType instantiates : procedure.getInstantiatesCanonical()) {
					String objectReference = generateFullLocalReference(instantiates.asStringValue(), baseUrl);

					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"instantiates-canonical", objectReference);
					resourcemetadataList.add(rMetadata);

					if (chainedResource == null) {
						// Add chained parameters
						rMetadataChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "instantiates-canonical", 0, instantiates.asStringValue(), null);
						resourcemetadataList.addAll(rMetadataChain);
					}
				}
			}

			// instantiates-uri : uri
			if (procedure.hasInstantiatesUri()) {

				for (UriType instantiates : procedure.getInstantiatesUri()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"instantiates-uri", instantiates.asStringValue());
					resourcemetadataList.add(rMetadata);
				}
			}

			// location : reference
			if (procedure.hasLocation()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "location", 0, procedure.getLocation(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// part-of : reference
			if (procedure.hasPartOf()) {

				for (Reference partOf : procedure.getPartOf()) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "part-of", 0, partOf, null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// subject : reference
			if (procedure.hasSubject()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "subject", 0, procedure.getSubject(), null);
				resourcemetadataList.addAll(rMetadataChain);

				// patient : reference
				if ((procedure.getSubject().hasReference() && procedure.getSubject().getReference().indexOf("Patient") >= 0)
						|| (procedure.getSubject().hasType() && procedure.getSubject().getType().equals("Patient"))) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "patient", 0, procedure.getSubject(), null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// performer : reference
			if (procedure.hasPerformer()) {
				for (ProcedurePerformerComponent performer : procedure.getPerformer()) {

					if (performer.hasActor()) {
						rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "performer", 0, performer.getActor(), null);
						resourcemetadataList.addAll(rMetadataChain);
					}
				}
			}

			// reason-code : token
			if (procedure.hasReasonCode()) {
				for (CodeableConcept reasonCode : procedure.getReasonCode()) {

					if (reasonCode.hasCoding()) {
						for (Coding code : reasonCode.getCoding()) {
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"reason-code", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rMetadata);
						}
					}
				}
			}

			// reason-reference : reference
			if (procedure.hasReasonReference()) {

				for (Reference reasonReference : procedure.getReasonReference()) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "reason-reference", 0, reasonReference, null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// status : token
			if (procedure.hasStatus() && procedure.getStatus() != null) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", procedure.getStatus().toCode(), procedure.getStatus().getSystem());
				resourcemetadataList.add(rMetadata);
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		} finally {
	        rMetadata = null;
	        rMetadataChain = null;
            if (iProcedure != null) {
                try {
                	iProcedure.close();
                } catch (IOException ioe) {
                	ioe.printStackTrace();
                }
            }
		}

		return resourcemetadataList;
	}

}
