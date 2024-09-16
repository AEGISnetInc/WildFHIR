
RuleSet: ResourceJurisdiction
* jurisdiction = http://unstats.un.org/unsd/methods/m49/m49.htm#001

RuleSet: ResourceCommonR4
* jurisdiction = http://unstats.un.org/unsd/methods/m49/m49.htm#001
* fhirVersion = #4.0.1

// Operation common definitions
RuleSet:       OperationCommon
* jurisdiction        = http://unstats.un.org/unsd/methods/m49/m49.htm#001
* status              = #active
* date                = "2024-09-16"
* publisher           = "AEGIS.net, Inc."
* contact[0].telecom[0].system = #url
* contact[0].telecom[0].value  = "https://www.aegis.net"
* affectsState        = false
* version             = "0.1.0"
* kind                = #operation
* extension[0].url          = "http://hl7.org/fhir/StructureDefinition/structuredefinition-fmm"
* extension[0].valueInteger = 0
* extension[1].url          = "http://hl7.org/fhir/StructureDefinition/structuredefinition-standards-status"
* extension[1].valueCode    = #trial-use
