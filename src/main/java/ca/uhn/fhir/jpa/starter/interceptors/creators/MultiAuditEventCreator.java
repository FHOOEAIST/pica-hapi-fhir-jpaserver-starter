package ca.uhn.fhir.jpa.starter.interceptors.creators;

import ca.uhn.fhir.jpa.starter.interceptors.AuditEventCreator;
import org.hl7.fhir.r4.model.AuditEvent;
import org.hl7.fhir.r4.model.Resource;
import science.aist.jack.general.util.CastUtils;

import java.util.Arrays;
import java.util.List;

/**
 * <p>TODO class description</p>
 *
 * @author Andreas Pointner
 * @since 1.0
 */
public class MultiAuditEventCreator implements AuditEventCreator<Resource> {

	private final List<AuditEventCreator<?>> creators;

	public MultiAuditEventCreator(AuditEventCreator<?>... auditEventCreators) {
		creators = Arrays.asList(auditEventCreators);
	}

	@Override
	public boolean canCreate(Object o) {
		return true;
	}

	@Override
	public AuditEvent createAuditEvent(Resource elem) {
		return creators.stream()
			.filter(aec -> aec.canCreate(elem))
			.map(aec -> aec.createAuditEvent(CastUtils.cast(elem)))
			.findFirst()
			.orElseGet(() -> null);
	}
}
