package cpf.pfw.common.batch;

import cpf.pfw.common.logging.ServerInstanceIdentity;
import cpf.pfw.common.logging.ServerInstanceIdentity.Identity;
import cpf.pfw.common.logging.TransactionContext;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;

import java.util.LinkedHashMap;
import java.util.Map;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * CPF 표준 배치 실행 Facade입니다.
 *
 * <p>호출자는 이 클래스만 사용해 수동 실행, 스케줄 실행, 실패 재수행, 중지를 요청합니다.
 * 실행 시작 전에 CPF execution을 먼저 만들고 Spring Batch JobParameter에 CPF execution ID를 전달하므로
 * Job/Step listener가 실행 중 heartbeat와 진행률을 같은 행에 갱신할 수 있습니다.</p>
 */
public class CpfBatchLauncher {
    private final JobLauncher jobLauncher;
    private final JobExplorer jobExplorer;
    private final JobOperator jobOperator;
    private final Map<String, Job> jobs;
    private final CpfBatchOperationRepository repository;
    private final CpfBatchLockManager lockManager;
    private final CpfBatchEventPublisher eventPublisher;
    private final int lockTtlSeconds;

    public CpfBatchLauncher(
            ObjectProvider<JobLauncher> jobLauncherProvider,
            ObjectProvider<JobExplorer> jobExplorerProvider,
            ObjectProvider<JobOperator> jobOperatorProvider,
            ObjectProvider<Map<String, Job>> jobsProvider,
            ObjectProvider<CpfBatchEventPublisher> eventPublisherProvider,
            CpfBatchOperationRepository repository,
            CpfBatchLockManager lockManager,
            @Value("${cpf.batch.lock-ttl-seconds:600}") int lockTtlSeconds) {
        this.jobLauncher = jobLauncherProvider.getIfAvailable();
        this.jobExplorer = jobExplorerProvider.getIfAvailable();
        this.jobOperator = jobOperatorProvider.getIfAvailable();
        this.jobs = jobsProvider.getIfAvailable(Map::of);
        this.eventPublisher = eventPublisherProvider.getIfAvailable(CpfBatchLoggingEventPublisher::new);
        this.repository = repository;
        this.lockManager = lockManager;
        this.lockTtlSeconds = lockTtlSeconds;
    }

    public CpfBatchExecutionResult run(CpfBatchExecutionRequest request) {
        CpfBatchOperationType operationType = request.operationType() == null ? CpfBatchOperationType.RUN : request.operationType();
        if (operationType == CpfBatchOperationType.RESTART) {
            return restart(request);
        }
        if (operationType == CpfBatchOperationType.RERUN) {
            return rerun(request);
        }
        if (operationType == CpfBatchOperationType.RETRY) {
            return retryCompatible(request);
        }
        if (operationType == CpfBatchOperationType.STOP) {
            return stop(request);
        }
        return launch(request, operationType);
    }

    public Map<String, Object> findExecutionDetail(long executionId) {
        return repository.available() ? repository.findExecutionDetail(executionId) : Map.of();
    }

