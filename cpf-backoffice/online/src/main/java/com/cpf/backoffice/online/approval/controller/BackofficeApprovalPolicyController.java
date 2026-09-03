package com.cpf.backoffice.online.approval.controller;

import com.cpf.web.api.CpfController;
import com.cpf.foundation.execution.api.CpfOnlineTransaction;

import com.cpf.backoffice.online.approval.service.BackofficeApprovalPolicyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/** MBW Versioned Approval Policy/Simulation/Participant API. */
@CpfController
@RequestMapping("/api/v1/backoffice/approvals")
@Tag(name = "MBW-Approval-Policy", description = "Versioned 정책, Target 해석, 위임, Snapshot 결재 API")
public class BackofficeApprovalPolicyController extends com.cpf.backoffice.online.base.BackofficeBaseController {
    private final BackofficeApprovalPolicyService service;

    public BackofficeApprovalPolicyController(BackofficeApprovalPolicyService service) { this.service = service; }

    @GetMapping("/policies")    @Operation(operationId = "MBW_APPROVAL_POLICIES", summary = "결재 정책 목록")
    @CpfOnlineTransaction(operationId = "MBW_APPROVAL_POLICIES", name = "결재 정책 목록", description = "결재 정책 목록 업무 거래를 CPF 표준 계약에 따라 처리한다.")
    public ResponseEntity<List<Map<String,Object>>> policies(
            @RequestParam(required = false) String businessDomain,
            @RequestParam(required = false) String approvalType) {
        return ResponseEntity.ok(service.findPolicies(businessDomain, approvalType));
    }

    @GetMapping("/policies/{policyCode}/{version}")    @Operation(operationId = "MBW_APPROVAL_POLICY_DETAIL", summary = "결재 정책/단계 상세")
    @CpfOnlineTransaction(operationId = "MBW_APPROVAL_POLICY_DETAIL", name = "결재 정책/단계 상세", description = "결재 정책/단계 상세 업무 거래를 CPF 표준 계약에 따라 처리한다.")
    public ResponseEntity<Map<String,Object>> policy(@PathVariable String policyCode, @PathVariable int version) {
        return ResponseEntity.ok(service.findPolicy(policyCode, version));
    }

    @PostMapping("/policies")    @Operation(operationId = "MBW_APPROVAL_POLICY_SAVE", summary = "Versioned 결재 정책 저장",
            description = "EMPLOYEE/ROLE/ORGANIZATION/ORG_MANAGER/POSITION Target과 ALL/ANY/N_OF_M 규칙을 저장합니다.")
    @CpfOnlineTransaction(operationId = "MBW_APPROVAL_POLICY_SAVE", name = "Versioned 결재 정책 저장", description = "Versioned 결재 정책 저장 업무 거래를 CPF 표준 계약에 따라 처리한다.")
    @ApiResponse(responseCode = "200", description = "처리 성공")
    @ApiResponse(responseCode = "422", description = "Validation failed")
    public ResponseEntity<Map<String,Object>> savePolicy(
            @RequestBody BackofficeApprovalPolicyService.PolicyRequest request,
            @RequestAttribute("backoffice.operatorId") String operatorId) {
        return ResponseEntity.ok(service.savePolicy(request, operatorId));
    }

    @PostMapping("/simulate")    @Operation(operationId = "MBW_APPROVAL_POLICY_SIMULATE", summary = "결재 정책 참여자 Simulation",
            description = "상신 전에 유효 조직/Role/직급/책임자/위임을 해석하고 fail-closed 결과를 반환합니다.")
    @CpfOnlineTransaction(operationId = "MBW_APPROVAL_POLICY_SIMULATE", name = "결재 정책 참여자 Simulation", description = "결재 정책 참여자 Simulation 업무 거래를 CPF 표준 계약에 따라 처리한다.")
    @ApiResponse(responseCode = "200", description = "처리 성공")
    @ApiResponse(responseCode = "422", description = "Validation failed")
    public ResponseEntity<Map<String,Object>> simulate(
            @RequestBody SimulationRequest request) {
        return ResponseEntity.ok(service.simulate(
                request.policyCode(), request.policyVersion(), request.businessDomain(), request.approvalType(),
                request.requesterEmployeeNo(), request.effectiveAt()));
    }

    @GetMapping("/delegations")    @Operation(operationId = "MBW_APPROVAL_DELEGATIONS", summary = "결재 위임 조회")
    @CpfOnlineTransaction(operationId = "MBW_APPROVAL_DELEGATIONS", name = "결재 위임 조회", description = "결재 위임 조회 업무 거래를 CPF 표준 계약에 따라 처리한다.")
    public ResponseEntity<List<Map<String,Object>>> delegations(
            @RequestParam(required = false) String employeeNo,
            @RequestParam(required = false) Instant effectiveAt) {
        return ResponseEntity.ok(service.findDelegations(employeeNo, effectiveAt));
    }

