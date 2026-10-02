/*
 * #%L
 * WildFHIR - wildfhir-service
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
package net.aegis.fhir.service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionManagement;
import jakarta.ejb.TransactionManagementType;
import jakarta.inject.Inject;

import org.apache.http.NameValuePair;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

/**
 * OAuth services for Authorization token verification and validation.
 *
 * The @Stateless annotation eliminates the need for manual transaction
 * demarcation
 *
 * @author richard.ettema
 * 
 */
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class OAuthOps {

	private Logger log = Logger.getLogger("OAuthOps");

	@Inject
	CodeService codeService;

	/**
	 * Check Authorization token scopes
	 * - Get auth token assigned scope(s)
	 * - Match scope(s) against resourceType and operation
	 *
	 * @param authToken
	 * @param resourceType
	 * @param operation Uses SMART v2 access format; only one of 'c','r','u','d','s'
	 * @param orderedParams
	 * @return Map<String> Matched scopes; empty if none
	 */
	public Map<String,String> getMappedAuthTokenScopes(String authToken, String resourceType, String operation) throws Exception {
		return getMappedAuthTokenScopes(authToken, resourceType, operation, null);
	}

	public Map<String,String> getMappedAuthTokenScopes(String authToken, String resourceType, String operation, List<NameValuePair> orderedParams) throws Exception {
		log.fine("\n\n=======================================================================\n\n");
		log.fine("OAuthOps.getMappedAuthTokenScopes - START: Operation: " + operation + ", resource Type: " + resourceType);

		Map<String,String> matchedScope = new HashMap<String,String>();
		String scope = "";
		Pattern p = Pattern.compile("c?r?u?d?s?");
		boolean isAuthorized = false;

		try {
			JWTClaimsSet claims = this.getJwtClaimsSet(authToken);

			scope = (String) claims.getClaim("scope");

			if (scope != null && !scope.isEmpty()) {
				int posDelim1 = -1;
				int posDelim2 = -1;
				int posDelim3 = -1;
				int endIndex = -1;
				String permission = null;
				String resource = null;
				String access = null;
				String queryParams = null;
				String[] splitScopes = scope.split(" ");
				for (String splitScope : splitScopes) {
					log.fine("\ngetMappedAuthTokenScopes(): SCOPE --> '" + splitScope + "'");
					/*
					 * We are interested in scopes that following the openid-heart-fhir-oauth2 scopes definitions and pattern
					 * for the SMART-on-FHIR 1.x/2.x and FHIR Bulk Data specifications
					 * link: https://openid.net/specs/openid-heart-fhir-oauth2-1_0.html
					 * link: https://hl7.org/fhir/smart-app-launch/1.0.0/scopes-and-launch-context/index.html#scopes-for-requesting-clinical-data
					 * link: https://hl7.org/fhir/smart-app-launch/STU2/scopes-and-launch-context.html#scopes-for-requesting-clinical-data
					 * link: https://hl7.org/fhir/uv/bulkdata/authorization/index.html
					 * pattern: permission/resource.access
					 *
					 * - permission (patient, user or system)
					 * - resource (FHIR Resource Type or *)
					 * - access SMART v1(read, write or *); v2(cruds)
					 *   v1 .read -> v2 .rs
					 *   v1 .write -> v2 .cud
					 *   v1 .* -> v2 .cruds
					 * - queryParams SMART v2 fine-grained read/search qualifier
					 */
					permission = null;
					resource = null;
					access = null;
					queryParams = null;

					posDelim1 = splitScope.indexOf("/");
					posDelim2 = splitScope.indexOf(".");
					posDelim3 = splitScope.indexOf("?");
					endIndex = splitScope.length();

					isAuthorized = false;

					//System.out.println("\n\n ---> OAuthOps.checkAuthIntrospectScopes(): delimiters for:  " + splitScope + ", posDelim1: " + posDelim1 + ", posDelim2: " + posDelim2 + ", posDelim3: " + posDelim3 + "\n\n");

					if (posDelim1 > 1) {
						permission = splitScope.substring(0, posDelim1);

						if (posDelim2 > 1) {
							resource = splitScope.substring(posDelim1 + 1, posDelim2);

							if (posDelim3 > 1) {
								access = splitScope.substring(posDelim2 + 1, posDelim3);
								queryParams = splitScope.substring(posDelim3 + 1, endIndex);
							}
							else {
								access = splitScope.substring(posDelim2 + 1, endIndex);
								//System.out.println("\n\n ---> OAuthOps.checkAuthIntrospectScopes(): access for the scope: " + access + " -> " + splitScope + "\n\n");
							}

							// If access is null or empty, skip
							if (access != null && !access.isEmpty()) {
								// Convert any SMART v1 access to v2
								if (access.equals("read")) {
									access = "rs";
								}
								else if (access.equals("write")) {
									access = "cud";
								}
								else if (access.equals("*")) {
									access = "cruds";
								}

								//System.out.println("\n\n ---> OAuthOps.checkAuthIntrospectScopes(): updated access for the scope: " + access + " -> " + splitScope + "\n\n");

								// Check for SMART v2 valid access format; i.e. pattern "c?r?u?d?s?"
								if (p.matcher(access).matches()) {

									boolean isResourceAllowed = false;
									boolean isAccessAllowed = false;
									boolean isQueryParmsAllowed = true;

									// Test SMART v2 access against current operation
									if (access.contains(operation)) {
										isAccessAllowed = true;
									}

									//System.out.println("\n\n ---> OAuthOps.checkAuthIntrospectScopes(): permission for the scope: " + access + " -> " + splitScope + "\n\n");

									// Next test permission level with resource type
									// Only allow patient, system and user permissions
									if (permission.equals("patient") || permission.equals("user") || permission.equals("system")) {
										// Check for all resource types allowed
										if (resource.equals("*")) {
											isResourceAllowed = true;
										}
										else {
											// FHIR-311 - Removed check for Patient Compartment resource types - not appropriate
											// Test requested resourceType equal to resource allowed
											if (resource.equals(resourceType)) {
												isResourceAllowed = true;
											}
										}
									}

									// If queryParams not null, check against passed in parameters
									if (queryParams != null && orderedParams != null && !orderedParams.isEmpty()) {
										/*
										 * Initialize isQueryParmsAllowed to false
										 * Iterate through orderedParams to find a match with queryParams
										 */
										isQueryParmsAllowed = false;
										for (NameValuePair param : orderedParams) {
											log.fine("  param.name = '" + param.getName() + "'; param.value = '" + param.getValue() + "'");
											if (queryParams.equals(param.getName() + "=" + param.getValue())) {
												isQueryParmsAllowed = true;
												log.fine("  --> isQueryParmsAllowed = true");
												break;
											}
										}
									}

									// No other permissions allowed
									//System.out.println("\n\n ---> OAuthOps.checkAuthIntrospectScopes(): isResourceAllowed? " + isResourceAllowed + ",  isAccessAllowed? " + isAccessAllowed
									//		 + ",  isQueryParmsAllowed? " + isQueryParmsAllowed+ " for the scope: " + splitScope + "\n\n");
									// isAuthorized determined by both isResourceAllowed and isAccessAllowed
									isAuthorized = (isResourceAllowed && isAccessAllowed && isQueryParmsAllowed);
									//System.out.println("\n\n ---> OAuthOps.checkAuthIntrospectScopes(): isAuthorized?-A " + isAuthorized);
								}
								else {
									log.fine("   <-- OAuthOps.getMappedAuthTokenScopes(): SKIPPED (access is not a valid SMART v2 pattern) -->: " + splitScope);
								}
							}
							else {
								log.fine("   <-- OAuthOps.getMappedAuthTokenScopes(): SKIPPED (scope access not defined) -->" + splitScope);
							}
						}
						else {
							log.fine("   <-- OAuthOps.getMappedAuthTokenScopes(): SKIPPED (cannot determine resource type) -->"+ splitScope);
						}
					}
					else {
						log.fine("   <-- OAuthOps.getMappedAuthTokenScopes(): SKIPPED (cannot determine permission) -->"+ splitScope);
					}

					//System.out.println("\n\n ---> OAuthOps.getMappedAuthTokenScopes(): isAuthorized?-B " + isAuthorized);
					//System.out.println(" ---> OAuthOps.getMappedAuthTokenScopes(): queryParams: " + (queryParams != null ? queryParams : ""));
					if (isAuthorized == true) {
						log.fine("   <-- OAuthOps.getMappedAuthTokenScopes(): AUTHORIZED (added to matched scope with queryParams '" + (queryParams != null ? queryParams : "") + "') -->");
						matchedScope.put(splitScope, queryParams);
						break;
					} else {
						log.fine("   <-- OAuthOps.getMappedAuthTokenScopes(): Not authorized for the scope: "+ splitScope);
					}
				}
			}
			log.fine("   <-- OAuthOps.getMappedAuthTokenScopes(): Returning the matched scope: " + matchedScope);
		} catch (Exception e) {
			throw e;
		}

		return matchedScope;
	}

	/**
	 * Check Authorization token scopes
	 * - Get auth token assigned scope(s)
	 * - Match scope(s) against resourceType and operation
	 *
	 * @param returnMessage
	 * @param authToken
	 * @param resourceType
	 * @param operation Uses SMART v2 access format; only one of 'c','r','u','d','s'
	 * @param orderedParams
	 * @return boolean
	 */
	public boolean isMappedAuthTokenScopes(StringBuffer returnMessage, String authToken, String resourceType, String operation) throws Exception {
		return isMappedAuthTokenScopes(returnMessage, authToken, resourceType, operation, null);
	}

	public boolean isMappedAuthTokenScopes(StringBuffer returnMessage, String authToken, String resourceType, String operation, List<NameValuePair> orderedParams) throws Exception {
		log.fine("\n\n=======================================================================\n\n");
		log.fine("OAuthOps.isMappedAuthTokenScopes - START: Operation: " + operation + ", resource Type: " + resourceType);

		String scope = "";
		Pattern p = Pattern.compile("c?r?u?d?s?");
		boolean isAuthorized = false;

		if (returnMessage == null) {
			returnMessage = new StringBuffer();
		}

		try {
			JWTClaimsSet claims = this.getJwtClaimsSet(authToken);

			// Check for expired authorization token before going any further
			if (!this.isTokenExpired(claims)) {

				scope = (String) claims.getClaim("scope");

				if (scope != null && !scope.isEmpty()) {
					int posDelim1 = -1;
					int posDelim2 = -1;
					int posDelim3 = -1;
					int endIndex = -1;
					String permission = null;
					String resource = null;
					String access = null;
					String queryParams = null;
					String[] splitScopes = scope.split(" ");
					for (String splitScope : splitScopes) {
						log.fine("\nisMappedAuthTokenScopes(): SCOPE --> '" + splitScope + "'");
						/*
						 * We are interested in scopes that following the openid-heart-fhir-oauth2 scopes definitions and pattern
						 * for the SMART-on-FHIR 1.x/2.x and FHIR Bulk Data specifications
						 * link: https://openid.net/specs/openid-heart-fhir-oauth2-1_0.html
						 * link: https://hl7.org/fhir/smart-app-launch/1.0.0/scopes-and-launch-context/index.html#scopes-for-requesting-clinical-data
						 * link: https://hl7.org/fhir/smart-app-launch/STU2/scopes-and-launch-context.html#scopes-for-requesting-clinical-data
						 * link: https://hl7.org/fhir/uv/bulkdata/authorization/index.html
						 * pattern: permission/resource.access
						 *
						 * - permission (patient, user or system)
						 * - resource (FHIR Resource Type or *)
						 * - access SMART v1(read, write or *); v2(cruds)
						 *   v1 .read -> v2 .rs
						 *   v1 .write -> v2 .cud
						 *   v1 .* -> v2 .cruds
						 * - queryParams SMART v2 fine-grained read/search qualifier
						 */
						permission = null;
						resource = null;
						access = null;
						queryParams = null;

						posDelim1 = splitScope.indexOf("/");
						posDelim2 = splitScope.indexOf(".");
						posDelim3 = splitScope.indexOf("?");
						endIndex = splitScope.length();

						isAuthorized = false;

						//System.out.println("\n\n ---> OAuthOps.checkAuthIntrospectScopes(): delimiters for:  " + splitScope + ", posDelim1: " + posDelim1 + ", posDelim2: " + posDelim2 + ", posDelim3: " + posDelim3 + "\n\n");

						if (posDelim1 > 1) {
							permission = splitScope.substring(0, posDelim1);

							if (posDelim2 > 1) {
								resource = splitScope.substring(posDelim1 + 1, posDelim2);

								if (posDelim3 > 1) {
									access = splitScope.substring(posDelim2 + 1, posDelim3);
									queryParams = splitScope.substring(posDelim3 + 1, endIndex);
								}
								else {
									access = splitScope.substring(posDelim2 + 1, endIndex);
									//System.out.println("\n\n ---> OAuthOps.checkAuthIntrospectScopes(): access for the scope: " + access + " -> " + splitScope + "\n\n");
								}

								// If access is null or empty, skip
								if (access != null && !access.isEmpty()) {
									// Convert any SMART v1 access to v2
									if (access.equals("read")) {
										access = "rs";
									}
									else if (access.equals("write")) {
										access = "cud";
									}
									else if (access.equals("*")) {
										access = "cruds";
									}

									//System.out.println("\n\n ---> OAuthOps.checkAuthIntrospectScopes(): updated access for the scope: " + access + " -> " + splitScope + "\n\n");

									// Check for SMART v2 valid access format; i.e. pattern "c?r?u?d?s?"
									if (p.matcher(access).matches()) {

										boolean isResourceAllowed = false;
										boolean isAccessAllowed = false;
										boolean isQueryParmsAllowed = true;

										// Test SMART v2 access against current operation
										if (access.contains(operation)) {
											isAccessAllowed = true;
										}

										//System.out.println("\n\n ---> OAuthOps.checkAuthIntrospectScopes(): permission for the scope: " + access + " -> " + splitScope + "\n\n");

										// Next test permission level with resource type
										// Only allow patient, system and user permissions
										if (permission.equals("patient") || permission.equals("user") || permission.equals("system")) {
											// Check for all resource types allowed
											if (resource.equals("*")) {
												isResourceAllowed = true;
											}
											else {
												// FHIR-311 - Removed check for Patient Compartment resource types - not appropriate
												// Test requested resourceType equal to resource allowed
												if (resource.equals(resourceType)) {
													isResourceAllowed = true;
												}
											}
										}

										// If queryParams not null, check against passed in parameters
										if (queryParams != null && orderedParams != null && !orderedParams.isEmpty()) {
											/*
											 * Initialize isQueryParmsAllowed to false
											 * Iterate through orderedParams to find a match with queryParams
											 */
											isQueryParmsAllowed = false;
											for (NameValuePair param : orderedParams) {
												log.fine("  param.name = '" + param.getName() + "'; param.value = '" + param.getValue() + "'");
												if (queryParams.equals(param.getName() + "=" + param.getValue())) {
													isQueryParmsAllowed = true;
													log.fine("  --> isQueryParmsAllowed = true");
													break;
												}
											}
										}

										// No other permissions allowed
										//System.out.println("\n\n ---> OAuthOps.checkAuthIntrospectScopes(): isResourceAllowed? " + isResourceAllowed + ",  isAccessAllowed? " + isAccessAllowed
										//		 + ",  isQueryParmsAllowed? " + isQueryParmsAllowed+ " for the scope: " + splitScope + "\n\n");
										// isAuthorized determined by both isResourceAllowed and isAccessAllowed
										isAuthorized = (isResourceAllowed && isAccessAllowed && isQueryParmsAllowed);
										//System.out.println("\n\n ---> OAuthOps.checkAuthIntrospectScopes(): isAuthorized?-A " + isAuthorized);
									}
									else {
										log.fine("   <-- OAuthOps.isMappedAuthTokenScopes(): SKIPPED (access is not a valid SMART v2 pattern) -->: " + splitScope);
									}
								}
								else {
									log.fine("   <-- OAuthOps.isMappedAuthTokenScopes(): SKIPPED (scope access not defined) -->" + splitScope);
								}
							}
							else {
								log.fine("   <-- OAuthOps.isMappedAuthTokenScopes(): SKIPPED (cannot determine resource type) -->"+ splitScope);
							}
						}
						else {
							log.fine("   <-- OAuthOps.isMappedAuthTokenScopes(): SKIPPED (cannot determine permission) -->"+ splitScope);
						}

						//System.out.println("\n\n ---> OAuthOps.isMappedAuthTokenScopes(): isAuthorized?-B " + isAuthorized);
						//System.out.println(" ---> OAuthOps.isMappedAuthTokenScopes(): queryParams: " + (queryParams != null ? queryParams : ""));
						if (isAuthorized == true) {
							log.fine("   <-- OAuthOps.isMappedAuthTokenScopes(): AUTHORIZED (added to matched scope with queryParams '" + (queryParams != null ? queryParams : "") + "') -->");
							break;
						} else {
							log.fine("   <-- OAuthOps.isMappedAuthTokenScopes(): Not authorized for the scope: "+ splitScope);
						}
					} // for each scope

					if (!isAuthorized) {
						returnMessage.append(" Authorization token is not authorized for any requested scopes: '" + scope + "'.");
					}
				} // if scope != null or empty
			} // if !token expired
			else {
				// token is expired
				returnMessage.append(" Authorization token is expired!");
			}

			log.fine("   <-- OAuthOps.isMappedAuthTokenScopes(): Returning is authorized: " + isAuthorized);
		} catch (Exception e) {
			throw e;
		}

		return isAuthorized;
	}

	/**
	 * Determine expiration state of given Authorization token by
	 * inspecting the JWT claims exp attribute.
	 * 
	 * @param authToken
	 * @return boolean
	 * @throws Exception
	 */
	public boolean isTokenExpired(String authToken) throws Exception {
		log.fine("OAuthOps.isTokenExpired(String authToken)");

		boolean isExpired = false;

		try {
			JWTClaimsSet claims = this.getJwtClaimsSet(authToken);

			Date exp = (Date)claims.getClaim("exp");
			Date now = new Date();
			log.fine("exp: " + exp);
			log.fine("now: " + now);

			if (exp != null) {
				if (now.after(exp)) {
					isExpired = true;
				}
			}
			else {
				throw new Exception("Authorization Token claims 'exp' is null!");
			}
		} catch (Exception e) {
			throw e;
		}

		return isExpired;
	}

	/*
	 * Private methods
	 */

	/**
	 * Determine expiration state of given Authorization token by
	 * inspecting the JWT claims exp attribute.
	 * 
	 * @param authToken
	 * @return boolean
	 * @throws Exception
	 */
	private boolean isTokenExpired(JWTClaimsSet claims) throws Exception {
		log.fine("OAuthOps.isTokenExpired(JWTClaimsSet claims)");

		boolean isExpired = false;

		try {
			Date exp = (Date)claims.getClaim("exp");
			Date now = new Date();
			log.fine("exp: " + exp);
			log.fine("now: " + now);

			if (exp != null) {
				if (now.after(exp)) {
					isExpired = true;
				}
			}
			else {
				throw new Exception("Authorization Token claims 'exp' is null!");
			}
		} catch (Exception e) {
			throw e;
		}

		return isExpired;
	}

	/**
	 * Helper method to return the JWTClaimsSet from an OAuth authorization token
	 * 
	 * @param authToken
	 * @return <code>JWTClaimsSet</code>
	 * @throws Exception
	 */
	private JWTClaimsSet getJwtClaimsSet(String authToken) throws Exception {
		log.fine("OAuthOps.getJwtClaimsSet(String authToken)");

		JWTClaimsSet claims = null;

		try {
			if (authToken != null && !authToken.isEmpty()) {

				// Remove prefix if present
				if (authToken.contains(" ")) {
					authToken = authToken.substring(authToken.indexOf(" ") + 1);
				}

				SignedJWT signedJWT = SignedJWT.parse(authToken);
				claims = signedJWT.getJWTClaimsSet();
			} else {
				throw new Exception("Authorization Token contents empty!");
			}
		} catch (Exception e) {
			throw e;
		}

		return claims;
	}

}
