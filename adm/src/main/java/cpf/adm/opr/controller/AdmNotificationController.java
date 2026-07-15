package cpf.adm.opr.controller;

import cpf.adm.opr.dto.AdmNotificationDeliveryLogResponse;
import cpf.adm.opr.dto.AdmNotificationRuleRequest;
import cpf.adm.opr.dto.AdmNotificationRuleResponse;
import cpf.adm.opr.dto.AdmNotificationTestSendRequest;
import cpf.adm.opr.service.AdmNotificationService;
import cpf.pfw.common.execution.CpfOnlineTransaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    private final AdmNotificationService notificationService;

    public AdmNotificationController(AdmNotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * 배치 실패, 보안 이벤트 같은 운영 알림 규칙을 조회합니다.
     */
    @GetMapping("/rules")
    @CpfOnlineTransaction(id = "OADM-NTF-01-0010", name = "ADMNotificationRuleList")
    @Operation(operationId = "admNotificationFindRules", summary = "운영 알림 규칙 조회", description = "PFW 운영 알림 규칙을 최근 등록 순서로 조회합니다.")
    public ResponseEntity<List<AdmNotificationRuleResponse>> findRules(
            @RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(notificationService.findRules(limit));
    }

    /**
     * 운영 알림 규칙 상세를 조회합니다.
     */
    @GetMapping("/rules/{ruleId}")
    @CpfOnlineTransaction(id = "OADM-NTF-01-0014", name = "ADMNotificationRuleDetail")
    @Operation(operationId = "admNotificationFindRule", summary = "운영 알림 규칙 상세 조회", description = "운영 알림 규칙 단건을 조회합니다.")
    public ResponseEntity<AdmNotificationRuleResponse> findRule(@PathVariable long ruleId) {
        return ResponseEntity.ok(notificationService.findRule(ruleId));
    }

    /**
     * 운영 알림 규칙을 등록하거나 같은 이벤트 기준의 기존 규칙을 갱신합니다.
     */
    @PostMapping("/rules")
    @CpfOnlineTransaction(id = "OADM-NTF-02-0012", name = "ADMNotificationRuleSave")
    @Operation(operationId = "admNotificationSaveRule", summary = "운영 알림 규칙 등록/수정", description = "이벤트 유형, 세부 유형, 채널 기준으로 운영 알림 규칙을 등록하거나 갱신합니다.")
    public ResponseEntity<AdmNotificationRuleResponse> saveRule(
            @RequestBody AdmNotificationRuleRequest request,
            HttpServletRequest servletRequest) {
        return ResponseEntity.ok(notificationService.saveRule(
                null, request, requestUser(servletRequest, request.requestUser()), servletRequest.getRemoteAddr()));
    }

    /**
     * 운영 알림 규칙을 ruleId 기준으로 수정합니다.
     */
    @PutMapping("/rules/{ruleId}")
    @CpfOnlineTransaction(id = "OADM-NTF-03-0015", name = "ADMNotificationRuleUpdate")
    @Operation(operationId = "admNotificationUpdateRule", summary = "운영 알림 규칙 수정", description = "운영 알림 규칙 단건을 수정하고 감사 로그를 남깁니다.")
    public ResponseEntity<AdmNotificationRuleResponse> updateRule(
            @PathVariable long ruleId,
            @RequestBody AdmNotificationRuleRequest request,
            HttpServletRequest servletRequest) {
        return ResponseEntity.ok(notificationService.saveRule(
                ruleId, request, requestUser(servletRequest, request.requestUser()), servletRequest.getRemoteAddr()));
    }

    /**
     * 운영 알림 규칙을 비활성화합니다.
     */
    @PutMapping("/rules/{ruleId}/disable")
    @CpfOnlineTransaction(id = "OADM-NTF-03-0013", name = "ADMNotificationRuleDisable")
    @Operation(operationId = "admNotificationDisableRule", summary = "운영 알림 규칙 비활성", description = "운영 알림 규칙의 사용 여부를 N으로 변경하고 감사 로그를 남깁니다.")
    public ResponseEntity<AdmNotificationRuleResponse> disableRule(
            @PathVariable long ruleId,
            @RequestParam String reason,
            @RequestParam(defaultValue = "ADM") String requestUser,
            HttpServletRequest servletRequest) {
        return ResponseEntity.ok(notificationService.disableRule(
                ruleId, reason, requestUser(servletRequest, requestUser), servletRequest.getRemoteAddr()));
    }

    /**
     * 운영 알림 발송 요청과 결과 이력을 조회합니다.
     */
    @GetMapping("/delivery-logs")
    @CpfOnlineTransaction(id = "OADM-NTF-01-0011", name = "ADMNotificationDeliveryLogList")
    @Operation(operationId = "admNotificationFindDeliveryLogs", summary = "운영 알림 발송 이력 조회", description = "PFW 운영 알림 발송 로그를 최근 요청 순서로 조회합니다.")
    public ResponseEntity<List<AdmNotificationDeliveryLogResponse>> findDeliveryLogs(
            @RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(notificationService.findDeliveryLogs(limit));
    }

    /**
     * mock sender를 사용해 운영 알림 테스트 발송을 수행합니다.
     */
    @PostMapping("/rules/{ruleId}/test-send")
    @CpfOnlineTransaction(id = "OADM-NTF-02-0016", name = "ADMNotificationTestSend")
    @Operation(operationId = "admNotificationSendTest", summary = "운영 알림 테스트 발송", description = "mock sender로 알림 발송을 시뮬레이션하고 발송 이력과 감사 로그를 남깁니다.")
    public ResponseEntity<Map<String, Object>> sendTest(
            @PathVariable long ruleId,
            @RequestBody AdmNotificationTestSendRequest request,
            HttpServletRequest servletRequest) {
        return ResponseEntity.ok(notificationService.sendTest(
                ruleId, request, requestUser(servletRequest, request.requestUser()), servletRequest.getRemoteAddr()));
    }

    private String requestUser(HttpServletRequest request, String fallback) {
        Object operatorId = request.getAttribute("adm.operatorId");
        if (operatorId instanceof String value && !value.isBlank()) {
            return value;
        }
        return fallback == null || fallback.isBlank() ? "ADM" : fallback;
    }
}
