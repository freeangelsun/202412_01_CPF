package cpf.pfw.common.broker;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JdbcCpfBrokerReliabilityRepositoryTest {

    @Test
    void saveOutboxWritesEnvelopeAsPersistentRecord() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        JdbcCpfBrokerReliabilityRepository repository = new JdbcCpfBrokerReliabilityRepository(jdbcTemplate);

        CpfBrokerResult result = repository.saveOutbox(envelope("msg-001", "idem-001"));

        assertThat(result.status()).isEqualTo("ACCEPTED");
        verify(jdbcTemplate).update(anyString(),
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void claimPendingMapsRowsAndClaimsWorker() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForList(anyString(), anyInt()))
                .thenReturn(List.of(claimedOutboxRow()));
        when(jdbcTemplate.update(anyString(), eq("worker-1"), eq("msg-002"))).thenReturn(1);
        JdbcCpfBrokerReliabilityRepository repository = new JdbcCpfBrokerReliabilityRepository(jdbcTemplate);

        List<CpfBrokerEnvelope> claimed = repository.claimPending("worker-1", 10);

        assertThat(claimed).hasSize(1);
        assertThat(claimed.get(0).message().messageId()).isEqualTo("msg-002");
        assertThat(claimed.get(0).attributes()).containsEntry("a", "b");
        verify(jdbcTemplate).update(anyString(), eq("worker-1"), eq("msg-002"));
    }

    @Test
    void inboxDuplicateReturnsFalse() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.update(anyString(), eq("msg-003"), eq("idem-003")))
                .thenThrow(new DuplicateKeyException("duplicate"));
        JdbcCpfBrokerReliabilityRepository repository = new JdbcCpfBrokerReliabilityRepository(jdbcTemplate);

        assertThat(repository.markReceived("msg-003", "idem-003")).isFalse();
    }

    @Test
    void replayMarksDlqAsRequested() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.update(anyString(), eq("msg-004"))).thenReturn(1);
        JdbcCpfBrokerReliabilityRepository repository = new JdbcCpfBrokerReliabilityRepository(jdbcTemplate);

        CpfBrokerResult result = repository.replay("msg-004");

        assertThat(result.status()).isEqualTo("ACCEPTED");
        verify(jdbcTemplate, org.mockito.Mockito.times(3)).update(anyString(), eq("msg-004"));
    }

    private Map<String, Object> claimedOutboxRow() {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("messageId", "msg-002");
        row.put("topic", "cpf.topic");
        row.put("messageKey", "key-1");
        row.put("transactionGlobalId", "202607100001");
        row.put("segmentId", "SEG-1");
        row.put("producerModule", "XYZ");
        row.put("consumerModule", "MBR");
        row.put("idempotencyKey", "idem-002");
        row.put("payload", "hello".getBytes(StandardCharsets.UTF_8));
        row.put("contentType", "text/plain");
        row.put("headerJson", "h=v\n");
        row.put("attributeJson", "a=b\n");
        row.put("occurredAt", Timestamp.from(Instant.parse("2026-07-10T01:00:00Z")));
        return row;
    }

    private CpfBrokerEnvelope envelope(String messageId, String idempotencyKey) {
        CpfBrokerMessage message = new CpfBrokerMessage(
                messageId,
                "cpf.topic",
                "key-1",
                "payload".getBytes(StandardCharsets.UTF_8),
                "text/plain",
                Map.of("trace", "T1"));
        return new CpfBrokerEnvelope(
                "202607100001",
                "SEG-1",
                "XYZ",
                "MBR",
                idempotencyKey,
                Instant.parse("2026-07-10T01:00:00Z"),
                message,
                Map.of("businessKey", "BIZ-1"));
    }
}
