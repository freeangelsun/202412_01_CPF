package com.cpf.notification.dispatch;

import com.cpf.core.api.context.CpfContexts;
import com.cpf.notification.api.CpfNotificationReceipt;
import com.cpf.notification.api.CpfNotificationRequest;
import com.cpf.notification.api.CpfNotificationResult;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.UUID;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

public final class JdbcCpfNotificationOutbox {
    private final JdbcTemplate jdbc;
    private final Clock clock;
    private final CpfNotificationContextCodec contextCodec;

    public JdbcCpfNotificationOutbox(JdbcTemplate jdbc, Clock clock, CpfNotificationContextCodec contextCodec) {
        this.jdbc = java.util.Objects.requireNonNull(jdbc, "jdbc");
        this.clock = java.util.Objects.requireNonNull(clock, "clock");
        this.contextCodec = java.util.Objects.requireNonNull(contextCodec, "contextCodec");
    }

    public CpfNotificationResult enqueue(CpfNotificationRequest request) {
        try {
            jdbc.update("""
                    INSERT INTO cpf_notification_outbox(
                      notification_id, channel_code, recipient_value, template_id, variable_json,
                      idempotency_key, transaction_id, context_lineage, notification_status, attempt_count, max_attempts,
                      next_attempt_at, created_at, updated_at)
                    VALUES(?,?,?,?,?,?,?,?,'PENDING',0,5,?,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)
                    """,
                    request.notificationId(), request.channel(), request.recipient(), request.templateId(),
                    encode(request.variables()), request.idempotencyKey(), CpfContexts.transactionId(), contextCodec.capture(),
                    request.notBefore() == null ? null : Timestamp.from(request.notBefore()));
            return new CpfNotificationResult(
                    request.notificationId(), "OUTBOX", "ACCEPTED", null, null, Instant.now(clock));
        } catch (DuplicateKeyException exception) {
            List<Map<String, Object>> rows = jdbc.queryForList("""
                    SELECT notification_id AS notificationId, notification_status AS status,
                           channel_code AS channelCode, recipient_value AS recipientValue,
                           template_id AS templateId, variable_json AS variableJson,
                           transaction_id AS transactionId, provider_name AS providerName,
                           provider_message_id AS providerMessageId, result_detail AS detail,
                           updated_at AS updatedAt
                      FROM cpf_notification_outbox WHERE idempotency_key=?
                    """, request.idempotencyKey());
            if (rows.isEmpty()) throw exception;
            Map<String, Object> row = rows.getFirst();
            assertSameIdempotentRequest(request, row);
            return new CpfNotificationResult(
                    String.valueOf(row.get("notificationId")), nullable(row.get("providerName")),
                    String.valueOf(row.get("status")), nullable(row.get("providerMessageId")),
                    nullable(row.get("detail")), Instant.now(clock));
        }
    }

    public List<CpfNotificationRequest> claim(String workerId, int limit) {
        return claim(workerId, limit, Instant.now(clock), Duration.ofSeconds(30));
    }

    /** PENDING과 lease가 만료된 CLAIMED를 원자적으로 재선점합니다. */
    public List<CpfNotificationRequest> claim(
            String workerId, int limit, Instant now, Duration leaseDuration) {
        requireClaimArguments(workerId, now, leaseDuration);
        int boundedLimit = Math.max(1, Math.min(limit, 500));
        List<Map<String, Object>> rows = jdbc.queryForList("""
                SELECT notification_id, channel_code, recipient_value, template_id, variable_json,
                       idempotency_key, transaction_id, context_lineage, attempt_count, next_attempt_at
                  FROM cpf_notification_outbox
                 WHERE (
                        notification_status='PENDING'
                        AND (next_attempt_at IS NULL OR next_attempt_at<=CURRENT_TIMESTAMP)
                       )
                    OR (
                        notification_status='CLAIMED'
                        AND lease_until<CURRENT_TIMESTAMP
                       )
                 ORDER BY created_at
                """);
        List<ClaimedNotification> claimed = new ArrayList<>();
        for (Map<String, Object> row : rows.stream().limit(boundedLimit).toList()) {
            String notificationId = String.valueOf(row.get("notification_id"));
            int updated = jdbc.update("""
                    UPDATE cpf_notification_outbox
                       SET notification_status='CLAIMED', worker_id=?, lease_until=?, updated_at=CURRENT_TIMESTAMP
                     WHERE notification_id=?
                       AND (
                            (notification_status='PENDING' AND (next_attempt_at IS NULL OR next_attempt_at<=CURRENT_TIMESTAMP))
                            OR (notification_status='CLAIMED' AND lease_until<CURRENT_TIMESTAMP)
                           )
                    """, workerId, Timestamp.from(now.plus(leaseDuration)), notificationId);
            if (updated == 1) claimed.add(toClaimed(row));
        }
        return claimed.stream().map(ClaimedNotification::request).toList();
    }

