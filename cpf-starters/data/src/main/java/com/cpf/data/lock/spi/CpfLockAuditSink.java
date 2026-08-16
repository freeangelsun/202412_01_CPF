package com.cpf.data.lock.spi;

import java.time.Instant;

@FunctionalInterface
/** CpfLockAuditSink 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public interface CpfLockAuditSink {
    void record(AuditEvent event);

    default boolean available() {
        return true;
    }

    record AuditEvent(
            String auditId, String action, String key, String actorId, String approverId,
            String reason, String approvalId, long fencingToken, Instant occurredAt, String result) {}

    static CpfLockAuditSink unavailable() {
        return new CpfLockAuditSink() {
            @Override
            public void record(AuditEvent event) {
                throw new IllegalStateException("lock audit sink is unavailable");
            }
            @Override
            public boolean available() {
                return false;
            }
        };
    }
}