    private CpfBatchExecutionResult launch(CpfBatchExecutionRequest request, CpfBatchOperationType operationType) {
        String jobId = request.requiredJobId();
        String user = request.normalizedRequestUser("PFW_BATCH");
        String transactionGlobalId = TransactionContext.getOrCreateTransactionId();
        Identity identity = ServerInstanceIdentity.current();
        String ownerId = identity.serverInstanceId();
        String workerId = ownerId;
        String lockKey = lockManager.lockKey(jobId, request.normalizedJobParameters());
        boolean locked = false;
        long pfwExecutionId = -1L;

        if (request.lockRequired() && !lockManager.acquire(lockKey, jobId, request.normalizedJobParameters(), ownerId, lockTtlSeconds)) {
            return notRun(jobId, "동일 Job/파라미터가 이미 실행 중입니다.", transactionGlobalId);
        }

        try {
            locked = request.lockRequired();
            Job job = resolveJob(jobId);
            repository.ensureJob(jobId, job == null ? jobId : job.getName(), "TASKLET", user);
            repository.recordWorkerHeartbeat(workerId, identity, "RUNNING", jobId, null, user);
            publish(CpfBatchEventType.RUN_REQUESTED, jobId, null, transactionGlobalId, "배치 실행 요청", Map.of());

            if (jobLauncher == null || job == null) {
                pfwExecutionId = repository.startExecution(
                        request,
                        "REQUESTED",
                        null,
                        ownerId,
                        identity.serverInstanceId(),
                        workerId,
                        transactionGlobalId,
                        user);
                repository.recordWorkerHeartbeat(workerId, identity, "IDLE", null, null, user);
                repository.recordOperation(jobId, pfwExecutionId, operationType.name(), user, request.reason(), null,
                        "Spring Batch 실행 인프라 또는 Job bean이 없어 요청 이력만 기록했습니다.", "S", "REQUESTED");
                publish(CpfBatchEventType.EXECUTION_NOT_RUN, jobId, pfwExecutionId, transactionGlobalId,
                        "Spring Batch 실행 인프라가 없어 요청만 기록했습니다.", Map.of());
                return result(false, jobId, pfwExecutionId, null, "REQUESTED",
                        "Spring Batch 실행 인프라 또는 Job bean이 없어 요청 이력만 기록했습니다.");
            }

            pfwExecutionId = repository.startExecution(
                    request,
                    "RUNNING",
                    null,
                    ownerId,
                    identity.serverInstanceId(),
                    workerId,
                    transactionGlobalId,
                    user);
            repository.recordWorkerHeartbeat(workerId, identity, "RUNNING", jobId, pfwExecutionId, user);
            JobExecution execution = jobLauncher.run(job, toJobParameters(request, transactionGlobalId, user, pfwExecutionId));
            String executionStatus = execution.getStatus().name();
            String executionFailureMessage = failureMessage(execution);
            repository.completeExecution(
                    pfwExecutionId,
                    executionStatus,
                    execution.getId(),
                    workerId,
                    executionFailureMessage,
                    execution,
                    user);
            boolean completed = "COMPLETED".equals(executionStatus);
            String executionMessage = completed
                    ? "배치 실행이 완료되었습니다."
                    : defaultIfBlank(executionFailureMessage, "배치 실행 완료 상태가 아닙니다. status=" + executionStatus);
            repository.recordWorkerHeartbeat(workerId, identity, "IDLE", null, null, user);
            repository.recordOperation(jobId, pfwExecutionId, operationType.name(), user, request.reason(), null,
                    "SPRING_BATCH_EXECUTION_ID=" + execution.getId(), completed ? "S" : "F", executionStatus);
            publish(completed ? CpfBatchEventType.RUN_COMPLETED : CpfBatchEventType.RUN_FAILED,
                    jobId,
                    pfwExecutionId,
                    transactionGlobalId,
                    executionMessage,
                    Map.of("springBatchExecutionId", execution.getId()));
            return result(true, jobId, pfwExecutionId, execution.getId(), executionStatus, executionMessage);
        } catch (Exception ex) {
            if (pfwExecutionId < 1 && repository.available()) {
                pfwExecutionId = repository.startExecution(
                        request,
                        "FAILED",
                        null,
                        ownerId,
                        identity.serverInstanceId(),
                        workerId,
                        transactionGlobalId,
                        user);
            }
            if (pfwExecutionId > 0) {
                repository.completeExecution(pfwExecutionId, "FAILED", null, workerId, ex.getMessage(), null, user);
            }
            repository.recordWorkerHeartbeat(workerId, identity, "ERROR", jobId, pfwExecutionId < 0 ? null : pfwExecutionId, user);
            repository.recordOperation(jobId, pfwExecutionId < 0 ? null : pfwExecutionId, operationType.name() + "_FAILED",
                    user, request.reason(), null, ex.getMessage(), "F", ex.getMessage());
            publish(CpfBatchEventType.RUN_FAILED, jobId, pfwExecutionId < 0 ? null : pfwExecutionId, transactionGlobalId,
                    ex.getMessage(), Map.of());
            return result(false, jobId, pfwExecutionId < 0 ? null : pfwExecutionId, null, "FAILED", ex.getMessage());
        } finally {
            if (locked) {
                lockManager.release(lockKey, ownerId);
            }
        }
    }

