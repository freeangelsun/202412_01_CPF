package com.cpf.starter.messaging.reliability.jdbc.internal;

import com.cpf.core.common.broker.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;

class JdbcCpfBrokerReliabilityRepositoryQa39Test {
    private static final Instant NOW = Instant.parse("2026-08-02T00:00:00Z");

    @Test
    void identicalMessageIdIsIdempotentWithoutMutation() {
        FakeJdbcTemplate jdbc = new FakeJdbcTemplate();
        jdbc.rows = List.of(row(envelope("topic", new byte[]{1, 2})));
        repository(jdbc).saveOutbox(envelope("topic", new byte[]{1, 2}));
        assertThat(jdbc.updates).isZero();
    }

    @Test
    void conflictingMessageIdFailsClosed() {
        FakeJdbcTemplate jdbc = new FakeJdbcTemplate();
        jdbc.rows = List.of(row(envelope("topic", new byte[]{1, 2})));
        assertThatThrownBy(() -> repository(jdbc).saveOutbox(
                envelope("other-topic", new byte[]{1, 2})))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("idempotency conflict");
    }

    @Test
    void concurrentSameInsertIsIdempotentButOtherUniqueConflictIsNotHidden() {
        FakeJdbcTemplate same = new FakeJdbcTemplate();
        same.sequence.add(List.of());
        same.sequence.add(List.of(row(envelope("topic", new byte[]{1, 2}))));
        same.duplicateInsert = true;
        repository(same).saveOutbox(envelope("topic", new byte[]{1, 2}));

        FakeJdbcTemplate otherUnique = new FakeJdbcTemplate();
        otherUnique.sequence.add(List.of());
        otherUnique.sequence.add(List.of());
        otherUnique.duplicateInsert = true;
        assertThatThrownBy(() -> repository(otherUnique).saveOutbox(
                envelope("topic", new byte[]{1, 2})))
                .isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    void expiredUnknownLeaseIsRecoveredBeforeClaim() {
        FakeJdbcTemplate jdbc = new FakeJdbcTemplate();
        jdbc.queryRows = List.of(row(envelope("topic", new byte[]{1, 2})));
        jdbc.updateResults.add(1);
        jdbc.updateResults.add(1);

        assertThat(repository(jdbc).claimUnknown("worker-1", 10)).hasSize(1);
        assertThat(jdbc.sqls.get(0))
                .contains("CLAIMED_UNKNOWN")
                .contains("lease_until <=");
        assertThat(jdbc.sqls.get(1)).contains("outbox_status = 'CLAIMED_UNKNOWN'");
    }

    @Test
    void definiteFailureFromReconcileReturnsToBoundedRetryStateMachine() {
        FakeJdbcTemplate jdbc = new FakeJdbcTemplate();
        jdbc.sequence.add(List.of(Map.of("attemptCount", 0, "maxAttempts", 3)));
        jdbc.sequence.add(List.of());
        jdbc.updateResults.add(1);

        repository(jdbc).markPublished("m1", new CpfBrokerResult(
                "FAILED", "m1", "KAFKA", null, NOW, "rejected"));

        assertThat(jdbc.arguments.getFirst()[1]).isEqualTo("PENDING");
        assertThat(jdbc.arguments.getFirst()[2]).isInstanceOf(Timestamp.class);
    }

    private static JdbcCpfBrokerReliabilityRepository repository(JdbcTemplate jdbc) {
        return new JdbcCpfBrokerReliabilityRepository(
                jdbc, Duration.ofSeconds(30), Clock.fixed(NOW, ZoneOffset.UTC));
    }

    private static CpfBrokerEnvelope envelope(String topic, byte[] payload) {
        return new CpfBrokerEnvelope(
                "tx", "seg", "PRD", "CON", "idem", NOW,
                new CpfBrokerMessage(
                        "m1", topic, "key", payload, "application/json",
                        Map.of("z", "9", "a", "1")),
                Map.of("b", "2", "a", "1"));
    }

    private static Map<String, Object> row(CpfBrokerEnvelope envelope) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("messageId", envelope.message().messageId());
        row.put("topic", envelope.message().topic());
        row.put("messageKey", envelope.message().key());
        row.put("transactionId", envelope.transactionId());
        row.put("segmentId", envelope.segmentId());
        row.put("producerModule", envelope.producerModule());
        row.put("consumerModule", envelope.consumerModule());
        row.put("idempotencyKey", envelope.idempotencyKey());
        row.put("payload", envelope.message().payload());
        row.put("contentType", envelope.message().contentType());
        row.put("headerJson", encode(envelope.message().headers()));
        row.put("attributeJson", encode(envelope.attributes()));
        row.put("occurredAt", Timestamp.from(envelope.occurredAt()));
        return row;
    }

    private static String encode(Map<String, String> values) {
        StringBuilder result = new StringBuilder("v2\n");
        values.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> result
                        .append(token(entry.getKey()))
                        .append('=')
                        .append(token(entry.getValue()))
                        .append('\n'));
        return result.toString();
    }

    private static String token(String value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(
                value.getBytes(StandardCharsets.UTF_8));
    }

    private static final class FakeJdbcTemplate extends JdbcTemplate {
        private List<Map<String, Object>> rows = List.of();
        private List<Map<String, Object>> queryRows = List.of();
        private final Deque<List<Map<String, Object>>> sequence = new ArrayDeque<>();
        private final Deque<Integer> updateResults = new ArrayDeque<>();
        private final List<String> sqls = new ArrayList<>();
        private final List<Object[]> arguments = new ArrayList<>();
        private boolean duplicateInsert;
        private int updates;

        @Override
        public List<Map<String, Object>> queryForList(String sql, Object... args) {
            return sequence.isEmpty() ? rows : sequence.removeFirst();
        }

        @Override
        public int update(String sql, Object... args) {
            updates++;
            sqls.add(sql);
            arguments.add(args);
            if (duplicateInsert && sql.contains("INSERT INTO cpf_broker_outbox")) {
                throw new DuplicateKeyException("concurrent insert");
            }
            return updateResults.isEmpty() ? 1 : updateResults.removeFirst();
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> List<T> query(
                PreparedStatementCreator creator, RowMapper<T> rowMapper) {
            return (List<T>) (List<?>) queryRows;
        }
    }
}
