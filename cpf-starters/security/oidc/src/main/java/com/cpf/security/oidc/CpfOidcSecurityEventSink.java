package com.cpf.security.oidc;

/** Safe OIDC security audit sink. Raw tokens and arbitrary claims must never be passed here. */
@FunctionalInterface
public interface CpfOidcSecurityEventSink {
    void record(String eventType,String subjectId,String tenantId,String transactionId);
    CpfOidcSecurityEventSink NOOP=(type,subject,tenant,tx)->{};
}
