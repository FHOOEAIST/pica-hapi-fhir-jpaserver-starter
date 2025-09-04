package ca.uhn.fhir.jpa.starter.controller;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.r5.model.*;
import org.hl7.fhir.utilities.xhtml.NodeType;
import org.hl7.fhir.utilities.xhtml.XhtmlNode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

@RestController
@RequestMapping("/csv")


public class CSVController {


	private static final String CSV_DIR = "fhir/upload/csv/";

	private static final String AUDITEVENT_DIR = "fhir/upload/ae";

	private static final String[] VALUE_FIELDS = {
		"valueString", "valueBoolean", "valueInteger", "valueQuantity",
		"valueCodeableConcept", "valueTime", "valueDateTime", "valueRange",
		"valueRatio", "valuePeriod", "valueBase64Binary"
	};

	@GetMapping("/convert/{filename}")
	public ResponseEntity<String> convertToAuditEvent(@PathVariable String filename) {
		Path path = Paths.get(CSV_DIR, filename);
		List<AuditEvent> events = new ArrayList<>();
		List<String> errors = new ArrayList<>();

		FhirContext ctx = FhirContext.forR5();

		try (BufferedReader reader = Files.newBufferedReader(path)) {
			String[] header = reader.readLine().split(",");

			String line;
			int count = 0;
			while ((line = reader.readLine()) != null) {
				String[] values = line.split(",");
				Map<String, String> row = new HashMap<>();

				for (int i = 0; i < header.length; i++) {
					String value = i < values.length ? values[i].trim() : "";
					row.put(header[i].trim(), value);
				}

				AuditEvent ae = csv_to_auditevent(row);
				if (ae != null && ae.hasCode()) {

					String aeName = "ae_" + filename.replace(".csv", "");

					events.add(ae);

					// Serialize to JSON
					String json = ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(ae);
					String outFile = AUDITEVENT_DIR + "/" + aeName + "_" + count + ".json";
					Files.write(Paths.get(outFile), json.getBytes());
					count++;
				} else {
					errors.add("Invalid row: " + row);
				}
			}
		} catch (IOException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error reading file: " + e.getMessage());
		}

		return ResponseEntity.ok("Converted: " + events.size() + "\nSkipped: " + errors.size());
	}


	public static AuditEvent csv_to_auditevent(Map<String, String> event) {
		AuditEvent ae = new AuditEvent();

		// Required fields check
		String codeCodesRaw = event.getOrDefault("code_code", "");
		String agentsRaw = event.getOrDefault("agent_who_reference", "");
		String recordedTime = event.getOrDefault("recorded", "");
		String sourceSite = event.getOrDefault("source_observer", "");
		String occurredRaw = event.getOrDefault("occurred", "");
		String patientReference = event.getOrDefault("patient_reference", "");
		String patientIdentifier = event.getOrDefault("patient_identifier", "");

		if (codeCodesRaw.isEmpty()
			|| agentsRaw.isEmpty()
			|| recordedTime.isEmpty()
			|| occurredRaw.isEmpty()
			|| (patientReference.isEmpty() && patientIdentifier.isEmpty())
			|| sourceSite.isEmpty()) {
			System.out.println("Error - missing required element.");
			return null;
		}

		try {
			// 1. code
			parseCode(event, ae);

			// 2. agents
			parseAgents(event, ae);

			// 3. recorded
			ae.setRecorded(Date.from(Instant.parse(recordedTime)));

			// 4. occurred
			ae.setOccurred(new DateTimeType(occurredRaw));

			// 5. patient
			parsePatient(event, ae);

			// 6. source
			parseSource(event,ae);

			// 7. outcome
			parseOutcome(event, ae);

			// 8. action
			String action = event.getOrDefault("action", "");
			if (!action.isEmpty()) {
				ae.setAction(AuditEvent.AuditEventAction.fromCode(action));
			}

			// 9. severity
			String severity = event.getOrDefault("severity", "");
			if (!severity.isEmpty()) {
				ae.setSeverity(AuditEvent.AuditEventSeverity.fromCode(severity));
			}

			// 10. extensions
			parseExtensions(event, ae);

			// 11. category
			parseCategories(event, ae);

			// 12. id
			String id = event.getOrDefault("id", "");
			if (!id.isEmpty()) {
				ae.setId(id);
			}

			// 13. encounter
			String encounter = event.getOrDefault("encounter", "");
			if (!encounter.isEmpty()) {
				ae.setEncounter(new Reference(encounter));
			}

			// 14. text status (narrative)
			String textStatus = event.getOrDefault("text_status", "");
			String textDiv = event.getOrDefault("text_div", "");

			if (!textStatus.isEmpty() && !textDiv.isEmpty()) {
				Narrative nar = new Narrative();
				nar.setStatus(Narrative.NarrativeStatus.fromCode(textStatus));
				nar.setDivAsString(textDiv);
				ae.setText(nar);

			}

			// 15. Authorization
			parseAuthorization(event, ae);

			// 16. BasedOn
			parseBasedOn(event,ae);

			// 17. language
			String language = event.getOrDefault("language", "");
			if(!language.isEmpty()){
				ae.setLanguage(language);
			}

			//18. implicitRules
			String implicitRules = event.getOrDefault("implicit_rules", "");
			if(!implicitRules.isEmpty()){
				ae.setImplicitRules(implicitRules);
			}

			// 19. Entity
			parseEntities(event,ae);

			// 20. modifierExtension
			parseModifierExtensions(event,ae);


		} catch (Exception e) {
			e.printStackTrace();
		}
		return ae;
	}

