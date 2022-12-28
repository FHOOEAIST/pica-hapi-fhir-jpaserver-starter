package ca.uhn.fhir.jpa.starter.interceptors.creators;

import ca.uhn.fhir.jpa.starter.interceptors.AuditEventCreator;
import org.hl7.fhir.r4.model.AuditEvent;
import org.hl7.fhir.r4.model.MedicationRequest;

/**
 * <p>Creates an {@link AuditEvent} for a {@link MedicationRequest}</p>
 *
 * @author Andreas Pointner
 * @since 1.0
 */
public class MedicationRequestAuditEventCreator extends AbstractAuditEventCreator implements AuditEventCreator<MedicationRequest> {

	@Override
	public boolean canCreate(Object o) {
		return o instanceof MedicationRequest;
	}

	@Override
	public AuditEvent createAuditEvent(MedicationRequest medicationRequest) {
		AuditEvent ae = new AuditEvent();

		addOccurredDateTime(ae, medicationRequest.getAuthoredOnElement());
		addPatient(ae, medicationRequest.getSubject());
		addEncounter(ae, medicationRequest.getEncounter());
		addCode(ae, medicationRequest.getMedicationCodeableConcept().getCodingFirstRep());

		return ae;
	}
}