    private CpfBatchExecutionResult restart(CpfBatchExecutionRequest request) {
        if (request.sourceExecutionId() == null) {
            throw new IllegalArgumentException("재시작 기준 실행 ID는 필수입니다.");
        }
        Map<String, Object> source = repository.findExecution(request.sourceExecutionId());
        String jobId = String.valueOf(source.get("job_id"));
        String user = request.normalizedRequestUser("PFW_BATCH");
        Object springExecutionId = source.get("spring_batch_execution_id");
        publish(CpfBatchEventType.RETRY_REQUESTED, jobId, request.sourceExecutionId(), TransactionContext.getOrCreateTransactionId(),
                "배치 재시작 요청", Map.of());

        if (jobOperator == null || springExecutionId == null) {
            return result(false, jobId, null, null, "RESTART_NOT_AVAILABLE",
                    "Spring Batch 재시작에 필요한 실행 정보가 없습니다.");
        }
        try {
            long restartedId = jobOperator.restart(Long.parseLong(String.valueOf(springExecutionId)));
            CpfBatchExecutionRequest restartRequest = CpfBatchExecutionRequest.run(
                    jobId, String.valueOf(source.get("job_parameters")), user, request.reason());
            Identity identity = ServerInstanceIdentity.current();
            String transactionGlobalId = TransactionContext.getOrCreateTransactionId();
            long pfwExecutionId = repository.startExecution(
                    restartRequest, "RESTARTED", restartedId, identity.serverInstanceId(),
                    identity.serverInstanceId(), identity.serverInstanceId(), transactionGlobalId, user);
            repository.recordOperation(jobId, pfwExecutionId, "RESTART", user, request.reason(), String.valueOf(source),
                    "SPRING_BATCH_EXECUTION_ID=" + restartedId, "S", "RESTARTED");
            return result(true, jobId, pfwExecutionId, restartedId, "RESTARTED", "Spring Batch 재시작을 요청했습니다.");
        } catch (Exception ex) {
            repository.recordOperation(jobId, request.sourceExecutionId(), "RESTART_FAILED", user,
                    request.reason(), String.valueOf(source), null, "F", ex.getClass().getSimpleName());
            return result(false, jobId, null, toLong(springExecutionId), "RESTART_FAILED",
                    "Spring Batch 재시작을 요청할 수 없습니다.");
        }
    }

    private CpfBatchExecutionResult rerun(CpfBatchExecutionRequest request) {
        if (request.sourceExecutionId() == null) {
            throw new IllegalArgumentException("재수행 기준 실행 ID는 필수입니다.");
        }
        Map<String, Object> source = repository.findExecution(request.sourceExecutionId());
        String jobId = String.valueOf(source.get("job_id"));
        String parameters = String.valueOf(source.get("job_parameters"));
        CpfBatchExecutionRequest rerunRequest = CpfBatchExecutionRequest.run(
                jobId, parameters, request.normalizedRequestUser("PFW_BATCH"), request.reason());
        return launch(rerunRequest, CpfBatchOperationType.RERUN);
    }

    /** 기존 RETRY API의 동작은 재시작 우선, 불가능하면 신규 재수행으로 유지합니다. */
    private CpfBatchExecutionResult retryCompatible(CpfBatchExecutionRequest request) {
        CpfBatchExecutionResult restarted = restart(request);
        return restarted.executed() ? restarted : rerun(request);
    }

    private CpfBatchExecutionResult stop(CpfBatchExecutionRequest request) {
        if (request.sourceExecutionId() == null) {
            throw new IllegalArgumentException("중지 기준 실행 ID는 필수입니다.");
        }
        Map<String, Object> before = repository.findExecution(request.sourceExecutionId());
        String jobId = String.valueOf(before.get("job_id"));
        Object springExecutionId = before.get("spring_batch_execution_id");
        String user = request.normalizedRequestUser("PFW_BATCH");
        if (jobOperator != null && springExecutionId != null) {
            try {
                jobOperator.stop(Long.parseLong(String.valueOf(springExecutionId)));
            } catch (Exception ignored) {
                // Spring Batch 중지 요청 실패도 CPF 운영 메타에는 중지 요청으로 남깁니다.
            }
        }
        repository.updateExecutionStatus(request.sourceExecutionId(), "STOPPING", user);
        Map<String, Object> after = repository.findExecution(request.sourceExecutionId());
        repository.recordOperation(jobId, request.sourceExecutionId(), "STOP", user, request.reason(),
                String.valueOf(before), String.valueOf(after), "S", "STOPPING");
        publish(CpfBatchEventType.STOP_REQUESTED, jobId, request.sourceExecutionId(), TransactionContext.getOrCreateTransactionId(),
                "배치 중지 요청", Map.of());
        return CpfBatchExecutionResult.of(false, jobId, request.sourceExecutionId(), toLong(springExecutionId),
                "STOPPING", "배치 중지를 요청했습니다.", after);
    }

