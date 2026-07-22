package com.cpf.admin.opr.controller;

import com.cpf.admin.opr.service.AdmCenterCutOperationService;
import com.cpf.core.common.execution.CpfOnlineTransaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    public AdmCenterCutController(AdmCenterCutOperationService centerCutOperationService) {
        this.centerCutOperationService = centerCutOperationService;
    }

    @GetMapping("/jobs")
    @CpfOnlineTransaction(id = "OADMCT0010", name = "ADMCenterCutJobList")
    @Operation(operationId = "admCenterCutFindJobs", summary = "Center-Cut Job 목록 조회", description = "등록된 Center-Cut Job 메타와 연결된 배치 Job 정보를 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findJobs() {
        return ResponseEntity.ok(centerCutOperationService.findJobs());
    }

    @GetMapping("/jobs/{centerCutJobId}")
    @CpfOnlineTransaction(id = "OADMCT0020", name = "ADMCenterCutJobDetail")
    @Operation(operationId = "admCenterCutFindJobDetail", summary = "Center-Cut Job 상세 조회", description = "Job 메타, 파라미터, 요약, target/result 일부를 함께 조회합니다.")
    public ResponseEntity<Map<String, Object>> findJobDetail(
            @Parameter(description = "Center-Cut Job ID", example = "CPF_REF_CENTER_CUT_SAMPLE_JOB")
            @PathVariable String centerCutJobId) {
        return ResponseEntity.ok(centerCutOperationService.findJobDetail(centerCutJobId));
    }

    @GetMapping("/jobs/{centerCutJobId}/parameters")
    @CpfOnlineTransaction(id = "OADMCT0030", name = "ADMCenterCutJobParameters")
    @Operation(operationId = "admCenterCutFindParameters", summary = "Center-Cut Job 파라미터 조회", description = "Center-Cut Job 실행에 사용하는 파라미터를 조회합니다. 암호화 값은 마스킹합니다.")
    public ResponseEntity<List<Map<String, Object>>> findParameters(
            @Parameter(description = "Center-Cut Job ID", example = "CPF_REF_CENTER_CUT_SAMPLE_JOB")
            @PathVariable String centerCutJobId) {
        return ResponseEntity.ok(centerCutOperationService.findParameters(centerCutJobId));
    }

    @GetMapping("/jobs/{centerCutJobId}/summary")
    @CpfOnlineTransaction(id = "OADMCT0040", name = "ADMCenterCutSummary")
    @Operation(operationId = "admCenterCutFindSummary", summary = "Center-Cut 처리 요약 조회", description = "대기, 처리중, 성공, 실패, 스킵, 재시도 요청, 중지 요청 건수를 조회합니다.")
    public ResponseEntity<Map<String, Object>> findSummary(
            @Parameter(description = "Center-Cut Job ID", example = "CPF_REF_CENTER_CUT_SAMPLE_JOB")
            @PathVariable String centerCutJobId) {
        return ResponseEntity.ok(centerCutOperationService.findSummary(centerCutJobId));
    }

    @GetMapping("/jobs/{centerCutJobId}/targets")
    @CpfOnlineTransaction(id = "OADMCT0050", name = "ADMCenterCutTargets")
    @Operation(operationId = "admCenterCutFindTargets", summary = "Center-Cut target 목록 조회", description = "업무 target 상태, 실패 사유, parent/child transactionGlobalId를 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findTargets(
            @Parameter(description = "Center-Cut Job ID", example = "CPF_REF_CENTER_CUT_SAMPLE_JOB")
            @PathVariable String centerCutJobId,
            @Parameter(description = "target 상태 필터", example = "FAILED")
            @RequestParam(required = false) String statusCode,
            @Parameter(description = "조회 건수 제한", example = "100")
            @RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(centerCutOperationService.findTargets(centerCutJobId, statusCode, limit));
    }

    @GetMapping("/jobs/{centerCutJobId}/results")
    @CpfOnlineTransaction(id = "OADMCT0060", name = "ADMCenterCutResults")
    @Operation(operationId = "admCenterCutFindResults", summary = "Center-Cut result 목록 조회", description = "업무 result 상태, 메시지, parent/child transactionGlobalId를 조회합니다. payload 원문은 마스킹합니다.")
    public ResponseEntity<List<Map<String, Object>>> findResults(
            @Parameter(description = "Center-Cut Job ID", example = "CPF_REF_CENTER_CUT_SAMPLE_JOB")
            @PathVariable String centerCutJobId,
            @Parameter(description = "result 상태 필터", example = "FAILED")
            @RequestParam(required = false) String resultStatus,
            @Parameter(description = "조회 건수 제한", example = "100")
            @RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(centerCutOperationService.findResults(centerCutJobId, resultStatus, limit));
    }

    @GetMapping("/results/{resultId}")
    @CpfOnlineTransaction(id = "OADMCT0070", name = "ADMCenterCutResultDetail")
    @Operation(operationId = "admCenterCutFindResultDetail", summary = "Center-Cut result 상세 조회", description = "result 단건 상세를 조회합니다. payload 원문은 응답하지 않습니다.")
    public ResponseEntity<Map<String, Object>> findResultDetail(
            @Parameter(description = "Center-Cut result ID", example = "1")
            @PathVariable String resultId) {
        return ResponseEntity.ok(centerCutOperationService.findResultDetail(resultId));
    }
}
