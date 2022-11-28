package ca.uhn.fhir.jpa.starter.interceptors.creators;

import org.hl7.fhir.r4.model.AuditEvent;
import org.hl7.fhir.r4.model.Type;

/**
 * <p>TODO class description</p>
 *
 * @author Andreas Pointner
 * @since 1.0
 */
public class AbstractAuditEventCreator {

	private static final String BASE_URL = "http://fhir.r5.extensions/";

	protected void addOccurredDateTime(AuditEvent ae, Type date) {
		addExtension(ae, "occurredDateTime", date);
	}

	protected void addPatient(AuditEvent ae, Type patient) {
		addExtension(ae, "patient", patient);
	}

	protected void addEncounter(AuditEvent ae, Type encounter) {
		addExtension(ae, "encounter", encounter);
	}

	protected void addCode(AuditEvent ae, Type code) {
		addExtension(ae, "code", code);
	}

	protected void addExtension(AuditEvent ae, String extension, Type value) {
		ae.addExtension()
			.setUrl(BASE_URL + extension) // no idea which uri makes sense here.
			.setValue(value);
	}
}
