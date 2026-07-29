package com.cpf.core.common.logging.segment;

import com.cpf.core.api.logging.CpfTransactionTimelineQueryPort;
import com.cpf.core.common.logging.SensitiveDataMasker;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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
        QueryParts query = buildGroupQuery(safeCriteria, sort);
        List<Map<String, Object>> rows = enrichGroupRows(
                queryForListLimited(query.sql(), query.args(), limit),
                query).stream()
                .map(this::maskGroupRow)
                .toList();
        return new GroupQueryResult(true, rows, limit, sort, null);
    }

    @Override
    public List<Map<String, Object>> findSegments(String transactionId) {
        if (!hasText(transactionId) || !tableAvailable()) {
            return List.of();
        }
        return jdbcTemplate.queryForList("""
                SELECT transaction_segment_id AS transactionSegmentId,
                       transaction_id AS transactionId,
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
                 WHERE transaction_id = ?
                 ORDER BY started_at, sequence_no, segment_id
                """, transactionId.trim()).stream()
                .map(this::maskSegmentRow)
                .toList();
    }

    @Override
    public List<Map<String, Object>> findExternalCandidates(String transactionId, int limit) {
        if (!hasText(transactionId) || !tableAvailable()) {
            return List.of();
        }
        return queryForListLimited("""
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
                 WHERE transaction_id = ?
                   AND (transaction_role = 'EXTERNAL' OR external_institution_code IS NOT NULL)
                 ORDER BY started_at, sequence_no
                """, List.of(transactionId.trim()), boundedLimit(limit)).stream()
                .map(this::maskExternalRow)
                .toList();
    }

    private QueryParts buildGroupQuery(Map<String, String> criteria, String sort) {
        StringBuilder sql = new StringBuilder("""
                WITH filtered_segments AS (
                    SELECT cpf_transaction_segment.*,
                           ROW_NUMBER() OVER (
                               PARTITION BY transaction_id
                               ORDER BY started_at, sequence_no, segment_id
                           ) AS cpf_row_no
                      FROM cpf_transaction_segment
                     WHERE 1 = 1
                """);
        List<Object> args = new ArrayList<>();
        appendCriteria(sql, args, criteria);
        sql.append("""
                )
                SELECT transaction_id AS transactionId,
                       MIN(started_at) AS startedAt,
                       MAX(ended_at) AS endedAt,
                       SUM(COALESCE(duration_ms, 0)) AS totalDurationMs,
                       COUNT(*) AS segmentCount,
                       SUM(CASE WHEN transaction_role = 'EXTERNAL' THEN 1 ELSE 0 END) AS externalCallCount,
                       MAX(CASE WHEN cpf_row_no = 1 THEN module_code END) AS originModuleCode,
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
                  FROM filtered_segments
                 GROUP BY transaction_id
                """);
        if (hasText(criteria.get("originModuleCode"))) {
            sql.append(" HAVING MAX(CASE WHEN cpf_row_no = 1 THEN module_code END) = ?");
            args.add(criteria.get("originModuleCode").trim().toUpperCase());
        }
        sql.append(orderBy(sort));

        StringBuilder detailSql = new StringBuilder("""
                SELECT transaction_id AS transactionId,
                       module_code AS moduleCode,
                       transaction_role AS transactionRole
                  FROM cpf_transaction_segment
                 WHERE 1 = 1
                """);
        List<Object> detailArgs = new ArrayList<>();
        appendCriteria(detailSql, detailArgs, criteria);
        return new QueryParts(sql.toString(), args, detailSql.toString(), detailArgs);
    }

    private void appendCriteria(StringBuilder sql, List<Object> args, Map<String, String> criteria) {
        appendLike(sql, args, "transaction_id", criteria.get("transactionId"));
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
    }

    private List<Map<String, Object>> enrichGroupRows(List<Map<String, Object>> groups, QueryParts query) {
        if (groups.isEmpty()) {
            return groups;
        }
        List<String> transactionIds = groups.stream()
                .map(row -> stringValue(value(row, "transactionId")))
                .filter(this::hasText)
                .toList();
        if (transactionIds.isEmpty()) {
            return groups;
        }
        String placeholders = String.join(",", transactionIds.stream().map(ignored -> "?").toList());
        List<Object> detailArgs = new ArrayList<>(query.detailArgs());
        detailArgs.addAll(transactionIds);
        String detailSql = query.detailSql()
                + " AND transaction_id IN (" + placeholders + ")"
                + " ORDER BY transaction_id, started_at, sequence_no, segment_id";
        List<Map<String, Object>> details = jdbcTemplate.queryForList(detailSql, detailArgs.toArray());
        Map<String, LinkedHashSet<String>> modules = new LinkedHashMap<>();
        Map<String, LinkedHashSet<String>> roles = new LinkedHashMap<>();
        for (Map<String, Object> detail : details) {
            String transactionId = stringValue(value(detail, "transactionId"));
            if (!hasText(transactionId)) {
                continue;
            }
            addIfPresent(modules.computeIfAbsent(transactionId, ignored -> new LinkedHashSet<>()),
                    stringValue(value(detail, "moduleCode")));
            addIfPresent(roles.computeIfAbsent(transactionId, ignored -> new LinkedHashSet<>()),
                    stringValue(value(detail, "transactionRole")));
        }
        List<Map<String, Object>> enriched = new ArrayList<>(groups.size());
        for (Map<String, Object> group : groups) {
            Map<String, Object> row = new LinkedHashMap<>(group);
            String transactionId = stringValue(value(group, "transactionId"));
            row.put("moduleFlowText", joinOrNull(modules.get(transactionId), " -> "));
            row.put("rolesText", joinOrNull(roles.get(transactionId), " / "));
            enriched.add(row);
        }
        return enriched;
    }

    private void addIfPresent(LinkedHashSet<String> values, String value) {
        if (hasText(value)) {
            values.add(value);
        }
    }

    private String joinOrNull(LinkedHashSet<String> values, String delimiter) {
        return values == null || values.isEmpty() ? null : String.join(delimiter, values);
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
        DataSource dataSource = jdbcTemplate.getDataSource();
        if (dataSource == null) {
            return false;
        }
        try (Connection connection = dataSource.getConnection()) {
            String catalog = connection.getCatalog();
            String schema = currentSchema(connection);
            for (String candidate : List.of(
                    "cpf_transaction_segment",
                    "CPF_TRANSACTION_SEGMENT")) {
                try (ResultSet tables = connection.getMetaData()
                        .getTables(catalog, schema, candidate, new String[]{"TABLE"})) {
                    if (tables.next()) {
                        return true;
                    }
                }
            }
            return false;
        } catch (SQLException ex) {
            return false;
        }
    }

    private void appendLike(StringBuilder sql, List<Object> args, String column, String value) {
        if (hasText(value)) {
            sql.append(" AND ").append(column).append(" LIKE ?");
            args.add('%' + value.trim() + '%');
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
            case "moduleAsc" -> " ORDER BY originModuleCode ASC, startedAt DESC";
            default -> " ORDER BY startedAt DESC";
        };
    }

    private List<Map<String, Object>> queryForListLimited(String sql, List<?> args, int limit) {
        return jdbcTemplate.query(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql);
            for (int index = 0; index < args.size(); index++) {
                statement.setObject(index + 1, args.get(index));
            }
            statement.setMaxRows(boundedLimit(limit));
            return statement;
        }, new ColumnMapRowMapper());
    }

    private String currentSchema(Connection connection) {
        try {
            return connection.getSchema();
        } catch (SQLException | AbstractMethodError ignored) {
            return null;
        }
    }

    private Object value(Map<String, Object> row, String key) {
        Object exact = row.get(key);
        if (exact != null || row.containsKey(key)) {
            return exact;
        }
        return row.entrySet().stream()
                .filter(entry -> entry.getKey().equalsIgnoreCase(key))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
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

    private record QueryParts(
            String sql,
            List<Object> args,
            String detailSql,
            List<Object> detailArgs) {
    }
}
