package com.cpf.platform.operations.api.retention;

/** Retention 실행 결과. */
public record CpfRetentionResult(String target, String action, boolean dryRun, boolean legalHold, long matched, long archived, long purged, String status) {}
