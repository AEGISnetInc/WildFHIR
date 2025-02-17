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

/**
 * @author Venkat.Keesara
 *
 */
public enum AuditEventOutcomeEnum {

	SUCCESS("0", "http://hl7.org/fhir/audit-event-outcome", "Success"),

	MINOR_FAILURE("4", "http://hl7.org/fhir/audit-event-outcome", "Minor failure"),

	SERIOUS_FAILURE("8", "http://hl7.org/fhir/audit-event-outcome", "Serious failure"),

	MAJOR_FAILURE("12", "http://hl7.org/fhir/audit-event-outcome", "Major failure");

	private final String code;
	private final String system;
	private final String display;

	/**
	 * @param code
	 * @param system
	 * @param display
	 */
	private AuditEventOutcomeEnum(String code, String system, String display) {
		this.code = code;
		this.system = system;
		this.display = display;
	}

	public String getCode() {
		return code;
	}

	public String getSystem() {
		return system;
	}

	public String getDisplay() {
		return display;
	}

}
