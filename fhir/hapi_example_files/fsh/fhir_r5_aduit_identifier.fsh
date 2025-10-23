/*##############################################################################
# Type:       FSH-File for an FHIR® Example
# About:      Example for the HL7 Austria FHIR® Core Profile for AuditEvent.
# Created by: HL7® Austria, TC FHIR®
##############################################################################*/

Instance:    HL7ATCoreAuditEventExample01
InstanceOf:  AuditEvent
Description: "Example for the usage of the HL7 AT Core AuditEvent Profile"
Usage:       #example

* id = "urn:uuid:00f4931b-925b-4b3a-a72f-5b12a0d5f43f"
* text.status = "generated"

* category[0].coding[0].system = "http://hl7.org/fhir/restful-interaction"
* category[0].coding[0].code = #transaction
* category[0].coding[0].display = "transaction"

* code.coding[0].system = "http://snomed.info/sct"
* code.coding[0].code = #239005
* code.coding[0].display = "SNOMED Code"
* code.text = "SNOMED Code"

* action = "C"
* occurredDateTime = "1988-08-02T05:16:11+02:00"
* recorded = "1988-08-02T05:16:11+02:00"

* outcome.code.system = "http://terminology.hl7.org/CodeSystem/audit-event-outcome"
* outcome.code.code = #0
* outcome.code.display = "Success"

* patient.identifier.system = "http://localhost:8080/fhir/Patient/1"
* patient.identifier.value = "18"

* encounter.reference = "urn:uuid:00f4931b-925b-4b3/a-a72f-5b12a0d5f43f"

* agent[0].type.coding[0].system = "http://terminology.hl7.org/CodeSystem/v3-RoleClass"
* agent[0].type.coding[0].code = #PRF
* agent[0].type.coding[0].display = "Practitioner"

* agent[0].who.reference = "Practitioner/2"
* agent[0].who.display = "Dr Adam Careful"
* agent[0].requestor = true
