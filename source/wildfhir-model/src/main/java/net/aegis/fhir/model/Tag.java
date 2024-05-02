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

import java.util.ArrayList;
import java.util.List;

/**
 * @author richard.ettema
 *
 */
public class Tag {

	public static String METADATA_NAME_GENERAL_TAG = "_tag";
	public static String METADATA_NAME_PROFILE_TAG = "_profile";
	public static String METADATA_NAME_SECURITY_TAG = "_security";

	private String term;
	private String scheme;
	private String label;

	/**
	 * Construct new Tag with only the term and scheme properties defined
	 *
	 * @param term
	 * @param scheme
	 */
	public Tag(String term, String scheme) {
		this.term = term;
		this.scheme = scheme;

		if (this.term == null || this.scheme == null) {
			throw new IllegalArgumentException("term and scheme cannot be null");
		}
	}

	/**
	 * Construct new Tag with all properties defined
	 *
	 * @param term
	 * @param scheme
	 * @param label
	 */
	public Tag(String term, String scheme, String label) {
		this.term = term;
		this.scheme = scheme;
		this.label = label;

		if (this.term == null || this.scheme == null) {
			throw new IllegalArgumentException("term and scheme cannot be null");
		}
	}

	public String getTerm() {
		return term;
	}

	public void setTerm(String term) {
		this.term = term;
	}

	public String getScheme() {
		return scheme;
	}

	public void setScheme(String scheme) {
		this.scheme = scheme;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	/*
	 * Helper Methods
	 */

	public String getMetadataName() {
		if (this.scheme != null) {
			if (this.scheme.contains("profile")) {
				return METADATA_NAME_PROFILE_TAG;
			}
			else if (this.scheme.contains("security")) {
				return METADATA_NAME_SECURITY_TAG;
			}
			else {
				return METADATA_NAME_GENERAL_TAG;
			}
		}
		else {
			return "";
		}
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("");

		if (this.term != null) {
			sb.append(this.term);
		}

		if (this.scheme != null) {
			if (sb.length() > 0) {
				sb.append("; ");
			}
			sb.append("scheme=\"").append(this.scheme).append("\"");
		}

		if (this.label != null) {
			if (sb.length() > 0) {
				sb.append("; ");
			}
			sb.append("label=\"").append(this.label).append("\"");
		}

		return sb.toString();
	}

	/**
	 * Return a new Tag instance with only the term and scheme properties defined
	 *
	 * @param term
	 * @param scheme
	 * @return <code>Tag</code>
	 */
	public static Tag getTag(String term, String scheme) {
		return getTag(term, scheme, null);
	}

	/**
	 * Return a new Tag instance with all properties defined
	 *
	 * @param term
	 * @param scheme
	 * @param label
	 * @return <code>Tag</code>
	 */
	public static Tag getTag(String term, String scheme, String label) {
		return new Tag(term, scheme, label);
	}

	/**
	 * Return the HL7 FHIR Tag formatted string representation
	 *
	 * @param term
	 * @param scheme
	 * @param label
	 * @return formatted String
	 */
	public static String format(String term, String scheme, String label) {
		StringBuffer sb = new StringBuffer("");

		if (term != null) {
			sb.append(term);
		}

		if (scheme != null) {
			if (sb.length() > 0) {
				sb.append("; ");
			}
			sb.append("scheme=\"").append(scheme).append("\"");
		}

		if (label != null) {
			if (sb.length() > 0) {
				sb.append("; ");
			}
			sb.append("label=\"").append(label).append("\"");
		}

		return sb.toString();
	}

	/**
	 * Return a new Tag instance with all properties parsed from an HL7 FHIR Tag formatted string
	 *
	 * The HL7 FHIR Tag formatted string is of the form
	 * {term}; scheme="{scheme}"; label="{label}"
	 * Example: http://hl7.org/fhir/v3/ActCode#DELAU; scheme="http://hl7.org/fhir/tag/security"; label="delete after use"
	 *
	 * @param formattedString
	 * @return <code>Tag</code>
	 */
	public static Tag parseTag(String formattedString) {
		String term = null;
		String scheme = null;
		String label = null;

		String[] tagStringArray = formattedString.split(";");

		for (String tagProperty : tagStringArray) {
			if (tagProperty != null && !tagProperty.isEmpty()) {
				tagProperty = tagProperty.trim();
				if (tagProperty.contains("scheme=\"")) {
					int start = tagProperty.indexOf("scheme=\"") + 8;
					scheme = tagProperty.substring(start, tagProperty.length() - 1);
				}
				else if (tagProperty.contains("label=\"")) {
					int start = tagProperty.indexOf("label=\"") + 7;
					label = tagProperty.substring(start, tagProperty.length() - 1);
				}
				else {
					term = tagProperty;
				}
			}
		}

		return new Tag(term, scheme, label);
	}

	/**
	 * Return a List of Tag instances with all properties parsed from an HL7 FHIR Tag formatted
	 * and comma delimited string
	 *
	 * The HL7 FHIR Tag formatted string is of the form
	 *  Category: [Tag Term]; scheme="[Tag Scheme]"; label="[Tag label]"(, ...)
	 * Example: http://hl7.org/fhir/v3/ActCode#DELAU; scheme="http://hl7.org/fhir/tag/security"; label="delete after use"
	 *
	 * @param formattedString
	 * @return <code>Tag</code>
	 */
	public static List<Tag> parseTagList(String formattedString) {
		List<Tag> tagList = new ArrayList<Tag>();

		if (formattedString != null && !formattedString.isEmpty()) {
			String[] tagArray = formattedString.split(",");

			for (String tagString : tagArray) {
				Tag tag = parseTag(tagString);
				tagList.add(tag);
			}
		}
		return tagList;
	}

}
