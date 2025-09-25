package ca.uhn.fhir.jpa.starter.providers;

import ca.uhn.fhir.jpa.api.dao.IFhirResourceDao;
import ca.uhn.fhir.jpa.searchparam.SearchParameterMap;
import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.OperationParam;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.AuditEvent;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r5.model.DateTimeType;
import org.springframework.beans.factory.annotation.Autowired;
import science.aist.gtf.transformation.Transformer;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ca.uhn.fhir.jpa.starter.providers.AuditEventResourceProviderR5.*;

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
	public void toXes(
		@OperationParam(name = "reasonCode", min = 0, max = 1) String reasonCode,
		@OperationParam(name = "carePlan", min=0) String carePlan,
		@OperationParam(name = "start", min = 0, max = 1) String startDateStr,
		@OperationParam(name = "end", min = 0, max = 1) String endDateStr,
		@OperationParam(name = "core", min = 0) List<String> patientStr,
		@OperationParam(name = "actorRef", min = 0, max = 1) List<String>  actorRef,
		@OperationParam(name = "patientvisit", min = 0) List<String> patientvisit,
		@OperationParam(name = "conformance", min = 0) List<String> conformance,
		HttpServletResponse theServletResponse) throws IOException {

		List<org.hl7.fhir.r5.model.AuditEvent> ae = filterAuditEventsByReasonCode(reasonCode);

		List<org.hl7.fhir.r5.model.AuditEvent> events = ae.stream()
			.filter(Objects::nonNull)
			.map(org.hl7.fhir.r5.model.AuditEvent.class::cast)
			.filter(auditEvent -> auditEvent.hasPatient() && auditEvent.getPatient() != null)
			.filter(auditEvent -> auditEvent.hasCode() && auditEvent.getCode() != null)
			.filter(org.hl7.fhir.r5.model.AuditEvent::hasOccurred)
			.collect(Collectors.toList());



		events = filterByPatient(events,patientStr);
		events = filterByTime(events,startDateStr,endDateStr);
		events = filterByActor(events, actorRef);
		events = filterByPatientVisit(events, patientvisit);
		events = filterByConformance(events,conformance);

		super.toXes(reasonCode, events, theServletResponse);
	}


	@Operation(name = "$addCarePathwayProfile", idempotent = true, type = AuditEvent.class)
	public Bundle addCarePathwayProfileToAuditEvent() {
		IBundleProvider search = myAuditEventDao.search(SearchParameterMap.newSynchronous());

		List<org.hl7.fhir.r4.model.AuditEvent> collect = search.getAllResources()
			.stream()
			.map(org.hl7.fhir.r4.model.AuditEvent.class::cast)
			.collect(Collectors.toList());

		collect.forEach(this::addCarePathwayProfile);

		Bundle bundle = new Bundle();
		for (org.hl7.fhir.r4.model.AuditEvent event : collect) {
			bundle.addEntry().setResource(event);
		}
		return bundle;
	}

	@Operation(name = "$addCoreProfile", idempotent = true, type = AuditEvent.class)
	public Bundle addCoreProfileToAuditEvent() {
		IBundleProvider search = myAuditEventDao.search(SearchParameterMap.newSynchronous());

		List<org.hl7.fhir.r4.model.AuditEvent> collect = search.getAllResources()
			.stream()
			.map(org.hl7.fhir.r4.model.AuditEvent.class::cast)
			.collect(Collectors.toList());

		collect.forEach(this::addCoreProfile);

		Bundle bundle = new Bundle();
		for (org.hl7.fhir.r4.model.AuditEvent event : collect) {
			bundle.addEntry().setResource(event);
		}
		return bundle;
	}

	@Operation(name = "$addActorProfile", idempotent = true, type = AuditEvent.class)
	public Bundle addActorProfileToAuditEvent() {
		IBundleProvider search = myAuditEventDao.search(SearchParameterMap.newSynchronous());

		List<org.hl7.fhir.r4.model.AuditEvent> collect = search.getAllResources()
			.stream()
			.map(org.hl7.fhir.r4.model.AuditEvent.class::cast)
			.collect(Collectors.toList());

		collect.forEach(this::addActorProfile);

		Bundle bundle = new Bundle();
		for (org.hl7.fhir.r4.model.AuditEvent event : collect) {
			bundle.addEntry().setResource(event);
		}
		return bundle;
	}

	@Operation(name = "$addConformanceProfile", idempotent = true, type = AuditEvent.class)
	public Bundle addConformanceProfileToAuditEvent() {
		IBundleProvider search = myAuditEventDao.search(SearchParameterMap.newSynchronous());

		List<org.hl7.fhir.r4.model.AuditEvent> collect = search.getAllResources()
			.stream()
			.map(org.hl7.fhir.r4.model.AuditEvent.class::cast)
			.collect(Collectors.toList());

		collect.forEach(this::addConformanceProfile);

		Bundle bundle = new Bundle();
		for (org.hl7.fhir.r4.model.AuditEvent event : collect) {
			bundle.addEntry().setResource(event);
		}
		return bundle;
	}

	@Operation(name = "$addPatientVisitProfile", idempotent = true, type = AuditEvent.class)
	public Bundle addPatientVisitProfileToAuditEvent() {
		IBundleProvider search = myAuditEventDao.search(SearchParameterMap.newSynchronous());

		List<org.hl7.fhir.r4.model.AuditEvent> collect = search.getAllResources()
			.stream()
			.map(org.hl7.fhir.r4.model.AuditEvent.class::cast)
			.collect(Collectors.toList());

		collect.forEach(this::addPatientVisitProfile);

		Bundle bundle = new Bundle();
		for (org.hl7.fhir.r4.model.AuditEvent event : collect) {
			bundle.addEntry().setResource(event);
		}
		return bundle;
	}

	@Operation(name = "$ocel", manualResponse = true, idempotent = true)
	public void toOcel(
		@OperationParam(name = "start", min = 0, max = 1) String startDateStr,
		@OperationParam(name = "end", min = 0, max = 1) String endDateStr,
		@OperationParam(name = "core", min = 0, max = 1) List<String> patientStr, //PatientId more than one
		@OperationParam(name = "actor", min = 0) List<String> actorRef,
		@OperationParam(name = "patientvisit", min = 0) List<String> patientvisit,
		@OperationParam(name = "conformance", min = 0) List<String> conformance,
		HttpServletResponse theServletResponse) throws IOException {
		IBundleProvider search = myAuditEventDao.search(SearchParameterMap.newSynchronous());
		List<org.hl7.fhir.r5.model.AuditEvent> collect = search.getAllResources().stream().map(AuditEvent.class::cast).map(auditEventR4ToR5Transformer::applyTransformation).collect(Collectors.toList());
		collect = filterByPatient(collect,patientStr);
		collect = filterByTime(collect,startDateStr,endDateStr);
		collect = filterByActor(collect, actorRef);
		collect = filterByPatientVisit(collect, patientvisit);
		collect = filterByConformance(collect,conformance);
		super.toOcel(collect, theServletResponse);
	}

	@Operation(name = "$dfg", manualResponse = true, idempotent = true)
	public void toDfg(
		@OperationParam(name = "reasonCode", min = 0, max = 1) String reasonCode,
		@OperationParam(name = "carePlan", min=0) String carePlan,
		@OperationParam(name = "start", min = 0, max = 1) String startDateStr,
		@OperationParam(name = "end", min = 0, max = 1) String endDateStr,
		@OperationParam(name = "core", min = 0) List<String> patientStr, //PatientId more than one
		@OperationParam(name = "actorRef", min = 0, max = 1) List<String>  actorRef,
		@OperationParam(name = "patientvisit", min = 0) List<String> patientvisit,
		@OperationParam(name = "conformance", min = 0) List<String> conformance,
		@OperationParam(name = "grouping", max = 1) String grouping,
		HttpServletResponse theServletResponse) throws IOException {

		List<org.hl7.fhir.r5.model.AuditEvent> ae = filterAuditEventsByReasonCode(reasonCode);

		List<org.hl7.fhir.r5.model.AuditEvent> events = ae.stream()
			.filter(Objects::nonNull)
			.map(org.hl7.fhir.r5.model.AuditEvent.class::cast)
			.filter(auditEvent -> auditEvent.hasPatient() && auditEvent.getPatient() != null)
			.filter(auditEvent -> auditEvent.hasCode() && auditEvent.getCode() != null)
			.filter(org.hl7.fhir.r5.model.AuditEvent::hasOccurred)
			.collect(Collectors.toList());


		events = filterByPatient(events,patientStr);
		events = filterByTime(events,startDateStr,endDateStr);
		events = filterByActor(events, actorRef);
		events = filterByPatientVisit(events, patientvisit);
		events = filterByConformance(events,conformance);

		super.toDfg(events, grouping, theServletResponse);
	}


	private List<org.hl7.fhir.r5.model.AuditEvent> filterAuditEventsByReasonCode(String reasonCode) {
		IBundleProvider search = myAuditEventDao.search(SearchParameterMap.newSynchronous());

		Stream<AuditEvent> auditEventStream = search.getAllResources()
			.stream()
			.map(AuditEvent.class::cast)
			.filter(Objects::nonNull);

		if (reasonCode != null && !reasonCode.isBlank() && !reasonCode.isEmpty()) {
			auditEventStream = auditEventStream.filter(ae -> {
				var reference = (Reference) ae.getExtensionByUrl("http://fhir.r5.extensions/encounter").getValue();
				var id = new IdType(reference.getReference());
				var encounter = myEncounterDao.read(id);
				return encounter.getReasonCodeFirstRep().getCoding().stream().anyMatch(c -> reasonCode.equals(c.getCode()));
			});
		}

		// no filtering ftm

		return  auditEventStream.map(auditEventR4ToR5Transformer::applyTransformation)
			.collect(Collectors.toList());

	}

	@Operation(name = "$filterByTime", idempotent = true, type = org.hl7.fhir.r4.model.AuditEvent.class)
	public org.hl7.fhir.r4.model.Bundle filterAuditEventsByStartAndEndDate(
		@OperationParam(name = "start", min = 1, max = 1) String startDateStr,
		@OperationParam(name = "end", min = 0, max = 1) String endDateStr) {

		if (startDateStr == null || startDateStr.trim().isEmpty()) {
			throw new InvalidRequestException("Start date must be provided.");
		}
		// Retrieve AuditEvents
		IBundleProvider search = myAuditEventDao.search(SearchParameterMap.newSynchronous());
		List<org.hl7.fhir.r4.model.AuditEvent> filteredEvents = search.getAllResources().stream()
			.filter(Objects::nonNull)
			.filter(org.hl7.fhir.r4.model.AuditEvent.class::isInstance)
			.map(org.hl7.fhir.r4.model.AuditEvent.class::cast)
			.filter(org.hl7.fhir.r4.model.AuditEvent::hasRecorded)
			.collect(Collectors.toList());

		//TODO why do i need that

		// filteredEvents.forEach(this::enrichAuditEventWithProfile);
		filteredEvents = filterByTimeR4(filteredEvents,startDateStr,endDateStr);

		org.hl7.fhir.r4.model.Bundle bundle = new org.hl7.fhir.r4.model.Bundle();
		for (org.hl7.fhir.r4.model.AuditEvent event : filteredEvents) {
			bundle.addEntry().setResource(event);
		}
		return bundle;
	}


	//ToDO validate if that filter makes sense => recorded time has been used instead of occured time
	public static List<org.hl7.fhir.r4.model.AuditEvent> filterByTimeR4(List<org.hl7.fhir.r4.model.AuditEvent> events, String startDateStr, String endDateStr){

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
					Date recordedDate = null;

					if (auditEvent.hasRecorded())
						recordedDate = auditEvent.getRecorded();

					if (recordedDate == null)
						return false;

					boolean isAfterStart = !recordedDate.before(startDateTime.getValue());
					boolean isBeforeEnd = !recordedDate.after(endDateTime.getValue());
					return isAfterStart && isBeforeEnd;
				})
				.collect(Collectors.toList());
		}
		return events;
	}

	protected void addCarePathwayProfile(org.hl7.fhir.r4.model.AuditEvent ae) {
		if (!ae.hasMeta()) ae.setMeta(new org.hl7.fhir.r4.model.Meta());
		ae.getMeta().setSource("AISTPICAAuditEventCarePathway");
		ae.getMeta().addProfile("http://hl7.at/fhir/AISTPICA/R4/StructureDefinition/aist-pica-auditevent-carepathway");
	}

	protected void addCoreProfile(org.hl7.fhir.r4.model.AuditEvent ae) {
		if (!ae.hasMeta()) ae.setMeta(new org.hl7.fhir.r4.model.Meta());
		ae.getMeta().setSource("AISTPICAAuditEventCore");
		ae.getMeta().addProfile("http://hl7.at/fhir/AISTPICA/R4/StructureDefinition/aist-pica-auditevent-core");
	}

	protected void addActorProfile(org.hl7.fhir.r4.model.AuditEvent ae) {
		if (!ae.hasMeta()) ae.setMeta(new org.hl7.fhir.r4.model.Meta());
		ae.getMeta().setSource("AISTPICAAuditEventActor");
		ae.getMeta().addProfile("http://hl7.at/fhir/AISTPICA/R4/StructureDefinition/aist-pica-auditevent-actor");
	}

	protected void addConformanceProfile(org.hl7.fhir.r4.model.AuditEvent ae) {
		if (!ae.hasMeta()) ae.setMeta(new org.hl7.fhir.r4.model.Meta());
		ae.getMeta().setSource("AISTPICAAuditEventConformance");
		ae.getMeta().addProfile("http://hl7.at/fhir/AISTPICA/R4/StructureDefinition/aist-pica-auditevent-conformance");
	}

	protected void addPatientVisitProfile(org.hl7.fhir.r4.model.AuditEvent ae) {
		if (!ae.hasMeta()) ae.setMeta(new org.hl7.fhir.r4.model.Meta());
		ae.getMeta().setSource("AISTPICAAuditEventPatientVisit");
		ae.getMeta().addProfile("http://hl7.at/fhir/AISTPICA/R4/StructureDefinition/aist-pica-auditevent-patientvisit");
	}


}
