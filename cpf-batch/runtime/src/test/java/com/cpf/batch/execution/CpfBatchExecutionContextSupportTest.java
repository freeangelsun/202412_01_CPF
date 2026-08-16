package com.cpf.batch.execution;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.batch.infrastructure.item.ExecutionContext;

/** Spring Batch 6 재시작 계보가 동일 실행의 retry를 restart로 오인하지 않는지 검증합니다. */
class CpfBatchExecutionContextSupportTest {
    @Test
    void persistsRestartLineageAcrossJobExecutions() {
        ExecutionContext context=new ExecutionContext();

        var initial=CpfBatchExecutionContextSupport.restoreExecutionLineage(context,101L);
        var retry=CpfBatchExecutionContextSupport.restoreExecutionLineage(context,101L);
        var firstRestart=CpfBatchExecutionContextSupport.restoreExecutionLineage(context,102L);
        var sameRestartAttempt=CpfBatchExecutionContextSupport.restoreExecutionLineage(context,102L);
        var secondRestart=CpfBatchExecutionContextSupport.restoreExecutionLineage(context,103L);

        assertThat(initial.originalJobExecution()).isEqualTo(101L);
        assertThat(initial.restartCount()).isZero();
        assertThat(retry.restartCount()).isZero();
        assertThat(firstRestart.restartCount()).isEqualTo(1);
        assertThat(sameRestartAttempt.restartCount()).isEqualTo(1);
        assertThat(secondRestart.restartCount()).isEqualTo(2);
        assertThat(context.getLong(CpfBatchExecutionContextSupport.ORIGINAL_JOB_EXECUTION)).isEqualTo(101L);
        assertThat(context.getLong(CpfBatchExecutionContextSupport.LAST_JOB_EXECUTION)).isEqualTo(103L);
    }
}
