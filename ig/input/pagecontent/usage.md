
### Usage

#### Patient Purge

An example for the use of the Patient/[id]/$purge operation is in the context of TestScript test executions where the workflow of the TestScript requires a clean recreation of known patient data on the destination WildFHIR CE test server.

##### Workflow Pattern

Here is a simple workflow pattern of TestScript steps that illustrate this scenario:

1. **Setup**

   `Purge all data for a known Patient to insure all clinical history and current instances are removed from the target test system.`

1. **Test 1**

   `Recreate the original data for the known Patient in order to provide a known starting data context for subsequent tests.`

1. **Subsequent Test(s)**

   `Execute and evaluate operations based on the recreated original Patient data.`

##### Example TestScript

An example of the above workflow pattern has been defined in the following TestScripts:

* [WildFHIR CE R4 TestScript Patient/[id]/$purge Usage Example JSON](TestScript-wildfhircer4patientpurgeusageexamplejson.html)
* [WildFHIR CE R4 TestScript Patient/[id]/$purge Usage Example XML](TestScript-wildfhircer4patientpurgeusageexamplexml.html)

