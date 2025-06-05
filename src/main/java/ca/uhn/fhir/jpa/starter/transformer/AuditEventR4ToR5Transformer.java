package ca.uhn.fhir.jpa.starter.transformer;

import ca.uhn.fhir.context.FhirContext;
import com.oracle.truffle.regex.tregex.parser.ast.visitors.NFATraversalRegexASTVisitor;
import org.apache.jena.base.Sys;
import org.hl7.fhir.Code;
import org.hl7.fhir.DateTime;
import org.hl7.fhir.convertors.conv40_50.resources40_50.AuditEvent40_50;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r5.model.DataType;
import science.aist.gtf.transformation.Transformer;
import science.aist.jack.general.PropertyMapperCreator;
import org.hl7.fhir.convertors.conv40_50.VersionConvertor_40_50;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

/**
 * <p>Transforms an {@link AuditEvent} into a {@link org.hl7.fhir.r5.model.AuditEvent}</p>
 *
 * @author Andreas Pointner
 * @since 1.0
 */
public class AuditEventR4ToR5Transformer implements Transformer<AuditEvent, org.hl7.fhir.r5.model.AuditEvent> {

	private Function<AuditEvent, org.hl7.fhir.r5.model.AuditEvent> auditEventAuditEventFunction = new PropertyMapperCreator<AuditEvent, org.hl7.fhir.r5.model.AuditEvent>()
			.from(ae -> (DateTimeType) ae.getExtensionByUrl("http://fhir.r5.extensions/occurredDateTime").getValue()).toWith(org.hl7.fhir.r5.model.AuditEvent::setOccurred).with(date -> new org.hl7.fhir.r5.model.DateTimeType(date.getValue()))
			.from(ae -> (Reference) ae.getExtensionByUrl("http://fhir.r5.extensions/encounter").getValue()).toWith(org.hl7.fhir.r5.model.AuditEvent::setEncounter).with(ref -> new org.hl7.fhir.r5.model.Reference(ref.getReference()))
			.from(ae -> (Coding) ae.getExtensionByUrl("http://fhir.r5.extensions/code").getValue()).toWith(org.hl7.fhir.r5.model.AuditEvent::setCode).with(code -> new org.hl7.fhir.r5.model.CodeableConcept().addCoding(new org.hl7.fhir.r5.model.Coding(code.getSystem(), code.getCode(), code.getDisplay())))
			.from(ae -> (Reference) ae.getEntityFirstRep().getWhat()).toWith(org.hl7.fhir.r5.model.AuditEvent::setPatient).with(ref -> new org.hl7.fhir.r5.model.Reference(ref.getReference()))
			.create(org.hl7.fhir.r5.model.AuditEvent::new);


