package com.cpf.admin.opr.service;

import com.cpf.admin.opr.dto.AdmNotificationDeliveryLogResponse;
import com.cpf.admin.opr.dto.AdmNotificationRuleRequest;
import com.cpf.admin.opr.dto.AdmNotificationRuleResponse;
import com.cpf.admin.opr.dto.AdmNotificationTestSendRequest;
import com.cpf.admin.opr.dto.NotificationSendResult;
import com.cpf.common.utils.TextUtils;
import com.cpf.core.common.exception.CpfValidationException;
import com.cpf.core.common.logging.TransactionContext;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ADM 운영 알림 규칙과 발송 이력을 관리합니다.
 *
 * <p>Controller는 HTTP 요청/응답만 담당하고, 알림 규칙 저장, 감사 로그, mock 발송,
 * 발송 이력 적재는 이 서비스에서 일관되게 처리합니다.</p>
 */
@Service
public class AdmNotificationService extends com.cpf.admin.common.base.AdmBaseService {
    private final JdbcTemplate cpfJdbcTemplate;
    private final AdmAuditLogService auditLogService;
    private final NotificationSender notificationSender;

    public AdmNotificationService(
            @Qualifier("cpfJdbcTemplate") JdbcTemplate cpfJdbcTemplate,
            AdmAuditLogService auditLogService,
            NotificationSender notificationSender) {
        this.cpfJdbcTemplate = cpfJdbcTemplate;
        this.auditLogService = auditLogService;
        this.notificationSender = notificationSender;
    }

    public List<AdmNotificationRuleResponse> findRules(int limit) {
        int resolvedLimit = Math.max(1, Math.min(limit, 500));
        return cpfJdbcTemplate.query("""
                SELECT rule_id, event_type, event_sub_type, channel_code, template_code,
                       severity, receiver_group, use_yn, created_by, created_at, updated_by, updated_at
                FROM cpf_notification_rule
                ORDER BY use_yn DESC, severity DESC, rule_id DESC
                LIMIT ?
                """, (rs, rowNum) -> toRule(rs), resolvedLimit);
    }

