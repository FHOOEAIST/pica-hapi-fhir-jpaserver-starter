package ca.uhn.fhir.jpa.starter.interceptors.creators;

import ca.uhn.fhir.jpa.starter.interceptors.AuditEventCreator;
import org.hl7.fhir.r4.model.AuditEvent;
import org.hl7.fhir.r4.model.ImagingStudy;

/**
 * <p>Creates an {@link AuditEvent} for a {@link ImagingStudy}</p>
 *
 * @author Andreas Pointner
 * @since 1.0
 */
public class ImagingStudyAuditEventCreator extends AbstractAuditEventCreator implements AuditEventCreator<ImagingStudy> {

	@Override
	public boolean canCreate(Object o) {
		return o instanceof ImagingStudy;
	}

	@Override
	public AuditEvent createAuditEvent(ImagingStudy imagingStudy) {
		AuditEvent ae = new AuditEvent();

		addOccurredDateTime(ae, imagingStudy.getStartedElement());
		addPatient(ae, imagingStudy.getSubject());
		addEncounter(ae, imagingStudy.getEncounter());
		addCode(ae, imagingStudy.getProcedureCodeFirstRep().getCodingFirstRep());

		return ae;
	}
}
