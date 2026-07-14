package cpf.adm.opr.service;

import cpf.pfw.api.logging.CpfTransactionTimelineQueryPort;
import cpf.pfw.common.http.CpfWebClient;
import cpf.pfw.common.logging.SensitiveDataMasker;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * transactionGlobalId 기준으로 PFW 거래 tree와 EXS 원장을 공개 port/facade로 조합합니다.
 *
 * <p>ADM은 PFW 또는 EXS DB를 직접 조회하지 않습니다. PFW 데이터는 query port로,
 * EXS 원장은 표준 서비스 호출 엔진을 사용하는 Remote Facade Proxy로 조회합니다.</p>
 */
@Service
public class AdmTransactionGroupService {
    private static final String EXS_SERVICE_ID = "exs";

    private final CpfTransactionTimelineQueryPort timelineQueryPort;
    private final CpfWebClient cpfWebClient;

    public AdmTransactionGroupService(
            CpfTransactionTimelineQueryPort timelineQueryPort,
            CpfWebClient cpfWebClient) {
        this.timelineQueryPort = timelineQueryPort;
        this.cpfWebClient = cpfWebClient;
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

    public Map<String, Object> findDetail(String transactionGlobalId) {
        List<Map<String, Object>> segments = findSegments(transactionGlobalId);
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("transactionGlobalId", transactionGlobalId);
        detail.put("segments", segments);
        detail.put("timeline", timelineFromSegments(segments));
        detail.put("summary", summarize(transactionGlobalId, segments));
        detail.put("headers", headerSnapshots(segments));
        detail.put("externalLogs", findExternalLogs(transactionGlobalId, 100));
        return detail;
    }

    public List<Map<String, Object>> findSegments(String transactionGlobalId) {
        if (!hasText(transactionGlobalId)) {
            return List.of();
        }
        return timelineQueryPort.findSegments(transactionGlobalId.trim());
    }

    public List<Map<String, Object>> findTimeline(String transactionGlobalId) {
        return timelineFromSegments(findSegments(transactionGlobalId));
    }

    public Map<String, Object> findHeaders(String transactionGlobalId) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("transactionGlobalId", transactionGlobalId);
        response.put("headers", headerSnapshots(findSegments(transactionGlobalId)));
        return response;
    }

    public Map<String, Object> findExternalLogs(String transactionGlobalId) {
        List<Map<String, Object>> items = findExternalLogs(transactionGlobalId, 100);
        boolean exsLedgerUsed = items.stream().anyMatch(item -> String.valueOf(item.get("source")).startsWith("EXS_"));
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("transactionGlobalId", transactionGlobalId);
        response.put("items", items);
        response.put("source", exsLedgerUsed ? "EXS_REMOTE_FACADE" : "PFW_SEGMENT_FALLBACK");
        response.put("fallbackUsed", !exsLedgerUsed);
        return response;
    }

    private List<Map<String, Object>> findExternalLogs(String transactionGlobalId, int limit) {
        if (!hasText(transactionGlobalId)) {
            return List.of();
        }
        List<Map<String, Object>> external = new ArrayList<>();
        try {
            external.addAll(fetchExs("/api/exs/transactions", transactionGlobalId, limit, "EXS_TRANSACTION"));
            external.addAll(fetchExs("/api/exs/messages", transactionGlobalId, limit, "EXS_MESSAGE"));
        } catch (RuntimeException ignored) {
            // EXS가 중단됐을 때도 PFW segment fallback으로 최소 추적을 제공합니다.
        }
        if (external.isEmpty()) {
            external.addAll(timelineQueryPort.findExternalCandidates(transactionGlobalId, limit));
        }
        return external.stream().limit(limit).toList();
    }

    private List<Map<String, Object>> fetchExs(
            String path,
            String transactionGlobalId,
            int limit,
            String source) {
        List<Map<String, Object>> rows = cpfWebClient.get(
                EXS_SERVICE_ID,
                builder -> builder.path(path)
                        .queryParam("transactionGlobalId", transactionGlobalId)
                        .queryParam("limit", boundedLimit(limit))
                        .build(),
                new ParameterizedTypeReference<List<Map<String, Object>>>() {
                });
        if (rows == null) {
            return List.of();
        }
        return rows.stream().map(row -> normalizeExternalRow(row, source)).toList();
    }

    private Map<String, Object> normalizeExternalRow(Map<String, Object> row, String source) {
        Map<String, Object> result = new LinkedHashMap<>(row);
        result.put("source", source);
        mask(result, "externalTransactionId", 500);
        mask(result, "requestHeaderMasked", 4000);
        mask(result, "responseHeaderMasked", 4000);
        mask(result, "requestPayloadMasked", 4000);
        mask(result, "responsePayloadMasked", 4000);
        mask(result, "messageSummary", 1000);
        mask(result, "failureMessageMasked", 1000);
        mask(result, "errorMessage", 1000);
        return result;
    }

    private Map<String, Object> summarize(String transactionGlobalId, List<Map<String, Object>> segments) {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("transactionGlobalId", transactionGlobalId);
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

    private void mask(Map<String, Object> row, String key, int limit) {
        row.computeIfPresent(key, (ignored, value) -> SensitiveDataMasker.mask(String.valueOf(value), limit));
    }

    private int boundedLimit(int limit) {
        return Math.max(1, Math.min(500, limit));
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
