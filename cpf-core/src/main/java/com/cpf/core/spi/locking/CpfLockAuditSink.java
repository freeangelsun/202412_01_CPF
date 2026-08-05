package com.cpf.core.spi.locking;

import java.time.Instant;

@FunctionalInterface
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
