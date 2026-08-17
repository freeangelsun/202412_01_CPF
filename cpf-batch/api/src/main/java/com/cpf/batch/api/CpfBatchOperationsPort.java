package com.cpf.batch.api;

import java.util.List;
import com.cpf.data.api.CpfDataRow;

/**
 * BAT Owner가 제공하고 ADM 같은 Control Plane이 소비하는 Batch 운영 계약입니다.
 *
 * <p>Consumer는 batDB나 Spring Batch Repository를 직접 접근하지 않습니다. 동일 JVM에서는
 * BAT 구현 Bean을 사용하고, 분리 WAS에서는 동일 계약의 Remote Adapter를 사용합니다.</p>
 */
public interface CpfBatchOperationsPort {
    /**
     * findJobs 공개 조회/판정 계약을 수행합니다. 조회·판정 자체는 업무 상태를 변경하지 않으며 실패나 UNKNOWN을 정상 결과로 오인하지 않습니다.
     * @return Owner 경계에서 조회한 결과입니다. 조회 불가/부분 실패는 구현 계약에 따라 명시적으로 표현해야 합니다.
     */
    List<CpfDataRow> findJobs();
    /**
     * findJobDetail 공개 조회/판정 계약을 수행합니다. 조회·판정 자체는 업무 상태를 변경하지 않으며 실패나 UNKNOWN을 정상 결과로 오인하지 않습니다.
     * @param jobId 배치 Job 식별자입니다. 조회 필터에서는 null/blank가 전체 범위를 뜻할 수 있습니다.
     * @return Owner 경계에서 조회한 결과입니다. 조회 불가/부분 실패는 구현 계약에 따라 명시적으로 표현해야 합니다.
     */
    CpfDataRow findJobDetail(String jobId);
    /**
     * findSchedules 공개 조회/판정 계약을 수행합니다. 조회·판정 자체는 업무 상태를 변경하지 않으며 실패나 UNKNOWN을 정상 결과로 오인하지 않습니다.
     * @return Owner 경계에서 조회한 결과입니다. 조회 불가/부분 실패는 구현 계약에 따라 명시적으로 표현해야 합니다.
     */
    List<CpfDataRow> findSchedules();
        /**
     * findExecutions 공개 조회/판정 계약을 수행합니다. 조회·판정 자체는 업무 상태를 변경하지 않으며 실패나 UNKNOWN을 정상 결과로 오인하지 않습니다.
     * @param jobId 배치 Job 식별자입니다. 조회 필터에서는 null/blank가 전체 범위를 뜻할 수 있습니다.
     * @param transactionId CPF 표준 거래 식별자입니다. null/blank 허용 여부는 해당 조회 설명을 따릅니다.
     * @param springBatchJobInstanceId Spring Batch Job Instance 식별자입니다. null이면 해당 축을 필터링하지 않습니다.
     * @param workerId 배치 Worker 식별자입니다. null/blank이면 해당 축을 필터링하지 않습니다.
     * @param instanceId 서버 Instance 식별자입니다. null/blank이면 해당 축을 필터링하지 않습니다.
     * @param limit 최대 조회 건수입니다. 구현체는 문서화된 상한으로 제한해야 합니다.
     * @return Owner 경계에서 조회한 결과입니다. 조회 불가/부분 실패는 구현 계약에 따라 명시적으로 표현해야 합니다.
     */
    List<CpfDataRow> findExecutions(
            String jobId,
            String transactionId,
            Long springBatchJobInstanceId,
            String workerId,
            String instanceId,
            int limit);

        /**
     * findExecutions 공개 조회/판정 계약을 수행합니다. 조회·판정 자체는 업무 상태를 변경하지 않으며 실패나 UNKNOWN을 정상 결과로 오인하지 않습니다.
     * @param jobId 배치 Job 식별자입니다. 조회 필터에서는 null/blank가 전체 범위를 뜻할 수 있습니다.
     * @param transactionId CPF 표준 거래 식별자입니다. null/blank 허용 여부는 해당 조회 설명을 따릅니다.
     * @param springBatchJobInstanceId Spring Batch Job Instance 식별자입니다. null이면 해당 축을 필터링하지 않습니다.
     * @param workerId 배치 Worker 식별자입니다. null/blank이면 해당 축을 필터링하지 않습니다.
     * @param instanceId 서버 Instance 식별자입니다. null/blank이면 해당 축을 필터링하지 않습니다.
     * @param fromDate 조회 시작 일시/일자 문자열입니다. null/blank이면 시작 경계를 적용하지 않습니다.
     * @param toDate 조회 종료 일시/일자 문자열입니다. null/blank이면 종료 경계를 적용하지 않습니다.
     * @param limit 최대 조회 건수입니다. 구현체는 문서화된 상한으로 제한해야 합니다.
     * @return Owner 경계에서 조회한 결과입니다. 조회 불가/부분 실패는 구현 계약에 따라 명시적으로 표현해야 합니다.
     */
    default List<CpfDataRow> findExecutions(
            String jobId,
            String transactionId,
            Long springBatchJobInstanceId,
            String workerId,
            String instanceId,
            String fromDate,
            String toDate,
            int limit) {
        return findExecutions(jobId, transactionId, springBatchJobInstanceId, workerId, instanceId, limit);
    }

