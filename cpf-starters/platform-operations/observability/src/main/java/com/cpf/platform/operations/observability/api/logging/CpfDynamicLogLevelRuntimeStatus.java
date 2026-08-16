package com.cpf.platform.operations.observability.api.logging;

/** Read-only versioned state contract for operations, monitoring and audit consumers. */
/** CpfDynamicLogLevelRuntimeStatus 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public interface CpfDynamicLogLevelRuntimeStatus {
    DynamicLogLevelRuntimeSnapshot snapshot();
}
