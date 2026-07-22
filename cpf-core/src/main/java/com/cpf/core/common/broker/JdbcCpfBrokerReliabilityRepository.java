package com.cpf.core.common.broker;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

/**
 * CPF broker outbox/inbox/DLQ/replay 테이블을 사용하는 JDBC reference adapter입니다.
 *
 * <p>실 Kafka/MQ adapter가 없어도 업무 트랜잭션과 outbox 저장, 소비자 중복 방지, DLQ 이력,
 * 관리자 replay 요청을 DB 기준으로 검증할 수 있게 하는 최소 운영 저장소입니다.</p>
 */
public class JdbcCpfBrokerReliabilityRepository
        implements CpfBrokerOutboxPort, CpfBrokerInboxPort, CpfBrokerDlqPort, CpfBrokerReplayPort, CpfBrokerIdempotencyPort {
    private final JdbcTemplate jdbcTemplate;

    public JdbcCpfBrokerReliabilityRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public CpfBrokerResult saveOutbox(CpfBrokerEnvelope envelope) {
        jdbcTemplate.update("""
                INSERT INTO cpf_broker_outbox (
                    message_id, topic, message_key, transaction_global_id, segment_id,
                    producer_module, consumer_module, idempotency_key, payload, content_type,
                    header_json, attribute_json, outbox_status, occurred_at, created_by, updated_by
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'PENDING', ?, 'CPF_BROKER', 'CPF_BROKER')
                ON DUPLICATE KEY UPDATE
                    attribute_json = VALUES(attribute_json),
                    updated_by = 'CPF_BROKER',
                    updated_at = CURRENT_TIMESTAMP
                """,
                envelope.message().messageId(),
                envelope.message().topic(),
                envelope.message().key(),
                envelope.transactionGlobalId(),
                envelope.segmentId(),
                envelope.producerModule(),
                envelope.consumerModule(),
                envelope.idempotencyKey(),
                envelope.message().payload(),
                envelope.message().contentType(),
                encodeMap(envelope.message().headers()),
                encodeMap(envelope.attributes()),
                Timestamp.from(envelope.occurredAt()));
        return CpfBrokerResult.accepted(envelope.message().messageId(), "CPF_OUTBOX", envelope.message().key());
    }

    @Override
    public List<CpfBrokerEnvelope> claimPending(String workerId, int limit) {
        jdbcTemplate.update("""
                UPDATE cpf_broker_outbox
                SET outbox_status = 'PENDING',
                    worker_id = NULL,
                    claimed_at = NULL,
                    lease_until = NULL,
                    updated_by = 'CPF_BROKER_RECOVERY',
                    updated_at = CURRENT_TIMESTAMP
                WHERE outbox_status = 'CLAIMED'
                  AND lease_until <= CURRENT_TIMESTAMP(3)
                """);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT message_id AS messageId,
                       topic,
                       message_key AS messageKey,
                       transaction_global_id AS transactionGlobalId,
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
                  AND (next_attempt_at IS NULL OR next_attempt_at <= CURRENT_TIMESTAMP(3))
                ORDER BY outbox_id
                LIMIT ?
                """, safeLimit(limit));
        List<CpfBrokerEnvelope> claimed = new ArrayList<>(rows.size());
        for (Map<String, Object> row : rows) {
            int updated = jdbcTemplate.update("""
                    UPDATE cpf_broker_outbox
                    SET outbox_status = 'CLAIMED',
                        worker_id = ?,
                        claimed_at = CURRENT_TIMESTAMP(3),
                        lease_until = TIMESTAMPADD(SECOND, 30, CURRENT_TIMESTAMP(3)),
                        updated_by = 'CPF_BROKER',
                        updated_at = CURRENT_TIMESTAMP
                    WHERE message_id = ?
                      AND outbox_status = 'PENDING'
                      AND (next_attempt_at IS NULL OR next_attempt_at <= CURRENT_TIMESTAMP(3))
                    """, workerId, string(row, "messageId"));
            if (updated == 1) {
                claimed.add(mapEnvelope(row));
            }
        }
        return List.copyOf(claimed);
    }

    @Override
    public void markPublished(String messageId, CpfBrokerResult result) {
        boolean published = "PUBLISHED".equalsIgnoreCase(result.status())
                || "SUCCESS".equalsIgnoreCase(result.status())
                || "ACCEPTED".equalsIgnoreCase(result.status());
        jdbcTemplate.update("""
                UPDATE cpf_broker_outbox
                SET attempt_count = attempt_count + 1,
                    outbox_status = CASE
                        WHEN ? = 'Y' THEN 'PUBLISHED'
                        WHEN attempt_count + 1 >= max_attempts THEN 'FAILED'
                        ELSE 'PENDING'
                    END,
                    next_attempt_at = CASE
                        WHEN ? = 'Y' OR attempt_count + 1 >= max_attempts THEN NULL
                        ELSE TIMESTAMPADD(SECOND, LEAST(300, POW(2, attempt_count) * 5), CURRENT_TIMESTAMP(3))
                    END,
                    worker_id = NULL,
                    lease_until = NULL,
                    broker_name = ?,
                    partition_key = ?,
                    published_at = CASE WHEN ? = 'Y' THEN ? ELSE NULL END,
                    failure_message = ?,
                    updated_by = 'CPF_BROKER',
                    updated_at = CURRENT_TIMESTAMP
                WHERE message_id = ?
                """,
                published ? "Y" : "N",
                published ? "Y" : "N",
                result.brokerName(),
                result.partitionKey(),
                published ? "Y" : "N",
                Timestamp.from(result.processedAt()),
                result.detail(),
                messageId);
        if (published) {
            jdbcTemplate.update("""
                    UPDATE cpf_broker_dlq
                    SET replay_status = 'COMPLETED',
                        replay_completed_at = CURRENT_TIMESTAMP(3),
                        updated_by = 'CPF_BROKER',
                        updated_at = CURRENT_TIMESTAMP
                    WHERE message_id = ? AND replay_status = 'REQUESTED'
                    """, messageId);
            return;
        }
        jdbcTemplate.update("""
                INSERT INTO cpf_broker_dlq (
                    message_id, topic, transaction_global_id, segment_id, failure_reason,
                    replay_status, created_by, updated_by
                )
                SELECT message_id, topic, transaction_global_id, segment_id, failure_message,
                       'WAITING', 'CPF_BROKER', 'CPF_BROKER'
                FROM cpf_broker_outbox
                WHERE message_id = ? AND outbox_status = 'FAILED'
                ON DUPLICATE KEY UPDATE
                    failure_reason = VALUES(failure_reason),
                    replay_status = 'FAILED',
                    replay_completed_at = CURRENT_TIMESTAMP(3),
                    updated_by = 'CPF_BROKER',
                    updated_at = CURRENT_TIMESTAMP
                """, messageId);
    }

    @Override
    public boolean markReceived(String messageId, String idempotencyKey) {
        try {
            jdbcTemplate.update("""
                    INSERT INTO cpf_broker_inbox (
                        message_id, idempotency_key, inbox_status, received_at, created_by, updated_by
                    ) VALUES (?, ?, 'RECEIVED', CURRENT_TIMESTAMP(3), 'CPF_BROKER', 'CPF_BROKER')
                    """, messageId, idempotencyKey);
            return true;
        } catch (DuplicateKeyException ex) {
            return false;
        }
    }

    @Override
    public void markConsumed(String messageId, CpfBrokerResult result) {
        jdbcTemplate.update("""
                UPDATE cpf_broker_inbox
                SET inbox_status = ?,
                    consumed_at = ?,
                    result_detail = ?,
                    updated_by = 'CPF_BROKER',
                    updated_at = CURRENT_TIMESTAMP
                WHERE message_id = ?
                """, result.status(), Timestamp.from(result.processedAt()), result.detail(), messageId);
    }

    @Override
    public CpfBrokerResult sendToDlq(CpfBrokerEnvelope envelope, String reason) {
        jdbcTemplate.update("""
                INSERT INTO cpf_broker_dlq (
                    message_id, topic, transaction_global_id, segment_id, failure_reason,
                    replay_status, created_by, updated_by
                ) VALUES (?, ?, ?, ?, ?, 'WAITING', 'CPF_BROKER', 'CPF_BROKER')
                ON DUPLICATE KEY UPDATE
                    failure_reason = VALUES(failure_reason),
                    replay_status = 'WAITING',
                    updated_by = 'CPF_BROKER',
                    updated_at = CURRENT_TIMESTAMP
                """,
                envelope.message().messageId(),
                envelope.message().topic(),
                envelope.transactionGlobalId(),
                envelope.segmentId(),
                reason);
        return CpfBrokerResult.failed(envelope.message().messageId(), "CPF_DLQ", reason);
    }

    @Override
    public List<CpfBrokerEnvelope> findDlqMessages(String topic, int limit) {
        return jdbcTemplate.queryForList("""
                SELECT o.message_id AS messageId,
                       o.topic,
                       o.message_key AS messageKey,
                       o.transaction_global_id AS transactionGlobalId,
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
                LIMIT ?
                """, topic, topic, safeLimit(limit)).stream().map(this::mapEnvelope).toList();
    }

    @Override
    public CpfBrokerResult replay(String messageId) {
        int requested = jdbcTemplate.update("""
                UPDATE cpf_broker_dlq
                SET replay_status = 'REQUESTED',
                    replay_requested_at = CURRENT_TIMESTAMP(3),
                    replay_count = replay_count + 1,
                    updated_by = 'CPF_BROKER',
                    updated_at = CURRENT_TIMESTAMP
                WHERE message_id = ?
                  AND replay_status IN ('WAITING', 'FAILED')
                """, messageId);
        if (requested != 1) {
            return CpfBrokerResult.failed(messageId, "CPF_REPLAY", "재처리 가능한 DLQ가 없습니다.");
        }
        jdbcTemplate.update("DELETE FROM cpf_broker_inbox WHERE message_id = ?", messageId);
        int requeued = jdbcTemplate.update("""
                UPDATE cpf_broker_outbox
                SET outbox_status = 'PENDING',
                    worker_id = NULL,
                    attempt_count = 0,
                    next_attempt_at = NULL,
                    lease_until = NULL,
                    claimed_at = NULL,
                    published_at = NULL,
                    failure_message = NULL,
                    updated_by = 'CPF_REPLAY',
                    updated_at = CURRENT_TIMESTAMP
                WHERE message_id = ?
                """, messageId);
        if (requeued != 1) {
            throw new IllegalStateException("DLQ 원본 outbox가 없어 실제 재처리를 시작할 수 없습니다.");
        }
        return CpfBrokerResult.accepted(messageId, "CPF_REPLAY", messageId);
    }

    @Override
    public List<CpfBrokerResult> replayRange(String topic, Instant from, Instant to, int limit) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT message_id AS messageId,
                       topic,
                       replay_status AS replayStatus,
                       replay_requested_at AS replayRequestedAt,
                       failure_reason AS failureReason
                FROM cpf_broker_dlq
                WHERE (? IS NULL OR topic = ?)
                  AND (? IS NULL OR created_at >= ?)
                  AND (? IS NULL OR created_at <= ?)
                ORDER BY dlq_id DESC
                LIMIT ?
                """,
                topic,
                topic,
                timestamp(from),
                timestamp(from),
                timestamp(to),
                timestamp(to),
                safeLimit(limit));
        return rows.stream().map(row -> replay(string(row, "messageId"))).toList();
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
                string(row, "transactionGlobalId"),
                string(row, "segmentId"),
                string(row, "producerModule"),
                string(row, "consumerModule"),
                string(row, "idempotencyKey"),
                instant(row, "occurredAt"),
                message,
                decodeMap(string(row, "attributeJson")));
    }

    private int safeLimit(int limit) {
        return Math.max(1, Math.min(limit, 1000));
    }

    private String encodeMap(Map<String, String> values) {
        if (values == null || values.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        values.forEach((key, value) -> builder
                .append(key == null ? "" : key.replace("\n", " "))
                .append('=')
                .append(value == null ? "" : value.replace("\n", " "))
                .append('\n'));
        return builder.toString();
    }

    private Map<String, String> decodeMap(String encoded) {
        if (encoded == null || encoded.isBlank()) {
            return Map.of();
        }
        Map<String, String> values = new LinkedHashMap<>();
        Arrays.stream(encoded.split("\\R"))
                .filter(line -> !line.isBlank())
                .forEach(line -> {
                    int index = line.indexOf('=');
                    if (index > 0) {
                        values.put(line.substring(0, index), line.substring(index + 1));
                    }
                });
        return values;
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
