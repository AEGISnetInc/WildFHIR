Instance:      wildfhirce-operation-convert-format
InstanceOf:    OperationDefinition
Usage:         #definition
Title:         "AEGIS WildFHIR Community Edition R4 Convert Format"
Description:   "This is the WildFHIR Community Edition R4 Global Convert Format operation. It provides a standardized mechanism to convert a posted FHIR resource instance to a specific FHIR syntax format. The operation simply returns the FHIR resource instance in the format specified by the request HTTP Accept Header."
* id            = "wildfhirce-operation-convert-format"
* name          = "AEGISWildFHIRCER4ConvertFormat"
* description   = "This is the WildFHIR Community Edition R4 Global Convert Format operation. It provides a standardized mechanism to convert a posted FHIR resource instance to a specific FHIR syntax format. The operation simply returns the FHIR resource instance in the format specified by the request HTTP Accept Header."
* comment       = "Implemented in AEGIS WildFHIR Community Edition R4 FHIR test server - http://wildfhir.aegis.net/r4"
* insert OperationCommon
* system        = true
* type          = false
* instance      = false
* code          = #convert-format

* parameter[+].name          = #input
* parameter[=].type          = #Resource
* parameter[=].use           = #in
* parameter[=].min           = 1
* parameter[=].max           = "1"
* parameter[=].documentation = "The FHIR resource instance in the HTTP Content-Type Header FHIR syntax format."

* parameter[+].name          = #return
* parameter[=].type          = #Resource
* parameter[=].use           = #out
* parameter[=].min           = 1
* parameter[=].max           = "1"
* parameter[=].documentation = "If successful, the resource instance in the requested HTTP Accept Header FHIR syntax format.\n\nIf the operation was unsuccessful, then an OperationOutcome is returned along with a BadRequest status Code (e.g. reference integrity issue, security issue, or insufficient privileges)."


Instance:      wildfhirce-operation-load-examples
InstanceOf:    OperationDefinition
Usage:         #definition
Title:         "AEGIS WildFHIR Community Edition R4 Load Examples"
Description:   "This is the WildFHIR Community Edition R4 Global Load Examples operation. It provides a standardized mechanism to process all files in the specified directory folder in ascending alphanumeric order by filename and create/update them on the target WildFHIR server."
* id            = "wildfhirce-operation-load-examples"
* name          = "AEGISWildFHIRCER4LoadExamples"
* description   = "This is the WildFHIR Community Edition R4 Global Load Examples operation. It provides a standardized mechanism to process all files in the specified directory folder in ascending alphanumeric order by filename and create/update them on the target WildFHIR server."
* comment       = "Implemented in AEGIS WildFHIR Community Edition R4 FHIR test server - http://wildfhir.aegis.net/r4"
* insert OperationCommon
* system        = true
* type          = false
* instance      = false
* code          = #load-examples

* parameter[+].name          = #baseurl
* parameter[=].type          = #string
* parameter[=].use           = #in
* parameter[=].min           = 1
* parameter[=].max           = "1"
* parameter[=].documentation = "The baseurl value is used to insure search parameter indexing for reference search parameter values is correct for the WildFHIR server's resource repository."

* parameter[+].name          = #dirpath
* parameter[=].type          = #string
* parameter[=].use           = #in
* parameter[=].min           = 1
* parameter[=].max           = "1"
* parameter[=].documentation = "The dirpath value must point to a valid directory folder on the physical machine hosting the WildFHIR server."

* parameter[+].name          = #return
* parameter[=].type          = #string
* parameter[=].use           = #out
* parameter[=].min           = 1
* parameter[=].max           = "1"
* parameter[=].documentation = "If successful, the result parameter will contain a string value with further information about the load results (such as a count of resources that were loaded / skipped).\n\nIf unsuccessful, the result parameter will contain a string value with a reason for the failure (e.g. reference integrity issue, security issue, or insufficient privileges)."


