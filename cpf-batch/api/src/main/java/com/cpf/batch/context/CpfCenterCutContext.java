package com.cpf.batch.context;

/** Center-Cut Owner가 item/claim 범위에서 유지하는 메타데이터입니다. */
public record CpfCenterCutContext(
        String centerCutId, String workerGroup, String workUnitId, String partitionId, String stepId,
        String rangeStart, String rangeEnd, String workerId, long fencingToken, int attempt,
        String originalExecutionId, String recoveryExecutionId, String checkpointId) {
    public CpfCenterCutContext { if (attempt < 1) attempt = 1; }
}
