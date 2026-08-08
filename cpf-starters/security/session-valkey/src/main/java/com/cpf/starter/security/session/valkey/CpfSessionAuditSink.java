package com.cpf.starter.security.session.valkey;
@FunctionalInterface
public interface CpfSessionAuditSink {
    void record(String action, String sessionId, String principalId, String reason);
    CpfSessionAuditSink NOOP = (action, sessionId, principalId, reason) -> {};
}
