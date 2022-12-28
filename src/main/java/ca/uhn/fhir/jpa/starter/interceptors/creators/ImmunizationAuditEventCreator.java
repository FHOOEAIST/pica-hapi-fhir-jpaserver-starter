package ca.uhn.fhir.jpa.starter.interceptors.creators;

import ca.uhn.fhir.jpa.starter.interceptors.AuditEventCreator;
import org.hl7.fhir.r4.model.AuditEvent;
import org.hl7.fhir.r4.model.Immunization;

/**
 * <p>Creates an {@link AuditEvent} for a {@link Immunization}</p>
 *
 * @author Andreas Pointner
 * @since 1.0
 */
public class ImmunizationAuditEventCreator extends AbstractAuditEventCreator implements AuditEventCreator<Immunization> {

	@Override
	public boolean canCreate(Object o) {
		return o instanceof Immunization;
	}

	@Override
	public AuditEvent createAuditEvent(Immunization immunization) {
		AuditEvent ae = new AuditEvent();

		addOccurredDateTime(ae, immunization.getOccurrenceDateTimeType());
		addPatient(ae, immunization.getPatient());
		addEncounter(ae, immunization.getEncounter());
		addCode(ae, immunization.getVaccineCode().getCodingFirstRep());

		return ae;
	}
}
