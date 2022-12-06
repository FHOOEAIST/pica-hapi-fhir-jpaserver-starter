package ca.uhn.fhir.jpa.starter.providers;

import ca.uhn.fhir.jpa.api.dao.IFhirResourceDao;
import ca.uhn.fhir.jpa.searchparam.SearchParameterMap;
import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.OperationParam;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
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
	public void toXes(@OperationParam(name = "planDefinition") String planDefinition, HttpServletResponse theServletResponse) throws IOException {
		IBundleProvider search = myAuditEventDao.search(SearchParameterMap.newSynchronous());
		// TODO filtering for plan definition would actually be needed
		List<AuditEvent> collect = search.getAllResources().stream().map(AuditEvent.class::cast).collect(Collectors.toList());
		super.toXes(planDefinition, collect, theServletResponse);
	}

	@Operation(name = "$ocel", manualResponse = true, idempotent = true)
	public void toOcel(HttpServletResponse theServletResponse) throws IOException {
		IBundleProvider search = myAuditEventDao.search(SearchParameterMap.newSynchronous());
		List<AuditEvent> collect = search.getAllResources().stream().map(AuditEvent.class::cast).collect(Collectors.toList());
		super.toOcel(collect, theServletResponse);
	}

	@Operation(name = "$dfg", manualResponse = true, idempotent = true)
	public void toDfg(HttpServletResponse theServletResponse) throws IOException {
		IBundleProvider search = myAuditEventDao.search(SearchParameterMap.newSynchronous());
		List<AuditEvent> collect = search.getAllResources().stream().map(AuditEvent.class::cast).collect(Collectors.toList());
		super.toDfg(collect, theServletResponse);
	}
}
