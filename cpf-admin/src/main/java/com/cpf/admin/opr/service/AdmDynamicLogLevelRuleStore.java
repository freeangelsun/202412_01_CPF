package cpf.adm.opr.service;

import cpf.pfw.common.logging.DynamicLogLevelRule;
import cpf.pfw.common.logging.CpfLogLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class AdmDynamicLogLevelRuleStore {
    private static final Logger log = LoggerFactory.getLogger(AdmDynamicLogLevelRuleStore.class);

    private final JdbcTemplate admJdbcTemplate;

    public AdmDynamicLogLevelRuleStore(@Qualifier("admJdbcTemplate") JdbcTemplate admJdbcTemplate) {
        this.admJdbcTemplate = admJdbcTemplate;
    }

    public void save(DynamicLogLevelRule rule) {
        try {
            admJdbcTemplate.update("""
                    INSERT INTO adm_dynamic_log_level_rule (
                        RULE_ID, TRANSACTION_ID, BUSINESS_TRANSACTION_ID, MODULE_ID, LOG_LEVEL,
                        EXPIRE_AT, REASON, USE_YN, CREATED_BY, CREATED_AT, UPDATED_BY, UPDATED_AT
                    )
                    VALUES (?, ?, ?, ?, ?, ?, ?, 'Y', ?, ?, ?, ?)
                    ON DUPLICATE KEY UPDATE
                        TRANSACTION_ID = VALUES(TRANSACTION_ID),
                        BUSINESS_TRANSACTION_ID = VALUES(BUSINESS_TRANSACTION_ID),
                        MODULE_ID = VALUES(MODULE_ID),
                        LOG_LEVEL = VALUES(LOG_LEVEL),
                        EXPIRE_AT = VALUES(EXPIRE_AT),
                        REASON = VALUES(REASON),
                        USE_YN = 'Y',
                        UPDATED_BY = VALUES(UPDATED_BY),
                        UPDATED_AT = VALUES(UPDATED_AT)
                    """,
                    rule.ruleId(),
                    rule.transactionId(),
                    rule.businessTransactionId(),
                    rule.moduleId(),
                    rule.logLevel().name(),
                    Timestamp.valueOf(rule.expiresAt()),
                    rule.reason(),
                    rule.createdBy(),
                    Timestamp.valueOf(rule.createdAt()),
                    rule.createdBy(),
                    Timestamp.valueOf(LocalDateTime.now()));
        } catch (Exception ex) {
            log.warn("Failed to persist dynamic log-level rule. ruleId={}, message={}", rule.ruleId(), ex.getMessage());
        }
    }

    public boolean disable(String ruleId, String requestUser) {
        try {
            return admJdbcTemplate.update("""
                    UPDATE adm_dynamic_log_level_rule
                       SET USE_YN = 'N',
                           UPDATED_BY = ?,
                           UPDATED_AT = CURRENT_TIMESTAMP
                     WHERE RULE_ID = ?
                    """, requestUser, ruleId) > 0;
        } catch (Exception ex) {
            log.warn("Failed to disable dynamic log-level rule. ruleId={}, message={}", ruleId, ex.getMessage());
            return false;
        }
    }

    public List<DynamicLogLevelRule> findActiveRules() {
        try {
            return admJdbcTemplate.query("""
                    SELECT RULE_ID, TRANSACTION_ID, BUSINESS_TRANSACTION_ID, MODULE_ID, LOG_LEVEL,
                           REASON, CREATED_BY, CREATED_AT, EXPIRE_AT
                      FROM adm_dynamic_log_level_rule
                     WHERE USE_YN = 'Y'
                       AND EXPIRE_AT > CURRENT_TIMESTAMP
                     ORDER BY CREATED_AT DESC
                    """, (rs, rowNum) -> new DynamicLogLevelRule(
                    rs.getString("RULE_ID"),
                    rs.getString("TRANSACTION_ID"),
                    rs.getString("BUSINESS_TRANSACTION_ID"),
                    rs.getString("MODULE_ID"),
                    CpfLogLevel.valueOf(rs.getString("LOG_LEVEL")),
                    rs.getString("REASON"),
                    rs.getString("CREATED_BY"),
                    toLocalDateTime(rs.getTimestamp("CREATED_AT")),
                    toLocalDateTime(rs.getTimestamp("EXPIRE_AT"))));
        } catch (Exception ex) {
            log.warn("Failed to query dynamic log-level rules. message={}", ex.getMessage());
            return List.of();
        }
    }

    public Map<String, Object> persistenceStatus() {
        try {
            Integer count = admJdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM adm_dynamic_log_level_rule WHERE USE_YN = 'Y' AND EXPIRE_AT > CURRENT_TIMESTAMP",
                    Integer.class);
            return Map.of("available", true, "activeCount", count == null ? 0 : count);
        } catch (Exception ex) {
            return Map.of("available", false, "message", ex.getMessage());
        }
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
