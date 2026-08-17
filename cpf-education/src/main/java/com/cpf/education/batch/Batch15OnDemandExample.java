package com.cpf.education.batch;

import com.cpf.batch.api.CpfBatchExecutionRequest;
import com.cpf.batch.api.CpfBatchExecutionResult;
import com.cpf.batch.api.CpfBatchOperations;
import com.cpf.batch.api.annotation.CpfBatchJob;
import com.cpf.batch.api.annotation.CpfBatchStep;
import com.cpf.batch.spi.BatchStepHandler.BatchStepCommand;
import com.cpf.batch.spi.BatchStepHandler.BatchStepResult;
import java.util.Map;

/** 배치-15 On-Demand: idempotency key와 lockRequired로 중복 launch를 BAT Owner Runtime에 위임합니다. */
@CpfBatchJob(value = "EDU-BATCH-15")
public class Batch15OnDemandExample {
    private final CpfBatchOperations batches;

    public Batch15OnDemandExample(CpfBatchOperations batches) {
        this.batches = batches;
    }

    @CpfBatchStep(value = "on-demand", order = 1)
    /** run 단계는 CPF Public 계약을 사용해 이 EDU 시나리오의 업무 흐름을 수행합니다. */
    public BatchStepResult run(BatchStepCommand command) {
        String key = String.valueOf(command.jobParameters().getOrDefault("idempotencyKey", command.cpfExecutionId()));
        CpfBatchExecutionRequest request = CpfBatchExecutionRequest.onDemand(
                "EDU-BATCH-15",
                "EDU-BATCH-15",
                String.valueOf(command.jobParameters().getOrDefault("businessDate", "")),
                key,
                "{}",
                "EDU",
                "교육 On-Demand 중복실행 방지");
        CpfBatchExecutionResult launch = batches.launch(request);
        CpfBatchExecutionResult current = batches.status(launch.executionRequestId());
        return BatchStepResult.completed(
                "on-demand request accepted",
                0, 0,
                Map.of(
                        "executionRequestId", launch.executionRequestId(),
                        "launchStatus", launch.status(),
                        "currentStatus", current.status(),
                        "idempotencyKey", key,
                        "lockRequired", request.lockRequired()));
    }
}
