package com.cpf.gateway.runtime;

import com.cpf.platform.operations.api.runtime.CpfRuntimePolicyDistributionPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * Gateway Instance가 Runtime 정책 Event를 Claim/ACK하고 필요 시 재발행·전달 상태 조회까지 수행하는 JDBC Adapter입니다.
 *
 * <p>Gateway의 정상 동작은 Claim/ACK 경로를 사용하지만 운영 복구와 동일 JVM 배치에서는 전체 Port가
 * 호출될 수 있으므로 부분 구현을 두지 않습니다.</p>
 */
@Component("gatewayRuntimePolicyDistributionPort")
@ConditionalOnBean(DataSource.class)
public class GatewayRuntimePolicyDistributionAdapter implements CpfRuntimePolicyDistributionPort {
    private static final Set<String> ACK_STATUSES = Set.of("APPLIED", "FAILED", "IGNORED");

    private final JdbcTemplate jdbc;
    private final CpfRuntimePolicyMetadataCodec metadataCodec;

    public GatewayRuntimePolicyDistributionAdapter(DataSource dataSource, ObjectMapper objectMapper) {
        this.jdbc = new JdbcTemplate(dataSource);
        this.metadataCodec = new CpfRuntimePolicyMetadataCodec(objectMapper);
    }

    @Override
    @Transactional
    public DistributionEvent publish(PublishCommand command) {
        validate(command);
        String eventId = blank(command.eventId()) ? UUID.randomUUID().toString() : command.eventId().trim();
        try {
            jdbc.update("""
                    INSERT INTO OPS_RUNTIME_POLICY_EVENT
                    (event_id,event_type,aggregate_type,aggregate_id,aggregate_version,action_code,payload_checksum,
                     metadata_text,reason,requested_by,occurred_at,event_status,created_at)
                    VALUES (?,?,?,?,?,?,?,?,?,?,?,'PENDING',CURRENT_TIMESTAMP)
                    """, eventId, command.eventType().trim(), command.aggregateType().trim(), command.aggregateId().trim(),
                    command.aggregateVersion(), command.action().trim(), clean(command.payloadChecksum()),
                    metadataCodec.encode(command.metadata()), command.reason().trim(), command.requestedBy().trim(),
                    timestamp(command.occurredAt()));
        } catch (DuplicateKeyException duplicate) {
            List<DistributionEvent> found = findEvent(eventId);
            if (found.isEmpty()) {
                throw duplicate;
            }
            DistributionEvent existing = found.getFirst();
            assertIdempotentPublish(existing, command);
            return existing;
        }
        return new DistributionEvent(eventId, command.eventType().trim(), command.aggregateType().trim(),
                command.aggregateId().trim(), command.aggregateVersion(), command.action().trim(),
                clean(command.payloadChecksum()), command.metadata(), command.reason().trim(),
                command.requestedBy().trim(), command.occurredAt(), 0, 0);
    }

