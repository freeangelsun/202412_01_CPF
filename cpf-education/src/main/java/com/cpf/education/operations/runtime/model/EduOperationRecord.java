package com.cpf.education.operations.runtime.model;
import java.io.Serializable;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
/** EduOperationRecord 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public record EduOperationRecord(
        String operationId, String requirementId, String businessKey, String idempotencyKey,
        String payloadHash, String actorId, String actorRoles, String dataScope,
        EduExecutionState state, long expectedBusinessVersion, long recordVersion,
        long fencingToken, int retryCount, int maxRetries, EduFailurePoint failurePoint,
        String resultCode, String resultMessage, String requestId, String traceId,
        Map<String,Object> payload, Map<String,Object> result,
        Instant createdAt, Instant updatedAt, Instant completedAt) implements Serializable {
    public EduOperationRecord {
        Objects.requireNonNull(operationId); Objects.requireNonNull(requirementId);
        Objects.requireNonNull(businessKey); Objects.requireNonNull(idempotencyKey);
        Objects.requireNonNull(payloadHash); Objects.requireNonNull(actorId);
        Objects.requireNonNull(actorRoles); Objects.requireNonNull(dataScope);
        Objects.requireNonNull(state); Objects.requireNonNull(failurePoint);
        Objects.requireNonNull(resultCode); Objects.requireNonNull(resultMessage);
        Objects.requireNonNull(requestId); Objects.requireNonNull(traceId);
        Objects.requireNonNull(payload); Objects.requireNonNull(result);
        Objects.requireNonNull(createdAt); Objects.requireNonNull(updatedAt);
        payload = Map.copyOf(payload); result = Map.copyOf(result);
    }
    /** transition 작업을 CPF 표준 계약에 따라 수행한다. */
    public EduOperationRecord transition(EduExecutionState next, String code, String message,
                                         Map<String,Object> nextResult, long nextFence,
                                         int nextRetry, Instant now) {
        return new EduOperationRecord(operationId, requirementId, businessKey, idempotencyKey,
                payloadHash, actorId, actorRoles, dataScope, next, expectedBusinessVersion,
                recordVersion + 1, nextFence, nextRetry, maxRetries, failurePoint, code, message,
                requestId, traceId, payload, nextResult, createdAt, now,
                next.terminal() ? now : completedAt);
    }
}
