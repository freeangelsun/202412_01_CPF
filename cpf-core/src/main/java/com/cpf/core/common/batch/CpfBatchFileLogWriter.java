package com.cpf.core.common.batch;

import com.cpf.core.common.logging.SensitiveDataMasker;
import com.cpf.core.common.logging.ServerInstanceIdentity;
import com.cpf.core.common.logging.TransactionContext;
import com.cpf.core.common.logging.TransactionIdGenerator;
import com.cpf.core.common.logging.file.CpfFileLogWriter;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Spring Batch Job/Step 생명주기를 JobInstance별 JSON Lines 파일에 기록합니다.
 *
 * <p>파일 분리 키는 {@code businessDate + jobName + jobInstanceId}입니다. 같은 JobInstance를
 * 재시작해도 같은 파일을 사용하고, 실행 회차는 본문의 jobExecutionId와 restartAttempt로 구분합니다.</p>
 */
public class CpfBatchFileLogWriter {
    public static final String CONTEXT_BUSINESS_DATE = "cpf.batch.businessDate";
    public static final String CONTEXT_TRANSACTION_GLOBAL_ID = "cpf.batch.transactionGlobalId";
    public static final String CONTEXT_PARENT_TRANSACTION_GLOBAL_ID = "cpf.batch.parentTransactionGlobalId";
    public static final String CONTEXT_SEGMENT_ID = "cpf.batch.segmentId";
    public static final String CONTEXT_PARENT_SEGMENT_ID = "cpf.batch.parentSegmentId";
    public static final String CONTEXT_ORIGINAL_JOB_EXECUTION_ID = "cpf.batch.originalJobExecutionId";
    public static final String CONTEXT_RESTART_ATTEMPT = "cpf.batch.restartAttempt";

    private final CpfFileLogWriter fileLogWriter;
    private final TransactionIdGenerator transactionIdGenerator;
    private final Clock clock;
    private final CpfBatchLockManager writerLockManager;
    private final int writerLeaseSeconds;

    public CpfBatchFileLogWriter(CpfFileLogWriter fileLogWriter) {
        this(
                fileLogWriter,
                new TransactionIdGenerator("BAT", "batWK01", 7, Clock.system(fileLogWriter.logZoneId())),
                Clock.system(fileLogWriter.logZoneId()),
                null,
                30);
    }

    public CpfBatchFileLogWriter(
            CpfFileLogWriter fileLogWriter,
            TransactionIdGenerator transactionIdGenerator,
            Clock clock) {
        this(fileLogWriter, transactionIdGenerator, clock, null, 30);
    }

    public CpfBatchFileLogWriter(
            CpfFileLogWriter fileLogWriter,
            TransactionIdGenerator transactionIdGenerator,
            Clock clock,
            CpfBatchLockManager writerLockManager,
            int writerLeaseSeconds) {
        this.fileLogWriter = fileLogWriter;
        this.transactionIdGenerator = transactionIdGenerator;
        this.clock = clock.withZone(fileLogWriter.logZoneId());
        this.writerLockManager = writerLockManager;
        this.writerLeaseSeconds = Math.max(30, writerLeaseSeconds);
    }

