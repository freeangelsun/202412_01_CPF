package com.cpf.integration.resilience.api;

import com.cpf.core.api.context.CpfContexts;
import java.time.Clock;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Correlation, timing and idempotency context for a resilient boundary call.
 *
 * <p>Normal application code never supplies a transaction id. {@link #current(String, String, Map, Clock)}
 * captures the current CPF transaction. {@link #recoveredLineage(String, String, String, Map, Clock)}
 * exists only for recovery/reconciliation of an already persisted canonical transaction lineage.</p>
 */
/** CpfResilienceCallContext는 복원력 경계 호출에서 현재 CPF 거래 Context와 멱등성·시간 정보를 안전하게 전달하는 Public Context입니다. */
public final class CpfResilienceCallContext {
    public static final String OPERATION_KIND_ATTRIBUTE = "cpf.resilience.operation-kind";
    public static final String TIMEOUT_RETRY_ATTRIBUTE = "cpf.resilience.timeout-retry-allowed";
    public static final String TRACE_SPAN_KIND_ATTRIBUTE = "cpf.trace.span-kind";
    public static final String TRACE_SEGMENT_ATTRIBUTE = "cpf.trace.segment";

    private final String operationId;
    private final String transactionId;
    private final String idempotencyKey;
    private final Instant requestedAt;
    private final Map<String, String> attributes;

    private CpfResilienceCallContext(
            String operationId,
            String transactionId,
            String idempotencyKey,
            Instant requestedAt,
            Map<String, String> attributes) {
        this.operationId = required(operationId, "operationId");
        this.transactionId = required(transactionId, "transactionId");
        this.idempotencyKey = optional(idempotencyKey, 256, "idempotencyKey");
        this.requestedAt = Objects.requireNonNull(requestedAt, "requestedAt");
        this.attributes = Map.copyOf(attributes == null ? Map.of() : attributes);
        if (this.attributes.size() > 64) {
            throw new IllegalArgumentException("attributes must contain at most 64 entries");
        }
        this.attributes.forEach((key, value) -> {
            required(key, "attribute key");
            if (key.length() > 128 || (value != null && value.length() > 1024)) {
                throw new IllegalArgumentException("attribute key/value exceeds the supported size");
            }
        });
    }

    /** Canonical application boundary factory. Framework-owned transaction lineage is captured automatically. */
    /** current 동작은 복원력 경계 호출에서 현재 CPF 거래 Context와 멱등성·시간 정보를 안전하게 전달하는 Public Context에서 필요한 공개 동작을 수행합니다. */
    public static CpfResilienceCallContext current(
            String operationId,
            String idempotencyKey,
            Map<String, String> attributes,
            Clock clock) {
        return new CpfResilienceCallContext(
                operationId,
                CpfContexts.transactionId(),
                idempotencyKey,
                Objects.requireNonNull(clock, "clock").instant(),
                attributes);
    }

    /**
     * Recovery-only factory for a persisted transaction lineage that was already validated by the recovery owner.
     * It is intentionally explicit so it cannot be mistaken for the normal application Golden Path.
     */
    /** recoveredLineage 동작은 복원력 경계 호출에서 현재 CPF 거래 Context와 멱등성·시간 정보를 안전하게 전달하는 Public Context에서 필요한 공개 동작을 수행합니다. */
    public static CpfResilienceCallContext recoveredLineage(
            String operationId,
            String canonicalTransactionId,
            String idempotencyKey,
            Map<String, String> attributes,
            Clock clock) {
        return new CpfResilienceCallContext(
                operationId,
                canonicalTransactionId,
                idempotencyKey,
                Objects.requireNonNull(clock, "clock").instant(),
                attributes);
    }

    /** operationId 동작은 복원력 경계 호출에서 현재 CPF 거래 Context와 멱등성·시간 정보를 안전하게 전달하는 Public Context에서 필요한 공개 동작을 수행합니다. */
    public String operationId() { return operationId; }
    /** transactionId 동작은 복원력 경계 호출에서 현재 CPF 거래 Context와 멱등성·시간 정보를 안전하게 전달하는 Public Context에서 필요한 공개 동작을 수행합니다. */
    public String transactionId() { return transactionId; }
    /** idempotencyKey 동작은 복원력 경계 호출에서 현재 CPF 거래 Context와 멱등성·시간 정보를 안전하게 전달하는 Public Context에서 필요한 공개 동작을 수행합니다. */
    public String idempotencyKey() { return idempotencyKey; }
    /** requestedAt 동작은 복원력 경계 호출에서 현재 CPF 거래 Context와 멱등성·시간 정보를 안전하게 전달하는 Public Context에서 필요한 공개 동작을 수행합니다. */
    public Instant requestedAt() { return requestedAt; }
    /** attributes 동작은 복원력 경계 호출에서 현재 CPF 거래 Context와 멱등성·시간 정보를 안전하게 전달하는 Public Context에서 필요한 공개 동작을 수행합니다. */
    public Map<String, String> attributes() { return attributes; }

    /** operationKind 동작은 복원력 경계 호출에서 현재 CPF 거래 Context와 멱등성·시간 정보를 안전하게 전달하는 Public Context에서 필요한 공개 동작을 수행합니다. */
    public OperationKind operationKind() {
        String configured = firstAttribute(OPERATION_KIND_ATTRIBUTE, "operationKind");
        if (configured == null) return OperationKind.UNKNOWN;
        try {
            return OperationKind.valueOf(configured.trim().toUpperCase(Locale.ROOT));
        // 실패·동시성·복구 경계에서도 원래 의미를 잃지 않도록 복원력 경계 호출에서 현재 CPF 거래 Context와 멱등성·시간 정보를 안전하게 전달하는 Public Context의 정책을 유지합니다.
        } catch (IllegalArgumentException ignored) {
            return OperationKind.UNKNOWN;
        }
    }

    /** sideEffecting 동작은 복원력 경계 호출에서 현재 CPF 거래 Context와 멱등성·시간 정보를 안전하게 전달하는 Public Context에서 필요한 공개 동작을 수행합니다. */
    public boolean sideEffecting() {
        return operationKind() == OperationKind.WRITE;
    }

    /** timeoutRetryAllowed 동작은 복원력 경계 호출에서 현재 CPF 거래 Context와 멱등성·시간 정보를 안전하게 전달하는 Public Context에서 필요한 공개 동작을 수행합니다. */
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

    /** OperationKind는 복원력 경계 호출에서 현재 CPF 거래 Context와 멱등성·시간 정보를 안전하게 전달하는 Public Context입니다. */
    public enum OperationKind { READ, WRITE, UNKNOWN }
}
