package com.cpf.education.batch;

import com.cpf.batch.api.annotation.CpfBatchJob;
import com.cpf.batch.api.annotation.CpfBatchStep;
import com.cpf.data.persistence.api.annotation.CpfTransactional;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Propagation;
import static org.assertj.core.api.Assertions.assertThat;

class BatchCanonicalEducationTest {
    @Test
    void exactlyFifteenBatchExamplesUseCanonicalBatchRuntimeAnnotations() throws Exception {
        for (int i = 1; i <= 15; i++) {
            Class<?> type = Class.forName("com.cpf.education.batch.Batch" + String.format("%02d", i) + suffix(i));
            CpfBatchJob job = type.getAnnotation(CpfBatchJob.class);
            assertThat(job).isNotNull();
            assertThat(job.value()).isEqualTo(String.format("EDU-BATCH-%02d", i));
            assertThat(List.of(type.getMethods()).stream().anyMatch(m -> m.getAnnotation(CpfBatchStep.class) != null)).isTrue();
        }
    }

    @Test
    void criticalBatchRecoveryAndTransactionBoundariesRemainExecutable() throws Exception {
        assertThat(Batch02DbChunkExample.class.getDeclaredMethod("run", com.cpf.batch.spi.BatchStepHandler.BatchStepCommand.class)).isNotNull();
        assertThat(Batch04PartitionParallelExample.class.getDeclaredMethod("run", com.cpf.batch.spi.BatchStepHandler.BatchStepCommand.class)).isNotNull();
        assertThat(Batch08DistributedWorkerExample.class.getDeclaredMethod("run", com.cpf.batch.spi.BatchStepHandler.BatchStepCommand.class)).isNotNull();

        Method chunk = Batch11ChunkTransactionExample.class.getDeclaredMethod("run", com.cpf.batch.spi.BatchStepHandler.BatchStepCommand.class);
        assertThat(chunk.getAnnotation(CpfTransactional.class).propagation()).isEqualTo(Propagation.REQUIRED);
        Method independent = Batch12RequiresNewTransactionExample.class.getDeclaredMethod("run", com.cpf.batch.spi.BatchStepHandler.BatchStepCommand.class);
        assertThat(independent.getAnnotation(CpfTransactional.class).propagation()).isEqualTo(Propagation.REQUIRES_NEW);

        assertThat(Batch14ExternalUnknownExample.StateService.class.getDeclaredMethod(
                "mark", String.class, String.class, String.class).getAnnotation(CpfTransactional.class).propagation())
                .isEqualTo(Propagation.REQUIRES_NEW);
        assertThat(Batch15OnDemandExample.class.getDeclaredMethod("run", com.cpf.batch.spi.BatchStepHandler.BatchStepCommand.class)).isNotNull();
    }

    private static String suffix(int i) {
        return switch (i) {
            case 1 -> "TaskletExample"; case 2 -> "DbChunkExample"; case 3 -> "FixedLengthFileExample"; case 4 -> "PartitionParallelExample";
            case 5 -> "CenterCutExample"; case 6 -> "SchedulerBusinessDayExample"; case 7 -> "RetrySkipRestartExample"; case 8 -> "DistributedWorkerExample";
            case 9 -> "ApprovedShellExample"; case 10 -> "ConditionalFlowExample"; case 11 -> "ChunkTransactionExample"; case 12 -> "RequiresNewTransactionExample";
            case 13 -> "StepTransactionExample"; case 14 -> "ExternalUnknownExample"; case 15 -> "OnDemandExample"; default -> throw new IllegalArgumentException();
        };
    }
}
