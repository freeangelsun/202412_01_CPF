package com.cpf.admin.opr.controller;

import com.cpf.admin.opr.dto.AdmChannelPackageImportRequest;
import com.cpf.admin.opr.dto.AdmChannelPolicySaveRequest;
import com.cpf.admin.opr.dto.AdmChannelSaveRequest;
import com.cpf.admin.opr.service.AdmAuditLogService;
import com.cpf.platform.operations.channelregistry.application.CpfChannelPolicyService;
import com.cpf.platform.operations.channelregistry.model.CpfChannelDefinition;
import com.cpf.platform.operations.channelregistry.model.CpfChannelExecutionPolicy;
import com.cpf.platform.operations.channelregistry.model.CpfChannelPolicyPackage;
import com.cpf.platform.operations.channelregistry.model.CpfChannelPolicySnapshot;
import com.cpf.foundation.annotation.CpfOnlineTransaction;
import com.cpf.core.api.context.CpfContexts;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import com.cpf.web.api.CpfController;

/** CPF 채널 레지스트리와 거래별 채널 허용 정책을 ADM에서 운영합니다. */
@CpfController
@RequestMapping("/adm/api/channels")
@Tag(name = "ADM-OPR Channel Policy", description = "채널 레지스트리, 실행 정책, 불변 스냅샷과 정책 패키지 관리")
public class AdmChannelController extends com.cpf.admin.common.base.AdmBaseController {
    private final CpfChannelPolicyService channelPolicyService;
    private final AdmAuditLogService auditLogService;

    public AdmChannelController(
            CpfChannelPolicyService channelPolicyService,
            AdmAuditLogService auditLogService) {
        this.channelPolicyService = channelPolicyService;
        this.auditLogService = auditLogService;
    }

    @GetMapping
    @CpfOnlineTransaction(id = "OADMCH0001", name = "ADMChannelPolicySnapshot", ownerDomain="ADM")
    @Operation(operationId = "admChannelFindSnapshot", summary = "채널 정책 스냅샷 조회",
            description = "Gateway와 온라인 거래가 현재 사용하는 채널 레지스트리, 실행 정책과 버전을 조회합니다.")
    public ResponseEntity<CpfChannelPolicySnapshot> findSnapshot() {
        return ResponseEntity.ok(channelPolicyService.snapshot());
    }

    @PostMapping("/refresh")
    @CpfOnlineTransaction(id = "OADMCH0002", name = "ADMChannelPolicyRefresh", ownerDomain="ADM")
    @Operation(operationId = "admChannelRefreshSnapshot", summary = "채널 정책 스냅샷 갱신",
            description = "cpfDB 정본을 다시 읽어 불변 스냅샷을 원자적으로 교체합니다.")
    public ResponseEntity<CpfChannelPolicySnapshot> refresh(
            @org.springframework.web.bind.annotation.RequestParam
            @jakarta.validation.constraints.NotBlank String reason,
            HttpServletRequest servletRequest) {
        String actor = requestUser(servletRequest, null);
        String auditReason = auditLogService.requireReason(reason);
        CpfChannelPolicySnapshot before = channelPolicyService.snapshot();
        CpfChannelPolicySnapshot after = channelPolicyService.refresh();
        audit(servletRequest, actor, "CHANNEL_POLICY_REFRESH", "SNAPSHOT", auditReason, before, after);
        return ResponseEntity.ok(after);
    }

    @PutMapping("/{channelCode}")
    @CpfOnlineTransaction(id = "OADMCH0003", name = "ADMChannelSave", ownerDomain="ADM")
    @Operation(operationId = "admChannelSave", summary = "채널 등록 또는 수정",
            description = "채널 신뢰 수준, 인증·서명 요구와 사용 상태를 저장하고 새 정책 버전을 발급합니다.")
    public ResponseEntity<CpfChannelPolicySnapshot> saveChannel(
            @PathVariable @jakarta.validation.constraints.Pattern(regexp = "[A-Z][A-Z0-9_]{1,29}") String channelCode,
            @Valid @RequestBody AdmChannelSaveRequest request,
            HttpServletRequest servletRequest) {
        String actor = requestUser(servletRequest, request.requestUser());
        CpfChannelPolicySnapshot before = channelPolicyService.snapshot();
        CpfChannelDefinition definition = new CpfChannelDefinition(
                channelCode, request.channelName(), request.channelType(), request.trustLevel(),
                request.clientChannel(), request.internalChannel(), request.authenticationRequired(),
                request.signatureRequired(), request.active(), request.description(), before.version());
        CpfChannelPolicySnapshot after = channelPolicyService.saveChannel(
                definition, actor, auditLogService.requireReason(request.reason()));
        audit(servletRequest, actor, "CHANNEL_SAVE", channelCode, request.reason(), before, after);
        return ResponseEntity.ok(after);
    }

