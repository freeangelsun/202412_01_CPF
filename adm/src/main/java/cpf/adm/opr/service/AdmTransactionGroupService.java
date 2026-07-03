package cpf.adm.opr.service;

import cpf.cmn.utils.TextUtils;
import cpf.pfw.common.logging.SensitiveDataMasker;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * transactionGlobalId 기준으로 복합 거래 구간, 헤더, 외부연계 원장을 조회하는 ADM 운영 서비스입니다.
 */
@Service
public class AdmTransactionGroupService {
    private final JdbcTemplate pfwJdbcTemplate;
    private final JdbcTemplate exsJdbcTemplate;

    public AdmTransactionGroupService(
            @Qualifier("pfwJdbcTemplate") JdbcTemplate pfwJdbcTemplate,
            @Qualifier("exsJdbcTemplate") JdbcTemplate exsJdbcTemplate) {
        this.pfwJdbcTemplate = pfwJdbcTemplate;
        this.exsJdbcTemplate = exsJdbcTemplate;
    }

    public Map<String, Object> findGroups(Map<String, String> criteria) {
        Map<String, Object> response = new LinkedHashMap<>();
        if (!tableAvailable(pfwJdbcTemplate, "pfw_transaction_segment")) {
            response.put("available", false);
            response.put("items", List.of());
            response.put("message", "pfw_transaction_segment 테이블이 없습니다.");
            return response;
        }

        QueryParts query = buildGroupQuery(criteria);
        List<Map<String, Object>> rows = pfwJdbcTemplate.queryForList(query.sql(), query.args().toArray());
        response.put("available", true);
        response.put("items", rows.stream().map(this::normalizeGroupRow).toList());
        response.put("limit", limit(criteria.get("limit")));
        response.put("sort", sort(criteria.get("sort")));
        response.put("criteria", criteria);
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
        if (!TextUtils.hasText(transactionGlobalId) || !tableAvailable(pfwJdbcTemplate, "pfw_transaction_segment")) {
            return List.of();
        }
        return pfwJdbcTemplate.queryForList("""
                SELECT segment_id, transaction_segment_id, transaction_global_id, root_transaction_global_id,
                       parent_transaction_global_id, parent_segment_id, transaction_role, module_code,
                       source_module_code, target_module_code, direction, call_depth, sequence_no,
                       api_path, transaction_name, started_at, ended_at, duration_ms, status,
                       failure_yn, failure_code, failure_message_masked, request_header_snapshot_masked,
                       response_header_snapshot_masked, extension_header_snapshot_masked,
                       customer_no_masked, member_no_masked, user_id_masked, operator_id_masked,
                       client_app_id, caller_service, channel_code, original_channel_code,
                       external_institution_code, external_transaction_id
                FROM pfw_transaction_segment
                WHERE transaction_global_id = ?
                ORDER BY started_at, sequence_no, segment_id
                """, transactionGlobalId.trim()).stream()
                .map(this::normalizeSegmentRow)
                .toList();
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
        response.put("source", exsLedgerUsed ? "exs_transaction_log/exs_message_log" : "pfw_transaction_segment.external_* fallback");
        response.put("fallbackUsed", !exsLedgerUsed);
        return response;
    }

