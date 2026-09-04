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
        // System6 receiver 검증의 기준은 host topology가 아니라 실제 resolved Operation owner다.
        // 따라서 1-WAS가 자체 SystemCode를 가지지 않아도 MBR/EXS 등 Domain Operation의 Header
        // System/Target 값은 반드시 해당 Operation의 canonical SystemCode와 일치해야 한다.
        assertSystem(headers.getRequired(CpfHttpHeaderNames.SYSTEM_CODE), operation.systemCode(),
                CpfHttpHeaderNames.SYSTEM_CODE, "SYSTEM_CODE_MISMATCH");
        assertSystem(headers.getRequired(CpfHttpHeaderNames.TARGET_SYSTEM_CODE), operation.systemCode(),
                CpfHttpHeaderNames.TARGET_SYSTEM_CODE, "TARGET_SYSTEM_CODE_MISMATCH");

        // 별도 Runtime이면 그 Runtime도 자기가 호스팅하는 Domain Operation과 일치해야 한다.
        // 1-WAS topology는 System이 아니므로 null을 가상 값으로 보정하지 않는다.
        String runtimeSystem = runtime.systemCode();
        if (runtimeSystem != null && !runtimeSystem.isBlank()
                && !runtimeSystem.equalsIgnoreCase(operation.systemCode())) {
            throw rejected(CpfHttpHeaderNames.TARGET_SYSTEM_CODE,
                    "Resolved Domain Operation does not belong to this runtime System identity.",
                    "DOMAIN_OPERATION_SYSTEM_MISMATCH");
        }
    }

    private static void assertSystem(String actual, String expected, String header, String category) {
        if (expected == null || expected.isBlank() || !expected.equalsIgnoreCase(actual)) {
            throw rejected(header, "Domain Operation owner System does not match the canonical request header.", category);
        }
    }

    private static CpfHeaderValidationException rejected(String header, String message, String category) {
        return new CpfHeaderValidationException(CpfFrameworkErrorCode.INVALID_TRANSACTION_METADATA,
                header, message, 409, category);
    }
}
