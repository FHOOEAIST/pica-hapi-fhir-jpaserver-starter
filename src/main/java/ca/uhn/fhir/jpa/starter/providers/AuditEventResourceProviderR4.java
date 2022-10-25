package ca.uhn.fhir.jpa.starter.providers;

import ca.uhn.fhir.jpa.api.dao.IFhirResourceDao;
import ca.uhn.fhir.jpa.searchparam.SearchParameterMap;
import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.OperationParam;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.AuditEvent;
import org.springframework.beans.factory.annotation.Autowired;
import science.aist.fhirauditeventtoxes.FhirAuditEventsToXESLogService;
import science.aist.fhirauditeventtoxes.domain.AuditEventBundle;
import science.aist.gtf.transformation.Transformer;
import science.aist.xes.model.LogType;
import science.aist.xes.model.ObjectFactory;
import science.aist.xes.model.XMLRepository;
import science.aist.xes.model.impl.LogRepository;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>Resource Provider for Audit Events to transform them into either xes or ocel representation</p>
 *
 * @author Andreas Pointner
 * @since 1.0
 */
public class AuditEventResourceProviderR4 implements IResourceProvider, AuditEventResourceProvider {

	@Autowired
	private IFhirResourceDao<AuditEvent> myAuditEventDao;

	@Autowired
	private Transformer<AuditEvent, org.hl7.fhir.r5.model.AuditEvent> auditEventR4ToR5Transformer;

	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return AuditEvent.class;
	}

	@Operation(name = "$xes", manualResponse = true, idempotent = true)
	public void toXes(@OperationParam(name = "planDefinition") String planDefinition, HttpServletResponse theServletResponse) throws IOException {
		IBundleProvider search = myAuditEventDao.search(SearchParameterMap.newSynchronous());

		theServletResponse.setStatus(200);
		theServletResponse.setContentType("text/xml");

		// Create a new Fhir service
		var service = new FhirAuditEventsToXESLogService();

		List<org.hl7.fhir.r5.model.AuditEvent> collect = search.getAllResources().stream().map(AuditEvent.class::cast).map(auditEventR4ToR5Transformer::applyTransformation).collect(Collectors.toList());

		// Execute the service method
		LogType log = service.convertFhirAuditEventsToXESLog(new AuditEventBundle(planDefinition, collect));
		XMLRepository<LogType> repository = new LogRepository();
		var outputStream = new ByteArrayOutputStream();
		repository.save(new ObjectFactory().createLog(log), outputStream);
		String res = outputStream.toString(StandardCharsets.UTF_8);
		theServletResponse.getWriter().write(res);
		theServletResponse.getWriter().close();
	}

}
