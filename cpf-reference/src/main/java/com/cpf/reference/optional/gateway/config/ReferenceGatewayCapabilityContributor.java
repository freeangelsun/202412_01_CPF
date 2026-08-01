package com.cpf.reference.optional.gateway.config;
import com.cpf.reference.edu.runtime.application.*;
import com.cpf.reference.optional.gateway.servergroup.EduGw01Handler;
import com.cpf.reference.optional.gateway.route.EduGw02Handler;
import com.cpf.reference.optional.gateway.security.EduGw03Handler;
import com.cpf.reference.optional.gateway.resilience.EduGw04Handler;
import com.cpf.reference.optional.gateway.publish.EduGw05Handler;
import com.cpf.reference.optional.gateway.reconcile.EduGw06Handler;
import com.cpf.reference.optional.gateway.registry.EduGw07Handler;
import com.cpf.reference.optional.gateway.health.EduGw08Handler;
import com.cpf.reference.optional.gateway.drain.EduGw09Handler;
import com.cpf.reference.optional.gateway.rejection.EduGw10Handler;
import com.cpf.reference.optional.gateway.version.EduGw11Handler;
import com.cpf.reference.optional.gateway.ratecontrol.EduGw12Handler;
import com.cpf.reference.optional.gateway.audit.EduGw13Handler;
import com.cpf.reference.optional.gateway.recovery.EduGw14Handler;
import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import java.util.*;
/** Product-independent reference-gateway EDU contribution owned only by cpf-reference. */
@Component
@ConditionalOnProperty(name="cpf.reference.features.gateway.enabled",havingValue="true",matchIfMissing=true)
public final class ReferenceGatewayCapabilityContributor implements EduCapabilityContributor {
    @Override public String featureId() { return "reference-gateway"; }
    @Override public Collection<? extends AbstractEduCapabilityHandler> handlers() {
        return List.of(new EduGw01Handler(), new EduGw02Handler(), new EduGw03Handler(), new EduGw04Handler(), new EduGw05Handler(), new EduGw06Handler(), new EduGw07Handler(), new EduGw08Handler(), new EduGw09Handler(), new EduGw10Handler(), new EduGw11Handler(), new EduGw12Handler(), new EduGw13Handler(), new EduGw14Handler());
    }
}