	private static void parseSource(Map<String, String> event, AuditEvent ae) {
		AuditEvent.AuditEventSourceComponent source = new AuditEvent.AuditEventSourceComponent();

		// observer
		String observerRef = event.getOrDefault("source_observer", "");
		source.setObserver(new Reference(observerRef));


		// id
		String id = event.getOrDefault("source_id", "");
		if (!id.isEmpty()) {
			source.setId(id);
		}

		// extension
		String[] extensions = event.getOrDefault("source_extension", "").split(";");
		for (String ext : extensions) {
			if (!ext.isEmpty()) source.addExtension(new Extension(ext));
		}

		// modifierExtension
		String[] modifierExtensions = event.getOrDefault("source_modifierExtension", "").split(";");
		for (String modExt : modifierExtensions) {
			if (!modExt.isEmpty()) source.addModifierExtension(new Extension(modExt));
		}

		// site
		String siteRef = event.getOrDefault("source_site", "");
		if (!siteRef.isEmpty()) {
			source.setSite(new Reference(siteRef));
		}

		// type
		String[] typeCodes = event.getOrDefault("source_type_code", "").split(";");
		String[] typeSystems = event.getOrDefault("source_type_system", "").split(";");
		String[] typeDisplays = event.getOrDefault("source_type_display", "").split(";");

		for (int i = 0; i < typeCodes.length; i++) {
			if (typeCodes[i].isEmpty()) continue;
			Coding coding = new Coding().setCode(typeCodes[i]);
			if (i < typeSystems.length && !typeSystems[i].isEmpty()) coding.setSystem(typeSystems[i]);
			if (i < typeDisplays.length && !typeDisplays[i].isEmpty()) coding.setDisplay(typeDisplays[i]);
			source.addType(new CodeableConcept().addCoding(coding));
		}

		ae.setSource(source);
	}

	private static void parseBasedOn(Map<String, String> event, AuditEvent ae) {
		String[] basedOnRaw = event.getOrDefault("basedOn", "").split("\\|");
		for (String ref : basedOnRaw) {
			if (!ref.isEmpty()) {
				ae.addBasedOn(new Reference(ref));
			}
		}
	}