	private org.hl7.fhir.r5.model.AuditEvent transformR4toR5(AuditEvent auditEvent) {
		System.out.println(auditEvent.toString());
		org.hl7.fhir.r5.model.AuditEvent ae = new org.hl7.fhir.r5.model.AuditEvent();
		System.out.println(auditEvent.getAction().toString());

		//set fhir r5 action
		org.hl7.fhir.r5.model.AuditEvent.AuditEventAction action = org.hl7.fhir.r5.model.AuditEvent.AuditEventAction.fromCode(auditEvent.getAction().toString());
		ae.setAction(action);


		//set fhir r5 recorded
		ae.setRecorded(auditEvent.getRecorded());


		//set fhir r5 outcome
		if(auditEvent.getOutcome() != null) {
			ae.getOutcome().getCode().setDisplay(auditEvent.getOutcome().getDisplay());
			ae.getOutcome().getCode().setCode(auditEvent.getOutcome().toCode());
			ae.getOutcome().getCode().setSystem(auditEvent.getOutcome().getSystem());
		}


		//set fhir r5 agent
		try {
			for (int i = 0; i < auditEvent.getAgent().size(); i++) {
			//for (int i = 0; i < 1; i++) {
				ae.addAgent();
				ae.getAgent().get(i).setWho(new org.hl7.fhir.r5.model.Reference(auditEvent.getAgent().get(i).getWho().getReference()));
				ae.getAgent().get(i).getWho().setDisplay(auditEvent.getAgent().get(i).getWho().getDisplay());
				ae.getAgent().get(i).setId(auditEvent.getAgent().get(i).getId());
				ae.getAgent().get(i).setRequestor(auditEvent.getAgent().get(i).getRequestor());
				ae.getAgent().get(i).setLocation(new org.hl7.fhir.r5.model.Reference(auditEvent.getAgent().get(i).getLocation().getReference()));
				ae.getAgent().get(i).getType().addCoding();
				ae.getAgent().get(i).getType().getCoding().get(0).setSystem(auditEvent.getAgent().get(i).getType().getCoding().get(0).getSystem());
				ae.getAgent().get(i).getType().getCoding().get(0).setCode(auditEvent.getAgent().get(i).getType().getCoding().get(0).getCode());
				ae.getAgent().get(i).getType().getCoding().get(0).setDisplay(auditEvent.getAgent().get(i).getType().getCoding().get(0).getDisplay());
			}
		}catch (Exception e) {
			System.out.println("No Source found");
		}


		//set fhir r5 source
		try {
			ae.getSource().setObserver(new org.hl7.fhir.r5.model.Reference(auditEvent.getSource().getObserver().getReference()));
			ae.getSource().setSite(new org.hl7.fhir.r5.model.Reference(auditEvent.getSource().getSite()));
			ae.getSource().getObserver().setDisplay(auditEvent.getSource().getObserver().getDisplay());
			ae.getSource().getObserver().setId(auditEvent.getSource().getObserver().getId());
		}catch (Exception e) {
			System.out.println("No Source found");
		}



		//set fhir r5 entity
		try {
			for (int i = 0; i < auditEvent.getEntity().size(); i++) {
				ae.addEntity();
				ae.getEntity().get(i).setWhat(new org.hl7.fhir.r5.model.Reference(auditEvent.getEntity().get(i).getWhat().getReference()));
				ae.getEntity().get(i).setQuery(auditEvent.getEntity().get(i).getQuery());
				ae.getEntity().get(i).getRole().addCoding();
				ae.getEntity().get(i).getRole().getCoding().get(0).setDisplay(auditEvent.getEntity().get(i).getRole().getDisplay());
				ae.getEntity().get(i).getRole().getCoding().get(0).setSystem(auditEvent.getEntity().get(i).getRole().getSystem());
				ae.getEntity().get(i).getRole().getCoding().get(0).setCode(auditEvent.getEntity().get(i).getRole().getCode());
				ae.getEntity().get(i).getWhat().setDisplay(auditEvent.getEntity().get(i).getWhat().getDisplay());
				ae.getEntity().get(i).getWhat().setId(auditEvent.getEntity().get(i).getWhat().getId());
				ae.getEntity().get(i).getWhat().setReference(auditEvent.getEntity().get(i).getWhat().getReference());
				org.hl7.fhir.r5.model.CodeableConcept c = new org.hl7.fhir.r5.model.CodeableConcept();

				if(auditEvent.getEntity().get(i).getDetail() != null && auditEvent.getEntity().get(i).getDetail().get(0).getType() != null)
					c.setText(auditEvent.getEntity().get(i).getDetail().get(0).getType());

				if(auditEvent.getEntity().get(i).getDetail() != null && auditEvent.getEntity().get(i).getDetail().get(0).getType() != null) {
					DataType dt = new org.hl7.fhir.r5.model.StringType(auditEvent.getEntity().get(i).getDetail().get(0).getValue().toString());
					ae.getEntity().get(i).addDetail().setValue(dt);
				}
			}
		}catch (Exception e) {
			System.out.println("No Entity found");
		}



		//set fhir r5 coding
		try {
			ae.getCode().addCoding();
			org.hl7.fhir.r5.model.BooleanType booleanType = new org.hl7.fhir.r5.model.BooleanType();
			booleanType.setValue(auditEvent.getSubtype().get(0).getUserSelected());

			ae.getCode().getCoding().get(0).setUserSelectedElement(booleanType);
			ae.getCode().getCoding().get(0).setSystem(auditEvent.getSubtype().get(0).getSystem());
			ae.getCode().getCoding().get(0).setCode(auditEvent.getSubtype().get(0).getCode());
			ae.getCode().getCoding().get(0).setDisplay(auditEvent.getSubtype().get(0).getDisplay());
			ae.getCode().getCoding().get(0).setVersion(auditEvent.getSubtype().get(0).getVersion());
			ae.getCode().getCoding().get(0).setSystem(auditEvent.getSubtype().get(0).getSystem());
		}catch (Exception e) {
			System.out.println("No subtype found");
		}


		//set fhir r5 Authorization
		ae.addAuthorization();
		org.hl7.fhir.r5.model.BooleanType booleanType2 = new org.hl7.fhir.r5.model.BooleanType();
		try {
			if (auditEvent.getPurposeOfEvent() != null &&
				auditEvent.getPurposeOfEvent().get(0) != null &&
				auditEvent.getPurposeOfEvent().get(0).getCoding() != null) {
				booleanType2.setValue(auditEvent.getPurposeOfEvent().get(0).getCoding().get(0).getUserSelected());
				ae.getAuthorization().get(0).addCoding().setUserSelectedElement(booleanType2);
			}
			ae.getAuthorization().get(0).addCoding().setCode(auditEvent.getPurposeOfEvent().get(0).getCoding().get(0).getCode());
			ae.getAuthorization().get(0).addCoding().setDisplay(auditEvent.getPurposeOfEvent().get(0).getCoding().get(0).getDisplay());
			ae.getAuthorization().get(0).addCoding().setSystem(auditEvent.getPurposeOfEvent().get(0).getCoding().get(0).getSystem());
			ae.getAuthorization().get(0).addCoding().setVersion(auditEvent.getPurposeOfEvent().get(0).getCoding().get(0).getVersion());
		}catch (Exception e) {
			System.out.println("No purposeOfEvent found");
		}


		//set fhir r5 Category
		try {
			ae.addCategory().addCoding();

			ae.getCategory().get(0).getCoding().get(0).setVersion(auditEvent.getType().getVersion());
			ae.getCategory().get(0).getCoding().get(0).setSystem(auditEvent.getType().getSystem());
			ae.getCategory().get(0).getCoding().get(0).setCode(auditEvent.getType().getCode());
			ae.getCategory().get(0).getCoding().get(0).setDisplay(auditEvent.getType().getDisplay());
			ae.getCategory().get(0).getCoding().get(0).setUserSelectedElement(booleanType2);
		}catch (Exception e) {
			System.out.println("No purposeOfEvent found");
		}




		// Set the patient reference if available
		org.hl7.fhir.r5.model.Reference r5Reference = new org.hl7.fhir.r5.model.Reference();
		if (auditEvent.getEntityFirstRep() != null && auditEvent.getEntityFirstRep().getWhat() != null) {
			r5Reference.setReference(auditEvent.getEntityFirstRep().getWhat().getReference());
			ae.setPatient(r5Reference);
		} else {
			System.out.println("Patient reference is null");
		}


		// Set the encounter reference if available
		Extension encounterExtension = auditEvent.getExtensionByUrl("http://hl7.at/fhir/AISTPICA/R4/StructureDefinition/PICAEncounter");
		if (encounterExtension != null && encounterExtension.getValue() != null) {
			org.hl7.fhir.r5.model.Reference reference = new org.hl7.fhir.r5.model.Reference();
			reference.setReference(encounterExtension.getValue().toString());
			ae.setEncounter(reference);
		} else {
			System.out.println("Encounter reference is null");
		}


		// Set the BasedOn reference if available
		List<Extension> basedOnExtension = auditEvent.getExtensionsByUrl("http://hl7.at/fhir/AISTPICA/R4/StructureDefinition/PICABasedOn");
		if (basedOnExtension != null && !basedOnExtension.isEmpty() && basedOnExtension.get(0).getValue() != null) {
			List<org.hl7.fhir.r5.model.Reference> references = new ArrayList<>();
			for (Extension extension : basedOnExtension) {
				org.hl7.fhir.r5.model.Reference reference = new org.hl7.fhir.r5.model.Reference();
				reference.setReference(extension.getValue().toString());
				references.add(reference);
			}
			ae.setBasedOn(references);
		} else {
			System.out.println("BasedOn reference is null");
		}


		// Set the core reference if available

		if (!auditEvent.getEntity().isEmpty() &&  auditEvent.getEntity().get(0).getWhat() != null) {
			Reference ref = auditEvent.getEntity().get(0).getWhat();
			org.hl7.fhir.r5.model.Reference reference = new org.hl7.fhir.r5.model.Reference();
			reference.setReference(ref.getReference());
			ae.setPatient(reference);
		} else {
			System.out.println("core reference is null");
		}




		// Set the severity reference if available
		Extension severityExtension = auditEvent.getExtensionByUrl("http://hl7.at/fhir/AISTPICA/R4/StructureDefinition/PICASeverity");
		if (severityExtension != null && severityExtension.getValue() != null) {
			org.hl7.fhir.r5.model.AuditEvent.AuditEventSeverity severity = org.hl7.fhir.r5.model.AuditEvent.AuditEventSeverity.fromCode(encounterExtension.getValue().toString());
			ae.setSeverity(severity);
		} else {
			System.out.println("Encounter severity is null");
		}



		// Set the code if available, handle different types
		Extension codeExtension = auditEvent.getExtensionByUrl("http://hl7.at/fhir/AISTPICA/R4/StructureDefinition/PICACode");
		if (codeExtension != null && codeExtension.getValue() != null) {
			Object value = codeExtension.getValue();

			if (value instanceof Coding) {

				Coding r4Code = (Coding) value;
				org.hl7.fhir.r5.model.Coding r5Coding = new org.hl7.fhir.r5.model.Coding();
				r5Coding.setSystem(r4Code.getSystem());
				r5Coding.setCode(r4Code.getCode());
				r5Coding.setDisplay(r4Code.getDisplay());
				ae.setCode(new org.hl7.fhir.r5.model.CodeableConcept().addCoding(r5Coding));

			} else if (value instanceof CodeType) {

				CodeType r4Code = (CodeType) value;
				org.hl7.fhir.r5.model.Coding r5Coding = new org.hl7.fhir.r5.model.Coding();
				r5Coding.setCode(r4Code.getValue());
				ae.setCode(new org.hl7.fhir.r5.model.CodeableConcept().addCoding(r5Coding));

			} else {

				org.hl7.fhir.r5.model.Coding coding = new org.hl7.fhir.r5.model.Coding();
				coding.setDisplay(value.toString());
				coding.setSystem(null);
				coding.setCode(null);
				ae.setCode(new org.hl7.fhir.r5.model.CodeableConcept().addCoding(coding));
				System.out.println("Unknown code type: " + value.getClass().getName());

			}
		} else {
			System.out.println("Code reference is null");
		}


		ae.getOccurredPeriod().setStart(auditEvent.getPeriod().getStart());
		ae.getOccurredPeriod().setEnd(auditEvent.getPeriod().getEnd());

		// Set the occurred date and time, use current date/time if missing
		Extension occurredExtension = auditEvent.getExtensionByUrl("http://hl7.at/fhir/AISTPICA/R4/StructureDefinition/PICAOccurredDateTime");

		if (occurredExtension != null && occurredExtension.getValue() != null) {
			// Use the occurred date from the extension
			DateTimeType dtt = (DateTimeType) occurredExtension.getValue();

			if (dtt != null) {
				org.hl7.fhir.r5.model.DateTimeType dateTimeType = new org.hl7.fhir.r5.model.DateTimeType(dtt.getValue());
				ae.setOccurred(dateTimeType);
				System.out.println("Occurred value: " + dateTimeType);

			} else {
				System.out.println("Occurred value is null");
			}
		} else {
			// Use the current date and time if the occurred date is not provided
			org.hl7.fhir.r5.model.DateTimeType currentDateTime = new org.hl7.fhir.r5.model.DateTimeType(new Date());
			ae.setOccurred(currentDateTime);
			System.out.println("Occurred value is null, using current date and time: " + currentDateTime);

		}



		//ToDo remove hardcoded information once the extensions are available
		org.hl7.fhir.r5.model.AuditEvent.AuditEventSeverity sev = org.hl7.fhir.r5.model.AuditEvent.AuditEventSeverity.fromCode("critical");
		ae.setSeverity(sev);
		ae.setEncounter(new org.hl7.fhir.r5.model.Reference("Encounter/3"));


		FhirContext ctx = FhirContext.forR5();
		String json = ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(ae);

		try(FileWriter writer = new FileWriter("C:\\Users\\P43104\\Desktop\\AuditEvent.json")){
			writer.write(json);
		}catch (IOException e){
			e.printStackTrace();
		}


		return ae;
	}


	@Override
	public org.hl7.fhir.r5.model.AuditEvent applyTransformation(AuditEvent auditEvent) {
		System.out.println("AuditEventR4ToR5Transformer.applyTransformation\n\n\n\nwdw\n\n");
		//return auditEventAuditEventFunction.apply(auditEvent);
		return transformR4toR5(auditEvent);
	}
}
