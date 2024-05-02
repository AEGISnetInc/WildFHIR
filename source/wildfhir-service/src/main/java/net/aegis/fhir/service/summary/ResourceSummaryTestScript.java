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
package net.aegis.fhir.service.summary;

import org.hl7.fhir.r4.model.ContactDetail;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.TestScript;
import org.hl7.fhir.r4.model.TestScript.SetupActionAssertComponent;
import org.hl7.fhir.r4.model.TestScript.SetupActionComponent;
import org.hl7.fhir.r4.model.TestScript.SetupActionOperationComponent;
import org.hl7.fhir.r4.model.TestScript.TeardownActionComponent;
import org.hl7.fhir.r4.model.TestScript.TestActionComponent;
import org.hl7.fhir.r4.model.TestScript.TestScriptFixtureComponent;
import org.hl7.fhir.r4.model.TestScript.TestScriptMetadataCapabilityComponent;
import org.hl7.fhir.r4.model.TestScript.TestScriptMetadataComponent;
import org.hl7.fhir.r4.model.TestScript.TestScriptMetadataLinkComponent;
import org.hl7.fhir.r4.model.TestScript.TestScriptSetupComponent;
import org.hl7.fhir.r4.model.TestScript.TestScriptTeardownComponent;
import org.hl7.fhir.r4.model.TestScript.TestScriptTestComponent;
import org.hl7.fhir.r4.model.TestScript.TestScriptVariableComponent;

/**
 * @author richard.ettema
 *
 */
public class ResourceSummaryTestScript extends ResourceSummaryProxy {

	/* (non-Javadoc)
	 * @see net.aegis.fhir.service.summary.ResourceSummaryProxy#generateSummary(org.hl7.fhir.instance.model.Resource)
	 */
	@Override
	public Resource generateSummary(Resource resource) throws Exception {

		TestScript summary = null;

		try {
			// Cast original resource to expected type
			TestScript original = (TestScript) resource;

			// Instantiate summary resource
			summary = new TestScript();

//			// Copy original resource and remove text
//			summary = original.copy();
//			((Resource)original).copyValues(summary);
//
//			// Remove non-summary Resource elements
//			removeNonSummaryResourceElements(summary);
//
//			// Remove non-summary DomainResource elements
//			removeNonSummaryDomainResourceElements(summary);
//
//			// Remove Resource Type non-summary data elements

			// Copy summary Resource elements
			summary.setId(original.getId());
			summary.setMeta(original.getMeta());
			summary.setImplicitRules(original.getImplicitRules());

			// Copy summary DomainResource elements
			// None

			// Copy summary Resource Type elements
			summary.setUrl(original.getUrl());
			summary.setIdentifier(original.getIdentifier());
			summary.setVersion(original.getVersion());
			summary.setName(original.getName());
			summary.setTitle(original.getTitle());
			summary.setStatus(original.getStatus());
			summary.setExperimental(original.getExperimental());
			summary.setDate(original.getDate());
			summary.setPublisher(original.getPublisher());
			ContactDetail summaryContact;
			for (ContactDetail contact : original.getContact()) {
				summaryContact = new ContactDetail();
				summaryContact.setName(contact.getName());
				summaryContact.setTelecom(contact.getTelecom());
				summary.addContact(summaryContact);
			}
			summary.setUseContext(original.getUseContext());
			summary.setJurisdiction(original.getJurisdiction());

			// Set Meta Security Tag SUBSETTED
			setMetaTagSubsetted(summary);
		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		}

		return summary;
	}

	/* (non-Javadoc)
	 * @see net.aegis.fhir.service.summary.ResourceSummaryProxy#generateDataSummary(org.hl7.fhir.instance.model.Resource)
	 */
	@Override
	public Resource generateDataSummary(Resource resource) throws Exception {

		TestScript summary = null;

		try {
			// Cast original resource to expected type
			TestScript original = (TestScript) resource;

			// Copy original resource and remove text
			summary = original.copy();
			((Resource)original).copyValues(summary);

			// Remove text element
			summary.setText(null);

			// Set Meta Security Tag SUBSETTED
			setMetaTagSubsetted(summary);
		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		}

		return summary;
	}