	private static void parseAuthorization(Map<String, String> event, AuditEvent ae) {
		String[] authCodes = event.getOrDefault("authorization_code", "").split("\\|");
		String[] authSystems = event.getOrDefault("authorization_system", "").split("\\|");
		String[] authDisplays = event.getOrDefault("authorization_display", "").split("\\|");
		String[] authTexts = event.getOrDefault("authorization_text", "").split("\\|");

		for (int i = 0; i < authCodes.length; i++) {
			if (!authCodes[i].isEmpty()){

				CodeableConcept concept = new CodeableConcept();
				Coding coding = new Coding().setCode(authCodes[i]);

				if (i < authSystems.length && !authSystems[i].isEmpty()) {
					coding.setSystem(authSystems[i]);
				}
				if (i < authDisplays.length && !authDisplays[i].isEmpty()) {
					coding.setDisplay(authDisplays[i]);
				}

				concept.addCoding(coding);

				if (i < authTexts.length && !authTexts[i].isEmpty()) {
					concept.setText(authTexts[i]);
				}

				ae.addAuthorization(concept);
			}
		}
	}

	private static void parseCode(Map<String, String> event, AuditEvent ae) {
		String code = event.getOrDefault("code_code", "");
		if (code.isEmpty()) return;


		String systems = event.getOrDefault("code_system", "");
		String displays = event.getOrDefault("code_display", "");
		String codeTextRaw = event.getOrDefault("code_text", "");

		CodeableConcept concept = new CodeableConcept();

		Coding coding = new Coding().setCode(code);
		coding.setSystem(systems);
		coding.setDisplay(displays);
		concept.addCoding(coding);

		if (!codeTextRaw.isEmpty()) concept.setText(codeTextRaw);
		ae.setCode(concept);
	}

