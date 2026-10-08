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

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

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

	private String codename;

	private String value;

	private Integer intvalue;

	private String description;

    private String resourcecontents;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getCodename() {
		return codename;
	}

	public void setCodename(String codename) {
		this.codename = codename;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Integer getIntvalue() {
		return intvalue;
	}

	public void setIntvalue(Integer intvalue) {
		this.intvalue = intvalue;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

    public String getResourcecontents() {
        return resourcecontents;
    }

    public void setResourcecontents(String resourcecontents) {
        this.resourcecontents = resourcecontents;
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
    	clone.setCodename(this.getCodename());
    	clone.setValue(this.getValue());
    	clone.setIntvalue(this.getIntvalue());
    	clone.setDescription(this.getDescription());
    	clone.setResourcecontents(this.getResourcecontents());

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
        return "Code [id=" + id + ", codename=" + codename + ", value=" + value
        		+ ", intvalue=" + intvalue.toString() + ", description=" + description + "]";
    }

}
