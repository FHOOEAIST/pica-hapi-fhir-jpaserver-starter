package ca.uhn.fhir.jpa.starter.providers;

import ca.uhn.fhir.jpa.api.dao.IFhirResourceDao;
import ca.uhn.fhir.jpa.searchparam.SearchParameterMap;
import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.OperationParam;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r5.model.AuditEvent;
import org.springframework.beans.factory.annotation.Autowired;

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
public class AuditEventResourceProviderR5 extends AbstractAuditEventResourceProvider implements IResourceProvider {

	@Autowired
	private IFhirResourceDao<AuditEvent> myAuditEventDao;

	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return AuditEvent.class;
	}

	@Operation(name = "$xes", manualResponse = true, idempotent = true)
	public void toXes(@OperationParam(name = "planDefinition") String planDefinition,
							@OperationParam(name = "grouping", max = 1) String grouping,
							@OperationParam(name = "patient", max = 1) String patient,
							@OperationParam(name = "basedOn", max = 1) String basedOn,
							@OperationParam(name = "encounter", max = 1) String encounter,
							@OperationParam(name = "agent", max = 1) String agent,
							HttpServletResponse theServletResponse) throws IOException {

		super.toXes(planDefinition, getAuditEvents(patient, basedOn, encounter, agent), grouping, theServletResponse);
	}

	@Operation(name = "$ocel", manualResponse = true, idempotent = true)
	public void toOcel(@OperationParam(name = "patient", max = 1) String patient,
							 @OperationParam(name = "basedOn", max = 1) String basedOn,
							 @OperationParam(name = "encounter", max = 1) String encounter,
							 @OperationParam(name = "agent", max = 1) String agent,
							 HttpServletResponse theServletResponse) throws IOException {
		super.toOcel(getAuditEvents(patient, basedOn, encounter, agent), theServletResponse);
	}

	@Operation(name = "$dfg", manualResponse = true, idempotent = true)
	public void toDfg(@OperationParam(name = "grouping", max = 1) String grouping,
							@OperationParam(name = "patient", max = 1) String patient,
							@OperationParam(name = "basedOn", max = 1) String basedOn,
							@OperationParam(name = "encounter", max = 1) String encounter,
							@OperationParam(name = "agent", max = 1) String agent,
							HttpServletResponse theServletResponse) throws IOException {
		super.toDfg(getAuditEvents(patient, basedOn, encounter, agent), grouping, theServletResponse);
	}

	private List<AuditEvent> getAuditEvents(String patient, String basedOn, String encounter, String agent) {
		SearchParameterMap searchParameterMap = SearchParameterMap.newSynchronous();
		if (isValidString(patient)) {
			searchParameterMap.add(AuditEvent.SP_PATIENT, new ReferenceParam(patient));
		}
		if (isValidString(basedOn)) {
			searchParameterMap.add(AuditEvent.SP_BASED_ON, new ReferenceParam(basedOn));
		}
		if (isValidString(encounter)) {
			searchParameterMap.add(AuditEvent.SP_ENCOUNTER, new ReferenceParam(encounter));
		}
		if (isValidString(agent)) {
			searchParameterMap.add(AuditEvent.SP_AGENT, new ReferenceParam(agent));
		}

		IBundleProvider search = myAuditEventDao.search(searchParameterMap);
		return search.getAllResources().stream().map(AuditEvent.class::cast).collect(Collectors.toList());
	}

	private static boolean isValidString(String str) {
		return str != null && !str.isEmpty() && !str.isBlank();
	}
}
