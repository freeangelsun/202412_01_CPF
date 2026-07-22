package com.cpf.core.common.logging.segment;

import com.cpf.core.api.logging.CpfTransactionTimelineQueryPort;
import com.cpf.core.common.logging.SensitiveDataMasker;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * CPF 소유 거래 구간 스키마를 조회하고 외부에는 테이블 독립적인 결과만 반환합니다.
 */
@Component
public class CpfTransactionTimelineQueryFacade implements CpfTransactionTimelineQueryPort {
    private final JdbcTemplate jdbcTemplate;

    public CpfTransactionTimelineQueryFacade(
            @Qualifier("cpfJdbcTemplate") ObjectProvider<JdbcTemplate> jdbcTemplateProvider) {
        // CPF DB를 사용하지 않는 업무 앱에서도 공개 조회 포트 자체는 안전하게 기동되어야 합니다.
        this.jdbcTemplate = jdbcTemplateProvider.getIfAvailable();
    }

    @Override
    public GroupQueryResult findGroups(Map<String, String> criteria) {
        Map<String, String> safeCriteria = criteria == null ? Map.of() : criteria;
        int limit = limit(safeCriteria.get("limit"));
        String sort = sort(safeCriteria.get("sort"));
        if (!tableAvailable()) {
            return new GroupQueryResult(false, List.of(), limit, sort, "CPF 거래 구간 저장소를 사용할 수 없습니다.");
        }
        QueryParts query = buildGroupQuery(safeCriteria, limit, sort);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(query.sql(), query.args().toArray()).stream()
                .map(this::maskGroupRow)
                .toList();
        return new GroupQueryResult(true, rows, limit, sort, null);
    }

    @Override
    public List<Map<String, Object>> findSegments(String transactionGlobalId) {
        if (!hasText(transactionGlobalId) || !tableAvailable()) {
            return List.of();
        }
        return jdbcTemplate.queryForList("""
                SELECT transaction_segment_id AS transactionSegmentId,
                       transaction_global_id AS transactionGlobalId,
                       root_transaction_global_id AS rootTransactionGlobalId,
                       parent_transaction_global_id AS parentTransactionGlobalId,
                       parent_segment_id AS parentSegmentId,
                       transaction_role AS transactionRole,
                       module_code AS moduleCode,
                       source_module_code AS sourceModuleCode,
                       target_module_code AS targetModuleCode,
                       direction,
                       call_depth AS callDepth,
                       sequence_no AS sequenceNo,
                       api_path AS apiPath,
                       transaction_name AS transactionName,
                       started_at AS startedAt,
                       ended_at AS endedAt,
                       duration_ms AS durationMs,
                       status,
                       failure_yn AS failureYn,
                       failure_code AS failureCode,
                       failure_message_masked AS failureMessageMasked,
                       request_header_snapshot_masked AS requestHeaderSnapshotMasked,
                       response_header_snapshot_masked AS responseHeaderSnapshotMasked,
                       extension_header_snapshot_masked AS extensionHeaderSnapshotMasked,
                       customer_no_masked AS customerNoMasked,
                       member_no_masked AS memberNoMasked,
                       user_id_masked AS userIdMasked,
                       operator_id_masked AS operatorIdMasked,
                       client_app_id AS clientAppId,
                       caller_service AS callerService,
                       channel_code AS channelCode,
                       original_channel_code AS originalChannelCode,
                       external_institution_code AS externalInstitutionCode,
                       external_transaction_id AS externalTransactionId,
                       selected_instance_id AS selectedInstanceId,
                       attempt_no AS attemptNo,
                       retry_yn AS retryYn,
                       failover_yn AS failoverYn,
                       circuit_state AS circuitState,
                       downstream_http_status AS downstreamHttpStatus,
                       result_state AS resultState,
                       unknown_result_id AS unknownResultId
                  FROM cpf_transaction_segment
                 WHERE transaction_global_id = ?
                 ORDER BY started_at, sequence_no, segment_id
                """, transactionGlobalId.trim()).stream()
                .map(this::maskSegmentRow)
                .toList();
    }

