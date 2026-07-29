package com.cpf.admin.opr.service;

import com.cpf.core.api.error.CpfBusinessException;
import com.cpf.core.api.error.CpfErrorCode;
import com.cpf.core.api.logging.DynamicLogLevelRule;
import com.cpf.core.api.logging.CpfLogLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
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
            LocalDateTime updatedAt = LocalDateTime.now();
            if (updateRule(rule, updatedAt) == 0) {
                try {
                    insertRule(rule, updatedAt);
                } catch (DuplicateKeyException concurrentInsert) {
                    if (updateRule(rule, updatedAt) == 0) {
                        throw concurrentInsert;
                    }
                }
            }
        } catch (DataAccessException ex) {
            throw unavailable("adm_dynamic_log_level_rule.save", ex);
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
        } catch (DataAccessException ex) {
            throw unavailable("adm_dynamic_log_level_rule.disable", ex);
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
        } catch (DataAccessException ex) {
            throw unavailable("adm_dynamic_log_level_rule.list", ex);
        }
    }

    public Map<String, Object> persistenceStatus() {
        try {
            Integer count = admJdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM adm_dynamic_log_level_rule WHERE USE_YN = 'Y' AND EXPIRE_AT > CURRENT_TIMESTAMP",
                    Integer.class);
            return Map.of("available", true, "activeCount", count == null ? 0 : count);
        } catch (DataAccessException ex) {
            log.warn("ADM dynamic log-level persistence unavailable. failureType={}",
                    ex.getClass().getSimpleName());
            return Map.of(
                    "available", false,
                    "activeCount", 0,
                    "reason", "ADM_DYNAMIC_LOG_LEVEL_STORE_UNAVAILABLE",
                    "failureType", ex.getClass().getSimpleName());
        }
    }

    private CpfBusinessException unavailable(String component, DataAccessException ex) {
        return new CpfBusinessException(
                CpfErrorCode.INFRASTRUCTURE_UNAVAILABLE,
                "ADM 동적 로그 레벨 저장소를 사용할 수 없습니다.",
                Map.of("0", component, "1", ex.getClass().getSimpleName()));
    }

    private int updateRule(DynamicLogLevelRule rule, LocalDateTime updatedAt) {
        return admJdbcTemplate.update("""
                UPDATE adm_dynamic_log_level_rule
                SET TRANSACTION_ID = ?,
                    BUSINESS_TRANSACTION_ID = ?,
                    MODULE_ID = ?,
                    LOG_LEVEL = ?,
                    EXPIRE_AT = ?,
                    REASON = ?,
                    USE_YN = 'Y',
                    UPDATED_BY = ?,
                    UPDATED_AT = ?
                WHERE RULE_ID = ?
                """,
                rule.transactionId(),
                rule.businessTransactionId(),
                rule.moduleId(),
                rule.logLevel().name(),
                Timestamp.valueOf(rule.expiresAt()),
                rule.reason(),
                rule.createdBy(),
                Timestamp.valueOf(updatedAt),
                rule.ruleId());
    }

    private void insertRule(DynamicLogLevelRule rule, LocalDateTime updatedAt) {
        admJdbcTemplate.update("""
                INSERT INTO adm_dynamic_log_level_rule (
                    RULE_ID, TRANSACTION_ID, BUSINESS_TRANSACTION_ID, MODULE_ID, LOG_LEVEL,
                    EXPIRE_AT, REASON, USE_YN, CREATED_BY, CREATED_AT, UPDATED_BY, UPDATED_AT
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, 'Y', ?, ?, ?, ?)
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
                Timestamp.valueOf(updatedAt));
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