    /**
     * findExecutions 공개 조회/판정 계약을 수행합니다. 조회·판정 자체는 업무 상태를 변경하지 않으며 실패나 UNKNOWN을 정상 결과로 오인하지 않습니다.
     * @param jobId 배치 Job 식별자입니다. 조회 필터에서는 null/blank가 전체 범위를 뜻할 수 있습니다.
     * @param limit 최대 조회 건수입니다. 구현체는 문서화된 상한으로 제한해야 합니다.
     * @return Owner 경계에서 조회한 결과입니다. 조회 불가/부분 실패는 구현 계약에 따라 명시적으로 표현해야 합니다.
     */
    default List<CpfDataRow> findExecutions(String jobId, int limit) {
        return findExecutions(jobId, null, null, null, null, limit);
    }


        /**
     * findExecutionPage 공개 조회/판정 계약을 수행합니다. 조회·판정 자체는 업무 상태를 변경하지 않으며 실패나 UNKNOWN을 정상 결과로 오인하지 않습니다.
     * @param jobId 배치 Job 식별자입니다. 조회 필터에서는 null/blank가 전체 범위를 뜻할 수 있습니다.
     * @param transactionId CPF 표준 거래 식별자입니다. null/blank 허용 여부는 해당 조회 설명을 따릅니다.
     * @param springBatchJobInstanceId Spring Batch Job Instance 식별자입니다. null이면 해당 축을 필터링하지 않습니다.
     * @param workerId 배치 Worker 식별자입니다. null/blank이면 해당 축을 필터링하지 않습니다.
     * @param instanceId 서버 Instance 식별자입니다. null/blank이면 해당 축을 필터링하지 않습니다.
     * @param status 상태 필터입니다. null/blank이면 상태를 제한하지 않습니다.
     * @param fromDate 조회 시작 일시/일자 문자열입니다. null/blank이면 시작 경계를 적용하지 않습니다.
     * @param toDate 조회 종료 일시/일자 문자열입니다. null/blank이면 종료 경계를 적용하지 않습니다.
     * @param page 0부터 시작하는 페이지 번호입니다.
     * @param size 요청 페이지 크기입니다. 구현체는 안전한 상한으로 제한해야 합니다.
     * @return Owner 경계에서 조회한 결과입니다. 조회 불가/부분 실패는 구현 계약에 따라 명시적으로 표현해야 합니다.
     */
    default CpfDataRow findExecutionPage(
            String jobId,
            String transactionId,
            Long springBatchJobInstanceId,
            String workerId,
            String instanceId,
            String status,
            String fromDate,
            String toDate,
            int page,
            int size) {
        int safePage = Math.max(0, page);
        int safeSize = Math.max(10, Math.min(size, 200));
        int required = Math.min(5000, Math.addExact(Math.multiplyExact(safePage + 1, safeSize), 1));
        List<CpfDataRow> source = findExecutions(
                jobId, transactionId, springBatchJobInstanceId, workerId, instanceId,
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


        /**
     * findJobPage 공개 조회/판정 계약을 수행합니다. 조회·판정 자체는 업무 상태를 변경하지 않으며 실패나 UNKNOWN을 정상 결과로 오인하지 않습니다.
     * @param query 검색어입니다. null/blank이면 전체 조회로 처리합니다.
     * @param page 0부터 시작하는 페이지 번호입니다.
     * @param size 요청 페이지 크기입니다. 구현체는 안전한 상한으로 제한해야 합니다.
     * @param sort 정렬 키입니다. null/blank이면 기본 정렬을 사용합니다.
     * @param direction 정렬 방향입니다. desc 외 값은 기본 오름차순으로 처리합니다.
     * @return Owner 경계에서 조회한 결과입니다. 조회 불가/부분 실패는 구현 계약에 따라 명시적으로 표현해야 합니다.
     */
    default CpfDataRow findJobPage(String query, int page, int size, String sort, String direction) {
        return ownerPage(findJobs(), query, page, size, sort, direction, "BAT_JOB_OWNER_WINDOW");
    }

        /**
     * findSchedulePage 공개 조회/판정 계약을 수행합니다. 조회·판정 자체는 업무 상태를 변경하지 않으며 실패나 UNKNOWN을 정상 결과로 오인하지 않습니다.
     * @param query 검색어입니다. null/blank이면 전체 조회로 처리합니다.
     * @param page 0부터 시작하는 페이지 번호입니다.
     * @param size 요청 페이지 크기입니다. 구현체는 안전한 상한으로 제한해야 합니다.
     * @param sort 정렬 키입니다. null/blank이면 기본 정렬을 사용합니다.
     * @param direction 정렬 방향입니다. desc 외 값은 기본 오름차순으로 처리합니다.
     * @return Owner 경계에서 조회한 결과입니다. 조회 불가/부분 실패는 구현 계약에 따라 명시적으로 표현해야 합니다.
     */
    default CpfDataRow findSchedulePage(String query, int page, int size, String sort, String direction) {
        return ownerPage(findSchedules(), query, page, size, sort, direction, "BAT_SCHEDULE_OWNER_WINDOW");
    }

        /**
     * findInfrastructureSnapshot 공개 조회/판정 계약을 수행합니다. 조회·판정 자체는 업무 상태를 변경하지 않으며 실패나 UNKNOWN을 정상 결과로 오인하지 않습니다.
     * @param heartbeatTimeoutSeconds 장애/ghost 판단에 사용할 heartbeat 제한 초입니다.
     * @param limit 최대 조회 건수입니다. 구현체는 문서화된 상한으로 제한해야 합니다.
     * @return Owner 경계에서 조회한 결과입니다. 조회 불가/부분 실패는 구현 계약에 따라 명시적으로 표현해야 합니다.
     */
    default CpfDataRow findInfrastructureSnapshot(int heartbeatTimeoutSeconds, int limit) {
        return CpfDataRow.of(
                "instances", findInstances(),
                "workers", findWorkers(Math.max(5, heartbeatTimeoutSeconds)),
                "targets", findExecutionTargets(null, null, Math.max(1, Math.min(limit, 1000))),
                "partial", false,
                "stale", false,
                "pagingMode", "BAT_OWNER_SNAPSHOT");
    }

        /**
     * findRecoverySnapshot 공개 조회/판정 계약을 수행합니다. 조회·판정 자체는 업무 상태를 변경하지 않으며 실패나 UNKNOWN을 정상 결과로 오인하지 않습니다.
     * @param heartbeatTimeoutSeconds 장애/ghost 판단에 사용할 heartbeat 제한 초입니다.
     * @param limit 최대 조회 건수입니다. 구현체는 문서화된 상한으로 제한해야 합니다.
     * @return Owner 경계에서 조회한 결과입니다. 조회 불가/부분 실패는 구현 계약에 따라 명시적으로 표현해야 합니다.
     */
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

    /**
     * findExecutionDetail 공개 조회/판정 계약을 수행합니다. 조회·판정 자체는 업무 상태를 변경하지 않으며 실패나 UNKNOWN을 정상 결과로 오인하지 않습니다.
     * @param executionId 배치 실행 식별자입니다.
     * @return Owner 경계에서 조회한 결과입니다. 조회 불가/부분 실패는 구현 계약에 따라 명시적으로 표현해야 합니다.
     */
    CpfDataRow findExecutionDetail(long executionId);
    /**
     * findInstances 공개 조회/판정 계약을 수행합니다. 조회·판정 자체는 업무 상태를 변경하지 않으며 실패나 UNKNOWN을 정상 결과로 오인하지 않습니다.
     * @return Owner 경계에서 조회한 결과입니다. 조회 불가/부분 실패는 구현 계약에 따라 명시적으로 표현해야 합니다.
     */
    List<CpfDataRow> findInstances();
    /**
     * findWorkers 공개 조회/판정 계약을 수행합니다. 조회·판정 자체는 업무 상태를 변경하지 않으며 실패나 UNKNOWN을 정상 결과로 오인하지 않습니다.
     * @param heartbeatTimeoutSeconds 장애/ghost 판단에 사용할 heartbeat 제한 초입니다.
     * @return Owner 경계에서 조회한 결과입니다. 조회 불가/부분 실패는 구현 계약에 따라 명시적으로 표현해야 합니다.
     */
    List<CpfDataRow> findWorkers(int heartbeatTimeoutSeconds);
    /**
     * findStepExecutions 공개 조회/판정 계약을 수행합니다. 조회·판정 자체는 업무 상태를 변경하지 않으며 실패나 UNKNOWN을 정상 결과로 오인하지 않습니다.
     * @param executionId 배치 실행 식별자입니다.
     * @param jobId 배치 Job 식별자입니다. 조회 필터에서는 null/blank가 전체 범위를 뜻할 수 있습니다.
     * @param limit 최대 조회 건수입니다. 구현체는 문서화된 상한으로 제한해야 합니다.
     * @return Owner 경계에서 조회한 결과입니다. 조회 불가/부분 실패는 구현 계약에 따라 명시적으로 표현해야 합니다.
     */
    List<CpfDataRow> findStepExecutions(Long executionId, String jobId, int limit);
    /**
     * findRelations 공개 조회/판정 계약을 수행합니다. 조회·판정 자체는 업무 상태를 변경하지 않으며 실패나 UNKNOWN을 정상 결과로 오인하지 않습니다.
     * @param jobId 배치 Job 식별자입니다. 조회 필터에서는 null/blank가 전체 범위를 뜻할 수 있습니다.
     * @return Owner 경계에서 조회한 결과입니다. 조회 불가/부분 실패는 구현 계약에 따라 명시적으로 표현해야 합니다.
     * @throws UnsupportedOperationException Provider가 필수 승인/버전 계약을 구현하지 않은 경우
     */
    List<CpfDataRow> findRelations(String jobId);
    /**
     * findExecutionTargets 공개 조회/판정 계약을 수행합니다. 조회·판정 자체는 업무 상태를 변경하지 않으며 실패나 UNKNOWN을 정상 결과로 오인하지 않습니다.
     * @param jobId 배치 Job 식별자입니다. 조회 필터에서는 null/blank가 전체 범위를 뜻할 수 있습니다.
     * @param dispatchStatus dispatchStatus 입력값입니다. null 허용 여부는 메서드 설명을 따릅니다.
     * @param limit 최대 조회 건수입니다. 구현체는 문서화된 상한으로 제한해야 합니다.
     * @return Owner 경계에서 조회한 결과입니다. 조회 불가/부분 실패는 구현 계약에 따라 명시적으로 표현해야 합니다.
     * @throws UnsupportedOperationException Provider가 필수 승인/버전 계약을 구현하지 않은 경우
     */
    List<CpfDataRow> findExecutionTargets(String jobId, String dispatchStatus, int limit);
    /**
     * findLocks 공개 조회/판정 계약을 수행합니다. 조회·판정 자체는 업무 상태를 변경하지 않으며 실패나 UNKNOWN을 정상 결과로 오인하지 않습니다.
     * @param jobId 배치 Job 식별자입니다. 조회 필터에서는 null/blank가 전체 범위를 뜻할 수 있습니다.
     * @return Owner 경계에서 조회한 결과입니다. 조회 불가/부분 실패는 구현 계약에 따라 명시적으로 표현해야 합니다.
     * @throws UnsupportedOperationException Provider가 필수 승인/버전 계약을 구현하지 않은 경우
     */
    List<CpfDataRow> findLocks(String jobId);
    /**
     * releaseLock 운영 조작을 수행합니다. 구현체는 인증 주체, 승인, 멱등성, 동시성, 감사와 UNKNOWN/복구 의미를 보존해야 합니다.
     *
     * <p>고위험 운영 Consumer는 가능한 경우 {@code CpfBatchRiskCommand} 기반 overload를 사용하고,
     * 버전 불일치·승인 불일치·결과 불확실성을 fail-closed로 처리해야 합니다.</p>
     * @param lockKey 배치 분산 Lock 식별자입니다.
     * @param requestUser 인증된 운영자 식별자입니다. 외부 입력을 그대로 신뢰하면 안 됩니다.
     * @param reason 운영 조작 사유입니다. 감사 로그에 남기되 민감정보를 포함하면 안 됩니다.
     * @return 조작 결과입니다. 실패·충돌·UNKNOWN 상태를 성공으로 축약해서는 안 됩니다.
     * @throws UnsupportedOperationException Provider가 필수 승인/버전 계약을 구현하지 않은 경우
     */
    CpfDataRow releaseLock(String lockKey, String requestUser, String reason);

        /**
     * releaseLock 운영 조작을 수행합니다. 구현체는 인증 주체, 승인, 멱등성, 동시성, 감사와 UNKNOWN/복구 의미를 보존해야 합니다.
     *
     * <p>고위험 운영 Consumer는 가능한 경우 {@code CpfBatchRiskCommand} 기반 overload를 사용하고,
     * 버전 불일치·승인 불일치·결과 불확실성을 fail-closed로 처리해야 합니다.</p>
     * @param lockKey 배치 분산 Lock 식별자입니다.
     * @param requestUser 인증된 운영자 식별자입니다. 외부 입력을 그대로 신뢰하면 안 됩니다.
     * @param reason 운영 조작 사유입니다. 감사 로그에 남기되 민감정보를 포함하면 안 됩니다.
     * @param expectedVersion 낙관적 동시성 제어용 기대 버전입니다. 불일치 시 조작은 실패해야 합니다.
     * @return 조작 결과입니다. 실패·충돌·UNKNOWN 상태를 성공으로 축약해서는 안 됩니다.
     * @throws UnsupportedOperationException Provider가 필수 승인/버전 계약을 구현하지 않은 경우
     */
    default CpfDataRow releaseLock(
            String lockKey, String requestUser, String reason, long expectedVersion) {
        throw expectedVersionUnsupported("releaseLock");
    }

        /**
     * releaseLock 운영 조작을 수행합니다. 구현체는 인증 주체, 승인, 멱등성, 동시성, 감사와 UNKNOWN/복구 의미를 보존해야 합니다.
     *
     * <p>고위험 운영 Consumer는 가능한 경우 {@code CpfBatchRiskCommand} 기반 overload를 사용하고,
     * 버전 불일치·승인 불일치·결과 불확실성을 fail-closed로 처리해야 합니다.</p>
     * @param lockKey 배치 분산 Lock 식별자입니다.
     * @param command 승인·멱등·감사·expectedVersion을 결합한 위험조치 명령입니다.
     * @return 조작 결과입니다. 실패·충돌·UNKNOWN 상태를 성공으로 축약해서는 안 됩니다.
     * @throws UnsupportedOperationException Provider가 필수 승인/버전 계약을 구현하지 않은 경우
     */
    default CpfDataRow releaseLock(String lockKey, CpfBatchRiskCommand command) {
        throw riskCommandUnsupported("releaseLock");
    }

    /**
     * findGhostCandidates 공개 조회/판정 계약을 수행합니다. 조회·판정 자체는 업무 상태를 변경하지 않으며 실패나 UNKNOWN을 정상 결과로 오인하지 않습니다.
     * @param heartbeatTimeoutSeconds 장애/ghost 판단에 사용할 heartbeat 제한 초입니다.
     * @return Owner 경계에서 조회한 결과입니다. 조회 불가/부분 실패는 구현 계약에 따라 명시적으로 표현해야 합니다.
     * @throws UnsupportedOperationException Provider가 필수 승인/버전 계약을 구현하지 않은 경우
     */
    List<CpfDataRow> findGhostCandidates(int heartbeatTimeoutSeconds);
    /**
     * actGhostExecution 운영 조작을 수행합니다. 구현체는 인증 주체, 승인, 멱등성, 동시성, 감사와 UNKNOWN/복구 의미를 보존해야 합니다.
     *
     * <p>고위험 운영 Consumer는 가능한 경우 {@code CpfBatchRiskCommand} 기반 overload를 사용하고,
     * 버전 불일치·승인 불일치·결과 불확실성을 fail-closed로 처리해야 합니다.</p>
     * @param executionId 배치 실행 식별자입니다.
     * @param actionType ghost 실행에 수행할 복구 조작 유형입니다.
     * @param requestUser 인증된 운영자 식별자입니다. 외부 입력을 그대로 신뢰하면 안 됩니다.
     * @param reason 운영 조작 사유입니다. 감사 로그에 남기되 민감정보를 포함하면 안 됩니다.
     * @return 조작 결과입니다. 실패·충돌·UNKNOWN 상태를 성공으로 축약해서는 안 됩니다.
     * @throws UnsupportedOperationException Provider가 필수 승인/버전 계약을 구현하지 않은 경우
     */
    CpfDataRow actGhostExecution(long executionId, String actionType, String requestUser, String reason);

        /**
     * actGhostExecution 운영 조작을 수행합니다. 구현체는 인증 주체, 승인, 멱등성, 동시성, 감사와 UNKNOWN/복구 의미를 보존해야 합니다.
     *
     * <p>고위험 운영 Consumer는 가능한 경우 {@code CpfBatchRiskCommand} 기반 overload를 사용하고,
     * 버전 불일치·승인 불일치·결과 불확실성을 fail-closed로 처리해야 합니다.</p>
     * @param executionId 배치 실행 식별자입니다.
     * @param actionType ghost 실행에 수행할 복구 조작 유형입니다.
     * @param requestUser 인증된 운영자 식별자입니다. 외부 입력을 그대로 신뢰하면 안 됩니다.
     * @param reason 운영 조작 사유입니다. 감사 로그에 남기되 민감정보를 포함하면 안 됩니다.
     * @param expectedVersion 낙관적 동시성 제어용 기대 버전입니다. 불일치 시 조작은 실패해야 합니다.
     * @return 조작 결과입니다. 실패·충돌·UNKNOWN 상태를 성공으로 축약해서는 안 됩니다.
     * @throws UnsupportedOperationException Provider가 필수 승인/버전 계약을 구현하지 않은 경우
     */
    default CpfDataRow actGhostExecution(
            long executionId, String actionType, String requestUser, String reason, long expectedVersion) {
        throw expectedVersionUnsupported("actGhostExecution");
    }

    /**
     * actGhostExecution 운영 조작을 수행합니다. 구현체는 인증 주체, 승인, 멱등성, 동시성, 감사와 UNKNOWN/복구 의미를 보존해야 합니다.
     *
     * <p>고위험 운영 Consumer는 가능한 경우 {@code CpfBatchRiskCommand} 기반 overload를 사용하고,
     * 버전 불일치·승인 불일치·결과 불확실성을 fail-closed로 처리해야 합니다.</p>
     * @param executionId 배치 실행 식별자입니다.
     * @param actionType ghost 실행에 수행할 복구 조작 유형입니다.
     * @param command 승인·멱등·감사·expectedVersion을 결합한 위험조치 명령입니다.
     * @return 조작 결과입니다. 실패·충돌·UNKNOWN 상태를 성공으로 축약해서는 안 됩니다.
     * @throws UnsupportedOperationException Provider가 필수 승인/버전 계약을 구현하지 않은 경우
     */
    default CpfDataRow actGhostExecution(long executionId, String actionType, CpfBatchRiskCommand command) {
        throw riskCommandUnsupported("actGhostExecution");
    }
    /**
     * findOperationLogs 공개 조회/판정 계약을 수행합니다. 조회·판정 자체는 업무 상태를 변경하지 않으며 실패나 UNKNOWN을 정상 결과로 오인하지 않습니다.
     * @param jobId 배치 Job 식별자입니다. 조회 필터에서는 null/blank가 전체 범위를 뜻할 수 있습니다.
     * @param executionId 배치 실행 식별자입니다.
     * @param limit 최대 조회 건수입니다. 구현체는 문서화된 상한으로 제한해야 합니다.
     * @return Owner 경계에서 조회한 결과입니다. 조회 불가/부분 실패는 구현 계약에 따라 명시적으로 표현해야 합니다.
     */
    List<CpfDataRow> findOperationLogs(String jobId, Long executionId, int limit);
    /**
     * simulateSchedule 공개 조회/판정 계약을 수행합니다. 조회·판정 자체는 업무 상태를 변경하지 않으며 실패나 UNKNOWN을 정상 결과로 오인하지 않습니다.
     * @param scheduleId 배치 스케줄 식별자입니다.
     * @param baseDate baseDate 입력값입니다. null 허용 여부는 메서드 설명을 따릅니다.
     * @param days days 입력값입니다. null 허용 여부는 메서드 설명을 따릅니다.
     * @return 계약에 정의된 결과입니다.
     */
    List<CpfDataRow> simulateSchedule(String scheduleId, String baseDate, int days);
    /**
     * registerJob 운영 조작을 수행합니다. 구현체는 인증 주체, 승인, 멱등성, 동시성, 감사와 UNKNOWN/복구 의미를 보존해야 합니다.
     *
     * <p>고위험 운영 Consumer는 가능한 경우 {@code CpfBatchRiskCommand} 기반 overload를 사용하고,
     * 버전 불일치·승인 불일치·결과 불확실성을 fail-closed로 처리해야 합니다.</p>
     * @param jobId 배치 Job 식별자입니다. 조회 필터에서는 null/blank가 전체 범위를 뜻할 수 있습니다.
     * @param jobName jobName 입력값입니다. null 허용 여부는 메서드 설명을 따릅니다.
     * @param jobType jobType 입력값입니다. null 허용 여부는 메서드 설명을 따릅니다.
     * @param description description 입력값입니다. null 허용 여부는 메서드 설명을 따릅니다.
     * @param requestUser 인증된 운영자 식별자입니다. 외부 입력을 그대로 신뢰하면 안 됩니다.
     * @return 조작 결과입니다. 실패·충돌·UNKNOWN 상태를 성공으로 축약해서는 안 됩니다.
     */
    CpfDataRow registerJob(String jobId, String jobName, String jobType, String description, String requestUser);
    /**
     * requestRun 운영 조작을 수행합니다. 구현체는 인증 주체, 승인, 멱등성, 동시성, 감사와 UNKNOWN/복구 의미를 보존해야 합니다.
     *
     * <p>고위험 운영 Consumer는 가능한 경우 {@code CpfBatchRiskCommand} 기반 overload를 사용하고,
     * 버전 불일치·승인 불일치·결과 불확실성을 fail-closed로 처리해야 합니다.</p>
     * @param jobId 배치 Job 식별자입니다. 조회 필터에서는 null/blank가 전체 범위를 뜻할 수 있습니다.
     * @param jobParameters 배치 실행 파라미터입니다. 민감정보를 포함하면 구현체가 마스킹해야 합니다.
     * @param requestUser 인증된 운영자 식별자입니다. 외부 입력을 그대로 신뢰하면 안 됩니다.
     * @param reason 운영 조작 사유입니다. 감사 로그에 남기되 민감정보를 포함하면 안 됩니다.
     * @return 조작 결과입니다. 실패·충돌·UNKNOWN 상태를 성공으로 축약해서는 안 됩니다.
     * @throws UnsupportedOperationException Provider가 필수 승인/버전 계약을 구현하지 않은 경우
     */
    CpfDataRow requestRun(String jobId, String jobParameters, String requestUser, String reason);
    /**
     * requestScheduledRun 운영 조작을 수행합니다. 구현체는 인증 주체, 승인, 멱등성, 동시성, 감사와 UNKNOWN/복구 의미를 보존해야 합니다.
     *
     * <p>고위험 운영 Consumer는 가능한 경우 {@code CpfBatchRiskCommand} 기반 overload를 사용하고,
     * 버전 불일치·승인 불일치·결과 불확실성을 fail-closed로 처리해야 합니다.</p>
     * @param scheduleId 배치 스케줄 식별자입니다.
     * @param jobId 배치 Job 식별자입니다. 조회 필터에서는 null/blank가 전체 범위를 뜻할 수 있습니다.
     * @param jobParameters 배치 실행 파라미터입니다. 민감정보를 포함하면 구현체가 마스킹해야 합니다.
     * @param requestUser 인증된 운영자 식별자입니다. 외부 입력을 그대로 신뢰하면 안 됩니다.
     * @param reason 운영 조작 사유입니다. 감사 로그에 남기되 민감정보를 포함하면 안 됩니다.
     * @return 조작 결과입니다. 실패·충돌·UNKNOWN 상태를 성공으로 축약해서는 안 됩니다.
     * @throws UnsupportedOperationException Provider가 필수 승인/버전 계약을 구현하지 않은 경우
     */
    CpfDataRow requestScheduledRun(String scheduleId, String jobId, String jobParameters, String requestUser, String reason);
    /**
     * requestRetry 운영 조작을 수행합니다. 구현체는 인증 주체, 승인, 멱등성, 동시성, 감사와 UNKNOWN/복구 의미를 보존해야 합니다.
     *
     * <p>고위험 운영 Consumer는 가능한 경우 {@code CpfBatchRiskCommand} 기반 overload를 사용하고,
     * 버전 불일치·승인 불일치·결과 불확실성을 fail-closed로 처리해야 합니다.</p>
     * @param executionId 배치 실행 식별자입니다.
     * @param requestUser 인증된 운영자 식별자입니다. 외부 입력을 그대로 신뢰하면 안 됩니다.
     * @param reason 운영 조작 사유입니다. 감사 로그에 남기되 민감정보를 포함하면 안 됩니다.
     * @return 조작 결과입니다. 실패·충돌·UNKNOWN 상태를 성공으로 축약해서는 안 됩니다.
     * @throws UnsupportedOperationException Provider가 필수 승인/버전 계약을 구현하지 않은 경우
     */
    CpfDataRow requestRetry(long executionId, String requestUser, String reason);

        /**
     * requestRetry 운영 조작을 수행합니다. 구현체는 인증 주체, 승인, 멱등성, 동시성, 감사와 UNKNOWN/복구 의미를 보존해야 합니다.
     *
     * <p>고위험 운영 Consumer는 가능한 경우 {@code CpfBatchRiskCommand} 기반 overload를 사용하고,
     * 버전 불일치·승인 불일치·결과 불확실성을 fail-closed로 처리해야 합니다.</p>
     * @param executionId 배치 실행 식별자입니다.
     * @param requestUser 인증된 운영자 식별자입니다. 외부 입력을 그대로 신뢰하면 안 됩니다.
     * @param reason 운영 조작 사유입니다. 감사 로그에 남기되 민감정보를 포함하면 안 됩니다.
     * @param expectedVersion 낙관적 동시성 제어용 기대 버전입니다. 불일치 시 조작은 실패해야 합니다.
     * @return 조작 결과입니다. 실패·충돌·UNKNOWN 상태를 성공으로 축약해서는 안 됩니다.
     * @throws UnsupportedOperationException Provider가 필수 승인/버전 계약을 구현하지 않은 경우
     */
    default CpfDataRow requestRetry(
            long executionId, String requestUser, String reason, long expectedVersion) {
        throw expectedVersionUnsupported("requestRetry");
    }

    /**
     * requestRetry 운영 조작을 수행합니다. 구현체는 인증 주체, 승인, 멱등성, 동시성, 감사와 UNKNOWN/복구 의미를 보존해야 합니다.
     *
     * <p>고위험 운영 Consumer는 가능한 경우 {@code CpfBatchRiskCommand} 기반 overload를 사용하고,
     * 버전 불일치·승인 불일치·결과 불확실성을 fail-closed로 처리해야 합니다.</p>
     * @param executionId 배치 실행 식별자입니다.
     * @param command 승인·멱등·감사·expectedVersion을 결합한 위험조치 명령입니다.
     * @return 조작 결과입니다. 실패·충돌·UNKNOWN 상태를 성공으로 축약해서는 안 됩니다.
     * @throws UnsupportedOperationException Provider가 필수 승인/버전 계약을 구현하지 않은 경우
     */
    default CpfDataRow requestRetry(long executionId, CpfBatchRiskCommand command) {
        throw riskCommandUnsupported("requestRetry");
    }

    /**
     * requestStop 운영 조작을 수행합니다. 구현체는 인증 주체, 승인, 멱등성, 동시성, 감사와 UNKNOWN/복구 의미를 보존해야 합니다.
     *
     * <p>고위험 운영 Consumer는 가능한 경우 {@code CpfBatchRiskCommand} 기반 overload를 사용하고,
     * 버전 불일치·승인 불일치·결과 불확실성을 fail-closed로 처리해야 합니다.</p>
     * @param executionId 배치 실행 식별자입니다.
     * @param requestUser 인증된 운영자 식별자입니다. 외부 입력을 그대로 신뢰하면 안 됩니다.
     * @param reason 운영 조작 사유입니다. 감사 로그에 남기되 민감정보를 포함하면 안 됩니다.
     * @return 조작 결과입니다. 실패·충돌·UNKNOWN 상태를 성공으로 축약해서는 안 됩니다.
     * @throws UnsupportedOperationException Provider가 필수 승인/버전 계약을 구현하지 않은 경우
     */
    CpfDataRow requestStop(long executionId, String requestUser, String reason);

        /**
     * requestStop 운영 조작을 수행합니다. 구현체는 인증 주체, 승인, 멱등성, 동시성, 감사와 UNKNOWN/복구 의미를 보존해야 합니다.
     *
     * <p>고위험 운영 Consumer는 가능한 경우 {@code CpfBatchRiskCommand} 기반 overload를 사용하고,
     * 버전 불일치·승인 불일치·결과 불확실성을 fail-closed로 처리해야 합니다.</p>
     * @param executionId 배치 실행 식별자입니다.
     * @param requestUser 인증된 운영자 식별자입니다. 외부 입력을 그대로 신뢰하면 안 됩니다.
     * @param reason 운영 조작 사유입니다. 감사 로그에 남기되 민감정보를 포함하면 안 됩니다.
     * @param expectedVersion 낙관적 동시성 제어용 기대 버전입니다. 불일치 시 조작은 실패해야 합니다.
     * @return 조작 결과입니다. 실패·충돌·UNKNOWN 상태를 성공으로 축약해서는 안 됩니다.
     * @throws UnsupportedOperationException Provider가 필수 승인/버전 계약을 구현하지 않은 경우
     */
    default CpfDataRow requestStop(
            long executionId, String requestUser, String reason, long expectedVersion) {
        throw expectedVersionUnsupported("requestStop");
    }

    /**
     * requestStop 운영 조작을 수행합니다. 구현체는 인증 주체, 승인, 멱등성, 동시성, 감사와 UNKNOWN/복구 의미를 보존해야 합니다.
     *
     * <p>고위험 운영 Consumer는 가능한 경우 {@code CpfBatchRiskCommand} 기반 overload를 사용하고,
     * 버전 불일치·승인 불일치·결과 불확실성을 fail-closed로 처리해야 합니다.</p>
     * @param executionId 배치 실행 식별자입니다.
     * @param command 승인·멱등·감사·expectedVersion을 결합한 위험조치 명령입니다.
     * @return 조작 결과입니다. 실패·충돌·UNKNOWN 상태를 성공으로 축약해서는 안 됩니다.
     * @throws UnsupportedOperationException Provider가 필수 승인/버전 계약을 구현하지 않은 경우
     */
    default CpfDataRow requestStop(long executionId, CpfBatchRiskCommand command) {
        throw riskCommandUnsupported("requestStop");
    }

    /**
     * updateScheduleEnabled 운영 조작을 수행합니다. 구현체는 인증 주체, 승인, 멱등성, 동시성, 감사와 UNKNOWN/복구 의미를 보존해야 합니다.
     *
     * <p>고위험 운영 Consumer는 가능한 경우 {@code CpfBatchRiskCommand} 기반 overload를 사용하고,
     * 버전 불일치·승인 불일치·결과 불확실성을 fail-closed로 처리해야 합니다.</p>
     * @param scheduleId 배치 스케줄 식별자입니다.
     * @param enabled 스케줄 활성화 여부입니다.
     * @param requestUser 인증된 운영자 식별자입니다. 외부 입력을 그대로 신뢰하면 안 됩니다.
     * @param reason 운영 조작 사유입니다. 감사 로그에 남기되 민감정보를 포함하면 안 됩니다.
     * @return 조작 결과입니다. 실패·충돌·UNKNOWN 상태를 성공으로 축약해서는 안 됩니다.
     * @throws UnsupportedOperationException Provider가 필수 승인/버전 계약을 구현하지 않은 경우
     */
    CpfDataRow updateScheduleEnabled(String scheduleId, boolean enabled, String requestUser, String reason);

        /**
     * updateScheduleEnabled 운영 조작을 수행합니다. 구현체는 인증 주체, 승인, 멱등성, 동시성, 감사와 UNKNOWN/복구 의미를 보존해야 합니다.
     *
     * <p>고위험 운영 Consumer는 가능한 경우 {@code CpfBatchRiskCommand} 기반 overload를 사용하고,
     * 버전 불일치·승인 불일치·결과 불확실성을 fail-closed로 처리해야 합니다.</p>
     * @param scheduleId 배치 스케줄 식별자입니다.
     * @param enabled 스케줄 활성화 여부입니다.
     * @param requestUser 인증된 운영자 식별자입니다. 외부 입력을 그대로 신뢰하면 안 됩니다.
     * @param reason 운영 조작 사유입니다. 감사 로그에 남기되 민감정보를 포함하면 안 됩니다.
     * @param expectedVersion 낙관적 동시성 제어용 기대 버전입니다. 불일치 시 조작은 실패해야 합니다.
     * @return 조작 결과입니다. 실패·충돌·UNKNOWN 상태를 성공으로 축약해서는 안 됩니다.
     * @throws UnsupportedOperationException Provider가 필수 승인/버전 계약을 구현하지 않은 경우
     */
    default CpfDataRow updateScheduleEnabled(
            String scheduleId, boolean enabled, String requestUser, String reason, long expectedVersion) {
        throw expectedVersionUnsupported("updateScheduleEnabled");
    }

    /**
     * updateScheduleEnabled 운영 조작을 수행합니다. 구현체는 인증 주체, 승인, 멱등성, 동시성, 감사와 UNKNOWN/복구 의미를 보존해야 합니다.
     *
     * <p>고위험 운영 Consumer는 가능한 경우 {@code CpfBatchRiskCommand} 기반 overload를 사용하고,
     * 버전 불일치·승인 불일치·결과 불확실성을 fail-closed로 처리해야 합니다.</p>
     * @param scheduleId 배치 스케줄 식별자입니다.
     * @param enabled 스케줄 활성화 여부입니다.
     * @param command 승인·멱등·감사·expectedVersion을 결합한 위험조치 명령입니다.
     * @return 조작 결과입니다. 실패·충돌·UNKNOWN 상태를 성공으로 축약해서는 안 됩니다.
     * @throws UnsupportedOperationException Provider가 필수 승인/버전 계약을 구현하지 않은 경우
     */
    default CpfDataRow updateScheduleEnabled(
            String scheduleId, boolean enabled, CpfBatchRiskCommand command) {
        throw riskCommandUnsupported("updateScheduleEnabled");
    }

    /**
     * runSchedulerOnce 운영 조작을 수행합니다. 구현체는 인증 주체, 승인, 멱등성, 동시성, 감사와 UNKNOWN/복구 의미를 보존해야 합니다.
     *
     * <p>고위험 운영 Consumer는 가능한 경우 {@code CpfBatchRiskCommand} 기반 overload를 사용하고,
     * 버전 불일치·승인 불일치·결과 불확실성을 fail-closed로 처리해야 합니다.</p>
     * @param requestUser 인증된 운영자 식별자입니다. 외부 입력을 그대로 신뢰하면 안 됩니다.
     * @return 조작 결과입니다. 실패·충돌·UNKNOWN 상태를 성공으로 축약해서는 안 됩니다.
     * @throws UnsupportedOperationException Provider가 필수 승인/버전 계약을 구현하지 않은 경우
     */
    List<CpfDataRow> runSchedulerOnce(String requestUser);

        /**
     * requestRun 운영 조작을 수행합니다. 구현체는 인증 주체, 승인, 멱등성, 동시성, 감사와 UNKNOWN/복구 의미를 보존해야 합니다.
     *
     * <p>고위험 운영 Consumer는 가능한 경우 {@code CpfBatchRiskCommand} 기반 overload를 사용하고,
     * 버전 불일치·승인 불일치·결과 불확실성을 fail-closed로 처리해야 합니다.</p>
     * @param jobId 배치 Job 식별자입니다. 조회 필터에서는 null/blank가 전체 범위를 뜻할 수 있습니다.
     * @param jobParameters 배치 실행 파라미터입니다. 민감정보를 포함하면 구현체가 마스킹해야 합니다.
     * @param command 승인·멱등·감사·expectedVersion을 결합한 위험조치 명령입니다.
     * @return 조작 결과입니다. 실패·충돌·UNKNOWN 상태를 성공으로 축약해서는 안 됩니다.
     * @throws UnsupportedOperationException Provider가 필수 승인/버전 계약을 구현하지 않은 경우
     */
    default CpfDataRow requestRun(String jobId, String jobParameters, CpfBatchRiskCommand command) {
        throw riskCommandUnsupported("requestRun");
    }

    /**
     * runSchedulerOnce 운영 조작을 수행합니다. 구현체는 인증 주체, 승인, 멱등성, 동시성, 감사와 UNKNOWN/복구 의미를 보존해야 합니다.
     *
     * <p>고위험 운영 Consumer는 가능한 경우 {@code CpfBatchRiskCommand} 기반 overload를 사용하고,
     * 버전 불일치·승인 불일치·결과 불확실성을 fail-closed로 처리해야 합니다.</p>
     * @param command 승인·멱등·감사·expectedVersion을 결합한 위험조치 명령입니다.
     * @return 조작 결과입니다. 실패·충돌·UNKNOWN 상태를 성공으로 축약해서는 안 됩니다.
     * @throws UnsupportedOperationException Provider가 필수 승인/버전 계약을 구현하지 않은 경우
     */
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
