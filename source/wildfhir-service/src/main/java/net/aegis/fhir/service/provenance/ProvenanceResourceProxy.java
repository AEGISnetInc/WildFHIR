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
package net.aegis.fhir.service.provenance;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.UriInfo;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Provenance;
import org.hl7.fhir.r4.model.Provenance.ProvenanceAgentComponent;
import org.hl7.fhir.r4.model.Provenance.ProvenanceEntityRole;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.Signature;

import net.aegis.fhir.service.audit.AuditEventConstant;
import net.aegis.fhir.service.util.ServicesUtil;
import net.aegis.fhir.service.util.UUIDUtil;

/**
 * @author Venkat.Keesara
 * @author richard.ettema
 *
 */
public abstract class ProvenanceResourceProxy {

	private Logger log = Logger.getLogger(getClass().getName());

	public static String appId = "WildFHIRCE";
	public static String userId = "wildfhirce-system";
	public static String userName = "WildFHIR System";
	public static String site = "wildfhirce-site";

	public abstract Resource generateProvenance(UriInfo context, HttpHeaders headers, String payload, String resourceType, String locationPath, String resourceId, Identifier identifier, String operation) throws Exception;

	public abstract CodeableConcept getActivity();

	protected void prepareBasicData(Provenance fhirResource, UriInfo context, HttpHeaders headers, String locationPath, Identifier identifier) throws Exception {

		try {
			//URI location = response.getLocation();
			URI location = new URI(locationPath);
			String locationStr = "";
			fhirResource.setId(UUIDUtil.getUUID());
			List<Reference> target = new ArrayList<Reference>();
			if (location != null) {
				locationStr = location.toString();
			}
			target.add(new Reference(locationStr));
			fhirResource.setTarget(target);
			Date currentDate = new Date();
			fhirResource.setRecorded(currentDate);

			fhirResource.setActivity(getActivity());

			List<ProvenanceAgentComponent> agentList = new ArrayList<>();
			agentList.add(getAgent(headers, location));
			fhirResource.setAgent(agentList);

			Reference what = new Reference(locationStr);
			if (identifier != null) {
				what.setIdentifier(identifier);
			}
			fhirResource.getEntityFirstRep().setRole(ProvenanceEntityRole.SOURCE).setWhat(what);

			// Signature
			List<Signature> signatureList = new ArrayList<Signature>();
			Signature signature = new Signature();
			signature.addType(getSignatureType());
			signature.setWhen(currentDate);
			signature.setWho(getWho(appId));
			signatureList.add(signature);
			fhirResource.setSignature(signatureList);

			log.info("prepareBasicData");
		}
		catch (FHIRException e) {
			e.printStackTrace();
			throw e;
		}

	}

	protected ProvenanceAgentComponent getAgent(HttpHeaders headers, URI location) throws Exception {

		ProvenanceAgentComponent agent = new ProvenanceAgentComponent();

		String userName = ServicesUtil.INSTANCE.getHttpHeader(headers, AuditEventConstant.HEADER_USER_NAME);
		if (StringUtils.isEmpty(userName)) {
			userName = ProvenanceResourceProxy.userName;
		}

		CodeableConcept cc = new CodeableConcept();
		Coding coding = new Coding();
		coding.setSystem("http://terminology.hl7.org/CodeSystem/provenance-participant-type");
		coding.setCode("performer");
		cc.addCoding(coding);
		agent.setType(cc);

		cc = new CodeableConcept();
		coding = new Coding();
		coding.setSystem("http://dicom.nema.org/resources/ontology/DCM");
		coding.setCode("110150");
		coding.setDisplay("Application");
		cc.addCoding(coding);

		// who
		Reference who = new Reference();
		who.setDisplay(userName);
		agent.setWho(who);

		return agent;
	}

	private Coding getSignatureType() {
		// Hard code to Author for now
		Coding type = new Coding();
		type.setSystem(SignatureTypeEnum.AUTHOR.getSystem());
		type.setCode(SignatureTypeEnum.AUTHOR.getCode());
		type.setDisplay(SignatureTypeEnum.AUTHOR.getDisplay());
		return type;
	}

	private Reference getWho(String appId) {
		Reference ref = new Reference();
		ref.setReference("Organization/" + appId);

		Identifier identifier = new Identifier();
		identifier.setValue(appId);
		ref.setIdentifier(identifier);

		return ref;
	}

}
