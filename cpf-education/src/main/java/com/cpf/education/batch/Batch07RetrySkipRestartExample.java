package com.cpf.education.batch;

import com.cpf.batch.api.annotation.CpfBatchJob;
import com.cpf.batch.api.annotation.CpfBatchStep;
import com.cpf.batch.spi.BatchStepHandler.BatchStepCommand;
import com.cpf.batch.spi.BatchStepHandler.BatchStepResult;
import java.util.Map;

/** 배치-07 Retry·Skip·Restart: retryable/skip/checkpoint를 구분하고 restart 시 중복 처리를 방지합니다. */
@CpfBatchJob(value = "EDU-BATCH-07")
public class Batch07RetrySkipRestartExample {
    @CpfBatchStep(value = "retry-skip-restart", order = 1)
    public BatchStepResult run(BatchStepCommand command) {
        boolean retryable = boolParam(command, "retryable");
        boolean skippable = boolParam(command, "skippable");
        long checkpoint = ((Number) command.jobParameters().getOrDefault("checkpoint", 0)).longValue();

        if (retryable) {
            return new BatchStepResult(
                    BatchStepResult.Status.RETRYABLE_FAILURE,
                    "TEMPORARY",
                    "CPF Runtime retry/backoff 후 동일 checkpoint에서 재실행합니다.",
                    1, 0, 0,
                    Map.of("checkpoint", checkpoint));
        }
        if (skippable) {
            return new BatchStepResult(
                    BatchStepResult.Status.COMPLETED,
                    "",
                    "허용된 데이터 오류 1건을 skip하고 checkpoint를 전진합니다.",
                    1, 0, 1,
                    Map.of("checkpoint", checkpoint + 1));
        }
        return BatchStepResult.completed(
                "restart-safe step",
                1, 1,
                Map.of("checkpoint", checkpoint + 1, "stepExecutionId", command.stepExecutionId()));
    }

    private static boolean boolParam(BatchStepCommand command, String name) {
        return Boolean.parseBoolean(String.valueOf(command.jobParameters().getOrDefault(name, false)));
    }
}
