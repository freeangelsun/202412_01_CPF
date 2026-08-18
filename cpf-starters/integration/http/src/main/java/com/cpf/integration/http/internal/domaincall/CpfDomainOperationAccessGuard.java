package com.cpf.integration.http.internal.domaincall;

import com.cpf.core.api.error.CpfFrameworkErrorCode;
import com.cpf.integration.api.domaincall.CpfDomainOperation;
import com.cpf.web.api.CpfHttpHeaders;
import com.cpf.web.context.CpfHeaderValidationException;
import com.cpf.web.context.CpfHttpHeaderNames;
import com.cpf.web.context.CpfRuntimeIdentity;
import java.util.Objects;

/** Registry가 resolve한 Domain 계약과 trusted wire target을 비교하는 내부 검증기입니다. */
final class CpfDomainOperationAccessGuard {
    private CpfDomainOperationAccessGuard() {}

    static void verifyResolvedContract(CpfHttpHeaders headers, CpfDomainOperation<?, ?> operation, CpfRuntimeIdentity runtime) {
        Objects.requireNonNull(headers, "headers");
        Objects.requireNonNull(operation, "operation");
        Objects.requireNonNull(runtime, "runtime");
        String declaredOperation = headers.getRequired(CpfHttpHeaderNames.TARGET_OPERATION_ID);
        if (!operation.operationId().equals(declaredOperation)) {
            throw rejected(CpfHttpHeaderNames.TARGET_OPERATION_ID,
                    "Target operation header does not match the resolved Domain operation.",
                    "TARGET_OPERATION_MISMATCH");
        }
        if (!runtime.currentChannel().equalsIgnoreCase(operation.systemCode())) {
            throw rejected(CpfHttpHeaderNames.TARGET_CHANNEL,
                    "Resolved Domain Operation does not belong to this runtime Channel identity.",
                    "DOMAIN_OPERATION_SYSTEM_MISMATCH");
        }
    }

    private static CpfHeaderValidationException rejected(String header, String message, String category) {
        return new CpfHeaderValidationException(CpfFrameworkErrorCode.INVALID_TRANSACTION_METADATA,
                header, message, 409, category);
    }
}
