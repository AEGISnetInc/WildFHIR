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

import java.util.HashMap;
import java.util.Map;

/**
 * @author Venkat.Keesara
 *
 */
public enum AuditEventActionEnum {

	/**
	 * Display: <b>Create</b><br/>
	 * Code Value: <b>C</b>
	 *
	 * Create a new database object, such as Placing an Order.
	 */
	CREATE("C", "http://hl7.org/fhir/security-event-action", "Create"),

	/**
	 * Display: <b>Read/View/Print</b><br/>
	 * Code Value: <b>R</b>
	 *
	 * Display or print data, such as a Doctor Census.
	 */
	READ_VIEW_PRINT("R", "http://hl7.org/fhir/security-event-action", "Read/View/Print"),

	/**
	 * Display: <b>Update</b><br/>
	 * Code Value: <b>U</b>
	 *
	 * Update data, such as Revise Patient Information.
	 */
	UPDATE("U", "http://hl7.org/fhir/security-event-action", "Update"),

	/**
	 * Display: <b>Delete</b><br/>
	 * Code Value: <b>D</b>
	 *
	 * Delete items, such as a doctor master file record.
	 */
	DELETE("D", "http://hl7.org/fhir/security-event-action", "Delete"),

	/**
	 * Display: <b>Execute</b><br/>
	 * Code Value: <b>E</b>
	 *
	 * Perform a system or application function such as log-on, program execution or use of an object's method, or
	 * perform a query/search operation.
	 */
	EXECUTE("E", "http://hl7.org/fhir/security-event-action", "Execute"),

	;

	/**
	 * Identifier for this Value Set: http://hl7.org/fhir/vs/security-event-action
	 */
	public static final String VALUESET_IDENTIFIER = "http://hl7.org/fhir/vs/security-event-action";

	/**
	 * Name for this Value Set: SecurityEventAction
	 */
	public static final String VALUESET_NAME = "SecurityEventAction";

	private static Map<String, AuditEventActionEnum> CODE_TO_ENUM = new HashMap<String, AuditEventActionEnum>();
	private static Map<String, Map<String, AuditEventActionEnum>> SYSTEM_TO_CODE_TO_ENUM = new HashMap<String, Map<String, AuditEventActionEnum>>();

	private final String code;
	private final String system;
	private final String display;

	static {
		for (AuditEventActionEnum next : AuditEventActionEnum.values()) {
			CODE_TO_ENUM.put(next.getCode(), next);

			if (!SYSTEM_TO_CODE_TO_ENUM.containsKey(next.getSystem())) {
				SYSTEM_TO_CODE_TO_ENUM.put(next.getSystem(), new HashMap<String, AuditEventActionEnum>());
			}
			SYSTEM_TO_CODE_TO_ENUM.get(next.getSystem()).put(next.getCode(), next);
		}
	}

	/**
	 * Returns the code associated with this enumerated value
	 */
	public String getCode() {
		return code;
	}

	/**
	 * Returns the code system associated with this enumerated value
	 */
	public String getSystem() {
		return system;
	}

	/**
	 * @return the display
	 */
	public String getDisplay() {
		return display;
	}

	/**
	 * Returns the enumerated value associated with this code
	 */
	public AuditEventActionEnum forCode(String theCode) {
		AuditEventActionEnum retVal = CODE_TO_ENUM.get(theCode);
		return retVal;
	}

	/**
	 * Converts codes to their respective enumerated values
	 *//*
		 * public static final IValueSetEnumBinder<SecurityEventActionEnum> VALUESET_BINDER = new
		 * IValueSetEnumBinder<SecurityEventActionEnum>() {
		 *
		 * @Override public String toCodeString(SecurityEventActionEnum theEnum) { return theEnum.getCode(); }
		 *
		 * @Override public String toSystemString(SecurityEventActionEnum theEnum) { return theEnum.getSystem(); }
		 *
		 * @Override public SecurityEventActionEnum fromCodeString(String theCodeString) { return
		 * CODE_TO_ENUM.get(theCodeString); }
		 *
		 * @Override public SecurityEventActionEnum fromCodeString(String theCodeString, String theSystemString) {
		 * Map<String, SecurityEventActionEnum> map = SYSTEM_TO_CODE_TO_ENUM.get(theSystemString); if (map == null) {
		 * return null; } return map.get(theCodeString); }
		 *
		 * };
		 */

	/**
	 * Constructor
	 */
	AuditEventActionEnum(String theCode, String theSystem, String theDisplay) {
		code = theCode;
		system = theSystem;
		display = theDisplay;
	}

}
