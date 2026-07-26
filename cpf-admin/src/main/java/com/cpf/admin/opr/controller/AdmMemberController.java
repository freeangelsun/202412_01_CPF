package com.cpf.admin.opr.controller;

import com.cpf.admin.opr.dto.AdmMemberRoleRequest;
import com.cpf.admin.opr.dto.AdmMemberSaveRequest;
import com.cpf.admin.opr.dto.AdmMemberStatusRequest;
import com.cpf.admin.opr.service.AdmAuditLogService;
import com.cpf.admin.opr.service.AdmMemberOperationService;
import com.cpf.core.api.execution.CpfOnlineTransaction;
import com.cpf.core.api.page.CpfPage;
import com.cpf.core.api.logging.CpfTransactionContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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
 * ADM 회원 관리 API입니다.
 *
 * <p>회원 기본정보, 상태, 잠금, 탈퇴 복구, 회원 역할을 운영자가 관리할 수 있게 제공합니다.
 * 모든 변경 API는 서버 권한검사와 감사 사유 검사를 통과해야 합니다.</p>
 */
@RestController
@RequestMapping("/adm/api/members")
@Tag(name = "ADM-Member", description = "ADM 회원 관리와 회원 권한 관리 API")
public class AdmMemberController extends com.cpf.admin.common.base.AdmBaseController {
    private final AdmMemberOperationService memberOperationService;
    private final AdmAuditLogService auditLogService;

    public AdmMemberController(AdmMemberOperationService memberOperationService, AdmAuditLogService auditLogService) {
        this.memberOperationService = memberOperationService;
        this.auditLogService = auditLogService;
    }

    @GetMapping
    @CpfOnlineTransaction(id = "OADMMB0010", name = "ADMMemberList")
    @Operation(operationId = "admMemberFindMembers", summary = "회원 목록 조회", description = "회원번호, 고객번호, 로그인 ID, 이름, 이메일, 휴대폰, 상태, 채널, 권한 조건으로 회원을 검색합니다.")
    public ResponseEntity<List<Map<String, Object>>> findMembers(
            @RequestParam(required = false) String memberNo,
            @RequestParam(required = false) String customerNo,
            @RequestParam(required = false) String loginId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String mobileNo,
            @RequestParam(required = false) String memberStatus,
            @RequestParam(required = false) String channelCode,
            @RequestParam(required = false) String roleCode,
            @RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(memberOperationService.findMembers(
                memberNo, customerNo, loginId, name, email, mobileNo, memberStatus, channelCode, roleCode, limit));
    }

    @GetMapping("/page")
    @CpfOnlineTransaction(id = "OADMMB0018", name = "ADMMemberPage")
    @Operation(operationId = "admMemberFindMembersPage", summary = "회원 서버 Paging 조회", description = "MBR Owner DB에서 count와 LIMIT/OFFSET을 수행하여 ADM에 표준 Page를 반환합니다.")
    public ResponseEntity<CpfPage<Map<String,Object>>> findMembersPage(
            @RequestParam(required=false) String memberNo,@RequestParam(required=false) String customerNo,@RequestParam(required=false) String loginId,@RequestParam(required=false) String name,@RequestParam(required=false) String email,@RequestParam(required=false) String mobileNo,@RequestParam(required=false) String memberStatus,@RequestParam(required=false) String channelCode,@RequestParam(required=false) String roleCode,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size) {
        return ResponseEntity.ok(memberOperationService.findMembersPage(memberNo,customerNo,loginId,name,email,mobileNo,memberStatus,channelCode,roleCode,page,size));
    }

    @GetMapping("/member-number-issues")
    @CpfOnlineTransaction(id = "OADMMB0017", name = "ADMMemberNumberIssueHistory")
    @Operation(operationId = "admMemberFindNumberIssueHistory", summary = "회원번호 발급 이력 조회",
            description = "자동/수동 회원번호 발급 이력을 회원번호, 발급유형, 발급자로 검색합니다.")
    public ResponseEntity<List<Map<String, Object>>> findMemberNumberIssueHistory(
            @RequestParam(required = false) String memberNo,
            @RequestParam(required = false) String issueType,
            @RequestParam(required = false) String issuedBy,
            @RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(memberOperationService.findMemberNoIssueHistory(memberNo, issueType, issuedBy, limit));
    }

    @GetMapping("/{memberId}")
    @CpfOnlineTransaction(id = "OADMMB0011", name = "ADMMemberDetail")
    @Operation(operationId = "admMemberFindMemberDetail", summary = "회원 상세 조회", description = "회원 기본정보, 권한, 로그인 이력, 거래 로그, 감사 로그를 조회합니다.")
    public ResponseEntity<Map<String, Object>> findMemberDetail(@PathVariable long memberId) {
        return ResponseEntity.ok(memberOperationService.findMemberDetail(memberId));
    }

