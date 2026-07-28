package com.cpf.admin.opr.service;

import com.cpf.admin.opr.dto.AdmNotificationRuleResponse;
import com.cpf.admin.opr.dto.AdmNotificationTestSendRequest;
import com.cpf.admin.opr.dto.NotificationSendResult;
import com.cpf.core.api.error.CpfValidationException;
import com.cpf.core.api.util.CpfStrings;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * 운영 알림을 원 업무 트랜잭션과 분리하는 Durable Outbox입니다.
 *
 * <p>요청은 먼저 {@code cpf_notification_delivery_log}에 READY 상태로 저장됩니다. Worker는
 * version/CAS와 lease를 사용해 한 인스턴스만 발송을 소유하며, Provider timeout·실패·응답 유실을
 * RETRY·FAILED·UNKNOWN_RESULT로 구분합니다. 외부 Provider 호출은 DB transaction 밖에서 수행합니다.</p>
 */
@Service
public class AdmNotificationOutboxService {
    private static final String SENSITIVE_PATTERN =
            "(?i).*(password|passwd|secret|private[ _-]?key|authorization:|bearer\\s+|access[_-]?token|refresh[_-]?token).*";

    private final JdbcTemplate jdbcTemplate;
    private final NotificationSender notificationSender;
    private final TransactionTemplate transactionTemplate;
    private final Duration leaseDuration;
    private final Duration retryDelay;
    private final int maxAttempts;

    public AdmNotificationOutboxService(
            @Qualifier("cpfJdbcTemplate") JdbcTemplate jdbcTemplate,
            NotificationSender notificationSender,
            PlatformTransactionManager transactionManager,
            @Value("${cpf.notification.outbox.lease-seconds:60}") long leaseSeconds,
            @Value("${cpf.notification.outbox.retry-delay-seconds:30}") long retryDelaySeconds,
            @Value("${cpf.notification.outbox.max-attempts:3}") int maxAttempts) {
        this.jdbcTemplate = jdbcTemplate;
        this.notificationSender = notificationSender;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.leaseDuration = Duration.ofSeconds(Math.max(10, leaseSeconds));
        this.retryDelay = Duration.ofSeconds(Math.max(1, retryDelaySeconds));
        this.maxAttempts = Math.max(1, Math.min(maxAttempts, 20));
    }