Instance:      wildfhirce-operation-patient-purge
InstanceOf:    OperationDefinition
Usage:         #definition
Title:         "AEGIS WildFHIR Community Edition R4 Patient Purge"
Description:   "This is the WildFHIR Community Edition R4 Patient Purge operation. It provides a standardized mechanism to force a hard delete or purge of all referenced FHIR resources to a specific Patient. The operation behaves similar to the Patient $everything operation where the Patient Compartment definition is used to discover all related Patient data."
* id            = "wildfhirce-operation-patient-purge"
* name          = "AEGISWildFHIRCER4PatientPurge"
* description   = "This is the WildFHIR Community Edition R4 Patient Purge operation. It provides a standardized mechanism to force a hard delete or purge of all referenced FHIR resources to a specific Patient. The operation behaves similar to the Patient $everything operation where the Patient Compartment definition is used to discover all related Patient data."
* comment       = "Implemented in AEGIS WildFHIR Community Edition R4 FHIR test server - http://wildfhir.aegis.net/r4"
* insert OperationCommon
* resource      = #Patient
* system        = false
* type          = true
* instance      = true
* code          = #purge

* parameter[+].name          = #start
* parameter[=].type          = #date
* parameter[=].use           = #in
* parameter[=].min           = 0
* parameter[=].max           = "1"
* parameter[=].documentation = "The date range relates to care dates, not record currency dates - e.g. all records relating to care provided in a certain date range. If no start date is provided, all records prior to the end date are in scope."

* parameter[+].name          = #end
* parameter[=].type          = #date
* parameter[=].use           = #in
* parameter[=].min           = 0
* parameter[=].max           = "1"
* parameter[=].documentation = "The date range relates to care dates, not record currency dates - e.g. all records relating to care provided in a certain date range. If no end date is provided, all records subsequent to the start date are in scope."

* parameter[+].name          = #return
* parameter[=].type          = #OperationOutcome
* parameter[=].use           = #out
* parameter[=].min           = 0
* parameter[=].max           = "1"
* parameter[=].documentation = "If successful, the operation may contain an OperationOutcome with further information about the purge results (such as warnings or information messages, such as a count of records that were purged / eliminated).\n\nIf the operation was unsuccessful, then an OperationOutcome may be returned along with a BadRequest status Code (e.g. reference integrity issue, security issue, or insufficient privileges)."


Instance:      wildfhirce-operation-purge-all
InstanceOf:    OperationDefinition
Usage:         #definition
Title:         "AEGIS WildFHIR Community Edition R4 Purge All"
Description:   "This is the WildFHIR Community Edition R4 Purge All operation. It provides a standardized mechanism to force a hard delete or purge of ALL FHIR resources (and their version history)."
* id            = "wildfhirce-operation-purge-all"
* name          = "AEGISWildFHIRCER4PurgeAll"
* description   = "This is the WildFHIR Community Edition R4 Purge All operation. It provides a standardized mechanism to force a hard delete or purge of ALL FHIR resources (and their version history).\n\nThe allowed use of this operation within the AEGIS WildFHIR R4 server is controlled via a configuration setting - true, use is allowed; false, a 400 (Bad Request) with an OperationOutcome is returned."
* comment       = "Implemented in AEGIS WildFHIR Community Edition R4 FHIR test server - http://wildfhir.aegis.net/r4"
* insert OperationCommon
* system        = true
* type          = false
* instance      = false
* code          = #purge-all

* parameter[+].name          = #return
* parameter[=].type          = #OperationOutcome
* parameter[=].use           = #out
* parameter[=].min           = 1
* parameter[=].max           = "1"
* parameter[=].documentation = "If successful, the operation may contain an OperationOutcome with further information about the purge results (such as warnings or information messages, such as a count of records that were purged / eliminated).\n\nIf the operation was unsuccessful, then an OperationOutcome may be returned along with a BadRequest status Code (e.g. reference integrity issue, security issue, or insufficient privileges)."
