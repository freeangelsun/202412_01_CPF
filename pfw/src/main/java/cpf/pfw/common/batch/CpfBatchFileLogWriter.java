package cpf.pfw.common.batch;

import cpf.pfw.common.logging.ServerInstanceIdentity;
import cpf.pfw.common.logging.SensitiveDataMasker;
import cpf.pfw.common.logging.file.CpfFileLogWriter;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Spring Batch Job/Step 생명주기를 CPF 파일 로그 표준에 맞춰 기록합니다.
 */
public class CpfBatchFileLogWriter {
    private final CpfFileLogWriter fileLogWriter;

    public CpfBatchFileLogWriter(CpfFileLogWriter fileLogWriter) {
        this.fileLogWriter = fileLogWriter;
    }

    public void writeBatch(String eventType, JobExecution jobExecution, StepExecution stepExecution) {
        String moduleCode = "BAT";
        Map<String, Object> event = fileLogWriter.newBaseEvent(moduleCode, "batch");
        event.put("eventType", defaultText(eventType, "BATCH"));
        if (jobExecution != null) {
            event.put("jobName", jobExecution.getJobInstance() != null ? jobExecution.getJobInstance().getJobName() : null);
            event.put("jobExecutionId", jobExecution.getId());
            event.put("runId", jobExecution.getJobParameters() != null ? jobExecution.getJobParameters().getString("runId") : null);
            event.put("rerunId", jobExecution.getJobParameters() != null ? jobExecution.getJobParameters().getString("rerunId") : null);
            event.put("transactionGlobalId", executionValue(jobExecution, "transactionGlobalId"));
            event.put("traceBoostPolicyId", firstText(
                    executionValue(jobExecution, "cpf.logPolicy.job.overrideId"),
                    executionValue(jobExecution, "cpf.logPolicy.job.policyId")));
            event.put("logLevelApplied", executionValue(jobExecution, "cpf.logPolicy.job.fileLogLevel"));
            event.put("status", jobExecution.getStatus() != null ? jobExecution.getStatus().name() : null);
            event.put("durationMs", duration(jobExecution.getStartTime(), jobExecution.getEndTime()));
        }
        if (stepExecution != null) {
            event.put("stepName", stepExecution.getStepName());
            event.put("stepExecutionId", stepExecution.getId());
            event.put("chunkNo", stepExecution.getCommitCount());
            event.put("itemId", null);
            event.put("partitionNo", null);
            event.put("traceBoostPolicyId", firstText(
                    executionValue(stepExecution, "cpf.logPolicy.step.overrideId"),
                    executionValue(stepExecution, "cpf.logPolicy.step.policyId")));
            event.put("logLevelApplied", executionValue(stepExecution, "cpf.logPolicy.step.fileLogLevel"));
            event.put("status", stepExecution.getStatus() != null ? stepExecution.getStatus().name() : null);
            event.put("durationMs", duration(stepExecution.getStartTime(), stepExecution.getEndTime()));
            event.put("failureCode", stepExecution.getExitStatus() != null ? stepExecution.getExitStatus().getExitCode() : null);
            event.put("failureMessageMasked", stepExecution.getExitStatus() != null
                    ? SensitiveDataMasker.mask(stepExecution.getExitStatus().getExitDescription())
                    : null);
        }
        event.put("workerInstanceId", ServerInstanceIdentity.current().serverInstanceId());
        fileLogWriter.writeEvent(moduleCode, "batch", event);
    }

    private String executionValue(JobExecution execution, String key) {
        if (execution == null || execution.getExecutionContext() == null || !execution.getExecutionContext().containsKey(key)) {
            return null;
        }
        Object value = execution.getExecutionContext().get(key);
        return value != null ? String.valueOf(value) : null;
    }

    private String executionValue(StepExecution execution, String key) {
        if (execution == null || execution.getExecutionContext() == null || !execution.getExecutionContext().containsKey(key)) {
            return null;
        }
        Object value = execution.getExecutionContext().get(key);
        return value != null ? String.valueOf(value) : null;
    }

    private Long duration(LocalDateTime start, LocalDateTime end) {
        if (start == null) {
            return null;
        }
        LocalDateTime resolvedEnd = end != null ? end : LocalDateTime.now();
        return java.time.Duration.between(start, resolvedEnd).toMillis();
    }

    private String firstText(String first, String second) {
        return first != null && !first.isBlank() ? first : second;
    }

    private String defaultText(String value, String fallback) {
        return value != null && !value.isBlank() ? value : fallback;
    }
}
