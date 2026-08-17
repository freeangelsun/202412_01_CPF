package com.cpf.education.batch;

import com.cpf.batch.api.annotation.CpfBatchJob;
import com.cpf.batch.api.annotation.CpfBatchStep;
import com.cpf.batch.spi.BatchStepHandler.BatchStepCommand;
import com.cpf.batch.spi.BatchStepHandler.BatchStepResult;
import java.util.Map;

/** 배치-08 분산 Worker·재할당: fencing token으로 stale worker의 중복 effect를 차단합니다. */
@CpfBatchJob(value = "EDU-BATCH-08")
public class Batch08DistributedWorkerExample {
    @CpfBatchStep(value = "distributed-worker", order = 1)
    public BatchStepResult run(BatchStepCommand command) {
        long expectedFencing = ((Number) command.jobParameters().getOrDefault("expectedFencingToken", 1)).longValue();
        String workerId = String.valueOf(command.jobParameters().getOrDefault("workerId", "worker-unknown"));
        String reassignedFrom = String.valueOf(command.jobParameters().getOrDefault("reassignedFrom", ""));

        if (command.fencingToken() < expectedFencing) {
            return new BatchStepResult(
                    BatchStepResult.Status.FAILED,
                    "STALE_WORKER_FENCED",
                    "재할당 이후 stale worker의 write를 차단합니다.",
                    0, 0, 0,
                    Map.of("workerId", workerId, "fencingToken", command.fencingToken(), "expected", expectedFencing));
        }
        return BatchStepResult.completed(
                "worker partition completed",
                100, 100,
                Map.of(
                        "workerId", workerId,
                        "reassignedFrom", reassignedFrom,
                        "fencingToken", command.fencingToken(),
                        "duplicateEffect", 0));
    }
}
