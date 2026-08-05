package com.cpf.core.spi.security;

import com.cpf.core.api.security.CpfMaskingPolicyAuditEvent;

/** Audit persistence for dangerous masking-policy changes. */
@FunctionalInterface
public interface CpfMaskingPolicyAuditSink {
    void record(CpfMaskingPolicyAuditEvent event);
}
