package cpf.adm.opr.service;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class AdmObservabilityServiceTest {

    @Test
    void traceReturnsEmptyBucketsWhenSourceTablesAreMissing() {
        JdbcTemplate pfwJdbcTemplate = mock(JdbcTemplate.class);
        JdbcTemplate admJdbcTemplate = mock(JdbcTemplate.class);
        AdmObservabilityService service = new AdmObservabilityService(pfwJdbcTemplate, admJdbcTemplate);

        Map<String, Object> result = service.traceByBusinessTransactionId("ADM03LGP0014", 20);

        assertThat(result).containsEntry("available", true);
        assertThat(result.get("transactionLogs")).isEqualTo(List.of());
        assertThat(result.get("failureLogs")).isEqualTo(List.of());
        assertThat(result.get("auditLogs")).isEqualTo(List.of());
        assertThat(result.get("policyAuditLogs")).isEqualTo(List.of());
        assertThat(result.get("relatedBatchExecutions")).isEqualTo(List.of());
    }

    @Test
    void policyAuditQueryReportsUnavailableWhenTableIsMissing() {
        JdbcTemplate pfwJdbcTemplate = mock(JdbcTemplate.class);
        JdbcTemplate admJdbcTemplate = mock(JdbcTemplate.class);
        when(pfwJdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq("pfw_log_policy_audit")))
                .thenReturn(0);
        AdmObservabilityService service = new AdmObservabilityService(pfwJdbcTemplate, admJdbcTemplate);

        Map<String, Object> result = service.findPolicyAudits(
                null, null, "ONLINE_TRANSACTION", "ADM03LGP0014", null, null, 10);

        assertThat(result)
                .containsEntry("available", false)
                .containsEntry("items", List.of())
                .containsEntry("source", "pfw_log_policy_audit");
        verifyNoMoreInteractions(admJdbcTemplate);
    }
}
