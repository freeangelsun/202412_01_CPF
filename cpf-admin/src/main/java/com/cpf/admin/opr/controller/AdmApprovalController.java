package com.cpf.admin.opr.controller;

import com.cpf.admin.opr.context.AdmAuthenticatedOperatorContext;
import com.cpf.admin.opr.service.AdmApprovalEngineService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @deprecated V9 canonical REST owner is {@code com.cpf.admin.approval.controller.AdmApprovalController}.
 * This source remains only as a binary/source compatibility facade and is intentionally not a Spring bean,
 * preventing duplicate {@code /adm/api/approvals/**} mappings and duplicate OpenAPI operationIds.
 */
@Deprecated(forRemoval = true, since = "V9")
public class AdmApprovalController {
    private final AdmApprovalEngineService approvals; private final AdmAuthenticatedOperatorContext operators;
    public AdmApprovalController(AdmApprovalEngineService approvals,AdmAuthenticatedOperatorContext operators){this.approvals=approvals;this.operators=operators;}

    @GetMapping("/policies")@Operation(operationId="admLegacyApprovalPolicies",summary="승인 정책 목록")
    public ResponseEntity<List<Map<String,Object>>> policies(@RequestParam(required=false)String actionType){return ResponseEntity.ok(approvals.policies(actionType));}
    @GetMapping("/policies/{policyCode}/versions/{version}")@Operation(operationId="admLegacyApprovalPolicyDetail",summary="승인 정책 상세")
    public ResponseEntity<Map<String,Object>> policy(@PathVariable String policyCode,@PathVariable int version){return ResponseEntity.ok(approvals.policy(policyCode,version));}
    @PostMapping("/policies")@Operation(operationId="admLegacyApprovalPolicySave",summary="승인 정책 Version 저장")
    public ResponseEntity<Map<String,Object>> save(@RequestBody AdmApprovalEngineService.PolicyCommand body){return ResponseEntity.ok(approvals.savePolicy(body,operators.currentOperatorId()));}
    @PostMapping("/requests")@Operation(operationId="admLegacyApprovalRequest",summary="승인 요청 생성")
    public ResponseEntity<Map<String,Object>> request(@RequestBody AdmApprovalEngineService.RequestCommand body){return ResponseEntity.ok(approvals.createRequest(body,operators.currentOperatorId()));}
    @GetMapping("/requests/{id}")@Operation(operationId="admLegacyApprovalRequestDetail",summary="승인 요청 상세")
    public ResponseEntity<Map<String,Object>> request(@PathVariable long id){return ResponseEntity.ok(approvals.request(id));}
    @PostMapping("/requests/{id}/decisions")@Operation(operationId="admLegacyApprovalDecision",summary="승인/반려 결정")
    public ResponseEntity<Map<String,Object>> decide(@PathVariable long id,@RequestBody AdmApprovalEngineService.DecisionCommand body){return ResponseEntity.ok(approvals.decide(id,body,operators.currentOperatorId()));}
    @PostMapping("/requests/{id}/execute")@Operation(operationId="admLegacyApprovalExecute",summary="승인 Owner Command 실행")
    public ResponseEntity<?> execute(@PathVariable long id){return ResponseEntity.ok(approvals.execute(id,operators.currentOperatorId()));}
}
