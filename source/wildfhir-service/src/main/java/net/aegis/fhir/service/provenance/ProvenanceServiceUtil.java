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
package net.aegis.fhir.service.provenance;

import java.io.ByteArrayOutputStream;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;

import org.hl7.fhir.r4.formats.IParser.OutputStyle;
import org.hl7.fhir.r4.formats.XmlParser;

import net.aegis.fhir.model.Resource;

/**
 * @author Venkat.Keesara
 *
 */
public enum ProvenanceServiceUtil {
	INSTANCE;
	private ProvenanceServiceUtil() {
	}

	public Resource generateProvenance(UriInfo context, HttpHeaders headers, String payload, String resourceType, String locationPath, String resourceId, String operation) throws Exception {
		net.aegis.fhir.model.Resource resourceDomain = new net.aegis.fhir.model.Resource();
		ProvenanceResourceProxyObjectFactory objectFactory = new ProvenanceResourceProxyObjectFactory();
		ProvenanceResourceProxy proxy = objectFactory.getProvenanceResourceProxy(resourceType, operation);
		org.hl7.fhir.r4.model.Resource fhirModel = proxy.generateProvenance(context, headers, payload, resourceType, locationPath, resourceId, operation);

		if (fhirModel != null) {
			XmlParser xmlP = new XmlParser();
			// Compose FHIR resource back to byte array
			ByteArrayOutputStream oResource = new ByteArrayOutputStream();
			xmlP.setOutputStyle(OutputStyle.PRETTY);
			xmlP.compose(oResource, fhirModel, true);
			resourceDomain.setResourceContents(oResource.toByteArray());
			resourceDomain.setResourceType("Provenance");
		}
		return resourceDomain;
	}

}