	/* (non-Javadoc)
	 * @see net.aegis.fhir.service.summary.ResourceSummaryProxy#generateTextSummary(org.hl7.fhir.instance.model.Resource)
	 */
	@Override
	public Resource generateTextSummary(Resource resource) throws Exception {

		TestScript summary = null;

		try {
			// Cast original resource to expected type
			TestScript original = (TestScript) resource;

			// Instantiate summary resource
			summary = new TestScript();

			// Copy text-only DomainResource elements
			copyTextOnlyDomainResourceElements(original, summary);

			// Set Meta Security Tag SUBSETTED
			setMetaTagSubsetted(summary);

			// Copy mandatory Resource Type elements
			summary.setUrl(original.getUrl());
			summary.setName(original.getName());
			summary.setStatus(original.getStatus());
			summary.setOrigin(original.getOrigin());
			summary.setDestination(original.getDestination());
			if (original.hasMetadata()) {
				TestScriptMetadataComponent summaryMetadata = new TestScriptMetadataComponent();
				TestScriptMetadataLinkComponent summaryMetadataLink = null;
				for (TestScriptMetadataLinkComponent link : original.getMetadata().getLink()) {
					summaryMetadataLink = new TestScriptMetadataLinkComponent();
					summaryMetadataLink.setUrl(link.getUrl());
					summaryMetadata.addLink(summaryMetadataLink);
				}
				TestScriptMetadataCapabilityComponent summaryMetadataCapability = null;
				for (TestScriptMetadataCapabilityComponent capability : original.getMetadata().getCapability()) {
					summaryMetadataCapability = new TestScriptMetadataCapabilityComponent();
					summaryMetadataCapability.setRequired(capability.getRequired());
					summaryMetadataCapability.setValidated(capability.getValidated());
					summaryMetadataCapability.setCapabilities(capability.getCapabilities());
					summaryMetadata.addCapability(summaryMetadataCapability);
				}
				summary.setMetadata(summaryMetadata);
			}
			TestScriptFixtureComponent summaryFixture = null;
			for (TestScriptFixtureComponent fixture : original.getFixture()) {
				summaryFixture = new TestScriptFixtureComponent();
				summaryFixture.setAutocreate(fixture.getAutocreate());
				summaryFixture.setAutodelete(fixture.getAutodelete());
				summary.addFixture(summaryFixture);
			}
			TestScriptVariableComponent summaryVariable = null;
			for (TestScriptVariableComponent variable : original.getVariable()) {
				summaryVariable = new TestScriptVariableComponent();
				summaryVariable.setName(variable.getName());
				summary.addVariable(summaryVariable);
			}
			if (original.hasSetup()) {
				TestScriptSetupComponent summarySetup = new TestScriptSetupComponent();
				SetupActionComponent summarySetupAction = null;
				for (SetupActionComponent action : original.getSetup().getAction()) {
					summarySetupAction = new SetupActionComponent();
					if (action.hasOperation()) {
						SetupActionOperationComponent summarySetupActionOperation = new SetupActionOperationComponent();
						summarySetupActionOperation.setEncodeRequestUrl(action.getOperation().getEncodeRequestUrl());
						summarySetupActionOperation.setRequestHeader(action.getOperation().getRequestHeader());
						summarySetupAction.setOperation(summarySetupActionOperation);
					}
					if (action.hasAssert()) {
						SetupActionAssertComponent summarySetupActionAssert = new SetupActionAssertComponent();
						summarySetupActionAssert.setWarningOnly(action.getAssert().getWarningOnly());
						summarySetupAction.setAssert(summarySetupActionAssert);
					}
					summarySetup.addAction(summarySetupAction);
				}
				summary.setSetup(summarySetup);
			}
			TestScriptTestComponent summaryTest = null;
			for (TestScriptTestComponent test : original.getTest()) {
				summaryTest = new TestScriptTestComponent();
				TestActionComponent summaryTestAction = null;
				for (TestActionComponent action : test.getAction()) {
					summaryTestAction = new TestActionComponent();
					if (action.hasOperation()) {
						SetupActionOperationComponent summaryTestActionOperation = new SetupActionOperationComponent();
						summaryTestActionOperation.setEncodeRequestUrl(action.getOperation().getEncodeRequestUrl());
						summaryTestActionOperation.setRequestHeader(action.getOperation().getRequestHeader());
						summaryTestAction.setOperation(summaryTestActionOperation);
					}
					if (action.hasAssert()) {
						SetupActionAssertComponent summaryTestActionAssert = new SetupActionAssertComponent();
						summaryTestActionAssert.setWarningOnly(action.getAssert().getWarningOnly());
						summaryTestAction.setAssert(summaryTestActionAssert);
					}

					summaryTest.addAction(summaryTestAction);
				}
				summary.addTest(summaryTest);
			}
			if (original.hasTeardown()) {
				TestScriptTeardownComponent summaryTeardown = new TestScriptTeardownComponent();
				TeardownActionComponent summaryTeardownAction = null;
				for (TeardownActionComponent action : original.getTeardown().getAction()) {
					summaryTeardownAction = new TeardownActionComponent();
					if (action.hasOperation()) {
						SetupActionOperationComponent summaryTeardownActionOperation = new SetupActionOperationComponent();
						summaryTeardownActionOperation.setEncodeRequestUrl(action.getOperation().getEncodeRequestUrl());
						summaryTeardownActionOperation.setRequestHeader(action.getOperation().getRequestHeader());
						summaryTeardownAction.setOperation(summaryTeardownActionOperation);
					}
					summaryTeardown.addAction(summaryTeardownAction);
				}
				summary.setTeardown(summaryTeardown);
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		}

		return summary;
	}

}