    @Override
    @Transactional
    public List<DistributionEvent> claimPending(String consumerId, List<String> eventTypes, int limit, int leaseSeconds) {
        String normalizedConsumerId = required(consumerId, "consumerId");
        int max = limit <= 0 ? 100 : Math.min(limit, 500);
        int lease = Math.max(10, Math.min(leaseSeconds, 600));
        List<DistributionEvent> candidates = queryCandidates(normalizedConsumerId, normalizeEventTypes(eventTypes), max);
        List<DistributionEvent> claimed = new ArrayList<>();
        OffsetDateTime leasedUntil = OffsetDateTime.now().plusSeconds(lease);

        for (DistributionEvent event : candidates) {
            long token = Math.max(1, event.fencingToken() + 1);
            int attempt = event.deliveryAttempt() + 1;
            int updated = jdbc.update("""
                    UPDATE OPS_RUNTIME_POLICY_DELIVERY
                       SET delivery_status='CLAIMED',attempt_count=?,fencing_token=?,leased_until=?,
                           error_code=NULL,error_message=NULL,acknowledged_at=NULL,updated_at=CURRENT_TIMESTAMP
                     WHERE event_id=? AND consumer_id=? AND fencing_token=?
                       AND (delivery_status IN ('PENDING','FAILED','CLAIMED') OR delivery_status IS NULL)
                       AND (leased_until IS NULL OR leased_until<CURRENT_TIMESTAMP)
                    """, attempt, token, timestamp(leasedUntil), event.eventId(), normalizedConsumerId,
                    event.fencingToken());

            if (updated == 0 && event.fencingToken() == 0) {
                try {
                    jdbc.update("""
                            INSERT INTO OPS_RUNTIME_POLICY_DELIVERY
                            (event_id,consumer_id,delivery_status,attempt_count,fencing_token,leased_until,
                             created_at,updated_at)
                            VALUES (?,?,'CLAIMED',1,1,?,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)
                            """, event.eventId(), normalizedConsumerId, timestamp(leasedUntil));
                    updated = 1;
                    token = 1;
                    attempt = 1;
                } catch (DuplicateKeyException ignored) {
                    updated = 0;
                }
            }

            if (updated == 1) {
                claimed.add(new DistributionEvent(event.eventId(), event.eventType(), event.aggregateType(),
                        event.aggregateId(), event.aggregateVersion(), event.action(), event.payloadChecksum(),
                        event.metadata(), event.reason(), event.requestedBy(), event.occurredAt(), token, attempt));
            }
        }
        return List.copyOf(claimed);
    }

    @Override
    @Transactional
    public DeliveryStatus acknowledge(AcknowledgeCommand command) {
        String eventId = required(command.eventId(), "eventId");
        String consumerId = required(command.consumerId(), "consumerId");
        String status = required(command.status(), "status").toUpperCase(Locale.ROOT);
        if (!ACK_STATUSES.contains(status)) {
            throw new IllegalArgumentException("Unsupported ACK status: " + status);
        }
        OffsetDateTime acknowledgedAt = command.acknowledgedAt() == null
                ? OffsetDateTime.now()
                : command.acknowledgedAt();
        String errorCode = truncate(clean(command.errorCode()), 100);
        String errorMessage = truncate(clean(command.errorMessage()), 1000);

        int updated = jdbc.update("""
                UPDATE OPS_RUNTIME_POLICY_DELIVERY
                   SET delivery_status=?,error_code=?,error_message=?,acknowledged_at=?,
                       leased_until=NULL,updated_at=CURRENT_TIMESTAMP
                 WHERE event_id=? AND consumer_id=? AND fencing_token=? AND delivery_status='CLAIMED'
                """, status, errorCode, errorMessage, timestamp(acknowledgedAt), eventId, consumerId,
                command.fencingToken());
        if (updated != 1) {
            throw new IllegalStateException("Runtime policy ACK fencing conflict: eventId=" + eventId
                    + ", consumerId=" + consumerId + ", fencingToken=" + command.fencingToken());
        }

        List<DeliveryStatus> rows = jdbc.query("""
                SELECT e.aggregate_type,e.aggregate_id,e.aggregate_version,d.attempt_count,d.updated_at
                  FROM OPS_RUNTIME_POLICY_DELIVERY d
                  JOIN OPS_RUNTIME_POLICY_EVENT e ON e.event_id=d.event_id
                 WHERE d.event_id=? AND d.consumer_id=?
                """, (rs, rowNum) -> new DeliveryStatus(
                eventId,
                consumerId,
                rs.getString("aggregate_type"),
                rs.getString("aggregate_id"),
                rs.getLong("aggregate_version"),
                status,
                rs.getInt("attempt_count"),
                command.fencingToken(),
                errorCode,
                errorMessage,
                null,
                acknowledgedAt,
                requiredOffset(rs.getTimestamp("updated_at"), "updated_at")),
                eventId, consumerId);
        if (rows.size() != 1) {
            throw new IllegalStateException("Runtime policy ACK row disappeared after update: eventId=" + eventId
                    + ", consumerId=" + consumerId);
        }
        return rows.getFirst();
    }

