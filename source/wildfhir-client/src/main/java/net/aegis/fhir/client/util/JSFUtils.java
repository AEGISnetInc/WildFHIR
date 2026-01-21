/*
 * #%L
 * WildFHIR - wildfhir-client
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
/*
 * Copyright 2012-2014 Blue Lotus Software, LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.aegis.fhir.client.util;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

/**
 * Subset of the collection of utility methods that handle repetitive boilerplate code from Blue Lotus Software, LLC.
 *
 * @author John Yeary <jyeary@bluelotussoftware.com>
 * @version 1.6.4
 */
public class JSFUtils implements Serializable {

    private static final long serialVersionUID = 4005663315445526130L;

    /**
     * This method provides a convenient means of determining the JSF
     * Specification version.
     *
     * @return JSF Specification version, e.g. 2.1
     * @since 1.5
     */
    public static String getSpecificationVersion() {
        return FacesContext.getCurrentInstance().getClass().getPackage().getSpecificationVersion();
    }

    /**
     * This method provides a convenient means of determining the JSF
     * Implementation version.
     *
     * @return JSF Implementation version, e.g. 2.1.26
     * @since 1.5
     */
    public static String getImplementationVersion() {
        return FacesContext.getCurrentInstance().getClass().getPackage().getImplementationVersion();
    }

    /**
     * This method provides a convenient means of determining the JSF
     * Implementation Title.
     *
     * @return JSF implementation title, e.g. Mojarra.
     * @since 1.5
     */
    public static String getImplementationTitle() {
        return FacesContext.getCurrentInstance().getClass().getPackage().getImplementationTitle();
    }

    /**
     * <p>
     * Determines the Base URL, e.g.,
     * {@literal http://localhost:8080/myApplication} from the
     * {@link FacesContext}.</p>
     *
     * @param facesContext The {@link FacesContext} to examine.
     * @return the base {@link URL}.
     * @throws MalformedURLException if an exception occurs during parsing of
     * the {@link URL}.
     * @since 1.3
     */
    public static String getBaseURL(final FacesContext facesContext) throws MalformedURLException {
        return getBaseURL(facesContext.getExternalContext());
    }

    /**
     * <p>
     * Determines the Base URL, e.g.,
     * {@literal http://localhost:8080/myApplication} from the
     * {@link ExternalContext}.</p>
     *
     * @param externalContext The {@link ExternalContext} to examine.
     * @return the base {@link URL}.
     * @throws MalformedURLException if an exception occurs during parsing of
     * the {@link URL}.
     * @since 1.3
     */
    public static String getBaseURL(final ExternalContext externalContext) throws MalformedURLException {
        return getBaseURL((HttpServletRequest) externalContext.getRequest());
    }

    /**
     * <p>
     * Determines the Base URL, e.g.,
     * {@literal http://localhost:8080/myApplication} from the
     * {@link HttpServletRequest}.</p>
     *
     * @param request The {@link HttpServletRequest} to examine.
     * @return the base {@link URL}.
     * @throws MalformedURLException if an exception occurs during parsing of
     * the {@link URL}.
     * @see URL
     * @since 1.3
     */
    public static String getBaseURL(final HttpServletRequest request) throws MalformedURLException {
        return new URL(request.getScheme(),
                request.getServerName(),
                request.getServerPort(),
                request.getContextPath()).toString();
    }

    /**
     * This method will encode text that contains illegal XML characters to
     * prevent errors.
     *
     * @param text The text to be evaluated and encoded.
     * @return An encoded {@code String}.
     * @since 1.6.4
     */
    public static String XMLEncode(final String text) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            char value = text.charAt(i);
            if (!((value >= 'a' && value <= 'z')
                    || (value >= 'A' && value <= 'Z')
                    || (value >= '0' && value <= '9'))) {
                MessageFormat.format("&#{0};", (int) value);
                result.append(MessageFormat.format("&#{0};", (int) value));
            } else {
                result.append(value);
            }
        }
        return result.toString();
    }
}