package com.cpf.admin.opr.controller;

import com.cpf.admin.opr.dto.AdmCenterCutActionRequest;
import com.cpf.admin.opr.service.AdmAuditLogService;
import com.cpf.admin.opr.service.AdmCenterCutOperationService;
import com.cpf.core.api.context.CpfContexts;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * ADM Center-Cut 운영 관제 API입니다.
 *
 * <p>CPF/BAT Center-Cut 메타와 업무 DB adapter 처리 상태를 조회합니다. 이 API는 운영 조회 전용이며
 * 업무 target/result를 직접 재처리하거나 변경하지 않습니다.</p>
 */
@RestController
@RequestMapping("/adm/api/center-cut")
@Tag(name = "ADM-CenterCut", description = "Center-Cut job, target, result 운영 관제 API")
public class AdmCenterCutController extends com.cpf.admin.common.base.AdmBaseController {
    private final AdmCenterCutOperationService centerCutOperationService;
    private final AdmAuditLogService auditLogService;

    public AdmCenterCutController(
            AdmCenterCutOperationService centerCutOperationService,
            AdmAuditLogService auditLogService) {
        this.centerCutOperationService = centerCutOperationService;
        this.auditLogService = auditLogService;
    }

    @GetMapping("/jobs")    @Operation(operationId = "admCenterCutFindJobs", summary = "Center-Cut Job 목록 조회", description = "등록된 Center-Cut Job 메타와 연결된 배치 Job 정보를 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findJobs() {
        return ResponseEntity.ok(centerCutOperationService.findJobs());
    }

    @GetMapping("/jobs/{centerCutJobId}")    @Operation(operationId = "admCenterCutFindJobDetail", summary = "Center-Cut Job 상세 조회", description = "Job 메타, 파라미터, 요약, target/result 일부를 함께 조회합니다.")
    public ResponseEntity<Map<String, Object>> findJobDetail(
            @Parameter(description = "Center-Cut Job ID", example = "CPF_CENTER_CUT_JOB")
            @PathVariable String centerCutJobId) {
        return ResponseEntity.ok(centerCutOperationService.findJobDetail(centerCutJobId));
    }

    @GetMapping("/jobs/{centerCutJobId}/parameters")    @Operation(operationId = "admCenterCutFindParameters", summary = "Center-Cut Job 파라미터 조회", description = "Center-Cut Job 실행에 사용하는 파라미터를 조회합니다. 암호화 값은 마스킹합니다.")
    public ResponseEntity<List<Map<String, Object>>> findParameters(
            @Parameter(description = "Center-Cut Job ID", example = "CPF_CENTER_CUT_JOB")
            @PathVariable String centerCutJobId) {
        return ResponseEntity.ok(centerCutOperationService.findParameters(centerCutJobId));
    }

    @GetMapping("/jobs/{centerCutJobId}/summary")    @Operation(operationId = "admCenterCutFindSummary", summary = "Center-Cut 처리 요약 조회", description = "대기, 처리중, 성공, 실패, 스킵, 재시도 요청, 중지 요청 건수를 조회합니다.")
    public ResponseEntity<Map<String, Object>> findSummary(
            @Parameter(description = "Center-Cut Job ID", example = "CPF_CENTER_CUT_JOB")
            @PathVariable String centerCutJobId) {
        return ResponseEntity.ok(centerCutOperationService.findSummary(centerCutJobId));
    }

    @GetMapping("/jobs/{centerCutJobId}/targets")    @Operation(operationId = "admCenterCutFindTargets", summary = "Center-Cut target 목록 조회", description = "업무 target 상태, 실패 사유, parent/child transactionId를 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findTargets(
            @Parameter(description = "Center-Cut Job ID", example = "CPF_CENTER_CUT_JOB")
            @PathVariable String centerCutJobId,
            @Parameter(description = "target 상태 필터", example = "FAILED")
            @RequestParam(required = false) String statusCode,
            @Parameter(description = "조회 건수 제한", example = "100")
            @RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(centerCutOperationService.findTargets(centerCutJobId, statusCode, limit));
    }

