package cpf.pfw.common.idempotency;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JdbcCpfIdempotencyRepositoryTest {

    @Test
    void reserveReturnsFalseWhenUniqueKeyAlreadyExists() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.update(anyString(), any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new DuplicateKeyException("duplicate"));
        JdbcCpfIdempotencyRepository repository = new JdbcCpfIdempotencyRepository(jdbcTemplate);

        boolean reserved = repository.reserve(new CpfIdempotencyRecord(
                "HTTP",
                "idem-001",
                "request-hash",
                "payload-hash",
                "PROCESSING",
                null,
                false,
                Instant.now(),
                null,
                Instant.now().plusSeconds(60)));

        assertThat(reserved).isFalse();
    }

    @Test
    void findMapsStoredResponseAndRetryPolicy() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForList(anyString(), eq("BROKER"), eq("idem-002")))
                .thenReturn(List.of(Map.of(
                        "scope", "BROKER",
                        "idempotencyKey", "idem-002",
                        "requestHash", "req",
                        "payloadHash", "pay",
                        "recordStatus", "SUCCESS",
                        "storedResponse", "{\"ok\":true}",
                        "retryAllowedYn", "N",
                        "createdAt", Timestamp.from(Instant.parse("2026-07-10T01:00:00Z")))));
        JdbcCpfIdempotencyRepository repository = new JdbcCpfIdempotencyRepository(jdbcTemplate);

        CpfIdempotencyRecord record = repository.find("BROKER", "idem-002").orElseThrow();

        assertThat(record.status()).isEqualTo("SUCCESS");
        assertThat(record.storedResponse()).contains("ok");
        assertThat(record.retryAllowed()).isFalse();
        assertThat(record.sameRequest("req")).isTrue();
    }

    @Test
    void completeStoresResponseAndStatus() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        JdbcCpfIdempotencyRepository repository = new JdbcCpfIdempotencyRepository(jdbcTemplate);

        repository.complete("FILE", "idem-003", "success", "DONE", false);

        verify(jdbcTemplate).update(anyString(), any(), any(), any(), any(), any());
    }

    @Test
    void restartUsesConditionalAtomicUpdate() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.update(anyString(), any(), any(), any(), any(), any()))
                .thenReturn(1);
        JdbcCpfIdempotencyRepository repository = new JdbcCpfIdempotencyRepository(jdbcTemplate);

        boolean restarted = repository.restart(
                "HTTP",
                "idem-004",
                "request-hash",
                "payload-hash",
                Instant.now().plusSeconds(60));

        assertThat(restarted).isTrue();
    }

    @Test
    void expireBeforeLimitsCleanupBatch() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.update(anyString(), any(), eq(1000))).thenReturn(3);
        JdbcCpfIdempotencyRepository repository = new JdbcCpfIdempotencyRepository(jdbcTemplate);

        assertThat(repository.expireBefore(Instant.now(), 5000)).isEqualTo(3);
    }
}
