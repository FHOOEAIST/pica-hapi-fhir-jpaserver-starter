/*##############################################################################
# Type:       FSH-File for an FHIR® Example
# About:      Example for the HL7 Austria FHIR® Core Profile for AuditEvent.
##############################################################################*/

Instance:    HL7ATCoreAuditEventExample02
InstanceOf:  AuditEvent
Description: "Converted AuditEvent example from JSON"
Usage:       #example

* status = #completed

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
* patient.reference = "Patient/1"

* agent[0].who.reference = "Practitioner/2"
* agent[0].type.coding[0].system = "http://terminology.hl7.org/CodeSystem/extra-agent-type"
* agent[0].type.coding[0].code = #humanuser
* agent[0].type.coding[0].display = "Human User"

* source.site = "Hospital System"
* source.observer.reference = "Device/5"