	private static void parseAgents(Map<String, String> event, AuditEvent ae) {
		String[] whoRefs = event.getOrDefault("agent_who_reference", "").split("\\|");
		String[] whoDisplays = event.getOrDefault("agent_who_display", "").split("\\|");

		String[] agentIds = event.getOrDefault("agent_id", "").split("\\|");
		String[] typeCodes = event.getOrDefault("agent_type_code", "").split("\\|");
		String[] typeSystems = event.getOrDefault("agent_type_system", "").split("\\|");
		String[] typeDisplays = event.getOrDefault("agent_type_display", "").split("\\|");

		String[] roleCodes = event.getOrDefault("agent_role_code", "").split("\\|");
		String[] roleSystems = event.getOrDefault("agent_role_system", "").split("\\|");
		String[] roleDisplays = event.getOrDefault("agent_role_display", "").split("\\|");

		String[] requestors = event.getOrDefault("agent_requestor", "").split("\\|");
		String[] locations = event.getOrDefault("agent_location", "").split("\\|");
		String[] policies = event.getOrDefault("agent_policy", "").split("\\|");

		String[] authCodes = event.getOrDefault("agent_auth_code", "").split("\\|");
		String[] authSystems = event.getOrDefault("agent_auth_system", "").split("\\|");
		String[] authDisplays = event.getOrDefault("agent_auth_display", "").split("\\|");

		String[] extensionsRaw = event.getOrDefault("agent_extension", "").split("\\|");
		String[] modifierExtensionsRaw = event.getOrDefault("agent_modifierExtension", "").split("\\|");

		int agentCount = Math.max(whoRefs.length, agentIds.length);
		for (int i = 0; i < agentCount; i++) {
			AuditEvent.AuditEventAgentComponent agent = new AuditEvent.AuditEventAgentComponent();

			// id
			if (i < agentIds.length && !agentIds[i].isEmpty()) agent.setId(agentIds[i]);

			// who
			if (i < whoRefs.length && !whoRefs[i].isEmpty()) {
				Reference who = new Reference(whoRefs[i]);
				if (i < whoDisplays.length && !whoDisplays[i].isEmpty()) who.setDisplay(whoDisplays[i]);
				agent.setWho(who);
			}

			// type
			if (i < typeCodes.length && !typeCodes[i].isEmpty()) {
				Coding coding = new Coding().setCode(typeCodes[i]);
				if (i < typeSystems.length && !typeSystems[i].isEmpty()) coding.setSystem(typeSystems[i]);
				if (i < typeDisplays.length && !typeDisplays[i].isEmpty()) coding.setDisplay(typeDisplays[i]);
				agent.setType(new CodeableConcept().addCoding(coding));
			}

			// roles
			if (i < roleCodes.length && !roleCodes[i].isEmpty()) {
				String[] codes = roleCodes[i].split(";");
				String[] systems = i < roleSystems.length ? roleSystems[i].split(";") : new String[codes.length];
				String[] displays = i < roleDisplays.length ? roleDisplays[i].split(";") : new String[codes.length];

				for (int j = 0; j < codes.length; j++) {
					if (codes[j].isEmpty()) continue;
					Coding roleCoding = new Coding().setCode(codes[j]);
					if (j < systems.length && !systems[j].isEmpty()) roleCoding.setSystem(systems[j]);
					if (j < displays.length && !displays[j].isEmpty()) roleCoding.setDisplay(displays[j]);
					agent.addRole(new CodeableConcept().addCoding(roleCoding));
				}
			}

			// requestors
			if (i < requestors.length && !requestors[i].isEmpty()) agent.setRequestor(Boolean.parseBoolean(requestors[i]));

			// location
			if (i < locations.length && !locations[i].isEmpty()) agent.setLocation(new Reference(locations[i]));

			// policy
			if (i < policies.length && !policies[i].isEmpty()) {
				for (String policy : policies[i].split(";")) {
					if (!policy.isEmpty()) agent.addPolicy(policy);
				}
			}

			// authorization
			if (i < authCodes.length && !authCodes[i].isEmpty()) {
				String[] codes = authCodes[i].split(";");
				String[] systems = i < authSystems.length ? authSystems[i].split(";") : new String[codes.length];
				String[] displays = i < authDisplays.length ? authDisplays[i].split(";") : new String[codes.length];

				for (int j = 0; j < codes.length; j++) {
					if (codes[j].isEmpty()) continue;
					Coding authCoding = new Coding().setCode(codes[j]);
					if (j < systems.length && !systems[j].isEmpty()) authCoding.setSystem(systems[j]);
					if (j < displays.length && !displays[j].isEmpty()) authCoding.setDisplay(displays[j]);
					agent.addAuthorization(new CodeableConcept().addCoding(authCoding));
				}
			}


			// extension
			for (String field : VALUE_FIELDS) {
				String csv = event.getOrDefault("agent_extension_" + field, "");
				if (!csv.isEmpty()) {
					String[] extEntries = csv.split("\\|");
					for (String extStr : extEntries) {
						String[] parts = extStr.split(";", 2);
						if (parts.length != 2) continue;
						Extension ext = new Extension();
						ext.setUrl(parts[0]);
						setExtensionValue(ext, field, parts[1]);
						agent.addExtension(ext);
					}
				}

				// modifier extension
				csv = event.getOrDefault("agent_modifierExtension_" + field, "");
				if (!csv.isEmpty()) {
					String[] modExtEntries = csv.split("\\|");
					for (String extStr : modExtEntries) {
						String[] parts = extStr.split(";", 2);
						if (parts.length != 2) continue;
						Extension ext = new Extension();
						ext.setUrl(parts[0]);
						setExtensionValue(ext, field, parts[1]);
						agent.addModifierExtension(ext);
					}
				}
			}

			ae.addAgent(agent);
		}
	}

