package ca.uhn.fhir.jpa.starter.interceptors.creators;

import ca.uhn.fhir.jpa.starter.interceptors.AuditEventCreator;
import org.hl7.fhir.r4.model.AuditEvent;
import org.hl7.fhir.r4.model.CarePlan;

/**
 * <p>Creates an {@link AuditEvent} for a {@link CarePlan}</p>
 *
 * @author Andreas Pointner
 * @since 1.0
 */
public class CarePlanAuditEventCreator extends AbstractAuditEventCreator implements AuditEventCreator<CarePlan> {

	@Override
	public boolean canCreate(Object o) {
		return o instanceof CarePlan;
	}

	@Override
	public AuditEvent createAuditEvent(CarePlan carePlan) {
		AuditEvent ae = new AuditEvent();

		addOccurredDateTime(ae, carePlan.getPeriod().getStartElement()); // TODO for the moment we just use the start element
		addPatient(ae, carePlan.getSubject());
		addEncounter(ae, carePlan.getEncounter());
		addCode(ae, carePlan.getActivityFirstRep().getDetail().getCode().getCodingFirstRep()); // TODO first activity or category better?

		return ae;
	}
}
