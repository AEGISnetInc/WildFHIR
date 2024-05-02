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
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.ImagingStudy;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.ImagingStudy.ImagingStudySeriesComponent;
import org.hl7.fhir.r4.model.ImagingStudy.ImagingStudySeriesInstanceComponent;
import org.hl7.fhir.r4.model.ImagingStudy.ImagingStudySeriesPerformerComponent;

/**
 * @author richard.ettema
 *
 */
public class ResourcemetadataImagingStudy extends ResourcemetadataProxy {

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
        ByteArrayInputStream iImagingStudy = null;

		try {
			// Extract and convert the resource contents to a ImagingStudy object
			if (chainedResource != null) {
				iImagingStudy = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iImagingStudy = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
			ImagingStudy imagingStudy = (ImagingStudy) xmlP.parse(iImagingStudy);
			iImagingStudy.close();

			/*
			 * Create new Resourcemetadata objects for each ImagingStudy metadata value and add to the resourcemetadataList
			 */

			// Add any passed in tags
			List<Resourcemetadata> tagMetadataList = this.generateResourcemetadataTagList(resource, imagingStudy, chainedParameter);
			resourcemetadataList.addAll(tagMetadataList);

			// _id : token
			Resourcemetadata _id = generateResourcemetadata(resource, chainedResource, chainedParameter+"_id", imagingStudy.getId());
			resourcemetadataList.add(_id);

			// _language : token
			if (imagingStudy.getLanguage() != null) {
				Resourcemetadata _language = generateResourcemetadata(resource, chainedResource, chainedParameter+"_language", imagingStudy.getLanguage());
				resourcemetadataList.add(_language);
			}

			// _lastUpdated : date
			if (imagingStudy.getMeta() != null && imagingStudy.getMeta().getLastUpdated() != null) {
				Resourcemetadata _lastUpdated = generateResourcemetadata(resource, chainedResource, chainedParameter+"_lastUpdated", utcDateUtil.formatDate(imagingStudy.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(imagingStudy.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(_lastUpdated);
			}

			// basedon : reference
			if (imagingStudy.hasBasedOn()) {

				List<Resourcemetadata> rBasedOnChain = null;
				for (Reference basedOn : imagingStudy.getBasedOn()) {

					if (basedOn.hasReference()) {
						Resourcemetadata rBasedOn = generateResourcemetadata(resource, chainedResource, chainedParameter+"basedon", generateFullLocalReference(basedOn.getReference(), baseUrl));
						resourcemetadataList.add(rBasedOn);

						if (chainedResource == null) {
							// Add chained parameters for any
							rBasedOnChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "basedon", 0, basedOn.getReference());
							resourcemetadataList.addAll(rBasedOnChain);
						}
					}
				}
			}

			// encounter : reference
			if (imagingStudy.hasEncounter()) {
				Resourcemetadata rEncounter = generateResourcemetadata(resource, chainedResource, chainedParameter+"encounter", generateFullLocalReference(imagingStudy.getEncounter().getReference(), baseUrl));
				resourcemetadataList.add(rEncounter);

				if (chainedResource == null) {
					// Add chained parameters for any
					List<Resourcemetadata> rEncounterChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "encounter", 0, imagingStudy.getEncounter().getReference());
					resourcemetadataList.addAll(rEncounterChain);
				}
			}

			// endpoint : reference
			if (imagingStudy.hasEndpoint()) {

				for (Reference endpoint : imagingStudy.getEndpoint()) {

					if (endpoint.hasReference()) {
						Resourcemetadata rEndpoint = generateResourcemetadata(resource, chainedResource, chainedParameter + "endpoint", generateFullLocalReference(endpoint.getReference(), baseUrl));
						resourcemetadataList.add(rEndpoint);

						if (chainedResource == null) {
							// Add chained parameters
							List<Resourcemetadata> rEndpointChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "endpoint", 0, endpoint.getReference());
							resourcemetadataList.addAll(rEndpointChain);
						}
					}
				}
			}

			// identifier : token
			if (imagingStudy.hasIdentifier()) {

				for (Identifier identifier : imagingStudy.getIdentifier()) {
					Resourcemetadata rIdentifier = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rIdentifier);
				}
			}

			// interpreter : reference
			if (imagingStudy.hasInterpreter()) {

				Resourcemetadata rInterpreter = null;
				List<Resourcemetadata> rInterpreterChain = null;
				for (Reference interpreter : imagingStudy.getInterpreter()) {

					if (interpreter.hasReference()) {
						rInterpreter = generateResourcemetadata(resource, chainedResource, chainedParameter+"interpreter", generateFullLocalReference(interpreter.getReference(), baseUrl));
						resourcemetadataList.add(rInterpreter);

						if (chainedResource == null) {
							// Add chained parameters for any
							rInterpreterChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "interpreter", 0, interpreter.getReference());
							resourcemetadataList.addAll(rInterpreterChain);
						}
					}
				}
			}

			// patient : reference
			// subject : reference
			if (imagingStudy.hasSubject() && imagingStudy.getSubject().hasReference()) {
				String subjectReference = generateFullLocalReference(imagingStudy.getSubject().getReference(), baseUrl);

				Resourcemetadata rSubject = generateResourcemetadata(resource, chainedResource, chainedParameter+"subject", subjectReference);
				resourcemetadataList.add(rSubject);

				if (chainedResource == null) {
					// Add chained parameters
					List<Resourcemetadata> rSubjectChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "subject", 0, imagingStudy.getSubject().getReference());
					resourcemetadataList.addAll(rSubjectChain);
				}

