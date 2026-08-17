package com.cpf.admin.opr.service;

import com.cpf.batch.api.CpfBatchOperationsPort;
import com.cpf.data.api.CpfDataRow;
import com.cpf.platform.operations.observability.api.logging.CpfTransactionTimelineQueryPort;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import com.cpf.foundation.annotation.CpfService;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * transactionId 기준으로 CPF 표준 거래 구간과 외부 호출 후보를 조합합니다.
 *
 * <p>ADM은 다른 주제영역 DB를 직접 조회하지 않고 CPF 공개 조회 포트만 사용합니다.
 * 외부 연계 모듈이 추가되더라도 표준 구간 로그에 기록하면 ADM 구현 변경 없이 함께 조회됩니다.</p>
 */
@CpfService
public class AdmTransactionGroupService extends com.cpf.admin.common.base.AdmBaseService {
    private final CpfTransactionTimelineQueryPort timelineQueryPort;
    private final CpfBatchOperationsPort batchOperations;

    @Autowired
    public AdmTransactionGroupService(
            CpfTransactionTimelineQueryPort timelineQueryPort,
            ObjectProvider<CpfBatchOperationsPort> batchOperationsProvider) {
        this.timelineQueryPort = timelineQueryPort;
        this.batchOperations = batchOperationsProvider == null ? null : batchOperationsProvider.getIfAvailable();
    }

    /** Test/single-module compatibility constructor. */
    public AdmTransactionGroupService(CpfTransactionTimelineQueryPort timelineQueryPort) {
        this.timelineQueryPort = timelineQueryPort;
        this.batchOperations = null;
    }

    public Map<String, Object> findGroups(Map<String, String> criteria) {
        CpfTransactionTimelineQueryPort.GroupQueryResult query = timelineQueryPort.findGroups(criteria);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("available", query.available());
        response.put("items", query.items());
        response.put("limit", query.limit());
        response.put("sort", query.sort());
        response.put("criteria", criteria == null ? Map.of() : criteria);
        if (query.message() != null) {
            response.put("message", query.message());
        }
        return response;
    }

    public Map<String, Object> findDetail(String transactionId) {
        List<Map<String, Object>> segments = findSegments(transactionId);
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("transactionId", transactionId);
        detail.put("segments", segments);
        detail.put("timeline", timelineFromSegments(segments));
        detail.put("summary", summarize(transactionId, segments));
        detail.put("headers", headerSnapshots(segments));
        detail.put("externalLogs", findExternalLogs(transactionId, 100));
        BatchLineageResult batchLineageResult = findBatchLineage(transactionId, 200);
        List<Map<String, Object>> batchLineage = batchLineageResult.rows();
        List<Map<String, Object>> lineage = new ArrayList<>(timelineQueryPort.findLineage(transactionId, 500));
        lineage.addAll(batchLineage);
        lineage.sort((left, right) -> text(left, "occurredAt").compareTo(text(right, "occurredAt")));
        detail.put("lineage", List.copyOf(lineage));
        detail.put("tree", lineageTree(lineage));
        detail.put("sourceFreshness", mergeBatchFreshness(timelineQueryPort.sourceFreshness(transactionId), batchLineageResult));
        return detail;
    }

    public List<Map<String, Object>> findSegments(String transactionId) {
        if (!hasText(transactionId)) {
            return List.of();
        }
        return timelineQueryPort.findSegments(transactionId.trim());
    }

    public List<Map<String, Object>> findTimeline(String transactionId) {
        return timelineFromSegments(findSegments(transactionId));
    }

