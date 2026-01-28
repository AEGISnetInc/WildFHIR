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

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

/**
 * The persistent class for the subscriptionactivity database table.
 *
 * @author richard.ettema
 *
 */
@Entity
@Table(name="subscriptionactivity")
public class Subscriptionactivity implements Serializable {

	private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Integer id;

    private String subscriptionId;

    @Temporal(TemporalType.TIMESTAMP)
    private Date recorded;

    private String type;

    private String status;

    private String description;

    public Subscriptionactivity() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

	public String getSubscriptionId() {
		return subscriptionId;
	}

	public void setSubscriptionId(String subscriptionId) {
		this.subscriptionId = subscriptionId;
	}

	public Date getRecorded() {
		return recorded;
	}

	public void setRecorded(Date recorded) {
		this.recorded = recorded;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Return copy of this object
	 *
	 * @param cloneId - true, clone id value; false, set cloned id to null
	 */
	public Subscriptionactivity clone() {
		return this.clone(true);
	}

	public Subscriptionactivity clone(boolean cloneId) {
		Subscriptionactivity clone = new Subscriptionactivity();

		if (cloneId) {
			clone.setId(this.getId());
		} else {
			clone.setId(null);
		}
		clone.setSubscriptionId(this.getSubscriptionId());
		clone.setRecorded(this.getRecorded());
		clone.setType(this.getType());
		clone.setStatus(this.getStatus());
		clone.setDescription(this.getDescription());

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
        Subscriptionactivity other = (Subscriptionactivity) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Subscriptionactivity [id=" + id + ", subscriptionId=" + subscriptionId
        		+ ", recorded=" + recorded + ", type=" + type + ", status=" + status
        		+ ", description=" + description;
    }

    public Subscriptionactivity copy() {
    	Subscriptionactivity dst = new Subscriptionactivity();

    	dst.setId(this.id);
    	dst.setSubscriptionId(this.subscriptionId);
    	dst.setRecorded(this.recorded);
    	dst.setType(this.type);
    	dst.setStatus(this.getStatus());
    	dst.setDescription(this.description);

    	return dst;
    }

}
