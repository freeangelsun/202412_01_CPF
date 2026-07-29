package com.cpf.admin.opr.controller;

import com.cpf.admin.opr.dto.AdmNotificationDeliveryAttemptResponse;
import com.cpf.admin.opr.dto.AdmNotificationDeliveryLogResponse;
import com.cpf.admin.opr.dto.AdmNotificationRuleRequest;
import com.cpf.admin.opr.dto.AdmNotificationRuleResponse;
import com.cpf.admin.opr.dto.AdmNotificationDeliveryStatusResponse;
import com.cpf.admin.opr.dto.AdmNotificationTestSendResponse;
import com.cpf.admin.opr.dto.AdmNotificationTestSendRequest;
import com.cpf.admin.opr.service.AdmNotificationService;
import com.cpf.core.api.execution.CpfOnlineTransaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

/**
 * ADM 운영 알림 규칙과 발송 이력을 조회·제어하는 API입니다.
 *
 * <p>모든 운영 변경과 테스트 발송은 검증된 ADM Session의 operator만 수행할 수 있으며,
 * 요청 Body나 Query parameter의 사용자 값으로 인증 주체를 대체하지 않습니다.</p>
 */
@RestController
@RequestMapping("/adm/api/notifications")
@Tag(name = "ADM-Notification", description = "운영 알림 규칙과 발송 이력 조회·제어 API")
public class AdmNotificationController extends com.cpf.admin.common.base.AdmBaseController {
    private final AdmNotificationService notificationService;

