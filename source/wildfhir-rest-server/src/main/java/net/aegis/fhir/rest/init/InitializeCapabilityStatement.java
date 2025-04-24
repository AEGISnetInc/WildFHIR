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

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.hl7.fhir.r4.formats.XmlParser;
import org.hl7.fhir.r4.model.CapabilityStatement;
import org.hl7.fhir.r4.model.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.aegis.fhir.model.ResourceContainer;
import net.aegis.fhir.operation.ResourceOperationProxy;
import net.aegis.fhir.operation.ResourceOperationProxyObjectFactory;
import net.aegis.fhir.service.CodeService;
import net.aegis.fhir.service.ConformanceService;
import net.aegis.fhir.service.util.ServicesUtil;

/**
 * @author richard.ettema
 *
 */
public class InitializeCapabilityStatement extends HttpServlet {

	private static final long serialVersionUID = 4973142572981426904L;

	private static final Logger log = LoggerFactory.getLogger(InitializeCapabilityStatement.class);

	@Inject
    CodeService codeService;

	@Inject
	ConformanceService conformanceService;

	public void init() throws ServletException {
		log.info("InitializeCapabilityStatement.init() - START");

		initCapabilityStatement();

		log.info("InitializeCapabilityStatement.init() - END");
	}

	public void initCapabilityStatement() {

		try {
			ResourceContainer resourceContainer = conformanceService.read();

			String softwareVersion = ServicesUtil.INSTANCE.getSoftwareVersion();
			boolean versionsMatch = false;

			if (resourceContainer != null && resourceContainer.getConformance() != null && resourceContainer.getConformance().getResourceContents() != null) {
				// CapabilityStatement exists, check current version

				// Parse contents to CapabilityStatement object
				XmlParser xmlParser = new XmlParser();
				CapabilityStatement capstmt = (CapabilityStatement)xmlParser.parse(resourceContainer.getConformance().getResourceContents());

				String capstmtVersion = capstmt.getVersion();

				if (capstmtVersion.equals(softwareVersion)) {
					versionsMatch = true;
				}
			}

			if (!versionsMatch ||
					(resourceContainer != null && resourceContainer.getConformance() != null
					&& (resourceContainer.getConformance().getResourceContents() == null || resourceContainer.getConformance().getResourceContents().length == 0))) {
				/*
				 * Use Factory Pattern for execution of capability-reload operation
				 */
				ResourceOperationProxyObjectFactory operationFactory = new ResourceOperationProxyObjectFactory();

				ResourceOperationProxy operationProxy = operationFactory.getResourceOperationProxy(null, "capability-reload");

				if (operationProxy != null) {
					Parameters outputParameters = operationProxy.executeOperation(null, null, null, null, null, null, codeService, conformanceService, softwareVersion, null, null, null, null, null, null, false, null);

					if (outputParameters != null) {
						if (!outputParameters.hasParameter()) {
							// Something went wrong
							log.error("InitializeCapabilityStatement failure! capability-reload returned no parameters.");
						}
						else if (outputParameters.hasParameter() && outputParameters.getParameter().get(0).hasName() &&
								outputParameters.getParameter().get(0).getName().equals("result") &&
								outputParameters.getParameter().get(0).hasValue()) {

							log.info("InitializeCapabilityStatement complete. " + outputParameters.getParameter().get(0).getValue().primitiveValue());
						}
						else {
							log.warn("InitializeCapabilityStatement complete. No result message returned.");
						}
					}
					else {
						// Something went wrong
						log.error("InitializeCapabilityStatement failure! capability-reload no response returned.");
					}
				}
				else {
					// Something went wrong
					log.error("InitializeCapabilityStatement failure! capability-reload was not found.");
				}
			}
			else {
				// CapabilityStatement already generated
				log.info("InitializeCapabilityStatement skipped. CapabilityStatement exists.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