    @Override
    public List<Map<String, Object>> findExternalCandidates(String transactionGlobalId, int limit) {
        if (!hasText(transactionGlobalId) || !tableAvailable()) {
            return List.of();
        }
        return jdbcTemplate.queryForList("""
                SELECT transaction_segment_id AS transactionSegmentId,
                       module_code AS moduleCode,
                       external_institution_code AS externalInstitutionCode,
                       external_transaction_id AS externalTransactionId,
                       api_path AS apiPath,
                       status,
                       failure_yn AS failureYn,
                       failure_code AS failureCode,
                       failure_message_masked AS failureMessageMasked,
                       selected_instance_id AS selectedInstanceId,
                       attempt_no AS attemptNo,
                       retry_yn AS retryYn,
                       failover_yn AS failoverYn,
                       circuit_state AS circuitState,
                       downstream_http_status AS downstreamHttpStatus,
                       result_state AS resultState,
                       unknown_result_id AS unknownResultId,
                       started_at AS startedAt,
                       ended_at AS endedAt,
                       duration_ms AS durationMs
                  FROM cpf_transaction_segment
                 WHERE transaction_global_id = ?
                   AND (transaction_role = 'EXTERNAL' OR external_institution_code IS NOT NULL)
                 ORDER BY started_at, sequence_no
                 LIMIT ?
                """, transactionGlobalId.trim(), boundedLimit(limit)).stream()
                .map(this::maskExternalRow)
                .toList();
    }

    private QueryParts buildGroupQuery(Map<String, String> criteria, int limit, String sort) {
        StringBuilder sql = new StringBuilder("""
                SELECT transaction_global_id AS transactionGlobalId,
                       MIN(root_transaction_global_id) AS rootTransactionGlobalId,
                       MIN(started_at) AS startedAt,
                       MAX(ended_at) AS endedAt,
                       SUM(COALESCE(duration_ms, 0)) AS totalDurationMs,
                       COUNT(*) AS segmentCount,
                       SUM(CASE WHEN transaction_role = 'EXTERNAL' THEN 1 ELSE 0 END) AS externalCallCount,
                       GROUP_CONCAT(DISTINCT module_code ORDER BY started_at SEPARATOR ' -> ') AS moduleFlowText,
                       SUBSTRING_INDEX(GROUP_CONCAT(module_code ORDER BY started_at SEPARATOR ' -> '), ' -> ', 1) AS originModuleCode,
                       GROUP_CONCAT(DISTINCT transaction_role ORDER BY started_at SEPARATOR ' / ') AS rolesText,
                       MAX(CASE WHEN failure_yn = 'Y' THEN module_code ELSE NULL END) AS failedModuleCode,
                       MAX(CASE WHEN failure_yn = 'Y' THEN transaction_segment_id ELSE NULL END) AS failedSegmentId,
                       MAX(CASE WHEN failure_yn = 'Y' THEN transaction_name ELSE NULL END) AS failedSegmentName,
                       MAX(CASE WHEN failure_yn = 'Y' THEN failure_code ELSE NULL END) AS failureCode,
                       MAX(CASE WHEN failure_yn = 'Y' THEN failure_message_masked ELSE NULL END) AS failureMessageMasked,
                       CASE WHEN SUM(CASE WHEN failure_yn = 'Y' THEN 1 ELSE 0 END) > 0 THEN 'FAILED' ELSE 'SUCCESS' END AS overallStatus,
                       CASE WHEN SUM(CASE WHEN failure_yn = 'Y' THEN 1 ELSE 0 END) > 0 THEN 'Y' ELSE 'N' END AS failureYn,
                       MAX(customer_no_masked) AS customerNoMasked,
                       MAX(member_no_masked) AS memberNoMasked,
                       MAX(user_id_masked) AS userIdMasked,
                       MAX(operator_id_masked) AS operatorIdMasked,
                       MAX(client_app_id) AS clientAppId,
                       MAX(caller_service) AS callerService,
                       MAX(channel_code) AS channelCode,
                       MAX(original_channel_code) AS originalChannelCode,
                       MAX(external_institution_code) AS externalInstitutionCode,
                       MAX(external_transaction_id) AS externalTransactionId,
                       MAX(transaction_name) AS transactionName,
                       MAX(api_path) AS apiPath
                  FROM cpf_transaction_segment
                 WHERE 1 = 1
                """);
        List<Object> args = new ArrayList<>();
        appendLike(sql, args, "transaction_global_id", criteria.get("transactionGlobalId"));
        appendLike(sql, args, "transaction_segment_id", first(criteria, "transactionSegmentId", "segmentId", "failedSegmentId"));
        appendLike(sql, args, "module_code", first(criteria, "includedModuleCode", "moduleCode"));
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
        appendLike(sql, args, "extension_header_snapshot_masked", first(criteria, "extensionHeaderValue", "extHeaderValue"));
        appendDateTime(sql, args, "started_at", ">=", criteria.get("startedAtFrom"));
        appendDateTime(sql, args, "started_at", "<=", criteria.get("startedAtTo"));
        appendLong(sql, args, "duration_ms", ">=", criteria.get("durationMsFrom"));
        appendLong(sql, args, "duration_ms", "<=", criteria.get("durationMsTo"));
        sql.append(" GROUP BY transaction_global_id");
        if (hasText(criteria.get("originModuleCode"))) {
            sql.append(" HAVING originModuleCode = ?");
            args.add(criteria.get("originModuleCode").trim().toUpperCase());
        }
        sql.append(orderBy(sort)).append(" LIMIT ?");
        args.add(limit);
        return new QueryParts(sql.toString(), args);
    }

