/*
 * #%L
 * WildFHIR - wildfhir-service
 * %%
 * Copyright (C) 2025 AEGIS.net, Inc.
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
 * @author richard.ettema
 *
 */
public enum SignatureTypeEnum {

	AUTHOR("1.2.840.10065.1.12.1.1", "urn:iso-astm:E1762-95:2013", "Author's Signature"),
	COAUTHOR("1.2.840.10065.1.12.1.2", "urn:iso-astm:E1762-95:2013", "Coauthor's Signature"),
	COPARTICIPANT("1.2.840.10065.1.12.1.3", "urn:iso-astm:E1762-95:2013", "Co-participant's Signature"),
	TRANSCRIPTIONIST("1.2.840.10065.1.12.1.4", "urn:iso-astm:E1762-95:2013", "Transcriptionist/Recorder"),
	VERIFICATION("1.2.840.10065.1.12.1.5", "urn:iso-astm:E1762-95:2013", "Verification Signature"),
	VALIDATION("1.2.840.10065.1.12.1.6", "urn:iso-astm:E1762-95:2013", "Validation Signature"),
	CONSENT("1.2.840.10065.1.12.1.7", "urn:iso-astm:E1762-95:2013", "Consent Signature"),
	SIGNATURE_WITNESS("1.2.840.10065.1.12.1.8", "urn:iso-astm:E1762-95:2013", "Signature Witness Signature"),
	EVENT_WITNESS("1.2.840.10065.1.12.1.9", "urn:iso-astm:E1762-95:2013", "Event Witness Signaturethe"),
	IDENTITY_WITNESS("1.2.840.10065.1.12.1.10", "urn:iso-astm:E1762-95:2013", "Identity Witness Signature"),
	CONSENT_WITNESS("1.2.840.10065.1.12.1.11", "urn:iso-astm:E1762-95:2013", "Consent Witness Signature"),
	INTERPRETER("1.2.840.10065.1.12.1.12", "urn:iso-astm:E1762-95:2013", "Interpreter Signature"),
	REVIEW("1.2.840.10065.1.12.1.13", "urn:iso-astm:E1762-95:2013", "Review Signature"),
	SOURCE("1.2.840.10065.1.12.1.14", "urn:iso-astm:E1762-95:2013", "Source Signature"),
	ADDENDUM("1.2.840.10065.1.12.1.15", "urn:iso-astm:E1762-95:2013", "Addendum Signature"),
	MODIFICATION("1.2.840.10065.1.12.1.16", "urn:iso-astm:E1762-95:2013", "Modification Signature"),
	ADMINISTRATIVE("1.2.840.10065.1.12.1.17", "urn:iso-astm:E1762-95:2013", "Administrative (Error/Edit) Signature"),
	TIMESTAMP("1.2.840.10065.1.12.1.18", "urn:iso-astm:E1762-95:2013", "Timestamp Signature");

	private final String code;
	private final String system;
	private final String display;

	/**
	 * Constructor
	 */
	SignatureTypeEnum(String theCode, String theSystem, String theDisplay) {
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