    private QueryParts buildGroupQuery(Map<String, String> criteria) {
        StringBuilder sql = new StringBuilder("""
                SELECT transaction_global_id,
                       MIN(root_transaction_global_id) AS root_transaction_global_id,
                       MIN(started_at) AS started_at,
                       MAX(ended_at) AS ended_at,
                       SUM(COALESCE(duration_ms, 0)) AS total_duration_ms,
                       COUNT(*) AS segment_count,
                       SUM(CASE WHEN transaction_role = 'EXTERNAL' THEN 1 ELSE 0 END) AS external_call_count,
                       GROUP_CONCAT(DISTINCT module_code ORDER BY started_at SEPARATOR ' -> ') AS module_flow_text,
                       SUBSTRING_INDEX(GROUP_CONCAT(module_code ORDER BY started_at SEPARATOR ' -> '), ' -> ', 1) AS origin_module_code,
                       GROUP_CONCAT(DISTINCT transaction_role ORDER BY started_at SEPARATOR ' / ') AS roles_text,
                       MAX(CASE WHEN failure_yn = 'Y' THEN module_code ELSE NULL END) AS failed_module_code,
                       MAX(CASE WHEN failure_yn = 'Y' THEN transaction_segment_id ELSE NULL END) AS failed_segment_id,
                       MAX(CASE WHEN failure_yn = 'Y' THEN transaction_name ELSE NULL END) AS failed_segment_name,
                       MAX(CASE WHEN failure_yn = 'Y' THEN failure_code ELSE NULL END) AS failure_code,
                       MAX(CASE WHEN failure_yn = 'Y' THEN failure_message_masked ELSE NULL END) AS failure_message_masked,
                       CASE WHEN SUM(CASE WHEN failure_yn = 'Y' THEN 1 ELSE 0 END) > 0 THEN 'FAILED' ELSE 'SUCCESS' END AS overall_status,
                       CASE WHEN SUM(CASE WHEN failure_yn = 'Y' THEN 1 ELSE 0 END) > 0 THEN 'Y' ELSE 'N' END AS failure_yn,
                       MAX(customer_no_masked) AS customer_no_masked,
                       MAX(member_no_masked) AS member_no_masked,
                       MAX(user_id_masked) AS user_id_masked,
                       MAX(operator_id_masked) AS operator_id_masked,
                       MAX(client_app_id) AS client_app_id,
                       MAX(caller_service) AS caller_service,
                       MAX(channel_code) AS channel_code,
                       MAX(original_channel_code) AS original_channel_code,
                       MAX(external_institution_code) AS external_institution_code,
                       MAX(external_transaction_id) AS external_transaction_id,
                       MAX(transaction_name) AS transaction_name,
                       MAX(api_path) AS api_path
                FROM pfw_transaction_segment
                WHERE 1 = 1
                """);
        List<Object> args = new ArrayList<>();
        appendLike(sql, args, "transaction_global_id", criteria.get("transactionGlobalId"));
        appendLike(sql, args, "transaction_segment_id", criteria.get("transactionSegmentId"));
        appendLike(sql, args, "transaction_segment_id", criteria.get("segmentId"));
        appendLike(sql, args, "transaction_segment_id", criteria.get("failedSegmentId"));
        appendLike(sql, args, "module_code", criteria.get("includedModuleCode"));
        appendLike(sql, args, "module_code", criteria.get("moduleCode"));
        appendEquals(sql, args, "source_module_code", criteria.get("sourceModuleCode"));
        appendEquals(sql, args, "target_module_code", criteria.get("targetModuleCode"));
        appendEquals(sql, args, "transaction_role", criteria.get("transactionRole"));
        appendEquals(sql, args, "direction", criteria.get("direction"));
        appendEquals(sql, args, "status", criteria.get("status"));
        appendEquals(sql, args, "failure_yn", criteria.get("failureYn"));
        appendEquals(sql, args, "module_code", criteria.get("failedModuleCode"));
        appendLike(sql, args, "failure_code", criteria.get("failureCode"));
        appendLike(sql, args, "customer_no_masked", criteria.get("customerNo"));
        appendLike(sql, args, "member_no_masked", criteria.get("memberNo"));
        appendLike(sql, args, "user_id_masked", criteria.get("userId"));
        appendLike(sql, args, "operator_id_masked", criteria.get("operatorId"));
        appendLike(sql, args, "client_app_id", criteria.get("clientAppId"));
        appendLike(sql, args, "caller_service", criteria.get("callerService"));
        appendEquals(sql, args, "channel_code", criteria.get("channelCode"));
        appendEquals(sql, args, "original_channel_code", criteria.get("originalChannelCode"));
        appendEquals(sql, args, "external_institution_code", criteria.get("externalInstitutionCode"));
        appendLike(sql, args, "external_transaction_id", criteria.get("externalTransactionId"));
        appendLike(sql, args, "api_path", criteria.get("apiPath"));
        appendLike(sql, args, "transaction_name", criteria.get("transactionName"));
        appendLike(sql, args, "request_header_snapshot_masked", criteria.get("standardHeaderValue"));
        appendLike(sql, args, "response_header_snapshot_masked", criteria.get("responseHeaderValue"));
        appendLike(sql, args, "extension_header_snapshot_masked", criteria.get("extensionHeaderValue"));
        appendLike(sql, args, "extension_header_snapshot_masked", criteria.get("extHeaderValue"));
        appendDateTime(sql, args, "started_at", ">=", criteria.get("startedAtFrom"));
        appendDateTime(sql, args, "started_at", "<=", criteria.get("startedAtTo"));
        appendLong(sql, args, "duration_ms", ">=", criteria.get("durationMsFrom"));
        appendLong(sql, args, "duration_ms", "<=", criteria.get("durationMsTo"));
        sql.append(" GROUP BY transaction_global_id ");
        appendOriginModule(sql, args, criteria.get("originModuleCode"));
        sql.append(orderBy(criteria.get("sort")));
        sql.append(" LIMIT ?");
        args.add(limit(criteria.get("limit")));
        return new QueryParts(sql.toString(), args);
    }

