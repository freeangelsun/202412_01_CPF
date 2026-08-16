package com.cpf.security.api;

/** Distributed-session operational counters exposed without a provider dependency. */
/** CpfSessionMetricsSnapshot 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public record CpfSessionMetricsSnapshot(
        long creates, long reads, long updates, long revocations,
        long misses, long providerFailures, long forcedLogouts) {
    public CpfSessionMetricsSnapshot {
        if (creates < 0 || reads < 0 || updates < 0 || revocations < 0 || misses < 0
                || providerFailures < 0 || forcedLogouts < 0) {
            throw new IllegalArgumentException("session metrics must be non-negative");
        }
    }
}
