package com.cpf.batch.api;

import java.util.Map;

/** 승인·권한·사유와 Spring Batch JobParameters를 결합한 실행 요청입니다. */
public record BatchApprovedLaunchRequest(
        BatchJobDefinition definition,
        BatchExecutionPlan plan,
        Map<String, Object> parameters,
        String approvalId,
        String operatorId,
        String reason,
        String idempotencyKey,
        long fencingToken) {
    public BatchApprovedLaunchRequest {
        if (definition == null || definition.state() != BatchJobDefinition.State.PUBLISHED) throw new IllegalArgumentException("Published definition is required.");
        if (plan == null) throw new IllegalArgumentException("Execution plan is required.");
        parameters = parameters == null ? Map.of() : Map.copyOf(parameters);
        approvalId = required(approvalId, "approvalId");
        operatorId = required(operatorId, "operatorId");
        reason = required(reason, "reason");
        idempotencyKey = required(idempotencyKey, "idempotencyKey");
        if (fencingToken <= 0) throw new IllegalArgumentException("fencingToken must be positive.");
        if (!definition.jobId().equals(plan.planId())) throw new IllegalArgumentException("Definition and plan identity mismatch.");
    }
    private static String required(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " is required.");
        return value.trim();
    }
}