    @Override
    public List<DeliveryStatus> findDeliveryStatus(String aggregateType, String aggregateId, int limit) {
        StringBuilder sql = new StringBuilder("""
                SELECT d.event_id,d.consumer_id,e.aggregate_type,e.aggregate_id,e.aggregate_version,d.delivery_status,
                       d.attempt_count,d.fencing_token,d.error_code,d.error_message,d.leased_until,
                       d.acknowledged_at,d.updated_at
                  FROM OPS_RUNTIME_POLICY_DELIVERY d
                  JOIN OPS_RUNTIME_POLICY_EVENT e ON e.event_id=d.event_id
                 WHERE 1=1
                """);
        List<Object> args = new ArrayList<>();
        append(sql, args, " AND e.aggregate_type=?", aggregateType);
        append(sql, args, " AND e.aggregate_id=?", aggregateId);
        sql.append(" ORDER BY d.updated_at DESC, d.event_id DESC, d.consumer_id");
        return queryLimited(sql.toString(), args, limit, (rs, rowNum) -> new DeliveryStatus(
                rs.getString("event_id"), rs.getString("consumer_id"), rs.getString("aggregate_type"),
                rs.getString("aggregate_id"), rs.getLong("aggregate_version"),
                rs.getString("delivery_status"), rs.getInt("attempt_count"), rs.getLong("fencing_token"),
                rs.getString("error_code"), rs.getString("error_message"),
                offset(rs.getTimestamp("leased_until")), offset(rs.getTimestamp("acknowledged_at")),
                offset(rs.getTimestamp("updated_at"))));
    }

    private List<DistributionEvent> queryCandidates(String consumerId, List<String> eventTypes, int max) {
        StringBuilder sql = new StringBuilder("""
                SELECT e.event_id,e.event_type,e.aggregate_type,e.aggregate_id,e.aggregate_version,e.action_code,
                       e.payload_checksum,e.metadata_text,e.reason,e.requested_by,e.occurred_at,
                       COALESCE(d.fencing_token,0) fencing_token,COALESCE(d.attempt_count,0) attempt_count
                  FROM OPS_RUNTIME_POLICY_EVENT e
                  LEFT JOIN OPS_RUNTIME_POLICY_DELIVERY d
                    ON d.event_id=e.event_id AND d.consumer_id=?
                 WHERE e.event_status='PENDING'
                   AND (d.delivery_status IS NULL OR d.delivery_status IN ('PENDING','FAILED')
                        OR (d.delivery_status='CLAIMED' AND d.leased_until<CURRENT_TIMESTAMP))
                """);
        List<Object> args = new ArrayList<>();
        args.add(consumerId);
        if (!eventTypes.isEmpty()) {
            sql.append(" AND e.event_type IN (");
            for (int index = 0; index < eventTypes.size(); index++) {
                if (index > 0) {
                    sql.append(',');
                }
                sql.append('?');
                args.add(eventTypes.get(index));
            }
            sql.append(')');
        }
        sql.append(" ORDER BY e.occurred_at,e.event_id");
        return jdbc.query(connection -> {
            var statement = connection.prepareStatement(sql.toString());
            statement.setMaxRows(max);
            for (int index = 0; index < args.size(); index++) {
                statement.setObject(index + 1, args.get(index));
            }
            return statement;
        }, (rs, rowNum) -> new DistributionEvent(rs.getString("event_id"), rs.getString("event_type"),
                rs.getString("aggregate_type"), rs.getString("aggregate_id"), rs.getLong("aggregate_version"),
                rs.getString("action_code"), rs.getString("payload_checksum"),
                metadataCodec.decode(rs.getString("metadata_text")), rs.getString("reason"), rs.getString("requested_by"),
                offset(rs.getTimestamp("occurred_at")), rs.getLong("fencing_token"),
                rs.getInt("attempt_count")));
    }

