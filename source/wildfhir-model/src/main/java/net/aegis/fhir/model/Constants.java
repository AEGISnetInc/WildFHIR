/*
 * #%L
 * WildFHIR - wildfhir-model
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
package net.aegis.fhir.model;

/**
 * <p>
 * Constants used by the AEGIS FHIR(r) client application.
 * </p>
 *
 * @author richard.ettema
 *
 */
public class Constants {

	public static final String READ = "read";
	public static final String VREAD = "vread";
	public static final String DEFAULT_ROOT_URL = "http://localhost:8080/fhir";
	public static final String CHARSET_UTF8_EXT = "; charset=utf-8";
	public static final String GRAPHQL_CONTENT = "application/graphql";
	public static final String JSON_CONTENT = "application/json";
	public static final String XML_CONTENT = "application/xml";
	public static final String FHIR_JSON_CONTENT = "application/fhir+json";
	public static final String FHIR_XML_CONTENT = "application/fhir+xml";
	public static final String FHIR_PATCH_JSON_CONTENT = "application/fhir+json";
	public static final String FHIR_PATCH_XML_CONTENT = "application/fhir+xml";
	public static final String JSON_PATCH_CONTENT = "application/json-patch+json";
	public static final String XML_PATCH_CONTENT = "application/xml-patch+xml";

	public static final String ID = "_id";
	public static final String ACTIVE = "active";
	public static final String ADDRESS = "address";
	public static final String NAME_GIVEN = "given";
	public static final String NAME = "name";
	public static final String EMPTY_STRING = "";

}
