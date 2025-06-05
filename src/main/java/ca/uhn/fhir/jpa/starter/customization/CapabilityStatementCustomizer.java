package ca.uhn.fhir.jpa.starter.customization;

import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.interceptor.api.Pointcut;
import org.hl7.fhir.instance.model.api.IBaseConformance;
import org.hl7.fhir.r4.model.CapabilityStatement;

import java.util.List;

@Interceptor
public class CapabilityStatementCustomizer {

	@Hook(Pointcut.SERVER_CAPABILITY_STATEMENT_GENERATED)
	public void customize(IBaseConformance theCapabilityStatement) {

		if (theCapabilityStatement instanceof CapabilityStatement) {
			CapabilityStatement cs = (CapabilityStatement) theCapabilityStatement;

			cs.getSoftware()
				.setName("Custom HAPI Server")
				.setVersion("1.0");

			cs.getImplementation()
				.setDescription("FHIR Server with extended capabilities and custom profiles");

			cs.getRest().forEach(rest -> {

				rest.getResource().stream()
					.filter(resource -> "AuditEvent".equals(resource.getType()))
					.forEach(resource -> resource.addSupportedProfile(
						"http://example.org/fhir/StructureDefinition/AuditEvent"));

				rest.getResource().stream()
					.filter(resource -> "Patient".equals(resource.getType()))
					.forEach(resource -> resource.addSupportedProfile(
						"http://example.org/fhir/StructureDefinition/Patient"));

				rest.getResource().stream()
					.filter(resource -> "Practitioner".equals(resource.getType()))
					.forEach(resource -> resource.addSupportedProfile(
						"http://example.org/fhir/StructureDefinition/Practitioner"));

				rest.getResource().forEach(resource -> {
					resource.addInteraction()
						.setCode(CapabilityStatement.TypeRestfulInteraction.READ);
					resource.addInteraction()
						.setCode(CapabilityStatement.TypeRestfulInteraction.SEARCHTYPE);
				});

				rest.getSecurity()
					.setCors(true)
					.addService()
					.getCodingFirstRep()
					.setSystem("http://hl7.org/fhir/restful-security-service")
					.setCode("SMART-on-FHIR")
					.setDisplay("SMART-on-FHIR Security");
			});

		}
	}
}