    private Map<String, Object> maskGroupRow(Map<String, Object> row) {
        Map<String, Object> result = new LinkedHashMap<>(row);
        mask(result, "externalTransactionId", 500);
        return result;
    }

    private Map<String, Object> maskSegmentRow(Map<String, Object> row) {
        Map<String, Object> result = new LinkedHashMap<>(row);
        mask(result, "requestHeaderSnapshotMasked", 4000);
        mask(result, "responseHeaderSnapshotMasked", 4000);
        mask(result, "extensionHeaderSnapshotMasked", 4000);
        mask(result, "externalTransactionId", 500);
        return result;
    }

    private Map<String, Object> maskExternalRow(Map<String, Object> row) {
        Map<String, Object> result = new LinkedHashMap<>(row);
        result.put("source", "CPF_SEGMENT_FALLBACK");
        mask(result, "externalTransactionId", 500);
        mask(result, "failureMessageMasked", 1000);
        return result;
    }

    private void mask(Map<String, Object> row, String key, int limit) {
        row.computeIfPresent(key, (ignored, value) -> SensitiveDataMasker.mask(String.valueOf(value), limit));
    }

    private boolean tableAvailable() {
        if (jdbcTemplate == null) {
            return false;
        }
        try {
            Integer count = jdbcTemplate.queryForObject("""
                    SELECT COUNT(*)
                      FROM information_schema.tables
                     WHERE table_schema = DATABASE()
                       AND table_name = 'cpf_transaction_segment'
                    """, Integer.class);
            return count != null && count > 0;
        } catch (DataAccessException ex) {
            return false;
        }
    }

    private void appendLike(StringBuilder sql, List<Object> args, String column, String value) {
        if (hasText(value)) {
            sql.append(" AND ").append(column).append(" LIKE CONCAT('%', ?, '%')");
            args.add(value.trim());
        }
    }

    private void appendEquals(StringBuilder sql, List<Object> args, String column, String value) {
        if (hasText(value)) {
            sql.append(" AND ").append(column).append(" = ?");
            args.add(value.trim().toUpperCase());
        }
    }

    private void appendLong(StringBuilder sql, List<Object> args, String column, String operator, String value) {
        if (!hasText(value)) {
            return;
        }
        try {
            sql.append(" AND ").append(column).append(' ').append(operator).append(" ?");
            args.add(Long.parseLong(value.trim()));
        } catch (NumberFormatException ignored) {
            // 숫자 형식이 아닌 검색 조건은 안전하게 제외합니다.
        }
    }

    private void appendDateTime(StringBuilder sql, List<Object> args, String column, String operator, String value) {
        if (!hasText(value)) {
            return;
        }
        try {
            sql.append(" AND ").append(column).append(' ').append(operator).append(" ?");
            args.add(LocalDateTime.parse(value.trim()));
        } catch (DateTimeParseException ignored) {
            // ISO-8601 형식이 아닌 검색 조건은 안전하게 제외합니다.
        }
    }

    private String orderBy(String sort) {
        return switch (sort) {
            case "durationDesc" -> " ORDER BY totalDurationMs DESC, startedAt DESC";
            case "statusAsc" -> " ORDER BY overallStatus ASC, startedAt DESC";
            case "failedFirst" -> " ORDER BY failureYn DESC, startedAt DESC";
            case "moduleAsc" -> " ORDER BY moduleFlowText ASC, startedAt DESC";
            default -> " ORDER BY startedAt DESC";
        };
    }

    private int limit(String value) {
        try {
            return hasText(value) ? boundedLimit(Integer.parseInt(value.trim())) : 100;
        } catch (NumberFormatException ex) {
            return 100;
        }
    }

    private int boundedLimit(int value) {
        return Math.max(1, Math.min(500, value));
    }

    private String sort(String value) {
        if (!hasText(value)) {
            return "startedAtDesc";
        }
        return switch (value.trim()) {
            case "durationDesc", "statusAsc", "failedFirst", "moduleAsc" -> value.trim();
            default -> "startedAtDesc";
        };
    }

    private String first(Map<String, String> values, String... keys) {
        for (String key : keys) {
            if (hasText(values.get(key))) {
                return values.get(key);
            }
        }
        return null;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private record QueryParts(String sql, List<Object> args) {
    }
}