    private JobParameters toJobParameters(
            CpfBatchExecutionRequest request,
            String transactionGlobalId,
            String user,
            long pfwExecutionId) {
        String workerInstanceId = ServerInstanceIdentity.current().serverInstanceId();
        long requestTime = System.currentTimeMillis();
        JobParametersBuilder builder = new JobParametersBuilder()
                .addString("cpfJobParameters", request.normalizedJobParameters())
                .addString("cpfRequestUser", user)
                .addString("cpfRequestReason", request.normalizedReason("배치 실행 요청"))
                .addString("transactionGlobalId", transactionGlobalId)
                .addString("businessDate", hasText(request.businessDate())
                        ? request.businessDate().trim()
                        : LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE))
                .addString("serverInstanceId", workerInstanceId)
                .addString("workerInstanceId", workerInstanceId)
                .addString("selectedInstanceId", workerInstanceId)
                .addString("runId", String.valueOf(requestTime))
                .addString("rerunId", "0")
                .addLong("restartAttempt", 0L)
                .addLong(CpfBatchHeartbeatService.PARAM_PFW_EXECUTION_ID, pfwExecutionId)
                .addLong("requestTime", requestTime);
        if (hasText(request.standardBatchId())) {
            builder.addString("standardBatchId", request.standardBatchId().trim());
        }
        if (hasText(request.idempotencyKey())) {
            builder.addString("idempotencyKey", request.idempotencyKey().trim());
        }
        if (TransactionContext.parentTransactionId() != null) {
            builder.addString("parentTransactionGlobalId", TransactionContext.parentTransactionId());
        }
        if (TransactionContext.currentSpanId() != null) {
            builder.addString("parentSegmentId", TransactionContext.currentSpanId());
        }
        return builder.toJobParameters();
    }

    private Job resolveJob(String jobId) {
        Job direct = jobs.get(jobId);
        if (direct != null) {
            return direct;
        }
        return jobs.values().stream()
                .filter(job -> jobId.equals(job.getName()))
                .findFirst()
                .orElse(null);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String failureMessage(JobExecution execution) {
        if (execution == null || execution.getAllFailureExceptions().isEmpty()) {
            return null;
        }
        return execution.getAllFailureExceptions().toString();
    }

    private CpfBatchExecutionResult result(
            boolean executed,
            String jobId,
            Long pfwExecutionId,
            Long springBatchExecutionId,
            String status,
            String message) {
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("jobId", jobId);
        detail.put("executionId", pfwExecutionId);
        detail.put("springBatchExecutionId", springBatchExecutionId);
        detail.put("status", status);
        detail.put("message", message);
        return CpfBatchExecutionResult.of(executed, jobId, pfwExecutionId, springBatchExecutionId, status, message, detail);
    }

    private CpfBatchExecutionResult notRun(String jobId, String message, String transactionGlobalId) {
        publish(CpfBatchEventType.EXECUTION_NOT_RUN, jobId, null, transactionGlobalId, message, Map.of());
        return CpfBatchExecutionResult.of(false, jobId, null, null, "SKIPPED_LOCKED", message, Map.of("message", message));
    }

    private void publish(
            CpfBatchEventType eventType,
            String jobId,
            Long executionId,
            String transactionGlobalId,
            String message,
            Map<String, Object> payload) {
        eventPublisher.publish(CpfBatchEvent.now(eventType, jobId, executionId, transactionGlobalId, message, payload));
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }

    private String defaultIfBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