	private static void parseOutcome(Map<String, String> event, AuditEvent ae) {
		AuditEvent.AuditEventOutcomeComponent outcome = new AuditEvent.AuditEventOutcomeComponent();

		// id
		String id = event.getOrDefault("outcome_id", "");
		if (!id.isEmpty()) {
			outcome.setId(id);
		}

		// extension
		for (String field : VALUE_FIELDS) {
			String csv = event.getOrDefault("outcome_extension_" + field, "");
			if (!csv.isEmpty()) {
				String[] extEntries = csv.split("\\|");
				for (String extStr : extEntries) {
					String[] parts = extStr.split(";", 2);
					if (parts.length != 2) continue;
					Extension ext = new Extension();
					ext.setUrl(parts[0]);
					setExtensionValue(ext, field, parts[1]);
					outcome.addExtension(ext);
				}
			}

			// modifier extensions
			csv = event.getOrDefault("outcome_modifierExtension_" + field, "");
			if (!csv.isEmpty()) {
				String[] modExtEntries = csv.split("\\|");
				for (String extStr : modExtEntries) {
					String[] parts = extStr.split(";", 2);
					if (parts.length != 2) continue;
					Extension ext = new Extension();
					ext.setUrl(parts[0]);
					setExtensionValue(ext, field, parts[1]);
					outcome.addModifierExtension(ext);
				}
			}

			// code
			String outcomeCodeRaw = event.getOrDefault("outcome_code", "");
			String outcomeSystemRaw = event.getOrDefault("outcome_system", "");
			String outcomeDisplayRaw = event.getOrDefault("outcome_display", "");

			if ((!outcomeSystemRaw.isEmpty() || !outcomeDisplayRaw.isEmpty()) && outcomeCodeRaw.isEmpty()) {
				System.out.println("Warning: outcome code is missing, skipping outcome.");
				return; // Invalid skip outcome
			}

			if (!outcomeCodeRaw.isEmpty()) {
				Coding coding = new Coding().setCode(outcomeCodeRaw);
				if (!outcomeSystemRaw.isEmpty()) coding.setSystem(outcomeSystemRaw);
				if (!outcomeDisplayRaw.isEmpty()) coding.setDisplay(outcomeDisplayRaw);
				outcome.setCode(coding);
			} else {
				return;
			}

			// detail
			String[] detailCodes = event.getOrDefault("outcome_detail_code", "").split(";");
			String[] detailSystems = event.getOrDefault("outcome_detail_system", "").split(";");
			String[] detailDisplays = event.getOrDefault("outcome_detail_display", "").split(";");

			for (int i = 0; i < detailCodes.length; i++) {
				if (detailCodes[i].isEmpty()) continue;
				Coding detailCoding = new Coding().setCode(detailCodes[i]);
				if (i < detailSystems.length && !detailSystems[i].isEmpty()) detailCoding.setSystem(detailSystems[i]);
				if (i < detailDisplays.length && !detailDisplays[i].isEmpty()) detailCoding.setDisplay(detailDisplays[i]);
				outcome.addDetail(new CodeableConcept().addCoding(detailCoding));
			}

			ae.setOutcome(outcome);
		}
	}

	private static void parsePatient(Map<String, String> event, AuditEvent ae) {
		String patientReference = event.getOrDefault("patient_reference", "");
		String patientIdentifier = event.getOrDefault("patient_identifier", "");
		String patientDisplay = event.getOrDefault("patient_display", "");
		String patientType = event.getOrDefault("patient_type", "");

		Reference ref = null;
		if (!patientReference.isEmpty()) {
			ref = new Reference(patientReference);
			ae.setPatient(ref);
		}
		if (!patientDisplay.isEmpty()) {
			if (ref == null) {
				ref = new Reference(patientReference);
				ae.setPatient(ref);
			}
			ref.setDisplay(patientDisplay);
		}
		if (!patientIdentifier.isEmpty()) {
			if (ref == null) {
				ref = new Reference();
				ae.setPatient(ref);
			}
			Identifier id = new Identifier().setValue(patientIdentifier);
			ref.setIdentifier(id);
		}
		if (!patientType.isEmpty()) {
			if (ref == null) {
				ref = new Reference();
				ae.setPatient(ref);
			}
			ref.setType(patientType);
		}
	}

	private static void parseExtensions(Map<String, String> event, AuditEvent ae) {
		String prefix = "extension_";

		for (String field : VALUE_FIELDS) {
			String csv = event.getOrDefault(prefix + field, "");
			if (csv.isEmpty()) continue;

			String[] extEntries = csv.split("\\|"); // <-- mehrere Extensions
			for (String extStr : extEntries) {
				String[] parts = extStr.split(";", 2); // URL ; Value
				if (parts.length != 2) continue;

				Extension ext = new Extension();
				ext.setUrl(parts[0]);
				setExtensionValue(ext, field, parts[1]);

				ae.addExtension(ext);
			}
		}
	}

