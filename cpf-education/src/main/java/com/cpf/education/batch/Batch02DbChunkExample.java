package com.cpf.education.batch;

import com.cpf.batch.api.annotation.CpfBatchJob;
import com.cpf.batch.api.annotation.CpfBatchStep;
import com.cpf.batch.spi.BatchStepHandler.BatchStepCommand;
import com.cpf.batch.spi.BatchStepHandler.BatchStepResult;
import java.util.Map;

/** 배치-02 DB Chunk: commit interval, checkpoint, retryable failure와 restart 지점을 함께 표현합니다. */
@CpfBatchJob(value = "EDU-BATCH-02")
public class Batch02DbChunkExample {
    @CpfBatchStep(value = "db-chunk", order = 1)
    public BatchStepResult run(BatchStepCommand command) {
        int chunkSize = intParam(command, "chunkSize", 100);
        long offset = longParam(command, "checkpointOffset", 0);
        long totalRows = longParam(command, "totalRows", 1_000);
        long endExclusive = Math.min(totalRows, offset + chunkSize);
        long processed = Math.max(0, endExclusive - offset);
        long failAt = longParam(command, "failAtRow", -1);

        if (failAt >= offset && failAt < endExclusive) {
            return new BatchStepResult(
                    BatchStepResult.Status.RETRYABLE_FAILURE,
                    "CHUNK_WRITE_RETRY",
                    "현재 Chunk만 rollback하고 이전 checkpoint부터 재시작합니다.",
                    processed,
                    0,
                    0,
                    Map.of("checkpointOffset", offset, "commitInterval", chunkSize));
        }
        return BatchStepResult.completed(
                "chunk committed",
                processed,
                processed,
                Map.of("checkpointOffset", endExclusive, "commitInterval", chunkSize, "remaining", totalRows - endExclusive));
    }

    private static int intParam(BatchStepCommand command, String name, int defaultValue) {
        return ((Number) command.jobParameters().getOrDefault(name, defaultValue)).intValue();
    }
    private static long longParam(BatchStepCommand command, String name, long defaultValue) {
        return ((Number) command.jobParameters().getOrDefault(name, defaultValue)).longValue();
    }
}
