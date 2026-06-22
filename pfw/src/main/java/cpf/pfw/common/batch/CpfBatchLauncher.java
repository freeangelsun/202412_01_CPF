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

/**
 * CPF 표준 배치 실행 Facade입니다.
 *
 * <p>호출자는 이 클래스를 통해 수동 실행, 스케줄 실행, 실패 재수행, 중지를 요청합니다.
 * 이 계층은 Spring Batch 실행 ID와 CPF 운영 메타 실행 ID를 함께 기록해 ADM 관제 화면에서
 * 한 흐름으로 조회할 수 있게 합니다.</p>
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
        if (operationType == CpfBatchOperationType.RETRY) {
            return retry(request);
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
                long executionId = repository.insertExecution(
                        request, "REQUESTED", null, ownerId, identity.serverInstanceId(), workerId,
                        transactionGlobalId, null, null);
                repository.recordWorkerHeartbeat(workerId, identity, "IDLE", null, null, user);
                repository.recordOperation(jobId, executionId, operationType.name(), user, request.reason(), null,
                        "Spring Batch 인프라 또는 Job bean이 없어 요청 이력만 기록했습니다.", "S", "REQUESTED");
                publish(CpfBatchEventType.EXECUTION_NOT_RUN, jobId, executionId, transactionGlobalId,
                        "Spring Batch 실행 인프라가 없어 요청만 기록했습니다.", Map.of());
                return result(false, jobId, executionId, null, "REQUESTED",
                        "Spring Batch 인프라 또는 Job bean이 없어 요청 이력만 기록했습니다.");
            }

            JobExecution execution = jobLauncher.run(job, toJobParameters(request, transactionGlobalId, user));
            long pfwExecutionId = repository.insertExecution(
                    request,
                    execution.getStatus().name(),
                    execution.getId(),
                    ownerId,
                    identity.serverInstanceId(),
                    workerId,
                    transactionGlobalId,
                    failureMessage(execution),
                    execution);
            String executionStatus = execution.getStatus().name();
            String executionFailureMessage = failureMessage(execution);
            boolean completed = "COMPLETED".equals(executionStatus);
            String executionMessage = completed
                    ? "배치 실행이 완료되었습니다."
                    : defaultIfBlank(executionFailureMessage, "배치 실행이 완료 상태가 아닙니다. status=" + executionStatus);
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
            long executionId = repository.available()
                    ? repository.insertExecution(
                            request, "FAILED", null, ownerId, identity.serverInstanceId(), workerId,
                            transactionGlobalId, ex.getMessage(), null)
                    : -1L;
            repository.recordWorkerHeartbeat(workerId, identity, "ERROR", jobId, executionId < 0 ? null : executionId, user);
            repository.recordOperation(jobId, executionId < 0 ? null : executionId, operationType.name() + "_FAILED",
                    user, request.reason(), null, ex.getMessage(), "F", ex.getMessage());
            publish(CpfBatchEventType.RUN_FAILED, jobId, executionId < 0 ? null : executionId, transactionGlobalId,
                    ex.getMessage(), Map.of());
            return result(false, jobId, executionId < 0 ? null : executionId, null, "FAILED", ex.getMessage());
        } finally {
            if (locked) {
                lockManager.release(lockKey, ownerId);
            }
        }
    }

    private CpfBatchExecutionResult retry(CpfBatchExecutionRequest request) {
        if (request.sourceExecutionId() == null) {
            throw new IllegalArgumentException("재수행 기준 실행 ID는 필수입니다.");
        }
        Map<String, Object> source = repository.findExecution(request.sourceExecutionId());
        String jobId = String.valueOf(source.get("job_id"));
        String parameters = String.valueOf(source.get("job_parameters"));
        String user = request.normalizedRequestUser("PFW_BATCH");
        Object springExecutionId = source.get("spring_batch_execution_id");
        publish(CpfBatchEventType.RETRY_REQUESTED, jobId, request.sourceExecutionId(), TransactionContext.getOrCreateTransactionId(),
                "배치 재수행 요청", Map.of());

        if (jobOperator != null && springExecutionId != null) {
            try {
                long restartedId = jobOperator.restart(Long.parseLong(String.valueOf(springExecutionId)));
                CpfBatchExecutionRequest retryRequest = CpfBatchExecutionRequest.run(jobId, parameters, user, request.reason());
                Identity identity = ServerInstanceIdentity.current();
                String transactionGlobalId = TransactionContext.getOrCreateTransactionId();
                repository.recordWorkerHeartbeat(identity.serverInstanceId(), identity, "RUNNING", jobId, null, user);
                long pfwExecutionId = repository.insertExecution(
                        retryRequest, "RESTARTED", restartedId, identity.serverInstanceId(),
                        identity.serverInstanceId(), identity.serverInstanceId(), transactionGlobalId,
                        null, jobExplorer == null ? null : jobExplorer.getJobExecution(restartedId));
                repository.recordWorkerHeartbeat(identity.serverInstanceId(), identity, "IDLE", null, null, user);
                repository.recordOperation(jobId, pfwExecutionId, "RETRY", user, request.reason(), String.valueOf(source),
                        "SPRING_BATCH_EXECUTION_ID=" + restartedId, "S", "RESTARTED");
                return result(true, jobId, pfwExecutionId, restartedId, "RESTARTED", "Spring Batch 재시작을 요청했습니다.");
            } catch (Exception ignored) {
                // restart가 불가능하면 동일 파라미터 신규 실행 흐름으로 떨어뜨립니다.
            }
        }

        return launch(CpfBatchExecutionRequest.run(jobId, parameters, user, request.reason()), CpfBatchOperationType.RETRY);
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

    private JobParameters toJobParameters(CpfBatchExecutionRequest request, String transactionGlobalId, String user) {
        return new JobParametersBuilder()
                .addString("cpfJobParameters", request.normalizedJobParameters())
                .addString("cpfRequestUser", user)
                .addString("cpfRequestReason", request.normalizedReason("배치 실행 요청"))
                .addString("transactionGlobalId", transactionGlobalId)
                .addString("serverInstanceId", ServerInstanceIdentity.current().serverInstanceId())
                .addLong("requestTime", System.currentTimeMillis())
                .toJobParameters();
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
