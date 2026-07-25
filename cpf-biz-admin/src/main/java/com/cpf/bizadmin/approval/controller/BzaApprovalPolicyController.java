package com.cpf.bizadmin.approval.controller;

import com.cpf.bizadmin.approval.service.BzaApprovalPolicyService;
import com.cpf.core.common.execution.CpfOnlineTransaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/** BZA Versioned Approval Policy/Simulation/Participant API. */
@RestController
@RequestMapping("/api/bza/approval")
@Tag(name = "BZA-Approval-Policy", description = "Versioned 정책, Target 해석, 위임, Snapshot 결재 API")
public class BzaApprovalPolicyController extends com.cpf.bizadmin.common.base.BzaBaseController {
    private final BzaApprovalPolicyService service;

    public BzaApprovalPolicyController(BzaApprovalPolicyService service) { this.service = service; }

    @GetMapping("/policies")
    @CpfOnlineTransaction(id = "OBZAAP0101", name = "BzaApprovalPolicyList")
    @Operation(operationId = "bzaApprovalPolicies", summary = "결재 정책 목록")
    public ResponseEntity<List<Map<String,Object>>> policies(
            @RequestParam(required = false) String businessDomain,
            @RequestParam(required = false) String approvalType) {
        return ResponseEntity.ok(service.findPolicies(businessDomain, approvalType));
    }

    @GetMapping("/policies/{policyCode}/{version}")
    @CpfOnlineTransaction(id = "OBZAAP0102", name = "BzaApprovalPolicyDetail")
    @Operation(operationId = "bzaApprovalPolicyDetail", summary = "결재 정책/단계 상세")
    public ResponseEntity<Map<String,Object>> policy(@PathVariable String policyCode, @PathVariable int version) {
        return ResponseEntity.ok(service.findPolicy(policyCode, version));
    }

    @PostMapping("/policies")
    @CpfOnlineTransaction(id = "OBZAAP0103", name = "BzaApprovalPolicySave")
    @Operation(operationId = "bzaApprovalPolicySave", summary = "Versioned 결재 정책 저장",
            description = "EMPLOYEE/ROLE/ORGANIZATION/ORG_MANAGER/POSITION Target과 ALL/ANY/N_OF_M 규칙을 저장합니다.")
    public ResponseEntity<Map<String,Object>> savePolicy(
            @RequestBody BzaApprovalPolicyService.PolicyRequest request,
            @RequestAttribute("bza.operatorId") String operatorId) {
        return ResponseEntity.ok(service.savePolicy(request, operatorId));
    }

    @PostMapping("/simulate")
    @CpfOnlineTransaction(id = "OBZAAP0104", name = "BzaApprovalPolicySimulation")
    @Operation(operationId = "bzaApprovalPolicySimulate", summary = "결재 정책 참여자 Simulation",
            description = "상신 전에 유효 조직/Role/직급/책임자/위임을 해석하고 fail-closed 결과를 반환합니다.")
    public ResponseEntity<Map<String,Object>> simulate(
            @RequestBody SimulationRequest request) {
        return ResponseEntity.ok(service.simulate(
                request.policyCode(), request.policyVersion(), request.businessDomain(), request.approvalType(),
                request.requesterEmployeeNo(), request.effectiveAt()));
    }

    @GetMapping("/delegations")
    @CpfOnlineTransaction(id = "OBZAAP0105", name = "BzaApprovalDelegationList")
    @Operation(operationId = "bzaApprovalDelegations", summary = "결재 위임 조회")
    public ResponseEntity<List<Map<String,Object>>> delegations(
            @RequestParam(required = false) String employeeNo,
            @RequestParam(required = false) Instant effectiveAt) {
        return ResponseEntity.ok(service.findDelegations(employeeNo, effectiveAt));
    }

    @PostMapping("/delegations")
    @CpfOnlineTransaction(id = "OBZAAP0106", name = "BzaApprovalDelegationSave")
    @Operation(operationId = "bzaApprovalDelegationSave", summary = "결재 위임/대결 유효기간 저장")
    public ResponseEntity<Map<String,Object>> saveDelegation(
            @RequestBody BzaApprovalPolicyService.DelegationRequest request,
            @RequestAttribute("bza.operatorId") String operatorId) {
        return ResponseEntity.ok(service.saveDelegation(request, operatorId));
    }

    @PostMapping("/submissions")
    @CpfOnlineTransaction(id = "OBZAAP0107", name = "BzaApprovalPolicySubmit")
    @Operation(operationId = "bzaApprovalPolicySubmit", summary = "정책 기반 결재 상신",
            description = "Policy/참여자/요청자 조직정보를 Snapshot하고 상신 멱등 키와 payload hash를 고정합니다.")
    public ResponseEntity<Map<String,Object>> submit(
            @RequestBody BzaApprovalPolicyService.SubmitRequest request,
            @RequestAttribute("bza.operatorId") String operatorId) {
        return ResponseEntity.ok(service.submit(request, operatorId));
    }

    @GetMapping("/submissions/{approvalId}")
    @CpfOnlineTransaction(id = "OBZAAP0108", name = "BzaApprovalPolicyDetail")
    @Operation(operationId = "bzaApprovalSubmissionDetail", summary = "정책 기반 결재/참여자 상세")
    public ResponseEntity<Map<String,Object>> submission(@PathVariable long approvalId) {
        return ResponseEntity.ok(service.detail(approvalId));
    }

    @PostMapping("/submissions/{approvalId}/decisions")
    @CpfOnlineTransaction(id = "OBZAAP0109", name = "BzaApprovalPolicyDecision")
    @Operation(operationId = "bzaApprovalParticipantDecision", summary = "결재 참여자 결정",
            description = "Snapshot 참여자만 결정할 수 있으며 ALL/ANY/N_OF_M, 순차/병렬, 멱등성, 낙관적 잠금을 적용합니다.")
    public ResponseEntity<Map<String,Object>> decide(
            @PathVariable long approvalId,
            @RequestBody BzaApprovalPolicyService.DecisionRequest request,
            @RequestAttribute("bza.operatorId") String operatorId) {
        return ResponseEntity.ok(service.decide(approvalId, request, operatorId));
    }

    public record SimulationRequest(String policyCode, Integer policyVersion, String businessDomain,
                                    String approvalType, String requesterEmployeeNo, Instant effectiveAt) {}
}
