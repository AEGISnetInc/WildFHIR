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
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.CarePlan;
import org.hl7.fhir.r4.model.CarePlan.CarePlanActivityComponent;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.UriType;

import net.aegis.fhir.model.Resource;
import net.aegis.fhir.model.Resourcemetadata;
import net.aegis.fhir.service.ResourceService;
import net.aegis.fhir.service.util.ServicesUtil;
import net.aegis.fhir.service.util.UTCDateUtil;

/**
 * @author richard.ettema
 *
 */
public class ResourcemetadataCarePlan extends ResourcemetadataProxy {

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
        ByteArrayInputStream iCarePlan = null;
        Resourcemetadata rMetadata = null;
        List<Resourcemetadata> rMetadataChain = null;

		try {
			// Extract and convert the resource contents to a CarePlan object
			if (chainedResource != null) {
				iCarePlan = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iCarePlan = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
			CarePlan carePlan = (CarePlan) xmlP.parse(iCarePlan);
			iCarePlan.close();

			/*
			 * Create new Resourcemetadata objects for each CarePlan metadata value and add to the resourcemetadataList
			 */

			// Add Resource common parameters
            rMetadataChain = this.generateResourcemetadataTagList(resource, carePlan, chainedParameter);
			resourcemetadataList.addAll(rMetadataChain);

			// carePlan.activity
			if (carePlan.hasActivity()) {

				for (CarePlanActivityComponent activity : carePlan.getActivity()) {

					// carePlan.activity.detail
					if (activity.hasDetail()) {

						// (activity-code) detail.code : token
						if (activity.getDetail().hasCode() && activity.getDetail().getCode().hasCoding()) {
							for (Coding code : activity.getDetail().getCode().getCoding()) {
								rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"activity-code", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
								resourcemetadataList.add(rMetadata);
							}
						}

						// activity-date : date(period) - activity.detail.scheduledPeriod
						if (activity.getDetail().hasScheduledPeriod()) {
							Period activityDate = activity.getDetail().getScheduledPeriod();

							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"activity-date", utcDateUtil.formatDate(activityDate.getStart(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(activityDate.getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(activityDate.getStart(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), utcDateUtil.formatDate(activityDate.getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), "PERIOD");
							resourcemetadataList.add(rMetadata);
						}

						// detail.performer : reference
						if (activity.getDetail().hasPerformer()) {

							for (Reference performer : activity.getDetail().getPerformer()) {
								rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "performer", 0, performer, null);
								resourcemetadataList.addAll(rMetadataChain);
							}
						}
					}

					// (activity-reference) activity.reference : reference
					if (activity.hasReference()) {
						rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "activity-reference", 0, activity.getReference(), null);
						resourcemetadataList.addAll(rMetadataChain);
					}
				}
			}

			// carePlan.based-on
			if (carePlan.hasBasedOn()) {

				for (Reference basedOn : carePlan.getBasedOn()) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "based-on", 0, basedOn, null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// care-team : reference
			if (carePlan.hasCareTeam()) {

				for (Reference careteam : carePlan.getAddresses()) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "care-team", 0, careteam, null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// category : token
			if (carePlan.hasCategory()) {

				for (CodeableConcept category : carePlan.getCategory()) {

					if (category.hasCoding()) {
						for (Coding code : category.getCoding()) {
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"category", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rMetadata);
						}
					}
				}
			}

			// condition : reference
			if (carePlan.hasAddresses()) {

				for (Reference condition : carePlan.getAddresses()) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "condition", 0, condition, null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// encounter : reference
			if (carePlan.hasEncounter()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "encounter", 0, carePlan.getEncounter(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// date : date(period)
			if (carePlan.hasPeriod()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"date", utcDateUtil.formatDate(carePlan.getPeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(carePlan.getPeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(carePlan.getPeriod().getStart(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), utcDateUtil.formatDate(carePlan.getPeriod().getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), "PERIOD");
				resourcemetadataList.add(rMetadata);
			}

			// goal : reference
			if (carePlan.hasGoal()) {

				for (Reference goal : carePlan.getGoal()) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "goal", 0, goal, null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// identifier : token
			if (carePlan.hasIdentifier()) {

				for (Identifier identifier : carePlan.getIdentifier()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rMetadata);
				}
			}

			// instantiates-canonical : reference - instantiatesCanonical is a Canonical, no Reference.identifier
			if (carePlan.hasInstantiatesCanonical()) {

				for (CanonicalType instantiates : carePlan.getInstantiatesCanonical()) {
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
			if (carePlan.hasInstantiatesUri()) {

				for (UriType instantiates : carePlan.getInstantiatesUri()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"instantiates-uri", instantiates.asStringValue());
					resourcemetadataList.add(rMetadata);
				}
			}

			// intent : token
			if (carePlan.hasIntent() && carePlan.getIntent() != null) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"intent", carePlan.getIntent().toCode(), carePlan.getIntent().getSystem());
				resourcemetadataList.add(rMetadata);
			}

			// carePlan.part-of
			if (carePlan.hasPartOf()) {

				for (Reference partOf : carePlan.getPartOf()) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "part-of", 0, partOf, null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// subject : reference
			if (carePlan.hasSubject() && carePlan.getSubject().hasReference()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "subject", 0, carePlan.getSubject(), null);
				resourcemetadataList.addAll(rMetadataChain);

				// patient : reference
				if ((carePlan.getSubject().hasReference() && carePlan.getSubject().getReference().indexOf("Patient") >= 0)
						|| (carePlan.getSubject().hasType() && carePlan.getSubject().getType().equals("Patient"))) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "patient", 0, carePlan.getSubject(), null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// carePlan.replaces
			if (carePlan.hasReplaces()) {

				for (Reference replaces : carePlan.getReplaces()) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "replaces", 0, replaces, null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// status : token
			if (carePlan.hasStatus() && carePlan.getStatus() != null) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", carePlan.getStatus().toCode(), carePlan.getStatus().getSystem());
				resourcemetadataList.add(rMetadata);
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		} finally {
	        rMetadata = null;
	        rMetadataChain = null;
            if (iCarePlan != null) {
                try {
                	iCarePlan.close();
                } catch (IOException ioe) {
                	ioe.printStackTrace();
                }
            }
		}

		return resourcemetadataList;
	}

}
