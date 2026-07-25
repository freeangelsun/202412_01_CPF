package com.cpf.bizadmin.sample.sequence;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 선택형 업무 채번 Sample의 BZA 소유 저장소입니다.
 *
 * <p>실제 발급은 규칙 Row를 {@code FOR UPDATE}로 잠근 동일 DB Transaction 안에서
 * 증가/이력 기록하여 동일 JVM뿐 아니라 다중 인스턴스에서도 중복 번호를 방지합니다.</p>
 */
@Repository
public class BzaSequenceSampleRepository {
    private final ObjectProvider<NamedParameterJdbcTemplate> jdbcTemplateProvider;

    public BzaSequenceSampleRepository(
            @Qualifier("bzaJdbcTemplate") ObjectProvider<NamedParameterJdbcTemplate> jdbcTemplateProvider) {
        this.jdbcTemplateProvider = jdbcTemplateProvider;
    }

    public List<Map<String, Object>> rules() {
        return jdbc().queryForList("""
                SELECT rule_code AS ruleCode, rule_name AS ruleName, prefix,
                       current_value AS currentValue, padding_length AS paddingLength,
                       use_yn AS useYn, updated_at AS updatedAt
                  FROM bza_sequence_sample_rule
                 ORDER BY rule_code
                """, Map.of());
    }

    public Map<String, Object> rule(String code) {
        return findRule(code, false);
    }

    public Map<String, Object> ruleForUpdate(String code) {
        return findRule(code, true);
    }

    private Map<String, Object> findRule(String code, boolean forUpdate) {
        String sql = """
                SELECT rule_code AS ruleCode, rule_name AS ruleName, prefix,
                       current_value AS currentValue, padding_length AS paddingLength, use_yn AS useYn
                  FROM bza_sequence_sample_rule
                 WHERE rule_code=:ruleCode
                """ + (forUpdate ? " FOR UPDATE" : "");
        return jdbc().queryForList(sql, Map.of("ruleCode", code)).stream().findFirst()
                .orElseThrow(() -> new IllegalArgumentException("채번 Sample 규칙을 찾을 수 없습니다."));
    }

    public void save(String code, String name, String prefix, long current, int padding, String useYn, String user) {
        MapSqlParameterSource p = new MapSqlParameterSource()
                .addValue("ruleCode", code).addValue("ruleName", name).addValue("prefix", prefix)
                .addValue("currentValue", current).addValue("paddingLength", padding)
                .addValue("useYn", useYn).addValue("operatorId", user);
        int updated = jdbc().update("""
                UPDATE bza_sequence_sample_rule
                   SET rule_name=:ruleName, prefix=:prefix, padding_length=:paddingLength,
                       use_yn=:useYn, updated_by=:operatorId, updated_at=CURRENT_TIMESTAMP(3)
                 WHERE rule_code=:ruleCode
                """, p);
        if (updated == 0) {
            jdbc().update("""
                    INSERT INTO bza_sequence_sample_rule(
                        rule_code, rule_name, prefix, current_value, padding_length, use_yn,
                        created_by, updated_by
                    ) VALUES(
                        :ruleCode, :ruleName, :prefix, :currentValue, :paddingLength, :useYn,
                        :operatorId, :operatorId
                    )
                    """, p);
        }
    }

    public void updateCurrentValue(String code, long current, String operator) {
        int updated = jdbc().update("""
                UPDATE bza_sequence_sample_rule
                   SET current_value=:currentValue, updated_by=:operatorId, updated_at=CURRENT_TIMESTAMP(3)
                 WHERE rule_code=:ruleCode
                """, new MapSqlParameterSource()
                .addValue("ruleCode", code).addValue("currentValue", current).addValue("operatorId", operator));
        if (updated != 1) {
            throw new IllegalStateException("채번 Sample 규칙 증가에 실패했습니다: " + code);
        }
    }

    public void appendIssue(String code, String issued, String operator, String reason) {
        jdbc().update("""
                INSERT INTO bza_sequence_sample_issue(
                    issue_id, rule_code, issued_value, operator_id, reason, issued_at
                ) VALUES(
                    :issueId, :ruleCode, :issuedValue, :operatorId, :reason, CURRENT_TIMESTAMP(3)
                )
                """, new MapSqlParameterSource()
                .addValue("issueId", UUID.randomUUID().toString())
                .addValue("ruleCode", code).addValue("issuedValue", issued)
                .addValue("operatorId", operator).addValue("reason", reason));
    }

    public List<Map<String, Object>> history(String code, int limit) {
        int bounded = Math.max(1, Math.min(limit, 500));
        // LIMIT은 선택형 MariaDB Sample Pack에서만 사용합니다. Platform DB multi-vendor 정본에는 포함되지 않습니다.
        return jdbc().queryForList("""
                SELECT issue_id AS issueId, rule_code AS ruleCode, issued_value AS issuedValue,
                       operator_id AS operatorId, reason, issued_at AS issuedAt
                  FROM bza_sequence_sample_issue
                 WHERE (:ruleCode IS NULL OR rule_code=:ruleCode)
                 ORDER BY issued_at DESC
                 LIMIT :limit
                """, new MapSqlParameterSource().addValue("ruleCode", blankToNull(code)).addValue("limit", bounded));
    }

    private NamedParameterJdbcTemplate jdbc() {
        NamedParameterJdbcTemplate jdbc = jdbcTemplateProvider.getIfAvailable();
        if (jdbc == null) throw new IllegalStateException("BZA datasource가 활성화되지 않았습니다.");
        return jdbc;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