    public void writeBatch(String eventType, JobExecution jobExecution, StepExecution stepExecution) {
        initializeContext(jobExecution);
        JobInstance jobInstance = requireJobInstance(jobExecution);
        long jobInstanceId = requirePositive(jobInstance.getInstanceId(), "jobInstanceId");
        LocalDate businessDate = resolveBusinessDate(jobExecution);
        String transactionGlobalId = resolveTransactionGlobalId(jobExecution);
        String jobSegmentId = resolveJobSegmentId(jobExecution, jobInstanceId);
        String workerInstanceId = parameterOrContext(jobExecution, "workerInstanceId",
                ServerInstanceIdentity.current().serverInstanceId());

        Map<String, Object> event = new LinkedHashMap<>(fileLogWriter.newBaseEvent("BAT", "batch"));
        event.put("eventType", defaultText(eventType, "BATCH"));
        event.put("businessDate", businessDate.format(DateTimeFormatter.BASIC_ISO_DATE));
        event.put("jobName", jobInstance.getJobName());
        event.put("jobInstanceId", jobInstanceId);
        event.put("jobExecutionId", jobExecution.getId());
        event.put("runId", parameterOrContext(jobExecution, "runId", executionId(jobExecution)));
        event.put("rerunId", parameterOrContext(jobExecution, "rerunId", "0"));
        event.put("originalJobExecutionId", contextValue(jobExecution, CONTEXT_ORIGINAL_JOB_EXECUTION_ID));
        event.put("restartAttempt", contextLong(jobExecution, CONTEXT_RESTART_ATTEMPT, 0L));
        event.put("transactionGlobalId", transactionGlobalId);
        event.put("parentTransactionGlobalId", contextValue(jobExecution, CONTEXT_PARENT_TRANSACTION_GLOBAL_ID));
        event.put("segmentId", stepExecution == null ? jobSegmentId : stepSegmentId(stepExecution));
        event.put("parentSegmentId", stepExecution == null
                ? contextValue(jobExecution, CONTEXT_PARENT_SEGMENT_ID)
                : jobSegmentId);
        event.put("serverInstanceId", ServerInstanceIdentity.current().serverInstanceId());
        event.put("workerInstanceId", workerInstanceId);
        event.put("selectedInstanceId", parameterOrContext(jobExecution, "selectedInstanceId", workerInstanceId));
        event.put("status", jobExecution.getStatus() != null ? jobExecution.getStatus().name() : "UNKNOWN");
        event.put("startTime", format(jobExecution.getStartTime()));
        event.put("endTime", format(jobExecution.getEndTime()));
        event.put("durationMs", duration(jobExecution.getStartTime(), jobExecution.getEndTime()));
        event.put("traceBoostPolicyId", firstText(
                contextValue(jobExecution, "cpf.logPolicy.job.overrideId"),
                contextValue(jobExecution, "cpf.logPolicy.job.policyId")));
        event.put("logLevelApplied", contextValue(jobExecution, "cpf.logPolicy.job.fileLogLevel"));
        appendStepFields(event, jobExecution, stepExecution);

        writeWithOwnership(
                CpfBatchJobLogPath.relativePath(jobInstance.getJobName(), jobInstanceId, businessDate),
                jobInstance.getJobName(),
                jobInstanceId,
                jobExecution,
                event);
    }

    /**
     * 공유 로그 파일은 DB lease를 획득한 단일 writer만 기록합니다.
     *
     * <p>CPF DB가 없는 로컬 실행은 JVM 단일 writer 모드로 동작합니다. 다른 인스턴스가 lease를
     * 보유한 경우에는 이벤트를 버리지 않고 인스턴스별 fragment에 기록해 운영 병합 대상으로 남깁니다.</p>
     */
    private void writeWithOwnership(
            java.nio.file.Path logicalPath,
            String jobName,
            long jobInstanceId,
            JobExecution execution,
            Map<String, Object> event) {
        if (writerLockManager == null || !writerLockManager.available()) {
            event.put("writerOwnershipMode", "LOCAL_SINGLE_WRITER");
            event.put("logicalFile", logicalPath.toString().replace('\\', '/'));
            fileLogWriter.writeEventAtRelativePath(logicalPath, event);
            return;
        }

        String ownerId = ServerInstanceIdentity.current().serverInstanceId()
                + ':' + parameterOrContext(execution, "workerInstanceId", "worker")
                + ':' + executionId(execution);
        String lockKey = "batch:file:" + CpfBatchJobLogPath.sanitize(jobName) + ':' + jobInstanceId;
        boolean acquired = writerLockManager.acquire(
                lockKey,
                jobName,
                logicalPath.toString(),
                ownerId,
                writerLeaseSeconds);
        event.put("writerLockKey", lockKey);
        event.put("writerOwnerId", ownerId);
        event.put("logicalFile", logicalPath.toString().replace('\\', '/'));
        if (!acquired) {
            event.put("writerOwnershipMode", "DEGRADED_FRAGMENT");
            event.put("writerConflict", true);
            fileLogWriter.writeEventAtRelativePath(fragmentPath(logicalPath, ownerId), event);
            return;
        }

        try {
            event.put("writerOwnershipMode", "DB_LEASE");
            event.put("writerConflict", false);
            fileLogWriter.writeEventAtRelativePath(logicalPath, event);
        } finally {
            writerLockManager.release(lockKey, ownerId);
        }
    }

    private java.nio.file.Path fragmentPath(java.nio.file.Path logicalPath, String ownerId) {
        String fileName = logicalPath.getFileName().toString();
        String safeOwner = CpfBatchJobLogPath.sanitize(ownerId);
        return logicalPath.getParent()
                .resolve("fragments")
                .resolve(fileName.replaceFirst("\\.log$", "") + '-' + safeOwner + ".fragment.log");
    }

