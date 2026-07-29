package com.cpf.batch.control.compat;

import com.cpf.core.api.database.CpfVendorSqlCatalog;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BatCenterCutOperationsServiceTest {
    @Test
    void targetPayloadIsNeverLoadedAndSensitiveErrorTextIsMasked() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        BatCenterCutOperationsService service = service(jdbc);
        LinkedHashMap<String, Object> row = new LinkedHashMap<>();
        // Oracle처럼 unquoted alias를 대문자로 반환하는 Driver에서도 API key는 동일해야 합니다.
        row.put("TARGETID", 7L);
        row.put("LASTERRORMESSAGE", "token=plain-secret");
        row.put("TARGETPAYLOADLENGTH", 321L);
        when(jdbc.queryForList(
                "centercut-operations-find-targets",
                "CC-JOB",
                null,
                null,
                100))
                .thenReturn(List.of(row));

        List<Map<String, Object>> results =
                service.findTargets("CC-JOB", " ", 100);

        assertThat(results).singleElement().satisfies(result -> {
            assertThat(result).doesNotContainKeys("itemPayload", "targetPayload");
            assertThat(result.get("targetId")).isEqualTo(7L);
            assertThat(result.get("targetPayloadLength")).isEqualTo(321L);
            assertThat(result.get("targetPayloadMasked"))
                    .isEqualTo("[MASKED payload length=321]");
            assertThat(result.get("lastErrorMessage")).isEqualTo("token=***");
        });
    }

    @Test
    void databaseFailureIsNotConvertedToAnEmptyList() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        BatCenterCutOperationsService service = service(jdbc);
        when(jdbc.queryForList("centercut-operations-find-jobs"))
                .thenThrow(new DataAccessResourceFailureException("database unavailable"));

        assertThatThrownBy(service::findJobs)
                .isInstanceOf(DataAccessResourceFailureException.class)
                .hasMessageContaining("database unavailable");
    }

    @Test
    void missingJobReturnsExplicitEmptyDetailWithoutRunningChildQueries() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        BatCenterCutOperationsService service = service(jdbc);
        when(jdbc.queryForList(
                "centercut-operations-find-job-detail",
                "MISSING"))
                .thenReturn(List.of());

        assertThat(service.findJobDetail("MISSING")).isEmpty();

        verify(jdbc).queryForList(
                "centercut-operations-find-job-detail",
                "MISSING");
    }

    @Test
    void invalidResultIdAndLimitFailClosed() {
        BatCenterCutOperationsService service = service(mock(JdbcTemplate.class));

        assertThatThrownBy(() -> service.findResultDetail("not-a-number"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("positive number");
        assertThatThrownBy(() -> service.findResults("CC-JOB", null, 501))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("between 1 and 500");
    }

    private static BatCenterCutOperationsService service(JdbcTemplate jdbc) {
        CpfVendorSqlCatalog catalog = mock(CpfVendorSqlCatalog.class);
        when(catalog.required(any(String.class)))
                .thenAnswer(invocation -> invocation.getArgument(0, String.class));
        return new BatCenterCutOperationsService(jdbc, catalog);
    }
}
