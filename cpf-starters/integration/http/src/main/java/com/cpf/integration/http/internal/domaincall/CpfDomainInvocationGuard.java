package com.cpf.integration.http.internal.domaincall;

import com.cpf.core.api.context.CpfContext;
import com.cpf.core.api.context.CpfContexts;
import com.cpf.core.api.error.CpfFrameworkErrorCode;
import com.cpf.foundation.execution.api.CpfOperationAccessPolicy;
import com.cpf.integration.api.domaincall.CpfDomainOperation;
import com.cpf.web.api.CpfHttpHeaders;
import com.cpf.web.context.CpfHeaderValidationException;
import com.cpf.web.context.CpfHttpHeaderNames;
import com.cpf.web.context.CpfHttpIngressTrust;
import com.cpf.web.context.CpfRuntimeIdentity;
import com.cpf.web.runtime.CpfWebContextFilter;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Remote Domain transport에서 실제 Operation 호출 직전에 Canonical 접근정책을 강제합니다.
 * Generic transport Controller를 업무 Online Controller로 위장하지 않고, Registry가 resolve한
 * 실제 Domain Operation을 정책 평가 대상으로 사용합니다.
 */
final class CpfDomainInvocationGuard {
    private final List<CpfOperationAccessPolicy> policies;
    private final CpfRuntimeIdentity runtime;

    CpfDomainInvocationGuard(List<CpfOperationAccessPolicy> policies, CpfRuntimeIdentity runtime) {
        this.policies = policies == null ? List.of() : List.copyOf(policies);
        this.runtime = runtime;
    }

    void verify(HttpServletRequest request, CpfDomainOperation<?, ?> operation) {
        Object trust = request.getAttribute(CpfWebContextFilter.INGRESS_TRUST_ATTRIBUTE);
        if (trust != CpfHttpIngressTrust.TRUSTED_INTERNAL) {
            throw new CpfHeaderValidationException(CpfFrameworkErrorCode.INVALID_TRANSACTION_METADATA,
                    CpfHttpHeaderNames.CALLER_CHANNEL,
                    "Remote CPF Domain transport requires a trusted internal caller.",
                    403, "DOMAIN_CALLER_NOT_TRUSTED");
        }
        Object captured = request.getAttribute(CpfWebContextFilter.RECEIVED_HEADERS_ATTRIBUTE);
        if (!(captured instanceof CpfHttpHeaders headers)) {
            throw new CpfHeaderValidationException(CpfFrameworkErrorCode.INVALID_TRANSACTION_METADATA,
                    CpfHttpHeaderNames.TARGET_OPERATION_ID,
                    "Trusted Domain request has no captured canonical CPF headers.",
                    503, "DOMAIN_HEADERS_UNAVAILABLE");
        }
        CpfDomainOperationAccessGuard.verifyResolvedContract(headers, operation, runtime);

        CpfContext context = CpfContexts.requireCurrent();
        if (policies.size() != 1) {
            throw new CpfHeaderValidationException(CpfFrameworkErrorCode.INVALID_TRANSACTION_METADATA,
                    CpfHttpHeaderNames.TARGET_OPERATION_ID,
                    "Canonical operation access policy runtime is unavailable or ambiguous.",
                    503, "OPERATION_POLICY_UNAVAILABLE");
        }
        Object callerSystemAttribute = request.getAttribute(CpfWebContextFilter.VERIFIED_CALLER_SYSTEM_ATTRIBUTE);
        String trustedCallerSystem = callerSystemAttribute instanceof String value && !value.isBlank() ? value.trim() : null;
        CpfOperationAccessPolicy.Decision decision = policies.getFirst().evaluate(new CpfOperationAccessPolicy.Request(
                operation.operationId(), trustedCallerSystem, runtime.systemCode(),
                context.callerChannel(), false, false, true));
        if (!decision.allowed()) {
            int status = "CALLER_NOT_REGISTERED".equals(decision.reasonCode())
                    || "CALLER_DISABLED".equals(decision.reasonCode()) ? 403 : 409;
            throw new CpfHeaderValidationException(CpfFrameworkErrorCode.INVALID_TRANSACTION_METADATA,
                    CpfHttpHeaderNames.TARGET_OPERATION_ID,
                    "Domain operation access policy denied request: " + decision.reasonCode(),
                    status, decision.reasonCode());
        }
        request.setAttribute("cpf.operation.policy-version", decision.policyVersion());
    }
}
