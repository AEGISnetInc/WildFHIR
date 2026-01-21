/*
 * #%L
 * WildFHIR - wildfhir-service
 * %%
 * Copyright (C) 2025 AEGIS.net, Inc.
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
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.formats.XmlParser;
import org.hl7.fhir.r4.model.Basic;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.UriType;

import net.aegis.fhir.model.Resource;
import net.aegis.fhir.model.Resourcemetadata;
import net.aegis.fhir.service.ResourceService;
import net.aegis.fhir.service.util.ServicesUtil;
import net.aegis.fhir.service.util.UTCDateUtil;

/**
 * SubscriptionTopic R5 Special Support
 * 
 * This is a FHIR R5 canonical resource type. The FHIR core library conversion package is used
 * to accept the R5 SubscriptionTopic resource instance which is then immediately converted to
 * a FHIR R4 Basic resource instance where all R5 data elements are represented as cross version
 * extensions.
 * 
 * WildFHIR leverages the existing resource meta data logic to store custom search parameters
 * needed for internal support of the R5 Subscriptions Backport functionality. Storage of
 * the R5 SubscriptionTopic resource will be done via a FHIR R4 Basic resource.
 * 
 * @author richard.ettema
 *
 */
public class ResourcemetadataSubscriptionTopic extends ResourcemetadataProxy {

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
        ByteArrayInputStream iSubscriptionTopic = null;
        Resourcemetadata rMetadata = null;
        List<Resourcemetadata> rMetadataChain = null;

