package com.cpf.admin.opr.controller;

import com.cpf.admin.opr.dto.AdmMenu;
import com.cpf.admin.opr.dto.AdmOperator;
import com.cpf.admin.opr.dto.AdmOperatorCreateRequest;
import com.cpf.admin.opr.dto.AdmOperatorRawContactResponse;
import com.cpf.admin.opr.dto.AdmOperatorContactUpdateRequest;
import com.cpf.admin.opr.dto.AdmOperatorStatusUpdateRequest;
import com.cpf.admin.opr.dto.AdmOperatorPasswordResetRequest;
import com.cpf.admin.opr.dto.AdmOperatorRoleUpdateRequest;
import com.cpf.admin.opr.dto.AdmPasswordChangeRequest;
import com.cpf.admin.opr.dto.AdmRole;
import com.cpf.admin.opr.dto.AdmSessionRevokeRequest;
import com.cpf.admin.opr.dto.AdmPasswordPolicyResponse;
import com.cpf.admin.opr.dto.AdmPasswordValidationResponse;
import com.cpf.admin.opr.dto.AdmSessionSummaryResponse;
import com.cpf.admin.opr.dto.AdmSessionMutationResponse;
import com.cpf.admin.opr.service.AdmOperatorService;
import com.cpf.admin.opr.service.AdmAuditLogService;
import com.cpf.admin.opr.service.AdmSessionService;
import com.cpf.core.api.context.CpfContexts;
import com.cpf.security.api.CpfSensitiveDataAccessRequest;
import com.cpf.core.api.error.CpfValidationException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    @GetMapping    @Operation(operationId = "admOperatorFindOperators", summary = "List operators", description = "Returns ADM operators and account status.")
    public ResponseEntity<List<AdmOperator>> findOperators() {
        return ResponseEntity.ok(operatorService.findOperators());
    }

    @PostMapping    @Operation(operationId = "admOperatorCreateOperator", summary = "운영자 생성",
            description = "operationId는 필수이며 결과불명 재시도에서 동일 값을 재사용합니다. 일반 운영자 생성 요청은 Role 동시부여를 허용하지 않습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "생성 성공 또는 동일 operationId의 동일 생성 결과"),
            @ApiResponse(responseCode = "400", description = "비밀번호/필수값/Role 동시부여 정책 위반"),
            @ApiResponse(responseCode = "409", description = "operationId 충돌 또는 동시 생성 충돌"),
            @ApiResponse(responseCode = "503", description = "DB/Audit 저장소 장애로 결과 확정 불가")
    })
    public ResponseEntity<AdmOperator> createOperator(@RequestBody AdmOperatorCreateRequest request, HttpServletRequest servletRequest) {
        String reason = auditLogService.requireReason(request.reason());
        String actor = requestUser(servletRequest, request.requestUser());
        AdmOperator operator = auditLogService.executeAudited(
                CpfContexts.transactionId(),
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

    @GetMapping("/operations/{operationId}")    @Operation(operationId = "admOperatorFindCreateResult", summary = "operationId로 운영자 생성 결과 조회",
            description = "생성 응답 유실이나 timeout 결과불명 시 최초 요청의 동일 operationId로 생성 결과를 조회합니다.")
    public ResponseEntity<AdmOperator> findCreateResult(@PathVariable String operationId) {
        return ResponseEntity.ok(operatorService.findOperatorByCreateOperationId(operationId));
    }

    @PostMapping("/{operatorId}/contacts/raw")    @Operation(operationId = "admOperatorRawContact", summary = "운영자 연락처 원문 조회",
            description = "PII_RAW 권한과 사유를 검증하고 감사 DB 기록이 성공한 경우에만 mobileNo/officePhoneNo 최소 Projection을 no-store로 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "감사 완료된 최소 Raw Projection"),
            @ApiResponse(responseCode = "400", description = "감사 사유 누락/형식 오류"),
            @ApiResponse(responseCode = "403", description = "PII_RAW 권한 없음"),
            @ApiResponse(responseCode = "409", description = "동시 변경으로 조회 계약 충돌"),
            @ApiResponse(responseCode = "503", description = "DB 또는 Audit 저장 실패. Raw 데이터는 반환하지 않음")
    })
    public ResponseEntity<AdmOperatorRawContactResponse> rawContact(
            @PathVariable String operatorId,
            @RequestBody CpfSensitiveDataAccessRequest request,
            HttpServletRequest servletRequest) {
        String actor = requestUser(servletRequest, null);
        String auditReason = auditLogService.requireReason(request.reason());
        AdmOperatorRawContactResponse result = auditLogService.executeAudited(
                CpfContexts.transactionId(), actor, "OPERATOR_PII_RAW_VIEW",
                "adm_operator_profile", operatorId, auditReason, null, servletRequest.getRemoteAddr(),
                () -> operatorService.findOperatorRaw(operatorId),
                value -> "operatorId=" + value.operatorId() + ",rawContactViewed=true");
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(result);
    }

    @PutMapping("/{operatorId}/contacts")    @Operation(operationId = "admOperatorUpdateContact", summary = "운영자 연락처 수정",
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
                CpfContexts.transactionId(), actor, "OPERATOR_CONTACT_UPDATE",
                "adm_operator_profile", operatorId, reason, String.valueOf(before),
                servletRequest.getRemoteAddr(),
                () -> operatorService.updateContact(operatorId, request),
                value -> "operatorId=" + value.operatorId() + ",versionNo=" + value.versionNo());
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{operatorId}/status")    @Operation(operationId = "admOperatorUpdateStatus", summary = "운영자 계정 상태 변경",
            description = "expectedVersion 기반 CAS와 상태 Transition Matrix를 적용하고 같은 운영 변경 책임에서 기존 Session을 무효화합니다. Role 없는 ACTIVE 전환은 거부합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상태 변경과 Session 무효화 성공"),
            @ApiResponse(responseCode = "400", description = "허용되지 않은 상태 전이 또는 Role 없는 ACTIVE"),
            @ApiResponse(responseCode = "409", description = "expectedVersion 충돌"),
            @ApiResponse(responseCode = "503", description = "DB/Session 저장소 장애로 원자 처리 실패")
    })
    public ResponseEntity<AdmOperator> updateStatus(
            @PathVariable String operatorId,
            @RequestBody AdmOperatorStatusUpdateRequest request,
            HttpServletRequest servletRequest) {
        String actor = requestUser(servletRequest, request.requestUser());
        String reason = auditLogService.requireReason(request.reason());
        AdmOperator before = operatorService.findOperators().stream()
                .filter(value -> value.operatorId().equals(operatorId)).findFirst().orElse(null);
        AdmOperator result = auditLogService.executeAudited(
                CpfContexts.transactionId(), actor, "OPERATOR_STATUS_UPDATE",
                "adm_operator", operatorId, reason, String.valueOf(before),
                servletRequest.getRemoteAddr(),
                () -> operatorService.updateAccountStatus(operatorId, request),
                value -> "operatorId=" + value.operatorId() + ",accountStatus=" + value.accountStatus()
                        + ",versionNo=" + value.versionNo());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{operatorId}/password")    @Operation(
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
                CpfContexts.transactionId(),
                authenticatedOperatorId,
                "OPERATOR_PASSWORD_CHANGE",
                "adm_operator",
                operatorId,
                reason,
                servletRequest.getRemoteAddr());
        return ResponseEntity.ok(operator);
    }

    @GetMapping("/password-policy")    @Operation(operationId = "admOperatorPasswordPolicy", summary = "비밀번호 정책 조회", description = "ADM 운영자 비밀번호 정책을 조회합니다.")
    public ResponseEntity<AdmPasswordPolicyResponse> passwordPolicy() {
        return ResponseEntity.ok(operatorService.passwordPolicy());
    }

    @PostMapping("/{operatorId}/password/reset")    @Operation(operationId = "admOperatorResetPassword", summary = "비밀번호 초기화", description = "운영자 비밀번호를 초기화하고 필요 시 다음 로그인 강제 변경을 설정합니다.")
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
                CpfContexts.transactionId(),
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

    @PostMapping("/{operatorId}/unlock")    @Operation(operationId = "admOperatorUnlockOperator", summary = "운영자 잠금 해제", description = "운영자 계정 잠금과 로그인 실패 횟수를 초기화합니다.")
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
                CpfContexts.transactionId(),
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

    @PutMapping("/{operatorId}/roles")    @Operation(operationId = "admOperatorUpdateRoles", summary = "운영자 역할 변경", description = "운영자에게 부여된 ADM 역할을 변경합니다.")
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
                CpfContexts.transactionId(),
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

    @GetMapping("/password-policy/validate")    @Operation(operationId = "admOperatorValidatePassword", summary = "Validate password policy", description = "Checks whether a password satisfies the ADM policy.")
    public ResponseEntity<AdmPasswordValidationResponse> validatePassword(@RequestParam String operatorId, @RequestParam String password) {
        return ResponseEntity.ok(operatorService.validatePassword(operatorId, password));
    }

    @GetMapping("/sessions")    @Operation(operationId = "admOperatorFindSessions", summary = "ADM 세션 조회", description = "ADM 운영자 세션을 조회합니다.")
    public ResponseEntity<List<AdmSessionSummaryResponse>> findSessions(@RequestParam(required = false) String operatorId) {
        return ResponseEntity.ok(sessionService.findSessions(operatorId));
    }

    @PostMapping("/sessions/{sessionId}/revoke")    @Operation(operationId = "admOperatorRevokeSession", summary = "ADM 세션 강제 종료", description = "지정한 ADM 세션을 폐기합니다.")
    public ResponseEntity<AdmSessionMutationResponse> revokeSession(
            @PathVariable String sessionId,
            @RequestBody AdmSessionRevokeRequest request,
            HttpServletRequest servletRequest) {
        String reason = auditLogService.requireReason(request.reason());
        int revoked = sessionService.revokeSession(sessionId);
        auditLogService.record(
                CpfContexts.transactionId(),
                requestUser(servletRequest, request.requestUser()),
                "SESSION_REVOKE",
                "adm_operator_session",
                sessionId,
                reason,
                null,
                "revoked=" + revoked,
                "세션 강제 종료",
                servletRequest.getRemoteAddr());
        return ResponseEntity.ok(new AdmSessionMutationResponse(revoked));
    }

    @PostMapping("/sessions/cleanup-expired")    @Operation(operationId = "admOperatorCleanupExpiredSessions", summary = "만료 세션 정리", description = "만료된 ADM 세션을 폐기 상태로 변경합니다.")
    public ResponseEntity<AdmSessionMutationResponse> cleanupExpiredSessions(
            @RequestBody AdmSessionRevokeRequest request,
            HttpServletRequest servletRequest) {
        String reason = auditLogService.requireReason(request.reason());
        int revoked = sessionService.cleanupExpiredSessions();
        auditLogService.record(
                CpfContexts.transactionId(),
                requestUser(servletRequest, request.requestUser()),
                "SESSION_CLEANUP_EXPIRED",
                "adm_operator_session",
                "EXPIRED",
                reason,
                null,
                "revoked=" + revoked,
                "만료 세션 정리",
                servletRequest.getRemoteAddr());
        return ResponseEntity.ok(new AdmSessionMutationResponse(revoked));
    }

    @GetMapping("/roles")    @Operation(operationId = "admOperatorFindRoles", summary = "List roles", description = "Returns ADM roles.")
    public ResponseEntity<List<AdmRole>> findRoles() {
        return ResponseEntity.ok(operatorService.findRoles());
    }

    @GetMapping("/menus")    @Operation(operationId = "admOperatorFindMenus", summary = "List menus", description = "Returns ADM menus.")
    public ResponseEntity<List<AdmMenu>> findMenus() {
        return ResponseEntity.ok(operatorService.findMenus());
    }

    private String requestUser(HttpServletRequest request, String fallback) {
        return requireOperator(request);
    }
}
