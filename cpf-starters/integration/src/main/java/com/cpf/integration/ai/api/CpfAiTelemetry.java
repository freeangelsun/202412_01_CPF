package com.cpf.integration.ai.api;

/** Provider-neutral AI observability sink. Implementations may bridge metrics/tracing without leaking vendor API. */
/** CpfAiTelemetry 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public interface CpfAiTelemetry {
    CpfAiTelemetry NOOP = new CpfAiTelemetry() { };

    default void accepted(CpfAiRequest request) { }
    default void completed(CpfAiRequest request, CpfAiResponse response, long elapsedNanos) { }
    default void failed(CpfAiRequest request, Throwable failure, long elapsedNanos) { }
    default void limited(CpfAiRequest request, String reason) { }
}