    public List<ClaimedNotification> claimWithContext(String workerId,int limit,Instant now,Duration leaseDuration) {
        requireClaimArguments(workerId,now,leaseDuration);int boundedLimit=Math.max(1,Math.min(limit,500));List<Map<String,Object>> rows=jdbc.queryForList("""
                SELECT notification_id, channel_code, recipient_value, template_id, variable_json,
                       idempotency_key, transaction_id, context_lineage, attempt_count, next_attempt_at
                  FROM cpf_notification_outbox
                 WHERE (notification_status='PENDING' AND (next_attempt_at IS NULL OR next_attempt_at<=CURRENT_TIMESTAMP))
                    OR (notification_status='CLAIMED' AND lease_until<CURRENT_TIMESTAMP)
                 ORDER BY created_at
                """);List<ClaimedNotification> claimed=new ArrayList<>();for(Map<String,Object> row:rows.stream().limit(boundedLimit).toList()){String id=String.valueOf(row.get("notification_id"));int updated=jdbc.update("""
                    UPDATE cpf_notification_outbox SET notification_status='CLAIMED', worker_id=?, lease_until=?, updated_at=CURRENT_TIMESTAMP
                     WHERE notification_id=? AND ((notification_status='PENDING' AND (next_attempt_at IS NULL OR next_attempt_at<=CURRENT_TIMESTAMP)) OR (notification_status='CLAIMED' AND lease_until<CURRENT_TIMESTAMP))
                    """,workerId,Timestamp.from(now.plus(leaseDuration)),id);if(updated==1)claimed.add(toClaimed(row));}return List.copyOf(claimed);
    }

    public List<CpfNotificationRequest> claimUnknownForReconcile(
            String workerId, int limit, Instant now, Duration leaseDuration) {
        requireClaimArguments(workerId, now, leaseDuration);
        int boundedLimit = Math.max(1, Math.min(limit, 500));
        List<Map<String, Object>> rows = jdbc.queryForList("""
                SELECT notification_id, channel_code, recipient_value, template_id, variable_json,
                       idempotency_key, transaction_id, context_lineage, attempt_count, next_attempt_at
                  FROM cpf_notification_outbox
                 WHERE (
                        notification_status='UNKNOWN_RESULT'
                        AND next_attempt_at IS NOT NULL
                        AND next_attempt_at<=CURRENT_TIMESTAMP
                       )
                    OR (
                        notification_status='RECONCILING'
                        AND lease_until<CURRENT_TIMESTAMP
                       )
                 ORDER BY updated_at
                """);
        List<ClaimedNotification> claimed = new ArrayList<>();
        for (Map<String, Object> row : rows.stream().limit(boundedLimit).toList()) {
            String notificationId = String.valueOf(row.get("notification_id"));
            int updated = jdbc.update("""
                    UPDATE cpf_notification_outbox
                       SET notification_status='RECONCILING', worker_id=?, lease_until=?, updated_at=CURRENT_TIMESTAMP
                     WHERE notification_id=?
                       AND (
                            (notification_status='UNKNOWN_RESULT' AND next_attempt_at IS NOT NULL
                             AND next_attempt_at<=CURRENT_TIMESTAMP)
                            OR (notification_status='RECONCILING' AND lease_until<CURRENT_TIMESTAMP)
                           )
                    """, workerId, Timestamp.from(now.plus(leaseDuration)), notificationId);
            if (updated == 1) claimed.add(toClaimed(row));
        }
        return claimed.stream().map(ClaimedNotification::request).toList();
    }