    private void appendOriginModule(StringBuilder sql, List<Object> args, String originModuleCode) {
        if (!TextUtils.hasText(originModuleCode)) {
            return;
        }
        sql.append("""
                 HAVING SUBSTRING_INDEX(module_flow_text, ' -> ', 1) = ?
                """);
        args.add(originModuleCode.trim().toUpperCase());
    }

    private String orderBy(String sortValue) {
        return switch (sort(sortValue)) {
            case "durationDesc" -> " ORDER BY total_duration_ms DESC, started_at DESC";
            case "statusAsc" -> " ORDER BY overall_status ASC, started_at DESC";
            case "failedFirst" -> " ORDER BY failure_yn DESC, started_at DESC";
            case "moduleAsc" -> " ORDER BY module_flow_text ASC, started_at DESC";
            default -> " ORDER BY started_at DESC";
        };
    }

    private String sort(String sortValue) {
        if (!TextUtils.hasText(sortValue)) {
            return "startedAtDesc";
        }
        return switch (sortValue.trim()) {
            case "durationDesc", "statusAsc", "failedFirst", "moduleAsc" -> sortValue.trim();
            default -> "startedAtDesc";
        };
    }

    private Map<String, Object> normalizeGroupRow(Map<String, Object> row) {
        Map<String, Object> result = new LinkedHashMap<>(row);
        result.computeIfPresent("external_transaction_id", (key, value) -> SensitiveDataMasker.mask(String.valueOf(value)));
        return result;
    }

    private Map<String, Object> normalizeSegmentRow(Map<String, Object> row) {
        Map<String, Object> result = new LinkedHashMap<>(row);
        result.computeIfPresent("request_header_snapshot_masked", (key, value) -> SensitiveDataMasker.mask(String.valueOf(value)));
        result.computeIfPresent("response_header_snapshot_masked", (key, value) -> SensitiveDataMasker.mask(String.valueOf(value)));
        result.computeIfPresent("extension_header_snapshot_masked", (key, value) -> SensitiveDataMasker.mask(String.valueOf(value)));
        result.computeIfPresent("external_transaction_id", (key, value) -> SensitiveDataMasker.mask(String.valueOf(value)));
        return result;
    }

    private Map<String, Object> normalizeExternalRow(Map<String, Object> row, String source) {
        Map<String, Object> result = new LinkedHashMap<>(row);
        result.put("source", source);
        result.computeIfPresent("external_transaction_id", (key, value) -> SensitiveDataMasker.mask(String.valueOf(value)));
        result.computeIfPresent("request_header_masked", (key, value) -> SensitiveDataMasker.mask(String.valueOf(value)));
        result.computeIfPresent("response_header_masked", (key, value) -> SensitiveDataMasker.mask(String.valueOf(value)));
        result.computeIfPresent("request_payload_masked", (key, value) -> SensitiveDataMasker.mask(String.valueOf(value)));
        result.computeIfPresent("response_payload_masked", (key, value) -> SensitiveDataMasker.mask(String.valueOf(value)));
        result.computeIfPresent("message_summary", (key, value) -> SensitiveDataMasker.mask(String.valueOf(value)));
        result.computeIfPresent("failure_message_masked", (key, value) -> SensitiveDataMasker.mask(String.valueOf(value)));
        result.computeIfPresent("error_message", (key, value) -> SensitiveDataMasker.mask(String.valueOf(value)));
        return result;
    }

