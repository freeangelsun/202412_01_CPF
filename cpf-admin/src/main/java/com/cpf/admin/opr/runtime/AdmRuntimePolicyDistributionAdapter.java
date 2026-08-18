package com.cpf.admin.opr.runtime;

import com.cpf.data.persistence.api.annotation.CpfTransactional;
import com.cpf.platform.operations.api.runtime.CpfRuntimePolicyDistributionPort;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * ADM에서 Runtime 정책 Event를 발행하고 Consumer별 전달 상태를 관리하는 JDBC Adapter입니다.
 *
 * <p>운영 도구 자체도 복구 작업과 재처리 진단을 수행할 수 있도록 Publish, Claim, ACK,
 * 상태 조회 계약을 모두 구현합니다. Claim/ACK는 fencing token과 lease를 사용하므로
 * 여러 ADM 인스턴스가 동시에 복구 작업을 수행해도 오래된 작업자가 최신 상태를 덮어쓰지 않습니다.</p>
 */
@Component("admRuntimePolicyDistributionPort")
public class AdmRuntimePolicyDistributionAdapter implements CpfRuntimePolicyDistributionPort {
    private static final Set<String> ACK_STATUSES = Set.of("APPLIED", "FAILED", "IGNORED");

    private final JdbcTemplate jdbc;

    public AdmRuntimePolicyDistributionAdapter(@Qualifier("cpfJdbcTemplate") JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    @CpfTransactional
    public DistributionEvent publish(PublishCommand command) {
        validate(command);
        String eventId = blank(command.eventId()) ? UUID.randomUUID().toString() : command.eventId().trim();
        String metadata = encode(command.metadata());
        try {
            jdbc.update("""
                    INSERT INTO OPS_RUNTIME_POLICY_EVENT
                    (event_id,event_type,aggregate_type,aggregate_id,aggregate_version,action_code,payload_checksum,
                     metadata_text,reason,requested_by,occurred_at,event_status,created_at)
                    VALUES (?,?,?,?,?,?,?,?,?,?,?,'PENDING',CURRENT_TIMESTAMP)
                    """, eventId, command.eventType().trim(), command.aggregateType().trim(), command.aggregateId().trim(),
                    command.aggregateVersion(), command.action().trim(), clean(command.payloadChecksum()), metadata,
                    command.reason().trim(), command.requestedBy().trim(), timestamp(command.occurredAt()));
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
    @CpfTransactional
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
    @CpfTransactional
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

        Map<String, Object> row = jdbc.queryForMap("""
                SELECT e.aggregate_type,e.aggregate_id,e.aggregate_version,d.attempt_count,d.updated_at
                  FROM OPS_RUNTIME_POLICY_DELIVERY d
                  JOIN OPS_RUNTIME_POLICY_EVENT e ON e.event_id=d.event_id
                 WHERE d.event_id=? AND d.consumer_id=?
                """, eventId, consumerId);
        return new DeliveryStatus(eventId, consumerId, String.valueOf(row.get("aggregate_type")),
                String.valueOf(row.get("aggregate_id")), number(row.get("aggregate_version")).longValue(), status,
                number(row.get("attempt_count")).intValue(), command.fencingToken(), errorCode, errorMessage,
                null, acknowledgedAt, toOffsetDateTime(row.get("updated_at")));
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
                decode(rs.getString("metadata_text")), rs.getString("reason"), rs.getString("requested_by"),
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
                decode(rs.getString("metadata_text")), rs.getString("reason"), rs.getString("requested_by"),
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

    private static String encode(Map<String, String> map) {
        if (map == null || map.isEmpty()) {
            return "";
        }
        return map.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> escape(entry.getKey()) + '=' + escape(entry.getValue()))
                .reduce((left, right) -> left + '\n' + right)
                .orElse("");
    }

    private static Map<String, String> decode(String value) {
        if (blank(value)) {
            return Map.of();
        }
        Map<String, String> result = new LinkedHashMap<>();
        for (String line : value.split("\\R")) {
            int separator = line.indexOf('=');
            if (separator > 0) {
                result.put(unescape(line.substring(0, separator)), unescape(line.substring(separator + 1)));
            }
        }
        return Map.copyOf(result);
    }

    private static String escape(String value) {
        return clean(value).replace("%", "%25").replace("\n", "%0A").replace("=", "%3D");
    }

    private static String unescape(String value) {
        return clean(value).replace("%3D", "=").replace("%0A", "\n").replace("%25", "%");
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

    private static OffsetDateTime toOffsetDateTime(Object value) {
        if (value instanceof Timestamp timestamp) {
            return offset(timestamp);
        }
        if (value instanceof OffsetDateTime offsetDateTime) {
            return offsetDateTime;
        }
        return OffsetDateTime.now();
    }

    private static Number number(Object value) {
        if (value instanceof Number number) {
            return number;
        }
        return Long.parseLong(String.valueOf(value));
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
