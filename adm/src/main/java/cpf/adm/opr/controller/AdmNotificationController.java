package cpf.adm.opr.controller;

import cpf.adm.opr.service.AdmAuditLogService;
import cpf.pfw.common.exception.CpfValidationException;
import cpf.pfw.common.logging.CpfTransaction;
import cpf.pfw.common.logging.TransactionContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ADM 운영 알림 규칙과 발송 이력을 조회하는 API입니다.
 */
@RestController
@RequestMapping("/adm/api/notifications")
@Tag(name = "ADM-Notification", description = "운영 알림 규칙과 발송 이력 조회 API")
public class AdmNotificationController {
    private final JdbcTemplate pfwJdbcTemplate;
    private final AdmAuditLogService auditLogService;

    public AdmNotificationController(
            @Qualifier("pfwJdbcTemplate") JdbcTemplate pfwJdbcTemplate,
            AdmAuditLogService auditLogService) {
        this.pfwJdbcTemplate = pfwJdbcTemplate;
        this.auditLogService = auditLogService;
    }

    /**
     * 배치 실패, 보안 이벤트 같은 운영 알림 규칙을 조회합니다.
     */
    @GetMapping("/rules")
    @CpfTransaction(id = "ADM01NTF0010", name = "ADMNotificationRuleList")
    @Operation(summary = "운영 알림 규칙 조회", description = "PFW 운영 알림 규칙을 최근 등록 순서로 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findRules(
            @RequestParam(defaultValue = "100") int limit) {
        int resolvedLimit = Math.max(1, Math.min(limit, 500));
        return ResponseEntity.ok(pfwJdbcTemplate.queryForList("""
                SELECT rule_id, event_type, event_sub_type, channel_code, template_code,
                       severity, receiver_group, use_yn, created_at, updated_at
                FROM pfw_notification_rule
                ORDER BY use_yn DESC, severity DESC, rule_id DESC
                LIMIT ?
                """, resolvedLimit));
    }

