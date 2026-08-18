package com.cpf.batch.control.retention;

import java.time.Instant;

/** One scheduled/manual/resume retention execution. */
public record BatRetentionRunSnapshot(
        String runId, String policyId, String triggerType, String status, String runtimeInstanceId,
        String actorId, String reason, long policyVersion, Instant cutoffAt, Instant startedAt, Instant completedAt,
        long matchedCount, long archivedCount, long deletedCount, long processedCount, long compressedCount,
        long freedBytes, boolean pauseRequested, String errorCode, String errorSummary) {}
