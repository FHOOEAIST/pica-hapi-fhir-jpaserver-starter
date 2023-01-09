package ca.uhn.fhir.jpa.starter.providers;

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;
import org.hl7.fhir.r5.model.AuditEvent;
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

/**
 * <p>Abstract base class for both R4 and R5 that performs actions on audit events</p>
 *
 * @author Andreas Pointner
 * @since 1.0
 */
public class AbstractAuditEventResourceProvider {

	protected void toXes(String rootElement, List<AuditEvent> collect, String traceConceptNameResolverPath, HttpServletResponse theServletResponse) throws IOException {
		if (traceConceptNameResolverPath == null || traceConceptNameResolverPath.isEmpty() || traceConceptNameResolverPath.isBlank()) {
			traceConceptNameResolverPath = "getPatient.getReference";
		}

		var xesService = new FhirAuditEventsToXESLogService(traceConceptNameResolverPath, "getCode.getCodingFirstRep.getDisplay");
		LogType log = xesService.convertFhirAuditEventsToXESLog(new AuditEventBundle(rootElement, collect));
		XMLRepository<LogType> repository = new LogRepository();
		var outputStream = new ByteArrayOutputStream();
		repository.save(new ObjectFactory().createLog(log), outputStream);
		String res = outputStream.toString(StandardCharsets.UTF_8);

		theServletResponse.setStatus(200);
		theServletResponse.setContentType("text/xml");
		theServletResponse.getWriter().write(res);
		theServletResponse.getWriter().close();
	}

	protected void toOcel(List<AuditEvent> collect, HttpServletResponse theServletResponse) throws IOException {
		var ocelService = new FhirAuditEventsToOCELLogService();
		var log = ocelService.convertFhirAuditEventsToOCELLog(collect);
		var repository = new science.aist.ocel.model.impl.LogRepository();
		var outputStream = new ByteArrayOutputStream();
		repository.save(new science.aist.ocel.model.ObjectFactory().createLog(log), outputStream);
		String res = outputStream.toString(StandardCharsets.UTF_8);

		theServletResponse.setStatus(200);
		theServletResponse.setContentType("text/xml");
		theServletResponse.getWriter().write(res);
		theServletResponse.getWriter().close();
	}

	protected void toDfg(List<AuditEvent> collect, String traceConceptNameResolverPath, HttpServletResponse theServletResponse) throws IOException {
		if (traceConceptNameResolverPath == null || traceConceptNameResolverPath.isEmpty() || traceConceptNameResolverPath.isBlank()) {
			traceConceptNameResolverPath = "getPatient.getReference";
		}
		var xesService = new FhirAuditEventsToXESLogService(traceConceptNameResolverPath, "getCode.getCodingFirstRep.getDisplay");
		LogType log = xesService.convertFhirAuditEventsToXESLog(new AuditEventBundle("not needed", collect));

		Transformer<LogType, String> xes2graphViz = new XesToGraphTransformer().andThen(new GraphToDirectlyFollowsGraphGraphVizTransformer());
		String res = xes2graphViz.applyTransformation(log);

		theServletResponse.setStatus(200);
		theServletResponse.setContentType("image/svg+xml");
		MutableGraph g = new Parser().read(res);
		Graphviz.fromGraph(g).totalMemory(1000000000).width(1024).render(Format.SVG).toOutputStream(theServletResponse.getOutputStream());
	}
}
