package com.cpf.platform.operations.observability.spi.logging;

import com.cpf.platform.operations.observability.api.logging.policy.CpfLogPolicyVersionAuditEvent;

/** Durable append-only audit boundary for versioned log-policy mutations. */
@FunctionalInterface
/** CpfLogPolicyVersionAuditSink 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public interface CpfLogPolicyVersionAuditSink {
    void record(CpfLogPolicyVersionAuditEvent event);
}
