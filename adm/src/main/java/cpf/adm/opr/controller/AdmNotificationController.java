package cpf.adm.opr.controller;

import cpf.pfw.common.logging.CpfTransaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    public AdmNotificationController(@Qualifier("pfwJdbcTemplate") JdbcTemplate pfwJdbcTemplate) {
        this.pfwJdbcTemplate = pfwJdbcTemplate;
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
}
