/*
 * #%L
 * WildFHIR - wildfhir-model
 * %%
 * Copyright (C) 2025 AEGIS.net, Inc.
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
 * The persistent class for the serverdirectory database table.
 *
 * @author richard.ettema
 *
 */
@Entity
@Table(name = "serverdirectory")
public class Serverdirectory implements Serializable {

	private static final long serialVersionUID = 4315645126010870553L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private String name;

	private String description;

	private String basepath;

	private String lastuser;

	@Temporal(TemporalType.TIMESTAMP)
	private Date lastupdate;

    private String oauthgranttype;

    private String oauthclientid;

    private String oauthclientsecret;

    private String oauthusername;

    private String oauthpassword;

    private String oauthprivatekey;

    private String oauthpublickey;

    private String oauthscope;

    private String oauthauthurl;

    private String oauthtokenurl;

    private String oauthintrospecturl;

	public Serverdirectory() {
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getBasepath() {
		return basepath;
	}

	public void setBasepath(String basepath) {
		this.basepath = basepath;
	}

	public String getLastuser() {
		return lastuser;
	}

	public void setLastuser(String lastuser) {
		this.lastuser = lastuser;
	}

	public Date getLastupdate() {
		return lastupdate;
	}

	public void setLastupdate(Date lastupdate) {
		this.lastupdate = lastupdate;
	}

	public String getOauthgranttype() {
		return oauthgranttype;
	}

	public void setOauthgranttype(String oauthgranttype) {
		if (oauthgranttype != null && oauthgranttype.isEmpty()) {
			oauthgranttype = null;
		}
		this.oauthgranttype = oauthgranttype;
	}

	public String getOauthclientid() {
		return oauthclientid;
	}

	public void setOauthclientid(String oauthclientid) {
		if (oauthclientid != null && oauthclientid.isEmpty()) {
			oauthclientid = null;
		}
		this.oauthclientid = oauthclientid;
	}

	public String getOauthclientsecret() {
		return oauthclientsecret;
	}

	public void setOauthclientsecret(String oauthclientsecret) {
		if (oauthclientsecret != null && oauthclientsecret.isEmpty()) {
			oauthclientsecret = null;
		}
		this.oauthclientsecret = oauthclientsecret;
	}

	public String getOauthusername() {
		return oauthusername;
	}

	public void setOauthusername(String oauthusername) {
		if (oauthusername != null && oauthusername.isEmpty()) {
			oauthusername = null;
		}
		this.oauthusername = oauthusername;
	}

	public String getOauthpassword() {
		return oauthpassword;
	}

	public void setOauthpassword(String oauthpassword) {
		if (oauthpassword != null && oauthpassword.isEmpty()) {
			oauthpassword = null;
		}
		this.oauthpassword = oauthpassword;
	}

	public String getOauthprivatekey() {
		return oauthprivatekey;
	}

	public void setOauthprivatekey(String oauthprivatekey) {
		if (oauthprivatekey != null && oauthprivatekey.isEmpty()) {
			oauthprivatekey = null;
		}
		this.oauthprivatekey = oauthprivatekey;
	}

	public String getOauthpublickey() {
		return oauthpublickey;
	}

	public void setOauthpublickey(String oauthpublickey) {
		if (oauthpublickey != null && oauthpublickey.isEmpty()) {
			oauthpublickey = null;
		}
		this.oauthpublickey = oauthpublickey;
	}

	public String getOauthscope() {
		return oauthscope;
	}

	public void setOauthscope(String oauthscope) {
		if (oauthscope != null && oauthscope.isEmpty()) {
			oauthscope = null;
		}
		this.oauthscope = oauthscope;
	}

	public String getOauthauthurl() {
		return oauthauthurl;
	}

	public void setOauthauthurl(String oauthauthurl) {
		if (oauthauthurl != null && oauthauthurl.isEmpty()) {
			oauthauthurl = null;
		}
		this.oauthauthurl = oauthauthurl;
	}

	public String getOauthtokenurl() {
		return oauthtokenurl;
	}

	public void setOauthtokenurl(String oauthtokenurl) {
		if (oauthtokenurl != null && oauthtokenurl.isEmpty()) {
			oauthtokenurl = null;
		}
		this.oauthtokenurl = oauthtokenurl;
	}

	public String getOauthintrospecturl() {
		return oauthintrospecturl;
	}

	public void setOauthintrospecturl(String oauthintrospecturl) {
		if (oauthintrospecturl != null && oauthintrospecturl.isEmpty()) {
			oauthintrospecturl = null;
		}
		this.oauthintrospecturl = oauthintrospecturl;
	}

	/**
	 * Return copy of this object
	 *
	 * @param cloneId - true, clone id value; false, set cloned id to null
	 */
	public Serverdirectory clone() {
		return this.clone(true);
	}

	public Serverdirectory clone(boolean cloneId) {
		Serverdirectory clone = new Serverdirectory();

		if (cloneId) {
			clone.setId(this.getId());
		} else {
			clone.setId(null);
		}
		clone.setBasepath(this.getBasepath());
		clone.setName(this.getName());
		clone.setDescription(this.getDescription());
		clone.setLastuser(this.getLastuser());
		clone.setLastupdate(this.getLastupdate());
		clone.setOauthgranttype(this.getOauthgranttype());
		clone.setOauthclientid(this.getOauthclientid());
		clone.setOauthclientsecret(this.getOauthclientsecret());
		clone.setOauthusername(this.getOauthusername());
		clone.setOauthpassword(this.getOauthpassword());
		clone.setOauthprivatekey(this.getOauthprivatekey());
		clone.setOauthpublickey(this.getOauthpublickey());
		clone.setOauthscope(this.getOauthscope());
		clone.setOauthauthurl(this.getOauthauthurl());
		clone.setOauthtokenurl(this.getOauthtokenurl());
		clone.setOauthintrospecturl(this.getOauthintrospecturl());

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
		Serverdirectory other = (Serverdirectory) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Serverdirectory [id=" + id + ", name=" + name + ", description=" + description + ", basepath="
				+ basepath + ", lastuser=" + lastuser + ", lastupdate=" + lastupdate + ", oauthgranttype" + oauthgranttype
				+ ", oauthclientid" + oauthclientid + ", oauthclientsecret" + oauthclientsecret + ", oauthusername" + oauthusername
				+ ", oauthpassword" + oauthpassword + ", oauthprivatekey" + oauthprivatekey + ", oauthpublickey" + oauthpublickey
				+ ", oauthscope" + oauthscope + ", oauthauthurl" + oauthauthurl + ", oauthtokenurl" + oauthtokenurl
				+ ", oauthintrospecturl" + oauthintrospecturl;
	}

}
