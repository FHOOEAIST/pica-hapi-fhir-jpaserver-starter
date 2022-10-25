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
public interface AuditEventResourceProvider extends IResourceProvider {

	@Operation(name = "$xes", manualResponse = true, idempotent = true)
	void toXes(@OperationParam(name = "planDefinition") String planDefinition, HttpServletResponse theServletResponse) throws IOException;

}
