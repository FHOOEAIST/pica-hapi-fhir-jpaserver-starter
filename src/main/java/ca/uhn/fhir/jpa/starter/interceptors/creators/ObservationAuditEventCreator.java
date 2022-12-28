package ca.uhn.fhir.jpa.starter.interceptors.creators;

import ca.uhn.fhir.jpa.starter.interceptors.AuditEventCreator;
import org.hl7.fhir.r4.model.AuditEvent;
import org.hl7.fhir.r4.model.Observation;

/**
 * <p>Creates an {@link AuditEvent} for a {@link Observation}</p>
 *
 * @author Andreas Pointner
 * @since 1.0
 */
public class ObservationAuditEventCreator extends AbstractAuditEventCreator implements AuditEventCreator<Observation> {

	@Override
	public boolean canCreate(Object o) {
		return o instanceof Observation;
	}

	@Override
	public AuditEvent createAuditEvent(Observation observation) {
		AuditEvent ae = new AuditEvent();

		addOccurredDateTime(ae, observation.getEffectiveDateTimeType());
		addPatient(ae, observation.getSubject());
		addEncounter(ae, observation.getEncounter());
		addCode(ae, observation.getCode().getCodingFirstRep());

		return ae;
	}
}
