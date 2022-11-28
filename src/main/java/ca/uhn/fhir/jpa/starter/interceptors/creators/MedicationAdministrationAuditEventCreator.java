package ca.uhn.fhir.jpa.starter.interceptors.creators;

import ca.uhn.fhir.jpa.starter.interceptors.AuditEventCreator;
import org.hl7.fhir.r4.model.AuditEvent;
import org.hl7.fhir.r4.model.MedicationAdministration;

/**
 * <p>TODO class description</p>
 *
 * @author Andreas Pointner
 * @since 1.0
 */
public class MedicationAdministrationAuditEventCreator extends AbstractAuditEventCreator implements AuditEventCreator<MedicationAdministration> {

	@Override
	public boolean canCreate(Object o) {
		return o instanceof MedicationAdministration;
	}

	@Override
	public AuditEvent createAuditEvent(MedicationAdministration medicationAdministration) {
		AuditEvent ae = new AuditEvent();

		addOccurredDateTime(ae, medicationAdministration.getEffectiveDateTimeType());
		addPatient(ae, medicationAdministration.getSubject());
		addEncounter(ae, medicationAdministration.getContext());
		addCode(ae, medicationAdministration.getMedicationCodeableConcept().getCodingFirstRep());

		return ae;
	}
}