    @PostMapping("/delegations")    @Operation(operationId = "MBW_APPROVAL_DELEGATION_SAVE", summary = "결재 위임/대결 유효기간 저장")
    @CpfOnlineTransaction(operationId = "MBW_APPROVAL_DELEGATION_SAVE", name = "결재 위임/대결 유효기간 저장", description = "결재 위임/대결 유효기간 저장 업무 거래를 CPF 표준 계약에 따라 처리한다.")
    @ApiResponse(responseCode = "200", description = "처리 성공")
    @ApiResponse(responseCode = "422", description = "Validation failed")
    public ResponseEntity<Map<String,Object>> saveDelegation(
            @RequestBody BackofficeApprovalPolicyService.DelegationRequest request,
            @RequestAttribute("backoffice.operatorId") String operatorId) {
        return ResponseEntity.ok(service.saveDelegation(request, operatorId));
    }

    @PostMapping("/submissions")    @Operation(operationId = "MBW_APPROVAL_POLICY_SUBMIT", summary = "정책 기반 결재 상신",
            description = "Policy/참여자/요청자 조직정보를 Snapshot하고 상신 멱등 키와 payload hash를 고정합니다.")
    @CpfOnlineTransaction(operationId = "MBW_APPROVAL_POLICY_SUBMIT", name = "정책 기반 결재 상신", description = "정책 기반 결재 상신 업무 거래를 CPF 표준 계약에 따라 처리한다.")
    @ApiResponse(responseCode = "200", description = "처리 성공")
    @ApiResponse(responseCode = "422", description = "Validation failed")
    public ResponseEntity<Map<String,Object>> submit(
            @RequestBody BackofficeApprovalPolicyService.SubmitRequest request,
            @RequestAttribute("backoffice.operatorId") String operatorId) {
        return ResponseEntity.ok(service.submit(request, operatorId));
    }

