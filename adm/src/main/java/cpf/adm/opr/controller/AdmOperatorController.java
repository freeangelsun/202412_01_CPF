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
import cpf.pfw.common.logging.FpsTransaction;
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
    @FpsTransaction(id = "ADM01OPR0030", name = "ADMOperatorList")
    @Operation(summary = "List operators", description = "Returns ADM operators and account status.")
    public ResponseEntity<List<AdmOperator>> findOperators() {
        return ResponseEntity.ok(operatorService.findOperators());
    }

    @PostMapping
    @FpsTransaction(id = "ADM02OPR0031", name = "ADMOperatorCreate")
    @Operation(summary = "Create operator", description = "Creates an ADM operator after password policy validation.")
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
    @FpsTransaction(id = "ADM03OPR0032", name = "ADMOperatorPasswordChange")
    @Operation(summary = "Change operator password", description = "Changes an operator password after policy validation.")
    public ResponseEntity<AdmOperator> changePassword(
            @PathVariable String operatorId,
            @RequestBody AdmPasswordChangeRequest request,
            HttpServletRequest servletRequest) {
        String reason = auditLogService.requireReason(request.reason());
        AdmOperator operator = operatorService.changePassword(operatorId, request);
        auditLogService.record(
                TransactionContext.getOrCreateTransactionId(),
                requestUser(servletRequest, request.requestUser()),
                "OPERATOR_PASSWORD_CHANGE",
                "adm_operator",
                operatorId,
                reason,
                servletRequest.getRemoteAddr());
        return ResponseEntity.ok(operator);
    }

    @GetMapping("/password-policy")
    @FpsTransaction(id = "ADM01OPR0036", name = "ADMPasswordPolicy")
    @Operation(summary = "비밀번호 정책 조회", description = "ADM 운영자 비밀번호 정책을 조회합니다.")
    public ResponseEntity<Map<String, Object>> passwordPolicy() {
        return ResponseEntity.ok(operatorService.passwordPolicy());
    }

    @PostMapping("/{operatorId}/password/reset")
    @FpsTransaction(id = "ADM03OPR0037", name = "ADMOperatorPasswordReset")
    @Operation(summary = "비밀번호 초기화", description = "운영자 비밀번호를 초기화하고 필요 시 다음 로그인 강제 변경을 설정합니다.")
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
        return ResponseEntity.ok(operator);
    }

    @PostMapping("/{operatorId}/unlock")
    @FpsTransaction(id = "ADM03OPR0038", name = "ADMOperatorUnlock")
    @Operation(summary = "운영자 잠금 해제", description = "운영자 계정 잠금과 로그인 실패 횟수를 초기화합니다.")
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
    @FpsTransaction(id = "ADM03OPR0039", name = "ADMOperatorRoleUpdate")
    @Operation(summary = "운영자 역할 변경", description = "운영자에게 부여된 ADM 역할을 변경합니다.")
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
    @FpsTransaction(id = "ADM06OPR0033", name = "ADMPasswordPolicyValidate")
    @Operation(summary = "Validate password policy", description = "Checks whether a password satisfies the ADM policy.")
    public ResponseEntity<Map<String, Object>> validatePassword(@RequestParam String operatorId, @RequestParam String password) {
        return ResponseEntity.ok(operatorService.validatePassword(operatorId, password));
    }

    @GetMapping("/sessions")
    @FpsTransaction(id = "ADM01OPR0043", name = "ADMSessionList")
    @Operation(summary = "ADM 세션 조회", description = "ADM 운영자 세션을 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findSessions(@RequestParam(required = false) String operatorId) {
        return ResponseEntity.ok(sessionService.findSessions(operatorId));
    }

    @PostMapping("/sessions/{sessionId}/revoke")
    @FpsTransaction(id = "ADM03OPR0044", name = "ADMSessionRevoke")
    @Operation(summary = "ADM 세션 강제 종료", description = "지정한 ADM 세션을 폐기합니다.")
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
    @FpsTransaction(id = "ADM03OPR0045", name = "ADMSessionCleanupExpired")
    @Operation(summary = "만료 세션 정리", description = "만료된 ADM 세션을 폐기 상태로 변경합니다.")
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
    @FpsTransaction(id = "ADM01OPR0034", name = "ADMRoleList")
    @Operation(summary = "List roles", description = "Returns ADM roles.")
    public ResponseEntity<List<AdmRole>> findRoles() {
        return ResponseEntity.ok(operatorService.findRoles());
    }

    @GetMapping("/menus")
    @FpsTransaction(id = "ADM01OPR0035", name = "ADMMenuList")
    @Operation(summary = "List menus", description = "Returns ADM menus.")
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