    /**
     * 운영 알림 규칙을 등록하거나 같은 이벤트 기준의 기존 규칙을 갱신합니다.
     */
    @PostMapping("/rules")
    @CpfTransaction(id = "ADM02NTF0012", name = "ADMNotificationRuleSave")
    @Operation(summary = "운영 알림 규칙 등록/수정", description = "이벤트 유형, 세부 유형, 채널 기준으로 운영 알림 규칙을 등록하거나 갱신합니다.")
    public ResponseEntity<Map<String, Object>> saveRule(
            @RequestBody NotificationRuleSaveRequest request,
            HttpServletRequest servletRequest) {
        String reason = auditLogService.requireReason(request.reason());
        String eventType = required(request.eventType(), "eventType");
        String eventSubType = blankToNull(request.eventSubType());
        String channelCode = defaultText(request.channelCode(), "ADM");
        String requestUser = requestUser(servletRequest, request.requestUser());

        Map<String, Object> before = findRuleByBusinessKey(eventType, eventSubType, channelCode);
        pfwJdbcTemplate.update("""
                INSERT INTO pfw_notification_rule (
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
                defaultText(request.useYn(), "Y"),
                requestUser,
                requestUser);

        Map<String, Object> after = findRuleByBusinessKey(eventType, eventSubType, channelCode);
        auditLogService.record(
                TransactionContext.getOrCreateTransactionId(),
                requestUser,
                before.isEmpty() ? "NOTIFICATION_RULE_CREATE" : "NOTIFICATION_RULE_UPDATE",
                "pfw_notification_rule",
                String.valueOf(after.get("rule_id")),
                reason,
                before.isEmpty() ? null : String.valueOf(before),
                String.valueOf(after),
                null,
                servletRequest.getRemoteAddr());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("action", before.isEmpty() ? "CREATED" : "UPDATED");
        response.put("rule", after);
        return ResponseEntity.ok(response);
    }

    /**
     * 운영 알림 규칙을 비활성화합니다.
     */
    @PutMapping("/rules/{ruleId}/disable")
    @CpfTransaction(id = "ADM03NTF0013", name = "ADMNotificationRuleDisable")
    @Operation(summary = "운영 알림 규칙 비활성", description = "운영 알림 규칙의 사용 여부를 N으로 변경하고 감사 로그를 남깁니다.")
    public ResponseEntity<Map<String, Object>> disableRule(
            @PathVariable long ruleId,
            @RequestParam String reason,
            @RequestParam(defaultValue = "ADM") String requestUser,
            HttpServletRequest servletRequest) {
        String auditReason = auditLogService.requireReason(reason);
        String operatorId = requestUser(servletRequest, requestUser);
        Map<String, Object> before = findRuleById(ruleId);
        pfwJdbcTemplate.update("""
                UPDATE pfw_notification_rule
                SET use_yn = 'N',
                    updated_by = ?,
                    updated_at = CURRENT_TIMESTAMP
                WHERE rule_id = ?
                """, operatorId, ruleId);
        Map<String, Object> after = findRuleById(ruleId);
        auditLogService.record(
                TransactionContext.getOrCreateTransactionId(),
                operatorId,
                "NOTIFICATION_RULE_DISABLE",
                "pfw_notification_rule",
                String.valueOf(ruleId),
                auditReason,
                String.valueOf(before),
                String.valueOf(after),
                null,
                servletRequest.getRemoteAddr());
        return ResponseEntity.ok(after);
    }

    /**
     * 운영 알림 발송 요청과 결과 이력을 조회합니다.
     */
    @GetMapping("/delivery-logs")
    @CpfTransaction(id = "ADM01NTF0011", name = "ADMNotificationDeliveryLogList")
    @Operation(summary = "운영 알림 발송 이력 조회", description = "PFW 운영 알림 발송 로그를 최근 요청 순서로 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findDeliveryLogs(
            @RequestParam(defaultValue = "100") int limit) {
        int resolvedLimit = Math.max(1, Math.min(limit, 500));
        return ResponseEntity.ok(pfwJdbcTemplate.queryForList("""
                SELECT delivery_id, rule_id, event_type, target_type, target_id,
                       receiver, delivery_status, delivery_message, requested_at, delivered_at,
                       created_at, updated_at
                FROM pfw_notification_delivery_log
                ORDER BY requested_at DESC, delivery_id DESC
                LIMIT ?
                """, resolvedLimit));
    }

    private Map<String, Object> findRuleByBusinessKey(String eventType, String eventSubType, String channelCode) {
        try {
            return pfwJdbcTemplate.queryForMap("""
                    SELECT rule_id, event_type, event_sub_type, channel_code, template_code,
                           severity, receiver_group, use_yn, created_by, created_at, updated_by, updated_at
                    FROM pfw_notification_rule
                    WHERE event_type = ?
                      AND channel_code = ?
                      AND ((? IS NULL AND event_sub_type IS NULL) OR event_sub_type = ?)
                    """, eventType, channelCode, eventSubType, eventSubType);
        } catch (EmptyResultDataAccessException ex) {
            return Map.of();
        }
    }

    private Map<String, Object> findRuleById(long ruleId) {
        return pfwJdbcTemplate.queryForMap("""
                SELECT rule_id, event_type, event_sub_type, channel_code, template_code,
                       severity, receiver_group, use_yn, created_by, created_at, updated_by, updated_at
                FROM pfw_notification_rule
                WHERE rule_id = ?
                """, ruleId);
    }

    private String required(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new CpfValidationException(name + " 값은 필수입니다.");
        }
        return value.trim();
    }

    private String defaultText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String requestUser(HttpServletRequest request, String fallback) {
        Object operatorId = request.getAttribute("adm.operatorId");
        if (operatorId instanceof String value && !value.isBlank()) {
            return value;
        }
        return defaultText(fallback, "ADM");
    }

    public record NotificationRuleSaveRequest(
            String eventType,
            String eventSubType,
            String channelCode,
            String templateCode,
            String severity,
            String receiverGroup,
            String useYn,
            String reason,
            String requestUser) {
    }
}
