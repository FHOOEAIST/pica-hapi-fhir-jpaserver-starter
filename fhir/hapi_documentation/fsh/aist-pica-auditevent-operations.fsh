/*##############################################################################
# Type:       FSH-File for FHIR® Operations
# About:      AIST PICA Operations for AuditEvents
# Created by: FH Upper Austria
##############################################################################*/

// ============================================================================
// Operation: Transform AuditEvents to DFG
// ============================================================================
Instance:       AISTPICAAuditEventToDFG
InstanceOf:     OperationDefinition
Id:             aist-pica-operation-auditevent-dfg
Title:          "AIST PICA Operation: Transform AuditEvents to DFG"
Description:    """
Transforms AuditEvents into a DFG log. Allows filtering AuditEvents by date,
patient, actor, patient visit, conformance code, or grouping before transformation.
"""
* url = "AuditEvent/$dfg"
* version = "1.0.0"
* name = "AuditEventToDFG"
* status = #active
* kind = #operation
* date = "2025-05-13"
* publisher = "FH Upper Austria"
* contact.name = "FH Upper Austria"
* code = #dfg
* comment = "Implemented for R5 AuditEvents"
* system = false
* type = true
* instance = false
* resource = "AuditEvent"

// Parameters
* parameter[+].name = "start"
* parameter[=].use = #in
* parameter[=].min = 0
* parameter[=].max = "1"
* parameter[=].documentation = "Filters AuditEvents by occurredDateTime start. Events occurring after the start date are excluded."
* parameter[=].type = #dateTime

* parameter[+].name = "end"
* parameter[=].use = #in
* parameter[=].min = 0
* parameter[=].max = "1"
* parameter[=].documentation = "Filters AuditEvents by occurredDateTime end. Events occurring before the end date are excluded."
* parameter[=].type = #dateTime

* parameter[+].name = "core"
* parameter[=].use = #in
* parameter[=].min = 0
* parameter[=].max = "*"
* parameter[=].documentation = "Filters AuditEvents by AuditEvent.patient.reference or AuditEvent.patient.identifier.value. Only events matching one of the listed patients are included."
* parameter[=].type = #string

* parameter[+].name = "actor"
* parameter[=].use = #in
* parameter[=].min = 0
* parameter[=].max = "*"
* parameter[=].documentation = "Filters AuditEvents by AuditEvent.encounter.reference. Only events matching one of the referenced actors are included."
* parameter[=].type = #string

* parameter[+].name = "patientvisit"
* parameter[=].use = #in
* parameter[=].min = 0
* parameter[=].max = "*"
* parameter[=].documentation = "Filters AuditEvents by AuditEvent.practitioner.reference. Only events matching one of the referenced patient visits are included."
* parameter[=].type = #string

* parameter[+].name = "conformance"
* parameter[=].use = #in
* parameter[=].min = 0
* parameter[=].max = "*"
* parameter[=].documentation = "Filters AuditEvents by AuditEvent.code.text or AuditEvent.code.coding. Only events matching one of the referenced conformance codes are included."
* parameter[=].type = #string

* parameter[+].name = "grouping"
* parameter[=].use = #in
* parameter[=].min = 0
* parameter[=].max = "1"
* parameter[=].documentation = "Filters events based on the first code display. Only events matching the given display value are included."
* parameter[=].type = #string

* parameter[+].name = "result"
* parameter[=].use = #out
* parameter[=].min = 1
* parameter[=].max = "1"
* parameter[=].documentation = "Returns the transformed AuditEvents as a DFG log."
* parameter[=].type = #string


