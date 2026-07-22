package com.cpf.admin.opr.service;

import com.cpf.admin.opr.dto.AdmLogPolicyOverrideRequest;
import com.cpf.admin.opr.dto.AdmLogPolicyRequest;
import com.cpf.core.common.exception.CpfValidationException;
import com.cpf.core.common.logging.policy.LogPolicyResolver;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class AdmLogPolicyServiceTest {

    @Test
    void createOverrideRejectsInvalidPeriodBeforeDbAccess() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        AdmLogPolicyService service = new AdmLogPolicyService(jdbcTemplate, emptyResolverProvider());
        AdmLogPolicyOverrideRequest request = new AdmLogPolicyOverrideRequest(
                1L,
                "ONLINE_TRANSACTION",
                "ADM01TRN0010",
                "DEBUG",
                null,
                null,
                null,
                null,
                null,
                null,
                LocalDateTime.now().plusHours(2),
                LocalDateTime.now().plusHours(1),
                null,
                "tester",
                "장애 분석을 위한 임시 로그 레벨 상향");

        assertThatThrownBy(() -> service.createOverride(request, "tester", "127.0.0.1"))
                .isInstanceOf(CpfValidationException.class)
                .hasMessageContaining("시작일시");

        verifyNoInteractions(jdbcTemplate);
    }

    @Test
    void createPolicyRejectsMissingReasonBeforeDbAccess() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        AdmLogPolicyService service = new AdmLogPolicyService(jdbcTemplate, emptyResolverProvider());
        AdmLogPolicyRequest request = new AdmLogPolicyRequest(
                "ONLINE_DEFAULT",
                "온라인 기본",
                "ONLINE_TRANSACTION",
                "*",
                "INFO",
                "Y",
                "Y",
                "N",
                "N",
                "Y",
                null,
                90,
                BigDecimal.valueOf(100),
                100,
                "Y",
                "테스트 정책",
                "tester",
                "");

        assertThatThrownBy(() -> service.createPolicy(request, "tester", "127.0.0.1"))
                .isInstanceOf(CpfValidationException.class)
                .hasMessageContaining("감사 사유");

        verifyNoInteractions(jdbcTemplate);
    }

    @Test
    void findPoliciesReportsUnavailableWhenCpfTableIsMissing() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq("cpf_log_policy")))
                .thenThrow(new DataAccessResourceFailureException("cpfDB 미적용"));
        AdmLogPolicyService service = new AdmLogPolicyService(jdbcTemplate, emptyResolverProvider());

        Map<String, Object> result = service.findPolicies("ONLINE_TRANSACTION", "*", "Y", 10);

        assertThat(result)
                .containsEntry("available", false)
                .containsEntry("items", java.util.List.of());
    }

    @Test
    @SuppressWarnings("unchecked")
    void clearCacheCallsResolver() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        ObjectProvider<LogPolicyResolver> resolverProvider = mock(ObjectProvider.class);
        LogPolicyResolver resolver = mock(LogPolicyResolver.class);
        when(resolverProvider.getIfAvailable()).thenReturn(resolver);
        when(resolver.cachedSize()).thenReturn(0);
        AdmLogPolicyService service = new AdmLogPolicyService(jdbcTemplate, resolverProvider);

        Map<String, Object> result = service.clearCache("운영 정책 재적용", "tester", "127.0.0.1");

        verify(resolver).clear();
        assertThat(result).containsEntry("cleared", true);
    }

    @SuppressWarnings("unchecked")
    private ObjectProvider<LogPolicyResolver> emptyResolverProvider() {
        ObjectProvider<LogPolicyResolver> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(null);
        return provider;
    }
}
