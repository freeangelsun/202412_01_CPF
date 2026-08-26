package com.cpf.batch.control.retention;

import java.time.Instant;
import java.time.LocalTime;

/** Shared data-family retention policy persisted in OPS_RETENTION_POLICY. */
public record BatRetentionPolicyDefinition(
        String policyId, String target, String action, int retentionDays, String scheduleExpression,
        LocalTime maintenanceStart, LocalTime maintenanceEnd, boolean enabled, boolean legalHold,
        int chunkSize, long throttleMillis, long maxRowsPerRun, long maxRuntimeSeconds, int leaseSeconds,
        long policyVersion, Instant nextRunAt, long rowVersion) {
    public BatRetentionPolicyDefinition {
        if (policyId == null || policyId.isBlank()) throw new IllegalArgumentException("policyId는 필수입니다.");
        if (target == null || target.isBlank()) throw new IllegalArgumentException("target은 필수입니다.");
        if (action == null || action.isBlank()) action = "KEEP";
        if (retentionDays < 0 || retentionDays > 36500) throw new IllegalArgumentException("retentionDays 범위 오류");
        if (chunkSize < 1 || chunkSize > 100_000) throw new IllegalArgumentException("chunkSize 범위 오류");
        if (throttleMillis < 0 || throttleMillis > 60_000) throw new IllegalArgumentException("throttleMillis 범위 오류");
        if (maxRowsPerRun < 1) throw new IllegalArgumentException("maxRowsPerRun은 1 이상이어야 합니다.");
        if (maxRuntimeSeconds < 1 || maxRuntimeSeconds > 86_400) throw new IllegalArgumentException("maxRuntimeSeconds 범위 오류");
        if (leaseSeconds < 5 || leaseSeconds > 3_600) throw new IllegalArgumentException("leaseSeconds 범위 오류");
        policyId = policyId.trim().toUpperCase(java.util.Locale.ROOT); target = target.trim().toUpperCase(java.util.Locale.ROOT); action = action.trim().toUpperCase(java.util.Locale.ROOT);
    }
}
