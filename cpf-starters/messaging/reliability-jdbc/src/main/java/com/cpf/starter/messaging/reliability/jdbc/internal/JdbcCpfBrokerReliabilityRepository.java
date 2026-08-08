package com.cpf.starter.messaging.reliability.jdbc.internal;

import com.cpf.core.common.broker.*;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * CPF broker outbox/inbox/DLQ/replay 테이블을 사용하는 JDBC reference adapter입니다.
 *
 * <p>실 Kafka/MQ adapter가 없어도 업무 트랜잭션과 outbox 저장, 소비자 중복 방지, DLQ 이력,
 * 관리자 replay 요청을 DB 기준으로 검증할 수 있게 하는 최소 운영 저장소입니다.</p>
 */
public class JdbcCpfBrokerReliabilityRepository
        implements CpfBrokerOutboxPort, CpfBrokerUnknownResultPort,
        CpfBrokerInboxPort, CpfBrokerDlqPort, CpfBrokerFailureTransitionPort,
        CpfBrokerReplayPort, CpfBrokerIdempotencyPort {
    private final JdbcTemplate jdbcTemplate;
    private final Duration claimLease;
    private final Clock clock;

    public JdbcCpfBrokerReliabilityRepository(JdbcTemplate jdbcTemplate) {
        this(jdbcTemplate, Duration.ofSeconds(30), Clock.systemUTC());
    }

    public JdbcCpfBrokerReliabilityRepository(JdbcTemplate jdbcTemplate, Duration claimLease) {
        this(jdbcTemplate, claimLease, Clock.systemUTC());
    }

    public JdbcCpfBrokerReliabilityRepository(
            JdbcTemplate jdbcTemplate, Duration claimLease, Clock clock) {
        this.jdbcTemplate = java.util.Objects.requireNonNull(jdbcTemplate, "jdbcTemplate");
        if (claimLease == null || claimLease.isZero() || claimLease.isNegative()) {
            throw new IllegalArgumentException("claimLease must be positive");
        }
        this.claimLease = claimLease;
        this.clock = java.util.Objects.requireNonNull(clock, "clock");
    }


    @Override
    @Transactional(transactionManager = "cpfTransactionManager")
    public CpfBrokerResult saveOutbox(CpfBrokerEnvelope envelope) {
        java.util.Objects.requireNonNull(envelope, "envelope");
        java.util.Objects.requireNonNull(envelope.message(), "envelope.message");
        List<Map<String, Object>> existing = findOutboxIdentity(envelope.message().messageId());
        if (!existing.isEmpty()) {
            assertSameOutboxEnvelope(existing.getFirst(), envelope);
            return CpfBrokerResult.accepted(
                    envelope.message().messageId(), "CPF_OUTBOX", envelope.message().key());
        }
        try {
            insertOutbox(envelope);
        } catch (DuplicateKeyException duplicate) {
            // 동일 messageId의 동시 재요청만 멱등 성공으로 인정합니다.
            // idempotency_key 등 다른 unique key 충돌은 성공으로 숨기지 않습니다.
            existing = findOutboxIdentity(envelope.message().messageId());
            if (existing.isEmpty()) {
                throw duplicate;
            }
            assertSameOutboxEnvelope(existing.getFirst(), envelope);
        }
        return CpfBrokerResult.accepted(
                envelope.message().messageId(), "CPF_OUTBOX", envelope.message().key());
    }

    private List<Map<String, Object>> findOutboxIdentity(String messageId) {
        return jdbcTemplate.queryForList("""
                SELECT message_id AS messageId,
                       topic,
                       message_key AS messageKey,
                       transaction_id AS transactionId,
                       segment_id AS segmentId,
                       producer_module AS producerModule,
                       consumer_module AS consumerModule,
                       idempotency_key AS idempotencyKey,
                       payload,
                       content_type AS contentType,
                       header_json AS headerJson,
                       attribute_json AS attributeJson,
                       occurred_at AS occurredAt
                FROM cpf_broker_outbox
                WHERE message_id = ?
                """, messageId);
    }

    private void assertSameOutboxEnvelope(
            Map<String, Object> row, CpfBrokerEnvelope envelope) {
        CpfBrokerEnvelope stored = mapEnvelope(row);
        if (!CpfBrokerOutboxIdentity.same(stored, envelope)) {
            throw new IllegalStateException(
                    "Broker outbox messageId idempotency conflict: " + envelope.message().messageId());
        }
    }

    private void insertOutbox(CpfBrokerEnvelope envelope) {
        jdbcTemplate.update("""
                INSERT INTO cpf_broker_outbox (
                    message_id, topic, message_key, transaction_id, segment_id,
                    producer_module, consumer_module, idempotency_key, payload, content_type,
                    header_json, attribute_json, outbox_status, occurred_at, created_by, updated_by
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'PENDING', ?, 'CPF_BROKER', 'CPF_BROKER')
                """,
                envelope.message().messageId(),
                envelope.message().topic(),
                envelope.message().key(),
                envelope.transactionId(),
                envelope.segmentId(),
                envelope.producerModule(),
                envelope.consumerModule(),
                envelope.idempotencyKey(),
                envelope.message().payload(),
                envelope.message().contentType(),
                encodeMap(envelope.message().headers()),
                encodeMap(envelope.attributes()),
                Timestamp.from(envelope.occurredAt()));
    }

    @Override
    @Transactional(transactionManager = "cpfTransactionManager")
    public List<CpfBrokerEnvelope> claimPending(String workerId, int limit) {
        requireWorker(workerId);
        Instant now = Instant.now(clock);
        jdbcTemplate.update("""
                UPDATE cpf_broker_outbox
                SET outbox_status = 'UNKNOWN',
                    next_attempt_at = ?,
                    failure_message = 'Claim lease expired before durable provider outcome was recorded',
                    worker_id = NULL,
                    claimed_at = NULL,
                    lease_until = NULL,
                    updated_by = 'CPF_BROKER_RECOVERY',
                    updated_at = CURRENT_TIMESTAMP
                WHERE outbox_status = 'CLAIMED'
                  AND lease_until <= ?
                """, Timestamp.from(now), Timestamp.from(now));
        List<Map<String, Object>> rows = queryForListLimited("""
                SELECT message_id AS messageId,
                       topic,
                       message_key AS messageKey,
                       transaction_id AS transactionId,
                       segment_id AS segmentId,
                       producer_module AS producerModule,
                       consumer_module AS consumerModule,
                       idempotency_key AS idempotencyKey,
                       payload,
                       content_type AS contentType,
                       header_json AS headerJson,
                       attribute_json AS attributeJson,
                       occurred_at AS occurredAt
                FROM cpf_broker_outbox
                WHERE outbox_status = 'PENDING'
                  AND (next_attempt_at IS NULL OR next_attempt_at <= ?)
                ORDER BY outbox_id
                """, List.of(Timestamp.from(now)), limit);
        List<CpfBrokerEnvelope> claimed = new ArrayList<>(rows.size());
        for (Map<String, Object> row : rows) {
            Instant claimedAt = Instant.now(clock);
            int updated = jdbcTemplate.update("""
                    UPDATE cpf_broker_outbox
                    SET outbox_status = 'CLAIMED',
                        worker_id = ?,
                        claimed_at = ?,
                        lease_until = ?,
                        updated_by = 'CPF_BROKER',
                        updated_at = CURRENT_TIMESTAMP
                    WHERE message_id = ?
                      AND outbox_status = 'PENDING'
                      AND (next_attempt_at IS NULL OR next_attempt_at <= ?)
                    """,
                    workerId,
                    Timestamp.from(claimedAt),
                    Timestamp.from(claimedAt.plus(claimLease)),
                    string(row, "messageId"),
                    Timestamp.from(claimedAt));
            if (updated == 1) {
                claimed.add(mapEnvelope(row));
            }
        }
        return List.copyOf(claimed);
    }

    @Override
    @Deprecated(forRemoval = false)
    public void markUnknown(String messageId, CpfBrokerResult result, Instant nextReconcileAt) {
        throw new SecurityException("Broker UNKNOWN transition requires a fenced worker claim");
    }

    @Override
    @Transactional(transactionManager = "cpfTransactionManager")
    public void markUnknown(String workerId, String messageId, CpfBrokerResult result, Instant nextReconcileAt) {
        requireWorker(workerId);
        int updated = jdbcTemplate.update("""
                UPDATE cpf_broker_outbox
                SET outbox_status = 'UNKNOWN', next_attempt_at = ?, worker_id = NULL,
                    claimed_at = NULL, lease_until = NULL, broker_name = ?, failure_message = ?,
                    updated_by = 'CPF_BROKER_RECONCILE', updated_at = CURRENT_TIMESTAMP
                WHERE message_id = ? AND outbox_status = 'CLAIMED' AND worker_id = ?
                """, timestamp(nextReconcileAt), result.brokerName(),
                CpfBrokerFailureSanitizer.sanitizeNullable(result.detail()), messageId, workerId);
        if (updated != 1) throw new IllegalStateException("Broker UNKNOWN claim fencing conflict: " + messageId);
    }

    @Override
    @Transactional(transactionManager = "cpfTransactionManager")
    public List<CpfBrokerEnvelope> claimUnknown(String workerId, int limit) {
        requireWorker(workerId);
        Instant now = Instant.now(clock);
        jdbcTemplate.update("""
                UPDATE cpf_broker_outbox SET outbox_status = 'UNKNOWN', worker_id = NULL,
                    claimed_at = NULL, lease_until = NULL, updated_by = 'CPF_BROKER_RECOVERY',
                    updated_at = CURRENT_TIMESTAMP
                WHERE outbox_status = 'CLAIMED_UNKNOWN' AND lease_until <= ?
                """, timestamp(now));
        List<Map<String, Object>> rows = queryForListLimited("""
                SELECT message_id AS messageId, topic, message_key AS messageKey,
                       transaction_id AS transactionId, segment_id AS segmentId,
                       producer_module AS producerModule, consumer_module AS consumerModule,
                       idempotency_key AS idempotencyKey, payload, content_type AS contentType,
                       header_json AS headerJson, attribute_json AS attributeJson, occurred_at AS occurredAt
                FROM cpf_broker_outbox
                WHERE outbox_status = 'UNKNOWN' AND next_attempt_at IS NOT NULL AND next_attempt_at <= ?
                ORDER BY outbox_id
                """, List.of(timestamp(now)), limit);
        List<CpfBrokerEnvelope> claimed = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Instant claimedAt = Instant.now(clock);
            int updated = jdbcTemplate.update("""
                    UPDATE cpf_broker_outbox SET outbox_status = 'CLAIMED_UNKNOWN', worker_id = ?,
                        claimed_at = ?, lease_until = ?, updated_at = CURRENT_TIMESTAMP
                    WHERE message_id = ? AND outbox_status = 'UNKNOWN'
                      AND next_attempt_at IS NOT NULL AND next_attempt_at <= ?
                    """, workerId, timestamp(claimedAt), timestamp(claimedAt.plus(claimLease)),
                    string(row, "messageId"), timestamp(claimedAt));
            if (updated == 1) claimed.add(mapEnvelope(row));
        }
        return List.copyOf(claimed);
    }

    @Override
    @Deprecated(forRemoval = false)
    public void releaseUnknown(String messageId, String detail, Instant nextReconcileAt) {
        throw new SecurityException("Broker UNKNOWN release requires a fenced worker claim");
    }

    @Override
    @Transactional(transactionManager = "cpfTransactionManager")
    public void releaseUnknown(String workerId, String messageId, String detail, Instant nextReconcileAt) {
        requireWorker(workerId);
        int updated = jdbcTemplate.update("""
                UPDATE cpf_broker_outbox SET outbox_status = 'UNKNOWN', next_attempt_at = ?,
                    worker_id = NULL, claimed_at = NULL, lease_until = NULL, failure_message = ?,
                    updated_by = 'CPF_BROKER_RECONCILE', updated_at = CURRENT_TIMESTAMP
                WHERE message_id = ? AND outbox_status = 'CLAIMED_UNKNOWN' AND worker_id = ?
                """, timestamp(nextReconcileAt), CpfBrokerFailureSanitizer.sanitize(detail), messageId, workerId);
        if (updated != 1) throw new IllegalStateException("Broker UNKNOWN reconcile fencing conflict: " + messageId);
    }

    @Override
    @Deprecated(forRemoval = false)
    public void markPublished(String messageId, CpfBrokerResult result) {
        throw new SecurityException("Broker publish completion requires a fenced worker claim");
    }

    @Override
    @Transactional(transactionManager = "cpfTransactionManager")
    public void markPublished(String workerId, String messageId, CpfBrokerResult result) {
        requireWorker(workerId);
        java.util.Objects.requireNonNull(result, "result");
        boolean published = "PUBLISHED".equalsIgnoreCase(result.status())
                || "SUCCESS".equalsIgnoreCase(result.status()) || "ACCEPTED".equalsIgnoreCase(result.status());
        List<Map<String, Object>> attempts = jdbcTemplate.queryForList("""
                SELECT attempt_count AS attemptCount, max_attempts AS maxAttempts
                FROM cpf_broker_outbox WHERE message_id = ? AND worker_id = ?
                  AND outbox_status IN ('CLAIMED', 'CLAIMED_UNKNOWN')
                """, messageId, workerId);
        if (attempts.size() != 1) throw new IllegalStateException("Broker publish claim fencing conflict: " + messageId);
        int previousAttempt = intValue(attempts.getFirst().get("attemptCount"));
        int maxAttempts = Math.max(1, intValue(attempts.getFirst().get("maxAttempts")));
        int nextAttempt = previousAttempt + 1;
        String nextStatus = published ? "PUBLISHED" : (nextAttempt >= maxAttempts ? "FAILED" : "PENDING");
        Instant processedAt = result.processedAt() == null ? Instant.now(clock) : result.processedAt();
        Timestamp nextAttemptAt = !published && nextAttempt < maxAttempts
                ? timestamp(processedAt.plusSeconds(retryDelaySeconds(previousAttempt))) : null;
        int updated = jdbcTemplate.update("""
                UPDATE cpf_broker_outbox SET attempt_count = ?, outbox_status = ?, next_attempt_at = ?,
                    worker_id = NULL, claimed_at = NULL, lease_until = NULL, broker_name = ?,
                    partition_key = ?, published_at = ?, failure_message = ?, updated_by = 'CPF_BROKER', updated_at = ?
                WHERE message_id = ? AND attempt_count = ? AND worker_id = ?
                  AND outbox_status IN ('CLAIMED', 'CLAIMED_UNKNOWN')
                """, nextAttempt, nextStatus, nextAttemptAt, result.brokerName(), result.partitionKey(),
                published ? timestamp(processedAt) : null,
                CpfBrokerFailureSanitizer.sanitizeNullable(result.detail()), timestamp(processedAt),
                messageId, previousAttempt, workerId);
        if (updated != 1) throw new IllegalStateException("Broker publish claim fencing conflict: " + messageId);
        if (published) {
            jdbcTemplate.update("""
                    UPDATE cpf_broker_dlq SET replay_status = 'COMPLETED', replay_completed_at = ?,
                        updated_by = 'CPF_BROKER', updated_at = CURRENT_TIMESTAMP
                    WHERE message_id = ? AND replay_status = 'REQUESTED'
                    """, timestamp(processedAt), messageId);
        } else if ("FAILED".equals(nextStatus)) {
            List<Map<String, Object>> failed = jdbcTemplate.queryForList("""
                    SELECT message_id AS messageId, topic, transaction_id AS transactionId,
                           segment_id AS segmentId, failure_message AS failureReason
                    FROM cpf_broker_outbox WHERE message_id = ? AND outbox_status = 'FAILED'
                    """, messageId);
            if (!failed.isEmpty()) {
                Map<String, Object> row = failed.getFirst();
                upsertDlq(string(row, "messageId"), string(row, "topic"), string(row, "transactionId"),
                        string(row, "segmentId"), string(row, "failureReason"), "FAILED", processedAt);
            }
        }
    }

    @Override
    @Transactional(transactionManager = "cpfTransactionManager")
    public boolean markReceived(String messageId, String idempotencyKey) {
        java.util.Objects.requireNonNull(messageId, "messageId");
        try {
            jdbcTemplate.update("""
                    INSERT INTO cpf_broker_inbox (message_id, idempotency_key, inbox_status, received_at, created_by, updated_by)
                    VALUES (?, ?, 'RECEIVED', CURRENT_TIMESTAMP, 'CPF_BROKER', 'CPF_BROKER')
                    """, messageId, idempotencyKey);
            return true;
        } catch (DuplicateKeyException ex) {
            Instant staleBefore = Instant.now(clock).minus(claimLease);
            int reclaimed = jdbcTemplate.update("""
                    UPDATE cpf_broker_inbox SET received_at = CURRENT_TIMESTAMP, result_detail = NULL,
                        updated_by = 'CPF_BROKER_RECOVERY', updated_at = CURRENT_TIMESTAMP
                    WHERE message_id = ? AND inbox_status = 'RECEIVED' AND updated_at <= ?
                    """, messageId, timestamp(staleBefore));
            return reclaimed == 1;
        }
    }

    @Override
    @Transactional(transactionManager = "cpfTransactionManager")
    public void markConsumed(String messageId, CpfBrokerResult result) {
        Instant processedAt = result.processedAt() == null ? Instant.now(clock) : result.processedAt();
        int updated = jdbcTemplate.update("""
                UPDATE cpf_broker_inbox SET inbox_status = ?, consumed_at = ?, result_detail = ?,
                    updated_by = 'CPF_BROKER', updated_at = ?
                WHERE message_id = ? AND inbox_status = 'RECEIVED'
                """, result.status(), timestamp(processedAt), CpfBrokerFailureSanitizer.sanitizeNullable(result.detail()),
                timestamp(processedAt), messageId);
        if (updated != 1) throw new IllegalStateException("Broker inbox finalization conflict: " + messageId);
    }

    @Override
    @Transactional(transactionManager = "cpfTransactionManager")
    public void markConsumerUnknown(String messageId, String detail) {
        int updated = jdbcTemplate.update("""
                UPDATE cpf_broker_inbox SET inbox_status = 'UNKNOWN', result_detail = ?,
                    updated_by = 'CPF_BROKER_RECOVERY', updated_at = CURRENT_TIMESTAMP
                WHERE message_id = ? AND inbox_status = 'RECEIVED'
                """, CpfBrokerFailureSanitizer.sanitize(detail), messageId);
        if (updated != 1) throw new IllegalStateException("Broker inbox UNKNOWN transition conflict: " + messageId);
    }

    @Override
    @Transactional(transactionManager = "cpfTransactionManager")
    public CpfBrokerResult sendToDlq(CpfBrokerEnvelope envelope, String reason) {
        String safe = CpfBrokerFailureSanitizer.sanitize(reason);
        upsertDlq(envelope.message().messageId(), envelope.message().topic(), envelope.transactionId(),
                envelope.segmentId(), safe, "WAITING", null);
        return CpfBrokerResult.failed(envelope.message().messageId(), "CPF_DLQ", safe);
    }

    @Override
    @Transactional(transactionManager = "cpfTransactionManager")
    public CpfBrokerResult moveToDlq(CpfBrokerEnvelope envelope, String reason) {
        CpfBrokerResult result = sendToDlq(envelope, reason);
        int updated = jdbcTemplate.update("""
                UPDATE cpf_broker_inbox SET inbox_status = 'DLQ', consumed_at = CURRENT_TIMESTAMP,
                    result_detail = ?, updated_by = 'CPF_BROKER', updated_at = CURRENT_TIMESTAMP
                WHERE message_id = ? AND inbox_status = 'RECEIVED'
                """, result.detail(), envelope.message().messageId());
        if (updated != 1) throw new IllegalStateException("Broker inbox DLQ transition conflict: " + envelope.message().messageId());
        return result;
    }

    @Override
    public List<CpfBrokerEnvelope> findDlqMessages(String topic, int limit) {
        return queryForListLimited("""
                SELECT o.message_id AS messageId,
                       o.topic,
                       o.message_key AS messageKey,
                       o.transaction_id AS transactionId,
                       o.segment_id AS segmentId,
                       o.producer_module AS producerModule,
                       o.consumer_module AS consumerModule,
                       o.idempotency_key AS idempotencyKey,
                       o.payload,
                       o.content_type AS contentType,
                       o.header_json AS headerJson,
                       o.attribute_json AS attributeJson,
                       o.occurred_at AS occurredAt
                FROM cpf_broker_outbox o
                JOIN cpf_broker_dlq d ON d.message_id = o.message_id
                WHERE (? IS NULL OR d.topic = ?)
                ORDER BY d.dlq_id DESC
                """, Arrays.asList(topic, topic), limit).stream().map(this::mapEnvelope).toList();
    }

    /**
     * Low-level replay used to be a mutation escape hatch. Runtime replay is now owned by the
     * approved reliability command and this SPI remains fail-closed for binary compatibility.
     */
    @Override
    @Deprecated(forRemoval = false)
    @Transactional(transactionManager = "cpfTransactionManager")
    public CpfBrokerResult replay(String messageId) {
        if (messageId == null || messageId.isBlank()) {
            throw new IllegalArgumentException("messageId is required");
        }
        throw new SecurityException("DLQ replay requires an approved owner command");
    }

    @Override
    @Deprecated(forRemoval = false)
    @Transactional(transactionManager = "cpfTransactionManager")
    public List<CpfBrokerResult> replayRange(String topic, Instant from, Instant to, int limit) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException("from must not be after to");
        }
        if (limit < 1 || limit > 5_000) {
            throw new IllegalArgumentException("limit must be between 1 and 5000");
        }
        throw new SecurityException("DLQ range replay requires per-target approved snapshots");
    }

    @Override
    public boolean isDuplicate(String idempotencyKey) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM cpf_broker_inbox
                WHERE idempotency_key = ?
                """, Integer.class, idempotencyKey);
        return count != null && count > 0;
    }

    @Override
    public void remember(String idempotencyKey, String messageId) {
        markReceived(messageId, idempotencyKey);
    }

    private CpfBrokerEnvelope mapEnvelope(Map<String, Object> row) {
        CpfBrokerMessage message = new CpfBrokerMessage(
                string(row, "messageId"),
                string(row, "topic"),
                string(row, "messageKey"),
                bytes(row.get("payload")),
                string(row, "contentType"),
                decodeMap(string(row, "headerJson")));
        return new CpfBrokerEnvelope(
                string(row, "transactionId"),
                string(row, "segmentId"),
                string(row, "producerModule"),
                string(row, "consumerModule"),
                string(row, "idempotencyKey"),
                instant(row, "occurredAt"),
                message,
                decodeMap(string(row, "attributeJson")));
    }

    private String requireWorker(String workerId) {
        if (workerId == null || workerId.isBlank()) throw new IllegalArgumentException("workerId is required");
        return workerId.trim();
    }

    private int safeLimit(int limit) {
        return Math.max(1, Math.min(limit, 1000));
    }

    private List<Map<String, Object>> queryForListLimited(String sql, List<?> args, int limit) {
        return jdbcTemplate.query(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql);
            for (int index = 0; index < args.size(); index++) {
                statement.setObject(index + 1, args.get(index));
            }
            statement.setMaxRows(safeLimit(limit));
            return statement;
        }, new ColumnMapRowMapper());
    }

    private void upsertDlq(
            String messageId,
            String topic,
            String transactionId,
            String segmentId,
            String reason,
            String existingStatus,
            Instant completedAt) {
        int updated = updateDlq(messageId, reason, existingStatus, completedAt);
        if (updated != 0) {
            return;
        }
        try {
            jdbcTemplate.update("""
                    INSERT INTO cpf_broker_dlq (
                        message_id, topic, transaction_id, segment_id, failure_reason,
                        replay_status, created_by, updated_by
                    ) VALUES (?, ?, ?, ?, ?, 'WAITING', 'CPF_BROKER', 'CPF_BROKER')
                    """, messageId, topic, transactionId, segmentId, reason);
        } catch (DuplicateKeyException duplicate) {
            if (updateDlq(messageId, reason, existingStatus, completedAt) != 1) {
                throw duplicate;
            }
        }
    }

    private int updateDlq(String messageId, String reason, String status, Instant completedAt) {
        if (completedAt == null) {
            return jdbcTemplate.update("""
                    UPDATE cpf_broker_dlq
                    SET failure_reason = ?,
                        replay_status = ?,
                        updated_by = 'CPF_BROKER',
                        updated_at = CURRENT_TIMESTAMP
                    WHERE message_id = ?
                    """, reason, status, messageId);
        }
        return jdbcTemplate.update("""
                UPDATE cpf_broker_dlq
                SET failure_reason = ?,
                    replay_status = ?,
                    replay_completed_at = ?,
                    updated_by = 'CPF_BROKER',
                    updated_at = CURRENT_TIMESTAMP
                WHERE message_id = ?
                """, reason, status, timestamp(completedAt), messageId);
    }

    private long retryDelaySeconds(int previousAttempt) {
        int exponent = Math.max(0, Math.min(previousAttempt, 20));
        return Math.min(300L, 5L * (1L << exponent));
    }

    private int intValue(Object value) {
        return value instanceof Number number ? number.intValue() : 0;
    }

    private String encodeMap(Map<String, String> values) {
        if (values == null || values.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder("v2\n");
        values.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(java.util.Comparator.nullsFirst(String::compareTo)))
                .forEach(entry -> builder
                        .append(encodeToken(entry.getKey()))
                        .append('=')
                        .append(encodeToken(entry.getValue()))
                        .append('\n'));
        return builder.toString();
    }

    private Map<String, String> decodeMap(String encoded) {
        if (encoded == null || encoded.isBlank()) {
            return Map.of();
        }
        Map<String, String> values = new LinkedHashMap<>();
        boolean version2 = encoded.startsWith("v2\n");
        String body = version2 ? encoded.substring(3) : encoded;
        Arrays.stream(body.split("\\R"))
                .filter(line -> !line.isBlank())
                .forEach(line -> {
                    int index = line.indexOf('=');
                    if (index >= 0) {
                        String key = line.substring(0, index);
                        String value = line.substring(index + 1);
                        values.put(version2 ? decodeToken(key) : key,
                                version2 ? decodeToken(value) : value);
                    }
                });
        return Map.copyOf(values);
    }

    private String encodeToken(String value) {
        String normalized = value == null ? "" : value;
        return java.util.Base64.getUrlEncoder().withoutPadding()
                .encodeToString(normalized.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    private String decodeToken(String value) {
        return new String(java.util.Base64.getUrlDecoder().decode(value),
                java.nio.charset.StandardCharsets.UTF_8);
    }

    private byte[] bytes(Object value) {
        if (value instanceof byte[] payload) {
            return Arrays.copyOf(payload, payload.length);
        }
        return new byte[0];
    }

    private Timestamp timestamp(Instant instant) {
        return instant == null ? null : Timestamp.from(instant);
    }

    private Instant instant(Map<String, Object> row, String key) {
        Object value = row.get(key);
        if (value instanceof Timestamp timestamp) {
            return timestamp.toInstant();
        }
        if (value instanceof Instant instant) {
            return instant;
        }
        return null;
    }

    private String string(Map<String, Object> row, String key) {
        Object value = row.get(key);
        return value == null ? null : String.valueOf(value);
    }
}
