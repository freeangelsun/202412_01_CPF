package cpf.adm.opr.service;

import cpf.adm.opr.dto.PfwBatchScheduleCandidate;
import cpf.pfw.common.batch.CpfBatchLockManager;
import cpf.pfw.common.logging.SensitiveDataMasker;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * PFW 배치 스케줄 자동 실행기입니다.
 *
 * <p>스케줄 판정, 중복 실행 lock, 실행 대상 기록, 실행 요청을 순서대로 처리합니다.
 * 운영 안전성을 위해 기본값은 비활성화이며, `cpf.batch.scheduler.enabled=true`일 때 주기 실행됩니다.</p>
 */
@Component
public class PfwBatchScheduler {
    private final PfwBatchScheduleService scheduleService;
    private final CpfBatchLockManager lockService;
    private final PfwBatchExecutionTargetService targetService;
    private final AdmBatchOperationService batchOperationService;
    private final JdbcTemplate pfwJdbcTemplate;
    private final boolean enabled;
    private final int lockTtlSeconds;
    private final String ownerId;

    public PfwBatchScheduler(
            PfwBatchScheduleService scheduleService,
            CpfBatchLockManager lockService,
            PfwBatchExecutionTargetService targetService,
            AdmBatchOperationService batchOperationService,
            @Qualifier("pfwJdbcTemplate") JdbcTemplate pfwJdbcTemplate,
            @Value("${cpf.batch.scheduler.enabled:false}") boolean enabled,
            @Value("${cpf.batch.scheduler.lock-ttl-seconds:600}") int lockTtlSeconds) {
        this.scheduleService = scheduleService;
        this.lockService = lockService;
        this.targetService = targetService;
        this.batchOperationService = batchOperationService;
        this.pfwJdbcTemplate = pfwJdbcTemplate;
        this.enabled = enabled;
        this.lockTtlSeconds = lockTtlSeconds;
        this.ownerId = ownerId();
    }

    @Scheduled(
            initialDelayString = "${cpf.batch.scheduler.initial-delay-ms:30000}",
            fixedDelayString = "${cpf.batch.scheduler.fixed-delay-ms:60000}")
    public void runScheduledTick() {
        if (enabled) {
            runOnce("PfwBatchScheduler");
        }
    }

    public List<Map<String, Object>> runOnce(String requestUser) {
        List<Map<String, Object>> results = new ArrayList<>();
        for (PfwBatchScheduleCandidate candidate : scheduleService.findDueSchedules(LocalDateTime.now())) {
            results.add(runCandidate(candidate, requestUser));
        }
        return results;
    }

    private Map<String, Object> runCandidate(PfwBatchScheduleCandidate candidate, String requestUser) {
        String lockKey = lockService.scheduleLockKey(candidate.scheduleId(), String.valueOf(candidate.plannedRunAt()));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("scheduleId", candidate.scheduleId());
        result.put("jobId", candidate.jobId());
        result.put("plannedRunAt", String.valueOf(candidate.plannedRunAt()));

        if (!lockService.acquire(lockKey, candidate.jobId(), candidate.jobParameters(), ownerId, lockTtlSeconds)) {
            result.put("status", "SKIPPED_LOCKED");
            return result;
        }

        Long targetId = null;
        try {
            targetId = targetService.createWaitingTarget(candidate, requestUser);
            Map<String, Object> execution = batchOperationService.requestScheduledRun(
                    candidate.scheduleId(),
                    candidate.jobId(),
                    candidate.jobParameters(),
                    requestUser,
                    "자동 스케줄러 실행");
            Long executionId = extractExecutionId(execution);
            targetService.markDispatched(targetId, executionId, requestUser);
            scheduleService.updateFireTimes(candidate, requestUser);
            result.put("targetId", targetId);
            result.put("executionId", executionId);
            result.put("status", "DISPATCHED");
            return result;
        } catch (RuntimeException ex) {
            String message = SensitiveDataMasker.mask(ex.getMessage(), 500);
            if (targetId != null) {
                targetService.markFailed(targetId, message, requestUser);
            }
            recordSchedulerFailure(candidate, message, requestUser);
            result.put("targetId", targetId);
            result.put("status", "FAILED");
            result.put("message", message);
            return result;
        } finally {
            lockService.release(lockKey, ownerId);
        }
    }

    @SuppressWarnings("unchecked")
    private Long extractExecutionId(Map<String, Object> execution) {
        Object nested = execution.get("execution");
        if (nested instanceof Map<?, ?> map) {
            Object value = ((Map<String, Object>) map).get("execution_id");
            return toLong(value);
        }
        return toLong(execution.get("execution_id"));
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

    private void recordSchedulerFailure(PfwBatchScheduleCandidate candidate, String message, String requestUser) {
        pfwJdbcTemplate.update("""
                INSERT INTO pfw_notification_delivery_log (
                    rule_id, event_type, target_type, target_id, receiver,
                    delivery_status, delivery_message, requested_at, delivered_at,
                    created_by, updated_by
                ) VALUES (NULL, 'BATCH_SCHEDULER_FAILED', 'pfw_batch_schedule', ?, 'ADM_OPERATOR',
                          'FAILED', ?, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), ?, ?)
                """,
                candidate.scheduleId(),
                message,
                requestUser,
                requestUser);
    }

    private String ownerId() {
        try {
            return InetAddress.getLocalHost().getHostName() + ":" + ManagementFactory.getRuntimeMXBean().getName();
        } catch (Exception ex) {
            return "cpf-batch-scheduler";
        }
    }
}
