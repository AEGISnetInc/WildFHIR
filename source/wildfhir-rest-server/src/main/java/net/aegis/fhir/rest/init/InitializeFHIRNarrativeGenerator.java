/*
 * #%L
 * WildFHIR - wildfhir-rest-server
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
package net.aegis.fhir.rest.init;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.hl7.fhir.r4.formats.XmlParser;
import org.hl7.fhir.r4.model.DomainResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.aegis.fhir.service.narrative.FHIRNarrativeGeneratorClient;

/**
 * @author richard.ettema
 *
 */
public class InitializeFHIRNarrativeGenerator extends HttpServlet {

	private static final long serialVersionUID = 4739501506051998843L;

	private static final Logger log = LoggerFactory.getLogger(InitializeFHIRNarrativeGenerator.class);

	private String PatientContents = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<Patient xmlns=\"http://hl7.org/fhir\">\n"
			+ "  <id value=\"pat1\"/>\n"
			+ "  <name>\n"
			+ "    <use value=\"official\"/>\n"
			+ "    <family value=\"Donald\"/>\n"
			+ "    <given value=\"Duck\"/>\n"
			+ "  </name>\n"
			+ "  <gender value=\"male\"/>\n"
			+ "</Patient>";

	public void init() throws ServletException {
		log.info("InitializeFHIRNarrativeGenerator.init() - START");

		initNarrativeGenerator();

		log.info("InitializeFHIRNarrativeGenerator.init() - END");
	}

	public void initNarrativeGenerator() {

		try {
			// Convert XML contents to Resource
			XmlParser xmlP = new XmlParser();
			DomainResource resource = (DomainResource) xmlP.parse(PatientContents.getBytes());

			FHIRNarrativeGeneratorClient.instance().generate(resource);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}

}
