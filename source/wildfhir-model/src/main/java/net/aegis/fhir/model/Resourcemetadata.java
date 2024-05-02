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

/**
 * The persistent class for the resourcemetadata database table.
 *
 * @author richard.ettema
 *
 */
@Entity
@Table(name="resourcemetadata")
public class Resourcemetadata implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Integer id;

    //bi-directional many-to-one association to Resource
    @ManyToOne
    @JoinColumn(name="resourceJoinId")
    private Resource resource;

    private String paramName;

    private String paramType;

    private String paramValue;

    private String systemValue;

    private String codeValue;

    private String textValue;

    private String paramValueU;

    private String textValueU;


    public Resourcemetadata() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Resource getResource() {
        return this.resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public String getParamName() {
        return paramName;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    public String getParamType() {
		return paramType;
	}

	public void setParamType(String paramType) {
		this.paramType = paramType;
	}

	public String getParamValue() {
        return paramValue;
    }

    public void setParamValue(String paramValue) {
    	if (paramValue != null && paramValue.length() > 750) {
    		paramValue = paramValue.substring(0, 749);
    	}
        this.paramValue = paramValue;
    }

    public String getSystemValue() {
        return systemValue;
    }

    public void setSystemValue(String systemValue) {
    	if (systemValue != null && systemValue.length() > 750) {
    		systemValue = systemValue.substring(0, 749);
    	}
        this.systemValue = systemValue;
    }

    public String getCodeValue() {
		return codeValue;
	}

	public void setCodeValue(String codeValue) {
    	if (codeValue != null && codeValue.length() > 750) {
    		codeValue = codeValue.substring(0, 749);
    	}
		this.codeValue = codeValue;
	}

	public String getTextValue() {
		return textValue;
	}

	public void setTextValue(String textValue) {
    	if (textValue != null && textValue.length() > 750) {
    		textValue = textValue.substring(0, 749);
    	}
		this.textValue = textValue;
	}

	public String getParamValueU() {
        return paramValueU;
    }

    public void setParamValueU(String paramValueU) {
    	if (paramValueU != null && paramValueU.length() > 750) {
    		paramValueU = paramValueU.substring(0, 749);
    	}
    	if (paramValueU != null) {
    		paramValueU = paramValueU.toUpperCase();
    	}
        this.paramValueU = paramValueU;
    }

	public String getTextValueU() {
		return textValueU;
	}

	public void setTextValueU(String textValueU) {
    	if (textValueU != null && textValueU.length() > 750) {
    		textValueU = textValueU.substring(0, 749);
    	}
    	if (textValueU != null) {
    		textValueU = textValueU.toUpperCase();
    	}
		this.textValueU = textValueU;
	}

	/**
     * Return copy of this object
     *
     * @param cloneId - true, clone id value; false, set cloned id to null
     */
    public Resourcemetadata clone() {
    	return this.clone(true);
    }
    public Resourcemetadata clone(boolean cloneId) {
    	Resourcemetadata clone = new Resourcemetadata();

    	if (cloneId) {
    		clone.setId(this.getId());
    	} else {
    		clone.setId(null);
    	}
    	clone.setResource(this.getResource());
    	clone.setParamName(this.getParamName());
    	clone.setParamType(this.getParamType());
    	clone.setParamValue(this.getParamValue());
    	clone.setSystemValue(this.getSystemValue());
    	clone.setCodeValue(this.getCodeValue());
    	clone.setTextValue(this.getTextValue());
    	clone.setParamValueU(this.getParamValueU());
    	clone.setTextValueU(this.getTextValueU());

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
        Resourcemetadata other = (Resourcemetadata) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Resourcemetadata [id=" + id + ", resourceId=" + ((resource == null) ? 0 : resource.getId())
                + ", paramName=" + paramName+ ", paramType=" + paramType + ", paramValue=" + ((paramValue == null) ? "null" : paramValue)
                + ", system=" + ((systemValue == null) ? "null" : systemValue) + ", code=" + ((codeValue == null) ? "null" : codeValue)
                + ", textValue=" + ((textValue == null) ? "null" : textValue) + ", paramValueU=" + ((paramValueU == null) ? "null" : paramValueU)
                + ", textValueU=" + ((textValueU == null) ? "null" : textValueU);
    }

}