package com.cpf.batch.runtime.centercut;

import com.cpf.core.common.batch.centercut.CpfCenterCutSummary;

import java.time.Instant;

/** Center-Cut Runner 1회 실행 결과. */
public record BatCenterCutRunResult(
        String jobId,
        String runId,
        String status,
        Instant startedAt,
        Instant endedAt,
        CpfCenterCutSummary summary,
        String message) {
}
