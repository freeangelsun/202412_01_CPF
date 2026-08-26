package com.cpf.platform.operations.observability.internal.logging.policy;

import com.cpf.platform.operations.observability.api.logging.policy.LogPolicyDecision;
import com.cpf.platform.operations.observability.api.logging.policy.LogPolicyTargetType;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Versioned 로그 정책 JDBC Repository입니다.
 *
 * <p>DataSource가 없는 선택 설치 상태에서는 정책 없음으로 동작하지만, DB가 연결된 뒤의
 * SQL/Decode 실패는 빈 정책으로 숨기지 않고 fail-closed 합니다.</p>
 */
public class JdbcLogPolicyRepository implements LogPolicyRepository {

    private final ObjectProvider<JdbcTemplate> jdbcTemplateProvider;
    private final ObjectProvider<DataSource> dataSourceProvider;

    public JdbcLogPolicyRepository(
            @Qualifier("cpfJdbcTemplate") ObjectProvider<JdbcTemplate> jdbcTemplateProvider,
            @Qualifier("cpfDataSource") ObjectProvider<DataSource> dataSourceProvider) {
        this.jdbcTemplateProvider = jdbcTemplateProvider;
        this.dataSourceProvider = dataSourceProvider;
    }

    @Override
    public Optional<LogPolicyRow> findActiveOverride(LogPolicyTargetType targetType, String targetId, LocalDateTime now) {
        if (!tableAvailable("OPS_LOG_POLICY_OVERRIDE")) return Optional.empty();
        List<Object> args = new ArrayList<>();
        String targetTypePlaceholders = placeholders(targetType.databaseCodes(), args);
        args.add(LogPolicyDecision.normalizeTargetId(targetId));
        args.add(now);
        args.add(now);
        List<Map<String, Object>> rows = queryForListLimited(selectColumns("OPS_LOG_POLICY_OVERRIDE", true) + """
                WHERE active_yn = 'Y'
                  AND target_type IN (%s)
                  AND target_id IN (?, '*')
                  AND effective_start_at <= ?
                  AND effective_end_at >= ?
                ORDER BY CASE WHEN target_id = ? THEN 0 ELSE 1 END, override_id DESC
                """.formatted(targetTypePlaceholders), appendExactTarget(args, targetId), 1);
        return rows.stream().findFirst().map(row -> toRow(row, "ADM_OVERRIDE"));
    }

    @Override
    public Optional<LogPolicyRow> findActivePolicy(LogPolicyTargetType targetType, String targetId) {
        if (!tableAvailable("OPS_LOG_POLICY")) return Optional.empty();
        List<Object> args = new ArrayList<>();
        String targetTypePlaceholders = placeholders(targetType.databaseCodes(), args);
        args.add(LogPolicyDecision.normalizeTargetId(targetId));
        List<Map<String, Object>> rows = queryForListLimited(selectColumns("OPS_LOG_POLICY", false) + """
                WHERE active_yn = 'Y'
                  AND target_type IN (%s)
                  AND target_id IN (?, '*')
                ORDER BY CASE WHEN target_id = ? THEN 0 ELSE 1 END, priority ASC, policy_id ASC
                """.formatted(targetTypePlaceholders), appendExactTarget(args, targetId), 1);
        return rows.stream().findFirst().map(row -> toRow(row, "DB_POLICY"));
    }

    private String selectColumns(String table, boolean override) {
        return """
                SELECT policy_id, %s AS override_id, policy_schema_version, target_type, target_id, log_level,
                       db_log_enabled_yn, file_log_enabled_yn,
                       query_capture_mode, request_header_capture_mode, response_header_capture_mode,
                       request_body_capture_mode, response_body_capture_mode, error_stack_capture_mode,
                       query_allowlist, header_allowlist, field_allowlist,
                       max_query_bytes, max_header_bytes, max_request_body_bytes,
                       max_response_body_bytes, max_stack_bytes, masking_policy_key, policy_checksum
                FROM %s
                """.formatted(override ? "override_id" : "NULL", table);
    }

