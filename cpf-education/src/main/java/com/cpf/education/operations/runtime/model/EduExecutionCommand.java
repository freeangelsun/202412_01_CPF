package com.cpf.education.operations.runtime.model;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
/** EduExecutionCommand 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public record EduExecutionCommand(
        String businessKey,
        String idempotencyKey,
        long expectedVersion,
        String actorId,
        Set<String> roles,
        String dataScope,
        String requestReason,
        String requestId,
        String traceId,
        Map<String,Object> payload,
        EduFailurePoint failurePoint,
        boolean autoApprove,
        boolean autoAcknowledge) implements Serializable {
    public EduExecutionCommand {
        Objects.requireNonNull(businessKey); Objects.requireNonNull(idempotencyKey);
        Objects.requireNonNull(actorId); Objects.requireNonNull(roles);
        Objects.requireNonNull(dataScope); Objects.requireNonNull(requestReason);
        Objects.requireNonNull(requestId); Objects.requireNonNull(traceId);
        Objects.requireNonNull(payload); Objects.requireNonNull(failurePoint);
        roles = Set.copyOf(roles); payload = Map.copyOf(payload);
        if (businessKey.isBlank() || idempotencyKey.isBlank())
            throw new IllegalArgumentException("businessKey/idempotencyKey are required");
        if (requestReason.isBlank()) throw new IllegalArgumentException("requestReason is required");
    }
}
