package com.cpf.batch.context;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Batch Owner가 소유하는 실행 메타데이터입니다.
 * Core Context에 generic component로 삽입하지 않고 {@link CpfBatchContextBundle}로 명시적으로 전달합니다.
 */
public record CpfBatchContext(
        String jobName, String jobDisplayName, int jobVersion, String jobInstanceId, String jobExecutionId,
        String originalJobExecutionId, String stepName, String stepExecutionId, String scheduleId, String triggerId,
        CpfBatchLaunchMode launchMode, LocalDate businessDate, int restartCount, int attempt, String partitionId,
        String shardKey, String remoteRequestId, String remoteReplyId, String workerId, String workerGroup,
        String itemId, String checkpointId, String processStateId, String recoveryId, String unknownOutcomeId,
        long fencingToken, Instant startedAt) {
    public CpfBatchContext {
        if (launchMode == null) launchMode = CpfBatchLaunchMode.MANUAL;
        if (attempt < 1) attempt = 1;
        if (restartCount < 0) restartCount = 0;
        if (startedAt == null) startedAt = Instant.now();
    }
    public CpfBatchContext withJobExecution(String ji, String je, int restart, int nextAttempt,
                                            String original, String recovery, long fencing) {
        return new CpfBatchContext(jobName, jobDisplayName, jobVersion, ji, je, original, stepName, stepExecutionId,
                scheduleId, triggerId, CpfBatchLaunchMode.RESTART, businessDate, restart, Math.max(1,nextAttempt),
                partitionId, shardKey, remoteRequestId, remoteReplyId, workerId, workerGroup, itemId, checkpointId,
                processStateId, recovery, unknownOutcomeId, fencing, Instant.now());
    }
}
