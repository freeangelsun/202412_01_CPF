package com.cpf.admin.opr.controller;

import com.cpf.admin.opr.dto.AdmMenu;
import com.cpf.admin.opr.dto.AdmOperator;
import com.cpf.admin.opr.dto.AdmOperatorCreateRequest;
import com.cpf.admin.opr.dto.AdmOperatorContactUpdateRequest;
import com.cpf.admin.opr.dto.AdmOperatorStatusUpdateRequest;
import com.cpf.admin.opr.dto.AdmOperatorPasswordResetRequest;
import com.cpf.admin.opr.dto.AdmOperatorRoleUpdateRequest;
import com.cpf.admin.opr.dto.AdmPasswordChangeRequest;
import com.cpf.admin.opr.dto.AdmRole;
import com.cpf.admin.opr.dto.AdmSessionRevokeRequest;
import com.cpf.admin.opr.service.AdmOperatorService;
import com.cpf.admin.opr.service.AdmAuditLogService;
import com.cpf.admin.opr.service.AdmSessionService;
import com.cpf.core.api.logging.CpfTransactionContext;
import com.cpf.core.api.security.CpfSensitiveDataAccessRequest;
import com.cpf.core.api.execution.CpfOnlineTransaction;
import com.cpf.core.api.error.CpfValidationException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.http.CacheControl;
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

@RestController
@RequestMapping("/adm/api/operators")
@Tag(name = "ADM-OPR Operators", description = "Operator, role, and menu management APIs")
public class AdmOperatorController extends com.cpf.admin.common.base.AdmBaseController {
    private final AdmOperatorService operatorService;
    private final AdmSessionService sessionService;
    private final AdmAuditLogService auditLogService;

    public AdmOperatorController(
            AdmOperatorService operatorService,
            AdmSessionService sessionService,
            AdmAuditLogService auditLogService) {
        this.operatorService = operatorService;
        this.sessionService = sessionService;
        this.auditLogService = auditLogService;
    }

    @GetMapping
    @CpfOnlineTransaction(id = "OADMOP0030", name = "ADMOperatorList")
    @Operation(operationId = "admOperatorFindOperators", summary = "List operators", description = "Returns ADM operators and account status.")
    public ResponseEntity<List<AdmOperator>> findOperators() {
        return ResponseEntity.ok(operatorService.findOperators());
    }

    @PostMapping
    @CpfOnlineTransaction(id = "OADMOP0031", name = "ADMOperatorCreate")
    @Operation(operationId = "admOperatorCreateOperator", summary = "Create operator", description = "Creates an ADM operator after password policy validation.")
    public ResponseEntity<AdmOperator> createOperator(@RequestBody AdmOperatorCreateRequest request, HttpServletRequest servletRequest) {
        String reason = auditLogService.requireReason(request.reason());
        String actor = requestUser(servletRequest, request.requestUser());
        AdmOperator operator = auditLogService.executeAudited(
                CpfTransactionContext.transactionId(),
                actor,
                "OPERATOR_CREATE",
                "adm_operator",
                request.operatorId(),
                reason,
                null,
                servletRequest.getRemoteAddr(),
                () -> operatorService.createOperator(request),
                value -> "operatorId=" + value.operatorId() + ",accountStatus=" + value.accountStatus());
        return ResponseEntity.ok(operator);
    }

    @PostMapping("/{operatorId}/contacts/raw")
    @CpfOnlineTransaction(id = "OADMOP0048", name = "ADMOperatorRawContact")
    @Operation(operationId = "admOperatorRawContact", summary = "운영자 연락처 원문 조회",
            description = "별도 PII_RAW 권한과 사유가 있는 경우에만 연락처 원문을 반환하고 감사 로그를 남깁니다.")
    public ResponseEntity<AdmOperator> rawContact(
            @PathVariable String operatorId,
            @RequestBody CpfSensitiveDataAccessRequest request,
            HttpServletRequest servletRequest) {
        String actor = requestUser(servletRequest, null);
        String auditReason = auditLogService.requireReason(request.reason());
        AdmOperator operator = operatorService.findOperatorRaw(operatorId);
        auditLogService.record(
                CpfTransactionContext.transactionId(), actor, "OPERATOR_PII_RAW_VIEW",
                "adm_operator_profile", operatorId, auditReason,
                null, "rawContactViewed=true", "PII 원문 조회", servletRequest.getRemoteAddr());
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(operator);
    }

