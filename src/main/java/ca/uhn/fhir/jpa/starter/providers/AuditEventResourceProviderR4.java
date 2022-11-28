package ca.uhn.fhir.jpa.starter.providers;

import ca.uhn.fhir.jpa.api.dao.IFhirResourceDao;
import ca.uhn.fhir.jpa.searchparam.SearchParameterMap;
import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.OperationParam;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.server.IResourceProvider;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.AuditEvent;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import science.aist.fhirauditeventtoocel.FhirAuditEventsToOCELLogService;
import science.aist.fhirauditeventtoxes.FhirAuditEventsToXESLogService;
import science.aist.fhirauditeventtoxes.domain.AuditEventBundle;
import science.aist.gtf.transformation.Transformer;
import science.aist.xes.model.LogType;
import science.aist.xes.model.ObjectFactory;
import science.aist.xes.model.XMLRepository;
import science.aist.xes.model.impl.LogRepository;
import science.aist.xestographviz.GraphToDirectlyFollowsGraphGraphVizTransformer;
import science.aist.xestographviz.XesToGraphTransformer;

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
	private IFhirResourceDao<Encounter> myEncounterDao;

	@Autowired
	private Transformer<AuditEvent, org.hl7.fhir.r5.model.AuditEvent> auditEventR4ToR5Transformer;

	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return AuditEvent.class;
	}

	@Operation(name = "$xes", manualResponse = true, idempotent = true)
	public void toXes(@OperationParam(name = "reasonCode") String reasonCode, HttpServletResponse theServletResponse) throws IOException {
		IBundleProvider search = myAuditEventDao.search(SearchParameterMap.newSynchronous());

		theServletResponse.setStatus(200);
		theServletResponse.setContentType("text/xml");

		// Create a new Fhir service
		var service = new FhirAuditEventsToXESLogService();

		List<AuditEvent> collect1 = search.getAllResources().stream().map(AuditEvent.class::cast).filter(ae -> {
			var reference = (Reference) ae.getExtensionByUrl("http://fhir.r5.extensions/encounter").getValue();
			var id = new IdType(reference.getReference());
			var encounter = myEncounterDao.read(id);
			// TODO how do
			System.out.println();
			return true;
		}).collect(Collectors.toList());

		List<org.hl7.fhir.r5.model.AuditEvent> collect = search.getAllResources().stream().map(AuditEvent.class::cast).map(auditEventR4ToR5Transformer::applyTransformation).collect(Collectors.toList());

		// Execute the service method
		LogType log = service.convertFhirAuditEventsToXESLog(new AuditEventBundle(reasonCode, collect));
		XMLRepository<LogType> repository = new LogRepository();
		var outputStream = new ByteArrayOutputStream();
		repository.save(new ObjectFactory().createLog(log), outputStream);
		String res = outputStream.toString(StandardCharsets.UTF_8);
		theServletResponse.getWriter().write(res);
		theServletResponse.getWriter().close();
	}

	@Operation(name = "$ocel", manualResponse = true, idempotent = true)
	public void toOcel(HttpServletResponse theServletResponse) throws IOException {
		IBundleProvider search = myAuditEventDao.search(SearchParameterMap.newSynchronous());

		theServletResponse.setStatus(200);
		theServletResponse.setContentType("text/xml");

		// Create a new Fhir service
		var service = new FhirAuditEventsToOCELLogService();

		List<org.hl7.fhir.r5.model.AuditEvent> collect = search.getAllResources().stream().map(AuditEvent.class::cast).map(auditEventR4ToR5Transformer::applyTransformation).collect(Collectors.toList());

		// Execute the service method
		var log = service.convertFhirAuditEventsToOCELLog(collect);
		var repository = new science.aist.ocel.model.impl.LogRepository();
		var outputStream = new ByteArrayOutputStream();
		repository.save(new science.aist.ocel.model.ObjectFactory().createLog(log), outputStream);
		String res = outputStream.toString(StandardCharsets.UTF_8);

		theServletResponse.getWriter().write(res);
		theServletResponse.getWriter().close();
	}

	@Operation(name = "$dfg", manualResponse = true, idempotent = true)
	public void toDfg(HttpServletResponse theServletResponse) throws IOException {
		IBundleProvider search = myAuditEventDao.search(SearchParameterMap.newSynchronous());

		// Create a new Fhir service
		var service = new FhirAuditEventsToXESLogService();
		List<org.hl7.fhir.r5.model.AuditEvent> collect = search.getAllResources().stream().map(AuditEvent.class::cast).map(auditEventR4ToR5Transformer::applyTransformation).collect(Collectors.toList());
		LogType log = service.convertFhirAuditEventsToXESLog(new AuditEventBundle("not needed", collect));

		Transformer<LogType, String> xes2graphViz = new XesToGraphTransformer().andThen(new GraphToDirectlyFollowsGraphGraphVizTransformer());
		String res = xes2graphViz.applyTransformation(log);
		System.out.println(res);

		theServletResponse.setStatus(200);
		theServletResponse.setContentType("image/svg+xml");
		MutableGraph g = new Parser().read(res);
		Graphviz.fromGraph(g).width(1024).render(Format.SVG).toOutputStream(theServletResponse.getOutputStream());
	}

}
