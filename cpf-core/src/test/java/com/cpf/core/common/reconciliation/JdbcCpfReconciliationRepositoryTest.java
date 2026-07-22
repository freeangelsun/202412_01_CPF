package cpf.pfw.common.reconciliation;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JdbcCpfReconciliationRepositoryTest {

    @Test
    void registerGeneratesUnknownIdAndPersistsRecord() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        JdbcCpfReconciliationRepository repository = new JdbcCpfReconciliationRepository(jdbcTemplate);

        CpfUnknownResultRecord saved = repository.register(new CpfUnknownResultRecord(
                null,
                "SERVICE_CALL",
                "CHECK_PENDING",
                "202607100001",
                "SEG-1",
                "EXT-1",
                "TIMEOUT",
                "응답 확인 필요",
                "RECONCILE",
                Instant.parse("2026-07-10T01:00:00Z"),
                null));

        assertThat(saved.unknownId()).startsWith("UNK-");
        verify(jdbcTemplate).update(anyString(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void findMapsUnknownResultRows() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForList(anyString(), eq("BROKER"), eq("BROKER"), eq("MANUAL_REVIEW"), eq("MANUAL_REVIEW"), eq(20)))
                .thenReturn(List.of(Map.of(
                        "unknownId", "UNK-1",
                        "unknownType", "BROKER",
                        "unknownStatus", "MANUAL_REVIEW",
                        "transactionGlobalId", "202607100001",
                        "segmentId", "SEG-1",
                        "externalKey", "MSG-1",
                        "failureCode", "UNKNOWN",
                        "failureMessage", "결과 불명",
                        "nextAction", "MANUAL",
                        "detectedAt", Timestamp.from(Instant.parse("2026-07-10T01:00:00Z")))));
        JdbcCpfReconciliationRepository repository = new JdbcCpfReconciliationRepository(jdbcTemplate);

        List<CpfUnknownResultRecord> records = repository.find("BROKER", "MANUAL_REVIEW", 20);

        assertThat(records).hasSize(1);
        assertThat(records.get(0).unknownStatus()).isEqualTo("MANUAL_REVIEW");
    }

    @Test
    void resolveRequiresAuditReason() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        JdbcCpfReconciliationRepository repository = new JdbcCpfReconciliationRepository(jdbcTemplate);

        assertThatThrownBy(() -> repository.resolve("UNK-1", "RESOLVED", "operator", " "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("auditReason");
    }
}
