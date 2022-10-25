package ca.uhn.fhir.jpa.starter.interceptors;

import ca.uhn.fhir.jpa.api.dao.IFhirResourceDao;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.interceptor.consent.IConsentContextServices;
import ca.uhn.fhir.rest.server.interceptor.consent.IConsentService;
import org.hl7.fhir.r4.model.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>TODO class description</p>
 *
 * @author Andreas Pointner
 * @since 1.0
 */
public class AuditEventCreatorConsentInterceptor implements IConsentService {

	@Autowired
	private IFhirResourceDao<AuditEvent> myAuditEventDao;

	@Override
	public void completeOperationSuccess(RequestDetails theRequestDetails, IConsentContextServices theContextServices) {
		System.out.println(">>> create audit event.");
		// extract encounters:
		if (theRequestDetails.getResource() instanceof Bundle) {

			// TODO think which element is the correct one to use here ...

			List<Procedure> collect = ((Bundle) theRequestDetails.getResource()).getEntry().stream()
				.filter(Bundle.BundleEntryComponent::hasResource)
				.map(Bundle.BundleEntryComponent::getResource)
				.filter(Procedure.class::isInstance)
				.map(Procedure.class::cast)
				.collect(Collectors.toList());

			for (Procedure procedure : collect) {
				AuditEvent ae = new AuditEvent();

				ae.addExtension()
					.setUrl("http://fhir.r5.extensions/occurredDateTime") // no idea which uri makes sense here.
					.setValue(procedure.getPerformedPeriod().getStartElement()); // TODO for the moment we just use the start element

				// TODO maybe via encounter
//				ae.addExtension()
//					.setUrl("http://fhir.r5.extensions/patient")
//					.setValue(encounter.getSubject());

				ae.addExtension()
					.setUrl("http://fhir.r5.extensions/encounter")
					.setValue(procedure.getEncounter());

				ae.addExtension()
					.setUrl("http://fhir.r5.extensions/code")
					.setValue(procedure.getCode().getCodingFirstRep());

				myAuditEventDao.create(ae);

				System.out.println();
			}

			System.out.println(collect);
		}
	}
}