    private boolean tableAvailable(String tableName) {
        if (jdbcTemplateProvider.getIfAvailable() == null && dataSourceProvider.getIfAvailable() == null) return false;
        DataSource dataSource = dataSourceProvider.getIfAvailable();
        if (dataSource == null) {
            JdbcTemplate template = jdbcTemplateProvider.getIfAvailable();
            dataSource = template == null ? null : template.getDataSource();
        }
        if (dataSource == null) return false;
        try (Connection connection = dataSource.getConnection()) {
            String catalog = connection.getCatalog();
            String schema = currentSchema(connection);
            for (String candidate : List.of(tableName, tableName.toUpperCase(Locale.ROOT), tableName.toLowerCase(Locale.ROOT))) {
                try (ResultSet tables = connection.getMetaData().getTables(catalog, schema, candidate, new String[]{"TABLE"})) {
                    if (tables.next()) return true;
                }
            }
            return false;
        } catch (SQLException ex) {
            throw new IllegalStateException("로그 정책 Table 가용성 확인에 실패했습니다: " + tableName, ex);
        }
    }

    private JdbcTemplate jdbc() {
        JdbcTemplate template = jdbcTemplateProvider.getIfAvailable();
        if (template != null) return template;
        DataSource dataSource = dataSourceProvider.getIfAvailable();
        if (dataSource == null) throw new IllegalStateException("cpfDataSource 또는 cpfJdbcTemplate이 필요합니다.");
        return new JdbcTemplate(dataSource);
    }

    private String placeholders(String[] values, List<Object> args) {
        Arrays.stream(values).map(v -> v.toUpperCase(Locale.ROOT)).forEach(args::add);
        return String.join(",", Arrays.stream(values).map(v -> "?").toList());
    }

    private List<Object> appendExactTarget(List<Object> args, String targetId) {
        args.add(LogPolicyDecision.normalizeTargetId(targetId));
        return args;
    }

    private List<Map<String, Object>> queryForListLimited(String sql, List<?> args, int limit) {
        return jdbc().query(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql);
            for (int index = 0; index < args.size(); index++) statement.setObject(index + 1, args.get(index));
            statement.setMaxRows(Math.max(1, limit));
            return statement;
        }, new ColumnMapRowMapper());
    }

    private String currentSchema(Connection connection) {
        try { return connection.getSchema(); }
        catch (SQLException | AbstractMethodError ignored) { return null; }
    }

    private LogPolicyRow toRow(Map<String, Object> row, String source) {
        return new LogPolicyRow(
                longValue(row.get("policy_id")), longValue(row.get("override_id")),
                intValue(row.get("policy_schema_version")), stringRequired(row, "target_type"),
                stringRequired(row, "target_id"), stringRequired(row, "log_level"),
                stringValue(row.get("db_log_enabled_yn")), stringValue(row.get("file_log_enabled_yn")),
                stringValue(row.get("query_capture_mode")), stringValue(row.get("request_header_capture_mode")),
                stringValue(row.get("response_header_capture_mode")), stringValue(row.get("request_body_capture_mode")),
                stringValue(row.get("response_body_capture_mode")), stringValue(row.get("error_stack_capture_mode")),
                stringValue(row.get("query_allowlist")), stringValue(row.get("header_allowlist")),
                stringValue(row.get("field_allowlist")), intValue(row.get("max_query_bytes")),
                intValue(row.get("max_header_bytes")), intValue(row.get("max_request_body_bytes")),
                intValue(row.get("max_response_body_bytes")), intValue(row.get("max_stack_bytes")),
                stringValue(row.get("masking_policy_key")), stringValue(row.get("policy_checksum")), source);
    }

    private Integer intValue(Object value) {
        if (value instanceof Number n) return n.intValue();
        if (value == null) return null;
        try { return Integer.parseInt(String.valueOf(value)); }
        catch (NumberFormatException ex) { throw contract("integer", value); }
    }
    private String stringRequired(Map<String,Object> row, String key) {
        String value=stringValue(row.get(key));
        if (value == null || value.isBlank()) throw contract(key,row.get(key));
        return value;
    }
    private Long longValue(Object value) {
        if (value instanceof Number n) return n.longValue();
        if (value == null) return null;
        try { return Long.parseLong(String.valueOf(value)); }
        catch (NumberFormatException ex) { throw contract("long", value); }
    }
    private String stringValue(Object value) { return value == null ? null : String.valueOf(value); }
    private IllegalStateException contract(String field,Object value) {
        return new IllegalStateException("로그 정책 DB 계약 오류: " + field + "=" + value);
    }
}
