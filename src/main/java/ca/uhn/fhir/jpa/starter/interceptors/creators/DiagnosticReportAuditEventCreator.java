package ca.uhn.fhir.jpa.starter.interceptors.creators;

import ca.uhn.fhir.jpa.starter.interceptors.AuditEventCreator;
import org.hl7.fhir.r4.model.AuditEvent;
import org.hl7.fhir.r4.model.DiagnosticReport;

/**
 * <p>Creates an {@link AuditEvent} for a {@link DiagnosticReport}</p>
 *
 * @author Andreas Pointner
 * @since 1.0
 */
public class DiagnosticReportAuditEventCreator extends AbstractAuditEventCreator implements AuditEventCreator<DiagnosticReport> {

	@Override
	public boolean canCreate(Object o) {
		return o instanceof DiagnosticReport;
	}

	@Override
	public AuditEvent createAuditEvent(DiagnosticReport diagnosticReport) {
		AuditEvent ae = new AuditEvent();

		addOccurredDateTime(ae, diagnosticReport.getEffectiveDateTimeType());
		addPatient(ae, diagnosticReport.getSubject());
		addEncounter(ae, diagnosticReport.getEncounter());
		addCode(ae, diagnosticReport.getCode().getCodingFirstRep());

		return ae;
	}
}
