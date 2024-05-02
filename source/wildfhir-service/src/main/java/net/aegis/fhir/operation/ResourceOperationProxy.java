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
package net.aegis.fhir.operation;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;

import net.aegis.fhir.service.BatchService;
import net.aegis.fhir.service.CodeService;
import net.aegis.fhir.service.ConformanceService;
import net.aegis.fhir.service.ResourceService;
import net.aegis.fhir.service.ResourcemetadataService;
import net.aegis.fhir.service.TransactionService;

import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Resource;

/**
 * @author richard.ettema
 *
 */
public abstract class ResourceOperationProxy {

	/**
	 * Execute the operation
	 *
	 * @param context
	 * @param headers
	 * @param resourceService
	 * @param resourcemetadataService
	 * @param batchService
	 * @param transactionService
	 * @param codeService
	 * @param conformanceService
	 * @param softwareVersion
	 * @param resourceType
	 * @param resourceId
	 * @param inputParameters
	 * @param inputResource
	 * @param inputString
	 * @param contentType
	 * @param isPost
	 * @param returnedDirective
	 * @return <code>Parameters</code>
	 * @throws Exception
	 */
	public abstract Parameters executeOperation(UriInfo context, HttpHeaders headers, ResourceService resourceService, ResourcemetadataService resourcemetadataService, BatchService batchService, TransactionService transactionService, CodeService codeService, ConformanceService conformanceService, String softwareVersion, String resourceType, String resourceId, Parameters inputParameters, Resource inputResource, String inputString, String contentType, boolean isPost, StringBuffer returnedDirective) throws Exception;

}