				if (subjectReference.indexOf("Patient") >= 0) {
					Resourcemetadata rPatient = generateResourcemetadata(resource, chainedResource, chainedParameter+"patient", subjectReference);
					resourcemetadataList.add(rPatient);

					if (chainedResource == null) {
						// Add chained parameters
						List<Resourcemetadata> rPatientChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "patient", 0, imagingStudy.getSubject().getReference());
						resourcemetadataList.addAll(rPatientChain);
					}
				}
			}

			// reason : token
			if (imagingStudy.hasReasonCode()) {

				Resourcemetadata rCode = null;
				for (CodeableConcept reason : imagingStudy.getReasonCode()) {

					if (reason.hasCoding()) {
						for (Coding code : reason.getCoding()) {
							rCode = generateResourcemetadata(resource, chainedResource, chainedParameter+"reason", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rCode);
						}
					}
				}
			}

			// referrer : reference
			if (imagingStudy.hasReferrer() && imagingStudy.getReferrer().hasReference()) {

				Resourcemetadata rReferrer = generateResourcemetadata(resource, chainedResource, chainedParameter+"referrer", generateFullLocalReference(imagingStudy.getReferrer().getReference(), baseUrl));
				resourcemetadataList.add(rReferrer);

				if (chainedResource == null) {
					// Add chained parameters for any
					List<Resourcemetadata> rReferrerChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "referrer", 0, imagingStudy.getReferrer().getReference());
					resourcemetadataList.addAll(rReferrerChain);
				}
			}

			// started : datetime
			if (imagingStudy.hasStarted()) {
				Resourcemetadata rStarted = generateResourcemetadata(resource, chainedResource, chainedParameter+"started", utcDateUtil.formatDate(imagingStudy.getStarted(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(imagingStudy.getStarted(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rStarted);
			}

			// status : token
			if (imagingStudy.hasStatus() && imagingStudy.getStatus() != null) {
				Resourcemetadata rStatus = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", imagingStudy.getStatus().toCode(), imagingStudy.getStatus().getSystem());
				resourcemetadataList.add(rStatus);
			}

			if (imagingStudy.hasSeries()) {

				for (ImagingStudySeriesComponent series : imagingStudy.getSeries()) {

					// bodysite : token
					if (series.hasBodySite()) {
						Resourcemetadata rBodySite = generateResourcemetadata(resource, chainedResource, chainedParameter+"bodysite", series.getBodySite().getCode(), series.getBodySite().getSystem());
						resourcemetadataList.add(rBodySite);
					}

					// endpoint : reference
					if (series.hasEndpoint()) {

						for (Reference endpoint : series.getEndpoint()) {

							if (endpoint.hasReference()) {
								Resourcemetadata rEndpoint = generateResourcemetadata(resource, chainedResource, chainedParameter + "endpoint", generateFullLocalReference(endpoint.getReference(), baseUrl));
								resourcemetadataList.add(rEndpoint);

								if (chainedResource == null) {
									// Add chained parameters
									List<Resourcemetadata> rEndpointChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "endpoint", 0, endpoint.getReference());
									resourcemetadataList.addAll(rEndpointChain);
								}
							}
						}
					}

					// modality : token
					if (series.hasModality()) {
						Resourcemetadata rModality = generateResourcemetadata(resource, chainedResource, chainedParameter+"modality", series.getModality().getCode(), series.getModality().getSystem(), null, ServicesUtil.INSTANCE.getTextValue(series.getModality()));
						resourcemetadataList.add(rModality);
					}

					// performer : token
					if (series.hasPerformer()) {

						for (ImagingStudySeriesPerformerComponent performer : series.getPerformer()) {

							if (performer.hasActor() && performer.getActor().hasReference()) {
								Resourcemetadata rPerformer = generateResourcemetadata(resource, chainedResource, chainedParameter + "performer", generateFullLocalReference(performer.getActor().getReference(), baseUrl));
								resourcemetadataList.add(rPerformer);

								if (chainedResource == null) {
									// Add chained parameters
									List<Resourcemetadata> rPerformerChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "performer", 02, performer.getActor().getReference());
									resourcemetadataList.addAll(rPerformerChain);
								}
							}
						}
					}

					// series : token
					if (series.hasUid()) {
						Resourcemetadata rSeriesUid = generateResourcemetadata(resource, chainedResource, chainedParameter+"series", series.getUid());
						resourcemetadataList.add(rSeriesUid);
					}

					if (series.hasInstance()) {

						for (ImagingStudySeriesInstanceComponent instance : series.getInstance()) {

							// dicom-class : uri
							if (instance.hasSopClass()) {
								Resourcemetadata rSopClass = generateResourcemetadata(resource, chainedResource, chainedParameter+"dicom-class", instance.getSopClass().getCode(), instance.getSopClass().getSystem());
								resourcemetadataList.add(rSopClass);
							}

							// instance : identifier
							if (instance.hasUid()) {
								Resourcemetadata rInstanceUid = generateResourcemetadata(resource, chainedResource, chainedParameter+"instance", instance.getUid());
								resourcemetadataList.add(rInstanceUid);
							}
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