    @PutMapping("/{operatorId}/contacts")
    @CpfOnlineTransaction(id = "OADMOP0049", name = "ADMOperatorContactUpdate")
    @Operation(operationId = "admOperatorUpdateContact", summary = "운영자 연락처 수정",
            description = "Directory/Profile 연락처를 낙관적 잠금으로 수정합니다. 빈 값은 보존하고 clear 플래그만 명시적 삭제로 처리합니다.")
    public ResponseEntity<AdmOperator> updateContact(
            @PathVariable String operatorId,
            @RequestBody AdmOperatorContactUpdateRequest request,
            HttpServletRequest servletRequest) {
        String actor = requestUser(servletRequest, request.requestUser());
        String reason = auditLogService.requireReason(request.reason());
        AdmOperator before = operatorService.findOperators().stream()
                .filter(value -> value.operatorId().equals(operatorId)).findFirst().orElse(null);
        AdmOperator result = auditLogService.executeAudited(
                CpfTransactionContext.transactionId(), actor, "OPERATOR_CONTACT_UPDATE",
                "adm_operator_profile", operatorId, reason, String.valueOf(before),
                servletRequest.getRemoteAddr(),
                () -> operatorService.updateContact(operatorId, request),
                value -> "operatorId=" + value.operatorId() + ",versionNo=" + value.versionNo());
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{operatorId}/status")
    @CpfOnlineTransaction(id = "OADMOP0050", name = "ADMOperatorStatusUpdate")
    @Operation(operationId = "admOperatorUpdateStatus", summary = "운영자 계정 상태 변경",
            description = "PENDING_ACTIVATION/ACTIVE/LOCKED/SUSPENDED/DISABLED 상태를 낙관적 잠금으로 변경합니다.")
    public ResponseEntity<AdmOperator> updateStatus(
            @PathVariable String operatorId,
            @RequestBody AdmOperatorStatusUpdateRequest request,
            HttpServletRequest servletRequest) {
        String actor = requestUser(servletRequest, request.requestUser());
        String reason = auditLogService.requireReason(request.reason());
        AdmOperator before = operatorService.findOperators().stream()
                .filter(value -> value.operatorId().equals(operatorId)).findFirst().orElse(null);
        AdmOperator result = auditLogService.executeAudited(
                CpfTransactionContext.transactionId(), actor, "OPERATOR_STATUS_UPDATE",
                "adm_operator", operatorId, reason, String.valueOf(before),
                servletRequest.getRemoteAddr(),
                () -> operatorService.updateAccountStatus(operatorId, request),
                value -> "operatorId=" + value.operatorId() + ",accountStatus=" + value.accountStatus()
                        + ",versionNo=" + value.versionNo());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{operatorId}/password")
    @CpfOnlineTransaction(id = "OADMOP0032", name = "ADMOperatorPasswordChange")
    @Operation(
            operationId = "admOperatorChangePassword",
            summary = "본인 비밀번호 변경",
            description = "현재 비밀번호와 새 비밀번호 확인값, 비밀번호 정책과 최근 사용 이력을 검증한 뒤 본인 비밀번호를 변경합니다.")
    public ResponseEntity<AdmOperator> changePassword(
            @PathVariable String operatorId,
            @RequestBody AdmPasswordChangeRequest request,
            HttpServletRequest servletRequest) {
        String authenticatedOperatorId = requestUser(servletRequest, null);
        if (authenticatedOperatorId == null || !authenticatedOperatorId.equals(operatorId)) {
            throw new CpfValidationException("본인 계정의 비밀번호만 변경할 수 있습니다.");
        }
        String reason = auditLogService.requireReason(request.reason());
        AdmOperator operator = operatorService.changePassword(operatorId, request);
        auditLogService.record(
                CpfTransactionContext.transactionId(),
                authenticatedOperatorId,
                "OPERATOR_PASSWORD_CHANGE",
                "adm_operator",
                operatorId,
                reason,
                servletRequest.getRemoteAddr());
        sessionService.revokeOperatorSessions(operatorId);
        return ResponseEntity.ok(operator);
    }

    @GetMapping("/password-policy")
    @CpfOnlineTransaction(id = "OADMOP0036", name = "ADMPasswordPolicy")
    @Operation(operationId = "admOperatorPasswordPolicy", summary = "비밀번호 정책 조회", description = "ADM 운영자 비밀번호 정책을 조회합니다.")
    public ResponseEntity<Map<String, Object>> passwordPolicy() {
        return ResponseEntity.ok(operatorService.passwordPolicy());
    }

    @PostMapping("/{operatorId}/password/reset")
    @CpfOnlineTransaction(id = "OADMOP0037", name = "ADMOperatorPasswordReset")
    @Operation(operationId = "admOperatorResetPassword", summary = "비밀번호 초기화", description = "운영자 비밀번호를 초기화하고 필요 시 다음 로그인 강제 변경을 설정합니다.")
    public ResponseEntity<AdmOperator> resetPassword(
            @PathVariable String operatorId,
            @RequestBody AdmOperatorPasswordResetRequest request,
            HttpServletRequest servletRequest) {
        String reason = auditLogService.requireReason(request.reason());
        AdmOperator before = operatorService.findOperators().stream()
                .filter(operator -> operator.operatorId().equals(operatorId))
                .findFirst()
                .orElse(null);
        AdmOperator operator = operatorService.resetPassword(operatorId, request);
        auditLogService.record(
                CpfTransactionContext.transactionId(),
                requestUser(servletRequest, request.requestUser()),
                "OPERATOR_PASSWORD_RESET",
                "adm_operator",
                operatorId,
                reason,
                String.valueOf(before),
                String.valueOf(operator),
                "비밀번호 초기화",
                servletRequest.getRemoteAddr());
        sessionService.revokeOperatorSessions(operatorId);
        return ResponseEntity.ok(operator);
    }

    @PostMapping("/{operatorId}/unlock")
    @CpfOnlineTransaction(id = "OADMOP0038", name = "ADMOperatorUnlock")
    @Operation(operationId = "admOperatorUnlockOperator", summary = "운영자 잠금 해제", description = "운영자 계정 잠금과 로그인 실패 횟수를 초기화합니다.")
    public ResponseEntity<AdmOperator> unlockOperator(
            @PathVariable String operatorId,
            @RequestBody AdmSessionRevokeRequest request,
            HttpServletRequest servletRequest) {
        String reason = auditLogService.requireReason(request.reason());
        AdmOperator before = operatorService.findOperators().stream()
                .filter(operator -> operator.operatorId().equals(operatorId))
                .findFirst()
                .orElse(null);
        AdmOperator operator = operatorService.unlockOperator(operatorId, requestUser(servletRequest, request.requestUser()));
        auditLogService.record(
                CpfTransactionContext.transactionId(),
                requestUser(servletRequest, request.requestUser()),
                "OPERATOR_UNLOCK",
                "adm_operator",
                operatorId,
                reason,
                String.valueOf(before),
                String.valueOf(operator),
                "운영자 잠금 해제",
                servletRequest.getRemoteAddr());
        return ResponseEntity.ok(operator);
    }

    @PutMapping("/{operatorId}/roles")
    @CpfOnlineTransaction(id = "OADMOP0039", name = "ADMOperatorRoleUpdate")
    @Operation(operationId = "admOperatorUpdateRoles", summary = "운영자 역할 변경", description = "운영자에게 부여된 ADM 역할을 변경합니다.")
    public ResponseEntity<AdmOperator> updateRoles(
            @PathVariable String operatorId,
            @RequestBody AdmOperatorRoleUpdateRequest request,
            HttpServletRequest servletRequest) {
        String reason = auditLogService.requireReason(request.reason());
        AdmOperator before = operatorService.findOperators().stream()
                .filter(operator -> operator.operatorId().equals(operatorId))
                .findFirst()
                .orElse(null);
        AdmOperator operator = operatorService.updateRoles(operatorId, request);
        auditLogService.record(
                CpfTransactionContext.transactionId(),
                requestUser(servletRequest, request.requestUser()),
                "OPERATOR_ROLE_UPDATE",
                "adm_operator_role",
                operatorId,
                reason,
                String.valueOf(before),
                String.valueOf(operator),
                "운영자 역할 변경",
                servletRequest.getRemoteAddr());
        return ResponseEntity.ok(operator);
    }

    @GetMapping("/password-policy/validate")
    @CpfOnlineTransaction(id = "OADMOP0033", name = "ADMPasswordPolicyValidate")
    @Operation(operationId = "admOperatorValidatePassword", summary = "Validate password policy", description = "Checks whether a password satisfies the ADM policy.")
    public ResponseEntity<Map<String, Object>> validatePassword(@RequestParam String operatorId, @RequestParam String password) {
        return ResponseEntity.ok(operatorService.validatePassword(operatorId, password));
    }

    @GetMapping("/sessions")
    @CpfOnlineTransaction(id = "OADMOP0043", name = "ADMSessionList")
    @Operation(operationId = "admOperatorFindSessions", summary = "ADM 세션 조회", description = "ADM 운영자 세션을 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findSessions(@RequestParam(required = false) String operatorId) {
        return ResponseEntity.ok(sessionService.findSessions(operatorId));
    }

    @PostMapping("/sessions/{sessionId}/revoke")
    @CpfOnlineTransaction(id = "OADMOP0046", name = "ADMSessionRevoke")
    @Operation(operationId = "admOperatorRevokeSession", summary = "ADM 세션 강제 종료", description = "지정한 ADM 세션을 폐기합니다.")
    public ResponseEntity<Map<String, Object>> revokeSession(
            @PathVariable String sessionId,
            @RequestBody AdmSessionRevokeRequest request,
            HttpServletRequest servletRequest) {
        String reason = auditLogService.requireReason(request.reason());
        int revoked = sessionService.revokeSession(sessionId);
        auditLogService.record(
                CpfTransactionContext.transactionId(),
                requestUser(servletRequest, request.requestUser()),
                "SESSION_REVOKE",
                "adm_operator_session",
                sessionId,
                reason,
                null,
                "revoked=" + revoked,
                "세션 강제 종료",
                servletRequest.getRemoteAddr());
        return ResponseEntity.ok(Map.of("revoked", revoked));
    }

    @PostMapping("/sessions/cleanup-expired")
    @CpfOnlineTransaction(id = "OADMOP0047", name = "ADMSessionCleanupExpired")
    @Operation(operationId = "admOperatorCleanupExpiredSessions", summary = "만료 세션 정리", description = "만료된 ADM 세션을 폐기 상태로 변경합니다.")
    public ResponseEntity<Map<String, Object>> cleanupExpiredSessions(
            @RequestBody AdmSessionRevokeRequest request,
            HttpServletRequest servletRequest) {
        String reason = auditLogService.requireReason(request.reason());
        int revoked = sessionService.cleanupExpiredSessions();
        auditLogService.record(
                CpfTransactionContext.transactionId(),
                requestUser(servletRequest, request.requestUser()),
                "SESSION_CLEANUP_EXPIRED",
                "adm_operator_session",
                "EXPIRED",
                reason,
                null,
                "revoked=" + revoked,
                "만료 세션 정리",
                servletRequest.getRemoteAddr());
        return ResponseEntity.ok(Map.of("revoked", revoked));
    }

    @GetMapping("/roles")
    @CpfOnlineTransaction(id = "OADMOP0034", name = "ADMRoleList")
    @Operation(operationId = "admOperatorFindRoles", summary = "List roles", description = "Returns ADM roles.")
    public ResponseEntity<List<AdmRole>> findRoles() {
        return ResponseEntity.ok(operatorService.findRoles());
    }

    @GetMapping("/menus")
    @CpfOnlineTransaction(id = "OADMOP0035", name = "ADMMenuList")
    @Operation(operationId = "admOperatorFindMenus", summary = "List menus", description = "Returns ADM menus.")
    public ResponseEntity<List<AdmMenu>> findMenus() {
        return ResponseEntity.ok(operatorService.findMenus());
    }

    private String requestUser(HttpServletRequest request, String fallback) {
        return requireOperator(request);
    }
}
