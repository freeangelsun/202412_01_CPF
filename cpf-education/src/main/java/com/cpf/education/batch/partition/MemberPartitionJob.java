package com.cpf.education.batch.partition;

import com.cpf.batch.api.annotation.CpfBatchJob;
import com.cpf.batch.api.annotation.CpfBatchStep;
import com.cpf.batch.spi.BatchStepHandler.BatchStepCommand;
import com.cpf.batch.spi.BatchStepHandler.BatchStepResult;
import java.util.Map;

/** 배치-04 대용량 Partition·Parallel: partition별 watermark/checkpoint와 부분 실패 재실행을 표현합니다. */
@CpfBatchJob(value = "EDU_MEMBER_PARTITION_JOB")
public class MemberPartitionJob {
    @CpfBatchStep(value = "partition-worker", order = 1)
    public BatchStepResult run(BatchStepCommand command) {
        int partition = intParam(command, "partition", 0);
        int partitionCount = intParam(command, "partitionCount", 4);
        long pageSize = longParam(command, "pageSize", 1_000);
        long watermark = longParam(command, "watermark", partition * 10_000L);
        boolean failPartition = Boolean.parseBoolean(String.valueOf(command.jobParameters().getOrDefault("failPartition", false)));

        Map<String, Object> checkpoint = Map.of(
                "partition", partition,
                "partitionCount", partitionCount,
                "watermark", watermark + pageSize,
                "pageSize", pageSize,
                "fencingToken", command.fencingToken());
        if (failPartition) {
            return new BatchStepResult(
                    BatchStepResult.Status.RETRYABLE_FAILURE,
                    "PARTITION_RETRY",
                    "실패 partition만 checkpoint에서 재할당합니다.",
                    pageSize,
                    0,
                    0,
                    checkpoint);
        }
        return BatchStepResult.completed("partition completed", pageSize, pageSize, checkpoint);
    }

    private static int intParam(BatchStepCommand command, String name, int defaultValue) {
        return ((Number) command.jobParameters().getOrDefault(name, defaultValue)).intValue();
    }
    private static long longParam(BatchStepCommand command, String name, long defaultValue) {
        return ((Number) command.jobParameters().getOrDefault(name, defaultValue)).longValue();
    }
}
