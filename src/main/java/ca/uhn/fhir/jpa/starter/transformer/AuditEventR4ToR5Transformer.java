package ca.uhn.fhir.jpa.starter.transformer;

import org.hl7.fhir.r4.model.*;
import science.aist.gtf.transformation.Transformer;
import science.aist.jack.general.PropertyMapperCreator;

import java.util.function.Function;

/**
 * <p>TODO class description</p>
 *
 * @author Andreas Pointner
 * @since 1.0
 */
public class AuditEventR4ToR5Transformer implements Transformer<AuditEvent, org.hl7.fhir.r5.model.AuditEvent> {

	private Function<AuditEvent, org.hl7.fhir.r5.model.AuditEvent> auditEventAuditEventFunction = new PropertyMapperCreator<AuditEvent, org.hl7.fhir.r5.model.AuditEvent>()
		.from(ae -> (DateTimeType) ae.getExtensionByUrl("http://fhir.r5.extensions/occurredDateTime").getValue()).toWith(org.hl7.fhir.r5.model.AuditEvent::setOccurred).with(date -> new org.hl7.fhir.r5.model.DateTimeType(date.getValue()))
		.from(ae -> (Reference) ae.getExtensionByUrl("http://fhir.r5.extensions/encounter").getValue()).toWith(org.hl7.fhir.r5.model.AuditEvent::setEncounter).with(ref -> new org.hl7.fhir.r5.model.Reference(ref.getReference()))
		.from(ae -> (Coding) ae.getExtensionByUrl("http://fhir.r5.extensions/code").getValue()).toWith(org.hl7.fhir.r5.model.AuditEvent::setCode).with(code -> new org.hl7.fhir.r5.model.CodeableConcept().addCoding(new org.hl7.fhir.r5.model.Coding(code.getSystem(), code.getCode(), code.getDisplay())))
		.from(ae -> (Reference) ae.getExtensionByUrl("http://fhir.r5.extensions/patient").getValue()).toWith(org.hl7.fhir.r5.model.AuditEvent::setPatient).with(ref -> new org.hl7.fhir.r5.model.Reference(ref.getReference()))
		.create(org.hl7.fhir.r5.model.AuditEvent::new);

	@Override
	public org.hl7.fhir.r5.model.AuditEvent applyTransformation(AuditEvent auditEvent) {
		return auditEventAuditEventFunction.apply(auditEvent);
	}
}
