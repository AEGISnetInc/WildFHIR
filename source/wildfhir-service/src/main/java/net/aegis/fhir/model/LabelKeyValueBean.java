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
 * @author richard.ettema
 *
 */
public class LabelKeyValueBean {

	private String label;
	private String key;
	private String value;
	private String type;
	private String path;
	private String refType;

	public LabelKeyValueBean() {

	}

	public LabelKeyValueBean(String label, String key, String value) {
		this.label = label;
		this.key = key;
		this.value = value;
	}

	public LabelKeyValueBean(String label, String key, String value, String type) {
		this.label = label;
		this.key = key;
		this.value = value;
		this.type = type;
	}

	public LabelKeyValueBean(String label, String key, String value, String type, String path) {
		this.label = label;
		this.key = key;
		this.value = value;
		this.type = type;
		this.path = path;
	}

	public LabelKeyValueBean(String label, String key, String value, String type, String path, String refType) {
		this.label = label;
		this.key = key;
		this.value = value;
		this.type = type;
		this.path = path;
		this.refType = refType;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getRefType() {
		return refType;
	}

	public void setRefType(String refType) {
		this.refType = refType;
	}

}
