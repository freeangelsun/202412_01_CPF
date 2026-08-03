package com.cpf.core.api.batch;

import java.util.List;
import com.cpf.core.api.data.CpfDataRow;

/**
 * BAT Owner가 제공하고 ADM 같은 Control Plane이 소비하는 Batch 운영 계약입니다.
 *
 * <p>Consumer는 batDB나 Spring Batch Repository를 직접 접근하지 않습니다. 동일 JVM에서는
 * BAT 구현 Bean을 사용하고, 분리 WAS에서는 동일 계약의 Remote Adapter를 사용합니다.</p>
 */
public interface CpfBatchOperationsPort {
    List<CpfDataRow> findJobs();
    CpfDataRow findJobDetail(String jobId);
    List<CpfDataRow> findSchedules();
    /**
     * 배치 실행을 Job/transactionId/Spring Job Instance/Worker/Server Instance 축으로 통합 검색합니다.
     */
    List<CpfDataRow> findExecutions(
            String jobId,
            String transactionId,
            Long springBatchJobInstanceId,
            String workerId,
            String serverInstanceId,
            int limit);

    /**
     * 운영 다운로드/추적용 실행 조회입니다. 기간 조건은 BAT Owner가 자신의 스키마에 적용합니다.
     */
    default List<CpfDataRow> findExecutions(
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

    default List<CpfDataRow> findExecutions(String jobId, int limit) {
        return findExecutions(jobId, null, null, null, null, limit);
    }


    /**
     * 배치 실행 Workbench용 서버 Paging 계약입니다.
     *
     * <p>기존 구현과의 호환성을 위해 기본 구현은 BAT Owner 조회 결과를 서버에서 Windowing합니다.
     * DB Adapter가 offset/cursor paging을 지원하면 이 메서드를 재정의해야 합니다. 응답은
     * {@code items,page,size,hasNext,totalKnown,pagingMode}를 포함합니다.</p>
     */
    default CpfDataRow findExecutionPage(
            String jobId,
            String transactionId,
            Long springBatchJobInstanceId,
            String workerId,
            String serverInstanceId,
            String status,
            String fromDate,
            String toDate,
            int page,
            int size) {
        int safePage = Math.max(0, page);
        int safeSize = Math.max(10, Math.min(size, 200));
        int required = Math.min(5000, Math.addExact(Math.multiplyExact(safePage + 1, safeSize), 1));
        List<CpfDataRow> source = findExecutions(
                jobId, transactionId, springBatchJobInstanceId, workerId, serverInstanceId,
                fromDate, toDate, required);
        java.util.function.Predicate<CpfDataRow> statusFilter = row -> {
            if (status == null || status.isBlank()) return true;
            String expected = status.trim();
            for (String key : List.of("status", "execution_status", "batch_status", "STATUS", "EXECUTION_STATUS")) {
                Object value = row.get(key);
                if (value != null && expected.equalsIgnoreCase(String.valueOf(value))) return true;
            }
            return false;
        };
        List<CpfDataRow> filtered = source.stream().filter(statusFilter).toList();
        int from = Math.min(filtered.size(), safePage * safeSize);
        int to = Math.min(filtered.size(), from + safeSize);
        boolean hasNext = filtered.size() > to || source.size() >= required;
        return CpfDataRow.of(
                "items", filtered.subList(from, to),
                "page", safePage,
                "size", safeSize,
                "hasNext", hasNext,
                "totalKnown", false,
                "pagingMode", "OWNER_WINDOW");
    }


    /** Job Workbench server paging contract owned by BAT. */
    default CpfDataRow findJobPage(String query, int page, int size, String sort, String direction) {
        return ownerPage(findJobs(), query, page, size, sort, direction, "BAT_JOB_OWNER_WINDOW");
    }

    /** Scheduler HA Workbench server paging contract owned by BAT. */
    default CpfDataRow findSchedulePage(String query, int page, int size, String sort, String direction) {
        return ownerPage(findSchedules(), query, page, size, sort, direction, "BAT_SCHEDULE_OWNER_WINDOW");
    }

    /** Instance/Worker/Target snapshot assembled by the BAT owner boundary. */
    default CpfDataRow findInfrastructureSnapshot(int heartbeatTimeoutSeconds, int limit) {
        return CpfDataRow.of(
                "instances", findInstances(),
                "workers", findWorkers(Math.max(5, heartbeatTimeoutSeconds)),
                "targets", findExecutionTargets(null, null, Math.max(1, Math.min(limit, 1000))),
                "partial", false,
                "stale", false,
                "pagingMode", "BAT_OWNER_SNAPSHOT");
    }

    /** Ghost/Lease/Operation snapshot assembled by the BAT owner boundary. */
    default CpfDataRow findRecoverySnapshot(int heartbeatTimeoutSeconds, int limit) {
        return CpfDataRow.of(
                "ghostCandidates", findGhostCandidates(Math.max(5, heartbeatTimeoutSeconds)),
                "locks", findLocks(null),
                "operations", findOperationLogs(null, null, Math.max(1, Math.min(limit, 1000))),
                "partial", false,
                "stale", false,
                "pagingMode", "BAT_OWNER_SNAPSHOT");
    }

    private static CpfDataRow ownerPage(
            List<CpfDataRow> source,
            String query,
            int page,
            int size,
            String sort,
            String direction,
            String pagingMode) {
        int safePage = Math.max(0, page);
        int safeSize = Math.max(10, Math.min(size, 200));
        String needle = query == null ? "" : query.trim().toLowerCase(java.util.Locale.ROOT);
        List<CpfDataRow> filtered = source == null ? List.of() : source.stream()
                .filter(row -> needle.isEmpty() || row.values().stream().anyMatch(value -> value != null
                        && String.valueOf(value).toLowerCase(java.util.Locale.ROOT).contains(needle)))
                .map(CpfDataRow::new)
                .collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new));
        String sortKey = sort == null ? "" : sort.trim();
        if (!sortKey.isEmpty()) {
            java.util.Comparator<CpfDataRow> comparator = java.util.Comparator.comparing(
                    row -> String.valueOf(row.getOrDefault(sortKey, "")),
                    java.util.Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
            if ("desc".equalsIgnoreCase(direction)) comparator = comparator.reversed();
            filtered.sort(comparator.thenComparing(CpfDataRow::toString));
        }
        int from = Math.min(filtered.size(), safePage * safeSize);
        int to = Math.min(filtered.size(), from + safeSize);
        return CpfDataRow.of(
                "items", List.copyOf(filtered.subList(from, to)),
                "page", safePage,
                "size", safeSize,
                "total", filtered.size(),
                "hasNext", to < filtered.size(),
                "totalKnown", true,
                "pagingMode", pagingMode,
                "partial", false,
                "stale", false);
    }