// ============================================================================
// Operation: Filter AuditEvents by Time
// ============================================================================
Instance:       AISTPICAAuditEventFilterByTime
InstanceOf:     OperationDefinition
Id:             aist-pica-operation-auditevent-filterbytime
Title:          "AIST PICA Operation: Time Filter AuditEvents"
Description:    """
Filters AuditEvents according to the date. Can define a time interval
or a single start/end filter to select relevant events.
"""
* url = "AuditEvent/$filterByTime"
* version = "1.0.0"
* name = "TimeFilter"
* status = #active
* kind = #operation
* date = "2025-05-13"
* publisher = "FH Upper Austria"
* contact.name = "FH Upper Austria"
* code = #filterByTime
* comment = "Implemented for R5 AuditEvents"
* system = false
* type = true
* instance = false
* resource = "AuditEvent"

// Parameters
* parameter[+].name = "start"
* parameter[=].use = #in
* parameter[=].min = 0
* parameter[=].max = "1"
* parameter[=].documentation = "Filters AuditEvents by occurredDateTime start. Events occurring after the start date are excluded."
* parameter[=].type = #dateTime

* parameter[+].name = "end"
* parameter[=].use = #in
* parameter[=].min = 0
* parameter[=].max = "1"
* parameter[=].documentation = "Filters AuditEvents by occurredDateTime end. Events occurring before the end date are excluded."
* parameter[=].type = #dateTime

* parameter[+].name = "result"
* parameter[=].use = #out
* parameter[=].min = 1
* parameter[=].max = "1"
* parameter[=].documentation = "Returns the filtered AuditEvents."
* parameter[=].type = #string


// ============================================================================
// Operation: Transform AuditEvents to OCEL
// ============================================================================
Instance:       AISTPICAAuditEventToOCEL
InstanceOf:     OperationDefinition
Id:             aist-pica-operation-auditevent-ocel
Title:          "AIST PICA Operation: Transform AuditEvents to OCEL"
Description:    """
Transforms AuditEvents into an OCEL log. Allows filtering AuditEvents by date,
patient, actor, patient visit, or conformance code before transformation.
"""
* url = "AuditEvent/$ocel"
* version = "1.0.0"
* name = "AuditEventToOCEL"
* status = #active
* kind = #operation
* date = "2025-05-13"
* publisher = "FH Upper Austria"
* contact.name = "FH Upper Austria"
* code = #ocel
* comment = "Implemented for R5 AuditEvents"
* system = false
* type = true
* instance = false
* resource = "AuditEvent"

// Parameters – same pattern
* parameter[+].name = "start"
* parameter[=].use = #in
* parameter[=].min = 0
* parameter[=].max = "1"
* parameter[=].documentation = "Filters AuditEvents by occurredDateTime start. Events occurring after the given start date are excluded."
* parameter[=].type = #dateTime

* parameter[+].name = "end"
* parameter[=].use = #in
* parameter[=].min = 0
* parameter[=].max = "1"
* parameter[=].documentation = "Filters AuditEvents by occurredDateTime end. Events occurring before the given end date are excluded."
* parameter[=].type = #dateTime

* parameter[+].name = "core"
* parameter[=].use = #in
* parameter[=].min = 0
* parameter[=].max = "*"
* parameter[=].documentation = "Filters AuditEvents by AuditEvent.patient.reference or AuditEvent.patient.identifier.value. Only events matching one of the listed patients are included."
* parameter[=].type = #string

* parameter[+].name = "actor"
* parameter[=].use = #in
* parameter[=].min = 0
* parameter[=].max = "*"
* parameter[=].documentation = "Filters AuditEvents by AuditEvent.encounter.reference. Only events matching one of the referenced actors are included."
* parameter[=].type = #string

* parameter[+].name = "patientvisit"
* parameter[=].use = #in
* parameter[=].min = 0
* parameter[=].max = "*"
* parameter[=].documentation = "Filters AuditEvents by AuditEvent.practitioner.reference. Only events matching one of the referenced patient visits are included."
* parameter[=].type = #string

* parameter[+].name = "conformance"
* parameter[=].use = #in
* parameter[=].min = 0
* parameter[=].max = "*"
* parameter[=].documentation = "Filters AuditEvents by AuditEvent.code.text or AuditEvent.code.coding. Only events matching one of the referenced conformance codes are included."
* parameter[=].type = #string

