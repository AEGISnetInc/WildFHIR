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
package net.aegis.fhir.message;

import java.util.logging.Logger;

import javax.ws.rs.core.UriInfo;

import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.UrlType;

import net.aegis.fhir.service.CodeService;
import net.aegis.fhir.service.ResourceService;
import net.aegis.fhir.service.ResourcemetadataService;

/**
 * @author richard.ettema
 *
 */
public class ProcessMessageDefault extends ProcessMessageProxy {

	private Logger log = Logger.getLogger("ProcessMessageDefault");

	/* (non-Javadoc)
	 * @see net.aegis.fhir.message.ProcessMessageProxy#processMessage(javax.ws.rs.core.UriInfo, net.aegis.fhir.service.ResourceService, net.aegis.fhir.service.ResourcemetadataService, net.aegis.fhir.service.CodeService, org.hl7.fhir.r4.model.Bundle, org.hl7.fhir.r4.model.BooleanType, org.hl7.fhir.r4.model.UrlType)
	 */
	@Override
	public Bundle processMessage(UriInfo context, ResourceService resourceService, ResourcemetadataService resourcemetadataService, CodeService codeService, Bundle requestBundle, BooleanType async, UrlType responseUrl) throws Exception {

        log.fine("[START] ProcessMessageDefault.processMessage()");

		// Return null Bundle for simple 200 OK ack response
		return null;
	}

}