    public long enqueueTest(
            AdmNotificationRuleResponse rule,
            AdmNotificationTestSendRequest request,
            String requestUser) {
        String targetType = defaultText(request.targetType(), "ADM_TEST");
        String targetId = defaultText(request.targetId(), "TEST");
        String receiver = defaultText(request.receiver(), defaultText(rule.receiverGroup(), "ADM_OPERATOR"));
        String payload = defaultText(request.message(), "ADM 운영 알림 테스트 발송입니다.");
        validatePayload(payload);
        String operationId = UUID.randomUUID().toString();
        String requestHash = sha256(rule.ruleId() + "|" + targetType + "|" + targetId + "|" + receiver + "|" + payload);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        int updated = jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement("""
                    INSERT INTO cpf_notification_delivery_log (
                        rule_id, event_type, target_type, target_id, receiver,
                        delivery_status, delivery_message, requested_at, delivered_at,
                        operation_id, request_hash, payload_body, attempt_count, max_attempts,
                        next_attempt_at, lease_owner, lease_until, version, last_error_code,
                        created_by, updated_by
                    )
                    VALUES (?, ?, ?, ?, ?, 'READY', ?, ?, NULL, ?, ?, ?, 0, ?, ?, NULL, NULL, 0, NULL, ?, ?)
                    """, new String[] {"delivery_id"});
            statement.setLong(1, rule.ruleId());
            statement.setString(2, rule.eventType());
            statement.setString(3, targetType);
            statement.setString(4, targetId);
            statement.setString(5, receiver);
            statement.setString(6, "Queued for durable delivery");
            Timestamp now = Timestamp.from(Instant.now());
            statement.setTimestamp(7, now);
            statement.setString(8, operationId);
            statement.setString(9, requestHash);
            statement.setString(10, payload);
            statement.setInt(11, maxAttempts);
            statement.setTimestamp(12, now);
            statement.setString(13, requestUser);
            statement.setString(14, requestUser);
            return statement;
        }, keyHolder);
        if (updated != 1) {
            throw new IllegalStateException("알림 Outbox 생성 결과가 1건이 아닙니다. updated=" + updated);
        }
        Number key = generatedNumber(keyHolder, "delivery_id");
        if (key == null) {
            Long deliveryId = jdbcTemplate.queryForObject(
                    "SELECT delivery_id FROM cpf_notification_delivery_log WHERE operation_id = ?",
                    Long.class,
                    operationId);
            if (deliveryId == null) {
                throw new IllegalStateException("알림 Outbox deliveryId를 확인할 수 없습니다.");
            }
            return deliveryId;
        }
        return key.longValue();
    }

    /** due 항목을 claim하고 Provider 호출·결과 확정을 수행합니다. */
    public int processDue(String workerId, int requestedLimit) {
        String owner = required(workerId, "workerId");
        int limit = Math.max(1, Math.min(requestedLimit, 100));
        List<Candidate> candidates = transactionTemplate.execute(status -> findCandidates(limit));
        if (candidates == null || candidates.isEmpty()) {
            return 0;
        }
        int processed = 0;
        for (Candidate candidate : candidates) {
            ClaimedDelivery claimed = transactionTemplate.execute(status -> claim(candidate, owner));
            if (claimed == null) {
                continue;
            }
            NotificationSendResult sendResult;
            try {
                sendResult = notificationSender.send(
                        claimed.rule(),
                        claimed.targetType(),
                        claimed.targetId(),
                        claimed.receiver(),
                        claimed.payloadBody(),
                        claimed.requestUser());
                if (sendResult == null) {
                    sendResult = new NotificationSendResult(false, "UNKNOWN_RESULT", "Provider returned null", null);
                }
            } catch (RuntimeException providerFailure) {
                sendResult = new NotificationSendResult(
                        false,
                        "PROVIDER_EXCEPTION",
                        safeMessage(providerFailure),
                        null);
            }
            NotificationSendResult finalResult = sendResult;
            transactionTemplate.executeWithoutResult(status -> complete(claimed, owner, finalResult));
            processed++;
        }
        return processed;
    }

    public Map<String, Object> retry(long deliveryId, String operatorId) {
        int updated = jdbcTemplate.update("""
                UPDATE cpf_notification_delivery_log
                SET delivery_status = 'RETRY',
                    next_attempt_at = ?,
                    lease_owner = NULL,
                    lease_until = NULL,
                    version = version + 1,
                    updated_by = ?,
                    updated_at = CURRENT_TIMESTAMP
                WHERE delivery_id = ?
                  AND delivery_status IN ('FAILED', 'UNKNOWN_RESULT', 'CANCELLED')
                """, Timestamp.from(Instant.now()), required(operatorId, "operatorId"), deliveryId);
        if (updated != 1) {
            throw new CpfValidationException("재시도 가능한 알림 발송 건이 아닙니다. deliveryId=" + deliveryId);
        }
        return statusMap(deliveryId, "RETRY");
    }

    public Map<String, Object> cancel(long deliveryId, String operatorId) {
        int updated = jdbcTemplate.update("""
                UPDATE cpf_notification_delivery_log
                SET delivery_status = 'CANCELLED',
                    next_attempt_at = NULL,
                    lease_owner = NULL,
                    lease_until = NULL,
                    version = version + 1,
                    updated_by = ?,
                    updated_at = CURRENT_TIMESTAMP
                WHERE delivery_id = ?
                  AND delivery_status IN ('READY', 'RETRY', 'UNKNOWN_RESULT')
                """, required(operatorId, "operatorId"), deliveryId);
        if (updated != 1) {
            throw new CpfValidationException("취소 가능한 알림 발송 건이 아닙니다. deliveryId=" + deliveryId);
        }
        return statusMap(deliveryId, "CANCELLED");
    }

    private List<Candidate> findCandidates(int limit) {
        Timestamp now = Timestamp.from(Instant.now());
        return jdbcTemplate.query(connection -> {
            PreparedStatement statement = connection.prepareStatement("""
                    SELECT delivery_id, version
                    FROM cpf_notification_delivery_log
                    WHERE delivery_status IN ('READY', 'RETRY')
                      AND (next_attempt_at IS NULL OR next_attempt_at <= ?)
                      AND (lease_until IS NULL OR lease_until < ?)
                    ORDER BY requested_at, delivery_id
                    """);
            statement.setTimestamp(1, now);
            statement.setTimestamp(2, now);
            statement.setMaxRows(limit);
            return statement;
        }, (rs, rowNum) -> new Candidate(rs.getLong("delivery_id"), rs.getLong("version")));
    }

    private ClaimedDelivery claim(Candidate candidate, String owner) {
        Instant now = Instant.now();
        int updated = jdbcTemplate.update("""
                UPDATE cpf_notification_delivery_log
                SET delivery_status = 'PROCESSING',
                    lease_owner = ?,
                    lease_until = ?,
                    attempt_count = attempt_count + 1,
                    version = version + 1,
                    updated_by = ?,
                    updated_at = CURRENT_TIMESTAMP
                WHERE delivery_id = ?
                  AND version = ?
                  AND delivery_status IN ('READY', 'RETRY')
                  AND (lease_until IS NULL OR lease_until < ?)
                """,
                owner,
                Timestamp.from(now.plus(leaseDuration)),
                owner,
                candidate.deliveryId(),
                candidate.version(),
                Timestamp.from(now));
        if (updated != 1) {
            return null;
        }
        return jdbcTemplate.queryForObject("""
                SELECT d.delivery_id, d.target_type, d.target_id, d.receiver, d.payload_body,
                       d.attempt_count, d.max_attempts, d.version, d.created_by,
                       r.rule_id, r.event_type, r.event_sub_type, r.channel_code, r.template_code,
                       r.severity, r.receiver_group, r.use_yn, r.created_by AS rule_created_by,
                       r.created_at AS rule_created_at, r.updated_by AS rule_updated_by,
                       r.updated_at AS rule_updated_at
                FROM cpf_notification_delivery_log d
                JOIN cpf_notification_rule r ON r.rule_id = d.rule_id
                WHERE d.delivery_id = ? AND d.lease_owner = ? AND d.delivery_status = 'PROCESSING'
                """, (rs, rowNum) -> new ClaimedDelivery(
                rs.getLong("delivery_id"),
                rs.getString("target_type"),
                rs.getString("target_id"),
                rs.getString("receiver"),
                rs.getString("payload_body"),
                rs.getInt("attempt_count"),
                rs.getInt("max_attempts"),
                rs.getLong("version"),
                rs.getString("created_by"),
                new AdmNotificationRuleResponse(
                        rs.getLong("rule_id"),
                        rs.getString("event_type"),
                        rs.getString("event_sub_type"),
                        rs.getString("channel_code"),
                        rs.getString("template_code"),
                        rs.getString("severity"),
                        rs.getString("receiver_group"),
                        rs.getString("use_yn"),
                        rs.getString("rule_created_by"),
                        toLocalDateTime(rs.getTimestamp("rule_created_at")),
                        rs.getString("rule_updated_by"),
                        toLocalDateTime(rs.getTimestamp("rule_updated_at")))),
                candidate.deliveryId(), owner);
    }

    private void complete(ClaimedDelivery delivery, String owner, NotificationSendResult result) {
        String providerCode = defaultText(result.deliveryStatus(), "UNKNOWN_RESULT").toUpperCase(Locale.ROOT);
        String finalStatus;
        Timestamp nextAttemptAt = null;
        Timestamp deliveredAt = result.deliveredAt() == null ? null : Timestamp.valueOf(result.deliveredAt());
        if (result.success()) {
            finalStatus = providerCode.startsWith("SIMULATED_") ? providerCode : "SENT";
        } else if (providerCode.contains("UNKNOWN")) {
            finalStatus = "UNKNOWN_RESULT";
        } else if (delivery.attemptCount() < delivery.maxAttempts()) {
            finalStatus = "RETRY";
            nextAttemptAt = Timestamp.from(Instant.now().plus(retryDelay.multipliedBy(delivery.attemptCount())));
        } else {
            finalStatus = "FAILED";
        }
        int updated = jdbcTemplate.update("""
                UPDATE cpf_notification_delivery_log
                SET delivery_status = ?,
                    delivery_message = ?,
                    delivered_at = ?,
                    next_attempt_at = ?,
                    lease_owner = NULL,
                    lease_until = NULL,
                    last_error_code = ?,
                    version = version + 1,
                    updated_by = ?,
                    updated_at = CURRENT_TIMESTAMP
                WHERE delivery_id = ?
                  AND version = ?
                  AND delivery_status = 'PROCESSING'
                  AND lease_owner = ?
                """,
                finalStatus,
                truncate(defaultText(result.deliveryMessage(), finalStatus), 2000),
                deliveredAt,
                nextAttemptAt,
                result.success() ? null : truncate(providerCode, 80),
                owner,
                delivery.deliveryId(),
                delivery.version(),
                owner);
        if (updated != 1) {
            throw new IllegalStateException("알림 발송 결과 CAS 확정에 실패했습니다. deliveryId=" + delivery.deliveryId());
        }
    }

    private Map<String, Object> statusMap(long deliveryId, String status) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("deliveryId", deliveryId);
        response.put("deliveryStatus", status);
        return response;
    }

    private void validatePayload(String payload) {
        if (payload.length() > 2000) {
            throw new CpfValidationException("알림 메시지는 2000자를 초과할 수 없습니다.");
        }
        if (payload.matches(SENSITIVE_PATTERN)) {
            throw new CpfValidationException("Secret·Token·인증정보는 알림 Payload에 포함할 수 없습니다.");
        }
    }

    private String required(String value, String name) {
        if (!CpfStrings.hasText(value)) {
            throw new CpfValidationException(name + " 값은 필수입니다.");
        }
        return value.trim();
    }

    private String defaultText(String value, String fallback) {
        return CpfStrings.hasText(value) ? value.trim() : fallback;
    }

    private String safeMessage(Throwable throwable) {
        String message = throwable.getMessage();
        return truncate(throwable.getClass().getSimpleName() + (message == null ? "" : ": " + message), 2000);
    }

    private String truncate(String value, int max) {
        if (value == null || value.length() <= max) {
            return value;
        }
        return value.substring(0, max);
    }

    private String sha256(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("SHA-256 request hash 생성 실패", ex);
        }
    }

    private Number generatedNumber(KeyHolder keyHolder, String columnName) {
        try {
            Number key = keyHolder.getKey();
            if (key != null) {
                return key;
            }
        } catch (org.springframework.dao.InvalidDataAccessApiUsageException ignored) {
            // 이름 기반 generated key 확인으로 계속합니다.
        }
        Map<String, Object> keys = keyHolder.getKeys();
        if (keys == null || keys.isEmpty()) {
            return null;
        }
        Object value = keys.get(columnName);
        if (value == null) {
            value = keys.get(columnName.toUpperCase(Locale.ROOT));
        }
        if (value instanceof Number number) {
            return number;
        }
        return value == null ? null : Long.valueOf(String.valueOf(value));
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    private record Candidate(long deliveryId, long version) {
    }

    private record ClaimedDelivery(
            long deliveryId,
            String targetType,
            String targetId,
            String receiver,
            String payloadBody,
            int attemptCount,
            int maxAttempts,
            long version,
            String requestUser,
            AdmNotificationRuleResponse rule) {
    }
}
