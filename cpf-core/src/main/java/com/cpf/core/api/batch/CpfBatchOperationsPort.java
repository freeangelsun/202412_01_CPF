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
    List<Map<String,Object>> findExecutions(String jobId, int limit);
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
    List<Map<String,Object>> findBusinessCalendar(String calendarId, String fromDate, String toDate);
    Map<String,Object> registerJob(String jobId, String jobName, String jobType, String description, String requestUser);
    Map<String,Object> saveBusinessDay(String calendarId, String businessDate, String holidayYn, String businessDayYn,
                                       String description, String requestUser);
    Map<String,Object> requestRun(String jobId, String jobParameters, String requestUser, String reason);
    Map<String,Object> requestScheduledRun(String scheduleId, String jobId, String jobParameters, String requestUser, String reason);
    Map<String,Object> requestRetry(long executionId, String requestUser, String reason);
    Map<String,Object> requestStop(long executionId, String requestUser, String reason);
    Map<String,Object> updateScheduleEnabled(String scheduleId, boolean enabled, String requestUser, String reason);
    List<Map<String,Object>> runSchedulerOnce(String requestUser);
}
