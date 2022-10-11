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
import org.hl7.fhir.r5.model.AuditEvent;
import org.springframework.beans.factory.annotation.Autowired;
import science.aist.fhirauditeventtoocel.FhirAuditEventsToOCELLogService;
import science.aist.fhirauditeventtoxes.FhirAuditEventsToXESLogService;
import science.aist.fhirauditeventtoxes.domain.AuditEventBundle;
import science.aist.gtf.graph.Edge;
import science.aist.gtf.graph.Graph;
import science.aist.gtf.graph.builder.GraphBuilder;
import science.aist.gtf.graph.builder.impl.GraphBuilderImpl;
import science.aist.gtf.graph.impl.traversal.DepthFirstSearchTraversalStrategy;
import science.aist.gtf.transformation.Transformer;
import science.aist.xes.model.*;
import science.aist.xes.model.impl.LogRepository;
import science.aist.xestographviz.GraphToDirectFollowerGraphGraphVizTransformer;
import science.aist.xestographviz.XesToGraphTransformer;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>Resource Provider for Audit Events to transform them into either xes or ocel representation</p>
 *
 * @author Andreas Pointner
 * @since 1.0
 */
public class AuditEventResourceProvider implements IResourceProvider {

	@Autowired
	IFhirResourceDao<AuditEvent> myAuditEventDao;

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

		List<AuditEvent> collect = search.getAllResources().stream().map(AuditEvent.class::cast).collect(Collectors.toList());

		// Execute the service method
		LogType log = service.convertFhirAuditEventsToXESLog(new AuditEventBundle(planDefinition, collect));
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

		List<AuditEvent> collect = search.getAllResources().stream().map(AuditEvent.class::cast).collect(Collectors.toList());

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
		List<AuditEvent> collect = search.getAllResources().stream().map(AuditEvent.class::cast).collect(Collectors.toList());
		LogType log = service.convertFhirAuditEventsToXESLog(new AuditEventBundle("not needed", collect));

		Transformer<LogType, String> xes2graphViz = new XesToGraphTransformer().andThen(new GraphToDirectFollowerGraphGraphVizTransformer());
		String res = xes2graphViz.applyTransformation(log);

		theServletResponse.setStatus(200);
		theServletResponse.setContentType("image/svg+xml");
		MutableGraph g = new Parser().read(res);
		Graphviz.fromGraph(g).width(1024).render(Format.SVG).toOutputStream(theServletResponse.getOutputStream());
	}
}
