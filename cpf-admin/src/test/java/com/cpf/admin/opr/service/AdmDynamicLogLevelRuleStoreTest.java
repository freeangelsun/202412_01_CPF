package com.cpf.admin.opr.service;

import com.cpf.core.api.error.CpfBusinessException;
import com.cpf.platform.operations.observability.api.logging.CpfLogLevel;
import com.cpf.platform.operations.observability.api.logging.DynamicLogLevelRule;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdmDynamicLogLevelRuleStoreTest {

    @Test
    void saveUsesVendorNeutralUpdateFirstFlow() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        DynamicLogLevelRule rule = rule();
        whenUpdateRule(jdbc, rule).thenReturn(1);
        AdmDynamicLogLevelRuleStore store = new AdmDynamicLogLevelRuleStore(jdbc);

        assertThatCode(() -> store.save(rule)).doesNotThrowAnyException();

        verify(jdbc).update(
                contains("UPDATE ADM_DYNAMIC_LOG_LEVEL_RULE"),
                eq(rule.transactionId()),
                eq(rule.businessTransactionId()),
                eq(rule.moduleId()),
                eq(rule.logLevel().name()),
                any(Timestamp.class),
                eq(rule.reason()),
                eq(rule.createdBy()),
                any(Timestamp.class),
                eq(rule.ruleId()));
    }

    @Test
    void saveFailsClosedWhenStoreIsUnavailable() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        DynamicLogLevelRule rule = rule();
        whenUpdateRule(jdbc, rule)
                .thenThrow(new DataAccessResourceFailureException("test database unavailable"));
        AdmDynamicLogLevelRuleStore store = new AdmDynamicLogLevelRuleStore(jdbc);

        assertThatThrownBy(() -> store.save(rule))
                .isInstanceOf(CpfBusinessException.class)
                .hasMessageContaining("동적 로그 레벨 저장소");
    }

    @Test
    @SuppressWarnings("unchecked")
    void findActiveRulesReturnsPersistedRules() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        DynamicLogLevelRule rule = rule();
        when(jdbc.query(anyString(), any(RowMapper.class))).thenReturn(List.of(rule));
        AdmDynamicLogLevelRuleStore store = new AdmDynamicLogLevelRuleStore(jdbc);

        assertThat(store.findActiveRules()).containsExactly(rule);
    }

    @Test
    @SuppressWarnings("unchecked")
    void findActiveRulesFailsClosedWhenStoreIsUnavailable() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        when(jdbc.query(anyString(), any(RowMapper.class)))
                .thenThrow(new DataAccessResourceFailureException("test database unavailable"));
        AdmDynamicLogLevelRuleStore store = new AdmDynamicLogLevelRuleStore(jdbc);

        assertThatThrownBy(store::findActiveRules)
                .isInstanceOf(CpfBusinessException.class)
                .hasMessageContaining("동적 로그 레벨 저장소");
    }

    @Test
    void disableDistinguishesMissingRuleFromStoreFailure() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        when(jdbc.update(
                contains("UPDATE ADM_DYNAMIC_LOG_LEVEL_RULE"),
                eq("tester"), eq("missing-rule")))
                .thenReturn(0);
        AdmDynamicLogLevelRuleStore store = new AdmDynamicLogLevelRuleStore(jdbc);

        assertThat(store.disable("missing-rule", "tester")).isFalse();

        when(jdbc.update(
                contains("UPDATE ADM_DYNAMIC_LOG_LEVEL_RULE"),
                eq("tester"), eq("unavailable-rule")))
                .thenThrow(new DataAccessResourceFailureException("test database unavailable"));

        assertThatThrownBy(() -> store.disable("unavailable-rule", "tester"))
                .isInstanceOf(CpfBusinessException.class)
                .hasMessageContaining("동적 로그 레벨 저장소");
    }

    @Test
    void persistenceStatusReportsExplicitUnavailableState() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        when(jdbc.queryForObject(anyString(), eq(Integer.class)))
                .thenThrow(new DataAccessResourceFailureException("secret connection detail"));
        AdmDynamicLogLevelRuleStore store = new AdmDynamicLogLevelRuleStore(jdbc);

        Map<String, Object> status = store.persistenceStatus();

        assertThat(status)
                .containsEntry("available", false)
                .containsEntry("activeCount", 0)
                .containsEntry("reason", "ADM_DYNAMIC_LOG_LEVEL_STORE_UNAVAILABLE")
                .containsEntry("failureType", "DataAccessResourceFailureException")
                .doesNotContainValue("secret connection detail");
    }

    private static org.mockito.stubbing.OngoingStubbing<Integer> whenUpdateRule(
            JdbcTemplate jdbc,
            DynamicLogLevelRule rule) {
        return when(jdbc.update(
                contains("UPDATE ADM_DYNAMIC_LOG_LEVEL_RULE"),
                eq(rule.transactionId()),
                eq(rule.businessTransactionId()),
                eq(rule.moduleId()),
                eq(rule.logLevel().name()),
                any(Timestamp.class),
                eq(rule.reason()),
                eq(rule.createdBy()),
                any(Timestamp.class),
                eq(rule.ruleId())));
    }

    private static DynamicLogLevelRule rule() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 7, 29, 10, 0);
        return new DynamicLogLevelRule(
                "rule-1",
                "tx-1",
                "OADMOP0021",
                "ADM",
                CpfLogLevel.DEBUG,
                "failure analysis",
                "tester",
                createdAt,
                createdAt.plusMinutes(10));
    }
}
