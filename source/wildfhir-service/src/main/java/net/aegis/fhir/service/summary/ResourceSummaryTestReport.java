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

import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.TestReport;
import org.hl7.fhir.r4.model.TestReport.SetupActionComponent;
import org.hl7.fhir.r4.model.TestReport.SetupActionAssertComponent;
import org.hl7.fhir.r4.model.TestReport.SetupActionOperationComponent;
import org.hl7.fhir.r4.model.TestReport.TeardownActionComponent;
import org.hl7.fhir.r4.model.TestReport.TestActionComponent;
import org.hl7.fhir.r4.model.TestReport.TestReportParticipantComponent;
import org.hl7.fhir.r4.model.TestReport.TestReportSetupComponent;
import org.hl7.fhir.r4.model.TestReport.TestReportTeardownComponent;
import org.hl7.fhir.r4.model.TestReport.TestReportTestComponent;

/**
 * @author richard.ettema
 *
 */
public class ResourceSummaryTestReport extends ResourceSummaryProxy {

	/* (non-Javadoc)
	 * @see net.aegis.fhir.service.summary.ResourceSummaryProxy#generateSummary(org.hl7.fhir.instance.model.Resource)
	 */
	@Override
	public Resource generateSummary(Resource resource) throws Exception {

		TestReport summary = null;

		try {
			// Cast original resource to expected type
			TestReport original = (TestReport) resource;

			// Copy original resource and remove text
			summary = original.copy();
			((Resource)original).copyValues(summary);

			// Remove non-summary Resource elements
			removeNonSummaryResourceElements(summary);

			// Remove non-summary DomainResource elements
			removeNonSummaryDomainResourceElements(summary);

			// Set Meta Security Tag SUBSETTED
			setMetaTagSubsetted(summary);

			// Remove Resource Type non-summary data elements
			summary.setParticipant(null);
			summary.setSetup(null);
			summary.setTest(null);
			summary.setTeardown(null);
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

		TestReport summary = null;

		try {
			// Cast original resource to expected type
			TestReport original = (TestReport) resource;

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

		TestReport summary = null;

		try {
			// Cast original resource to expected type
			TestReport original = (TestReport) resource;

			// Instantiate summary resource
			summary = new TestReport();

			// Copy text-only DomainResource elements
			copyTextOnlyDomainResourceElements(original, summary);

			// Set Meta Security Tag SUBSETTED
			setMetaTagSubsetted(summary);

			// Copy mandatory Resource Type elements
			summary.setStatus(original.getStatus());
			summary.setTestScript(original.getTestScript());
			summary.setResult(original.getResult());
			TestReportParticipantComponent summaryParticipant;
			for (TestReportParticipantComponent participant : original.getParticipant()) {
				summaryParticipant = new TestReportParticipantComponent();
				summaryParticipant.setType(participant.getType());
				summaryParticipant.setUri(participant.getUri());
				summary.addParticipant(summaryParticipant);
			}
			if (original.hasSetup()) {
				TestReportSetupComponent summarySetup = new TestReportSetupComponent();
				SetupActionComponent summarySetupAction;
				for (SetupActionComponent action : original.getSetup().getAction()) {
					summarySetupAction = new SetupActionComponent();
					if (action.hasOperation()) {
						SetupActionOperationComponent summarySetupActionOperation = new SetupActionOperationComponent();
						summarySetupActionOperation.setResult(action.getOperation().getResult());
						summarySetupAction.setOperation(summarySetupActionOperation);
					}
					if (action.hasAssert()) {
						SetupActionAssertComponent summarySetupActionAssert = new SetupActionAssertComponent();
						summarySetupActionAssert.setResult(action.getAssert().getResult());
						summarySetupAction.setAssert(summarySetupActionAssert);
					}
					summarySetup.addAction(summarySetupAction);
				}
				summary.setSetup(summarySetup);
			}
			TestReportTestComponent summaryTest;
			for (TestReportTestComponent test : original.getTest()) {
				summaryTest = new TestReportTestComponent();
				TestActionComponent summaryTestAction;
				for (TestActionComponent action : test.getAction()) {
					summaryTestAction = new TestActionComponent();
					if (action.hasOperation()) {
						SetupActionOperationComponent summaryTestActionOperation = new SetupActionOperationComponent();
						summaryTestActionOperation.setResult(action.getOperation().getResult());
						summaryTestAction.setOperation(summaryTestActionOperation);
					}
					if (action.hasAssert()) {
						SetupActionAssertComponent summaryTestActionAssert = new SetupActionAssertComponent();
						summaryTestActionAssert.setResult(action.getAssert().getResult());
						summaryTestAction.setAssert(summaryTestActionAssert);
					}
					summaryTest.addAction(summaryTestAction);
				}
				summary.addTest(summaryTest);
			}
			if (original.hasTeardown()) {
				TestReportTeardownComponent summaryTeardown = new TestReportTeardownComponent();
				TeardownActionComponent summaryTeardownAction;
				for (TeardownActionComponent action : original.getTeardown().getAction()) {
					summaryTeardownAction = new TeardownActionComponent();
					if (action.hasOperation()) {
						SetupActionOperationComponent summaryTeardownActionOperation = new SetupActionOperationComponent();
						summaryTeardownActionOperation.setResult(action.getOperation().getResult());
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
