package cpf.adm.opr.controller;

import cpf.adm.opr.dto.AdmMenu;
import cpf.adm.opr.dto.AdmOperator;
import cpf.adm.opr.dto.AdmOperatorCreateRequest;
import cpf.adm.opr.dto.AdmOperatorPasswordResetRequest;
import cpf.adm.opr.dto.AdmOperatorRoleUpdateRequest;
import cpf.adm.opr.dto.AdmPasswordChangeRequest;
import cpf.adm.opr.dto.AdmRole;
import cpf.adm.opr.dto.AdmSessionRevokeRequest;
import cpf.adm.opr.service.AdmOperatorService;
import cpf.adm.opr.service.AdmAuditLogService;
import cpf.adm.opr.service.AdmSessionService;
import cpf.pfw.common.logging.TransactionContext;
import cpf.pfw.common.execution.CpfOnlineTransaction;
import cpf.pfw.common.exception.CpfValidationException;
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

@RestController
@RequestMapping("/adm/api/operators")
@Tag(name = "ADM-OPR Operators", description = "Operator, role, and menu management APIs")
public class AdmOperatorController {
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
    @CpfOnlineTransaction(id = "OADM-OPR-01-0030", name = "ADMOperatorList")
    @Operation(operationId = "admOperatorFindOperators", summary = "List operators", description = "Returns ADM operators and account status.")
    public ResponseEntity<List<AdmOperator>> findOperators() {
        return ResponseEntity.ok(operatorService.findOperators());
    }

    @PostMapping
    @CpfOnlineTransaction(id = "OADM-OPR-02-0031", name = "ADMOperatorCreate")
    @Operation(operationId = "admOperatorCreateOperator", summary = "Create operator", description = "Creates an ADM operator after password policy validation.")
    public ResponseEntity<AdmOperator> createOperator(@RequestBody AdmOperatorCreateRequest request, HttpServletRequest servletRequest) {
        String reason = auditLogService.requireReason(request.reason());
        AdmOperator operator = operatorService.createOperator(request);
        auditLogService.record(
                TransactionContext.getOrCreateTransactionId(),
                requestUser(servletRequest, request.requestUser()),
                "OPERATOR_CREATE",
                "adm_operator",
                operator.operatorId(),
                reason,
                servletRequest.getRemoteAddr());
        return ResponseEntity.ok(operator);
    }

    @PostMapping("/{operatorId}/password")
    @CpfOnlineTransaction(id = "OADM-OPR-03-0032", name = "ADMOperatorPasswordChange")
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
                TransactionContext.getOrCreateTransactionId(),
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
    @CpfOnlineTransaction(id = "OADM-OPR-01-0036", name = "ADMPasswordPolicy")
    @Operation(operationId = "admOperatorPasswordPolicy", summary = "비밀번호 정책 조회", description = "ADM 운영자 비밀번호 정책을 조회합니다.")
    public ResponseEntity<Map<String, Object>> passwordPolicy() {
        return ResponseEntity.ok(operatorService.passwordPolicy());
    }

    @PostMapping("/{operatorId}/password/reset")
    @CpfOnlineTransaction(id = "OADM-OPR-03-0037", name = "ADMOperatorPasswordReset")
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
                TransactionContext.getOrCreateTransactionId(),
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
    @CpfOnlineTransaction(id = "OADM-OPR-03-0038", name = "ADMOperatorUnlock")
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
                TransactionContext.getOrCreateTransactionId(),
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
    @CpfOnlineTransaction(id = "OADM-OPR-03-0039", name = "ADMOperatorRoleUpdate")
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
                TransactionContext.getOrCreateTransactionId(),
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
    @CpfOnlineTransaction(id = "OADM-OPR-06-0033", name = "ADMPasswordPolicyValidate")
    @Operation(operationId = "admOperatorValidatePassword", summary = "Validate password policy", description = "Checks whether a password satisfies the ADM policy.")
    public ResponseEntity<Map<String, Object>> validatePassword(@RequestParam String operatorId, @RequestParam String password) {
        return ResponseEntity.ok(operatorService.validatePassword(operatorId, password));
    }

    @GetMapping("/sessions")
    @CpfOnlineTransaction(id = "OADM-OPR-01-0043", name = "ADMSessionList")
    @Operation(operationId = "admOperatorFindSessions", summary = "ADM 세션 조회", description = "ADM 운영자 세션을 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findSessions(@RequestParam(required = false) String operatorId) {
        return ResponseEntity.ok(sessionService.findSessions(operatorId));
    }

    @PostMapping("/sessions/{sessionId}/revoke")
    @CpfOnlineTransaction(id = "OADM-OPR-03-0044", name = "ADMSessionRevoke")
    @Operation(operationId = "admOperatorRevokeSession", summary = "ADM 세션 강제 종료", description = "지정한 ADM 세션을 폐기합니다.")
    public ResponseEntity<Map<String, Object>> revokeSession(
            @PathVariable String sessionId,
            @RequestBody AdmSessionRevokeRequest request,
            HttpServletRequest servletRequest) {
        String reason = auditLogService.requireReason(request.reason());
        int revoked = sessionService.revokeSession(sessionId);
        auditLogService.record(
                TransactionContext.getOrCreateTransactionId(),
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
    @CpfOnlineTransaction(id = "OADM-OPR-03-0045", name = "ADMSessionCleanupExpired")
    @Operation(operationId = "admOperatorCleanupExpiredSessions", summary = "만료 세션 정리", description = "만료된 ADM 세션을 폐기 상태로 변경합니다.")
    public ResponseEntity<Map<String, Object>> cleanupExpiredSessions(
            @RequestBody AdmSessionRevokeRequest request,
            HttpServletRequest servletRequest) {
        String reason = auditLogService.requireReason(request.reason());
        int revoked = sessionService.cleanupExpiredSessions();
        auditLogService.record(
                TransactionContext.getOrCreateTransactionId(),
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
    @CpfOnlineTransaction(id = "OADM-OPR-01-0034", name = "ADMRoleList")
    @Operation(operationId = "admOperatorFindRoles", summary = "List roles", description = "Returns ADM roles.")
    public ResponseEntity<List<AdmRole>> findRoles() {
        return ResponseEntity.ok(operatorService.findRoles());
    }

    @GetMapping("/menus")
    @CpfOnlineTransaction(id = "OADM-OPR-01-0035", name = "ADMMenuList")
    @Operation(operationId = "admOperatorFindMenus", summary = "List menus", description = "Returns ADM menus.")
    public ResponseEntity<List<AdmMenu>> findMenus() {
        return ResponseEntity.ok(operatorService.findMenus());
    }

    private String requestUser(HttpServletRequest request, String fallback) {
        Object operatorId = request.getAttribute("adm.operatorId");
        if (operatorId instanceof String value && !value.isBlank()) {
            return value;
        }
        return fallback;
    }
}
