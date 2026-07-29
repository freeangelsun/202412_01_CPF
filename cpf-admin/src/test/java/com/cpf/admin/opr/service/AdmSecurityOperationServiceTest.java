package com.cpf.admin.opr.service;

import com.cpf.admin.opr.dto.AdmIpAllowlistRequest;
import com.cpf.admin.opr.dto.AdmMfaOtpRequest;
import com.cpf.core.api.error.CpfBusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdmSecurityOperationServiceTest {

    @Test
    void findIpAllowlistReturnsPersistedRows() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        List<Map<String, Object>> rows = List.of(Map.of(
                "ALLOW_ID", 1L,
                "IP_PATTERN", "10.0.0.0/8",
                "USE_YN", "Y"));
        when(jdbc.queryForList(contains("FROM adm_ip_allowlist"))).thenReturn(rows);

        AdmSecurityOperationService service = new AdmSecurityOperationService(jdbc);

        assertThat(service.findIpAllowlist()).isSameAs(rows);
    }

    @Test
    void findIpAllowlistFailsClosedWhenStoreIsUnavailable() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        when(jdbc.queryForList(contains("FROM adm_ip_allowlist")))
                .thenThrow(new DataAccessResourceFailureException("test database unavailable"));

        AdmSecurityOperationService service = new AdmSecurityOperationService(jdbc);

        assertThatThrownBy(service::findIpAllowlist)
                .isInstanceOf(CpfBusinessException.class)
                .hasMessageContaining("보안 운영 저장소");
    }

    @Test
    void findMfaStatesFailsClosedWhenStoreIsUnavailable() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        when(jdbc.queryForList(contains("FROM adm_mfa_otp_secret")))
                .thenThrow(new DataAccessResourceFailureException("test database unavailable"));

        AdmSecurityOperationService service = new AdmSecurityOperationService(jdbc);

        assertThatThrownBy(service::findMfaStates)
                .isInstanceOf(CpfBusinessException.class)
                .hasMessageContaining("보안 운영 저장소");
    }

    @Test
    void upsertIpAllowlistUsesVendorNeutralUpdateFirstFlow() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        when(jdbc.update(
                contains("UPDATE adm_ip_allowlist"),
                eq("office network"), eq("Y"), eq("tester"), eq("10.0.0.0/8")))
                .thenReturn(1);
        Map<String, Object> persisted = Map.of(
                "ALLOW_ID", 1L,
                "IP_PATTERN", "10.0.0.0/8",
                "USE_YN", "Y");
        when(jdbc.queryForMap(anyString(), eq("10.0.0.0/8"))).thenReturn(persisted);
        AdmSecurityOperationService service = new AdmSecurityOperationService(jdbc);

        Map<String, Object> result = service.upsertIpAllowlist(new AdmIpAllowlistRequest(
                " 10.0.0.0/8 ", "office network", "Y", "tester", "test"));

        assertThat(result).isSameAs(persisted);
        verify(jdbc).update(
                contains("UPDATE adm_ip_allowlist"),
                eq("office network"), eq("Y"), eq("tester"), eq("10.0.0.0/8"));
    }

    @Test
    void registerMfaRetriesUpdateAfterConcurrentInsert() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        when(jdbc.update(
                contains("UPDATE adm_mfa_otp_secret"),
                eq("vault://otp/operator-1"), eq("tester"), eq("operator-1")))
                .thenReturn(0, 1);
        when(jdbc.update(
                contains("INSERT INTO adm_mfa_otp_secret"),
                eq("operator-1"), eq("vault://otp/operator-1"), eq("tester"), eq("tester")))
                .thenThrow(new DuplicateKeyException("concurrent insert"));
        Map<String, Object> persisted = Map.of(
                "OPERATOR_ID", "operator-1",
                "SECRET_REF", "vault://otp/operator-1",
                "ENABLED_YN", "N");
        when(jdbc.queryForMap(anyString(), eq("operator-1"))).thenReturn(persisted);
        AdmSecurityOperationService service = new AdmSecurityOperationService(jdbc);

        Map<String, Object> result = service.registerMfa(
                "operator-1",
                new AdmMfaOtpRequest("vault://otp/operator-1", null, "tester", "test"));

        assertThat(result).isSameAs(persisted);
        verify(jdbc, times(2)).update(
                contains("UPDATE adm_mfa_otp_secret"),
                eq("vault://otp/operator-1"), eq("tester"), eq("operator-1"));
    }
}
