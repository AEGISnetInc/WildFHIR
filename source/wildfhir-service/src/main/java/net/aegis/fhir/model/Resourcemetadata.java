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
import jakarta.persistence.*;

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
    @JoinColumn(name="resourcejoinid")
    private Resource resource;

    private String paramname;

    private String paramtype;

    private String paramvalue;

    private String systemvalue;

    private String codevalue;

    private String textvalue;

    private String paramvalueu;

    private String textvalueu;


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

    public String getParamname() {
        return paramname;
    }

    public void setParamname(String paramname) {
        this.paramname = paramname;
    }

    public String getParamtype() {
		return paramtype;
	}

	public void setParamtype(String paramtype) {
		this.paramtype = paramtype;
	}

	public String getParamvalue() {
        return paramvalue;
    }

    public void setParamvalue(String paramvalue) {
    	if (paramvalue != null && paramvalue.length() > 670) {
    		paramvalue = paramvalue.substring(0, 669);
    	}
        this.paramvalue = paramvalue;
    }

    public String getSystemvalue() {
        return systemvalue;
    }

    public void setSystemvalue(String systemvalue) {
    	if (systemvalue != null && systemvalue.length() > 670) {
    		systemvalue = systemvalue.substring(0, 669);
    	}
        this.systemvalue = systemvalue;
    }

    public String getCodevalue() {
		return codevalue;
	}

	public void setCodevalue(String codevalue) {
    	if (codevalue != null && codevalue.length() > 670) {
    		codevalue = codevalue.substring(0, 669);
    	}
		this.codevalue = codevalue;
	}

	public String getTextvalue() {
		return textvalue;
	}

	public void setTextvalue(String textvalue) {
    	if (textvalue != null && textvalue.length() > 670) {
    		textvalue = textvalue.substring(0, 669);
    	}
		this.textvalue = textvalue;
	}

	public String getParamvalueu() {
        return paramvalueu;
    }

    public void setParamvalueu(String paramvalueu) {
    	if (paramvalueu != null && paramvalueu.length() > 670) {
    		paramvalueu = paramvalueu.substring(0, 669);
    	}
    	if (paramvalueu != null) {
    		paramvalueu = paramvalueu.toUpperCase();
    	}
        this.paramvalueu = paramvalueu;
    }

	public String getTextvalueu() {
		return textvalueu;
	}

	public void setTextvalueu(String textvalueu) {
    	if (textvalueu != null && textvalueu.length() > 670) {
    		textvalueu = textvalueu.substring(0, 669);
    	}
    	if (textvalueu != null) {
    		textvalueu = textvalueu.toUpperCase();
    	}
		this.textvalueu = textvalueu;
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
    	clone.setParamname(this.getParamname());
    	clone.setParamtype(this.getParamtype());
    	clone.setParamvalue(this.getParamvalue());
    	clone.setSystemvalue(this.getSystemvalue());
    	clone.setCodevalue(this.getCodevalue());
    	clone.setTextvalue(this.getTextvalue());
    	clone.setParamvalueu(this.getParamvalueu());
    	clone.setTextvalueu(this.getTextvalueu());

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
        return "Resourcemetadata [id=" + id + ", resourceid=" + ((resource == null) ? 0 : resource.getId())
                + ", paramname=" + paramname+ ", paramtype=" + paramtype + ", paramvalue=" + ((paramvalue == null) ? "null" : paramvalue)
                + ", system=" + ((systemvalue == null) ? "null" : systemvalue) + ", code=" + ((codevalue == null) ? "null" : codevalue)
                + ", textvalue=" + ((textvalue == null) ? "null" : textvalue) + ", paramvalueu=" + ((paramvalueu == null) ? "null" : paramvalueu)
                + ", textvalueu=" + ((textvalueu == null) ? "null" : textvalueu);
    }

}