    public List<ClaimedNotification> claimUnknownWithContext(String workerId,int limit,Instant now,Duration leaseDuration) {
        requireClaimArguments(workerId,now,leaseDuration);int boundedLimit=Math.max(1,Math.min(limit,500));List<Map<String,Object>> rows=jdbc.queryForList("""
                SELECT notification_id, channel_code, recipient_value, template_id, variable_json,
                       idempotency_key, transaction_id, context_lineage, attempt_count, next_attempt_at
                  FROM cpf_notification_outbox
                 WHERE (notification_status='UNKNOWN_RESULT' AND next_attempt_at IS NOT NULL AND next_attempt_at<=CURRENT_TIMESTAMP)
                    OR (notification_status='RECONCILING' AND lease_until<CURRENT_TIMESTAMP)
                 ORDER BY updated_at
                """);List<ClaimedNotification> claimed=new ArrayList<>();for(Map<String,Object> row:rows.stream().limit(boundedLimit).toList()){String id=String.valueOf(row.get("notification_id"));int updated=jdbc.update("""
                    UPDATE cpf_notification_outbox SET notification_status='RECONCILING',worker_id=?,lease_until=?,updated_at=CURRENT_TIMESTAMP
                     WHERE notification_id=? AND ((notification_status='UNKNOWN_RESULT' AND next_attempt_at IS NOT NULL AND next_attempt_at<=CURRENT_TIMESTAMP) OR (notification_status='RECONCILING' AND lease_until<CURRENT_TIMESTAMP))
                    """,workerId,Timestamp.from(now.plus(leaseDuration)),id);if(updated==1)claimed.add(toClaimed(row));}return List.copyOf(claimed);
    }

    public void complete(CpfNotificationResult result) {
        completeFrom(result, "CLAIMED");
    }

    public void completeReconcile(CpfNotificationResult result) {
        completeFrom(result, "RECONCILING");
    }

    private void completeFrom(CpfNotificationResult result, String expectedState) {
        int updated = jdbc.update("""
                UPDATE cpf_notification_outbox
                   SET notification_status=?, provider_name=?, provider_message_id=?, result_detail=?,
                       attempt_count=attempt_count+1, worker_id=NULL, lease_until=NULL,
                       completed_at=?, next_attempt_at=NULL, updated_at=CURRENT_TIMESTAMP
                 WHERE notification_id=? AND notification_status=?
                """, result.status(), result.provider(), result.providerMessageId(), result.detail(),
                Timestamp.from(result.processedAt()), result.notificationId(), expectedState);
        if (updated != 1) throw new IllegalStateException("notification outbox state conflict: " + result.notificationId());
    }

    public void markUnknown(CpfNotificationResult result, Instant reconcileAt) {
        int updated = jdbc.update("""
                UPDATE cpf_notification_outbox
                   SET notification_status=CASE
                           WHEN attempt_count+1>=max_attempts THEN 'DLQ'
                           ELSE 'UNKNOWN_RESULT'
                       END,
                       provider_name=?, provider_message_id=?, result_detail=?,
                       attempt_count=attempt_count+1,
                       next_attempt_at=CASE WHEN attempt_count+1>=max_attempts THEN NULL ELSE ? END,
                       worker_id=NULL, lease_until=NULL, updated_at=CURRENT_TIMESTAMP
                 WHERE notification_id=? AND notification_status IN ('CLAIMED','RECONCILING')
                """, result.provider(), result.providerMessageId(), result.detail(), Timestamp.from(reconcileAt),
                result.notificationId());
        if (updated != 1) throw new IllegalStateException("notification unknown-result state conflict: " + result.notificationId());
    }

    public void retry(String notificationId, String detail, Instant nextAttemptAt) {
        retryFrom(notificationId, detail, nextAttemptAt, List.of("CLAIMED", "RECONCILING"));
    }

    private void retryFrom(String notificationId, String detail, Instant nextAttemptAt, List<String> allowedStates) {
        String inClause = String.join(",", allowedStates.stream().map(state -> "'" + state + "'").toList());
        int updated = jdbc.update("""
                UPDATE cpf_notification_outbox
                   SET notification_status=CASE WHEN attempt_count+1>=max_attempts THEN 'DLQ' ELSE 'PENDING' END,
                       attempt_count=attempt_count+1, result_detail=?, next_attempt_at=?,
                       worker_id=NULL, lease_until=NULL, updated_at=CURRENT_TIMESTAMP
                 WHERE notification_id=? AND notification_status IN (""" + inClause + ")",
                detail, Timestamp.from(nextAttemptAt), notificationId);
        if (updated != 1) throw new IllegalStateException("notification retry conflict: " + notificationId);
    }

