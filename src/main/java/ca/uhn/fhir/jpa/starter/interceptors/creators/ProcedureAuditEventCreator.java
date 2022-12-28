package ca.uhn.fhir.jpa.starter.interceptors.creators;

import ca.uhn.fhir.jpa.api.dao.IFhirResourceDao;
import ca.uhn.fhir.jpa.starter.interceptors.AuditEventCreator;
import org.hl7.fhir.r4.model.AuditEvent;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Procedure;

/**
 * <p>Creates an {@link AuditEvent} for a {@link Procedure}</p>
 *
 * @author Andreas Pointner
 * @since 1.0
 */
public class ProcedureAuditEventCreator extends AbstractAuditEventCreator implements AuditEventCreator<Procedure> {

	private final IFhirResourceDao<Encounter> myEncounterDao;

	public ProcedureAuditEventCreator(IFhirResourceDao<Encounter> myEncounterDao) {
		this.myEncounterDao = myEncounterDao;
	}

	@Override
	public boolean canCreate(Object o) {
		return o instanceof Procedure;
	}

	@Override
	public AuditEvent createAuditEvent(Procedure procedure) {
		AuditEvent ae = new AuditEvent();

		addOccurredDateTime(ae, procedure.getPerformedPeriod().getStartElement()); // TODO for the moment we just use the start element
		addPatient(ae, myEncounterDao.read(procedure.getEncounter().getReferenceElement()).getSubject());
		addEncounter(ae, procedure.getEncounter());
		addCode(ae, procedure.getCode().getCodingFirstRep());

		return ae;
	}
}
