package com.cpf.security.spi;

import com.cpf.security.api.CpfMaskingPolicyAuditEvent;

/** Audit persistence for dangerous masking-policy changes. */
@FunctionalInterface
/** CpfMaskingPolicyAuditSink 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public interface CpfMaskingPolicyAuditSink {
    void record(CpfMaskingPolicyAuditEvent event);
}
