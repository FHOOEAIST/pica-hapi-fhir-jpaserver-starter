# PetriNetGenerator API Documentation

**Location:** /bpmn/
**Version:** 1.0.0
**Author:** FH Upper Austria
**Date:** 2025-07-10

---

## Endpoints

### 1. POST /convert-bpmn

**Description:** Converts BPMN XML to PNML. Saves the BPMN file, runs Python conversion, and returns PNML content.

**Request Example**

```http
POST /convert-bpmn
Content-Type: application/json

{
  "xml": "<definitions>...</definitions>",
  "filename": "process1"
}
```

**Response Example**

```
<?xml version="1.0"?>
<pnml>...</pnml>
```

---

### 2. GET /files/bpmn

**Description:** Lists all uploaded BPMN files as HTML links and triggers batch conversion.

**Request**

```http
GET /files/bpmn
```

**Response Example:** HTML page with links to `.bpmn` files.

---

### 3. GET /files/bpmn/{filename}

**Description:** Returns raw BPMN XML for the file and optionally triggers conversion.

**Request**

```http
GET /files/bpmn/process1.bpmn
```

**Response Example**

```xml
<definitions>...</definitions>
```

---

### 4. GET /files/pnml

**Description:** Lists all generated PNML files as HTML links.

**Request**

```http
GET /files/pnml
```

**Response Example:** HTML page with links to `.pnml` files.

---

### 5. GET /files/pnml/{filename}

**Description:** Returns raw PNML XML content.

**Request**

```http
GET /files/pnml/process1.pnml
```

**Response Example**

```xml
<pnml>...</pnml>
```

---

### 6. GET /files/pnml/{filename}/petrinet

**Description:** Parses PNML file and returns Petri net structure as JSON.

**Request**

```http
GET /files/pnml/process1.pnml/petrinet
```

**Response Example**

```json
{
  "places": ["p1","p2","p3"],
  "transitions": ["t1","t2"],
  "arcs": [
    {"source":"p1","target":"t1"},
    {"source":"t1","target":"p2"}
  ]
}
```

---

### 7. GET /view

**Description:** HTML viewer for BPMN → PNML conversion (XML input).

---

### 8. GET /view2

**Description:** Alternate HTML viewer for BPMN → PNML conversion (file input).

---

### 9. GET /view/petrinet

**Description:** HTML page for Petri net visualization.

---

## Storage Locations

* BPMN files: `fhir/upload/bpmn/`
* PNML files: `fhir/upload/pnml/`

---

## Notes

* POST `/convert-bpmn` expects JSON with `xml` and `filename`.
* Conversion process: saves BPMN, runs Python script, returns PNML.
* GET `/files/pnml/{filename}/petrinet` returns JSON Petri net structure.
* HTML endpoints provide viewers for BPMN/PNML and Petri nets.