    /**
     * Job 시작 시 업무일자와 추적 식별자를 ExecutionContext에 고정합니다.
     */
    public void initializeContext(JobExecution jobExecution) {
        JobInstance jobInstance = requireJobInstance(jobExecution);
        long jobInstanceId = requirePositive(jobInstance.getInstanceId(), "jobInstanceId");
        resolveBusinessDate(jobExecution);
        resolveTransactionGlobalId(jobExecution);
        resolveJobSegmentId(jobExecution, jobInstanceId);
        putIfAbsent(jobExecution, CONTEXT_PARENT_TRANSACTION_GLOBAL_ID,
                firstText(parameter(jobExecution.getJobParameters(), "parentTransactionGlobalId"),
                        TransactionContext.parentTransactionId()));
        putIfAbsent(jobExecution, CONTEXT_PARENT_SEGMENT_ID,
                firstText(parameter(jobExecution.getJobParameters(), "parentSegmentId"),
                        TransactionContext.currentSpanId()));
        putIfAbsent(jobExecution, CONTEXT_ORIGINAL_JOB_EXECUTION_ID,
                parameter(jobExecution.getJobParameters(), "originalJobExecutionId"));
        if (!jobExecution.getExecutionContext().containsKey(CONTEXT_RESTART_ATTEMPT)) {
            long attempt = longParameter(jobExecution.getJobParameters(), "restartAttempt",
                    contextValue(jobExecution, CONTEXT_ORIGINAL_JOB_EXECUTION_ID) == null ? 0L : 1L);
            jobExecution.getExecutionContext().putLong(CONTEXT_RESTART_ATTEMPT, attempt);
        }
    }

    private void appendStepFields(
            Map<String, Object> event,
            JobExecution jobExecution,
            StepExecution stepExecution) {
        if (stepExecution == null) {
            event.put("stepName", null);
            event.put("stepExecutionId", null);
            event.put("readCount", sum(jobExecution, StepExecution::getReadCount));
            event.put("writeCount", sum(jobExecution, StepExecution::getWriteCount));
            event.put("filterCount", sum(jobExecution, StepExecution::getFilterCount));
            event.put("skipCount", sum(jobExecution, StepExecution::getSkipCount));
            event.put("commitCount", sum(jobExecution, StepExecution::getCommitCount));
            event.put("rollbackCount", sum(jobExecution, StepExecution::getRollbackCount));
            event.put("errorCode", null);
            event.put("errorMessageMasked", null);
            return;
        }
        event.put("stepName", stepExecution.getStepName());
        event.put("stepExecutionId", stepExecution.getId());
        event.put("status", stepExecution.getStatus() != null ? stepExecution.getStatus().name() : "UNKNOWN");
        event.put("startTime", format(stepExecution.getStartTime()));
        event.put("endTime", format(stepExecution.getEndTime()));
        event.put("durationMs", duration(stepExecution.getStartTime(), stepExecution.getEndTime()));
        event.put("readCount", stepExecution.getReadCount());
        event.put("writeCount", stepExecution.getWriteCount());
        event.put("filterCount", stepExecution.getFilterCount());
        event.put("skipCount", stepExecution.getSkipCount());
        event.put("commitCount", stepExecution.getCommitCount());
        event.put("rollbackCount", stepExecution.getRollbackCount());
        event.put("errorCode", stepExecution.getExitStatus() != null
                ? stepExecution.getExitStatus().getExitCode()
                : null);
        event.put("errorMessageMasked", stepExecution.getExitStatus() != null
                ? SensitiveDataMasker.mask(stepExecution.getExitStatus().getExitDescription())
                : null);
        event.put("traceBoostPolicyId", firstText(
                contextValue(stepExecution, "cpf.logPolicy.step.overrideId"),
                contextValue(stepExecution, "cpf.logPolicy.step.policyId")));
        event.put("logLevelApplied", contextValue(stepExecution, "cpf.logPolicy.step.fileLogLevel"));
    }

    private long sum(JobExecution execution, java.util.function.ToLongFunction<StepExecution> extractor) {
        if (execution == null || execution.getStepExecutions() == null) {
            return 0L;
        }
        return execution.getStepExecutions().stream().mapToLong(extractor).sum();
    }

    private LocalDate resolveBusinessDate(JobExecution execution) {
        String current = contextValue(execution, CONTEXT_BUSINESS_DATE);
        if (current != null) {
            return parseBusinessDate(current);
        }
        String parameter = parameter(execution.getJobParameters(), "businessDate");
        LocalDate resolved = parameter == null ? LocalDate.now(clock) : parseBusinessDate(parameter);
        execution.getExecutionContext().putString(
                CONTEXT_BUSINESS_DATE,
                resolved.format(DateTimeFormatter.BASIC_ISO_DATE));
        return resolved;
    }