* parameter[+].name = "result"
* parameter[=].use = #out
* parameter[=].min = 1
* parameter[=].max = "1"
* parameter[=].documentation = "Returns the transformed AuditEvents as an OCEL log."
* parameter[=].type = #string


// ============================================================================
// Operation: Transform AuditEvents to XES
// ============================================================================
Instance:       AISTPICAAuditEventToXES
InstanceOf:     OperationDefinition
Id:             aist-pica-operation-auditevent-xes
Title:          "AIST PICA Operation: Transform AuditEvents to XES"
Description:    """
Transforms AuditEvents into an XES log. Allows filtering AuditEvents by date,
patient, actor, patient visit, or conformance code before transformation.
"""
* url = "AuditEvent/$xes"
* version = "1.0.0"
* name = "AuditEventToXES"
* status = #active
* kind = #operation
* date = "2025-05-13"
* publisher = "FH Upper Austria"
* contact.name = "FH Upper Austria"
* code = #xes
* comment = "Implemented for R5 AuditEvents"
* system = false
* type = true
* instance = false
* resource = "AuditEvent"

// Parameters
* parameter[+].name = "start"
* parameter[=].use = #in
* parameter[=].min = 0
* parameter[=].max = "1"
* parameter[=].documentation = "Filters events by occurredDateTime.start. Events after start date are excluded."
* parameter[=].type = #dateTime

* parameter[+].name = "end"
* parameter[=].use = #in
* parameter[=].min = 0
* parameter[=].max = "1"
* parameter[=].documentation = "Filters events by occurredDateTime.end. Events before end date are excluded."
* parameter[=].type = #dateTime

* parameter[+].name = "core"
* parameter[=].use = #in
* parameter[=].min = 0
* parameter[=].max = "*"
* parameter[=].documentation = "Filters events by patient reference or identifier. Only matching events remain."
* parameter[=].type = #string

* parameter[+].name = "actor"
* parameter[=].use = #in
* parameter[=].min = 0
* parameter[=].max = "*"
* parameter[=].documentation = "Filters events by encounter reference. Only matching events remain."
* parameter[=].type = #string

* parameter[+].name = "patientvisit"
* parameter[=].use = #in
* parameter[=].min = 0
* parameter[=].max = "*"
* parameter[=].documentation = "Filters events by practitioner reference. Only matching events remain."
* parameter[=].type = #string

* parameter[+].name = "conformance"
* parameter[=].use = #in
* parameter[=].min = 0
* parameter[=].max = "*"
* parameter[=].documentation = "Filters events by code text or coding. Only matching events remain."
* parameter[=].type = #string


// ============================================================================
// Add-* Profile Operations (short pattern repeated)
// ============================================================================
Instance:       AISTPICAAuditEventAddActorProfile
InstanceOf:     OperationDefinition
Id:             aist-pica-operation-auditevent-addactorprofile
Title:          "AIST PICA Operation: Add Actor Profile AuditEvents"
Description:    "Extends AuditEvents with the 'aist-pica-auditevent-actor' profile."
* url = "AuditEvent/$addActorProfile"
* version = "1.0.0"
* name = "AddActorProfile"
* status = #active
* kind = #operation
* date = "2025-09-12"
* publisher = "FH Upper Austria"
* contact.name = "FH Upper Austria"
* code = #addActorProfile
* comment = "Implemented for R4/R5 AuditEvents"
* system = false
* type = true
* instance = false
* resource = "AuditEvent"

* parameter[+].name = "result"
* parameter[=].use = #out
* parameter[=].min = 1
* parameter[=].max = "1"
* parameter[=].documentation = "Returns the AuditEvents extended with the actor profile."
* parameter[=].type = #string


