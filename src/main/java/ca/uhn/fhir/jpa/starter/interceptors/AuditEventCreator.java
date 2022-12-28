package ca.uhn.fhir.jpa.starter.interceptors;

import org.hl7.fhir.r4.model.AuditEvent;

/**
 * <p>Interface method that defines how to create an audit event out of another resource</p>
 *
 * @author Andreas Pointner
 * @since 1.0
 */
public interface AuditEventCreator<T>{
	/**
	 * Method that check if the resource is capable to create an audit event
	 *
	 * @param o the resource to be checked
	 * @return true if {@link AuditEventCreator#createAuditEvent(Object)} can be called safely
	 */
	boolean canCreate(Object o);

	/**
	 * Method that creates the AuditEvent out of the given resource
	 *
	 * @param elem the resource
	 * @return the resulting AuditEvent
	 */
	AuditEvent createAuditEvent(T elem);
}
