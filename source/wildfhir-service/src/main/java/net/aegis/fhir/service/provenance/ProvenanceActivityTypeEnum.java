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
package net.aegis.fhir.service.provenance;

/**
 * @author Venkat.Keesara
 *
 */
public enum ProvenanceActivityTypeEnum {

	/**
	 * Display: <b>Create</b><br/>
	 * Code Value: <b>C</b>
	 *
	 * Create a new database object, such as Placing an Order.
	 */
	CREATE("CREATE", "http://hl7.org/fhir/v3/DataOperation", "Create"),

	/**
	 * Display: <b>Read/View/Print</b><br/>
	 * Code Value: <b>R</b>
	 *
	 * Display or print data, such as a Doctor Census.
	 */
	OPERATE("OPERATE", "http://hl7.org/fhir/v3/DataOperation", "Read/View/Print"),

	/**
	 * Display: <b>Update</b><br/>
	 * Code Value: <b>U</b>
	 *
	 * Update data, such as Revise Patient Information.
	 */
	UPDATE("UPDATE", "http://hl7.org/fhir/v3/DataOperation", "Update"),

	/**
	 * Display: <b>Delete</b><br/>
	 * Code Value: <b>D</b>
	 *
	 * Delete items, such as a doctor master file record.
	 */
	DELETE("DELETE", "http://hl7.org/fhir/v3/DataOperation", "Delete"),

	/**
	 * Display: <b>Execute</b><br/>
	 * Code Value: <b>E</b>
	 *
	 * Perform a system or application function such as log-on, program execution or use of an object's method, or
	 * perform a query/search operation.
	 */
	EXECUTE("EXECUTE", "http://hl7.org/fhir/v3/DataOperation", "Execute"),

	;

	private final String code;
	private final String system;
	private final String display;

	/**
	 * Constructor
	 */
	ProvenanceActivityTypeEnum(String theCode, String theSystem, String theDisplay) {
		code = theCode;
		system = theSystem;
		display = theDisplay;
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

}
