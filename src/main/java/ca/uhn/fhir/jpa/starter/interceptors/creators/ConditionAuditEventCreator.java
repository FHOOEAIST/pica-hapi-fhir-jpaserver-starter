package ca.uhn.fhir.jpa.starter.interceptors.creators;

import ca.uhn.fhir.jpa.starter.interceptors.AuditEventCreator;
import org.hl7.fhir.r4.model.AuditEvent;
import org.hl7.fhir.r4.model.Condition;

/**
 * <p>Creates an {@link AuditEvent} for a {@link Condition}</p>
 *
 * @author Andreas Pointner
 * @since 1.0
 */
public class ConditionAuditEventCreator extends AbstractAuditEventCreator implements AuditEventCreator<Condition> {

	@Override
	public boolean canCreate(Object o) {
		return o instanceof Condition;
	}

	@Override
	public AuditEvent createAuditEvent(Condition condition) {
		AuditEvent ae = new AuditEvent();

		addOccurredDateTime(ae, condition.getRecordedDateElement());
		addPatient(ae, condition.getSubject());
		addEncounter(ae, condition.getEncounter());
		addCode(ae, condition.getCode().getCodingFirstRep());

		return ae;
	}
}
