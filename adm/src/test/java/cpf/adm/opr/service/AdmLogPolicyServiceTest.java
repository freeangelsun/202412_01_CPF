package cpf.adm.opr.service;

import cpf.adm.opr.dto.AdmLogPolicyOverrideRequest;
import cpf.adm.opr.dto.AdmLogPolicyRequest;
import cpf.pfw.common.exception.CpfValidationException;
import org.junit.jupiter.api.Test;
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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class AdmLogPolicyServiceTest {

    @Test
    void createOverrideRejectsInvalidPeriodBeforeDbAccess() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        AdmLogPolicyService service = new AdmLogPolicyService(jdbcTemplate);
        AdmLogPolicyOverrideRequest request = new AdmLogPolicyOverrideRequest(
                1L,
                "TRANSACTION",
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
        AdmLogPolicyService service = new AdmLogPolicyService(jdbcTemplate);
        AdmLogPolicyRequest request = new AdmLogPolicyRequest(
                "ONLINE_DEFAULT",
                "온라인 기본",
                "TRANSACTION",
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
    void findPoliciesReportsUnavailableWhenPfwTableIsMissing() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq("pfw_log_policy")))
                .thenThrow(new DataAccessResourceFailureException("pfwDB 미적용"));
        AdmLogPolicyService service = new AdmLogPolicyService(jdbcTemplate);

        Map<String, Object> result = service.findPolicies("TRANSACTION", "*", "Y", 10);

        assertThat(result)
                .containsEntry("available", false)
                .containsEntry("items", java.util.List.of());
    }
}