		try {
            // Extract and convert the resource contents to a Parameters object
			if (chainedResource != null) {
				iSubscriptionTopic = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iSubscriptionTopic = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
            Basic subscriptionTopic = (Basic) xmlP.parse(iSubscriptionTopic);
            iSubscriptionTopic.close();

			/*
             * Create new Resourcemetadata objects for each Basic(SubscriptionTopic) metadata value and add to the resourcemetadataList
			 */

			// Add Resource common parameters
            rMetadataChain = this.generateResourcemetadataTagList(resource, subscriptionTopic, chainedParameter);
			resourcemetadataList.addAll(rMetadataChain);

			/*
			 * R4 SubscriptionTopic is stored as a Basic resource type. The custom search parameters will be
			 * extracted from the R5 cross version extensions.
			 */

			if (subscriptionTopic.hasExtension()) {

				for (Extension extension : subscriptionTopic.getExtension()) {

					if (extension.hasUrl() && extension.hasValue()) {

						// date : date
						if (extension.getUrl().equals("http://hl7.org/fhir/5.0/StructureDefinition/extension-SubscriptionTopic.date") && extension.getValue() instanceof DateTimeType) {
							Date date = ((DateTimeType)extension.getValue()).getValue();

							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"date", utcDateUtil.formatDate(date, UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(date, UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
							resourcemetadataList.add(rMetadata);
						}

						// derived-or-self : uri, canonical
						// url : uri
						else if (extension.getUrl().equals("http://hl7.org/fhir/5.0/StructureDefinition/extension-SubscriptionTopic.url") && extension.getValue() instanceof UriType) {
							UriType url = (UriType)extension.getValue();

							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"derived-or-self", url.getValueAsString());
							resourcemetadataList.add(rMetadata);

							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"url", url.getValueAsString());
							resourcemetadataList.add(rMetadata);
						}
						else if (extension.getUrl().equals("http://hl7.org/fhir/5.0/StructureDefinition/extension-SubscriptionTopic.derivedFrom") && extension.getValue() instanceof CanonicalType) {
							CanonicalType derivedFrom = (CanonicalType)extension.getValue();

							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"derived-or-self", derivedFrom.getValueAsString());
							resourcemetadataList.add(rMetadata);
						}

						// effective : date(period)
						else if (extension.getUrl().equals("http://hl7.org/fhir/5.0/StructureDefinition/extension-SubscriptionTopic.effectivePeriod") && extension.getValue() instanceof Period) {
							Period effective = (Period)extension.getValue();

							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"effective", utcDateUtil.formatDate(effective.getStart(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(effective.getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT), utcDateUtil.formatDate(effective.getStart(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), utcDateUtil.formatDate(effective.getEnd(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()), "PERIOD");
							resourcemetadataList.add(rMetadata);
						}

						// identifier : token
						else if (extension.getUrl().equals("http://hl7.org/fhir/5.0/StructureDefinition/extension-SubscriptionTopic.identifier") && extension.getValue() instanceof Identifier) {
							Identifier identifier = (Identifier)extension.getValue();

							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", identifier.getValue(), identifier.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(identifier));
							resourcemetadataList.add(rMetadata);
						}

						// status : token
						else if (extension.getUrl().equals("http://hl7.org/fhir/5.0/StructureDefinition/extension-SubscriptionTopic.status") && extension.getValue() instanceof CodeType) {
							CodeType status = (CodeType)extension.getValue();

							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"status", status.getCode(), status.getSystem());
							resourcemetadataList.add(rMetadata);
						}

						// title : string
						else if (extension.getUrl().equals("http://hl7.org/fhir/5.0/StructureDefinition/extension-SubscriptionTopic.title") && extension.getValue() instanceof StringType) {
							StringType title = (StringType)extension.getValue();

							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"title", title.getValueAsString());
							resourcemetadataList.add(rMetadata);
						}

						// version : token(string)
						else if (extension.getUrl().equals("http://hl7.org/fhir/5.0/StructureDefinition/extension-SubscriptionTopic.version") && extension.getValue() instanceof StringType) {
							StringType version = (StringType)extension.getValue();

							rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"version", version.getValueAsString());
							resourcemetadataList.add(rMetadata);
						}

						// (eventTrigger)event : token(codeableconcept)
						// (eventTrigger)resource : uri
						else if (extension.getUrl().equals("http://hl7.org/fhir/5.0/StructureDefinition/extension-SubscriptionTopic.eventTrigger") && extension.hasExtension()) {

							for (Extension extension2 : extension.getExtension()) {

								// event : token(codeableconcept)
								if (extension2.getUrl().equals("event") && extension2.getValue() instanceof CodeableConcept) {
									CodeableConcept event = (CodeableConcept)extension2.getValue();

									if (event.hasCoding()) {
										for (Coding code : event.getCoding()) {
											rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"event", code.getCode(), code.getSystem(), null, ServicesUtil.INSTANCE.getTextValue(code));
											resourcemetadataList.add(rMetadata);
										}
									}
								}

								// resource : uri
								else if (extension2.getUrl().equals("resource") && extension2.getValue() instanceof UriType) {
									UriType eventResource = (UriType)extension2.getValue();

									rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"resource", eventResource.getValueAsString());
									resourcemetadataList.add(rMetadata);
								}
							}
						}

						// (resourceTrigger)resource : uri
						else if (extension.getUrl().equals("http://hl7.org/fhir/5.0/StructureDefinition/extension-SubscriptionTopic.resourceTrigger") && extension.hasExtension()) {

							for (Extension extension2 : extension.getExtension()) {

								// resource : uri
								if (extension2.getUrl().equals("resource") && extension2.getValue() instanceof UriType) {
									UriType eventResource = (UriType)extension2.getValue();

									rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"resource", eventResource.getValueAsString());
									resourcemetadataList.add(rMetadata);
								}
							}
						}

						// (canFilterBy)resource : uri
						else if (extension.getUrl().equals("http://hl7.org/fhir/5.0/StructureDefinition/extension-SubscriptionTopic.canFilterBy") && extension.hasExtension()) {

							for (Extension extension2 : extension.getExtension()) {

								// resource : uri
								if (extension2.getUrl().equals("resource") && extension2.getValue() instanceof UriType) {
									UriType eventResource = (UriType)extension2.getValue();

									rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"resource", eventResource.getValueAsString());
									resourcemetadataList.add(rMetadata);
								}
							}
						}

						// (notificationShape)resource : uri
						else if (extension.getUrl().equals("http://hl7.org/fhir/5.0/StructureDefinition/extension-SubscriptionTopic.notificationShape") && extension.hasExtension()) {

							for (Extension extension2 : extension.getExtension()) {

								// resource : uri
								if (extension2.getUrl().equals("resource") && extension2.getValue() instanceof UriType) {
									UriType eventResource = (UriType)extension2.getValue();

									rMetadata = generateResourcemetadata(resource, chainedResource, chainedParameter+"resource", eventResource.getValueAsString());
									resourcemetadataList.add(rMetadata);
								}
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
            if (iSubscriptionTopic != null) {
                try {
                	iSubscriptionTopic.close();
                } catch (IOException ioe) {
                	ioe.printStackTrace();
                }
            }
		}

		return resourcemetadataList;
	}

}