    private String resolveTransactionGlobalId(JobExecution execution) {
        String current = contextValue(execution, CONTEXT_TRANSACTION_GLOBAL_ID);
        if (current != null) {
            return current;
        }
        String resolved = firstText(
                parameter(execution.getJobParameters(), "transactionGlobalId"),
                TransactionContext.currentTransactionId());
        if (resolved == null) {
            resolved = transactionIdGenerator.generate("BAT", "batWK01");
        }
        execution.getExecutionContext().putString(CONTEXT_TRANSACTION_GLOBAL_ID, resolved);
        return resolved;
    }

    private String resolveJobSegmentId(JobExecution execution, long jobInstanceId) {
        String current = contextValue(execution, CONTEXT_SEGMENT_ID);
        if (current != null) {
            return current;
        }
        String resolved = "BAT-JOB-" + jobInstanceId;
        execution.getExecutionContext().putString(CONTEXT_SEGMENT_ID, resolved);
        return resolved;
    }

    private String stepSegmentId(StepExecution stepExecution) {
        Object id = stepExecution.getId();
        return "BAT-STEP-" + (id == null ? CpfBatchJobLogPath.sanitize(stepExecution.getStepName()) : id);
    }

    private JobInstance requireJobInstance(JobExecution execution) {
        if (execution == null || execution.getJobInstance() == null) {
            throw new IllegalArgumentException("JobExecution과 JobInstance는 필수입니다.");
        }
        return execution.getJobInstance();
    }

    private long requirePositive(Long value, String field) {
        if (value == null || value < 1) {
            throw new IllegalArgumentException(field + "는 1 이상이어야 합니다.");
        }
        return value;
    }

    private LocalDate parseBusinessDate(String value) {
        try {
            return value.contains("-")
                    ? LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE)
                    : LocalDate.parse(value, DateTimeFormatter.BASIC_ISO_DATE);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("businessDate는 yyyyMMdd 또는 yyyy-MM-dd 형식이어야 합니다.", ex);
        }
    }

    private String parameterOrContext(JobExecution execution, String key, String fallback) {
        return defaultText(firstText(parameter(execution.getJobParameters(), key), contextValue(execution, key)), fallback);
    }

    private String parameter(JobParameters parameters, String key) {
        if (parameters == null || parameters.getParameter(key) == null) {
            return null;
        }
        Object value = parameters.getParameter(key).getValue();
        return value == null ? null : String.valueOf(value);
    }

    private long longParameter(JobParameters parameters, String key, long fallback) {
        String value = parameter(parameters, key);
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(key + "는 숫자여야 합니다.", ex);
        }
    }

    private String contextValue(JobExecution execution, String key) {
        if (execution == null || execution.getExecutionContext() == null
                || !execution.getExecutionContext().containsKey(key)) {
            return null;
        }
        Object value = execution.getExecutionContext().get(key);
        return value == null ? null : String.valueOf(value);
    }

    private String contextValue(StepExecution execution, String key) {
        if (execution == null || execution.getExecutionContext() == null
                || !execution.getExecutionContext().containsKey(key)) {
            return null;
        }
        Object value = execution.getExecutionContext().get(key);
        return value == null ? null : String.valueOf(value);
    }

    private long contextLong(JobExecution execution, String key, long fallback) {
        String value = contextValue(execution, key);
        return value == null ? fallback : Long.parseLong(value);
    }

    private void putIfAbsent(JobExecution execution, String key, String value) {
        if (value != null && !value.isBlank() && !execution.getExecutionContext().containsKey(key)) {
            execution.getExecutionContext().putString(key, value);
        }
    }

    private Long duration(LocalDateTime start, LocalDateTime end) {
        if (start == null) {
            return null;
        }
        LocalDateTime resolvedEnd = end != null ? end : LocalDateTime.now(clock);
        return Duration.between(start, resolvedEnd).toMillis();
    }

    private String format(LocalDateTime value) {
        return value == null ? null : value.atZone(fileLogWriter.logZoneId()).toOffsetDateTime().toString();
    }

    private String executionId(JobExecution execution) {
        return execution.getId() == null ? "0" : String.valueOf(execution.getId());
    }

    private String firstText(String first, String second) {
        return first != null && !first.isBlank() ? first : second;
    }

    private String defaultText(String value, String fallback) {
        return value != null && !value.isBlank() ? value : fallback;
    }
}
