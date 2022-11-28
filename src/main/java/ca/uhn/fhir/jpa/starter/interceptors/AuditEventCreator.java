package ca.uhn.fhir.jpa.starter.interceptors;

import org.hl7.fhir.r4.model.AuditEvent;

/**
 * <p>TODO class description</p>
 *
 * @author Andreas Pointner
 * @since 1.0
 */
public interface AuditEventCreator<T>{
	boolean canCreate(Object o);

	AuditEvent createAuditEvent(T elem);
}
