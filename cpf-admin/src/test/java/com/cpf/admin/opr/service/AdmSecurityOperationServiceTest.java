package com.cpf.admin.opr.service;

import com.cpf.admin.opr.dto.AdmIpAllowlistRequest;
import com.cpf.admin.opr.dto.AdmMfaOtpRequest;
import com.cpf.core.api.error.CpfBusinessException;
import com.cpf.core.api.error.CpfValidationException;
import com.cpf.security.api.secret.CpfSecretMetadata;
import com.cpf.security.api.secret.CpfSecretProvider;
import com.cpf.security.api.secret.CpfSecretReference;
import com.cpf.security.api.secret.CpfSecretValue;
import com.cpf.admin.opr.security.AdmTotpVerifier;
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
        when(jdbc.queryForList(contains("FROM ADM_IP_ALLOWLIST"))).thenReturn(rows);

        AdmSecurityOperationService service = new AdmSecurityOperationService(jdbc);

        assertThat(service.findIpAllowlist()).isSameAs(rows);
    }

    @Test
    void findIpAllowlistFailsClosedWhenStoreIsUnavailable() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        when(jdbc.queryForList(contains("FROM ADM_IP_ALLOWLIST")))
                .thenThrow(new DataAccessResourceFailureException("test database unavailable"));

        AdmSecurityOperationService service = new AdmSecurityOperationService(jdbc);

        assertThatThrownBy(service::findIpAllowlist)
                .isInstanceOf(CpfBusinessException.class)
                .hasMessageContaining("보안 운영 저장소");
    }

    @Test
    void findMfaStatesFailsClosedWhenStoreIsUnavailable() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        when(jdbc.queryForList(contains("FROM ADM_MFA_OTP_SECRET")))
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
                contains("UPDATE ADM_IP_ALLOWLIST"),
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
                contains("UPDATE ADM_IP_ALLOWLIST"),
                eq("office network"), eq("Y"), eq("tester"), eq("10.0.0.0/8"));
    }

    @Test
    void registerMfaRetriesUpdateAfterConcurrentInsert() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        when(jdbc.update(
                contains("UPDATE ADM_MFA_OTP_SECRET"),
                eq("vault://otp/operator-1"), eq("tester"), eq("operator-1")))
                .thenReturn(0, 1);
        when(jdbc.update(
                contains("INSERT INTO ADM_MFA_OTP_SECRET"),
                eq("operator-1"), eq("vault://otp/operator-1"), eq("tester"), eq("tester")))
                .thenThrow(new DuplicateKeyException("concurrent insert"));
        Map<String, Object> persisted = Map.of(
                "OPERATOR_ID", "operator-1",
                "SECRET_REF", "vault://otp/operator-1",
                "ENABLED_YN", "N");
        when(jdbc.queryForMap(anyString(), eq("operator-1"))).thenReturn(persisted);
        CpfSecretProvider provider = provider("vault");
        AdmSecurityOperationService service = new AdmSecurityOperationService(jdbc, List.of(provider), new AdmTotpVerifier());

        Map<String, Object> result = service.registerMfa(
                "operator-1",
                new AdmMfaOtpRequest("vault://otp/operator-1", null, "tester", "test"));

        assertThat(result).isSameAs(persisted);
        verify(jdbc, times(2)).update(
                contains("UPDATE ADM_MFA_OTP_SECRET"),
                eq("vault://otp/operator-1"), eq("tester"), eq("operator-1"));
    }
    @Test
    void verifyMfaResolvesSecretAndRejectsInvalidOtpWithoutEnabling() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        when(jdbc.queryForMap(contains("FROM ADM_MFA_OTP_SECRET"), eq("operator-1"))).thenReturn(Map.of(
                "OPERATOR_ID", "operator-1", "SECRET_REF", "ENV:ADM_OTP", "ENABLED_YN", "N"));
        CpfSecretProvider provider = new CpfSecretProvider() {
            public String providerId() { return "ENV"; }
            public CpfSecretMetadata metadata(CpfSecretReference reference) { return null; }
            public CpfSecretValue resolve(CpfSecretReference reference) {
                return new CpfSecretValue("GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ".toCharArray());
            }
        };
        AdmTotpVerifier rejecting = mock(AdmTotpVerifier.class);
        when(rejecting.verify(org.mockito.ArgumentMatchers.any(CpfSecretValue.class), eq("000000"))).thenReturn(false);
        AdmSecurityOperationService service = new AdmSecurityOperationService(jdbc, List.of(provider), rejecting);

        assertThatThrownBy(() -> service.verifyMfa("operator-1",
                new AdmMfaOtpRequest(null, "000000", "tester", "verify")))
                .isInstanceOf(CpfValidationException.class)
                .hasMessageContaining("MFA 인증");
        verify(jdbc, org.mockito.Mockito.never()).update(contains("ENABLED_YN = 'Y'"),
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void loginRequiresOtpOnlyWhenMfaIsEnabled() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        when(jdbc.queryForMap(contains("FROM ADM_MFA_OTP_SECRET"), eq("operator-1"))).thenReturn(Map.of(
                "OPERATOR_ID", "operator-1", "SECRET_REF", "ENV:ADM_OTP", "ENABLED_YN", "Y"));
        CpfSecretProvider provider = new CpfSecretProvider() {
            public String providerId() { return "ENV"; }
            public CpfSecretMetadata metadata(CpfSecretReference reference) { return null; }
            public CpfSecretValue resolve(CpfSecretReference reference) {
                return new CpfSecretValue("GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ".toCharArray());
            }
        };
        AdmTotpVerifier verifier = mock(AdmTotpVerifier.class);
        when(verifier.verify(org.mockito.ArgumentMatchers.any(CpfSecretValue.class), eq("123456"))).thenReturn(true);
        AdmSecurityOperationService service = new AdmSecurityOperationService(jdbc, List.of(provider), verifier);

        service.requireMfaForLogin("operator-1", "123456");
        verify(verifier).verify(org.mockito.ArgumentMatchers.any(CpfSecretValue.class), eq("123456"));
    }

    private static CpfSecretProvider provider(String providerId) {
        return new CpfSecretProvider() {
            public String providerId() { return providerId; }
            public CpfSecretMetadata metadata(CpfSecretReference reference) { return null; }
            public CpfSecretValue resolve(CpfSecretReference reference) {
                return new CpfSecretValue("GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ".toCharArray());
            }
        };
    }

}