    @PostMapping
    @CpfOnlineTransaction(id = "OADMMB0012", name = "ADMMemberCreate")
    @Operation(operationId = "admMemberCreateMember", summary = "회원 등록", description = "ADM에서 회원을 등록하고 감사 로그를 남깁니다.")
    public ResponseEntity<Map<String, Object>> createMember(
            @RequestBody AdmMemberSaveRequest request,
            HttpServletRequest servletRequest) {
        String reason = auditLogService.requireReason(request.reason());
        String operatorId = requireOperator(servletRequest);
        Map<String, Object> after = memberOperationService.createMember(request, operatorId);
        recordAudit(servletRequest, operatorId, "MEMBER_CREATE", "mbr_member",
                String.valueOf(after.get("id")), reason, null, String.valueOf(after));
        return ResponseEntity.ok(after);
    }

    @PutMapping("/{memberId}")
    @CpfOnlineTransaction(id = "OADMMB0013", name = "ADMMemberUpdate")
    @Operation(operationId = "admMemberUpdateMember", summary = "회원 수정", description = "ADM에서 회원 기본정보를 수정하고 변경 전/후 감사 로그를 남깁니다.")
    public ResponseEntity<Map<String, Object>> updateMember(
            @PathVariable long memberId,
            @RequestBody AdmMemberSaveRequest request,
            HttpServletRequest servletRequest) {
        String reason = auditLogService.requireReason(request.reason());
        String operatorId = requireOperator(servletRequest);
        Map<String, Object> before = memberOperationService.findMemberDetail(memberId);
        Map<String, Object> after = memberOperationService.updateMember(memberId, request, operatorId);
        recordAudit(servletRequest, operatorId, "MEMBER_UPDATE", "mbr_member",
                String.valueOf(memberId), reason, String.valueOf(before.get("member")), String.valueOf(after));
        return ResponseEntity.ok(after);
    }

    @PutMapping("/{memberId}/status")
    @CpfOnlineTransaction(id = "OADMMB0014", name = "ADMMemberStatusUpdate")
    @Operation(operationId = "admMemberUpdateStatus", summary = "회원 상태 변경", description = "회원 상태, 잠금 여부, 탈퇴 여부를 변경합니다.")
    public ResponseEntity<Map<String, Object>> updateStatus(
            @PathVariable long memberId,
            @RequestBody AdmMemberStatusRequest request,
            HttpServletRequest servletRequest) {
        String reason = auditLogService.requireReason(request.reason());
        String operatorId = requireOperator(servletRequest);
        Map<String, Object> before = memberOperationService.findMemberDetail(memberId);
        Map<String, Object> after = memberOperationService.updateStatus(memberId, request, operatorId);
        recordAudit(servletRequest, operatorId, "MEMBER_STATUS", "mbr_member",
                String.valueOf(memberId), reason, String.valueOf(before.get("member")), String.valueOf(after));
        return ResponseEntity.ok(after);
    }

    @PostMapping("/{memberId}/roles")
    @CpfOnlineTransaction(id = "OADMMB0015", name = "ADMMemberRoleGrant")
    @Operation(operationId = "admMemberGrantRole", summary = "회원 권한 부여", description = "회원 역할이나 서비스 접근 권한을 부여하거나 갱신합니다.")
    public ResponseEntity<Map<String, Object>> grantRole(
            @PathVariable long memberId,
            @RequestBody AdmMemberRoleRequest request,
            HttpServletRequest servletRequest) {
        String reason = auditLogService.requireReason(request.reason());
        String operatorId = requireOperator(servletRequest);
        Map<String, Object> result = memberOperationService.grantRole(memberId, request, operatorId);
        recordAudit(servletRequest, operatorId, "MEMBER_ROLE_GRANT", "mbr_member_role",
                String.valueOf(memberId), reason, String.valueOf(result.get("before")), String.valueOf(result.get("after")));
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{memberId}/roles/{roleCode}")
    @CpfOnlineTransaction(id = "OADMMB0016", name = "ADMMemberRoleRevoke")
    @Operation(operationId = "admMemberRevokeRole", summary = "회원 권한 회수", description = "회원 역할이나 서비스 접근 권한을 비활성화하고 변경 이력을 남깁니다.")
    public ResponseEntity<Map<String, Object>> revokeRole(
            @PathVariable long memberId,
            @PathVariable String roleCode,
            @RequestParam(defaultValue = "MBR") String serviceCode,
            @RequestParam long expectedVersion,
            @RequestParam String idempotencyKey,
            @RequestParam String reason,
            HttpServletRequest servletRequest) {
        String requiredReason = auditLogService.requireReason(reason);
        String operatorId = requireOperator(servletRequest);
        Map<String, Object> result = memberOperationService.revokeRole(
                memberId, roleCode, serviceCode, expectedVersion, idempotencyKey, requiredReason, operatorId);
        recordAudit(servletRequest, operatorId, "MEMBER_ROLE_REVOKE", "mbr_member_role",
                String.valueOf(memberId), requiredReason, String.valueOf(result.get("before")), String.valueOf(result.get("after")));
        return ResponseEntity.ok(result);
    }

    private void recordAudit(
            HttpServletRequest servletRequest,
            String requestUser,
            String actionType,
            String targetType,
            String targetId,
            String reason,
            String beforeData,
            String afterData) {
        auditLogService.record(
                CpfTransactionContext.transactionId(),
                requestUser,
                actionType,
                targetType,
                targetId,
                reason,
                beforeData,
                afterData,
                actionType,
                servletRequest.getRemoteAddr());
    }

}
