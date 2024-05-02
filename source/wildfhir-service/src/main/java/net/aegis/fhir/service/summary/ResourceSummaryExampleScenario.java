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

import org.hl7.fhir.r4.model.ExampleScenario;
import org.hl7.fhir.r4.model.ExampleScenario.ExampleScenarioActorComponent;
import org.hl7.fhir.r4.model.ExampleScenario.ExampleScenarioInstanceComponent;
import org.hl7.fhir.r4.model.ExampleScenario.ExampleScenarioInstanceContainedInstanceComponent;
import org.hl7.fhir.r4.model.ExampleScenario.ExampleScenarioProcessComponent;
import org.hl7.fhir.r4.model.ExampleScenario.ExampleScenarioProcessStepAlternativeComponent;
import org.hl7.fhir.r4.model.ExampleScenario.ExampleScenarioProcessStepComponent;
import org.hl7.fhir.r4.model.ExampleScenario.ExampleScenarioProcessStepOperationComponent;
import org.hl7.fhir.r4.model.Resource;

/**
 * @author richard.ettema
 *
 */
public class ResourceSummaryExampleScenario extends ResourceSummaryProxy {

	/* (non-Javadoc)
	 * @see net.aegis.fhir.service.summary.ResourceSummaryProxy#generateSummary(org.hl7.fhir.instance.model.Resource)
	 */
	@Override
	public Resource generateSummary(Resource resource) throws Exception {

		ExampleScenario summary = null;

		try {
			// Cast original resource to expected type
			ExampleScenario original = (ExampleScenario) resource;

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
			summary.setCopyright(null);
			summary.setPurpose(null);
			summary.setActor(null);
			summary.setInstance(null);
			if (summary.hasProcess()) {
				for (ExampleScenarioProcessComponent process : summary.getProcess()) {
					setSummaryProcessComponent(process);
				}
			}
			summary.setWorkflow(null);
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

		ExampleScenario summary = null;

		try {
			// Cast original resource to expected type
			ExampleScenario original = (ExampleScenario) resource;

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

		ExampleScenario summary = null;

		try {
			// Cast original resource to expected type
			ExampleScenario original = (ExampleScenario) resource;

			// Instantiate summary resource
			summary = new ExampleScenario();

			// Copy text-only DomainResource elements
			copyTextOnlyDomainResourceElements(original, summary);

			// Set Meta Security Tag SUBSETTED
			setMetaTagSubsetted(summary);

			// Copy mandatory Resource Type elements
			summary.setStatus(original.getStatus());
			ExampleScenarioActorComponent summaryActor = null;
			for (ExampleScenarioActorComponent actor : original.getActor()) {
				summaryActor = new ExampleScenarioActorComponent();
				summaryActor.setActorId(actor.getActorId());
				summaryActor.setType(actor.getType());
				summary.addActor(summaryActor);
			}
			ExampleScenarioInstanceComponent summaryInstance = null;
			for (ExampleScenarioInstanceComponent instance : original.getInstance()) {
				summaryInstance = new ExampleScenarioInstanceComponent();
				summaryInstance.setResourceId(instance.getResourceId());
				summaryInstance.setResourceType(instance.getResourceType());
				summaryInstance.setVersion(instance.getVersion());

				ExampleScenarioInstanceContainedInstanceComponent summaryContainedInstance;
				for (ExampleScenarioInstanceContainedInstanceComponent containedInstance : instance.getContainedInstance()) {
					summaryContainedInstance = new ExampleScenarioInstanceContainedInstanceComponent();
					summaryContainedInstance.setResourceId(containedInstance.getResourceId());
					summaryInstance.addContainedInstance(summaryContainedInstance);
				}

				summary.addInstance(summaryInstance);
			}
			if (original.hasProcess()) {
				for (ExampleScenarioProcessComponent originalProcess : original.getProcess()) {
					ExampleScenarioProcessComponent summaryProcess = getSummaryProcessComponent(originalProcess);
					if (summaryProcess != null) {
						summary.addProcess(summaryProcess);
					}
				}
			}

		} catch (Exception e) {
			// Exception caught
			e.printStackTrace();
			throw e;
		}

		return summary;
	}

	private void setSummaryProcessComponent(ExampleScenarioProcessComponent process) {
		if (process != null) {
			process.setDescription(null);
			process.setPreConditions(null);
			process.setPostConditions(null);
			if (process.hasStep()) {
				for (ExampleScenarioProcessStepComponent processStep : process.getStep()) {
					setSummaryProcessStepComponent(processStep);
				}
			}
		}
	}

	private void setSummaryProcessStepComponent(ExampleScenarioProcessStepComponent processStep) {
		if (processStep != null) {
			if (processStep.hasProcess()) {
				for (ExampleScenarioProcessComponent stepProcess : processStep.getProcess()) {
					setSummaryProcessComponent(stepProcess);
				}
			}
			processStep.setPauseElement(null);
			processStep.setOperation(null);
			processStep.setAlternative(null);
		}
	}

	private ExampleScenarioProcessComponent getSummaryProcessComponent(ExampleScenarioProcessComponent process) {
		ExampleScenarioProcessComponent summaryProcess = null;

		if (process != null) {
			summaryProcess = new ExampleScenarioProcessComponent();

			summaryProcess.setTitle(process.getTitle());

			if (process.hasStep()) {
				for (ExampleScenarioProcessStepComponent processStep : process.getStep()) {
					ExampleScenarioProcessStepComponent summaryProcessStep = getSummaryProcessStepComponent(processStep);
					if (summaryProcessStep != null) {
						summaryProcess.addStep(summaryProcessStep);
					}
				}
			}
		}

		return summaryProcess;
	}

	private ExampleScenarioProcessStepComponent getSummaryProcessStepComponent(ExampleScenarioProcessStepComponent processStep) {
		ExampleScenarioProcessStepComponent summaryProcessStep = null;

		if (processStep != null) {
			if (processStep.hasProcess() || processStep.hasOperation()) {
				summaryProcessStep = new ExampleScenarioProcessStepComponent();

				if (processStep.hasProcess()) {
					for (ExampleScenarioProcessComponent processStepProcess : processStep.getProcess()) {
						processStepProcess = getSummaryProcessComponent(processStepProcess);
						if (processStepProcess != null) {
							summaryProcessStep.addProcess(processStepProcess);
						}
					}
				}

				if (processStep.hasOperation()) {
					ExampleScenarioProcessStepOperationComponent processStepOperation = new ExampleScenarioProcessStepOperationComponent();
					processStepOperation.setNumber(processStep.getOperation().getNumber());
					summaryProcessStep.setOperation(processStepOperation);
				}

				if (processStep.hasAlternative()) {
					ExampleScenarioProcessStepAlternativeComponent summaryAlternative;
					for (ExampleScenarioProcessStepAlternativeComponent alternative : processStep.getAlternative()) {
						if (alternative.hasStep()) {
							summaryAlternative = new ExampleScenarioProcessStepAlternativeComponent();
							for (ExampleScenarioProcessStepComponent alternativeStep : alternative.getStep()) {
								ExampleScenarioProcessStepComponent summaryAlternativeProcessStep = getSummaryProcessStepComponent(alternativeStep);
								if (summaryAlternativeProcessStep != null) {
									summaryAlternative.addStep(summaryAlternativeProcessStep);
								}
							}
							summaryProcessStep.addAlternative(summaryAlternative);
						}
					}
				}
			}
		}

		return summaryProcessStep;
	}

}
