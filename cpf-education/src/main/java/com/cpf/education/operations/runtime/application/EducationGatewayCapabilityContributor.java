package com.cpf.education.operations.runtime.application;

import com.cpf.education.operations.gateway.servergroup.EduGw01Handler;
import com.cpf.education.operations.gateway.route.EduGw02Handler;
import com.cpf.education.operations.gateway.security.EduGw03Handler;
import com.cpf.education.operations.gateway.resilience.EduGw04Handler;
import com.cpf.education.operations.gateway.publish.EduGw05Handler;
import com.cpf.education.operations.gateway.reconcile.EduGw06Handler;
import com.cpf.education.operations.gateway.registry.EduGw07Handler;
import com.cpf.education.operations.gateway.health.EduGw08Handler;
import com.cpf.education.operations.gateway.drain.EduGw09Handler;
import com.cpf.education.operations.gateway.rejection.EduGw10Handler;
import com.cpf.education.operations.gateway.version.EduGw11Handler;
import com.cpf.education.operations.gateway.ratecontrol.EduGw12Handler;
import com.cpf.education.operations.gateway.audit.EduGw13Handler;
import com.cpf.education.operations.gateway.recovery.EduGw14Handler;
import org.springframework.stereotype.Component;
import java.util.*;

/** education-gateway 기능군의 실행 가능한 Education Handler를 등록합니다. */
@Component
public final class EducationGatewayCapabilityContributor implements EduCapabilityContributor {
    @Override public String featureId() { return "education-gateway"; }
    @Override public Collection<? extends AbstractEduCapabilityHandler> handlers() { return List.of(new EduGw01Handler(), new EduGw02Handler(), new EduGw03Handler(), new EduGw04Handler(), new EduGw05Handler(), new EduGw06Handler(), new EduGw07Handler(), new EduGw08Handler(), new EduGw09Handler(), new EduGw10Handler(), new EduGw11Handler(), new EduGw12Handler(), new EduGw13Handler(), new EduGw14Handler()); }
}
