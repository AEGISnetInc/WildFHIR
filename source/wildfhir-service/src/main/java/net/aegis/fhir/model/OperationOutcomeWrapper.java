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

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.OperationOutcome.OperationOutcomeIssueComponent;
import org.hl7.fhir.r4.model.StringType;

/**
 * This is a wrapper to the OperationOutcome class to provide helper method access to assist
 * in the display of the internal values.
 *
 * @author richard.ettema
 *
 */
public class OperationOutcomeWrapper {

	private OperationOutcome operationOutcome;
	private List<OperationOutcomeIssueWrapper> issue;

	/**
	 * Constructor to initialize OperationOutcome
	 *
	 * @param operationOutcome
	 */
	public OperationOutcomeWrapper(OperationOutcome operationOutcome) {
		super();
		this.setOperationOutcome(operationOutcome);
	}

	public OperationOutcome getOperationOutcome() {
		return operationOutcome;
	}

	public void setOperationOutcome(OperationOutcome operationOutcome) {
		if (operationOutcome != null) {
			this.operationOutcome = operationOutcome;

			// Extract and set each issue
			for (OperationOutcomeIssueComponent operationOutcomeIssueComponent : operationOutcome.getIssue()) {
				OperationOutcomeIssueWrapper issue = new OperationOutcomeIssueWrapper(operationOutcomeIssueComponent);
				this.getIssue().add(issue);
			}
		}
	}

	public List<OperationOutcomeIssueWrapper> getIssue() {
		if (issue == null) {
			issue = new ArrayList<OperationOutcomeIssueWrapper>();
		}
		return issue;
	}

	public void setIssue(List<OperationOutcomeIssueWrapper> issue) {
		this.issue = issue;
	}

	/**
	 * Inner class to represent each OperationOutcomeIssueComponent
	 *
	 * @author richard.ettema
	 *
	 */
	public class OperationOutcomeIssueWrapper {

		private OperationOutcomeIssueComponent operationOutcomeIssueComponent;

		private String severity;
		private String code;
		private String details;
		private String diagnostics;
		private String location;

		/**
		 * Constructor to initialize OperationOutcomeIssueComponent
		 *
		 * @param operationOutcomeIssueComponent
		 */
		public OperationOutcomeIssueWrapper(OperationOutcomeIssueComponent operationOutcomeIssueComponent) {
			super();
			this.setOperationOutcomeIssueComponent(operationOutcomeIssueComponent);
		}

		public OperationOutcomeIssueComponent getOperationOutcomeIssueComponent() {
			return operationOutcomeIssueComponent;
		}

		public void setOperationOutcomeIssueComponent(OperationOutcomeIssueComponent operationOutcomeIssueComponent) {
			if (operationOutcomeIssueComponent != null) {
				this.operationOutcomeIssueComponent = operationOutcomeIssueComponent;

				// Extract and set severity
				if (operationOutcomeIssueComponent.hasSeverity()) {
					this.setSeverity(operationOutcomeIssueComponent.getSeverity().getDisplay());
				}
				else {
					this.setSeverity("NOT FOUND");
				}

				// Extract and set code
				if (operationOutcomeIssueComponent.hasCode()) {
					this.setCode(operationOutcomeIssueComponent.getCode().getDisplay());
				}
				else {
					this.setCode("NOT FOUND");
				}

				// Extract and set details
				if (operationOutcomeIssueComponent.hasDetails() && operationOutcomeIssueComponent.getDetails().hasText() && !operationOutcomeIssueComponent.getDetails().getText().isEmpty()) {
					this.setDetails(operationOutcomeIssueComponent.getDetails().getText());
				}
				else {
					this.setDetails(null);
				}

				// Extract and set diagnostics
				if (operationOutcomeIssueComponent.hasDiagnostics() && !operationOutcomeIssueComponent.getDiagnostics().isEmpty()) {
					this.setDiagnostics(operationOutcomeIssueComponent.getDiagnostics());
				}
				else {
					this.setDiagnostics(null);
				}

				// Extract and set location
				if (operationOutcomeIssueComponent.hasLocation()) {
					StringBuilder sbLocation = new StringBuilder("");

					for (StringType location : operationOutcomeIssueComponent.getLocation()) {
						if (sbLocation.length() > 2) {
							sbLocation.append("; ");
						}
						sbLocation.append(location.getValue());
					}
					this.setLocation(sbLocation.toString());
				}
				else {
					this.setLocation(null);
				}
			}
		}

		public String getSeverity() {
			return severity;
		}

		public void setSeverity(String severity) {
			this.severity = severity;
		}

		public String getCode() {
			return code;
		}

		public void setCode(String code) {
			this.code = code;
		}

		public String getDetails() {
			return details;
		}

		public void setDetails(String details) {
			this.details = details;
		}

		public String getDiagnostics() {
			return diagnostics;
		}

		public void setDiagnostics(String diagnostics) {
			this.diagnostics = diagnostics;
		}

		public String getLocation() {
			return location;
		}

		public void setLocation(String location) {
			this.location = location;
		}

	}

}
