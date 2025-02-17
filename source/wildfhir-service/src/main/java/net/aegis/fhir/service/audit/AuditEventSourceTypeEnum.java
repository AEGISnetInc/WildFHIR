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
public enum AuditEventSourceTypeEnum {

	/**
	 * Display: User Device
	 *
	 * Code Value: 1
	 *
	 * End-user display device, diagnostic device
	 */
	USER_DEVICE("1", "http://hl7.org/fhir/security-source-type", "End-user display device, diagnostic device."),

	/**
	 * Display: Data Interface
	 *
	 * Code Value: 2
	 *
	 * Data acquisition device or instrument
	 */
	DATA_INTERFACE("2", "http://hl7.org/fhir/security-source-type", "Data acquisition device or instrument."),

	/**
	 * Display: Web Server
	 *
	 * Code Value: 3
	 *
	 * Web Server process or thread
	 */
	WEB_SERVER("3", "http://hl7.org/fhir/security-source-type", "Web Server process or thread."),

	/**
	 * Display: Application Server
	 *
	 * Code Value: 4
	 *
	 * Application Server process or thread
	 */
	APPLICATION_SERVER("4", "http://hl7.org/fhir/security-source-type", "Application Server process or thread."),

	/**
	 * Display: Database Server
	 *
	 * Code Value: 5
	 *
	 * Database Server process or thread
	 */
	DATABASE_SERVER("5", "http://hl7.org/fhir/security-source-type", "Database Server process or thread."),

	/**
	 * Display: Security Server
	 *
	 * Code Value: 6
	 *
	 * Security server, e.g., a domain controller
	 */
	SECURITY_SERVER("6", "http://hl7.org/fhir/security-source-type", "Security server, e.g. a domain controller."),

	/**
	 * Display: Network Device
	 *
	 * Code Value: 7
	 *
	 * ISO level 1-3 network component
	 */
	NETWORK_DEVICE("7", "http://hl7.org/fhir/security-source-type", "ISO level 1-3 network component."),

	/**
	 * Display: Network Router
	 *
	 * Code Value: 8
	 *
	 * ISO level 4-6 operating software
	 */
	NETWORK_ROUTER("8", "http://hl7.org/fhir/security-source-type", "ISO level 4-6 operating software."),

	/**
	 * Display: Other
	 *
	 * Code Value: 9
	 *
	 * other kind of device (defined by DICOM, but some other code/system can be used)
	 */
	OTHER("9", "http://hl7.org/fhir/security-source-type", "other kind of device (defined by DICOM, but some other code/system can be used)"),

	;

	/**
	 * Identifier for this Value Set: http://hl7.org/fhir/vs/security-source-type
	 */
	public static final String VALUESET_IDENTIFIER = "http://hl7.org/fhir/vs/security-source-type";

	/**
	 * Name for this Value Set: Security Event Source Type
	 */
	public static final String VALUESET_NAME = "Security Event Source Type";

	private static Map<String, AuditEventSourceTypeEnum> CODE_TO_ENUM = new HashMap<String, AuditEventSourceTypeEnum>();
	private static Map<String, HashMap<String, AuditEventSourceTypeEnum>> SYSTEM_TO_CODE_TO_ENUM = new HashMap<String, HashMap<String, AuditEventSourceTypeEnum>>();

	private final String code;
	private final String system;
	private final String display;

	static {
		for (AuditEventSourceTypeEnum next : AuditEventSourceTypeEnum.values()) {
			CODE_TO_ENUM.put(next.getCode(), next);

			if (!SYSTEM_TO_CODE_TO_ENUM.containsKey(next.getSystem())) {
				SYSTEM_TO_CODE_TO_ENUM.put(next.getSystem(), new HashMap<String, AuditEventSourceTypeEnum>());
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
	public AuditEventSourceTypeEnum forCode(String theCode) {
		AuditEventSourceTypeEnum retVal = CODE_TO_ENUM.get(theCode);
		return retVal;
	}

	/**
	 * Converts codes to their respective enumerated values
	 */
//	public static final IValueSetEnumBinder VALUESET_BINDER = new IValueSetEnumBinder() {
//
//		@Override
//		public String toCodeString(SecurityEventSourceTypeEnum theEnum) {
//			return theEnum.getCode();
//		}
//
//		@Override
//		public String toSystemString(SecurityEventSourceTypeEnum theEnum) {
//			return theEnum.getSystem();
//		}
//
//		@Override
//		public SecurityEventSourceTypeEnum fromCodeString(String theCodeString) {
//			return CODE_TO_ENUM.get(theCodeString);
//		}
//
//		@Override
//		public SecurityEventSourceTypeEnum fromCodeString(String theCodeString, String theSystemString) {
//			Map map = SYSTEM_TO_CODE_TO_ENUM.get(theSystemString);
//			if (map == null) {
//				return null;
//			}
//			return map.get(theCodeString);
//		}
//
//	};

	/**
	 * Constructor
	 */
	AuditEventSourceTypeEnum(String theCode, String theSystem, String theDisplay) {
		code = theCode;
		system = theSystem;
		display = theDisplay;
	}

}