    CpfDataRow findExecutionDetail(long executionId);
    List<CpfDataRow> findInstances();
    List<CpfDataRow> findWorkers(int heartbeatTimeoutSeconds);
    List<CpfDataRow> findStepExecutions(Long executionId, String jobId, int limit);
    List<CpfDataRow> findRelations(String jobId);
    List<CpfDataRow> findExecutionTargets(String jobId, String dispatchStatus, int limit);
    List<CpfDataRow> findLocks(String jobId);
    CpfDataRow releaseLock(String lockKey, String requestUser, String reason);

    /** Version-aware expired lock release. Version-less providers fail closed by default. */
    default CpfDataRow releaseLock(
            String lockKey, String requestUser, String reason, long expectedVersion) {
        throw expectedVersionUnsupported("releaseLock");
    }

    /** 승인·멱등·Version을 포함한 운영용 계약. */
    default CpfDataRow releaseLock(String lockKey, CpfBatchRiskCommand command) {
        throw riskCommandUnsupported("releaseLock");
    }

    List<CpfDataRow> findGhostCandidates(int heartbeatTimeoutSeconds);
    CpfDataRow actGhostExecution(long executionId, String actionType, String requestUser, String reason);

    /** Version-aware ghost recovery command. */
    default CpfDataRow actGhostExecution(
            long executionId, String actionType, String requestUser, String reason, long expectedVersion) {
        throw expectedVersionUnsupported("actGhostExecution");
    }

    default CpfDataRow actGhostExecution(long executionId, String actionType, CpfBatchRiskCommand command) {
        throw riskCommandUnsupported("actGhostExecution");
    }
    List<CpfDataRow> findOperationLogs(String jobId, Long executionId, int limit);
    List<CpfDataRow> simulateSchedule(String scheduleId, String baseDate, int days);
    CpfDataRow registerJob(String jobId, String jobName, String jobType, String description, String requestUser);
    CpfDataRow requestRun(String jobId, String jobParameters, String requestUser, String reason);
    CpfDataRow requestScheduledRun(String scheduleId, String jobId, String jobParameters, String requestUser, String reason);
    CpfDataRow requestRetry(long executionId, String requestUser, String reason);

    /** Version-aware retry based on the exact source execution snapshot. */
    default CpfDataRow requestRetry(
            long executionId, String requestUser, String reason, long expectedVersion) {
        throw expectedVersionUnsupported("requestRetry");
    }

    default CpfDataRow requestRetry(long executionId, CpfBatchRiskCommand command) {
        throw riskCommandUnsupported("requestRetry");
    }

    CpfDataRow requestStop(long executionId, String requestUser, String reason);

    /** Version-aware stop request. */
    default CpfDataRow requestStop(
            long executionId, String requestUser, String reason, long expectedVersion) {
        throw expectedVersionUnsupported("requestStop");
    }

    default CpfDataRow requestStop(long executionId, CpfBatchRiskCommand command) {
        throw riskCommandUnsupported("requestStop");
    }

    CpfDataRow updateScheduleEnabled(String scheduleId, boolean enabled, String requestUser, String reason);

    /** Version-aware schedule enable/disable. */
    default CpfDataRow updateScheduleEnabled(
            String scheduleId, boolean enabled, String requestUser, String reason, long expectedVersion) {
        throw expectedVersionUnsupported("updateScheduleEnabled");
    }

    default CpfDataRow updateScheduleEnabled(
            String scheduleId, boolean enabled, CpfBatchRiskCommand command) {
        throw riskCommandUnsupported("updateScheduleEnabled");
    }

    List<CpfDataRow> runSchedulerOnce(String requestUser);

    /** 수동 실행도 승인·멱등 문맥을 잃지 않는 위험조치 계약을 사용합니다. */
    default CpfDataRow requestRun(String jobId, String jobParameters, CpfBatchRiskCommand command) {
        throw riskCommandUnsupported("requestRun");
    }

    default List<CpfDataRow> runSchedulerOnce(CpfBatchRiskCommand command) {
        throw riskCommandUnsupported("runSchedulerOnce");
    }

    private static UnsupportedOperationException expectedVersionUnsupported(String operation) {
        return new UnsupportedOperationException(
                "BAT provider does not implement the required expectedVersion contract: " + operation);
    }

    private static UnsupportedOperationException riskCommandUnsupported(String operation) {
        return new UnsupportedOperationException(
                "BAT provider does not implement the required approval/idempotency contract: " + operation);
    }
}
