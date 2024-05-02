/*
 * #%L
 * WildFHIR - wildfhir-rest-server
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
package net.aegis.fhir.rest;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

/**
 * JAX-RS CORS Support Service
 * <p/>
 * This class produces the RESTful services that provides support for Cross-site Scripting support.
 *
 * @author richard.ettema
 *
 */
@Path("/")
@ApplicationScoped
public class CORSRESTService {

	@OPTIONS
	@Path("{path : .*}")
	public Response options() {
	    return Response.ok("")
	            .header("Access-Control-Allow-Origin", "*")
	            .header("Access-Control-Allow-Headers", "Accept, Accept-Charset, Authorization, Content-Type, If-Match, If-Modified-Since, If-None-Exist, If-None-Match, Origin, Prefer, X-Requested-With")
	            .header("Access-Control-Allow-Credentials", "true")
	            .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD, PATCH")
	            .header("Access-Control-Max-Age", "1209600")
	            .header("Accept-Patch", "application/fhir+xml, application/fhir+json, application/xml+fhir, application/json+fhir, application/xml, application/json, application/xml-patch+xml, application/json-patch+json, text/xml, text/json, application/x-www-form-urlencoded, application/octet-stream")
	            .build();
	}

}
