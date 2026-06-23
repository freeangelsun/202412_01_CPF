package cpf.pfw.common.logging.policy;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * pfwDB의 로그 정책 테이블을 조회하는 JDBC 저장소입니다.
 *
 * <p>pfwDB가 아직 설치되지 않은 개발 환경에서도 애플리케이션 기동을 막지 않도록
 * 테이블 미존재, 연결 실패, 권한 오류는 빈 결과로 처리합니다. 정책 평가 계층은 이때
 * application.yml 기본값 또는 CPF 기본값으로 안전하게 fallback합니다.</p>
 */
public class JdbcLogPolicyRepository implements LogPolicyRepository {

    private final ObjectProvider<JdbcTemplate> jdbcTemplateProvider;
    private final ObjectProvider<DataSource> dataSourceProvider;

    public JdbcLogPolicyRepository(
            ObjectProvider<JdbcTemplate> jdbcTemplateProvider,
            ObjectProvider<DataSource> dataSourceProvider) {
        this.jdbcTemplateProvider = jdbcTemplateProvider;
        this.dataSourceProvider = dataSourceProvider;
    }

    @Override
    public Optional<LogPolicyRow> findActiveOverride(LogPolicyTargetType targetType, String targetId, LocalDateTime now) {
        if (!tableAvailable("pfw_log_policy_override")) {
            return Optional.empty();
        }
        try {
            List<Object> args = new java.util.ArrayList<>();
            String targetTypePlaceholders = placeholders(targetType.databaseCodes(), args);
            args.add(LogPolicyDecision.normalizeTargetId(targetId));
            args.add(now);
            args.add(now);
            List<Map<String, Object>> rows = jdbc().queryForList("""
                    SELECT override_id, policy_id, target_type, target_id, log_level,
                           db_log_enabled_yn, file_log_enabled_yn, request_body_log_yn,
                           response_body_log_yn, error_stack_log_yn, masking_policy_key
                    FROM pfw_log_policy_override
                    WHERE active_yn = 'Y'
                      AND target_type IN (%s)
                      AND target_id IN (?, '*')
                      AND effective_start_at <= ?
                      AND effective_end_at >= ?
                    ORDER BY CASE WHEN target_id = ? THEN 0 ELSE 1 END, override_id DESC
                    LIMIT 1
                    """.formatted(targetTypePlaceholders), appendExactTarget(args, targetId).toArray());
            return rows.stream().findFirst().map(row -> toRow(row, "ADM_OVERRIDE"));
        } catch (DataAccessException ex) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<LogPolicyRow> findActivePolicy(LogPolicyTargetType targetType, String targetId) {
        if (!tableAvailable("pfw_log_policy")) {
            return Optional.empty();
        }
        try {
            List<Object> args = new java.util.ArrayList<>();
            String targetTypePlaceholders = placeholders(targetType.databaseCodes(), args);
            args.add(LogPolicyDecision.normalizeTargetId(targetId));
            List<Map<String, Object>> rows = jdbc().queryForList("""
                    SELECT policy_id, NULL AS override_id, target_type, target_id, log_level,
                           db_log_enabled_yn, file_log_enabled_yn, request_body_log_yn,
                           response_body_log_yn, error_stack_log_yn, masking_policy_key
                    FROM pfw_log_policy
                    WHERE active_yn = 'Y'
                      AND target_type IN (%s)
                      AND target_id IN (?, '*')
                    ORDER BY CASE WHEN target_id = ? THEN 0 ELSE 1 END, priority ASC, policy_id ASC
                    LIMIT 1
                    """.formatted(targetTypePlaceholders), appendExactTarget(args, targetId).toArray());
            return rows.stream().findFirst().map(row -> toRow(row, "DB_POLICY"));
        } catch (DataAccessException ex) {
            return Optional.empty();
        }
    }

    private boolean tableAvailable(String tableName) {
        if (jdbcTemplateProvider.getIfAvailable() == null && dataSourceProvider.getIfAvailable() == null) {
            return false;
        }
        try {
            Integer count = jdbc().queryForObject("""
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

    private JdbcTemplate jdbc() {
        JdbcTemplate jdbcTemplate = jdbcTemplateProvider.getIfAvailable();
        if (jdbcTemplate != null) {
            return jdbcTemplate;
        }
        DataSource dataSource = dataSourceProvider.getIfAvailable();
        if (dataSource == null) {
            throw new IllegalStateException("pfwDataSource 또는 pfwJdbcTemplate이 필요합니다.");
        }
        return new JdbcTemplate(dataSource);
    }

    private String placeholders(String[] values, List<Object> args) {
        Arrays.stream(values)
                .map(value -> value.toUpperCase(Locale.ROOT))
                .forEach(args::add);
        return String.join(",", Arrays.stream(values).map(value -> "?").toList());
    }

    private List<Object> appendExactTarget(List<Object> args, String targetId) {
        args.add(LogPolicyDecision.normalizeTargetId(targetId));
        return args;
    }

    private LogPolicyRow toRow(Map<String, Object> row, String source) {
        return new LogPolicyRow(
                longValue(row.get("policy_id")),
                longValue(row.get("override_id")),
                stringValue(row.get("target_type")),
                stringValue(row.get("target_id")),
                stringValue(row.get("log_level")),
                stringValue(row.get("db_log_enabled_yn")),
                stringValue(row.get("file_log_enabled_yn")),
                stringValue(row.get("request_body_log_yn")),
                stringValue(row.get("response_body_log_yn")),
                stringValue(row.get("error_stack_log_yn")),
                stringValue(row.get("masking_policy_key")),
                source);
    }

    private Long longValue(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value == null) {
            return null;
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
