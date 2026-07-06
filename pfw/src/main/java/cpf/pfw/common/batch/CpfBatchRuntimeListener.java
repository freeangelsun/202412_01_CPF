package cpf.pfw.common.batch;

import cpf.pfw.common.logging.policy.LogPolicyDecision;
import cpf.pfw.common.logging.policy.LogPolicyResolver;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.beans.factory.ObjectProvider;

/**
 * Spring Batch Job/Step 생명주기를 CPF 운영 메타와 연결하는 listener입니다.
 *
 * <p>배치 개발자는 신규 Job과 Step을 만들 때 이 listener를 연결해 실행 시작, Step 시작,
 * Step 종료, Job 종료 상태가 ADM 관제 메타에 남도록 해야 합니다.</p>
 */
public class CpfBatchRuntimeListener implements JobExecutionListener, StepExecutionListener {
    private final CpfBatchHeartbeatService heartbeatService;
    private final ObjectProvider<LogPolicyResolver> logPolicyResolverProvider;
    private final ObjectProvider<CpfBatchFileLogWriter> batchFileLogWriterProvider;

    public CpfBatchRuntimeListener(CpfBatchHeartbeatService heartbeatService) {
        this(heartbeatService, null);
    }

    public CpfBatchRuntimeListener(
            CpfBatchHeartbeatService heartbeatService,
            ObjectProvider<LogPolicyResolver> logPolicyResolverProvider) {
        this(heartbeatService, logPolicyResolverProvider, null);
    }

    public CpfBatchRuntimeListener(
            CpfBatchHeartbeatService heartbeatService,
            ObjectProvider<LogPolicyResolver> logPolicyResolverProvider,
            ObjectProvider<CpfBatchFileLogWriter> batchFileLogWriterProvider) {
        this.heartbeatService = heartbeatService;
        this.logPolicyResolverProvider = logPolicyResolverProvider;
        this.batchFileLogWriterProvider = batchFileLogWriterProvider;
    }

    @Override
    public void beforeJob(JobExecution jobExecution) {
        applyBatchJobPolicy(jobExecution);
        heartbeatService.recordJobStarted(jobExecution);
        writeBatchFileLog("BATCH_JOB_STARTED", jobExecution, null);
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        heartbeatService.recordJobFinished(jobExecution);
        writeBatchFileLog("BATCH_JOB_FINISHED", jobExecution, null);
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
        applyBatchStepPolicy(stepExecution);
        heartbeatService.recordStepStarted(stepExecution);
        writeBatchFileLog("BATCH_STEP_STARTED", stepExecution.getJobExecution(), stepExecution);
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        heartbeatService.recordStepFinished(stepExecution);
        writeBatchFileLog("BATCH_STEP_FINISHED", stepExecution.getJobExecution(), stepExecution);
        return stepExecution.getExitStatus();
    }

    private void applyBatchJobPolicy(JobExecution jobExecution) {
        LogPolicyResolver resolver = resolver();
        if (resolver == null || jobExecution == null || jobExecution.getJobInstance() == null) {
            return;
        }
        LogPolicyDecision decision = resolver.resolveBatchJob(jobExecution.getJobInstance().getJobName());
        putPolicy(jobExecution.getExecutionContext(), "cpf.logPolicy.job.", decision);
    }

    private void applyBatchStepPolicy(StepExecution stepExecution) {
        LogPolicyResolver resolver = resolver();
        if (resolver == null || stepExecution == null || stepExecution.getJobExecution() == null
                || stepExecution.getJobExecution().getJobInstance() == null) {
            return;
        }
        String jobName = stepExecution.getJobExecution().getJobInstance().getJobName();
        LogPolicyDecision decision = resolver.resolveBatchStep(jobName, stepExecution.getStepName());
        putPolicy(stepExecution.getExecutionContext(), "cpf.logPolicy.step.", decision);
    }

    private LogPolicyResolver resolver() {
        return logPolicyResolverProvider != null ? logPolicyResolverProvider.getIfAvailable() : null;
    }

    private void writeBatchFileLog(String eventType, JobExecution jobExecution, StepExecution stepExecution) {
        CpfBatchFileLogWriter writer = batchFileLogWriterProvider != null ? batchFileLogWriterProvider.getIfAvailable() : null;
        if (writer != null) {
            writer.writeBatch(eventType, jobExecution, stepExecution);
        }
    }

    private void putPolicy(org.springframework.batch.item.ExecutionContext context, String prefix, LogPolicyDecision decision) {
        if (context == null || decision == null) {
            return;
        }
        context.putString(prefix + "targetType", decision.targetType());
        context.putString(prefix + "targetId", decision.targetId());
        context.putString(prefix + "fileLogLevel", decision.fileLogLevel());
        context.putString(prefix + "dbLogEnabled", decision.dbLogEnabledYn());
        context.putString(prefix + "dbLogLevel", decision.dbLogLevel());
        context.putString(prefix + "requestBodySaveYn", decision.requestBodySaveYn());
        context.putString(prefix + "responseBodySaveYn", decision.responseBodySaveYn());
        context.putString(prefix + "errorStackSaveYn", decision.errorStackSaveYn());
        context.putString(prefix + "resolvedSource", decision.resolvedSource());
        if (decision.policyId() != null) {
            context.putLong(prefix + "policyId", decision.policyId());
        }
        if (decision.overrideId() != null) {
            context.putLong(prefix + "overrideId", decision.overrideId());
        }
    }
}
