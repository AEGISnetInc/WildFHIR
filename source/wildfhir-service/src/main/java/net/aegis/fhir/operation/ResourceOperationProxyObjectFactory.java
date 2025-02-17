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

import org.hl7.fhir.r4.model.ResourceType;

/**
 * @author richard.ettema
 *
 */
public class ResourceOperationProxyObjectFactory {

	/**
     * Return an instance of the ResourceOperationProxy class.
     *
     * @return An instance of the ResourceOperationProxy class.
     */
	public ResourceOperationProxy getResourceOperationProxy(String resourceType, String operationName) {

		ResourceOperationProxy proxy = null;

		if (operationName.equals("capability-reload")) {
			proxy = new CapabilityStatementReload();
		}
		else if (operationName.equals("code-configuration")) {
			proxy = new CodeConfiguration();
		}
		else if (operationName.equals("convert")) {
			proxy = new ResourceConvertFormat();
		}
		else if (operationName.equals("load-examples")) {
			proxy = new ResourceLoadExamples();
		}
		else if (operationName.equals("meta")) {
			proxy = new ResourceMeta();
		}
		else if (operationName.equals("meta-add")) {
			proxy = new ResourceMetaAdd();
		}
		else if (operationName.equals("meta-delete")) {
			proxy = new ResourceMetaDelete();
		}
		else if (operationName.equals("process-message")) {
			proxy = new GlobalProcessMessage();
		}
		else if (operationName.equals("purge-all")) {
			proxy = new ResourcePurgeAll();
		}
		else if (operationName.equals("validate")) {
			proxy = new ResourceValidation();
		}
		else if (operationName.equals("versions")) {
			proxy = new GlobalVersions();
		}
		else if (resourceType.equals(ResourceType.Composition.name())) {
			if (operationName.equals("document")) {
				proxy = new CompositionDocument();
			}
		}
		else if (resourceType.equals(ResourceType.Observation.name())) {
			if (operationName.equals("lastn")) {
				proxy = new ObservationLastNOperation();
			}
		}
		else if (resourceType.equals(ResourceType.Patient.name())) {
			if (operationName.equals("everything")) {
				proxy = new PatientEverything();
			}
			else if (operationName.equals("match")) {
				proxy = new PatientMatch();
			}
			else if (operationName.equals("purge")) {
				proxy = new PatientPurge();
			}
		}

		return proxy;

	}

}
