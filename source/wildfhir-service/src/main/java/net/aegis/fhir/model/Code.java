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

import java.io.Serializable;
import java.nio.charset.StandardCharsets;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * The persistent class for the code database table.
 *
 * @author richard.ettema
 *
 */
@Entity
@Table(name = "code")
public class Code implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private String codeName;

	private String value;

	private Integer intValue;

	private String description;

    private byte[] resourceContents;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getCodeName() {
		return codeName;
	}

	public void setCodeName(String codeName) {
		this.codeName = codeName;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Integer getIntValue() {
		return intValue;
	}

	public void setIntValue(Integer intValue) {
		this.intValue = intValue;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

    public byte[] getResourceContents() {
        return resourceContents;
    }

    public void setResourceContents(byte[] resourceContents) {
        this.resourceContents = resourceContents;
    }

    // Helper methods for resourceContents as String
    public String getResourceContentsString() {
    	if (this.resourceContents != null) {
    		return new String(resourceContents, StandardCharsets.UTF_8);
    	}
		return null;
	}

	public void setResourceContentsString(String resourceContentsString) {
		if (resourceContentsString != null && !resourceContentsString.isEmpty()) {
			this.resourceContents = resourceContentsString.getBytes(StandardCharsets.UTF_8);
		}
		else {
			this.resourceContents = null;
		}
	}

    /**
     * Return copy of this object
     *
     * @param cloneId - true, clone id value; false, set cloned id to null
     */
    public Code clone() {
    	return this.clone(true);
    }
    public Code clone(boolean cloneId) {
    	Code clone = new Code();

    	if (cloneId) {
    		clone.setId(this.getId());
    	} else {
    		clone.setId(null);
    	}
    	clone.setCodeName(this.getCodeName());
    	clone.setValue(this.getValue());
    	clone.setIntValue(this.getIntValue());
    	clone.setDescription(this.getDescription());
    	clone.setResourceContents(this.getResourceContents());

    	return clone;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Code other = (Code) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Code [id=" + id + ", codeName=" + codeName + ", value=" + value
        		+ ", intValue=" + intValue.toString()
                + ", description=" + description + "]";
    }

}
