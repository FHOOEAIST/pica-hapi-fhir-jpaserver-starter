package ca.uhn.fhir.jpa.starter.providers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.api.dao.IFhirResourceDao;
import ca.uhn.fhir.jpa.searchparam.SearchParameterMap;
import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.OperationParam;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r5.model.AuditEvent;
import org.hl7.fhir.r5.model.Bundle;
import org.hl7.fhir.r5.model.DateTimeType;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
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
	public void toXes(
			@OperationParam(name = "planDefinition") String planDefinition,
			@OperationParam(name = "grouping", max = 1) String grouping,
			@OperationParam(name = "start", min = 0, max = 1) String startDateStr,
			@OperationParam(name = "end", min = 0, max = 1) String endDateStr,
			@OperationParam(name = "patientId", min = 0, max = 1) String patientStr,
			HttpServletResponse theServletResponse) throws IOException {

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

		// Filter by patient ID if provided
		if (patientStr != null && !patientStr.trim().isEmpty()) {
			String normalizedPatientId = patientStr.startsWith("Patient/") ? patientStr : "Patient/" + patientStr;

			events = events.stream()
					.filter(auditEvent ->
						normalizedPatientId.equals(auditEvent.getPatient().getIdentifier().getValue()) ||
						normalizedPatientId.equals(auditEvent.getPatient().getReference()))
					.collect(Collectors.toList());

		}

		// Filter by start and end date if a start date is provided
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

		super.toXes(planDefinition, events, grouping, theServletResponse);
	}


	@Operation(name = "$filterByTime", idempotent = true, type = AuditEvent.class)
	public Bundle filterAuditEventsByStartAndEndDate(
			@OperationParam(name = "start", min = 1, max = 1) String startDateStr,
			@OperationParam(name = "end", min = 0, max = 1) String endDateStr) {

		if (startDateStr == null || startDateStr.trim().isEmpty()) {
			throw new InvalidRequestException("Start date must be provided.");
		}

		if (endDateStr == null || endDateStr.trim().isEmpty()) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			endDateStr = sdf.format(new Date());
		}

		DateTimeType startDateTime;
		try {
			startDateTime = new DateTimeType(startDateStr.split("/")[0] );
		} catch (Exception e) {
			throw new InvalidRequestException("Invalid start date format. Use YYYY-MM-DD.");
		}

		DateTimeType endDateTime;
		try {
			endDateTime = new DateTimeType(endDateStr);
		} catch (Exception e) {
			throw new InvalidRequestException("Invalid end date format. Use YYYY-MM-DD.");
		}

		// Retrieve AuditEvents
		IBundleProvider search = myAuditEventDao.search(SearchParameterMap.newSynchronous());
		
		List<AuditEvent> filteredEvents = search.getAllResources().stream()
				.filter(Objects::nonNull)
				.filter(AuditEvent.class::isInstance)
				.map(AuditEvent.class::cast)
				.filter(AuditEvent::hasOccurred)
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

		Bundle bundle = new Bundle();
		for (AuditEvent event : filteredEvents) {
			bundle.addEntry().setResource(event);
		}
		return bundle;
	}

	@Operation(name = "$ocel", manualResponse = true, idempotent = true)
	public void toOcel(@OperationParam(name = "planDefinition") String planDefinition,
							 @OperationParam(name = "grouping", max = 1) String grouping,
							 @OperationParam(name = "start", min = 0, max = 1) String startDateStr,
							 @OperationParam(name = "end", min = 0, max = 1) String endDateStr,
							 @OperationParam(name = "patientId", min = 0, max = 1) String patientStr,
							 HttpServletResponse theServletResponse) throws IOException {

		IBundleProvider search = myAuditEventDao.search(SearchParameterMap.newSynchronous());
		List<AuditEvent> collect = search.getAllResources().stream()
			.map(AuditEvent.class::cast)
			.filter(auditEvent -> auditEvent.hasPatient() && auditEvent.getPatient() != null)
			.filter(auditEvent -> auditEvent.hasCode() && auditEvent.getCode() != null)
			.filter(AuditEvent::hasOccurred)
			.collect(Collectors.toList());

		// Filter by patient ID if provided
		if (patientStr != null && !patientStr.trim().isEmpty()) {
			String normalizedPatientId = patientStr.startsWith("Patient/") ? patientStr : "Patient/" + patientStr;

			collect = collect.stream()
				.filter(auditEvent ->
					normalizedPatientId.equals(auditEvent.getPatient().getIdentifier().getValue()) ||
				 	normalizedPatientId.equals(auditEvent.getPatient().getReference()))
				.collect(Collectors.toList());
		}

		// Filter by start and end date if a start date is provided
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
			collect = collect.stream()
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
		super.toOcel(collect, theServletResponse);

	}

	@Operation(name = "$dfg", manualResponse = true, idempotent = true)
	public void toDfg(@OperationParam(name = "grouping", max = 1) String grouping, HttpServletResponse theServletResponse) throws IOException {
		IBundleProvider search = myAuditEventDao.search(SearchParameterMap.newSynchronous());
		List<AuditEvent> collect = search.getAllResources().stream().map(AuditEvent.class::cast).collect(Collectors.toList());
		super.toDfg(collect, grouping, theServletResponse);
	}
}
