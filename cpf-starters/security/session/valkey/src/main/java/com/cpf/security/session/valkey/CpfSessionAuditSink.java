package com.cpf.security.session.valkey;

/** 분산 Session의 생성·갱신·회전·강제 로그아웃 감사 sink. */
@FunctionalInterface
public interface CpfSessionAuditSink {
    void record(String action, String sessionId, String principalId, String reason);
    CpfSessionAuditSink NOOP = (action, sessionId, principalId, reason) -> { };
}
