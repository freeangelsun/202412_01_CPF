package com.cpf.messaging.reliability.api.jdbc.internal;

import com.cpf.messaging.spi.broker.*;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class JdbcCpfBrokerReliabilityRepositoryTest {
    @Test
    void advertisesOnlyTheFencedMutationsImplementedByThisAdapter() {
        JdbcCpfBrokerReliabilityRepository repository =
                new JdbcCpfBrokerReliabilityRepository(mock(JdbcTemplate.class));

        assertThat(repository.supportsFencedUnknownMutation()).isTrue();
        assertThat(repository.supportsFencedPublishMutation()).isTrue();
    }


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
    void claimPendingMapsRowsAndClaimsWorkerWithPortableStatementLimit() throws Exception {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.query(
                any(PreparedStatementCreator.class),
                any(ColumnMapRowMapper.class)))
                .thenReturn(List.of(claimedOutboxRow()));
        when(jdbcTemplate.update(
                anyString(),
                eq("worker-1"),
                any(Timestamp.class),
                any(Timestamp.class),
                eq("msg-002"),
                any(Timestamp.class))).thenReturn(1);
        JdbcCpfBrokerReliabilityRepository repository = new JdbcCpfBrokerReliabilityRepository(jdbcTemplate);

        List<CpfBrokerEnvelope> claimed = repository.claimPending("worker-1", 10);

        assertThat(claimed).hasSize(1);
        assertThat(claimed.get(0).message().messageId()).isEqualTo("msg-002");
        assertThat(claimed.get(0).attributes()).containsEntry("a", "b");
        verify(jdbcTemplate).update(
                anyString(),
                eq("worker-1"),
                any(Timestamp.class),
                any(Timestamp.class),
                eq("msg-002"),
                any(Timestamp.class));

        ArgumentCaptor<PreparedStatementCreator> creator =
                ArgumentCaptor.forClass(PreparedStatementCreator.class);
        verify(jdbcTemplate).query(creator.capture(), any(ColumnMapRowMapper.class));
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        creator.getValue().createPreparedStatement(connection);
        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        verify(connection).prepareStatement(sql.capture());
        assertThat(sql.getValue().toUpperCase(Locale.ROOT))
                .doesNotContain(" LIMIT ", "TIMESTAMPADD", "ON DUPLICATE KEY");
        verify(statement).setMaxRows(10);
    }

    @Test
    void inboxDuplicateReturnsFalse() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.update(
                anyString(), eq("CPF_DEFAULT_CONSUMER"), eq("msg-003"), eq("idem-003")))
                .thenThrow(new DuplicateKeyException("duplicate"));
        JdbcCpfBrokerReliabilityRepository repository = new JdbcCpfBrokerReliabilityRepository(jdbcTemplate);

        assertThat(repository.markReceived("msg-003", "idem-003")).isFalse();
    }

    @Test
    void directReplayFailsClosedWithoutDatabaseMutation() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        JdbcCpfBrokerReliabilityRepository repository = new JdbcCpfBrokerReliabilityRepository(jdbcTemplate);

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> repository.replay("msg-004"))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("approved owner command");

        verifyNoInteractions(jdbcTemplate);
    }

    @Test
    void directRangeReplayFailsClosedAndValidatesBoundaryFirst() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        JdbcCpfBrokerReliabilityRepository repository = new JdbcCpfBrokerReliabilityRepository(jdbcTemplate);
        Instant now = Instant.parse("2026-08-05T00:00:00Z");

        org.assertj.core.api.Assertions.assertThatThrownBy(
                        () -> repository.replayRange("topic", now, now.minusSeconds(1), 10))
                .isInstanceOf(IllegalArgumentException.class);
        org.assertj.core.api.Assertions.assertThatThrownBy(
                        () -> repository.replayRange("topic", null, null, 10))
                .isInstanceOf(SecurityException.class);

        verifyNoInteractions(jdbcTemplate);
    }

    @Test
    void replayMethodsDeclareTransactionBoundary() throws Exception {
        assertThat(JdbcCpfBrokerReliabilityRepository.class
                .getMethod("replay", String.class)
                .isAnnotationPresent(org.springframework.transaction.annotation.Transactional.class))
                .isTrue();
        assertThat(JdbcCpfBrokerReliabilityRepository.class
                .getMethod("replayRange", String.class, Instant.class, Instant.class, int.class)
                .isAnnotationPresent(org.springframework.transaction.annotation.Transactional.class))
                .isTrue();
    }

    @Test
    void defaultDlqTransitionDelegatesToDefaultConsumerInsideTransactionBoundary() throws Exception {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        CpfBrokerEnvelope envelope = envelope("msg-default-dlq", "idem-default-dlq");
        CpfBrokerResult expected = CpfBrokerResult.failed("msg-default-dlq", "CPF_DLQ", "failure");
        class CapturingRepository extends JdbcCpfBrokerReliabilityRepository {
            private String consumerIdentity;
            private CpfBrokerEnvelope capturedEnvelope;
            private String reason;

            CapturingRepository() {
                super(jdbcTemplate);
            }

            @Override
            public CpfBrokerResult moveToDlq(
                    String consumerIdentity, CpfBrokerEnvelope envelope, String reason) {
                this.consumerIdentity = consumerIdentity;
                this.capturedEnvelope = envelope;
                this.reason = reason;
                return expected;
            }
        }
        CapturingRepository repository = new CapturingRepository();

        assertThat(repository.moveToDlq(envelope, "failure")).isSameAs(expected);
        assertThat(repository.consumerIdentity).isEqualTo("CPF_DEFAULT_CONSUMER");
        assertThat(repository.capturedEnvelope).isSameAs(envelope);
        assertThat(repository.reason).isEqualTo("failure");
        assertThat(JdbcCpfBrokerReliabilityRepository.class
                .getMethod("moveToDlq", CpfBrokerEnvelope.class, String.class)
                .isAnnotationPresent(org.springframework.transaction.annotation.Transactional.class))
                .isTrue();
        verifyNoInteractions(jdbcTemplate);
    }

    private Map<String, Object> claimedOutboxRow() {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("messageId", "msg-002");
        row.put("topic", "cpf.topic");
        row.put("messageKey", "key-1");
        row.put("transactionId", "202607100001");
        row.put("segmentId", "SEG-1");
        row.put("producerModule", "REF");
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
                "REF",
                "MBR",
                idempotencyKey,
                Instant.parse("2026-07-10T01:00:00Z"),
                message,
                Map.of("businessKey", "BIZ-1"));
    }
}
