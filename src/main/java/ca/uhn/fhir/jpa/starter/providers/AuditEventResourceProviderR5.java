package ca.uhn.fhir.jpa.starter.providers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.interceptor.api.Pointcut;
import ca.uhn.fhir.jpa.api.dao.IFhirResourceDao;
import ca.uhn.fhir.jpa.searchparam.SearchParameterMap;
import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.OperationParam;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import org.hl7.fhir.instance.model.api.IBaseConformance;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r5.model.*;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
	public void toXes(
		@OperationParam(name = "carePlan", min=0) String carePlan,
		@OperationParam(name = "start", min = 0, max = 1) String startDateStr,
		@OperationParam(name = "end", min = 0, max = 1) String endDateStr,
		@OperationParam(name = "core", min = 0) List<String> patientStr,
		@OperationParam(name = "actorRef", min = 0, max = 1) List<String>  actorRef,
		@OperationParam(name = "patientvisit", min = 0) List<String> patientvisit,
		@OperationParam(name = "conformance", min = 0) List<String> conformance,
		HttpServletResponse theServletResponse) throws IOException {

		System.out.println(patientStr);
		// Retrieve all AuditEvents
		IBundleProvider search = myAuditEventDao.search(SearchParameterMap.newSynchronous());
		List<AuditEvent> events = search.getAllResources().stream()
			.filter(Objects::nonNull)
			.filter(AuditEvent.class::isInstance)
			.map(AuditEvent.class::cast)
			.filter(auditEvent -> auditEvent.hasPatient() && auditEvent.getPatient() != null)
			.filter(auditEvent -> auditEvent.hasCode() && auditEvent.getCode() != null)
			.filter(AuditEvent::hasOccurred)
			.collect(Collectors.toList());


		events = filterByPatient(events,patientStr);
		events = filterByTime(events,startDateStr,endDateStr);
		events = filterByActor(events, actorRef);
		events = filterByPatientVisit(events, patientvisit);
		events = filterByConformance(events,conformance);

		super.toXes(carePlan, events, theServletResponse);
	}

	@Operation(name = "$filterByTime", idempotent = true, type = AuditEvent.class)
	public Bundle filterAuditEventsByStartAndEndDate(
		@OperationParam(name = "start", min = 1, max = 1) String startDateStr,
		@OperationParam(name = "end", min = 0, max = 1) String endDateStr) {

		if (startDateStr == null || startDateStr.trim().isEmpty()) {
			throw new InvalidRequestException("Start date must be provided.");
		}
		// Retrieve AuditEvents
		IBundleProvider search = myAuditEventDao.search(SearchParameterMap.newSynchronous());
		List<AuditEvent> filteredEvents = search.getAllResources().stream()
			.filter(Objects::nonNull)
			.filter(AuditEvent.class::isInstance)
			.map(AuditEvent.class::cast)
			.filter(auditEvent -> auditEvent.hasPatient() && auditEvent.getPatient() != null)
			.filter(auditEvent -> auditEvent.hasCode() && auditEvent.getCode() != null)
			.filter(AuditEvent::hasOccurred)
			.collect(Collectors.toList());

		filteredEvents.forEach(this::enrichAuditEventWithProfile);
		filteredEvents = filterByTime(filteredEvents,startDateStr,endDateStr);

		Bundle bundle = new Bundle();
		for (AuditEvent event : filteredEvents) {
			bundle.addEntry().setResource(event);
		}
		return bundle;
	}

	@Operation(name = "$addProfile", idempotent = true, type = AuditEvent.class)
	public Bundle addProfileToAudioEvent() {

		// Retrieve AuditEvents
		IBundleProvider search = myAuditEventDao.search(SearchParameterMap.newSynchronous());
		List<AuditEvent> filteredEvents = search.getAllResources().stream()
			.filter(Objects::nonNull)
			.filter(AuditEvent.class::isInstance)
			.map(AuditEvent.class::cast)
			.filter(auditEvent -> auditEvent.hasPatient() && auditEvent.getPatient() != null)
			.filter(auditEvent -> auditEvent.hasCode() && auditEvent.getCode() != null)
			.filter(AuditEvent::hasOccurred)
			.collect(Collectors.toList());

		filteredEvents.forEach(this::enrichAuditEventWithProfile);

		Bundle bundle = new Bundle();
		for (AuditEvent event : filteredEvents) {
			bundle.addEntry().setResource(event);
		}
		return bundle;
	}

	@Operation(name = "$ocel", manualResponse = true, idempotent = true)
	public void toOcel(
		@OperationParam(name = "start", min = 0, max = 1) String startDateStr,
		@OperationParam(name = "end", min = 0, max = 1) String endDateStr,
		@OperationParam(name = "core", min = 0) List<String> patientStr,
		@OperationParam(name = "actorRef", min = 0) List<String> actorRef,
		@OperationParam(name = "patientvisit", min = 0) List<String> patientvisit,
		@OperationParam(name = "conformance", min = 0) List<String> conformance,
		HttpServletResponse theServletResponse) throws IOException {

		IBundleProvider search = myAuditEventDao.search(SearchParameterMap.newSynchronous());
		List<AuditEvent> collect = search.getAllResources().stream()
			.map(AuditEvent.class::cast)
			.filter(auditEvent -> auditEvent.hasPatient() && auditEvent.getPatient() != null)
			.filter(auditEvent -> auditEvent.hasCode() && auditEvent.getCode() != null)
			.filter(AuditEvent::hasOccurred)
			.collect(Collectors.toList());
		System.out.println(patientStr);
		collect = filterByPatient(collect,patientStr);
		collect = filterByTime(collect,startDateStr,endDateStr);
		collect = filterByActor(collect, actorRef);
		collect = filterByPatientVisit(collect, patientvisit);
		collect = filterByConformance(collect,conformance);
		super.toOcel(collect, theServletResponse);

	}

	@Operation(name = "$dfg", manualResponse = true, idempotent = true)
	public void toDfg(
		@OperationParam(name = "start", min = 0, max = 1) String startDateStr,
		@OperationParam(name = "end", min = 0, max = 1) String endDateStr,
		@OperationParam(name = "core", min = 0) List<String> patientStr,
		@OperationParam(name = "actorRef", min = 0, max = 1) List<String>  actorRef,
		@OperationParam(name = "patientvisit", min = 0) List<String> patientvisit,
		@OperationParam(name = "conformance", min = 0) List<String> conformance,
		@OperationParam(name = "grouping", max = 1) String grouping,
		HttpServletResponse theServletResponse) throws IOException {
		IBundleProvider search = myAuditEventDao.search(SearchParameterMap.newSynchronous());

		List<AuditEvent> events = search.getAllResources().stream()
			.filter(Objects::nonNull)
			.filter(AuditEvent.class::isInstance)
			.map(AuditEvent.class::cast)
			.filter(auditEvent -> auditEvent.hasPatient() && auditEvent.getPatient() != null)
			.filter(auditEvent -> auditEvent.hasCode() && auditEvent.getCode() != null)
			.filter(AuditEvent::hasOccurred)
			.collect(Collectors.toList());


		events = filterByPatient(events,patientStr);
		events = filterByTime(events,startDateStr,endDateStr);
		events = filterByActor(events, actorRef);
		events = filterByPatientVisit(events, patientvisit);
		events = filterByConformance(events,conformance);

		super.toDfg(events, grouping, theServletResponse);
	}

	public static List<AuditEvent> filterByPatient(List<AuditEvent> events, List<String> patientLst){

		if (patientLst == null || patientLst.get(0).trim().isEmpty())
			return events;

		List<AuditEvent> returnVal = new java.util.ArrayList<>();
		for (String patientStr : patientLst){
			String normalizedPatientId = patientStr.startsWith("Patient/") ? patientStr : "Patient/" + patientStr;
			List<AuditEvent> temp = events.stream()
				.filter(auditEvent ->
					normalizedPatientId.equals(auditEvent.getPatient().getIdentifier().getValue()) ||
						normalizedPatientId.equals(auditEvent.getPatient().getReference()))
				.collect(Collectors.toList());

			returnVal = Stream.concat(returnVal.stream(), temp.stream())
				.collect(Collectors.toList());
		}
		Set<AuditEvent> uniqueEvents = new java.util.HashSet<>(returnVal);
		returnVal.clear();
		returnVal.addAll(uniqueEvents);
		return returnVal;
	}

	public static List<AuditEvent> filterByTime(List<AuditEvent> events, String startDateStr,String endDateStr){

		if (startDateStr != null && !startDateStr.trim().isEmpty()) {
			if (endDateStr == null || endDateStr.trim().isEmpty()) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				endDateStr = sdf.format(new Date());
			}

			DateTimeType startDateTime;
			try {
				startDateTime = new DateTimeType(startDateStr);
			} catch (Exception e) {
				throw new InvalidRequestException("Invalid start date. Use format YYYY-MM-DD.");
			}

			DateTimeType endDateTime;
			try {
				endDateTime = new DateTimeType(endDateStr);
			} catch (Exception e) {
				throw new InvalidRequestException("Invalid end date. Use format YYYY-MM-DD.");
			}

			// Filter events by occurred date
			events = events.stream()
				.filter(auditEvent -> {
					Date occurredDate = null;
					if (auditEvent.hasOccurredPeriod() && auditEvent.getOccurredPeriod().hasStart()) {
						occurredDate = auditEvent.getOccurredPeriod().getStart();
					} else if (auditEvent.hasOccurredDateTimeType()) {
						occurredDate = auditEvent.getOccurredDateTimeType().getValue();
					}
					if (occurredDate == null) {
						return false;
					}
					boolean isAfterStart = !occurredDate.before(startDateTime.getValue());
					boolean isBeforeEnd = !occurredDate.after(endDateTime.getValue());
					return isAfterStart && isBeforeEnd;
				})
				.collect(Collectors.toList());
		}
		return events;
	}

	public static List<AuditEvent> filterByActor(List<AuditEvent> events, List<String> actorReferenceLst) {

		if (actorReferenceLst == null || actorReferenceLst.get(0).trim().isEmpty()) {
			return events;
		}

		List<AuditEvent> returnVal = new java.util.ArrayList<>();
		for (String actorReference : actorReferenceLst) {
			List<AuditEvent> temp = events.stream()
				.filter(auditEvent -> auditEvent.getAgent().stream()
					.anyMatch(agent -> agent.hasWho()
						&& actorReference.equals(agent.getWho().getReference())))
				.collect(Collectors.toList());

			returnVal = Stream.concat(returnVal.stream(), temp.stream())
				.collect(Collectors.toList());
		}
		Set<AuditEvent> uniqueEvents = new java.util.HashSet<>(returnVal);
		returnVal.clear();
		returnVal.addAll(uniqueEvents);
		return returnVal;
	}


	public static List<AuditEvent> filterByPatientVisit(List<AuditEvent> events, List<String> patientVisitReferenceLst) {
		if (patientVisitReferenceLst == null || patientVisitReferenceLst.get(0).trim().isEmpty()) {
			return events;
		}
		List<AuditEvent> returnVal = new java.util.ArrayList<>();
		for (String patientVisitReference : patientVisitReferenceLst) {
			List<AuditEvent> temp = events.stream()
				.filter(auditEvent -> auditEvent.hasEncounter()
					&& patientVisitReference.equals(auditEvent.getEncounter().getReference()))
				.collect(Collectors.toList());

			returnVal = Stream.concat(returnVal.stream(), temp.stream())
				.collect(Collectors.toList());
		}
		Set<AuditEvent> uniqueEvents = new java.util.HashSet<>(returnVal);
		returnVal.clear();
		returnVal.addAll(uniqueEvents);
		return returnVal;
	}

	public static List<AuditEvent> filterByConformance(List<AuditEvent> events, List<String> conformanceLst) {
		if (conformanceLst == null || conformanceLst.get(0).trim().isEmpty()) {
			return events;
		}
		List<AuditEvent> returnVal = new java.util.ArrayList<>();
		for (String conformance : conformanceLst){
			List<AuditEvent> temp = events.stream()
				.filter(auditEvent -> auditEvent.hasCode()
					&& (auditEvent.getCode().getCoding().stream()
					.anyMatch(coding -> conformance.equals(coding.getCode()))
					|| conformance.equals(auditEvent.getCode().getText())))
				.collect(Collectors.toList());

			returnVal = Stream.concat(returnVal.stream(), temp.stream())
				.collect(Collectors.toList());
		}
		Set<AuditEvent> uniqueEvents = new java.util.HashSet<>(returnVal);
		returnVal.clear();
		returnVal.addAll(uniqueEvents);
		return returnVal;

	}

	protected void enrichAuditEventWithProfile(AuditEvent auditEvent) {
		if (!auditEvent.hasMeta()) {
			auditEvent.setMeta(new Meta());
		}
		auditEvent.getMeta().setSource("AISTPICAAuditEventCarePathway");
		auditEvent.getMeta().addProfile("http://hl7.at/fhir/AISTPICA/R5/StructureDefinition/aist-pica-auditevent-carepathway");
	}


}
