package com.cpf.batch.api;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * 위험 운영 명령의 감사/멱등/낙관락/UNKNOWN_RESULT 복구까지 포함하는 BAT Owner 계약.
 */
public record RuntimeCommand(
        String commandId,
        String idempotencyKey,
        String commandType,
        String targetType,
        List<String> targetIds,
        String targetSnapshot,
        String targetSnapshotHash,
        long expectedVersion,
        String requestedBy,
        String reason,
        Instant requestedAt,
        String approvalPolicyVersion,
        String approvalRequestId,
        String approvedBy,
        Instant expiresAt,
        CommandState executionState,
        int executionAttempt,
        Map<String, Object> parameters,
        String result,
        String failureStage,
        String beforeState,
        String afterState,
        String transactionId,
        String evidenceRef
) {
    public RuntimeCommand {
        require(commandId, "commandId"); require(idempotencyKey, "idempotencyKey");
        require(commandType, "commandType"); require(targetType, "targetType");
        require(requestedBy, "requestedBy"); require(reason, "reason");
        if (expectedVersion < 0) throw new IllegalArgumentException("expectedVersion must be non-negative");
        if (executionAttempt < 0) throw new IllegalArgumentException("executionAttempt must be non-negative");
        targetIds = targetIds == null ? List.of() : List.copyOf(targetIds);
        if (targetIds.isEmpty()) throw new IllegalArgumentException("targetIds is required");
        parameters = parameters == null ? Map.of() : Map.copyOf(parameters);
        requestedAt = requestedAt == null ? Instant.now() : requestedAt;
        executionState = executionState == null ? CommandState.REQUESTED : executionState;
    }
    /** Compatibility accessor for pre-R15 callers. */
    public CommandState state() { return executionState; }
    private static void require(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " is required");
    }
}
