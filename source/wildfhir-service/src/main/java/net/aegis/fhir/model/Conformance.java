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
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * The persistent class for the conformance database table.
 *
 * @author richard.ettema
 *
 */
@Entity
@Table(name = "conformance")
public class Conformance implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private String resourceId;

	private Integer versionId;

	private String resourceType;

	private String status;

	private String lastUser;

	@Temporal(TemporalType.TIMESTAMP)
	private Date lastUpdate;

	private byte[] resourceContents;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getResourceId() {
		return resourceId;
	}

	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}

	public Integer getVersionId() {
		return versionId;
	}

	public void setVersionId(Integer versionId) {
		this.versionId = versionId;
	}

	public String getResourceType() {
		return resourceType;
	}

	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getLastUser() {
		return lastUser;
	}

	public void setLastUser(String lastUser) {
		this.lastUser = lastUser;
	}

	public Date getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(Date lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public byte[] getResourceContents() {
		return resourceContents;
	}

	public void setResourceContents(byte[] resourceContents) {
		this.resourceContents = resourceContents;
	}

	/**
     * Return copy of this object
     *
     * @param cloneId - true, clone id value; false, set cloned id to null
     */
    public Conformance clone() {
    	return this.clone(true);
    }

    public Conformance clone(boolean cloneId) {
    	Conformance clone = new Conformance();

    	if (cloneId) {
    		clone.setId(this.getId());
    	} else {
    		clone.setId(null);
    	}
    	clone.setResourceId(this.getResourceId());
    	clone.setVersionId(this.getVersionId());
    	clone.setResourceType(this.getResourceType());
    	clone.setStatus(this.getStatus());
    	clone.setLastUser(this.getLastUser());
    	clone.setLastUpdate(this.getLastUpdate());
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
        Conformance other = (Conformance) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Conformance [id=" + id + ", resourceId=" + resourceId + ", versionId="
                + versionId + ", resourceType=" + resourceType + ", status=" + status
                + ", lastUser=" + lastUser;
    }


}