    @GetMapping("/submissions")    @Operation(operationId = "MBW_APPROVAL_SUBMISSIONS", summary = "인증 사용자의 상신 문서 목록")
    @CpfOnlineTransaction(operationId = "MBW_APPROVAL_SUBMISSIONS", name = "인증 사용자의 상신 문서 목록", description = "인증 사용자의 상신 문서 목록 업무 거래를 CPF 표준 계약에 따라 처리한다.")
    public ResponseEntity<List<Map<String,Object>>> submissions(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "100") int limit,
            @RequestAttribute("backoffice.operatorId") String operatorId) {
        return ResponseEntity.ok(service.findSubmissions(operatorId, status, limit));
    }

    @GetMapping("/inbox")    @Operation(operationId = "MBW_APPROVAL_INBOX", summary = "인증 사용자의 결재 참여 문서 목록")
    @CpfOnlineTransaction(operationId = "MBW_APPROVAL_INBOX", name = "인증 사용자의 결재 참여 문서 목록", description = "인증 사용자의 결재 참여 문서 목록 업무 거래를 CPF 표준 계약에 따라 처리한다.")
    public ResponseEntity<List<Map<String,Object>>> inbox(
            @RequestParam(required = false) String decisionStatus,
            @RequestParam(defaultValue = "100") int limit,
            @RequestAttribute("backoffice.operatorId") String operatorId) {
        return ResponseEntity.ok(service.findInbox(operatorId, decisionStatus, limit));
    }

    @GetMapping("/submissions/{approvalId}")    @Operation(operationId = "MBW_APPROVAL_SUBMISSION_DETAIL", summary = "정책 기반 결재/참여자 상세")
    @CpfOnlineTransaction(operationId = "MBW_APPROVAL_SUBMISSION_DETAIL", name = "정책 기반 결재/참여자 상세", description = "정책 기반 결재/참여자 상세 업무 거래를 CPF 표준 계약에 따라 처리한다.")
    public ResponseEntity<Map<String,Object>> submission(@PathVariable long approvalId) {
        return ResponseEntity.ok(service.detail(approvalId));
    }

    @PostMapping("/{approvalId}/decisions")    @Operation(operationId = "MBW_APPROVAL_PARTICIPANT_DECISION", summary = "결재 참여자 결정",
            description = "Snapshot 참여자만 결정할 수 있으며 ALL/ANY/N_OF_M, 순차/병렬, 멱등성, 낙관적 잠금을 적용합니다.")
    @CpfOnlineTransaction(operationId = "MBW_APPROVAL_PARTICIPANT_DECISION", name = "결재 참여자 결정", description = "결재 참여자 결정 업무 거래를 CPF 표준 계약에 따라 처리한다.")
    @ApiResponse(responseCode = "200", description = "처리 성공")
    @ApiResponse(responseCode = "422", description = "Validation failed")
    public ResponseEntity<Map<String,Object>> decide(
            @PathVariable long approvalId,
            @RequestBody BackofficeApprovalPolicyService.DecisionRequest request,
            @RequestAttribute("backoffice.operatorId") String operatorId) {
        return ResponseEntity.ok(service.decide(approvalId, request, operatorId));
    }

    @PostMapping("/submissions/{approvalId}/withdraw")    @Operation(operationId = "MBW_APPROVAL_WITHDRAW", summary = "결재 철회")
    @CpfOnlineTransaction(operationId = "MBW_APPROVAL_WITHDRAW", name = "결재 철회", description = "결재 철회 업무 거래를 CPF 표준 계약에 따라 처리한다.")
    @ApiResponse(responseCode = "200", description = "처리 성공")
    @ApiResponse(responseCode = "422", description = "Validation failed")
    public ResponseEntity<Map<String,Object>> withdraw(
            @PathVariable long approvalId,
            @RequestBody BackofficeApprovalPolicyService.LifecycleRequest request,
            @RequestAttribute("backoffice.operatorId") String operatorId) {
        return ResponseEntity.ok(service.withdraw(approvalId, request, operatorId));
    }

    @PostMapping("/submissions/{approvalId}/cancel")    @Operation(operationId = "MBW_APPROVAL_CANCEL", summary = "결재 취소")
    @CpfOnlineTransaction(operationId = "MBW_APPROVAL_CANCEL", name = "결재 취소", description = "결재 취소 업무 거래를 CPF 표준 계약에 따라 처리한다.")
    @ApiResponse(responseCode = "200", description = "처리 성공")
    @ApiResponse(responseCode = "422", description = "Validation failed")
    public ResponseEntity<Map<String,Object>> cancel(
            @PathVariable long approvalId,
            @RequestBody BackofficeApprovalPolicyService.LifecycleRequest request,
            @RequestAttribute("backoffice.operatorId") String operatorId) {
        return ResponseEntity.ok(service.cancel(approvalId, request, operatorId));
    }

    @PostMapping("/submissions/{approvalId}/resubmit")    @Operation(operationId = "MBW_APPROVAL_RESUBMIT", summary = "결재 재상신",
            description = "기존 Snapshot을 재활성화하지 않고 새로운 정책/참여자 Snapshot의 새 문서를 생성합니다.")
    @CpfOnlineTransaction(operationId = "MBW_APPROVAL_RESUBMIT", name = "결재 재상신", description = "결재 재상신 업무 거래를 CPF 표준 계약에 따라 처리한다.")
    @ApiResponse(responseCode = "200", description = "처리 성공")
    @ApiResponse(responseCode = "422", description = "Validation failed")
    public ResponseEntity<Map<String,Object>> resubmit(
            @PathVariable long approvalId,
            @RequestBody BackofficeApprovalPolicyService.SubmitRequest request,
            @RequestAttribute("backoffice.operatorId") String operatorId) {
        return ResponseEntity.ok(service.resubmit(approvalId, request, operatorId));
    }

    @PostMapping("/submissions/expire-due")    @Operation(operationId = "MBW_APPROVAL_EXPIRE_DUE", summary = "기한 경과 결재 만료 처리",
            description = "BAT Scheduler 등 외부 실행 Owner가 호출할 수 있는 멱등 만료 처리 API입니다.")
    @CpfOnlineTransaction(operationId = "MBW_APPROVAL_EXPIRE_DUE", name = "기한 경과 결재 만료 처리", description = "기한 경과 결재 만료 처리 업무 거래를 CPF 표준 계약에 따라 처리한다.")
    @ApiResponse(responseCode = "200", description = "처리 성공")
    @ApiResponse(responseCode = "422", description = "Validation failed")
    public ResponseEntity<Map<String,Object>> expireDue(
            @RequestParam(defaultValue = "100") int limit,
            @RequestAttribute("backoffice.operatorId") String operatorId) {
        List<Long> expired = service.expireDue(Instant.now(), limit, operatorId);
        return ResponseEntity.ok(Map.of("expiredCount", expired.size(), "approvalIds", expired));
    }

    public record SimulationRequest(String policyCode, Integer policyVersion, String businessDomain,
                                    String approvalType, String requesterEmployeeNo, Instant effectiveAt) {}
}
