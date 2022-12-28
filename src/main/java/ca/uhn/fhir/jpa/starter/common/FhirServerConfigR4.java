package ca.uhn.fhir.jpa.starter.common;

import ca.uhn.fhir.jpa.api.dao.IFhirResourceDao;
import ca.uhn.fhir.jpa.config.r4.JpaR4Config;
import ca.uhn.fhir.jpa.starter.annotations.OnR4Condition;
import ca.uhn.fhir.jpa.starter.cql.StarterCqlR4Config;
import ca.uhn.fhir.jpa.starter.interceptors.AuditEventCreatorConsentInterceptor;
import ca.uhn.fhir.jpa.starter.interceptors.creators.*;
import ca.uhn.fhir.jpa.starter.providers.AuditEventResourceProviderR4;
import ca.uhn.fhir.jpa.starter.transformer.AuditEventR4ToR5Transformer;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.hl7.fhir.r4.model.AuditEvent;
import org.hl7.fhir.r4.model.Encounter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import science.aist.fhirauditeventtoocel.FhirAuditEventsToOCELLogService;
import science.aist.fhirauditeventtoxes.FhirAuditEventsToXESLogService;
import science.aist.gtf.transformation.Transformer;

@Configuration
@Conditional(OnR4Condition.class)
@Import({
	JpaR4Config.class,
	StarterJpaConfig.class,
	StarterCqlR4Config.class,
	ElasticsearchConfig.class
})
public class FhirServerConfigR4 {
	@Bean
	public AuditEventCreatorConsentInterceptor auditEventCreatorConsentInterceptor() {
		return new AuditEventCreatorConsentInterceptor();
	}

	@Bean(name = "auditEventResourceProvider")
	public IResourceProvider auditEventResourceProvider() {
		return new AuditEventResourceProviderR4();
	}

	@Bean
	public Transformer<AuditEvent, org.hl7.fhir.r5.model.AuditEvent> auditEventR4ToR5Transformer() {
		return new AuditEventR4ToR5Transformer();
	}

	// creators:

	@Bean
	public ConditionAuditEventCreator conditionAuditEventCreator() {
		return new ConditionAuditEventCreator();
	}

	@Bean
	public CarePlanAuditEventCreator carePlanAuditEventCreator() {
		return new CarePlanAuditEventCreator();
	}

	@Bean
	public ProcedureAuditEventCreator procedureAuditEventCreator(IFhirResourceDao<Encounter> myEncounterDao) {
		return new ProcedureAuditEventCreator(myEncounterDao);
	}

	@Bean
	public DiagnosticReportAuditEventCreator diagnosticReportAuditEventCreator() {
		return new DiagnosticReportAuditEventCreator();
	}

	@Bean
	public ImmunizationAuditEventCreator immunizationAuditEventCreator() {
		return new ImmunizationAuditEventCreator();
	}

	@Bean
	public MedicationRequestAuditEventCreator medicationRequestAuditEventCreator() {
		return new MedicationRequestAuditEventCreator();
	}

	@Bean
	public ObservationAuditEventCreator observationAuditEventCreator() {
		return new ObservationAuditEventCreator();
	}

	@Bean
	public ImagingStudyAuditEventCreator imagingStudyAuditEventCreator() {
		return new ImagingStudyAuditEventCreator();
	}

	@Bean
	public MedicationAdministrationAuditEventCreator medicationAdministrationAuditEventCreator() {
		return new MedicationAdministrationAuditEventCreator();
	}

	@Bean
	public MultiAuditEventCreator multiAuditEventCreator(
		ProcedureAuditEventCreator procedureAuditEventCreator,
		CarePlanAuditEventCreator carePlanAuditEventCreator,
		ConditionAuditEventCreator conditionAuditEventCreator,
		DiagnosticReportAuditEventCreator diagnosticReportAuditEventCreator,
		ImmunizationAuditEventCreator immunizationAuditEventCreator,
		MedicationRequestAuditEventCreator medicationRequestAuditEventCreator,
		ObservationAuditEventCreator observationAuditEventCreator,
		ImagingStudyAuditEventCreator imagingStudyAuditEventCreator,
		MedicationAdministrationAuditEventCreator medicationAdministrationAuditEventCreator
	) {
		return new MultiAuditEventCreator(
			procedureAuditEventCreator,
			carePlanAuditEventCreator,
			conditionAuditEventCreator,
			diagnosticReportAuditEventCreator,
			immunizationAuditEventCreator,
			medicationRequestAuditEventCreator,
			observationAuditEventCreator,
			imagingStudyAuditEventCreator,
			medicationAdministrationAuditEventCreator
		);
	}
}
