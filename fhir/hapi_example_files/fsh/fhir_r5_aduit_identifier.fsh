/*##############################################################################
# Type:       FSH-File for an FHIR® Example
# About:      Converted AuditEvent example
##############################################################################*/

Instance:    HL7ATCoreAuditEventExample02
InstanceOf:  AuditEvent
Description: "Converted AuditEvent example from JSON"
Usage:       #example

* id = ""
* text.status = "generated"

* category[0].coding[0].system = "http://terminology.hl7.org/CodeSystem/audit-event-category"
* category[0].coding[0].code = #security
* category[0].coding[0].display = "Security"

* code.coding[0].system = "http://snomed.info/sct"
* code.coding[0].code = #239005
* code.coding[0].display = "SNOMED Code"
* code.text = "SNOMED Code"

* type.coding[0].system = "http://terminology.hl7.org/CodeSystem/audit-event-type"
* type.coding[0].code = #rest
* type.coding[0].display = "Restful Operation"

* occurredDateTime = "2025-10-24T10:00:00Z"

* basedOn[0].reference = "ServiceRequest/4"
* encounter.reference = "Encounter/3"
* patient.identifier.system = "system": "http://example.org/patient"
* patient.identifier.value = "pat-1"

* agent[0].type.coding[0].system = "http://terminology.hl7.org/CodeSystem/extra-agent-type"
* agent[0].type.coding[0].code = #humanuser
* agent[0].type.coding[0].display = "Human User"
* agent[0].who.reference = "Practitioner/2"

* source.site = "Hospital System"
* source.observer.reference = "Device/5"
