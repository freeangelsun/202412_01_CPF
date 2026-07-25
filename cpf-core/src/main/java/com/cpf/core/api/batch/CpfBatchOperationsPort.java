package com.cpf.core.api.batch;

import java.util.List;
import java.util.Map;

/**
 * BAT Owner가 제공하고 ADM 같은 Control Plane이 소비하는 Batch 운영 계약입니다.
 *
 * <p>Consumer는 batDB나 Spring Batch Repository를 직접 접근하지 않습니다. 동일 JVM에서는
 * BAT 구현 Bean을 사용하고, 분리 WAS에서는 동일 계약의 Remote Adapter를 사용합니다.</p>
 */
public interface CpfBatchOperationsPort {
    List<Map<String,Object>> findJobs();
    Map<String,Object> findJobDetail(String jobId);
    List<Map<String,Object>> findSchedules();
    /**
     * 배치 실행을 Job/transactionId/Spring Job Instance/Worker/Server Instance 축으로 통합 검색합니다.
     */
    List<Map<String,Object>> findExecutions(
            String jobId,
            String transactionId,
            Long springBatchJobInstanceId,
            String workerId,
            String serverInstanceId,
            int limit);

    /**
     * 운영 다운로드/추적용 실행 조회입니다. 기간 조건은 BAT Owner가 자신의 스키마에 적용합니다.
     */
    default List<Map<String,Object>> findExecutions(
            String jobId,
            String transactionId,
            Long springBatchJobInstanceId,
            String workerId,
            String serverInstanceId,
            String fromDate,
            String toDate,
            int limit) {
        return findExecutions(jobId, transactionId, springBatchJobInstanceId, workerId, serverInstanceId, limit);
    }

    default List<Map<String,Object>> findExecutions(String jobId, int limit) {
        return findExecutions(jobId, null, null, null, null, limit);
    }
    Map<String,Object> findExecutionDetail(long executionId);
    List<Map<String,Object>> findInstances();
    List<Map<String,Object>> findWorkers(int heartbeatTimeoutSeconds);
    List<Map<String,Object>> findStepExecutions(Long executionId, String jobId, int limit);
    List<Map<String,Object>> findRelations(String jobId);
    List<Map<String,Object>> findExecutionTargets(String jobId, String dispatchStatus, int limit);
    List<Map<String,Object>> findLocks(String jobId);
    Map<String,Object> releaseLock(String lockKey, String requestUser, String reason);
    List<Map<String,Object>> findGhostCandidates(int heartbeatTimeoutSeconds);
    Map<String,Object> actGhostExecution(long executionId, String actionType, String requestUser, String reason);
    List<Map<String,Object>> findOperationLogs(String jobId, Long executionId, int limit);
    List<Map<String,Object>> simulateSchedule(String scheduleId, String baseDate, int days);
    Map<String,Object> registerJob(String jobId, String jobName, String jobType, String description, String requestUser);
    Map<String,Object> requestRun(String jobId, String jobParameters, String requestUser, String reason);
    Map<String,Object> requestScheduledRun(String scheduleId, String jobId, String jobParameters, String requestUser, String reason);
    Map<String,Object> requestRetry(long executionId, String requestUser, String reason);
    Map<String,Object> requestStop(long executionId, String requestUser, String reason);
    Map<String,Object> updateScheduleEnabled(String scheduleId, boolean enabled, String requestUser, String reason);
    List<Map<String,Object>> runSchedulerOnce(String requestUser);
}
