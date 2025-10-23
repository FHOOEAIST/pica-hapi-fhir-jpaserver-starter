# CSV Structure for AuditEvent

## **Endpoint**
- GET /csv/convert/filename.csv

---

## **Mandatory Fields**
- `code_code`  
- `occurred` (date/time of the event)  
- `recorded` (timestamp of recording)  
- **(If Outcome exists)**: `outcome_code`  
- `patient_reference` or `patient_identifier`  
- `agent_who_reference`  
- `source_observer`  

---

## **General Structure**
- Each row = 1 AuditEvent  
- Each column = 1 field  
- Nested structures → separate columns  
- Multiple entries within a field → `|`  
- Multiple values within an element → `;`  

---

## **Delimiter Rules**
- `|` → multiple elements of an array (e.g., multiple Agents, Entities, Details, Extensions)  
- `;` → multiple values within an element (e.g., roles, policies, extensions)  

---

## **Field-Level Structure**

### 1. Code
- `code_code` → mandatory  
- `code_system`, `code_display`, `code_text` → optional  

### 2. Agents
- `agent_who_reference` → mandatory  
- `agent_who_display`, `agent_type_code/system/display` → optional  
- `agent_role_code/system/display` → optional (`;` for multiple roles)  
- `agent_requestor` → optional (boolean)  
- `agent_location`, `agent_policy` → optional (`;` for multiple)  
- `agent_auth_code/system/display` → optional (`;` for multiple)  

### 3. Patient
- `patient_reference` or `patient_identifier` → mandatory  
- `patient_display`, `patient_type` → optional  

### 4. AuditEvent Metadata
- `id`, `action` → optional  
- `recorded`, `occurred`, `source_observer` → mandatory  
- `source_id`, `source_site`, `source_type_code/system/display` → optional  
- `basedOn` → optional (`|`)  
- `encounter`, `text_status`, `severity`, `language`, `implicit_rules` → optional  

---

### 5. Extensions / ModifierExtensions
**Applies to all:**
- `extension`, `modifier_extension`  
- `source_extension`, `source_modifierExtension`  
- `outcome_extension`, `outcome_modifierExtension`  
- `entity_extension`, `entity_modifierExtension`  

**Rules:**
- Each **Value[x]** type → separate column (e.g., `extension_valueString`, `extension_valueBoolean`, `modifierExtension_valueDateTime`, …)  
- Column content = `url;value[x]`  
- Multiple extensions of the same type → `|`  
- Multiple values in one extension → `;`  

**Notes:**
- Multiple values for extensions or modifierExtensions are separated by `|`.  
  Example: `http://ex.org/ext1;Hello|http://ex.org/ext2;World`  
- Complex types like `valueRange`, `valueRatio`, `valuePeriod` must be formatted exactly or values will not import correctly.  
- Empty fields are ignored; no extension entry is created.  

---

### VALUE_FIELDS – Format Specifications

| Field                     | Meaning / Type                          | CSV Format / Example                               |
|---------------------------|----------------------------------------|---------------------------------------------------|
| `valueString`             | Text                                    | Simple string, e.g., `Hello World`               |
| `valueBoolean`            | True/False                              | `true` or `false`                                 |
| `valueInteger`            | Integer                                 | `42`                                              |
| `valueQuantity`           | Number (quantity)                        | Decimal, e.g., `12.5`                             |
| `valueCodeableConcept`    | Coded text                              | Text string, e.g., `Admin`                        |
| `valueTime`               | Time                                    | `HH:mm:ss`, e.g., `14:30:00`                      |
| `valueDateTime`           | Date and time                           | ISO 8601, e.g., `2025-08-28T09:00:00Z`           |
| `valueRange`              | Range of values                         | `low-high`, e.g., `10-20`                         |
| `valueRatio`              | Ratio                                   | `numerator:denominator`, e.g., `2:3`             |
| `valuePeriod`             | Period                                  | `start|end`, e.g., `2025-08-01T00:00:00Z|2025-08-28T23:59:59Z` |
| `valueBase64Binary`       | Binary data (Base64)                    | Base64-encoded, e.g., `SGVsbG8=`                 |

---

### 6. Outcome (optional)
- `outcome_code` → mandatory if outcome exists  
- `outcome_system/display` → optional  
- **Outcome detail value[x]** → separate columns (like extensions), e.g., `outcome_extension_valueString`  

---

### 7. Authorization (optional)
- `authorization_code/system/display/text` → optional (`|` for multiple)  

---

### 8. Category (optional)
- `category_code/system/version/display` → optional (`|` for multiple)  

---

### 9. Entities (optional)
- `entity_id`, `entity_what`, `entity_role`, `entity_securityLabel`, `entity_query` → optional (`|` for multiple entities, `;` for multiple roles/labels)  
- **Entity detail value[x]** → separate columns (like extensions), e.g., `entity_extension_valueString`  

---

## **Example CSV Row**
```csv
id,code_code,code_system,code_display,code_text,agent_who_reference,agent_who_display,agent_type,agent_role,recorded,occurred,patient_reference,patient_display,patient_type,source_observer
1,LOGIN,systemA,Login,User login,Practitioner/123,Dr. Smith,User,Admin,2025-08-20T10:00:00Z,2025-08-20T09:59:00Z,Patient/456,John Doe,Patient,Server1
```
## **Assumptions for References**

All references in AuditEvents assume that the referenced resource already exists on the server.
Missing resources (e.g., Patient/123 or Practitioner/456) will cause upload errors.

- **Note**: CSV should only contain valid, existing references.
- **Alternative**: Using full URL as a reference bypasses local server existence checks.