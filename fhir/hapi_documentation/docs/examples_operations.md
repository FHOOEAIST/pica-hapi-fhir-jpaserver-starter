# Examples for AuditEvent Operations


## 1. TimeFilter

**Description:** Filters AuditEvents by date interval. You can specify `start`, `end`, or both.

**Request Example**
```http
# Only start date
GET http://localhost:8080/fhir/AuditEvent/$filterByTime?start=2025-10-20

# Only end date
GET http://localhost:8080/fhir/AuditEvent/$filterByTime?end=2025-10-25

# Both start and end
GET http://localhost:8080/fhir/AuditEvent/$filterByTime?start=2025-10-20&end=2025-10-25
```
**Response**
```json
{
  "resourceType": "Bundle",
  "type": "searchset",
  "total": 2,
  "entry": [
    {
      "resource": {
        "resourceType": "AuditEvent",
        "id": "ae1",
        "occurredDateTime": "2025-10-21T10:00:00Z"
      }
    },
    {
      "resource": {
        "resourceType": "AuditEvent",
        "id": "ae2",
        "occurredDateTime": "2025-10-22T14:30:00Z"
      }
    }
  ]
}
```
## 2. XESFilter

**Description**: Transforms AuditEvents into an XES log. You can filter by start, end, core, actor, patientvisit, conformance.

**Request Example**
```http
# Only date range
GET http://localhost:8080/fhir/AuditEvent/$xes?start=2025-10-20&end=2025-10-25

# Date range + specific patient
GET http://localhost:8080/fhir/AuditEvent/$xes?start=2025-10-20&end=2025-10-25&core=patient-123

# Multiple filters
GET http://localhost:8080/fhir/AuditEvent/$xes?start=2025-10-20&end=2025-10-25&core=patient-123&actor=actor-456&conformance=code-789
```

**Response**
```xml
<log xes.version="1.0">
    <extension name="Time" prefix="time" uri="http://www.xes-standard.org/time.xesext"/>
    <extension name="Lifecycle" prefix="lifecycle" uri="http://www.xes-standard.org/lifecycle.xesext"/>
    <extension name="Concept" prefix="concept" uri="http://www.xes-standard.org/concept.xesext"/>
    <string key="concept:name"/>
    <trace>
        <string value="18" key="concept:name"/>
        <event>
            <string value="SNOMED Code" key="concept:name"/>
            <date value="1988-08-02T05:16:11.000+02:00" key="time:timestamp"/>
            <string value="complete" key="lifecycle:transition"/>
        </event>
    </trace>
</log>
```
## 3. OCELFilter

**Description:** Transforms AuditEvents into an OCEL log. Supports the same filters as XESFilter.

**Request Example**
```http
# Only date range
GET http://localhost:8080/fhir/AuditEvent/$ocel?start=2025-10-20&end=2025-10-25

# Date range + specific actor
GET http://localhost:8080/fhir/AuditEvent/$ocel?start=2025-10-20&end=2025-10-25&actor=actor-456

# Multiple filters
GET http://localhost:8080/fhir/AuditEvent/$ocel?start=2025-10-20&end=2025-10-25&actor=actor-456&core=patient-123&conformance=code-789
```
**Response**
```xml
<log>
    <events>
        <event>
            <string value="AuditEvent/1/_history/1" key="id"/>
            <string value="SNOMED Code" key="activity"/>
            <date value="1988-08-02T05:16:11.000+02:00" key="timestamp"/>
            <list key="omap">
                <string value="urn:uuid:00f4931b-925b-4b3/a-a72f-5b12a0d5f43f" key="object-id"/>
            </list>
        </event>
    </events>
    <objects>
        <object>
            <string value="urn:uuid:00f4931b-925b-4b3/a-a72f-5b12a0d5f43f" key="id"/>
            <string key="type"/>
        </object>
    </objects>
</log>
```
## 4. DFGFilter

**Description:** Transforms AuditEvents into a DFG log. Supports filtering by `start`, `end`, `core`, `actor`, `patientvisit`, `conformance`, and `grouping`.

**Request Example**
```http
# Only date range
GET http://localhost:8080/fhir/AuditEvent/$dfg?start=2025-10-20&end=2025-10-25

# Date range + specific patient
GET http://localhost:8080/fhir/AuditEvent/$dfg?start=2025-10-20&end=2025-10-25&core=patient-123

# Multiple filters
GET http://localhost:8080/fhir/AuditEvent/$dfg?start=2025-10-20&end=2025-10-25&core=patient-123&actor=actor-456&conformance=code-789&grouping=tag-abc
```

## 5. Profile Extensions

These operations extend AuditEvents with predefined profiles.

### Add Core Profile
**Request**
```http
POST http://localhost:8080/fhir/AuditEvent/$addCoreProfile
```
**Response**
```json
{
  "resourceType": "Bundle",
  "type": "collection",
  "entry": [
    {
      "resourceType": "AuditEvent",
      "id": "ae1",
      "meta": {
        "profile": ["http://example.org/fhir/StructureDefinition/aist-pica-auditevent-core"]
      }
    }
  ]
}
```
### Add CarePathway Profile
**Request**
```http
POST http://localhost:8080/fhir/AuditEvent/$addCCarePathwayProfile
```
**Response**
```json
{
  "resourceType": "Bundle",
  "type": "collection",
  "entry": [
    {
      "resourceType": "AuditEvent",
      "id": "ae1",
      "meta": {
        "profile": ["http://example.org/fhir/StructureDefinition/aist-pica-auditevent-carepathway"]
      }
    }
  ]
}
```
### Add Actor Profile
**Request**
```http
POST http://localhost:8080/fhir/AuditEvent/$addActorProfile
```
**Response**
```json
{
  "resourceType": "Bundle",
  "type": "collection",
  "entry": [
    {
      "resourceType": "AuditEvent",
      "id": "ae1",
      "meta": {
        "profile": ["http://example.org/fhir/StructureDefinition/aist-pica-auditevent-actor"]
      }
    }
  ]
}
```
### Add Conformance Profile
**Request**
```http
POST http://localhost:8080/fhir/AuditEvent/$addConformanceProfile
```
**Response**
```json
{
  "resourceType": "Bundle",
  "type": "collection",
  "entry": [
    {
      "resourceType": "AuditEvent",
      "id": "ae1",
      "meta": {
        "profile": ["http://example.org/fhir/StructureDefinition/aist-pica-auditevent-conformance"]
      }
    }
  ]
}
```
### Add PatientVisit Profile
**Request**
```http
POST http://localhost:8080/fhir/AuditEvent/$addPatientVisitProfile
```
**Response**
```json
{
  "resourceType": "Bundle",
  "type": "collection",
  "entry": [
    {
      "resourceType": "AuditEvent",
      "id": "ae1",
      "meta": {
        "profile": ["http://example.org/fhir/StructureDefinition/aist-pica-auditevent-patientvisit"]
      }
    }
  ]
}
```
