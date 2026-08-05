package com.cpf.core.spi.logging;

import com.cpf.core.api.logging.policy.CpfLogPolicyVersionAuditEvent;

/** Durable append-only audit boundary for versioned log-policy mutations. */
@FunctionalInterface
public interface CpfLogPolicyVersionAuditSink {
    void record(CpfLogPolicyVersionAuditEvent event);
}
