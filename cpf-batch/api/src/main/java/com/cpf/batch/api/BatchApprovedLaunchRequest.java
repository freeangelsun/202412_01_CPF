package com.cpf.batch.api;

import java.util.Locale;
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
        if (definition == null || definition.state() != BatchJobDefinition.State.PUBLISHED) {
            throw new IllegalArgumentException("Published definition is required.");
        }
        if (plan == null) throw new IllegalArgumentException("Execution plan is required.");
        parameters = BatchCanonicalDigest.immutableParameters(parameters);
        approvalId = required(approvalId, "approvalId", 120);
        operatorId = required(operatorId, "operatorId", 120);
        reason = required(reason, "reason", 500);
        if (reason.length() < 5) throw new IllegalArgumentException("reason must be at least 5 characters.");
        idempotencyKey = required(idempotencyKey, "idempotencyKey", 200);
        if (!idempotencyKey.matches("[A-Za-z0-9._:-]{8,200}")) {
            throw new IllegalArgumentException("idempotencyKey format invalid.");
        }
        if (fencingToken <= 0) throw new IllegalArgumentException("fencingToken must be positive.");
        if (!definition.jobId().equals(plan.planId())
                || definition.definitionVersion() != plan.planVersion()) {
            throw new IllegalArgumentException("Definition and plan identity mismatch.");
        }
        if (definition.checksum() == null || !definition.checksum().matches("[0-9a-fA-F]{64}")) {
            throw new IllegalArgumentException("Published definition checksum must be SHA-256.");
        }
        plan.verifyIntegrity();
    }

    /** idempotency key가 충돌하지 않아야 하는 immutable 승인 범위입니다. */
    public String idempotencyScope() {
        return definition.jobId() + ":" + definition.definitionVersion() + ":" + approvalId;
    }

    /** 동일 key 재요청이 실제로 동일 요청인지 판정하는 Canonical Hash입니다. */
    public String requestHash() {
        return BatchCanonicalDigest.requestHash(this).toLowerCase(Locale.ROOT);
    }

    private static String required(String value, String field, int maximum) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " is required.");
        String result = value.trim();
        if (result.length() > maximum) throw new IllegalArgumentException(field + " exceeds " + maximum + " characters.");
        return result;
    }
}