Instance:       AISTPICAAuditEventAddCarePathwayProfile
InstanceOf:     OperationDefinition
Id:             aist-pica-operation-auditevent-addcarepathwayprofile
Title:          "AIST PICA Operation: Add CarePathway Profile AuditEvents"
Description:    "Extends AuditEvents with the 'aist-pica-auditevent-carepathway' profile."
* url = "AuditEvent/$addCarePathwayProfile"
* version = "1.0.0"
* name = "AddCarePathwayProfile"
* status = #active
* kind = #operation
* date = "2025-09-12"
* publisher = "FH Upper Austria"
* contact.name = "FH Upper Austria"
* code = #addCarePathwayProfile
* comment = "Implemented for R4/R5 AuditEvents"
* system = false
* type = true
* instance = false
* resource = "AuditEvent"

* parameter[+].name = "result"
* parameter[=].use = #out
* parameter[=].min = 1
* parameter[=].max = "1"
* parameter[=].documentation = "Returns the AuditEvents extended with the carepathway profile."
* parameter[=].type = #string


Instance:       AISTPICAAuditEventAddConformanceProfile
InstanceOf:     OperationDefinition
Id:             aist-pica-operation-auditevent-addconformanceprofile
Title:          "AIST PICA Operation: Add Conformance Profile AuditEvents"
Description:    "Extends AuditEvents with the 'aist-pica-auditevent-conformance' profile."
* url = "AuditEvent/$addConformanceProfile"
* version = "1.0.0"
* name = "AddConformanceProfile"
* status = #active
* kind = #operation
* date = "2025-09-12"
* publisher = "FH Upper Austria"
* contact.name = "FH Upper Austria"
* code = #addConformanceProfile
* comment = "Implemented for R4/R5 AuditEvents"
* system = false
* type = true
* instance = false
* resource = "AuditEvent"

* parameter[+].name = "result"
* parameter[=].use = #out
* parameter[=].min = 1
* parameter[=].max = "1"
* parameter[=].documentation = "Returns the AuditEvents extended with the conformance profile."
* parameter[=].type = #string


Instance:       AISTPICAAuditEventAddCoreProfile
InstanceOf:     OperationDefinition
Id:             aist-pica-operation-auditevent-addcoreprofile
Title:          "AIST PICA Operation: Add Core Profile AuditEvents"
Description:    "Extends AuditEvents with the 'aist-pica-auditevent-core' profile."
* url = "AuditEvent/$addCoreProfile"
* version = "1.0.0"
* name = "AddCoreProfile"
* status = #active
* kind = #operation
* date = "2025-09-12"
* publisher = "FH Upper Austria"
* contact.name = "FH Upper Austria"
* code = #addCoreProfile
* comment = "Implemented for R4/R5 AuditEvents"
* system = false
* type = true
* instance = false
* resource = "AuditEvent"

* parameter[+].name = "result"
* parameter[=].use = #out
* parameter[=].min = 1
* parameter[=].max = "1"
* parameter[=].documentation = "Returns the AuditEvents extended with the core profile."
* parameter[=].type = #string


Instance:       AISTPICAAuditEventAddPatientVisitProfile
InstanceOf:     OperationDefinition
Id:             aist-pica-operation-auditevent-addpatientvisitprofile
Title:          "AIST PICA Operation: Add PatientVisit Profile AuditEvents"
Description:    "Extends AuditEvents with the 'aist-pica-auditevent-patientvisit' profile."
* url = "AuditEvent/$addPatientVisitProfile"
* version = "1.0.0"
* name = "AddPatientVisitProfile"
* status = #active
* kind = #operation
* date = "2025-09-12"
* publisher = "FH Upper Austria"
* contact.name = "FH Upper Austria"
* code = #addPatientVisitProfile
* comment = "Implemented for R4/R5 AuditEvents"
* system = false
* type = true
* instance = false
* resource = "AuditEvent"

* parameter[+].name = "result"
* parameter[=].use = #out
* parameter[=].min = 1
* parameter[=].max = "1"
* parameter[=].documentation = "Returns the AuditEvents extended with the patientvisit profile."
* parameter[=].type = #string