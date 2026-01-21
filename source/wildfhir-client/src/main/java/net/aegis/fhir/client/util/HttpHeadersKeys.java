/*
 * #%L
 * WildFHIR - wildfhir-client
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
package net.aegis.fhir.client.util;

/**
 * <p>
 * Enum of HTTP header keys that contain useful information when a FHIR REST call is made.
 * </p>
 *
 * @author Richard Ettema
 *
 */
public enum HttpHeadersKeys {

	Content_Type,
	Category,
	ETag,
	Last_Modified,
	Content_Length,
	Server,
	Todays_Date,
	Content_Location,
	Location;

	@Override
	public String toString() {
		switch (this) {
		case Content_Type:
			return "Content-Type";
		case Category:
			return "Category";
		case ETag:
			return "ETag";
		case Last_Modified:
			return "Last-Modified";
		case Content_Length:
			return "Content-Length";
		case Server:
			return "Server";
		case Todays_Date:
			return "Date";
		case Content_Location:
			return "Content-Location";
		case Location:
			return "Location";
		default:
			return "?";
		}
	}

	public static HttpHeadersKeys stringToKey(String str) throws Exception {

		if (str == null || "".equals(str))
			return null;
		if ("Content-Type".equals(str))
			return Content_Type;
		if ("Category".equals(str))
			return Category;
		if ("ETag".equals(str))
			return ETag;
		if ("Last-Modified".equals(str))
			return Last_Modified;
		if ("Content-Length".equals(str))
			return Content_Length;
		if ("Server".equals(str))
			return Server;
		if ("Date".equals(str))
			return Todays_Date;
		if ("Content-Location".equals(str))
			return Content_Location;
		if ("Location".equals(str))
			return Location;

		throw new Exception("Unknown HTTP Header Key '" + str + "'");
	}

}