    @Transactional
    public void approveReprocess(String notificationId, String operatorId, String reason, Instant now) {
        requireOperatorReason(operatorId, reason);
        String before = currentStatus(notificationId);
        if (!("DLQ".equals(before) || "UNKNOWN_RESULT".equals(before) || "FAILED".equals(before))) {
            throw new IllegalStateException("notification is not reprocessable: " + notificationId + " status=" + before);
        }
        int updated = jdbc.update("""
                UPDATE cpf_notification_outbox
                   SET notification_status='PENDING', next_attempt_at=?, worker_id=NULL, lease_until=NULL,
                       result_detail=?, updated_at=CURRENT_TIMESTAMP
                 WHERE notification_id=? AND notification_status=?
                """, Timestamp.from(now), "REPROCESS_APPROVED:" + safe(reason), notificationId, before);
        if (updated != 1) throw new IllegalStateException("notification reprocess conflict: " + notificationId);
        audit(notificationId, "APPROVE_REPROCESS", operatorId, reason, before, "PENDING", now);
    }

    @Transactional
    public void recordReceipt(CpfNotificationReceipt receipt, String operatorId) {
        if (operatorId == null || operatorId.isBlank()) throw new IllegalArgumentException("operatorId is required");
        try {
            jdbc.update("""
                    INSERT INTO cpf_notification_receipt(
                      receipt_id, notification_id, provider_name, receipt_status, receipt_detail, received_at, created_at)
                    VALUES(?,?,?,?,?,?,CURRENT_TIMESTAMP)
                    """, receipt.receiptId(), receipt.notificationId(), receipt.provider(), receipt.receiptStatus(),
                    receipt.detail(), Timestamp.from(receipt.receivedAt()));
        } catch (DuplicateKeyException duplicate) {
            List<Map<String, Object>> existing = jdbc.queryForList("""
                    SELECT notification_id, provider_name, receipt_status, receipt_detail, received_at
                      FROM cpf_notification_receipt WHERE receipt_id=?
                    """, receipt.receiptId());
            if (existing.size() != 1 || !sameReceipt(receipt, existing.getFirst())) {
                throw new IllegalStateException("receipt idempotency conflict: " + receipt.receiptId(), duplicate);
            }
            return;
        }
        String before = currentStatus(receipt.notificationId());
        String after = switch (receipt.receiptStatus()) {
            case "DELIVERED" -> "DELIVERED";
            case "BOUNCED", "REJECTED" -> "FAILED";
            default -> before;
        };
        if (!after.equals(before)) {
            jdbc.update("""
                    UPDATE cpf_notification_outbox
                       SET notification_status=?, result_detail=?, updated_at=CURRENT_TIMESTAMP
                     WHERE notification_id=?
                    """, after, safe(receipt.detail()), receipt.notificationId());
        }
        audit(receipt.notificationId(), "RECEIPT_" + receipt.receiptStatus(), operatorId,
                receipt.detail(), before, after, receipt.receivedAt());
    }

    public CpfNotificationResult currentResult(String notificationId) {
        Map<String, Object> row = jdbc.queryForMap("""
                SELECT notification_id, notification_status, provider_name, provider_message_id,
                       result_detail, updated_at
                  FROM cpf_notification_outbox WHERE notification_id=?
                """, notificationId);
        return new CpfNotificationResult(
                String.valueOf(row.get("notification_id")), nullable(row.get("provider_name")),
                String.valueOf(row.get("notification_status")), nullable(row.get("provider_message_id")),
                nullable(row.get("result_detail")), instantOrNow(row.get("updated_at")));
    }

    private String currentStatus(String notificationId) {
        List<String> values = jdbc.queryForList(
                "SELECT notification_status FROM cpf_notification_outbox WHERE notification_id=?",
                String.class, notificationId);
        if (values.size() != 1) throw new IllegalStateException("notification not found or duplicated: " + notificationId);
        return values.getFirst();
    }

    private void audit(String notificationId, String operation, String operatorId, String reason,
                       String before, String after, Instant at) {
        jdbc.update("""
                INSERT INTO cpf_notification_operation_audit(
                  operation_id, notification_id, operation_code, operator_id, operation_reason,
                  before_status, after_status, created_at)
                VALUES(?,?,?,?,?,?,?,?)
                """, UUID.randomUUID().toString(), notificationId, operation, operatorId,
                safeRequired(reason), before, after, Timestamp.from(at));
    }

