package cpf.adm.opr.service;

import cpf.pfw.api.logging.CpfTransactionTimelineQueryPort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * transactionGlobalId 기준으로 PFW 표준 거래 구간과 외부 호출 후보를 조합합니다.
 *
 * <p>ADM은 다른 주제영역 DB를 직접 조회하지 않고 PFW 공개 조회 포트만 사용합니다.
 * 외부 연계 모듈이 추가되더라도 표준 구간 로그에 기록하면 ADM 구현 변경 없이 함께 조회됩니다.</p>
 */
@Service
public class AdmTransactionGroupService extends cpf.adm.common.base.AdmBaseService {
    private final CpfTransactionTimelineQueryPort timelineQueryPort;

    public AdmTransactionGroupService(CpfTransactionTimelineQueryPort timelineQueryPort) {
        this.timelineQueryPort = timelineQueryPort;
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
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("transactionGlobalId", transactionGlobalId);
        response.put("items", items);
        response.put("source", "PFW_TRANSACTION_SEGMENT");
        response.put("fallbackUsed", false);
        return response;
    }

    private List<Map<String, Object>> findExternalLogs(String transactionGlobalId, int limit) {
        if (!hasText(transactionGlobalId)) {
            return List.of();
        }
        return timelineQueryPort.findExternalCandidates(transactionGlobalId, boundedLimit(limit));
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

    private int boundedLimit(int limit) {
        return Math.max(1, Math.min(500, limit));
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
