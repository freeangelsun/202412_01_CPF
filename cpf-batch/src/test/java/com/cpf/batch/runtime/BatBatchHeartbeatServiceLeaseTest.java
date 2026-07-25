package com.cpf.batch.runtime;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParametersBuilder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class BatBatchHeartbeatServiceLeaseTest {

    @Test
    void leaseWorkerDelegatesFinalStateToTokenFencedAgent() {
        BatBatchOperationRepository repository = mock(BatBatchOperationRepository.class);
        BatBatchHeartbeatService service = new BatBatchHeartbeatService(repository, 1, 10);
        JobExecution execution = execution(true);
        execution.setStatus(BatchStatus.COMPLETED);

        service.recordJobFinished(execution);

        verify(repository).recordWorkerHeartbeat(
                eq("worker-1"), any(), eq("IDLE"), isNull(), isNull(), eq("CPF_BATCH"));
        verify(repository, never()).completeExecution(
                anyLong(), anyString(), any(), anyString(), any(), any(), anyString());
    }

    @Test
    void launcherExecutionKeepsListenerCompletionBehavior() {
        BatBatchOperationRepository repository = mock(BatBatchOperationRepository.class);
        BatBatchHeartbeatService service = new BatBatchHeartbeatService(repository, 1, 10);
        JobExecution execution = execution(false);
        execution.setStatus(BatchStatus.COMPLETED);

        service.recordJobFinished(execution);

        verify(repository).completeExecution(
                eq(101L), eq("COMPLETED"), any(), eq("worker-1"), any(), eq(execution), eq("CPF_BATCH"));
    }

    private JobExecution execution(boolean includeLeaseToken) {
        JobParametersBuilder parameters = new JobParametersBuilder()
                .addLong(BatBatchHeartbeatService.PARAM_CPF_EXECUTION_ID, 101L)
                .addString("serverInstanceId", "worker-1");
        if (includeLeaseToken) {
            parameters.addString(BatBatchHeartbeatService.PARAM_WORKER_LEASE_TOKEN, "lease-token-1");
        }
        JobExecution execution = new JobExecution(new JobInstance(1L, "CPF_BAT_TEST_JOB"), parameters.toJobParameters());
        execution.setId(201L);
        return execution;
    }
}
