Instance: WildFHIRCER4PatientPurgeUsageExampleJSON
InstanceOf: TestScript
Title: "WildFHIR CE R4 TestScript Patient/[id]/$purge Usage Example JSON"
Usage: #definition
* insert TSMetadata(wildfhircer4patientpurgeusageexamplejson, WildFHIRCER4PatientPurgeUsageExampleJSON, http://wildfhir.aegis.net/ig/wildfhirce, 2025-03-24, "Example TestScript to illustrate the use of the WildFHIR CE R4 Patient/[id]/$purge operation using JSON Format. Data for the known Patient id 'wildfhirce-patient-purge' is purged in the setup followed by a single test that recreates the original data via PUT update/create operations.")
* insert TSOrigin(1)
* insert TSDestination(1)

* insert TSFixture(patient-update,"./Patient-wildfhirce-patient-purge.json")
* insert TSFixture(observation-update,"./Observation-wildfhirce-observation-purge.json")

* insert TSVariablePath("patientId",".id","patient-update")
* insert TSVariablePath("observationId",".id","observation-update")

// Setup
* insert TSSetupOperationResource(#process-message, #Patient, #json, 1, 1, "Purge all data for the known Patient id 'wildfhirce-patient-purge' to insure all history and current instances are removed")
* setup.action[=].operation.params = "/${patientId}"
* insert TSSetupAssert("Confirm that the returned HTTP response code is success.",false,#response)
* setup.action[=].assert.value = "200"

// Test - Recreate Patient Data
* insert TSTest(RecreatePatientData, "Recreate the original data for the Patient id 'wildfhirce-patient-purge' using JSON Format on destination server.")

* insert TSTestOperationResource(#update,#Patient,#json,1,1,"Create the known Patient via an update\, order matters for referential integrity")
* test[=].action[=].operation.params = "/${patientId}"
* test[=].action[=].operation.sourceId = "patient-update"
* insert TSTestAssert("Confirm that the returned HTTP response code is success.",false,#response)
* test[=].action[=].assert.operator = #in
* test[=].action[=].assert.value = "200,201"

* insert TSTestOperationResource(#update,#Observation,#json,1,1,"Create the known Observation via an update\, order matters for referential integrity")
* test[=].action[=].operation.params = "/${observationId}"
* test[=].action[=].operation.sourceId = "observation-update"
* insert TSTestAssert("Confirm that the returned HTTP response code is success.",false,#response)
* test[=].action[=].assert.operator = #in
* test[=].action[=].assert.value = "200,201"

Instance: WildFHIRCER4PatientPurgeUsageExampleXML
InstanceOf: TestScript
Title: "WildFHIR CE R4 TestScript Patient/[id]/$purge Usage Example XML"
Usage: #definition
* insert TSMetadata(wildfhircer4patientpurgeusageexamplexml, WildFHIRCER4PatientPurgeUsageExampleXML, http://wildfhir.aegis.net/ig/wildfhirce, 2025-03-24, "Example TestScript to illustrate the use of the WildFHIR CE R4 Patient/[id]/$purge operation using XML Format. Data for the known Patient id 'wildfhirce-patient-purge' is purged in the setup followed by a single test that recreates the original data via PUT update/create operations.")
* insert TSOrigin(1)
* insert TSDestination(1)

* insert TSFixture(patient-update,"./Patient-wildfhirce-patient-purge.xml")
* insert TSFixture(observation-update,"./Observation-wildfhirce-observation-purge.xml")

* insert TSVariablePath("patientId","/Patient/id","patient-update")
* insert TSVariablePath("observationId","/Observation/id","observation-update")

// Setup
* insert TSSetupOperationResource(#process-message, #Patient, #xml, 1, 1, "Purge all data for the known Patient id 'wildfhirce-patient-purge' to insure all history and current instances are removed")
* setup.action[=].operation.params = "/${patientId}"
* insert TSSetupAssert("Confirm that the returned HTTP response code is success.",false,#response)
* setup.action[=].assert.value = "200"

// Test - Recreate Patient Data
* insert TSTest(RecreatePatientData, "Recreate the original data for the Patient id 'wildfhirce-patient-purge' using XML Format on destination server.")

* insert TSTestOperationResource(#update,#Patient,#xml,1,1,"Create the known Patient via an update\, order matters for referential integrity")
* test[=].action[=].operation.params = "/${patientId}"
* test[=].action[=].operation.sourceId = "patient-update"
* insert TSTestAssert("Confirm that the returned HTTP response code is success.",false,#response)
* test[=].action[=].assert.operator = #in
* test[=].action[=].assert.value = "200,201"

* insert TSTestOperationResource(#update,#Observation,#xml,1,1,"Create the known Observation via an update\, order matters for referential integrity")
* test[=].action[=].operation.params = "/${observationId}"
* test[=].action[=].operation.sourceId = "observation-update"
* insert TSTestAssert("Confirm that the returned HTTP response code is success.",false,#response)
* test[=].action[=].assert.operator = #in
* test[=].action[=].assert.value = "200,201"
