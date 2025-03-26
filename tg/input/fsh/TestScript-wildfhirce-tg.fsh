Instance: WildFHIRCER4PatientPurgeJSON
InstanceOf: TestScript
Title: "WildFHIR CE R4 TestScript Patient/[id]/$purge JSON"
Usage: #definition
* insert TSMetadata(wildfhircer4patientpurgejson, WildFHIRCER4PatientPurgeJSON, http://wildfhir.aegis.net/tg/wildfhirce, 2025-03-24, "Test the WildFHIR CE R4 Patient/[id]/$purge operation using JSON Format")
* insert TSScope("http://wildfhir.aegis.net/ig/wildfhirce/ActorDefinition/wildfhirce-responder")
* insert TSScope("http://wildfhir.aegis.net/ig/wildfhirce/OperationDefinition/wildfhirce-operation-patient-purge")
* insert TSOrigin(1)
* insert TSDestination(1)

* insert TSFixture(patient-update,"./Patient-wildfhirce-patient-purge.json")
* insert TSFixture(observation-update,"./Observation-wildfhirce-observation-purge.json")

* insert TSVariablePath("updatePatientId",".id","patient-update")
* insert TSVariablePath("updateObservationId",".id","observation-update")

// Setup
* insert TSSetupOperationResource(#delete, #Observation, #json, 1, 1, "Delete the known Observation to insure it does not exist\, order matters for referential integrity")
* setup.action[=].operation.params = "[[/${updateObservationId}]]"
* insert TSSetupAssert("Confirm that the returned HTTP response code is success.",false,#response)
* setup.action[=].assert.operator = #in
* setup.action[=].assert.value = "200,204,404"

* insert TSSetupOperationResource(#delete,#Patient,#json,1,1,"Delete the known Patient to insure it does not exist")
* setup.action[=].operation.params = "[[/${updatePatientId}]]"
* insert TSSetupAssert("Confirm that the returned HTTP response code is success.",false,#response)
* setup.action[=].assert.operator = #in
* setup.action[=].assert.value = "200,204,404"

* insert TSSetupOperationResource(#update,#Patient,#json,1,1,"Create the known Patient via an update\, order matters for referential integrity")
* setup.action[=].operation.params = "[[/${updatePatientId}]]"
* setup.action[=].operation.sourceId = "patient-update"
* insert TSSetupAssert("Confirm that the returned HTTP response code is success.",false,#response)
* setup.action[=].assert.operator = #in
* setup.action[=].assert.value = "200,201"

* insert TSSetupOperationResource(#update,#Observation,#json,1,1,"Create the known Observation via an update\, order matters for referential integrity")
* setup.action[=].operation.params = "[[/${updateObservationId}]]"
* setup.action[=].operation.sourceId = "observation-update"
* insert TSSetupAssert("Confirm that the returned HTTP response code is success.",false,#response)
* setup.action[=].assert.operator = #in
* setup.action[=].assert.value = "200,201"

// Test Patient Purge
* insert TSTest(PatientPurge, "Test the Patient/[id]/$purge operation using JSON Format on destination server and assert successful response.")
* insert TSTestOperationResource(#process-message, #Patient, #json, 1, 1, "Patient $purge operation")
* test[=].action[=].operation.params = "[[/${updatePatientId}/$purge]]"
* insert TSTestAssertWithProp("Confirm that the returned response code is 200 OK.", false, #response, responseCode, "200")
* insert TSTestAssertWithProp("Confirm that the returned response payload is an OperationOutcome resource.", false, #response, resource, #OperationOutcome)
* insert TSTestAssert("Confirm that the returned OperationOutcome contains an infomation issue.", false, #response)
* test[=].action[=].assert.expression = "OperationOutcome.issue.where(severity = 'infomation').exists()"

* insert TSTestOperationResource(#read, #Observation, #json, 1, 1, "Attempt to read purged Observation")
* test[=].action[=].operation.params = "[[/${updateObservationId}]]"
* insert TSTestAssertWithProp("Confirm that the returned response code is 404 Not Found.", false, #response, responseCode, "404")

* insert TSTestOperationResource(#read, #Patient, #json, 1, 1, "Attempt to read purged Patient")
* test[=].action[=].operation.params = "[[/${updatePatientId}]]"
* insert TSTestAssertWithProp("Confirm that the returned response code is 404 Not Found.", false, #response, responseCode, "404")

Instance: WildFHIRCER4PatientPurgeXML
InstanceOf: TestScript
Title: "WildFHIR CE R4 TestScript Patient/[id]/$purge XML"
Usage: #definition
* insert TSMetadata(wildfhircer4patientpurgexml, WildFHIRCER4PatientPurgeXML, http://wildfhir.aegis.net/tg/wildfhirce, 2025-03-24, "Test the WildFHIR CE R4 Patient/[id]/$purge operation using XML Format")
* insert TSScope("http://wildfhir.aegis.net/ig/wildfhirce/ActorDefinition/wildfhirce-responder")
* insert TSScope("http://wildfhir.aegis.net/ig/wildfhirce/OperationDefinition/wildfhirce-operation-patient-purge")
* insert TSOrigin(1)
* insert TSDestination(1)

* insert TSFixture(patient-update,"./Patient-wildfhirce-patient-purge.xml")
* insert TSFixture(observation-update,"./Observation-wildfhirce-observation-purge.xml")

* insert TSVariablePath("updatePatientId","/Patient/id","patient-update")
* insert TSVariablePath("updateObservationId","/Observation/id","observation-update")

// Setup
* insert TSSetupOperationResource(#delete,#Observation,#xml,1,1,"Delete the known Observation to insure it does not exist\, order matters for referential integrity")
* setup.action[=].operation.params = "[[/${updateObservationId}]]"
* insert TSSetupAssert("Confirm that the returned HTTP response code is success.",false,#response)
* setup.action[=].assert.operator = #in
* setup.action[=].assert.value = "200,204,404"

* insert TSSetupOperationResource(#delete,#Patient,#xml,1,1,"Delete the known Patient to insure it does not exist\, order matters for referential integrity")
* setup.action[=].operation.params = "[[/${updatePatientId}]]"
* insert TSSetupAssert("Confirm that the returned HTTP response code is success.",false,#response)
* setup.action[=].assert.operator = #in
* setup.action[=].assert.value = "200,204,404"

* insert TSSetupOperationResource(#update,#Patient,#xml,1,1,"Create the known Patient via an update\, order matters for referential integrity")
* setup.action[=].operation.params = "[[/${updatePatientId}]]"
* setup.action[=].operation.sourceId = "patient-update"
* insert TSSetupAssert("Confirm that the returned HTTP response code is success.",false,#response)
* setup.action[=].assert.operator = #in
* setup.action[=].assert.value = "200,201"

* insert TSSetupOperationResource(#update,#Observation,#xml,1,1,"Create the known Observation via an update\, order matters for referential integrity")
* setup.action[=].operation.params = "[[/${updateObservationId}]]"
* setup.action[=].operation.sourceId = "observation-update"
* insert TSSetupAssert("Confirm that the returned HTTP response code is success.",false,#response)
* setup.action[=].assert.operator = #in
* setup.action[=].assert.value = "200,201"

// Test Patient Purge
* insert TSTest(PatientPurge, "Test the Patient/[id]/$purge operation using XML Format on destination server and assert successful response.")
* insert TSTestOperationResource(#process-message, #Patient, #xml, 1, 1, "Patient $purge operation")
* test[=].action[=].operation.params = "[[/${updatePatientId}/$purge]]"
* insert TSTestAssertWithProp("Confirm that the returned response code is 200 OK.", false, #response, responseCode, "200")
* insert TSTestAssertWithProp("Confirm that the returned response payload is an OperationOutcome resource.", false, #response, resource, #OperationOutcome)
* insert TSTestAssert("Confirm that the returned OperationOutcome contains an infomation issue.", false, #response)
* test[=].action[=].assert.expression = "OperationOutcome.issue.where(severity = 'infomation').exists()"

* insert TSTestOperationResource(#read, #Observation, #xml, 1, 1, "Attempt to read purged Observation")
* test[=].action[=].operation.params = "[[/${updateObservationId}]]"
* insert TSTestAssertWithProp("Confirm that the returned response code is 404 Not Found.", false, #response, responseCode, "404")

* insert TSTestOperationResource(#read, #Patient, #xml, 1, 1, "Attempt to read purged Patient")
* test[=].action[=].operation.params = "[[/${updatePatientId}]]"
* insert TSTestAssertWithProp("Confirm that the returned response code is 404 Not Found.", false, #response, responseCode, "404")
