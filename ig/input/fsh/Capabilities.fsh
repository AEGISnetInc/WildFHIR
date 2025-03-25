Alias: $exp = http://hl7.org/fhir/StructureDefinition/capabilitystatement-expectation

RuleSet: CapabilityCommon
* status              = #active
* date                = "2025-03-24"
* publisher           = "AEGIS.net, Inc."
// * contact[0].telecom[0].system = #url
// * contact[0].telecom[0].value  = "https://hl7.org/Special/committees/fiwg/index.cfm"
* kind                = #requirements
* format[0]           = #xml
* format[1]           = #json

RuleSet: SupportResourceNoExp (resource)
* rest.resource[+].type = #{resource}

RuleSet: SupportResource (resource, expectation)
* rest.resource[+].type = #{resource}
* rest.resource[=].extension[0].url = $exp
* rest.resource[=].extension[0].valueCode = {expectation}

RuleSet: SupportProfileNoExp (profile)
// This rule set must follow a SupportResource rule set, and applies to that resource.
* rest.resource[=].supportedProfile[+] = "{profile}"

RuleSet: SupportProfile (profile, expectation)
// This rule set must follow a SupportResource rule set, and applies to that resource.
* rest.resource[=].supportedProfile[+] = "{profile}"
* rest.resource[=].supportedProfile[=].extension[0].url = $exp
* rest.resource[=].supportedProfile[=].extension[0].valueCode = {expectation}

RuleSet: SupportInteractionNoExp (interaction)
// This rule set must follow a SupportResource rule set, and applies to that resource.
* rest.resource[=].interaction[+].code = {interaction}

RuleSet: SupportInteraction (interaction, expectation)
// This rule set must follow a SupportResource rule set, and applies to that resource.
* rest.resource[=].interaction[+].code = {interaction}
* rest.resource[=].interaction[=].extension[0].url = $exp
* rest.resource[=].interaction[=].extension[0].valueCode = {expectation}

RuleSet: SupportSearchParamNoExp (name, canonical, type)
// This rule set must follow a SupportResource rule set, and applies to that resource.
* rest.resource[=].searchParam[+].name = "{name}"
* rest.resource[=].searchParam[=].definition = "{canonical}"
* rest.resource[=].searchParam[=].type = {type}

RuleSet: SupportSearchParam (name, canonical, type, expectation)
// This rule set must follow a SupportResource rule set, and applies to that resource.
* rest.resource[=].searchParam[+].name = "{name}"
* rest.resource[=].searchParam[=].definition = "{canonical}"
* rest.resource[=].searchParam[=].type = {type}
* rest.resource[=].searchParam[=].extension[0].url = $exp
* rest.resource[=].searchParam[=].extension[0].valueCode = {expectation}

RuleSet: SupportOperationNoExp (name, canonical)
// This rule set must follow a SupportResource rule set, and applies to that resource.
* rest.resource[=].operation[+].name = "{name}"
* rest.resource[=].operation[=].definition = "{canonical}"

RuleSet: SupportOperation (name, canonical, expectation)
// This rule set must follow a SupportResource rule set, and applies to that resource.
* rest.resource[=].operation[+].name = "{name}"
* rest.resource[=].operation[=].definition = "{canonical}"
* rest.resource[=].operation[=].extension[0].url = $exp
* rest.resource[=].operation[=].extension[0].valueCode = {expectation}

RuleSet: GlobalOperationNoExp (name, canonical)
// This rule set defines a global operation.
* rest.operation[+].name = "{name}"
* rest.operation[=].definition = "{canonical}"

Instance:      CapabilityWildFHIRServerR4
InstanceOf:    CapabilityStatement
Usage:         #definition
Title:         "WildFHIR R4 Server Capability Statement"
Description:   "CapabilityStatement describing the minimal required capabilities of a FHIR Server supporting the WildFHIR Community Edition R4 custom operations functionality."
* insert ResourceCommonR4
* id            = "wildfhirce-server-r4"
* name          = "CapabilityWildFHIRServerR4"
* url           = "http://wildfhir.aegis.net/ig/wildfhirce/CapabilityStatement/wildfhirce-server-r4"
* description   = "CapabilityStatement describing the minimal required capabilities of a FHIR Server supporting the WildFHIR Community Edition R4 custom operations functionality."
* implementationGuide = "http://wildfhir.aegis.net/ig/wildfhirce/ImplementationGuide/net.aegis.wildfhir.ig.wildfhirce"
* insert CapabilityCommon

* rest[+].mode  = #server

* insert SupportResourceNoExp(Patient)
* insert SupportOperationNoExp(purge, http://wildfhir.aegis.net/ig/wildfhirce/OperationDefinition/wildfhir-operation-patient-purge)

* insert GlobalOperationNoExp(convert-format, http://wildfhir.aegis.net/ig/wildfhirce/OperationDefinition/wildfhir-operation-convert-format)
* insert GlobalOperationNoExp(load-examples, http://wildfhir.aegis.net/ig/wildfhirce/OperationDefinition/wildfhir-operation-load-examples)
* insert GlobalOperationNoExp(purge-all, http://wildfhir.aegis.net/ig/wildfhirce/OperationDefinition/wildfhir-operation-purge-all)