    public Map<String, Object> findHeaders(String transactionId) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("transactionId", transactionId);
        response.put("headers", headerSnapshots(findSegments(transactionId)));
        return response;
    }

    public Map<String, Object> findExternalLogs(String transactionId) {
        List<Map<String, Object>> items = findExternalLogs(transactionId, 100);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("transactionId", transactionId);
        response.put("items", items);
        response.put("source", "CPF_TRANSACTION_SEGMENT");
        response.put("fallbackUsed", false);
        return response;
    }

    private List<Map<String, Object>> findExternalLogs(String transactionId, int limit) {
        if (!hasText(transactionId)) {
            return List.of();
        }
        return timelineQueryPort.findExternalCandidates(transactionId, boundedLimit(limit));
    }

    private Map<String, Object> summarize(String transactionId, List<Map<String, Object>> segments) {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("transactionId", transactionId);
        summary.put("segmentCount", segments.size());
        summary.put("moduleFlowText", moduleFlowText(segments));
        summary.put("overallStatus", segments.stream().anyMatch(row -> "Y".equals(text(row, "failureYn")))
                ? "FAILED"
                : "SUCCESS");
        summary.put("totalDurationMs", segments.stream()
                .map(row -> row.get("durationMs"))
                .filter(Number.class::isInstance)
                .map(Number.class::cast)
                .mapToLong(Number::longValue)
                .sum());
        segments.stream()
                .filter(row -> "Y".equals(text(row, "failureYn")))
                .findFirst()
                .ifPresent(row -> {
                    summary.put("failedModuleCode", row.get("moduleCode"));
                    summary.put("failedSegmentId", row.get("transactionSegmentId"));
                    summary.put("failureCode", row.get("failureCode"));
                    summary.put("failureMessageMasked", row.get("failureMessageMasked"));
                });
        return summary;
    }

    private List<Map<String, Object>> timelineFromSegments(List<Map<String, Object>> segments) {
        List<Map<String, Object>> timeline = new ArrayList<>();
        for (Map<String, Object> segment : segments) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("transactionSegmentId", segment.get("transactionSegmentId"));
            item.put("executionId", segment.get("executionId"));
            item.put("parentSegmentId", segment.get("parentSegmentId"));
            item.put("sequenceNo", segment.get("sequenceNo"));
            item.put("callDepth", segment.get("callDepth"));
            item.put("moduleCode", segment.get("moduleCode"));
            item.put("sourceModuleCode", segment.get("sourceModuleCode"));
            item.put("targetModuleCode", segment.get("targetModuleCode"));
            item.put("transactionRole", segment.get("transactionRole"));
            item.put("direction", segment.get("direction"));
            item.put("status", segment.get("status"));
            item.put("selectedInstanceId", segment.get("selectedInstanceId"));
            item.put("attemptNo", segment.get("attemptNo"));
            item.put("retryYn", segment.get("retryYn"));
            item.put("failoverYn", segment.get("failoverYn"));
            item.put("circuitState", segment.get("circuitState"));
            item.put("downstreamHttpStatus", segment.get("downstreamHttpStatus"));
            item.put("resultState", segment.get("resultState"));
            item.put("unknownResultId", segment.get("unknownResultId"));
            item.put("startedAt", segment.get("startedAt"));
            item.put("endedAt", segment.get("endedAt"));
            item.put("durationMs", segment.get("durationMs"));
            item.put("label", label(segment));
            timeline.add(item);
        }
        return timeline;
    }

    private List<Map<String, Object>> headerSnapshots(List<Map<String, Object>> segments) {
        return segments.stream().map(segment -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("transactionSegmentId", segment.get("transactionSegmentId"));
            item.put("moduleCode", segment.get("moduleCode"));
            item.put("requestHeaders", segment.get("requestHeaderSnapshotMasked"));
            item.put("responseHeaders", segment.get("responseHeaderSnapshotMasked"));
            item.put("extensionHeaders", segment.get("extensionHeaderSnapshotMasked"));
            return item;
        }).toList();
    }

    private String moduleFlowText(List<Map<String, Object>> segments) {
        StringJoiner joiner = new StringJoiner(" -> ");
        String previous = null;
        for (Map<String, Object> segment : segments) {
            String moduleCode = text(segment, "moduleCode");
            if (!moduleCode.equals(previous)) {
                joiner.add(moduleCode);
                previous = moduleCode;
            }
        }
        return joiner.toString();
    }

    private String label(Map<String, Object> row) {
        return text(row, "moduleCode") + " " + text(row, "transactionRole") + " "
                + text(row, "direction") + " / " + text(row, "durationMs") + "ms";
    }

    private String text(Map<String, Object> row, String key) {
        Object value = row.get(key);
        return value == null ? "" : String.valueOf(value);
    }

    private int boundedLimit(int limit) {
        return Math.max(1, Math.min(500, limit));
    }

    private List<Map<String, Object>> lineageTree(List<Map<String, Object>> lineage) {
        Map<String, Map<String, Object>> nodes = new LinkedHashMap<>();
        List<Map<String, Object>> roots = new ArrayList<>();
        for (Map<String, Object> row : lineage) {
            Map<String, Object> node = new LinkedHashMap<>(row);
            node.put("children", new ArrayList<Map<String, Object>>());
            String segmentId = text(row, "segmentId");
            if (!segmentId.isBlank()) nodes.put(segmentId, node);
        }
        for (Map<String, Object> node : nodes.values()) {
            String parent = text(node, "parentSegmentId");
            Map<String, Object> parentNode = nodes.get(parent);
            if (parentNode == null) {
                roots.add(node);
            } else {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> children = (List<Map<String, Object>>) parentNode.get("children");
                children.add(node);
            }
        }
        return roots;
    }


    private BatchLineageResult findBatchLineage(String transactionId, int limit) {
        if (!hasText(transactionId) || batchOperations == null) {
            return BatchLineageResult.notApplicable();
        }
        try {
            List<CpfDataRow> executions = batchOperations.findExecutions(
                    null, transactionId.trim(), null, null, null, boundedLimit(limit));
            if (executions == null || executions.isEmpty()) {
                return BatchLineageResult.success(List.of());
            }
            List<Map<String, Object>> rows = new ArrayList<>();
            for (CpfDataRow execution : executions) {
                Map<String, Object> row = new LinkedHashMap<>();
                Object executionId = firstValue(execution, "executionId", "execution_id", "jobExecutionId", "job_execution_id");
                Object jobInstanceId = firstValue(execution, "springBatchJobInstanceId", "spring_batch_job_instance_id", "jobInstanceId", "job_instance_id");
                Object jobId = firstValue(execution, "jobId", "job_id", "jobName", "job_name");
                Object workerId = firstValue(execution, "workerId", "worker_id");
                Object instanceId = firstValue(execution, "instanceId", "instance_id", "instanceId", "instance_id");
                Object startedAt = firstValue(execution, "startedAt", "startTime", "start_time", "createdAt", "created_at");
                Object endedAt = firstValue(execution, "endedAt", "endTime", "end_time", "updatedAt", "updated_at");
                Object status = firstValue(execution, "status", "batchStatus", "batch_status", "executionStatus", "execution_status");
                Object failure = firstValue(execution, "failureMessageMasked", "exitMessageMasked", "errorMessageMasked", "failureCode");
                row.put("transactionId", transactionId.trim());
                row.put("segmentId", executionId == null ? "BATCH:" + String.valueOf(jobId) : "BATCH:" + executionId);
                row.put("parentSegmentId", firstValue(execution, "parentSegmentId", "parent_segment_id"));
                row.put("attempt", firstValueOrDefault(execution, 1, "attempt", "attemptNo", "attempt_no"));
                row.put("traceId", firstValue(execution, "traceId", "trace_id"));
                row.put("spanId", firstValue(execution, "spanId", "span_id"));
                row.put("requestId", firstValue(execution, "requestId", "request_id", "commandRequestId", "command_request_id"));
                row.put("idempotencyKey", firstValue(execution, "idempotencyKey", "idempotency_key"));
                row.put("tenantId", firstValue(execution, "tenantId", "tenant_id"));
                row.put("channel", "BATCH");
                row.put("actorIdMasked", firstValue(execution, "requestUserMasked", "request_user_masked", "actorIdMasked"));
                row.put("instanceId", instanceId);
                row.put("wasId", firstValue(execution, "wasId", "was_id"));
                row.put("agentId", firstValue(execution, "agentId", "agent_id"));
                row.put("workerId", workerId);
                row.put("remoteSystem", "cpf-batch");
                row.put("operation", jobId);
                row.put("messageId", null);
                row.put("consumerGroup", null);
                row.put("dlqId", null);
                row.put("batchJobInstanceId", jobInstanceId);
                row.put("batchJobExecutionId", executionId);
                row.put("batchStepExecutionId", firstValue(execution, "stepExecutionId", "step_execution_id"));
                row.put("partitionId", firstValue(execution, "partitionId", "partition_id"));
                row.put("fileId", null);
                row.put("sourceType", "BATCH");
                row.put("sourceRefId", executionId);
                row.put("lifecycleState", status);
                row.put("failureStage", failure);
                row.put("unknownYn", "UNKNOWN".equalsIgnoreCase(String.valueOf(status)) ? "Y" : "N");
                row.put("reconcileState", firstValue(execution, "reconcileState", "reconcile_state"));
                row.put("occurredAt", startedAt);
                row.put("freshnessAt", endedAt == null ? startedAt : endedAt);
                rows.add(row);
            }
            return BatchLineageResult.success(List.copyOf(rows));
        } catch (RuntimeException queryFailure) {
            // Applicable BATCH source query failures must remain operator-visible.
            // Never collapse an unavailable owner into a normal empty/not-applicable result.
            return BatchLineageResult.queryFailed(queryFailure.getClass().getSimpleName());
        }
    }

    private Map<String, Object> mergeBatchFreshness(Map<String, Object> base, BatchLineageResult batchResult) {
        Map<String, Object> result = new LinkedHashMap<>(base == null ? Map.of() : base);
        List<Map<String, Object>> sources = new ArrayList<>();
        Object existingSources = result.get("sources");
        if (existingSources instanceof List<?> list) {
            for (Object item : list) {
                if (item instanceof Map<?, ?> map) {
                    Map<String, Object> copy = new LinkedHashMap<>();
                    map.forEach((key, value) -> copy.put(String.valueOf(key), value));
                    sources.add(copy);
                }
            }
        }

        if (!batchResult.applicable()) {
            result.put("sources", List.copyOf(sources));
            return result;
        }

        sources.removeIf(source -> "BATCH".equals(String.valueOf(source.get("sourceType"))));
        List<String> missing = stringList(result.get("missingSources"));
        List<String> failed = stringList(result.get("failedSources"));
        missing.removeIf("BATCH"::equals);
        failed.removeIf("BATCH"::equals);

        if (batchResult.queryFailed()) {
            Map<String, Object> batch = new LinkedHashMap<>();
            batch.put("sourceType", "BATCH");
            batch.put("state", "QUERY_FAILED");
            batch.put("availability", "UNAVAILABLE");
            batch.put("reason", "BATCH_QUERY_FAILED");
            batch.put("failureType", batchResult.failureType());
            batch.put("eventCount", 0);
            batch.put("freshnessAt", null);
            sources.add(batch);
            missing.add("BATCH");
            failed.add("BATCH");
            result.put("partial", true);
            result.put("available", false);
            result.put("resultState", "PARTIAL");
        } else {
            Object latest = null;
            for (Map<String, Object> row : batchResult.rows()) {
                Object candidate = row.get("freshnessAt");
                if (candidate != null && (latest == null || String.valueOf(candidate).compareTo(String.valueOf(latest)) > 0)) {
                    latest = candidate;
                }
            }
            Map<String, Object> batch = new LinkedHashMap<>();
            batch.put("sourceType", "BATCH");
            batch.put("state", "AVAILABLE");
            batch.put("eventCount", batchResult.rows().size());
            batch.put("freshnessAt", latest);
            sources.add(batch);
        }
        result.put("missingSources", List.copyOf(missing));
        result.put("failedSources", List.copyOf(failed));
        result.put("partial", Boolean.TRUE.equals(result.get("partial")) || !missing.isEmpty() || !failed.isEmpty());
        result.put("sources", List.copyOf(sources));
        return result;
    }

    private List<String> stringList(Object value) {
        List<String> result = new ArrayList<>();
        if (value instanceof List<?> list) {
            for (Object item : list) {
                if (item != null && !String.valueOf(item).isBlank()) result.add(String.valueOf(item));
            }
        }
        return result;
    }

    private record BatchLineageResult(
            List<Map<String, Object>> rows,
            boolean applicable,
            boolean queryFailed,
            String failureType) {
        private static BatchLineageResult notApplicable() {
            return new BatchLineageResult(List.of(), false, false, null);
        }

        private static BatchLineageResult success(List<Map<String, Object>> rows) {
            return new BatchLineageResult(rows == null ? List.of() : List.copyOf(rows), true, false, null);
        }

        private static BatchLineageResult queryFailed(String failureType) {
            return new BatchLineageResult(List.of(), true, true, failureType == null ? "RuntimeException" : failureType);
        }
    }

    private Object firstValue(Map<String, Object> row, String... keys) {
        if (row == null || keys == null) return null;
        for (String key : keys) {
            Object value = row.get(key);
            if (value != null && !String.valueOf(value).isBlank()) return value;
        }
        return null;
    }

    private Object firstValueOrDefault(Map<String, Object> row, Object fallback, String... keys) {
        Object value = firstValue(row, keys);
        return value == null ? fallback : value;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
