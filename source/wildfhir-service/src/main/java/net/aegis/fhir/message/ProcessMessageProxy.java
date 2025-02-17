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
public abstract class ProcessMessageProxy {

	/**
	 * Process the message based on the MessageHeader.eventCoding.code
	 *
	 * @param context
	 * @param resourceService
	 * @param resourcemetadataService
	 * @param codeService
	 * @param requestBundle
	 * @param async
	 * @param responseUrl
	 * @return <code>Bundle</code>
	 * @throws Exception
	 */
	public abstract Bundle processMessage(UriInfo context, ResourceService resourceService, ResourcemetadataService resourcemetadataService, CodeService codeService, Bundle requestBundle, BooleanType async, UrlType responseUrl) throws Exception;

	/**
	 * Process the message bundle to persist (save) the message bundle resource as-is and all
	 * message bundle entry.resource instances. All entry.resource instances are expected to
	 * have existing resource.id values so, treat the message bundle as a batch bundle where
	 * all entry.resource instances have a conditional update request.
	 *
	 * The returned bundle is a batch response of all updated (created) resources including
	 * the original message bundle.
	 *
	 * @param resourceService
	 * @param codeService
	 * @param requestBundle
	 * @return <code>Bundle</code>
	 * @throws Exception
	 */
	public Bundle persistMessageBundle(ResourceService resourceService, CodeService codeService, Bundle requestBundle) throws Exception {

		Bundle returnBundle = null;

		return returnBundle;
	}

}
