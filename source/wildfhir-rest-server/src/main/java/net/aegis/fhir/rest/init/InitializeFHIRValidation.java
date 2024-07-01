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
package net.aegis.fhir.rest.init;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.aegis.fhir.service.validation.FHIRValidatorClient;

/**
 * @author richard.ettema
 *
 */
public class InitializeFHIRValidation extends HttpServlet {

	private static final long serialVersionUID = 1048087438631885825L;

	private static final Logger log = LoggerFactory.getLogger(InitializeFHIRValidation.class);

	private String PatientContents = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<Patient xmlns=\"http://hl7.org/fhir\">\n" +
			"  <id value=\"pat1\"/>\n" +
			"  <text>\n" +
			"    <status value=\"generated\"/>\n" +
			"    <div xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
			"      <p>Patient Donald DUCK @ Acme Healthcare, Inc. MR = 654321</p>\n" +
			"    </div>\n" +
			"  </text>\n" +
			"  <identifier>\n" +
			"    <use value=\"usual\"/>\n" +
			"    <type>\n" +
			"      <coding>\n" +
			"        <system value=\"http://hl7.org/fhir/v2/0203\"/>\n" +
			"        <code value=\"MR\"/>\n" +
			"      </coding>\n" +
			"    </type>\n" +
			"    <system value=\"urn:oid:0.1.2.3.4.5.6.7\"/>\n" +
			"    <value value=\"654321\"/>\n" +
			"  </identifier>\n" +
			"  <active value=\"true\"/>\n" +
			"  <name>\n" +
			"    <use value=\"official\"/>\n" +
			"    <family value=\"Donald\"/>\n" +
			"    <given value=\"Duck\"/>\n" +
			"  </name>\n" +
			"  <gender value=\"male\"/>\n" +
			"  <contact>\n" +
			"    <relationship>\n" +
			"      <coding>\n" +
			"        <system value=\"http://hl7.org/fhir/patient-contact-relationship\"/>\n" +
			"        <code value=\"owner\"/>\n" +
			"      </coding>\n" +
			"    </relationship>\n" +
			"    <organization>\n" +
			"      <reference value=\"Organization/1\"/>\n" +
			"      <display value=\"Walt Disney Corporation\"/>\n" +
			"    </organization>\n" +
			"  </contact>\n" +
			"  <managingOrganization>\n" +
			"    <reference value=\"Organization/1\"/>\n" +
			"    <display value=\"ACME Healthcare, Inc\"/>\n" +
			"  </managingOrganization>\n" +
			"  <link>\n" +
			"    <other>\n" +
			"      <reference value=\"Patient/pat2\"/>\n" +
			"    </other>\n" +
			"    <type value=\"seealso\"/>\n" +
			"  </link>\n" +
			"</Patient>";

	public void init() throws ServletException {
		log.info("InitializeFHIRValidation.init() - START");

		initValidation();

		log.info("InitializeFHIRValidation.init() - END");
	}

	public void initValidation() {

		try {
			FHIRValidatorClient.instance().validateResource("Patient", PatientContents.getBytes(), null);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
