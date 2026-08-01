package com.cpf.reference.optional.operations.config;
import com.cpf.reference.edu.runtime.application.*;
import com.cpf.reference.optional.operations.reuse.EduAdm01Handler;
import com.cpf.reference.optional.operations.query.EduAdm02Handler;
import com.cpf.reference.optional.operations.command.EduAdm03Handler;
import com.cpf.reference.optional.operations.approval.EduAdm04Handler;
import com.cpf.reference.optional.operations.asyncoperation.EduAdm05Handler;
import com.cpf.reference.optional.operations.partialrecovery.EduAdm06Handler;
import com.cpf.reference.optional.operations.customscreen.EduAdm07Handler;
import com.cpf.reference.optional.operations.search.EduAdm08Handler;
import com.cpf.reference.optional.operations.detail.EduAdm09Handler;
import com.cpf.reference.optional.operations.bulk.EduAdm10Handler;
import com.cpf.reference.optional.operations.configuration.EduAdm11Handler;
import com.cpf.reference.optional.operations.incident.EduAdm12Handler;
import com.cpf.reference.optional.operations.evidence.EduAdm13Handler;
import com.cpf.reference.optional.operations.topology.EduAdm14Handler;
import com.cpf.reference.optional.operations.correlation.EduAdm15Handler;
import com.cpf.reference.optional.operations.notification.EduAdm16Handler;
import com.cpf.reference.optional.operations.session.EduAdm17Handler;
import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import java.util.*;
/** Product-independent reference-operations EDU contribution owned only by cpf-reference. */
@Component
@ConditionalOnProperty(name="cpf.reference.features.operations.enabled",havingValue="true",matchIfMissing=true)
public final class ReferenceOperationsCapabilityContributor implements EduCapabilityContributor {
    @Override public String featureId() { return "reference-operations"; }
    @Override public Collection<? extends AbstractEduCapabilityHandler> handlers() {
        return List.of(new EduAdm01Handler(), new EduAdm02Handler(), new EduAdm03Handler(), new EduAdm04Handler(), new EduAdm05Handler(), new EduAdm06Handler(), new EduAdm07Handler(), new EduAdm08Handler(), new EduAdm09Handler(), new EduAdm10Handler(), new EduAdm11Handler(), new EduAdm12Handler(), new EduAdm13Handler(), new EduAdm14Handler(), new EduAdm15Handler(), new EduAdm16Handler(), new EduAdm17Handler());
    }
}