    @PutMapping("/policies/{policyKey}")
    @CpfOnlineTransaction(id = "OADMCH0004", name = "ADMChannelExecutionPolicySave", ownerDomain="ADM")
    @Operation(operationId = "admChannelSaveExecutionPolicy", summary = "거래별 채널 정책 등록 또는 수정",
            description = "표준 실행 ID, 최초 채널, 호출 채널과 요청 유형 조합의 허용 정책을 저장합니다.")
    public ResponseEntity<CpfChannelPolicySnapshot> savePolicy(
            @PathVariable @jakarta.validation.constraints.Pattern(regexp = "[A-Z][A-Z0-9_.-]{2,99}") String policyKey,
            @Valid @RequestBody AdmChannelPolicySaveRequest request,
            HttpServletRequest servletRequest) {
        String actor = requestUser(servletRequest, request.requestUser());
        CpfChannelPolicySnapshot before = channelPolicyService.snapshot();
        CpfChannelExecutionPolicy policy = new CpfChannelExecutionPolicy(
                policyKey, request.standardExecutionId(), request.originalChannelCode(),
                request.callerChannelCode(), request.requestType(), request.allowed(),
                request.authenticationRequired(), request.signatureRequired(), request.maxTps(),
                request.effectiveFrom(), request.effectiveTo(), request.active(), before.version());
        CpfChannelPolicySnapshot after = channelPolicyService.savePolicy(
                policy, actor, auditLogService.requireReason(request.reason()));
        audit(servletRequest, actor, "CHANNEL_EXECUTION_POLICY_SAVE", policyKey, request.reason(), before, after);
        return ResponseEntity.ok(after);
    }

    @GetMapping("/package")
    @CpfOnlineTransaction(id = "OADMCH0005", name = "ADMChannelPolicyPackageExport", ownerDomain="ADM")
    @Operation(operationId = "admChannelExportPackage", summary = "채널 정책 패키지 반출",
            description = "환경 간 이동에 사용할 정렬된 채널 정책과 SHA-256 checksum을 반환합니다.")
    public ResponseEntity<CpfChannelPolicyPackage> exportPackage() {
        return ResponseEntity.ok(channelPolicyService.exportPackage());
    }

    @PostMapping("/package/import")
    @CpfOnlineTransaction(id = "OADMCH0006", name = "ADMChannelPolicyPackageImport", ownerDomain="ADM")
    @Operation(operationId = "admChannelImportPackage", summary = "채널 정책 패키지 반입",
            description = "schema와 checksum을 검증한 뒤 dry-run 또는 실제 반입을 수행합니다.")
    public ResponseEntity<CpfChannelPolicySnapshot> importPackage(
            @Valid @RequestBody AdmChannelPackageImportRequest request,
            HttpServletRequest servletRequest) {
        String actor = requestUser(servletRequest, request.requestUser());
        CpfChannelPolicySnapshot before = channelPolicyService.snapshot();
        CpfChannelPolicySnapshot after = channelPolicyService.importPackage(
                request.policyPackage(), request.dryRun(), actor,
                auditLogService.requireReason(request.reason()));
        audit(servletRequest, actor,
                request.dryRun() ? "CHANNEL_POLICY_IMPORT_DRY_RUN" : "CHANNEL_POLICY_IMPORT",
                request.policyPackage().checksumSha256(), request.reason(), before, after);
        return ResponseEntity.ok(after);
    }

    private void audit(
            HttpServletRequest request,
            String actor,
            String action,
            String targetId,
            String reason,
            Object before,
            Object after) {
        auditLogService.record(CpfContexts.transactionId(), actor, action,
                "cpf_channel_policy", targetId, reason,
                String.valueOf(before), String.valueOf(after),
                "snapshotVersion 변경", request.getRemoteAddr());
    }

    private String requestUser(HttpServletRequest request, String fallback) {
        return requireOperator(request);
    }
}