	private static void parseModifierExtensions(Map<String, String> event, AuditEvent ae) {
		String prefix = "modifier_extension_";

		for (String field : VALUE_FIELDS) {
			String csv = event.getOrDefault(prefix + field, "");
			if (csv.isEmpty()) continue;

			String[] extEntries = csv.split("\\|"); // <-- mehrere ModifierExtensions
			for (String extStr : extEntries) {
				String[] parts = extStr.split(";", 2); // URL ; Value
				if (parts.length != 2) continue;

				Extension ext = new Extension();
				ext.setUrl(parts[0]);
				setExtensionValue(ext, field, parts[1]);

				ae.addModifierExtension(ext);
			}
		}
	}

	private static void parseCategories(Map<String, String> event, AuditEvent ae) {
		String[] categoryCodes = event.getOrDefault("category_code", "").split("\\|");
		String[] categorySystems = event.getOrDefault("category_system", "").split("\\|");
		String[] categoryVersions = event.getOrDefault("category_version", "").split("\\|");
		String[] categoryDisplays = event.getOrDefault("category_display", "").split("\\|");

		List<CodeableConcept> categories = new ArrayList<>();
		for (int i = 0; i < categoryCodes.length; i++) {
			if (categoryCodes[i].isEmpty()) continue;
			Coding coding = new Coding().setCode(categoryCodes[i]);
			if (i < categorySystems.length && !categorySystems[i].isEmpty()) coding.setSystem(categorySystems[i]);
			if (i < categoryVersions.length && !categoryVersions[i].isEmpty()) coding.setVersion(categoryVersions[i]);
			if (i < categoryDisplays.length && !categoryDisplays[i].isEmpty()) coding.setDisplay(categoryDisplays[i]);

			CodeableConcept concept = new CodeableConcept().addCoding(coding);
			categories.add(concept);
		}
		ae.setCategory(categories);
	}

	private static void parseEntities(Map<String, String> event, AuditEvent ae) {
		String[] entityIds = event.getOrDefault("entity_id", "").split("\\|");
		String[] entityWhats = event.getOrDefault("entity_what", "").split("\\|");
		String[] entityRolesRaw = event.getOrDefault("entity_role", "").split("\\|");
		String[] entitySecurityLabelsRaw = event.getOrDefault("entity_securityLabel", "").split("\\|");
		String[] entityQueries = event.getOrDefault("entity_query", "").split("\\|");

		int entityCount = Math.max(entityIds.length, Math.max(entityWhats.length, entityRolesRaw.length));

		for (int i = 0; i < entityCount; i++) {
			AuditEvent.AuditEventEntityComponent entity = new AuditEvent.AuditEventEntityComponent();

			if (i < entityIds.length && !entityIds[i].isEmpty()) entity.setId(entityIds[i]);
			if (i < entityWhats.length && !entityWhats[i].isEmpty()) entity.setWhat(new Reference(entityWhats[i]));
			if (i < entityQueries.length && !entityQueries[i].isEmpty()) entity.setQuery(entityQueries[i].getBytes());

			// roles
			if (i < entityRolesRaw.length && !entityRolesRaw[i].isEmpty()) {
				String[] roles = entityRolesRaw[i].split(";");
				for (String role : roles) {
					entity.setRole(new CodeableConcept().addCoding(new Coding().setCode(role)));
				}
			}

			// security labels
			if (i < entitySecurityLabelsRaw.length && !entitySecurityLabelsRaw[i].isEmpty()) {
				String[] labels = entitySecurityLabelsRaw[i].split(";");
				for (String label : labels) {
					entity.addSecurityLabel(new CodeableConcept().addCoding(new Coding().setCode(label)));
				}
			}

			// extensions
			for (String field : VALUE_FIELDS) {
				String csv = event.getOrDefault("entity_extension_" + field, "");
				if (!csv.isEmpty()) {
					String[] extEntries = csv.split("\\|");
					for (String extStr : extEntries) {
						String[] parts = extStr.split(";", 2);
						if (parts.length != 2) continue;
						Extension ext = new Extension();
						ext.setUrl(parts[0]);
						setExtensionValue(ext, field, parts[1]);
						entity.addExtension(ext);
					}
				}

				// modifier extensions
				csv = event.getOrDefault("entity_modifierExtension_" + field, "");
				if (!csv.isEmpty()) {
					String[] modExtEntries = csv.split("\\|");
					for (String extStr : modExtEntries) {
						String[] parts = extStr.split(";", 2);
						if (parts.length != 2) continue;
						Extension ext = new Extension();
						ext.setUrl(parts[0]);
						setExtensionValue(ext, field, parts[1]);
						entity.addModifierExtension(ext);
					}
				}

				// details
				csv = event.getOrDefault("entity_detail_" + field, "");
				if (!csv.isEmpty()) {
					String[] detailEntries = csv.split("\\|");
					for (String detStr : detailEntries) {
						String[] parts = detStr.split(";", 2);
						if (parts.length != 2) continue;
						AuditEvent.AuditEventEntityDetailComponent detail = new AuditEvent.AuditEventEntityDetailComponent();
						detail.setType(new CodeableConcept().setText(parts[0]));

						switch (field) {
							case "valueString": detail.setValue(new StringType(parts[1])); break;
							case "valueBoolean": detail.setValue(new BooleanType(Boolean.parseBoolean(parts[1]))); break;
							case "valueInteger": detail.setValue(new IntegerType(Integer.parseInt(parts[1]))); break;
							case "valueQuantity": detail.setValue(new Quantity(Double.parseDouble(parts[1]))); break;
							case "valueCodeableConcept": detail.setValue(new CodeableConcept().setText(parts[1])); break;
							case "valueTime": detail.setValue(new TimeType(parts[1])); break;
							case "valueDateTime": detail.setValue(new DateTimeType(parts[1])); break;
							case "valueRange":
								String[] r = parts[1].split("-");
								if (r.length == 2) detail.setValue(new Range()
									.setLow(new Quantity(Double.parseDouble(r[0])))
									.setHigh(new Quantity(Double.parseDouble(r[1]))));
								break;
							case "valueRatio":
								String[] ratio = parts[1].split(":");
								if (ratio.length == 2) detail.setValue(new Ratio()
									.setNumerator(new Quantity(Double.parseDouble(ratio[0])))
									.setDenominator(new Quantity(Double.parseDouble(ratio[1]))));
								break;
							case "valuePeriod":
								String[] p = parts[1].split("\\|");
								if (p.length == 2) detail.setValue(new Period()
									.setStartElement(new DateTimeType(p[0]))
									.setEndElement(new DateTimeType(p[1])));
								break;
							case "valueBase64Binary":
								detail.setValue(new Base64BinaryType(Base64.getDecoder().decode(parts[1])));
								break;
						}

						entity.addDetail(detail);
					}
				}
			}

			ae.addEntity(entity);
		}
	}

