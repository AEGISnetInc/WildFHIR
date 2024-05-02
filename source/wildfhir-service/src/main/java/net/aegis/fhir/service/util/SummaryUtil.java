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
package net.aegis.fhir.service.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.logging.Logger;

import org.hl7.fhir.r4.formats.XmlParser;
import org.hl7.fhir.r4.formats.IParser.OutputStyle;
import org.hl7.fhir.r4.model.DomainResource;

import net.aegis.fhir.model.Resource;
import net.aegis.fhir.service.summary.ResourceSummaryProxy;
import net.aegis.fhir.service.summary.ResourceSummaryProxyObjectFactory;

/**
 * @author richard.ettema
 *
 */
public enum SummaryUtil {

	INSTANCE;

	private Logger log = Logger.getLogger("SummaryUtil");

	private SummaryUtil() {
	}

	/**
	 *
	 * @param resource
	 * @param _summary
	 * @return summarized resource
	 * @throws Exception
	 */
	public void generateResourceSummary(Resource resource, String _summary) throws Exception {

		log.fine("[START] SummaryUtil.generateResourceSummary()");

		try {
			// Convert XML contents to Resource
			XmlParser xmlP = new XmlParser();
			ByteArrayInputStream iResource = new ByteArrayInputStream(resource.getResourceContents());
			org.hl7.fhir.r4.model.Resource originalResource = xmlP.parse(iResource);

			org.hl7.fhir.r4.model.Resource summaryResource = null;

			// Only valid for DomainResource types
			if (originalResource instanceof DomainResource) {

				ResourceSummaryProxyObjectFactory objectFactory = new ResourceSummaryProxyObjectFactory();
				ResourceSummaryProxy proxy = objectFactory.getResourceSummaryProxy(resource.getResourceType());

				// Check for valid proxy
				if (proxy != null) {

					log.info("Generating _summary=" + _summary + " for resource type " + resource.getResourceType());

					// Convert resource based on _summary type
					if (_summary.equals("true")) {
						// Strip text and all non-summary data elements; mark as SUBSETTED
						summaryResource = proxy.generateSummary(originalResource);
					}
					else if (_summary.equals("false")) {
						// no-op
						summaryResource = originalResource;
					}
					else if (_summary.equals("data")) {
						// Strip text narrative; mark as SUBSETTED
						summaryResource = proxy.generateDataSummary(originalResource);
					}
					else if (_summary.equals("text")) {
						// Strip all data elements; mark as SUBSETTED
						summaryResource = proxy.generateTextSummary(originalResource);
					}

					if (summaryResource != null) {
						// Compose FHIR resource back to byte array
						ByteArrayOutputStream oResource = new ByteArrayOutputStream();
						xmlP.setOutputStyle(OutputStyle.PRETTY);
						xmlP.compose(oResource, summaryResource, true);

						resource.setResourceContents(oResource.toByteArray());
					}
				}
			}
		}
		catch (Exception e) {
			// Handle generic exceptions
			log.severe(e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}

}
