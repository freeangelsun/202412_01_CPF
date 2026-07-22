package cpf.pfw.common.batch;

import cpf.pfw.common.logging.policy.LogPolicyDecision;
import cpf.pfw.common.logging.policy.LogPolicyResolver;
import cpf.pfw.common.logging.policy.LogPolicyTargetType;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;
import org.springframework.beans.factory.ObjectProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CpfBatchRuntimeListenerPolicyTest {

    @Test
    @SuppressWarnings("unchecked")
    void listenerStoresJobAndStepPolicyInExecutionContext() {
        CpfBatchHeartbeatService heartbeatService = mock(CpfBatchHeartbeatService.class);
        ObjectProvider<LogPolicyResolver> resolverProvider = mock(ObjectProvider.class);
        LogPolicyResolver resolver = mock(LogPolicyResolver.class);
        when(resolverProvider.getIfAvailable()).thenReturn(resolver);
        when(resolver.resolveBatchJob("CPF_EDU_TASKLET_JOB")).thenReturn(policy(LogPolicyTargetType.BATCH_JOB, "CPF_EDU_TASKLET_JOB"));
        when(resolver.resolveBatchStep("CPF_EDU_TASKLET_JOB", "sampleStep")).thenReturn(policy(LogPolicyTargetType.BATCH_STEP, "CPF_EDU_TASKLET_JOB:sampleStep"));
        CpfBatchRuntimeListener listener = new CpfBatchRuntimeListener(heartbeatService, resolverProvider);
        JobExecution jobExecution = new JobExecution(new JobInstance(1L, "CPF_EDU_TASKLET_JOB"), new JobParameters());
        StepExecution stepExecution = new StepExecution("sampleStep", jobExecution);

        listener.beforeJob(jobExecution);
        listener.beforeStep(stepExecution);

        assertThat(jobExecution.getExecutionContext().getString("cpf.logPolicy.job.targetType")).isEqualTo("BATCH_JOB");
        assertThat(stepExecution.getExecutionContext().getString("cpf.logPolicy.step.targetType")).isEqualTo("BATCH_STEP");
        assertThat(stepExecution.getExecutionContext().getString("cpf.logPolicy.step.targetId")).isEqualTo("CPF_EDU_TASKLET_JOB:sampleStep");
        verify(heartbeatService).recordJobStarted(jobExecution);
        verify(heartbeatService).recordStepStarted(stepExecution);
    }

    @Test
    @SuppressWarnings("unchecked")
    void listenerWritesBatchFileLogForJobAndStepLifecycle() {
        CpfBatchHeartbeatService heartbeatService = mock(CpfBatchHeartbeatService.class);
        ObjectProvider<CpfBatchFileLogWriter> writerProvider = mock(ObjectProvider.class);
        CpfBatchFileLogWriter writer = mock(CpfBatchFileLogWriter.class);
        when(writerProvider.getIfAvailable()).thenReturn(writer);
        CpfBatchRuntimeListener listener = new CpfBatchRuntimeListener(heartbeatService, null, writerProvider);
        JobExecution jobExecution = new JobExecution(new JobInstance(2L, "CPF_EDU_CHUNK_JOB"), new JobParameters());
        StepExecution stepExecution = new StepExecution("chunkStep", jobExecution);

        listener.beforeJob(jobExecution);
        listener.beforeStep(stepExecution);
        listener.afterStep(stepExecution);
        listener.afterJob(jobExecution);

        verify(writer).writeBatch(eq("BATCH_JOB_STARTED"), eq(jobExecution), eq(null));
        verify(writer).writeBatch(eq("BATCH_STEP_STARTED"), eq(jobExecution), eq(stepExecution));
        verify(writer).writeBatch(eq("BATCH_STEP_FINISHED"), eq(jobExecution), eq(stepExecution));
        verify(writer).writeBatch(eq("BATCH_JOB_FINISHED"), eq(jobExecution), eq(null));
        verify(writerProvider, times(4)).getIfAvailable();
    }

    private LogPolicyDecision policy(LogPolicyTargetType targetType, String targetId) {
        return new LogPolicyDecision(
                targetType.code(),
                targetId,
                "INFO",
                true,
                "INFO",
                false,
                false,
                true,
                "DEFAULT",
                "DB_POLICY",
                null,
                1L);
    }
}