    public AdmNotificationRuleResponse findRule(long ruleId) {
        return cpfJdbcTemplate.queryForObject("""
                SELECT rule_id, event_type, event_sub_type, channel_code, template_code,
                       severity, receiver_group, use_yn, created_by, created_at, updated_by, updated_at
                FROM cpf_notification_rule
                WHERE rule_id = ?
                """, (rs, rowNum) -> toRule(rs), ruleId);
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
        String requestUser = defaultText(operatorId, defaultText(request.requestUser(), "ADM"));
        Map<String, Object> before = ruleId == null
                ? findRuleMapByBusinessKey(eventType, eventSubType, channelCode)
                : findRuleMapById(ruleId);

        if (ruleId == null) {
            cpfJdbcTemplate.update("""
                    INSERT INTO cpf_notification_rule (
                        event_type, event_sub_type, channel_code, template_code, severity,
                        receiver_group, use_yn, created_by, updated_by
                    )
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                    ON DUPLICATE KEY UPDATE
                        template_code = VALUES(template_code),
                        severity = VALUES(severity),
                        receiver_group = VALUES(receiver_group),
                        use_yn = VALUES(use_yn),
                        updated_by = VALUES(updated_by),
                        updated_at = CURRENT_TIMESTAMP
                    """,
                    eventType,
                    eventSubType,
                    channelCode,
                    blankToNull(request.templateCode()),
                    defaultText(request.severity(), "INFO"),
                    blankToNull(request.receiverGroup()),
                    yn(request.useYn(), "Y"),
                    requestUser,
                    requestUser);
        } else {
            cpfJdbcTemplate.update("""
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
        }

        Map<String, Object> after = ruleId == null
                ? findRuleMapByBusinessKey(eventType, eventSubType, channelCode)
                : findRuleMapById(ruleId);
        auditLogService.record(
                TransactionContext.getOrCreateTransactionId(),
                requestUser,
                before.isEmpty() ? "NOTIFICATION_RULE_CREATE" : "NOTIFICATION_RULE_UPDATE",
                "cpf_notification_rule",
                String.valueOf(after.get("rule_id")),
                reason,
                before.isEmpty() ? null : String.valueOf(before),
                String.valueOf(after),
                null,
                clientIp);
        return findRule(longValue(after.get("rule_id")));
    }

    @Transactional
    public AdmNotificationRuleResponse disableRule(long ruleId, String reason, String operatorId, String clientIp) {
        String auditReason = auditLogService.requireReason(reason);
        String requestUser = defaultText(operatorId, "ADM");
        Map<String, Object> before = findRuleMapById(ruleId);
        cpfJdbcTemplate.update("""
                UPDATE cpf_notification_rule
                SET use_yn = 'N',
                    updated_by = ?,
                    updated_at = CURRENT_TIMESTAMP
                WHERE rule_id = ?
                """, requestUser, ruleId);
        Map<String, Object> after = findRuleMapById(ruleId);
        auditLogService.record(
                TransactionContext.getOrCreateTransactionId(),
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
        int resolvedLimit = Math.max(1, Math.min(limit, 500));
        return cpfJdbcTemplate.query("""
                SELECT delivery_id, rule_id, event_type, target_type, target_id,
                       receiver, delivery_status, delivery_message, requested_at, delivered_at,
                       created_at, updated_at
                FROM cpf_notification_delivery_log
                ORDER BY requested_at DESC, delivery_id DESC
                LIMIT ?
                """, (rs, rowNum) -> new AdmNotificationDeliveryLogResponse(
                rs.getLong("delivery_id"),
                objectLong(rs.getObject("rule_id")),
                rs.getString("event_type"),
                rs.getString("target_type"),
                rs.getString("target_id"),
                rs.getString("receiver"),
                rs.getString("delivery_status"),
                rs.getString("delivery_message"),
                toLocalDateTime(rs.getTimestamp("requested_at")),
                toLocalDateTime(rs.getTimestamp("delivered_at")),
                toLocalDateTime(rs.getTimestamp("created_at")),
                toLocalDateTime(rs.getTimestamp("updated_at"))), resolvedLimit);
    }

    @Transactional
    public Map<String, Object> sendTest(long ruleId, AdmNotificationTestSendRequest request, String operatorId, String clientIp) {
        String reason = auditLogService.requireReason(request.reason());
        String requestUser = defaultText(operatorId, defaultText(request.requestUser(), "ADM"));
        AdmNotificationRuleResponse rule = findRule(ruleId);
        NotificationSendResult sendResult = notificationSender.send(
                rule,
                defaultText(request.targetType(), "ADM_TEST"),
                defaultText(request.targetId(), "TEST"),
                defaultText(request.receiver(), defaultText(rule.receiverGroup(), "ADM_OPERATOR")),
                defaultText(request.message(), "ADM 운영 알림 테스트 발송입니다."),
                requestUser);
        long deliveryId = insertDeliveryLog(rule, request, sendResult, requestUser);
        auditLogService.record(
                TransactionContext.getOrCreateTransactionId(),
                requestUser,
                "NOTIFICATION_TEST_SEND",
                "cpf_notification_delivery_log",
                String.valueOf(deliveryId),
                reason,
                null,
                String.valueOf(sendResult),
                null,
                clientIp);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("deliveryId", deliveryId);
        response.put("rule", rule);
        response.put("sendResult", sendResult);
        return response;
    }

    private long insertDeliveryLog(
            AdmNotificationRuleResponse rule,
            AdmNotificationTestSendRequest request,
            NotificationSendResult sendResult,
            String requestUser) {
        cpfJdbcTemplate.update("""
                INSERT INTO cpf_notification_delivery_log (
                    rule_id, event_type, target_type, target_id, receiver,
                    delivery_status, delivery_message, requested_at, delivered_at,
                    created_by, updated_by
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP(3), ?, ?, ?)
                """,
                rule.ruleId(),
                rule.eventType(),
                defaultText(request.targetType(), "ADM_TEST"),
                defaultText(request.targetId(), "TEST"),
                defaultText(request.receiver(), defaultText(rule.receiverGroup(), "ADM_OPERATOR")),
                sendResult.deliveryStatus(),
                sendResult.deliveryMessage(),
                sendResult.deliveredAt(),
                requestUser,
                requestUser);
        Long deliveryId = cpfJdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        if (deliveryId == null) {
            throw new IllegalStateException("알림 발송 이력 ID를 확인할 수 없습니다.");
        }
        return deliveryId;
    }

    private Map<String, Object> findRuleMapByBusinessKey(String eventType, String eventSubType, String channelCode) {
        try {
            return cpfJdbcTemplate.queryForMap("""
                    SELECT rule_id, event_type, event_sub_type, channel_code, template_code,
                           severity, receiver_group, use_yn, created_by, created_at, updated_by, updated_at
                    FROM cpf_notification_rule
                    WHERE event_type = ?
                      AND channel_code = ?
                      AND ((? IS NULL AND event_sub_type IS NULL) OR event_sub_type = ?)
                    """, eventType, channelCode, eventSubType, eventSubType);
        } catch (EmptyResultDataAccessException ex) {
            return Map.of();
        }
    }

    private Map<String, Object> findRuleMapById(long ruleId) {
        try {
            return cpfJdbcTemplate.queryForMap("""
                    SELECT rule_id, event_type, event_sub_type, channel_code, template_code,
                           severity, receiver_group, use_yn, created_by, created_at, updated_by, updated_at
                    FROM cpf_notification_rule
                    WHERE rule_id = ?
                    """, ruleId);
        } catch (EmptyResultDataAccessException ex) {
            return Map.of();
        }
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

    private String required(String value, String name) {
        if (!TextUtils.hasText(value)) {
            throw new CpfValidationException(name + " 값은 필수입니다.");
        }
        return value.trim();
    }

    private String defaultText(String value, String fallback) {
        return TextUtils.hasText(value) ? value.trim() : fallback;
    }

    private String blankToNull(String value) {
        return TextUtils.hasText(value) ? value.trim() : null;
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
        if (value == null) {
            return null;
        }
        return longValue(value);
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
