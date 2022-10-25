package ca.uhn.fhir.jpa.starter;

import ca.uhn.fhir.jpa.config.r4.JpaR4Config;
import ca.uhn.fhir.jpa.starter.annotations.OnR4Condition;
import ca.uhn.fhir.jpa.starter.cql.StarterCqlR4Config;
import ca.uhn.fhir.jpa.starter.interceptors.AuditEventCreatorConsentInterceptor;
import ca.uhn.fhir.jpa.starter.providers.AuditEventResourceProvider;
import ca.uhn.fhir.jpa.starter.providers.AuditEventResourceProviderR4;
import ca.uhn.fhir.jpa.starter.transformer.AuditEventR4ToR5Transformer;
import org.hl7.fhir.r4.model.AuditEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import science.aist.gtf.transformation.Transformer;

@Configuration
@Conditional(OnR4Condition.class)
@Import({
	StarterJpaConfig.class,
	JpaR4Config.class,
	StarterCqlR4Config.class,
	ElasticsearchConfig.class
})
public class FhirServerConfigR4 {
	@Bean
	AuditEventCreatorConsentInterceptor auditEventCreatorConsentInterceptor() {
		return new AuditEventCreatorConsentInterceptor();
	}

	@Bean(name = "auditEventResourceProvider")
	public AuditEventResourceProvider auditEventResourceProvider() {
		return new AuditEventResourceProviderR4();
	}

	@Bean
	public Transformer<AuditEvent, org.hl7.fhir.r5.model.AuditEvent> auditEventR4ToR5Transformer() {
		return new AuditEventR4ToR5Transformer();
	}
}
