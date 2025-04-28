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
        ByteArrayInputStream iImagingStudy = null;
        Resourcemetadata rMetadata = null;
        List<Resourcemetadata> rMetadataChain = null;

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

			// Add Resource common parameters
            rMetadataChain = this.generateResourcemetadataTagList(resource, imagingStudy, chainedParameter);
			resourcemetadataList.addAll(rMetadataChain);

			// basedon : reference
			if (imagingStudy.hasBasedOn()) {

				for (Reference basedOn : imagingStudy.getBasedOn()) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "basedon", 0, basedOn, null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// encounter : reference
			if (imagingStudy.hasEncounter()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "encounter", 0, imagingStudy.getEncounter(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// endpoint : reference
			if (imagingStudy.hasEndpoint()) {

				for (Reference endpoint : imagingStudy.getEndpoint()) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "endpoint", 0, endpoint, null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// identifier : token
			if (imagingStudy.hasIdentifier()) {

				for (Identifier identifier : imagingStudy.getIdentifier()) {
					rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
					resourcemetadataList.add(rMetadata);
				}
			}

			// interpreter : reference
			if (imagingStudy.hasInterpreter()) {

				for (Reference interpreter : imagingStudy.getInterpreter()) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "interpreter", 0, interpreter, null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// subject : reference
			if (imagingStudy.hasSubject() && imagingStudy.getSubject().hasReference()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "subject", 0, imagingStudy.getSubject(), null);
				resourcemetadataList.addAll(rMetadataChain);

				// patient : reference
				if ((imagingStudy.getSubject().hasReference() && imagingStudy.getSubject().getReference().indexOf("Patient") >= 0)
						|| (imagingStudy.getSubject().hasType() && imagingStudy.getSubject().getType().equals("Patient"))) {
					rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "patient", 0, imagingStudy.getSubject(), null);
					resourcemetadataList.addAll(rMetadataChain);
				}
			}

			// reason : token
			if (imagingStudy.hasReasonCode()) {
				for (CodeableConcept reason : imagingStudy.getReasonCode()) {

					if (reason.hasCoding()) {
						for (Coding code : reason.getCoding()) {
							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"reason", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
							resourcemetadataList.add(rMetadata);
						}
					}
				}
			}

			// referrer : reference
			if (imagingStudy.hasReferrer()) {
				rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "referrer", 0, imagingStudy.getReferrer(), null);
				resourcemetadataList.addAll(rMetadataChain);
			}

			// started : datetime
			if (imagingStudy.hasStarted()) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"started", utcDateUtil.formatDate(imagingStudy.getStarted(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(imagingStudy.getStarted(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rMetadata);
			}

			// status : token
			if (imagingStudy.hasStatus() && imagingStudy.getStatus() != null) {
				rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", imagingStudy.getStatus().toCode(), imagingStudy.getStatus().getSystem());
				resourcemetadataList.add(rMetadata);
			}

			if (imagingStudy.hasSeries()) {

				for (ImagingStudySeriesComponent series : imagingStudy.getSeries()) {

					// bodysite : token
					if (series.hasBodySite()) {
						rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"bodysite", series.getBodySite().getCode(), series.getBodySite().getSystem());
						resourcemetadataList.add(rMetadata);
					}

					// endpoint : reference
					if (series.hasEndpoint()) {

						for (Reference endpoint : series.getEndpoint()) {
							rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "endpoint", 0, endpoint, null);
							resourcemetadataList.addAll(rMetadataChain);
						}
					}

					// modality : token
					if (series.hasModality()) {
						rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"modality", series.getModality().getCode(), series.getModality().getSystem(), null, ServicesUtil.INSTANCE.getTextValue(series.getModality()));
						resourcemetadataList.add(rMetadata);
					}

					// performer : reference
					if (series.hasPerformer()) {

						for (ImagingStudySeriesPerformerComponent performer : series.getPerformer()) {

							if (performer.hasActor()) {
								rMetadataChain = this.generateChainedResourcemetadataAny(resource, chainedResource, baseUrl, resourceService, chainedParameter, "performer", 0, performer.getActor(), null);
								resourcemetadataList.addAll(rMetadataChain);
							}
						}
					}

					// series : token
					if (series.hasUid()) {
						rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"series", series.getUid());
						resourcemetadataList.add(rMetadata);
					}

					if (series.hasInstance()) {

						for (ImagingStudySeriesInstanceComponent instance : series.getInstance()) {

							// dicom-class : uri
							if (instance.hasSopClass()) {
								rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"dicom-class", instance.getSopClass().getCode(), instance.getSopClass().getSystem());
								resourcemetadataList.add(rMetadata);
							}

							// instance : identifier
							if (instance.hasUid()) {
								rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"instance", instance.getUid());
								resourcemetadataList.add(rMetadata);
							}
						}
					}
				}
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		} finally {
	        rMetadata = null;
	        rMetadataChain = null;
            if (iImagingStudy != null) {
                try {
                	iImagingStudy.close();
                } catch (IOException ioe) {
                	ioe.printStackTrace();
                }
            }
		}

		return resourcemetadataList;
	}

}