    private List<DistributionEvent> findEvent(String eventId) {
        return jdbc.query("""
                SELECT event_id,event_type,aggregate_type,aggregate_id,aggregate_version,action_code,
                       payload_checksum,metadata_text,reason,requested_by,occurred_at
                  FROM OPS_RUNTIME_POLICY_EVENT
                 WHERE event_id=?
                """, (rs, rowNum) -> new DistributionEvent(rs.getString("event_id"), rs.getString("event_type"),
                rs.getString("aggregate_type"), rs.getString("aggregate_id"), rs.getLong("aggregate_version"),
                rs.getString("action_code"), rs.getString("payload_checksum"),
                metadataCodec.decode(rs.getString("metadata_text")), rs.getString("reason"), rs.getString("requested_by"),
                offset(rs.getTimestamp("occurred_at")), 0, 0), eventId);
    }

    private static void validate(PublishCommand command) {
        required(command.eventType(), "eventType");
        required(command.aggregateType(), "aggregateType");
        required(command.aggregateId(), "aggregateId");
        required(command.action(), "action");
        required(command.requestedBy(), "requestedBy");
        if (clean(command.reason()).length() < 5) {
            throw new IllegalArgumentException("reason must be at least 5 characters");
        }
        if (command.aggregateVersion() < 0) {
            throw new IllegalArgumentException("aggregateVersion must be zero or greater");
        }
    }

    private static void assertIdempotentPublish(DistributionEvent existing, PublishCommand requested) {
        boolean same = existing.eventType().equals(requested.eventType().trim())
                && existing.aggregateType().equals(requested.aggregateType().trim())
                && existing.aggregateId().equals(requested.aggregateId().trim())
                && existing.aggregateVersion() == requested.aggregateVersion()
                && existing.action().equals(requested.action().trim())
                && existing.payloadChecksum().equals(clean(requested.payloadChecksum()));
        if (!same) {
            throw new IllegalStateException("eventId already exists with a different runtime policy command: "
                    + existing.eventId());
        }
    }

    private static List<String> normalizeEventTypes(List<String> eventTypes) {
        if (eventTypes == null || eventTypes.isEmpty()) {
            return List.of();
        }
        return eventTypes.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .distinct()
                .toList();
    }

    private static void append(StringBuilder sql, List<Object> args, String clause, String value) {
        if (!blank(value)) {
            sql.append(clause);
            args.add(value.trim());
        }
    }

    private <T> List<T> queryLimited(String sql, List<Object> args, int limit,
                                     org.springframework.jdbc.core.RowMapper<T> mapper) {
        int max = limit <= 0 ? 100 : Math.min(limit, 1000);
        return jdbc.query(connection -> {
            var statement = connection.prepareStatement(sql);
            statement.setMaxRows(max);
            for (int index = 0; index < args.size(); index++) {
                statement.setObject(index + 1, args.get(index));
            }
            return statement;
        }, mapper);
    }

    private static Timestamp timestamp(OffsetDateTime value) {
        OffsetDateTime effective = value == null ? OffsetDateTime.now() : value;
        return Timestamp.from(effective.toInstant());
    }

    private static OffsetDateTime offset(Timestamp value) {
        return value == null ? null : value.toInstant().atOffset(ZoneOffset.UTC);
    }

    private static OffsetDateTime requiredOffset(Timestamp value, String column) {
        OffsetDateTime converted = offset(value);
        if (converted == null) {
            throw new IllegalStateException("Required runtime policy timestamp is null: " + column);
        }
        return converted;
    }

    private static String truncate(String value, int maxLength) {
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    private static String required(String value, String name) {
        if (blank(value)) {
            throw new IllegalArgumentException(name + " required");
        }
        return value.trim();
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