    @GetMapping("/jobs/{centerCutJobId}/results")    @Operation(operationId = "admCenterCutFindResults", summary = "Center-Cut result 목록 조회", description = "업무 result 상태, 메시지, parent/child transactionId를 조회합니다. payload 원문은 마스킹합니다.")
    public ResponseEntity<List<Map<String, Object>>> findResults(
            @Parameter(description = "Center-Cut Job ID", example = "CPF_CENTER_CUT_JOB")
            @PathVariable String centerCutJobId,
            @Parameter(description = "result 상태 필터", example = "FAILED")
            @RequestParam(required = false) String resultStatus,
            @Parameter(description = "조회 건수 제한", example = "100")
            @RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(centerCutOperationService.findResults(centerCutJobId, resultStatus, limit));
    }

    @GetMapping("/results/{resultId}")    @Operation(operationId = "admCenterCutFindResultDetail", summary = "Center-Cut result 상세 조회", description = "result 단건 상세를 조회합니다. payload 원문은 응답하지 않습니다.")
    public ResponseEntity<Map<String, Object>> findResultDetail(
            @Parameter(description = "Center-Cut result ID", example = "1")
            @PathVariable String resultId) {
        return ResponseEntity.ok(centerCutOperationService.findResultDetail(resultId));
    }

    @PostMapping("/executions/{executionId}/reprocess-failed")    @Operation(
            operationId = "admCenterCutReprocessFailedExecution",
            summary = "Center-Cut 실패 실행 재처리",
            description = "별도 승인과 멱등 키를 검증한 뒤 executionId 범위의 실패 Item만 재처리합니다.")
    public ResponseEntity<Map<String, Object>> reprocessFailedExecution(
            @PathVariable String executionId,
            @RequestBody AdmCenterCutActionRequest request,
            HttpServletRequest servletRequest) {
        String operator = requireOperator(servletRequest);
        String reason = auditLogService.requireReason(request.reason());
        long approvalRequestId = parseApprovalRequestId(request.approvalRequestId());
        Map<String, Object> result = auditLogService.executeAudited(
                CpfContexts.transactionId(), operator, "CENTER_CUT_REPROCESS_FAILED",
                "center_cut_execution", executionId, reason, null, servletRequest.getRemoteAddr(),
                () -> centerCutOperationService.reprocessFailed(
                        executionId, approvalRequestId, request.idempotencyKey(), reason, operator),
                String::valueOf);
        return ResponseEntity.accepted().body(result);
    }

    @PostMapping("/executions/{executionId}/reconcile-unknown")    @Operation(
            operationId = "admCenterCutReconcileUnknownExecution",
            summary = "Center-Cut 결과불명 실행 대사",
            description = "별도 승인과 멱등 키를 검증한 뒤 executionId 범위의 UNKNOWN Item만 재대사합니다.")
    public ResponseEntity<Map<String, Object>> reconcileUnknownExecution(
            @PathVariable String executionId,
            @RequestBody AdmCenterCutActionRequest request,
            HttpServletRequest servletRequest) {
        String operator = requireOperator(servletRequest);
        String reason = auditLogService.requireReason(request.reason());
        long approvalRequestId = parseApprovalRequestId(request.approvalRequestId());
        Map<String, Object> result = auditLogService.executeAudited(
                CpfContexts.transactionId(), operator, "CENTER_CUT_RECONCILE_UNKNOWN",
                "center_cut_execution", executionId, reason, null, servletRequest.getRemoteAddr(),
                () -> centerCutOperationService.reconcileUnknown(
                        executionId, approvalRequestId, request.idempotencyKey(), reason, operator),
                String::valueOf);
        return ResponseEntity.accepted().body(result);
    }


    private static long parseApprovalRequestId(String value) {
        try {
            long parsed = Long.parseLong(value);
            if (parsed <= 0) throw new NumberFormatException("non-positive");
            return parsed;
        } catch (RuntimeException invalid) {
            throw new com.cpf.core.api.error.CpfValidationException(
                    "approvalRequestId는 양수 숫자여야 합니다.");
        }
    }

}