    private static ClaimedNotification toClaimed(Map<String,Object> row) {
        return new ClaimedNotification(toRequest(row), nullable(row.get("context_lineage")),
                row.get("attempt_count") instanceof Number n ? n.intValue() : 0);
    }

    public record ClaimedNotification(CpfNotificationRequest request,String contextLineage,int attemptCount) {}

    private static CpfNotificationRequest toRequest(Map<String, Object> row) {
        return new CpfNotificationRequest(
                String.valueOf(row.get("notification_id")), String.valueOf(row.get("channel_code")),
                String.valueOf(row.get("recipient_value")), String.valueOf(row.get("template_id")),
                decode(nullable(row.get("variable_json"))), String.valueOf(row.get("idempotency_key")), null);
    }


    private static void requireClaimArguments(String workerId, Instant now, Duration leaseDuration) {
        if (workerId == null || workerId.isBlank()) {
            throw new IllegalArgumentException("workerId is required");
        }
        if (now == null) {
            throw new IllegalArgumentException("now is required");
        }
        if (leaseDuration == null || leaseDuration.isZero() || leaseDuration.isNegative()) {
            throw new IllegalArgumentException("leaseDuration must be positive");
        }
    }

    private static void assertSameIdempotentRequest(
            CpfNotificationRequest request, Map<String, Object> row) {
        boolean same = request.channel().equals(String.valueOf(row.get("channelCode")))
                && request.recipient().equals(String.valueOf(row.get("recipientValue")))
                && request.templateId().equals(String.valueOf(row.get("templateId")))
                && encode(request.variables()).equals(nullable(row.get("variableJson")));
        if (!same) {
            throw new IllegalStateException(
                    "notification idempotency conflict: " + request.idempotencyKey());
        }
    }

    private static boolean sameReceipt(CpfNotificationReceipt receipt, Map<String, Object> row) {
        return receipt.notificationId().equals(String.valueOf(row.get("notification_id")))
                && receipt.provider().equals(String.valueOf(row.get("provider_name")))
                && receipt.receiptStatus().equals(String.valueOf(row.get("receipt_status")))
                && java.util.Objects.equals(receipt.detail(), nullable(row.get("receipt_detail")))
                && receipt.receivedAt().equals(instant(row.get("received_at")));
    }

    private Instant instantOrNow(Object value) {
        if (value instanceof Timestamp timestamp) {
            return timestamp.toInstant();
        }
        if (value instanceof Instant instant) {
            return instant;
        }
        return Instant.now(clock);
    }

    private static Instant instant(Object value) {
        if (value instanceof Timestamp timestamp) {
            return timestamp.toInstant();
        }
        if (value instanceof Instant instant) {
            return instant;
        }
        throw new IllegalStateException("timestamp value is missing or unsupported: " + value);
    }

    private static String safeRequired(String value) {
        String sanitized = safe(value);
        return sanitized == null || sanitized.isBlank() ? "N/A" : sanitized;
    }

    private static void requireOperatorReason(String operatorId, String reason) {
        if (operatorId == null || operatorId.isBlank()) throw new IllegalArgumentException("operatorId is required");
        if (reason == null || reason.isBlank()) throw new IllegalArgumentException("reason is required");
    }

    private static String safe(String value) {
        if (value == null) return null;
        return value.substring(0, Math.min(1000, value.length()));
    }

    private static String nullable(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private static String encode(Map<String, String> values) {
        StringJoiner joiner = new StringJoiner("&");
        values.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> joiner.add(
                        encodePart(entry.getKey()) + "=" + encodePart(entry.getValue())));
        return joiner.toString();
    }

    private static String encodePart(String value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private static Map<String, String> decode(String value) {
        if (value == null || value.isBlank() || "null".equals(value)) return Map.of();
        Map<String, String> decoded = new LinkedHashMap<>();
        for (String entry : value.split("&")) {
            String[] pair = entry.split("=", 2);
            if (pair.length == 2) {
                decoded.put(decodePart(pair[0]), decodePart(pair[1]));
            }
        }
        return Map.copyOf(decoded);
    }

    private static String decodePart(String value) {
        return new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8);
    }
}
