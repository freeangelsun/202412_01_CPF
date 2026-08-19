package com.cpf.integration.http.internal.domaincall;

import com.cpf.core.api.error.CpfFrameworkErrorCode;
import com.cpf.integration.api.domaincall.CpfDomainOperation;
import com.cpf.integration.api.domaincall.CpfDomainOperationRegistry;
import com.cpf.web.api.CpfHttpHeaders;
import com.cpf.web.context.CpfHeaderValidationException;
import com.cpf.web.context.CpfHttpHeaderNames;
import com.cpf.web.context.CpfHttpIngressTrust;
import com.cpf.web.context.CpfRuntimeIdentity;
import com.cpf.web.runtime.CpfWebContextFilter;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Remote Domain transport의 trust/Header/contract 경계를 검증합니다.
 * 실제 Operation/System/Domain/Channel Policy는 Local/Remote 공통 Operation Registry가 실행 직전에 강제합니다.
 */
final class CpfDomainInvocationGuard {
    private final CpfRuntimeIdentity runtime;

    CpfDomainInvocationGuard(CpfRuntimeIdentity runtime) {
        this.runtime = runtime;
    }

    CpfDomainOperationRegistry.InvocationMetadata verify(
            HttpServletRequest request, CpfDomainOperation<?, ?> operation) {
        Object trust = request.getAttribute(CpfWebContextFilter.INGRESS_TRUST_ATTRIBUTE);
        if (trust != CpfHttpIngressTrust.TRUSTED_INTERNAL) {
            throw new CpfHeaderValidationException(CpfFrameworkErrorCode.INVALID_TRANSACTION_METADATA,
                    CpfHttpHeaderNames.CALLER_SYSTEM_CODE,
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

        Object callerSystemAttribute = request.getAttribute(CpfWebContextFilter.VERIFIED_CALLER_SYSTEM_ATTRIBUTE);
        String trustedCallerSystem = callerSystemAttribute instanceof String value && !value.isBlank()
                ? value.trim() : null;
        return CpfDomainOperationRegistry.InvocationMetadata.trustedInternal(trustedCallerSystem);
    }
}
