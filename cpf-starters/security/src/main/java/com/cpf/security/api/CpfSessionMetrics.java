package com.cpf.security.api;

/** Provider-neutral metrics access for JDBC/Valkey session implementations. */
@FunctionalInterface
/** CpfSessionMetrics 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public interface CpfSessionMetrics {
    CpfSessionMetricsSnapshot snapshot();
}
