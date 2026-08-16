package com.cpf.platform.operations.api.audit;

/** Audit storage/stream provider가 구현하는 append-only Sink입니다. */
@FunctionalInterface
public interface CpfAuditSink {
    void append(CpfAuditEvent event);
}
