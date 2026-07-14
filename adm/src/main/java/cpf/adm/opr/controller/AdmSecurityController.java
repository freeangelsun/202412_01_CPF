package cpf.adm.opr.controller;

import cpf.adm.opr.dto.AdmIpAllowlistRequest;
import cpf.adm.opr.dto.AdmMfaOtpRequest;
import cpf.adm.opr.service.AdmAuditLogService;
import cpf.adm.opr.service.AdmSecurityOperationService;
import cpf.pfw.common.logging.CpfTransaction;
import cpf.pfw.common.logging.TransactionContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/adm/api/security")
@Tag(name = "ADM-Security", description = "ADM 보안 운영 API")
public class AdmSecurityController {
    private final AdmSecurityOperationService securityService;
    private final AdmAuditLogService auditLogService;

    public AdmSecurityController(AdmSecurityOperationService securityService, AdmAuditLogService auditLogService) {
        this.securityService = securityService;
        this.auditLogService = auditLogService;
    }

    @GetMapping("/ip-allowlist")
    @CpfTransaction(id = "ADM01SEC0010", name = "ADMIpAllowlist")
    @Operation(operationId = "admSecurityFindIpAllowlist", summary = "IP 허용 목록 조회", description = "ADM 접속 허용 IP/CIDR 목록을 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findIpAllowlist() {
        return ResponseEntity.ok(securityService.findIpAllowlist());
    }

    @PostMapping("/ip-allowlist")
    @CpfTransaction(id = "ADM03SEC0011", name = "ADMIpAllowlistSave")
    @Operation(operationId = "admSecuritySaveIpAllowlist", summary = "IP 허용 목록 저장", description = "ADM 접속 허용 IP/CIDR 목록을 등록 또는 수정합니다.")
    public ResponseEntity<Map<String, Object>> saveIpAllowlist(
            @RequestBody AdmIpAllowlistRequest request,
            HttpServletRequest servletRequest) {
        String reason = auditLogService.requireReason(request.reason());
        Map<String, Object> saved = securityService.upsertIpAllowlist(request);
        recordAudit(servletRequest, request.requestUser(), "IP_ALLOWLIST_SAVE", "adm_ip_allowlist",
                request.ipPattern(), reason, null, String.valueOf(saved));
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/mfa")
    @CpfTransaction(id = "ADM01SEC0012", name = "ADMMfaList")
    @Operation(operationId = "admSecurityFindMfaStates", summary = "MFA 상태 조회", description = "ADM 운영자 MFA 등록/검증 상태를 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findMfaStates() {
        return ResponseEntity.ok(securityService.findMfaStates());
    }

    @PostMapping("/mfa/{operatorId}/register")
    @CpfTransaction(id = "ADM03SEC0013", name = "ADMMfaRegister")
    @Operation(operationId = "admSecurityRegisterMfa", summary = "MFA 등록", description = "운영자 MFA secret 참조를 등록합니다.")
    public ResponseEntity<Map<String, Object>> registerMfa(
            @PathVariable String operatorId,
            @RequestBody AdmMfaOtpRequest request,
            HttpServletRequest servletRequest) {
        String reason = auditLogService.requireReason(request.reason());
        Map<String, Object> saved = securityService.registerMfa(operatorId, request);
        recordAudit(servletRequest, request.requestUser(), "MFA_REGISTER", "adm_mfa_otp_secret",
                operatorId, reason, null, maskSecret(saved));
        return ResponseEntity.ok(maskSecret(saved));
    }

    @PostMapping("/mfa/{operatorId}/verify")
    @CpfTransaction(id = "ADM03SEC0014", name = "ADMMfaVerify")
    @Operation(operationId = "admSecurityVerifyMfa", summary = "MFA 검증", description = "운영자 MFA 검증 상태를 활성화합니다.")
    public ResponseEntity<Map<String, Object>> verifyMfa(
            @PathVariable String operatorId,
            @RequestBody AdmMfaOtpRequest request,
            HttpServletRequest servletRequest) {
        String reason = auditLogService.requireReason(request.reason());
        Map<String, Object> saved = securityService.verifyMfa(operatorId, request);
        recordAudit(servletRequest, request.requestUser(), "MFA_VERIFY", "adm_mfa_otp_secret",
                operatorId, reason, null, maskSecret(saved));
        return ResponseEntity.ok(maskSecret(saved));
    }

    @PostMapping("/mfa/{operatorId}/disable")
    @CpfTransaction(id = "ADM03SEC0015", name = "ADMMfaDisable")
    @Operation(operationId = "admSecurityDisableMfa", summary = "MFA 해제", description = "운영자 MFA를 비활성화합니다.")
    public ResponseEntity<Map<String, Object>> disableMfa(
            @PathVariable String operatorId,
            @RequestParam String reason,
            @RequestParam(defaultValue = "SYSTEM") String requestUser,
            HttpServletRequest servletRequest) {
        String requiredReason = auditLogService.requireReason(reason);
        Map<String, Object> saved = securityService.disableMfa(operatorId, requestUser(servletRequest, requestUser));
        recordAudit(servletRequest, requestUser, "MFA_DISABLE", "adm_mfa_otp_secret",
                operatorId, requiredReason, null, maskSecret(saved));
        return ResponseEntity.ok(maskSecret(saved));
    }

    private void recordAudit(
            HttpServletRequest servletRequest,
            String requestUser,
            String actionType,
            String targetType,
            String targetId,
            String reason,
            String beforeData,
            Object afterData) {
        auditLogService.record(
                TransactionContext.getOrCreateTransactionId(),
                requestUser(servletRequest, requestUser),
                actionType,
                targetType,
                targetId,
                reason,
                beforeData,
                String.valueOf(afterData),
                actionType,
                servletRequest.getRemoteAddr());
    }

    private Map<String, Object> maskSecret(Map<String, Object> source) {
        if (source == null || source.isEmpty()) {
            return Map.of();
        }
        java.util.LinkedHashMap<String, Object> masked = new java.util.LinkedHashMap<>(source);
        masked.computeIfPresent("SECRET_REF", (key, value) -> "********");
        masked.computeIfPresent("secretRef", (key, value) -> "********");
        return masked;
    }

    private String requestUser(HttpServletRequest request, String fallback) {
        Object operatorId = request.getAttribute("adm.operatorId");
        if (operatorId instanceof String value && !value.isBlank()) {
            return value;
        }
        return fallback;
    }
}