    private Map<String, Object> summarize(String transactionGlobalId, List<Map<String, Object>> segments) {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("transactionGlobalId", transactionGlobalId);
        summary.put("segmentCount", segments.size());
        summary.put("moduleFlowText", moduleFlowText(segments));
        summary.put("overallStatus", segments.stream().anyMatch(row -> "Y".equals(String.valueOf(row.get("failure_yn")))) ? "FAILED" : "SUCCESS");
        summary.put("totalDurationMs", segments.stream()
                .map(row -> row.get("duration_ms"))
                .filter(Number.class::isInstance)
                .map(Number.class::cast)
                .mapToLong(Number::longValue)
                .sum());
        segments.stream()
                .filter(row -> "Y".equals(String.valueOf(row.get("failure_yn"))))
                .findFirst()
                .ifPresent(row -> {
                    summary.put("failedSegmentId", row.get("transaction_segment_id"));
                    summary.put("failedModuleCode", row.get("module_code"));
                    summary.put("failureCode", row.get("failure_code"));
                    summary.put("failureMessageMasked", row.get("failure_message_masked"));
                });
        return summary;
    }

    private List<Map<String, Object>> timelineFromSegments(List<Map<String, Object>> segments) {
        return segments.stream().map(row -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("transactionSegmentId", row.get("transaction_segment_id"));
            item.put("parentSegmentId", row.get("parent_segment_id"));
            item.put("sequenceNo", row.get("sequence_no"));
            item.put("callDepth", row.get("call_depth"));
            item.put("label", label(row));
            item.put("moduleCode", row.get("module_code"));
            item.put("sourceModuleCode", row.get("source_module_code"));
            item.put("targetModuleCode", row.get("target_module_code"));
            item.put("transactionRole", row.get("transaction_role"));
            item.put("direction", row.get("direction"));
            item.put("status", row.get("status"));
            item.put("startedAt", row.get("started_at"));
            item.put("endedAt", row.get("ended_at"));
            item.put("durationMs", row.get("duration_ms"));
            item.put("failureCode", row.get("failure_code"));
            item.put("failureMessageMasked", row.get("failure_message_masked"));
            return item;
        }).toList();
    }

    private List<Map<String, Object>> headerSnapshots(List<Map<String, Object>> segments) {
        return segments.stream().map(row -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("transactionSegmentId", row.get("transaction_segment_id"));
            item.put("requestHeaderSnapshotMasked", row.get("request_header_snapshot_masked"));
            item.put("responseHeaderSnapshotMasked", row.get("response_header_snapshot_masked"));
            item.put("extensionHeaderSnapshotMasked", row.get("extension_header_snapshot_masked"));
            item.put("clientAppId", row.get("client_app_id"));
            item.put("callerService", row.get("caller_service"));
            return item;
        }).toList();
    }

    private List<Map<String, Object>> findExternalLogs(String transactionGlobalId, int limit) {
        if (!TextUtils.hasText(transactionGlobalId)) {
            return List.of();
        }
        List<Map<String, Object>> ledgerRows = new ArrayList<>();
        ledgerRows.addAll(findExsTransactionLogs(transactionGlobalId, limit));
        if (ledgerRows.size() < limit) {
            ledgerRows.addAll(findExsMessageLogs(transactionGlobalId, limit - ledgerRows.size()));
        }
        if (!ledgerRows.isEmpty()) {
            return ledgerRows;
        }
        return findPfwExternalCandidates(transactionGlobalId, limit);
    }

    private List<Map<String, Object>> findExsTransactionLogs(String transactionGlobalId, int limit) {
        if (!tableAvailable(exsJdbcTemplate, "exs_transaction_log")) {
            return List.of();
        }
        try {
            return exsJdbcTemplate.queryForList("""
                    SELECT transaction_log_id, transaction_global_id, transaction_segment_id,
                           external_transaction_id, institution_code, channel_code, endpoint_code,
                           api_path, module_id, was_id, server_instance_id, request_at, response_at,
                           elapsed_ms, direction, http_method, request_uri, request_header_masked,
                           response_header_masked, request_payload_masked, response_payload_masked,
                           status, result_code, http_status, error_code, error_message,
                           retryable_yn, timeout_ms, retry_count, created_at
                    FROM exs_transaction_log
                    WHERE transaction_global_id = ?
                    ORDER BY request_at, transaction_log_id
                    LIMIT ?
                    """, transactionGlobalId.trim(), limit).stream()
                    .map(row -> normalizeExternalRow(row, "EXS_TRANSACTION_LOG"))
                    .toList();
        } catch (DataAccessException ex) {
            return List.of();
        }
    }

    private List<Map<String, Object>> findExsMessageLogs(String transactionGlobalId, int limit) {
        if (limit < 1 || !tableAvailable(exsJdbcTemplate, "exs_message_log")) {
            return List.of();
        }
        try {
            return exsJdbcTemplate.queryForList("""
                    SELECT message_log_id, transaction_global_id, transaction_segment_id,
                           external_transaction_id, direction, message_code, message_summary,
                           request_payload_masked, response_payload_masked, payload_store_yn,
                           payload_ref, status, failure_code, failure_message_masked, created_at
                    FROM exs_message_log
                    WHERE transaction_global_id = ?
                    ORDER BY created_at, message_log_id
                    LIMIT ?
                    """, transactionGlobalId.trim(), limit).stream()
                    .map(row -> normalizeExternalRow(row, "EXS_MESSAGE_LOG"))
                    .toList();
        } catch (DataAccessException ex) {
            return List.of();
        }
    }

    private List<Map<String, Object>> findPfwExternalCandidates(String transactionGlobalId, int limit) {
        if (!tableAvailable(pfwJdbcTemplate, "pfw_transaction_segment")) {
            return List.of();
        }
        return pfwJdbcTemplate.queryForList("""
                SELECT transaction_segment_id, module_code, external_institution_code, external_transaction_id,
                       api_path, status, failure_yn, failure_code, failure_message_masked,
                       started_at, ended_at, duration_ms
                FROM pfw_transaction_segment
                WHERE transaction_global_id = ?
                  AND (transaction_role = 'EXTERNAL' OR external_institution_code IS NOT NULL)
                ORDER BY started_at, sequence_no
                LIMIT ?
                """, transactionGlobalId.trim(), limit).stream()
                .map(row -> normalizeExternalRow(row, "PFW_SEGMENT_FALLBACK"))
                .toList();
    }

    private String moduleFlowText(List<Map<String, Object>> segments) {
        StringJoiner joiner = new StringJoiner(" -> ");
        String previous = null;
        for (Map<String, Object> segment : segments) {
            String moduleCode = String.valueOf(segment.get("module_code"));
            if (!moduleCode.equals(previous)) {
                joiner.add(moduleCode);
                previous = moduleCode;
            }
        }
        return joiner.toString();
    }

    private String label(Map<String, Object> row) {
        return row.get("module_code") + " " + row.get("transaction_role") + " "
                + row.get("direction") + " / " + row.get("duration_ms") + "ms";
    }

    private void appendLike(StringBuilder sql, List<Object> args, String column, String value) {
        if (TextUtils.hasText(value)) {
            sql.append(" AND ").append(column).append(" LIKE CONCAT('%', ?, '%')");
            args.add(value.trim());
        }
    }

    private void appendEquals(StringBuilder sql, List<Object> args, String column, String value) {
        if (TextUtils.hasText(value)) {
            sql.append(" AND ").append(column).append(" = ?");
            args.add(value.trim().toUpperCase());
        }
    }

    private void appendLong(StringBuilder sql, List<Object> args, String column, String operator, String value) {
        if (!TextUtils.hasText(value)) {
            return;
        }
        try {
            sql.append(" AND ").append(column).append(" ").append(operator).append(" ?");
            args.add(Long.parseLong(value.trim()));
        } catch (NumberFormatException ignored) {
            // 숫자 조건이 아니면 검색 조건에서 제외합니다.
        }
    }

    private void appendDateTime(StringBuilder sql, List<Object> args, String column, String operator, String value) {
        if (!TextUtils.hasText(value)) {
            return;
        }
        try {
            sql.append(" AND ").append(column).append(" ").append(operator).append(" ?");
            args.add(LocalDateTime.parse(value.trim()));
        } catch (DateTimeParseException ignored) {
            // ISO-8601 형식이 아니면 검색 조건에서 제외합니다.
        }
    }

    private int limit(String limitValue) {
        if (!TextUtils.hasText(limitValue)) {
            return 100;
        }
        try {
            return Math.max(1, Math.min(500, Integer.parseInt(limitValue.trim())));
        } catch (NumberFormatException ex) {
            return 100;
        }
    }

    private boolean tableAvailable(JdbcTemplate jdbcTemplate, String tableName) {
        try {
            Integer count = jdbcTemplate.queryForObject("""
                    SELECT COUNT(*)
                    FROM information_schema.tables
                    WHERE table_schema = DATABASE()
                      AND table_name = ?
                    """, Integer.class, tableName);
            return count != null && count > 0;
        } catch (DataAccessException ex) {
            return false;
        }
    }

    private record QueryParts(String sql, List<Object> args) {
    }
}
