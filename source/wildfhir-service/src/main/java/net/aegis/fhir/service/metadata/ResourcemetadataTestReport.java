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
import org.hl7.fhir.r4.model.TestReport;
import org.hl7.fhir.r4.model.TestReport.TestReportParticipantComponent;

/**
 * @author richard.ettema
 *
 */
public class ResourcemetadataTestReport extends ResourcemetadataProxy {

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
        ByteArrayInputStream iTestReport = null;

		try {
            // Extract and convert the resource contents to a TestReport object
			if (chainedResource != null) {
				iTestReport = new ByteArrayInputStream(chainedResource.getResourceContents());
			}
			else {
				iTestReport = new ByteArrayInputStream(resource.getResourceContents());
			}
			XmlParser xmlP = new XmlParser();
            TestReport testReport = (TestReport) xmlP.parse(iTestReport);
            iTestReport.close();

			/*
             * Create new Resourcemetadata objects for each TestReport metadata value and add to the resourcemetadataList
			 */

			// Add any passed in tags
			List<Resourcemetadata> tagMetadataList = this.generateResourcemetadataTagList(resource, testReport, chainedParameter);
			resourcemetadataList.addAll(tagMetadataList);

			// _id : token
			Resourcemetadata _id = generateResourcemetadata(resource, chainedResource, chainedParameter+"_id", testReport.getId());
			resourcemetadataList.add(_id);

			// _language : token
			if (testReport.getLanguage() != null) {
				Resourcemetadata _language = generateResourcemetadata(resource, chainedResource, chainedParameter+"_language", testReport.getLanguage());
				resourcemetadataList.add(_language);
			}

			// _lastUpdated : date
			if (testReport.getMeta() != null && testReport.getMeta().getLastUpdated() != null) {
				Resourcemetadata _lastUpdated = generateResourcemetadata(resource, chainedResource, chainedParameter+"_lastUpdated", utcDateUtil.formatDate(testReport.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(testReport.getMeta().getLastUpdated(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(_lastUpdated);
			}

			// identifier : token
			if (testReport.hasIdentifier()) {
				Resourcemetadata rIdentifier = generateResourcemetadata(resource, chainedResource, chainedParameter+"identifier", testReport.getIdentifier().getValue(), testReport.getIdentifier().getSystem(), null, ServicesUtil.INSTANCE.getTextValue(testReport.getIdentifier()));
				resourcemetadataList.add(rIdentifier);
			}

			// issued : date
			if (testReport.hasIssued()) {
				Resourcemetadata rIssued = generateResourcemetadata(resource, chainedResource, chainedParameter+"issued", utcDateUtil.formatDate(testReport.getIssued(), UTCDateUtil.DATETIME_SORT_FORMAT), null, utcDateUtil.formatDate(testReport.getIssued(), UTCDateUtil.DATETIME_SORT_FORMAT, TimeZone.getDefault()));
				resourcemetadataList.add(rIssued);
			}

			// participant : reference
			if (testReport.hasParticipant()) {

				for (TestReportParticipantComponent participant : testReport.getParticipant()) {

					if (participant.hasUri()) {
						Resourcemetadata rParticipant = generateResourcemetadata(resource, chainedResource, chainedParameter+"participant", participant.getUri());
						resourcemetadataList.add(rParticipant);
					}
				}
			}

			// result : token
			if (testReport.hasResult() && testReport.getResult() != null) {
				Resourcemetadata rResult = generateResourcemetadata(resource, chainedResource, chainedParameter+"result", testReport.getResult().toCode(), testReport.getResult().getSystem());
				resourcemetadataList.add(rResult);
			}

			// tester : string
			if (testReport.hasTester()) {
				Resourcemetadata rTester = generateResourcemetadata(resource, chainedResource, chainedParameter+"tester", testReport.getTester());
				resourcemetadataList.add(rTester);
			}

			// testscript : reference
			if (testReport.hasTestScript() && testReport.getTestScript().hasReference()) {
				Resourcemetadata rTestScript = generateResourcemetadata(resource, chainedResource, chainedParameter+"testscript", generateFullLocalReference(testReport.getTestScript().getReference(), baseUrl));
				resourcemetadataList.add(rTestScript);

				if (chainedResource == null) {
					// Add chained parameters for any
					List<Resourcemetadata> rTestScriptChain = this.generateChainedResourcemetadataAny(resource, baseUrl, resourceService, "testscript", 0, testReport.getTestScript().getReference());
					resourcemetadataList.addAll(rTestScriptChain);
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
