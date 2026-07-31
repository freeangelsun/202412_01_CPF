package com.cpf.batch.api;

/** ADM·Scheduler·Center-Cut이 사용하는 Spring Batch 실행 생명주기 계약입니다. */
public interface BatchExecutionControlPort {
    BatchExecutionLink start(BatchApprovedLaunchRequest request);
    boolean stop(long jobExecutionId, String operatorId, String reason);
    BatchExecutionLink restart(long jobExecutionId, String operatorId, String reason, long fencingToken);
    void abandon(long jobExecutionId, String operatorId, String reason);
    BatchExecutionLink reconcile(String cpfExecutionId);
}