	private static void setExtensionValue(Extension ext, String field, String value) {
		switch (field) {
			case "valueString": ext.setValue(new StringType(value)); break;
			case "valueBoolean": ext.setValue(new BooleanType(Boolean.parseBoolean(value))); break;
			case "valueInteger": ext.setValue(new IntegerType(Integer.parseInt(value))); break;
			case "valueQuantity": ext.setValue(new Quantity(Double.parseDouble(value))); break;
			case "valueCodeableConcept": ext.setValue(new CodeableConcept().setText(value)); break;
			case "valueTime": ext.setValue(new TimeType(value)); break;
			case "valueDateTime": ext.setValue(new DateTimeType(value)); break;
			case "valueRange":
				String[] r = value.split("-");
				if (r.length==2) ext.setValue(new Range().setLow(new Quantity(Double.parseDouble(r[0]))).setHigh(new Quantity(Double.parseDouble(r[1]))));
				break;
			case "valueRatio":
				String[] ratio = value.split(":");
				if (ratio.length==2) ext.setValue(new Ratio().setNumerator(new Quantity(Double.parseDouble(ratio[0]))).setDenominator(new Quantity(Double.parseDouble(ratio[1]))));
				break;
			case "valuePeriod":
				String[] p = value.split("\\|");
				if (p.length==2) ext.setValue(new Period().setStartElement(new DateTimeType(p[0])).setEndElement(new DateTimeType(p[1])));
				break;
			case "valueBase64Binary":
				ext.setValue(new Base64BinaryType(Base64.getDecoder().decode(value)));
				break;
		}
	}




}
