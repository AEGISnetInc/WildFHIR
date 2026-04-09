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
package net.aegis.fhir.service.audit;

import org.hl7.fhir.r4.model.AuditEvent;
import org.hl7.fhir.r4.model.AuditEvent.AuditEventAction;
import org.hl7.fhir.r4.model.AuditEvent.AuditEventEntityComponent;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.HttpHeaders;

/**
 * @author Venkat.Keesara
 *
 */
public class AuditEventCreateResource extends AuditEventResourceProxy {

	@Override
	public Resource generateAuditEvent(HttpServletRequest request, HttpHeaders headers, String payload, String resourceType, boolean response, String resourceId, Identifier identifier, String operation) throws Exception {

		AuditEvent audit = new AuditEvent();

		prepareBasicData(audit, request, headers, response);

		// subType is FHIR create
		Coding coding = new Coding();
		coding.setSystem("http://hl7.org/fhir/restful-interaction");
		coding.setCode("create");
		audit.addSubtype(coding);

		// entity based on resourceType and resourceId
		AuditEventEntityComponent entity = new AuditEventEntityComponent();

		Reference what = new Reference();
		what.setReference(resourceType + "/" + resourceId);
		if (identifier != null) {
			what.setIdentifier(identifier);
		}
		entity.setWhat(what);

		coding = new Coding();
		coding.setSystem("http://hl7.org/fhir/resource-types");
		coding.setCode(resourceType);
		entity.setType(coding);

		coding = new Coding();
		coding.setSystem("http://terminology.hl7.org/CodeSystem/object-role");
		coding.setCode("4");
		entity.setRole(coding);

		coding = new Coding();
		coding.setSystem("http://terminology.hl7.org/CodeSystem/dicom-audit-lifecycle");
		coding.setCode("1");
		coding.setDisplay("Origination / Creation");
		entity.setLifecycle(coding);

		audit.addEntity(entity);

		return audit;
	}

	@Override
	public AuditEventAction setAction() {
		return AuditEventAction.C;
	}

}
