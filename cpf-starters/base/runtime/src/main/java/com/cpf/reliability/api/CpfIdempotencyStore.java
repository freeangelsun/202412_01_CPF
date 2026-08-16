package com.cpf.reliability.api;

import java.time.Instant;
import java.util.Map;

/**
 * 다중 인스턴스에서 원자적으로 멱등 실행권을 획득/완료/실패 처리하는 durable store 계약입니다.
 * 구현체는 key+operation에 대해 단일 승자를 보장하고, Process Kill 뒤 IN_PROGRESS/UNKNOWN을 복구할 수 있어야 합니다.
 */
public interface CpfIdempotencyStore {
    AcquireResult acquire(AcquireRequest request);
    void complete(String leaseToken, StoredResult result, Instant completedAt);
    void fail(String leaseToken, Failure failure, Instant failedAt);

    enum State { ACQUIRED, REPLAY, IN_PROGRESS, CONFLICT, UNKNOWN }

    record AcquireRequest(String key, String operation, String payloadFingerprint,
                          String transactionId, String executionId, Instant now,
                          Instant expiresAt, Instant inProgressDeadline) {
        public AcquireRequest {
            if (key == null || key.isBlank()) throw new IllegalArgumentException("key");
            if (operation == null || operation.isBlank()) throw new IllegalArgumentException("operation");
            if (payloadFingerprint == null || payloadFingerprint.isBlank()) throw new IllegalArgumentException("payloadFingerprint");
            if (transactionId == null || transactionId.isBlank()) throw new IllegalArgumentException("transactionId");
            if (executionId == null || executionId.isBlank()) throw new IllegalArgumentException("executionId");
            if (now == null || expiresAt == null || inProgressDeadline == null) throw new IllegalArgumentException("time");
        }
    }

    /** AcquireResult 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
    record AcquireResult(State state, String leaseToken, String storedFingerprint, StoredResult storedResult) {
        public AcquireResult {
            if (state == null) throw new IllegalArgumentException("state");
            if (state == State.ACQUIRED && (leaseToken == null || leaseToken.isBlank())) {
                throw new IllegalArgumentException("leaseToken required for ACQUIRED");
            }
            if (state == State.REPLAY && storedResult == null) throw new IllegalArgumentException("storedResult required for REPLAY");
        }
        /** acquired 작업을 CPF 표준 계약에 따라 수행한다. */
        public static AcquireResult acquired(String token) { return new AcquireResult(State.ACQUIRED, token, null, null); }
        public static AcquireResult replay(String fingerprint, StoredResult result) { return new AcquireResult(State.REPLAY, null, fingerprint, result); }
        public static AcquireResult inProgress(String fingerprint) { return new AcquireResult(State.IN_PROGRESS, null, fingerprint, null); }
        public static AcquireResult conflict(String fingerprint) { return new AcquireResult(State.CONFLICT, null, fingerprint, null); }
        public static AcquireResult unknown(String fingerprint) { return new AcquireResult(State.UNKNOWN, null, fingerprint, null); }
    }

    record StoredResult(String codec, byte[] payload, String declaredType) {
        public StoredResult {
            if (codec == null || codec.isBlank()) throw new IllegalArgumentException("codec");
            payload = payload == null ? new byte[0] : payload.clone();
            declaredType = declaredType == null ? "void" : declaredType;
        }
        @Override public byte[] payload() { return payload.clone(); }
    }

    /** Failure 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
    record Failure(String type, String safeCode, boolean retryable, Map<String, String> attributes) {
        public Failure {
            type = type == null || type.isBlank() ? "UNCLASSIFIED" : type;
            safeCode = safeCode == null || safeCode.isBlank() ? "UNCLASSIFIED" : safeCode;
            attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
        }
    }
}
