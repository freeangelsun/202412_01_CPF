package com.cpf.integration.resilience.api;

import java.time.Clock;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/** Correlation, timing and idempotency context for a resilient outbound call. */
/** CpfResilienceCallContext 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public record CpfResilienceCallContext(
        String operationId, String transactionId, String idempotencyKey,
        Instant requestedAt, Map<String, String> attributes) {
    public static final String OPERATION_KIND_ATTRIBUTE = "cpf.resilience.operation-kind";
    public static final String TIMEOUT_RETRY_ATTRIBUTE = "cpf.resilience.timeout-retry-allowed";
    public static final String TRACE_SPAN_KIND_ATTRIBUTE = "cpf.trace.span-kind";
    public static final String TRACE_SEGMENT_ATTRIBUTE = "cpf.trace.segment";

    public CpfResilienceCallContext {
        operationId = required(operationId, "operationId");
        transactionId = required(transactionId, "transactionId");
        idempotencyKey = optional(idempotencyKey, 256, "idempotencyKey");
        requestedAt = Objects.requireNonNull(requestedAt,
                "requestedAt is required; use CpfResilienceCallContext.now(..., Clock) at the boundary");
        attributes = Map.copyOf(attributes == null ? Map.of() : attributes);
        if (attributes.size() > 64) throw new IllegalArgumentException("attributes must contain at most 64 entries");
        attributes.forEach((key, value) -> {
            required(key, "attribute key");
            if (key.length() > 128 || (value != null && value.length() > 1024)) {
                throw new IllegalArgumentException("attribute key/value exceeds the supported size");
            }
        });
    }

    /** now 작업을 CPF 표준 계약에 따라 수행한다. */
    public static CpfResilienceCallContext now(
            String operationId, String transactionId, String idempotencyKey,
            Map<String, String> attributes, Clock clock) {
        return new CpfResilienceCallContext(operationId, transactionId, idempotencyKey,
                Objects.requireNonNull(clock, "clock").instant(), attributes);
    }

    /** operationKind 작업을 CPF 표준 계약에 따라 수행한다. */
    public OperationKind operationKind() {
        String configured = firstAttribute(OPERATION_KIND_ATTRIBUTE, "operationKind");
        if (configured == null) return OperationKind.UNKNOWN;
        try {
            return OperationKind.valueOf(configured.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return OperationKind.UNKNOWN;
        }
    }

    /** sideEffecting 작업을 CPF 표준 계약에 따라 수행한다. */
    public boolean sideEffecting() {
        return operationKind() == OperationKind.WRITE;
    }

    public boolean timeoutRetryAllowed() {
        String configured = firstAttribute(TIMEOUT_RETRY_ATTRIBUTE, "timeoutRetryAllowed");
        if (configured == null) return !sideEffecting();
        return "true".equalsIgnoreCase(configured)
                || "yes".equalsIgnoreCase(configured)
                || "1".equals(configured);
    }

    private String firstAttribute(String primary, String compatibility) {
        String value = attributes.get(primary);
        if (value == null || value.isBlank()) value = attributes.get(compatibility);
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static String required(String value, String name) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(name + " is required");
        String normalized = value.trim();
        if (normalized.length() > 256) throw new IllegalArgumentException(name + " exceeds 256 characters");
        return normalized;
    }

    private static String optional(String value, int maxLength, String name) {
        if (value == null || value.isBlank()) return null;
        String normalized = value.trim();
        if (normalized.length() > maxLength) throw new IllegalArgumentException(name + " exceeds " + maxLength + " characters");
        return normalized;
    }

    /** OperationKind 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
    public enum OperationKind { READ, WRITE, UNKNOWN }
}
