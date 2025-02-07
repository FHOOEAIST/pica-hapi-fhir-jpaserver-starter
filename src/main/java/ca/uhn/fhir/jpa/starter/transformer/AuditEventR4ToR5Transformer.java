package ca.uhn.fhir.jpa.starter.transformer;

import org.apache.jena.base.Sys;
import org.hl7.fhir.Code;
import org.hl7.fhir.DateTime;
import org.hl7.fhir.r4.model.*;
import science.aist.gtf.transformation.Transformer;
import science.aist.jack.general.PropertyMapperCreator;

import java.util.Date;
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
		org.hl7.fhir.r5.model.AuditEvent ae = new org.hl7.fhir.r5.model.AuditEvent();

		// Set the patient reference if available
		org.hl7.fhir.r5.model.Reference r5Reference = new org.hl7.fhir.r5.model.Reference();
		if (auditEvent.getEntityFirstRep() != null && auditEvent.getEntityFirstRep().getWhat() != null) {
			r5Reference.setReference(auditEvent.getEntityFirstRep().getWhat().getReference());
			ae.setPatient(r5Reference);
		} else {
			System.out.println("Patient reference is null");
		}

		// Set the encounter reference if available
		Extension encounterExtension = auditEvent.getExtensionByUrl("http://fhir.r5.extensions/encounter");
		if (encounterExtension != null && encounterExtension.getValue() != null) {
			org.hl7.fhir.r5.model.Reference reference = new org.hl7.fhir.r5.model.Reference();
			reference.setReference(encounterExtension.getValue().toString());
			ae.setEncounter(reference);
		} else {
			System.out.println("Encounter reference is null");
		}

		// Set the code if available, handle different types
		Extension codeExtension = auditEvent.getExtensionByUrl("http://fhir.r5.extensions/code");
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

		// Set the occurred date and time, use current date/time if missing
		Extension occurredExtension = auditEvent.getExtensionByUrl("http://fhir.r5.extensions/occurredDateTime");

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
		return ae;
	}
	@Override
	public org.hl7.fhir.r5.model.AuditEvent applyTransformation(AuditEvent auditEvent) {

		//return auditEventAuditEventFunction.apply(auditEvent);
		return transformR4toR5(auditEvent);
	}
}
