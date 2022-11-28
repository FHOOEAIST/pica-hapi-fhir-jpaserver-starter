package ca.uhn.fhir.jpa.starter.interceptors;

import ca.uhn.fhir.jpa.api.dao.IFhirResourceDao;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.interceptor.consent.IConsentContextServices;
import ca.uhn.fhir.rest.server.interceptor.consent.IConsentService;
import org.hl7.fhir.r4.model.AuditEvent;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Objects;

/**
 * <p>Consent Interceptor, that is used to create AuditEvents</p>
 *
 * @author Andreas Pointner
 * @since 1.0
 */
public class AuditEventCreatorConsentInterceptor implements IConsentService {

	@Autowired
	private IFhirResourceDao<AuditEvent> myAuditEventDao;

	@Autowired
	@Qualifier("multiAuditEventCreator")
	private AuditEventCreator<Resource> auditEventCreator;

	@Override
	public void completeOperationSuccess(RequestDetails theRequestDetails, IConsentContextServices theContextServices) {
		// extract resources:
		if (theRequestDetails.getResource() instanceof Bundle) {
			((Bundle) theRequestDetails.getResource()).getEntry().stream()
				.filter(Bundle.BundleEntryComponent::hasResource)
				.map(Bundle.BundleEntryComponent::getResource)
				.map(auditEventCreator::createAuditEvent)
				.filter(Objects::nonNull)
				.forEach(myAuditEventDao::create);
		}
	}
}
