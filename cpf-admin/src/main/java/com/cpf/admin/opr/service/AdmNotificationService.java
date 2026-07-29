package com.cpf.admin.opr.service;

import com.cpf.admin.opr.dto.AdmNotificationDeliveryAttemptResponse;
import com.cpf.admin.opr.dto.AdmNotificationDeliveryLogResponse;
import com.cpf.admin.opr.dto.AdmNotificationRuleRequest;
import com.cpf.admin.opr.dto.AdmNotificationRuleResponse;
import com.cpf.admin.opr.dto.AdmNotificationDeliveryStatusResponse;
import com.cpf.admin.opr.dto.AdmNotificationTestSendResponse;
import com.cpf.admin.opr.dto.AdmNotificationTestSendRequest;
import com.cpf.admin.opr.dto.NotificationSendResult;
import com.cpf.core.api.util.CpfStrings;
import com.cpf.core.api.error.CpfValidationException;
import com.cpf.core.api.logging.CpfTransactionContext;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ADM 운영 알림 규칙과 발송 이력을 관리합니다.
 *
 * <p>공통 Runtime Repository에서 특정 DB 전용 조회 제한·upsert·생성 ID 문법을
 * 사용하지 않습니다. 조회 제한은 JDBC {@link PreparedStatement#setMaxRows(int)}, 생성 ID는
 * JDBC generated key 계약으로 처리하여 Oracle·PostgreSQL·MariaDB에서 동일한 코드 경로를 사용합니다.</p>
 */
@Service
public class AdmNotificationService extends com.cpf.admin.common.base.AdmBaseService {
    private static final String RULE_SELECT = """
            SELECT rule_id, event_type, event_sub_type, channel_code, template_code,
                   severity, receiver_group, use_yn, created_by, created_at, updated_by, updated_at
            FROM cpf_notification_rule
            """;
    private final JdbcTemplate cpfJdbcTemplate;
    private final AdmAuditLogService auditLogService;
    private final AdmNotificationOutboxService notificationOutboxService;

    public AdmNotificationService(
            @Qualifier("cpfJdbcTemplate") JdbcTemplate cpfJdbcTemplate,
            AdmAuditLogService auditLogService,
            AdmNotificationOutboxService notificationOutboxService) {
        this.cpfJdbcTemplate = cpfJdbcTemplate;
        this.auditLogService = auditLogService;
        this.notificationOutboxService = notificationOutboxService;
    }

    public List<AdmNotificationRuleResponse> findRules(int limit) {
        return queryWithMaxRows(
                RULE_SELECT + " ORDER BY use_yn DESC, severity DESC, rule_id DESC",
                resolveLimit(limit),
                (rs, rowNum) -> toRule(rs));
    }

    public AdmNotificationRuleResponse findRule(long ruleId) {
        return cpfJdbcTemplate.queryForObject(
                RULE_SELECT + " WHERE rule_id = ?",
                (rs, rowNum) -> toRule(rs),
                ruleId);
    }

    @Transactional
    public AdmNotificationRuleResponse saveRule(
            Long ruleId,
            AdmNotificationRuleRequest request,
            String operatorId,
            String clientIp) {
        String reason = auditLogService.requireReason(request.reason());
        String eventType = required(request.eventType(), "eventType");
        String eventSubType = blankToNull(request.eventSubType());
        String channelCode = defaultText(request.channelCode(), "ADM");
        String requestUser = required(operatorId, "operatorId");
        Map<String, Object> before;
        long targetRuleId;

        if (ruleId == null) {
            before = findRuleMapByBusinessKey(eventType, eventSubType, channelCode);
            if (before.isEmpty()) {
                try {
                    targetRuleId = insertRule(request, eventType, eventSubType, channelCode, requestUser);
                } catch (DuplicateKeyException concurrentInsert) {
                    before = findRuleMapByBusinessKey(eventType, eventSubType, channelCode);
                    if (before.isEmpty()) {
                        throw concurrentInsert;
                    }
                    targetRuleId = longValue(before.get("rule_id"));
                    updateRule(targetRuleId, request, eventType, eventSubType, channelCode, requestUser);
                }
            } else {
                targetRuleId = longValue(before.get("rule_id"));
                updateRule(targetRuleId, request, eventType, eventSubType, channelCode, requestUser);
            }
        } else {
            before = findRuleMapById(ruleId);
            if (before.isEmpty()) {
                throw new EmptyResultDataAccessException("알림 규칙을 찾을 수 없습니다. ruleId=" + ruleId, 1);
            }
            targetRuleId = ruleId;
            updateRule(targetRuleId, request, eventType, eventSubType, channelCode, requestUser);
        }

        Map<String, Object> after = findRuleMapById(targetRuleId);
        auditLogService.record(
                CpfTransactionContext.transactionId(),
                requestUser,
                before.isEmpty() ? "NOTIFICATION_RULE_CREATE" : "NOTIFICATION_RULE_UPDATE",
                "cpf_notification_rule",
                String.valueOf(targetRuleId),
                reason,
                before.isEmpty() ? null : String.valueOf(before),
                String.valueOf(after),
                null,
                clientIp);
        return findRule(targetRuleId);
    }

    @Transactional
    public AdmNotificationRuleResponse disableRule(long ruleId, String reason, String operatorId, String clientIp) {
        String auditReason = auditLogService.requireReason(reason);
        String requestUser = required(operatorId, "operatorId");
        Map<String, Object> before = findRuleMapById(ruleId);
        if (before.isEmpty()) {
            throw new EmptyResultDataAccessException("알림 규칙을 찾을 수 없습니다. ruleId=" + ruleId, 1);
        }
        int updated = cpfJdbcTemplate.update("""
                UPDATE cpf_notification_rule
                SET use_yn = 'N',
                    updated_by = ?,
                    updated_at = CURRENT_TIMESTAMP
                WHERE rule_id = ?
                """, requestUser, ruleId);
        if (updated != 1) {
            throw new IllegalStateException("알림 규칙 비활성화 결과가 1건이 아닙니다. updated=" + updated);
        }
        Map<String, Object> after = findRuleMapById(ruleId);
        auditLogService.record(
                CpfTransactionContext.transactionId(),
                requestUser,
                "NOTIFICATION_RULE_DISABLE",
                "cpf_notification_rule",
                String.valueOf(ruleId),
                auditReason,
                String.valueOf(before),
                String.valueOf(after),
                null,
                clientIp);
        return findRule(ruleId);
    }

    public List<AdmNotificationDeliveryLogResponse> findDeliveryLogs(int limit) {
        return queryWithMaxRows("""
                SELECT delivery_id, rule_id, event_type, target_type, target_id,
                       receiver, delivery_status, delivery_message,
                       operation_id, request_hash, attempt_count, max_attempts,
                       next_attempt_at, lease_owner, lease_until, version, last_error_code,
                       created_by, updated_by, requested_at, delivered_at, created_at, updated_at
                FROM cpf_notification_delivery_log
                ORDER BY requested_at DESC, delivery_id DESC
                """, resolveLimit(limit), (rs, rowNum) -> new AdmNotificationDeliveryLogResponse(
                rs.getLong("delivery_id"),
                objectLong(rs.getObject("rule_id")),
                rs.getString("event_type"),
                rs.getString("target_type"),
                rs.getString("target_id"),
                maskReceiver(rs.getString("receiver")),
                rs.getString("delivery_status"),
                redactSensitiveText(rs.getString("delivery_message")),
                rs.getString("operation_id"),
                rs.getString("request_hash"),
                rs.getInt("attempt_count"),
                rs.getInt("max_attempts"),
                toLocalDateTime(rs.getTimestamp("next_attempt_at")),
                rs.getString("lease_owner"),
                toLocalDateTime(rs.getTimestamp("lease_until")),
                rs.getLong("version"),
                rs.getString("last_error_code"),
                rs.getString("created_by"),
                rs.getString("updated_by"),
                toLocalDateTime(rs.getTimestamp("requested_at")),
                toLocalDateTime(rs.getTimestamp("delivered_at")),
                toLocalDateTime(rs.getTimestamp("created_at")),
                toLocalDateTime(rs.getTimestamp("updated_at"))));
    }

    @Transactional
    public AdmNotificationTestSendResponse sendTest(
            long ruleId,
            AdmNotificationTestSendRequest request,
            String operatorId,
            String clientIp) {
        String reason = auditLogService.requireReason(request.reason());
        String requestUser = required(operatorId, "operatorId");
        AdmNotificationRuleResponse rule = findRule(ruleId);
        long deliveryId = notificationOutboxService.enqueueTest(rule, request, requestUser);
        auditLogService.record(
                CpfTransactionContext.transactionId(),
                requestUser,
                "NOTIFICATION_TEST_ENQUEUE",
                "cpf_notification_delivery_log",
                String.valueOf(deliveryId),
                reason,
                null,
                "{status=READY}",
                null,
                clientIp);

        return new AdmNotificationTestSendResponse(deliveryId, rule, "READY", "QUEUED_NOT_PROVIDER_RESULT");
    }

    /**
     * 선택한 발송 건의 Provider 호출 Attempt를 시간순으로 조회합니다.
     *
     * <p>각 Attempt는 immutable 이력이며 재시도 성공 후에도 이전 실패·결과 불명 이력을
     * 덮어쓰지 않습니다.</p>
     */
    public List<AdmNotificationDeliveryAttemptResponse> findDeliveryAttempts(long deliveryId, int limit) {
        return cpfJdbcTemplate.query(connection -> {
            PreparedStatement statement = connection.prepareStatement("""
                    SELECT delivery_id, attempt_no, operation_id, worker_id, attempt_status,
                           provider_status, provider_message, started_at, completed_at,
                           lease_version, created_by, created_at
                    FROM cpf_notification_delivery_attempt
                    WHERE delivery_id = ?
                    ORDER BY attempt_no DESC
                    """);
            statement.setLong(1, deliveryId);
            statement.setMaxRows(resolveLimit(limit));
            return statement;
        }, (rs, rowNum) -> new AdmNotificationDeliveryAttemptResponse(
                rs.getLong("delivery_id"),
                rs.getInt("attempt_no"),
                rs.getString("operation_id"),
                rs.getString("worker_id"),
                rs.getString("attempt_status"),
                rs.getString("provider_status"),
                redactSensitiveText(rs.getString("provider_message")),
                toLocalDateTime(rs.getTimestamp("started_at")),
                toLocalDateTime(rs.getTimestamp("completed_at")),
                rs.getLong("lease_version"),
                rs.getString("created_by"),
                toLocalDateTime(rs.getTimestamp("created_at"))));
    }

    @Transactional
    public AdmNotificationDeliveryStatusResponse retryDelivery(
            long deliveryId,
            long expectedVersion,
            String reason,
            String operatorId,
            String clientIp) {
        String auditReason = auditLogService.requireReason(reason);
        String requestUser = required(operatorId, "operatorId");
        AdmNotificationDeliveryStatusResponse before = notificationOutboxService.findStatus(deliveryId);
        AdmNotificationDeliveryStatusResponse result = notificationOutboxService.retry(deliveryId, expectedVersion, requestUser);
        auditLogService.record(
                CpfTransactionContext.transactionId(),
                requestUser,
                "NOTIFICATION_DELIVERY_RETRY",
                "cpf_notification_delivery_log",
                String.valueOf(deliveryId),
                auditReason,
                String.valueOf(before),
                String.valueOf(result),
                null,
                clientIp);
        return result;
    }

    @Transactional
    public AdmNotificationDeliveryStatusResponse cancelDelivery(
            long deliveryId,
            long expectedVersion,
            String reason,
            String operatorId,
            String clientIp) {
        String auditReason = auditLogService.requireReason(reason);
        String requestUser = required(operatorId, "operatorId");
        AdmNotificationDeliveryStatusResponse before = notificationOutboxService.findStatus(deliveryId);
        AdmNotificationDeliveryStatusResponse result = notificationOutboxService.cancel(deliveryId, expectedVersion, requestUser);
        auditLogService.record(
                CpfTransactionContext.transactionId(),
                requestUser,
                "NOTIFICATION_DELIVERY_CANCEL",
                "cpf_notification_delivery_log",
                String.valueOf(deliveryId),
                auditReason,
                String.valueOf(before),
                String.valueOf(result),
                null,
                clientIp);
        return result;
    }

    private long insertRule(
            AdmNotificationRuleRequest request,
            String eventType,
            String eventSubType,
            String channelCode,
            String requestUser) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        int updated = cpfJdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement("""
                    INSERT INTO cpf_notification_rule (
                        event_type, event_sub_type, channel_code, template_code, severity,
                        receiver_group, use_yn, created_by, updated_by
                    )
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """, new String[] {"rule_id"});
            statement.setString(1, eventType);
            statement.setString(2, eventSubType);
            statement.setString(3, channelCode);
            statement.setString(4, blankToNull(request.templateCode()));
            statement.setString(5, defaultText(request.severity(), "INFO"));
            statement.setString(6, blankToNull(request.receiverGroup()));
            statement.setString(7, yn(request.useYn(), "Y"));
            statement.setString(8, requestUser);
            statement.setString(9, requestUser);
            return statement;
        }, keyHolder);
        if (updated != 1) {
            throw new IllegalStateException("알림 규칙 생성 결과가 1건이 아닙니다. updated=" + updated);
        }
        return generatedKeyOrLookup(keyHolder, eventType, eventSubType, channelCode);
    }

    private void updateRule(
            long ruleId,
            AdmNotificationRuleRequest request,
            String eventType,
            String eventSubType,
            String channelCode,
            String requestUser) {
        int updated = cpfJdbcTemplate.update("""
                UPDATE cpf_notification_rule
                SET event_type = ?,
                    event_sub_type = ?,
                    channel_code = ?,
                    template_code = ?,
                    severity = ?,
                    receiver_group = ?,
                    use_yn = ?,
                    updated_by = ?,
                    updated_at = CURRENT_TIMESTAMP
                WHERE rule_id = ?
                """,
                eventType,
                eventSubType,
                channelCode,
                blankToNull(request.templateCode()),
                defaultText(request.severity(), "INFO"),
                blankToNull(request.receiverGroup()),
                yn(request.useYn(), "Y"),
                requestUser,
                ruleId);
        if (updated != 1) {
            throw new IllegalStateException("알림 규칙 수정 결과가 1건이 아닙니다. updated=" + updated);
        }
    }

    private long generatedKeyOrLookup(
            KeyHolder keyHolder,
            String eventType,
            String eventSubType,
            String channelCode) {
        Number generated = generatedNumber(keyHolder, "rule_id");
        if (generated != null) {
            return generated.longValue();
        }
        Map<String, Object> inserted = findRuleMapByBusinessKey(eventType, eventSubType, channelCode);
        if (inserted.isEmpty()) {
            throw new IllegalStateException("알림 규칙 생성 ID를 확인할 수 없습니다.");
        }
        return longValue(inserted.get("rule_id"));
    }

    private Number generatedNumber(KeyHolder keyHolder, String columnName) {
        try {
            Number key = keyHolder.getKey();
            if (key != null) {
                return key;
            }
        } catch (org.springframework.dao.InvalidDataAccessApiUsageException ignored) {
            // Driver가 여러 column을 반환하면 이름 기반으로 확인합니다.
        }
        Map<String, Object> keys = keyHolder.getKeys();
        if (keys == null || keys.isEmpty()) {
            return null;
        }
        Object value = keys.get(columnName);
        if (value == null) {
            value = keys.get(columnName.toUpperCase(java.util.Locale.ROOT));
        }
        if (value instanceof Number number) {
            return number;
        }
        return value == null ? null : Long.valueOf(String.valueOf(value));
    }

    private Map<String, Object> findRuleMapByBusinessKey(
            String eventType,
            String eventSubType,
            String channelCode) {
        try {
            return cpfJdbcTemplate.queryForMap(
                    RULE_SELECT + """
                     WHERE event_type = ?
                       AND channel_code = ?
                       AND ((? IS NULL AND event_sub_type IS NULL) OR event_sub_type = ?)
                    """,
                    eventType,
                    channelCode,
                    eventSubType,
                    eventSubType);
        } catch (EmptyResultDataAccessException ex) {
            return Map.of();
        }
    }

    private Map<String, Object> findRuleMapById(long ruleId) {
        try {
            return cpfJdbcTemplate.queryForMap(RULE_SELECT + " WHERE rule_id = ?", ruleId);
        } catch (EmptyResultDataAccessException ex) {
            return Map.of();
        }
    }

    private <T> List<T> queryWithMaxRows(String sql, int maxRows, RowMapper<T> rowMapper) {
        return cpfJdbcTemplate.query(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setMaxRows(maxRows);
            return statement;
        }, rowMapper);
    }

    private AdmNotificationRuleResponse toRule(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new AdmNotificationRuleResponse(
                rs.getLong("rule_id"),
                rs.getString("event_type"),
                rs.getString("event_sub_type"),
                rs.getString("channel_code"),
                rs.getString("template_code"),
                rs.getString("severity"),
                rs.getString("receiver_group"),
                rs.getString("use_yn"),
                rs.getString("created_by"),
                toLocalDateTime(rs.getTimestamp("created_at")),
                rs.getString("updated_by"),
                toLocalDateTime(rs.getTimestamp("updated_at")));
    }

    private int resolveLimit(int limit) {
        return Math.max(1, Math.min(limit, 500));
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

    private String blankToNull(String value) {
        return CpfStrings.hasText(value) ? value.trim() : null;
    }

    private String yn(String value, String fallback) {
        String normalized = defaultText(value, fallback).toUpperCase();
        return "N".equals(normalized) ? "N" : "Y";
    }

    private long longValue(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }

    private Long objectLong(Object value) {
        return value == null ? null : longValue(value);
    }

    private String redactSensitiveText(String value) {
        if (!CpfStrings.hasText(value)) {
            return value;
        }
        return value
                .replaceAll("(?i)authorization\\s*[:=]\\s*bearer\\s+[A-Za-z0-9._~+/=-]+", "Authorization: [REDACTED]")
                .replaceAll("(?i)bearer\\s+[A-Za-z0-9._~+/=-]+", "Bearer [REDACTED]")
                .replaceAll("(?i)(password|passwd|secret|access[_-]?token|refresh[_-]?token)\\s*[:=]\\s*\\S+", "$1=[REDACTED]");
    }

    private String maskReceiver(String value) {
        if (!CpfStrings.hasText(value)) {
            return "***";
        }
        String trimmed = value.trim();
        if (trimmed.length() <= 3) {
            return "***";
        }
        return trimmed.substring(0, 2) + "***" + trimmed.substring(trimmed.length() - 1);
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
