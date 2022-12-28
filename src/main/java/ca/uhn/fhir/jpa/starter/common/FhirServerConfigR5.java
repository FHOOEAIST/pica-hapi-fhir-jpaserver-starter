package ca.uhn.fhir.jpa.starter.common;

import ca.uhn.fhir.jpa.config.r5.JpaR5Config;
import ca.uhn.fhir.jpa.starter.annotations.OnR5Condition;
import ca.uhn.fhir.jpa.starter.providers.AuditEventResourceProviderR5;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.interceptor.consent.IConsentService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import science.aist.fhirauditeventtoocel.FhirAuditEventsToOCELLogService;
import science.aist.fhirauditeventtoxes.FhirAuditEventsToXESLogService;

@Configuration
@Conditional(OnR5Condition.class)
@Import({
	StarterJpaConfig.class,
	JpaR5Config.class,
	ElasticsearchConfig.class
})
public class FhirServerConfigR5 {

	@Bean
	public FhirAuditEventsToXESLogService fhirAuditEventsToXESLogService() {
		return new FhirAuditEventsToXESLogService();
	}

	@Bean
	public FhirAuditEventsToOCELLogService fhirAuditEventsToOCELLogService() {
		return new FhirAuditEventsToOCELLogService();
	}

	@Bean(name = "auditEventResourceProvider")
	public IResourceProvider auditEventResourceProvider() {
		return new AuditEventResourceProviderR5();
	}

	@Bean
	public IConsentService auditEventCreatorConsentInterceptor() {
		// empty consent service, because we have one in R4 as well, to make sure that we can inject them properly
		return new IConsentService() {
		};
	}
}
