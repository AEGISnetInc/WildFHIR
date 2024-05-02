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
import javax.persistence.*;

import java.util.Date;
import java.util.Set;

/**
 * The persistent class for the resource database table.
 *
 * @author richard.ettema
 *
 */
@Entity
@Table(name="resource")
public class Resource implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Integer id;

    private String resourceId;

    private Integer versionId;

    private String resourceType;

    private String status;

    private String lastUser;

    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdate;

    private byte[] resourceContents;

    // 10 Placeholder columns used for sort criteria
    private String sort0;
    private String sort1;
    private String sort2;
    private String sort3;
    private String sort4;
    private String sort5;
    private String sort6;
    private String sort7;
    private String sort8;
    private String sort9;

    //bi-directional many-to-one association to Resourcemetadata
    @OneToMany(mappedBy="resource")
    private Set<Resourcemetadata> resourcemetadatas;


    public Resource() {
    }

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

    public String getSort0() {
		return sort0;
	}

	public void setSort0(String sort0) {
		this.sort0 = sort0;
	}

	public String getSort1() {
		return sort1;
	}

	public void setSort1(String sort1) {
		this.sort1 = sort1;
	}

	public String getSort2() {
		return sort2;
	}

	public void setSort2(String sort2) {
		this.sort2 = sort2;
	}

	public String getSort3() {
		return sort3;
	}

	public void setSort3(String sort3) {
		this.sort3 = sort3;
	}

	public String getSort4() {
		return sort4;
	}

	public void setSort4(String sort4) {
		this.sort4 = sort4;
	}

	public String getSort5() {
		return sort5;
	}

	public void setSort5(String sort5) {
		this.sort5 = sort5;
	}

	public String getSort6() {
		return sort6;
	}

	public void setSort6(String sort6) {
		this.sort6 = sort6;
	}

	public String getSort7() {
		return sort7;
	}

	public void setSort7(String sort7) {
		this.sort7 = sort7;
	}

	public String getSort8() {
		return sort8;
	}

	public void setSort8(String sort8) {
		this.sort8 = sort8;
	}

	public String getSort9() {
		return sort9;
	}

	public void setSort9(String sort9) {
		this.sort9 = sort9;
	}

	public Set<Resourcemetadata> getResourcemetadatas() {
        return this.resourcemetadatas;
    }

    public void setResourcemetadatas(Set<Resourcemetadata> resourcemetadatas) {
        this.resourcemetadatas = resourcemetadatas;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((resourceId == null) ? 0 : resourceId.hashCode());
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
        Resource other = (Resource) obj;
        if (resourceId == null) {
            if (other.resourceId != null)
                return false;
        } else if (!resourceId.equals(other.resourceId))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Resource [id=" + id + ", resourceId=" + resourceId + ", versionId="
                + versionId + ", resourceType=" + resourceType + ", status=" + status
                + ", lastUser=" + lastUser + ", lastUpdate=" + lastUpdate
                + ", resourceContents=" + resourceContents.toString();
    }

    public Resource copy() {
    	Resource dst = new Resource();

    	dst.setId(this.id);
    	dst.setResourceId(this.getResourceId());
    	dst.setVersionId(this.getVersionId());
    	dst.setResourceType(this.getResourceType());
    	dst.setStatus(this.getStatus());
    	dst.setLastUser(this.getLastUser());
    	dst.setLastUpdate(this.getLastUpdate());
    	dst.setResourceContents(this.getResourceContents());
    	dst.setSort0(this.getSort0());
    	dst.setSort1(this.getSort1());
    	dst.setSort2(this.getSort2());
    	dst.setSort3(this.getSort3());
    	dst.setSort4(this.getSort4());
    	dst.setSort5(this.getSort5());
    	dst.setSort6(this.getSort6());
    	dst.setSort7(this.getSort7());
    	dst.setSort8(this.getSort8());
    	dst.setSort9(this.getSort9());

    	return dst;
    }
}