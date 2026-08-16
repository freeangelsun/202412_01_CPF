package com.cpf.platform.operations.api.retention;

/** 보존 정책 Snapshot. */
public record CpfRetentionPolicy(String target, String action, boolean legalHold, boolean dryRun) {
    public CpfRetentionPolicy {
        if (target == null || target.isBlank()) throw new IllegalArgumentException("target은 필수입니다.");
        if (action == null || action.isBlank()) action = "KEEP";
        target = target.trim().toUpperCase();
        action = action.trim().toUpperCase();
    }
}
