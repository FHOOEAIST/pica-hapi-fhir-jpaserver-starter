package ca.uhn.fhir.jpa.starter.providers;

import ca.uhn.fhir.jpa.api.dao.IFhirResourceDao;
import ca.uhn.fhir.jpa.searchparam.SearchParameterMap;
import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.OperationParam;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.AuditEvent;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import science.aist.gtf.transformation.Transformer;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>Resource Provider for Audit Events to transform them into either xes or ocel representation</p>
 *
 * @author Andreas Pointner
 * @since 1.0
 */
public class AuditEventResourceProviderR4 extends AbstractAuditEventResourceProvider implements IResourceProvider {

	@Autowired
	private IFhirResourceDao<AuditEvent> myAuditEventDao;

	@Autowired
	private IFhirResourceDao<Encounter> myEncounterDao;

	@Autowired
	private Transformer<AuditEvent, org.hl7.fhir.r5.model.AuditEvent> auditEventR4ToR5Transformer;

	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return AuditEvent.class;
	}

	@Operation(name = "$xes", manualResponse = true, idempotent = true)
	public void toXes(@OperationParam(name = "reasonCode", min = 1, max = 1) String reasonCode, HttpServletResponse theServletResponse) throws IOException {
		super.toXes(reasonCode, filterAuditEventsByReasonCode(reasonCode), theServletResponse);
	}

	@Operation(name = "$ocel", manualResponse = true, idempotent = true)
	public void toOcel(HttpServletResponse theServletResponse) throws IOException {
		IBundleProvider search = myAuditEventDao.search(SearchParameterMap.newSynchronous());
		List<org.hl7.fhir.r5.model.AuditEvent> collect = search.getAllResources().stream().map(AuditEvent.class::cast).map(auditEventR4ToR5Transformer::applyTransformation).collect(Collectors.toList());
		super.toOcel(collect, theServletResponse);
	}

	@Operation(name = "$dfg", manualResponse = true, idempotent = true)
	public void toDfg(@OperationParam(name = "reasonCode", min = 1, max = 1) String reasonCode, HttpServletResponse theServletResponse) throws IOException {
		super.toDfg(filterAuditEventsByReasonCode(reasonCode), theServletResponse);
	}

	private List<org.hl7.fhir.r5.model.AuditEvent> filterAuditEventsByReasonCode(String reasonCode) {
		IBundleProvider search = myAuditEventDao.search(SearchParameterMap.newSynchronous());

		return search.getAllResources()
			.stream()
			.map(AuditEvent.class::cast)
			// no filtering ftm
//			.filter(ae -> {
//				var reference = (Reference) ae.getExtensionByUrl("http://fhir.r5.extensions/encounter").getValue();
//				var id = new IdType(reference.getReference());
//				var encounter = myEncounterDao.read(id);
//				System.out.println(encounter.getReasonCodeFirstRep().getCodingFirstRep().getCode());
//				return encounter.getReasonCodeFirstRep().getCoding().stream().anyMatch(c -> reasonCode.equals(c.getCode()));
//			})
			.map(auditEventR4ToR5Transformer::applyTransformation)
			.collect(Collectors.toList());
	}

}
