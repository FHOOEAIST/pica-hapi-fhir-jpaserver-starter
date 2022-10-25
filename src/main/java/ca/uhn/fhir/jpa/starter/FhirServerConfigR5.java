package ca.uhn.fhir.jpa.starter;

import ca.uhn.fhir.jpa.config.r5.JpaR5Config;
import ca.uhn.fhir.jpa.starter.annotations.OnR5Condition;
import ca.uhn.fhir.jpa.starter.providers.AuditEventResourceProvider;
import ca.uhn.fhir.jpa.starter.providers.AuditEventResourceProviderR5;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Conditional(OnR5Condition.class)
@Import({
	StarterJpaConfig.class,
	JpaR5Config.class,
	ElasticsearchConfig.class
})
public class FhirServerConfigR5 {
	@Bean(name = "auditEventOperations")
	public AuditEventResourceProvider auditEventResourceProvider() {
		return new AuditEventResourceProviderR5();
	}
}
