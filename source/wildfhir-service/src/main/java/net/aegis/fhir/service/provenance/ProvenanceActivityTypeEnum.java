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
	 * Display: <b>create</b><br/>
	 * Code Value: <b>CREATE</b>
	 *
	 * Fundamental operation in an Information System (IS) that results only in the act of bringing an object into existence.
	 */
	CREATE("CREATE", "http://terminology.hl7.org/CodeSystem/v3-DataOperation", "create"),

	/**
	 * Display: <b>delete</b><br/>
	 * Code Value: <b>DELETE</b>
	 *
	 * Fundamental operation in an Information System (IS) that results only in the removal of information about an object from memory or storage.
	 */
	DELETE("DELETE", "http://terminology.hl7.org/CodeSystem/v3-DataOperation", "delete"),

	/**
	 * Display: <b>update</b><br/>
	 * Code Value: <b>UPDATE</b>
	 *
	 * Fundamental operation in an Information System (IS) that results only in the revision or alteration of an object.
	 */
	UPDATE("UPDATE", "http://terminology.hl7.org/CodeSystem/v3-DataOperation", "revise"),

	/**
	 * Display: <b>append</b><br/>
	 * Code Value: <b>APPEND</b>
	 *
	 * Fundamental operation in an Information System (IS) that results only in the addition of information to an object already in existence.
	 */
	APPEND("APPEND", "http://terminology.hl7.org/CodeSystem/v3-DataOperation", "append"),

	/**
	 * Display: <b>nullify</b><br/>
	 * Code Value: <b>NULLIFY</b>
	 *
	 * Change the status of an object representing an Act to "nullified", i.e., treat as though it never existed.
	 */
	NULLIFY("NULLIFY", "http://terminology.hl7.org/CodeSystem/v3-DataOperation", "nullify"),

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