    public AdmNotificationController(AdmNotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/rules")
    @CpfOnlineTransaction(id = "OADMNT0010", name = "ADMNotificationRuleList")
    @Operation(operationId = "admNotificationFindRules", summary = "운영 알림 규칙 조회")
    public ResponseEntity<List<AdmNotificationRuleResponse>> findRules(
            @RequestParam(defaultValue = "100") int limit,
            HttpServletRequest servletRequest) {
        operator(servletRequest, null);
        return ResponseEntity.ok(notificationService.findRules(limit));
    }

    @GetMapping("/rules/{ruleId}")
    @CpfOnlineTransaction(id = "OADMNT0014", name = "ADMNotificationRuleDetail")
    @Operation(operationId = "admNotificationFindRule", summary = "운영 알림 규칙 상세 조회")
    public ResponseEntity<AdmNotificationRuleResponse> findRule(
            @PathVariable long ruleId,
            HttpServletRequest servletRequest) {
        operator(servletRequest, null);
        return ResponseEntity.ok(notificationService.findRule(ruleId));
    }

    @PostMapping("/rules")
    @CpfOnlineTransaction(id = "OADMNT0012", name = "ADMNotificationRuleSave")
    @Operation(operationId = "admNotificationSaveRule", summary = "운영 알림 규칙 등록")
    public ResponseEntity<AdmNotificationRuleResponse> saveRule(
            @RequestBody AdmNotificationRuleRequest request,
            HttpServletRequest servletRequest) {
        String operatorId = operator(servletRequest, request.requestUser());
        return ResponseEntity.ok(notificationService.saveRule(
                null, request, operatorId, servletRequest.getRemoteAddr()));
    }

    @PutMapping("/rules/{ruleId}")
    @CpfOnlineTransaction(id = "OADMNT0015", name = "ADMNotificationRuleUpdate")
    @Operation(operationId = "admNotificationUpdateRule", summary = "운영 알림 규칙 수정")
    public ResponseEntity<AdmNotificationRuleResponse> updateRule(
            @PathVariable long ruleId,
            @RequestBody AdmNotificationRuleRequest request,
            HttpServletRequest servletRequest) {
        String operatorId = operator(servletRequest, request.requestUser());
        return ResponseEntity.ok(notificationService.saveRule(
                ruleId, request, operatorId, servletRequest.getRemoteAddr()));
    }

    @PutMapping("/rules/{ruleId}/disable")
    @CpfOnlineTransaction(id = "OADMNT0013", name = "ADMNotificationRuleDisable")
    @Operation(operationId = "admNotificationDisableRule", summary = "운영 알림 규칙 비활성")
    public ResponseEntity<AdmNotificationRuleResponse> disableRule(
            @PathVariable long ruleId,
            @RequestParam String reason,
            @RequestParam(required = false) String requestUser,
            HttpServletRequest servletRequest) {
        String operatorId = operator(servletRequest, requestUser);
        return ResponseEntity.ok(notificationService.disableRule(
                ruleId, reason, operatorId, servletRequest.getRemoteAddr()));
    }

    @GetMapping("/delivery-logs")
    @CpfOnlineTransaction(id = "OADMNT0011", name = "ADMNotificationDeliveryLogList")
    @Operation(operationId = "admNotificationFindDeliveryLogs", summary = "운영 알림 발송 이력 조회")
    public ResponseEntity<List<AdmNotificationDeliveryLogResponse>> findDeliveryLogs(
            @RequestParam(defaultValue = "100") int limit,
            HttpServletRequest servletRequest) {
        operator(servletRequest, null);
        return ResponseEntity.ok(notificationService.findDeliveryLogs(limit));
    }

    @GetMapping("/delivery-logs/{deliveryId}/attempts")
    @CpfOnlineTransaction(id = "OADMNT0019", name = "ADMNotificationDeliveryAttemptList")
    @Operation(
            operationId = "admNotificationFindDeliveryAttempts",
            summary = "운영 알림 Provider Attempt 이력 조회",
            description = "재시도 전후 모든 Provider 호출 Attempt와 결과 불명 원인을 immutable 이력으로 조회합니다.")
    public ResponseEntity<List<AdmNotificationDeliveryAttemptResponse>> findDeliveryAttempts(
            @PathVariable long deliveryId,
            @RequestParam(defaultValue = "100") int limit,
            HttpServletRequest servletRequest) {
        operator(servletRequest, null);
        return ResponseEntity.ok(notificationService.findDeliveryAttempts(deliveryId, limit));
    }

    @PostMapping("/rules/{ruleId}/test-send")
    @CpfOnlineTransaction(id = "OADMNT0016", name = "ADMNotificationTestSend")
    @Operation(
            operationId = "admNotificationSendTest",
            summary = "운영 알림 테스트 발송",
            description = "Provider 중립 Sender로 테스트 발송하며 Simulator 결과는 실제 외부 수신 완료로 판정하지 않습니다.")
    public ResponseEntity<AdmNotificationTestSendResponse> sendTest(
            @PathVariable long ruleId,
            @RequestBody AdmNotificationTestSendRequest request,
            HttpServletRequest servletRequest) {
        String operatorId = operator(servletRequest, request.requestUser());
        return ResponseEntity.ok(notificationService.sendTest(
                ruleId, request, operatorId, servletRequest.getRemoteAddr()));
    }

    @PostMapping("/delivery-logs/{deliveryId}/retry")
    @CpfOnlineTransaction(id = "OADMNT0017", name = "ADMNotificationDeliveryRetry")
    @Operation(operationId = "admNotificationRetryDelivery", summary = "운영 알림 발송 재시도")
    public ResponseEntity<AdmNotificationDeliveryStatusResponse> retryDelivery(
            @PathVariable long deliveryId,
            @RequestParam long expectedVersion,
            @RequestParam String reason,
            @RequestParam(required = false) String requestUser,
            HttpServletRequest servletRequest) {
        String operatorId = operator(servletRequest, requestUser);
        return ResponseEntity.ok(notificationService.retryDelivery(
                deliveryId, expectedVersion, reason, operatorId, servletRequest.getRemoteAddr()));
    }

    @PostMapping("/delivery-logs/{deliveryId}/cancel")
    @CpfOnlineTransaction(id = "OADMNT0018", name = "ADMNotificationDeliveryCancel")
    @Operation(operationId = "admNotificationCancelDelivery", summary = "운영 알림 발송 취소")
    public ResponseEntity<AdmNotificationDeliveryStatusResponse> cancelDelivery(
            @PathVariable long deliveryId,
            @RequestParam long expectedVersion,
            @RequestParam String reason,
            @RequestParam(required = false) String requestUser,
            HttpServletRequest servletRequest) {
        String operatorId = operator(servletRequest, requestUser);
        return ResponseEntity.ok(notificationService.cancelDelivery(
                deliveryId, expectedVersion, reason, operatorId, servletRequest.getRemoteAddr()));
    }

    private String operator(HttpServletRequest request, String claimedOperator) {
        Object value = request.getAttribute("adm.operatorId");
        if (!(value instanceof String operatorId) || operatorId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "검증된 ADM operator session이 필요합니다.");
        }
        if (claimedOperator != null && !claimedOperator.isBlank() && !operatorId.equals(claimedOperator.trim())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "요청 사용자와 인증된 ADM operator가 일치하지 않습니다.");
        }
        return operatorId;
    }
}
