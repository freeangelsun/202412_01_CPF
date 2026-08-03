package com.cpf.admin.approval.controller;

import com.cpf.admin.approval.service.AdmApprovalService;
import com.cpf.core.api.execution.CpfOnlineTransaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** ADM 위험조치 Approval Policy/Request/Execution API. */
@RestController
@RequestMapping("/adm/api/approvals")
@Tag(name="ADM-Approval",description="위험조치 정책·승인·Owner Command 실행·UNKNOWN 보존 API")
public class AdmApprovalController extends com.cpf.admin.common.base.AdmBaseController {
    private final AdmApprovalService service;
    public AdmApprovalController(AdmApprovalService service){this.service=service;}

    @GetMapping("/policies")
    @CpfOnlineTransaction(id="OADMAP0101",name="AdmApprovalPolicyList")
    @Operation(operationId="admApprovalPolicies",summary="위험조치 승인 정책 목록")
    public ResponseEntity<List<Map<String,Object>>> policies(@RequestParam(required=false)String actionType){
        return ResponseEntity.ok(service.findPolicies(actionType));
    }

    @GetMapping("/policies/{policyCode}/versions/{version}")
    @CpfOnlineTransaction(id="OADMAP0102",name="AdmApprovalPolicyDetail")
    @Operation(operationId="admApprovalPolicyDetail",summary="승인 정책/단계 상세")
    public ResponseEntity<Map<String,Object>> policy(@PathVariable String policyCode,@PathVariable int version){
        return ResponseEntity.ok(service.findPolicy(policyCode,version));
    }

    @PostMapping("/policies")
    @CpfOnlineTransaction(id="OADMAP0103",name="AdmApprovalPolicySave")
    @Operation(operationId="admApprovalPolicySave",summary="Versioned 위험조치 승인 정책 저장")
    public ResponseEntity<Map<String,Object>> savePolicy(@RequestBody AdmApprovalService.PolicyRequest request,
            @RequestAttribute("adm.operatorId") String operatorId){
        return ResponseEntity.ok(service.savePolicy(request,operatorId));
    }

    @PostMapping("/requests")
    @CpfOnlineTransaction(id="OADMAP0104",name="AdmApprovalRequest")
    @Operation(operationId="admApprovalRequest",summary="위험조치 승인 요청",
            description="동적 Target을 운영자 Snapshot으로 고정하고 요청 key와 payload hash로 승인 대상 변경을 방지합니다.")
    public ResponseEntity<Map<String,Object>> request(@RequestBody AdmApprovalService.CreateRequest request,
            @RequestAttribute("adm.operatorId") String operatorId){
        return ResponseEntity.ok(service.requestApproval(request,operatorId));
    }

    @GetMapping("/requests/{id}")
    @CpfOnlineTransaction(id="OADMAP0105",name="AdmApprovalRequestDetail")
    @Operation(operationId="admApprovalRequestDetail",summary="승인 요청/참여자/실행 상세")
    public ResponseEntity<Map<String,Object>> detail(@PathVariable long id){return ResponseEntity.ok(service.detail(id));}

    @PostMapping("/requests/{id}/decisions")
    @CpfOnlineTransaction(id="OADMAP0106",name="AdmApprovalDecision")
    @Operation(operationId="admApprovalDecision",summary="승인/반려 결정",
            description="Snapshot 참여자, ALL/ANY/N_OF_M, 자기승인 정책, 멱등키, optimistic version을 적용합니다.")
    public ResponseEntity<Map<String,Object>> decide(@PathVariable long id,
            @RequestBody AdmApprovalService.DecisionRequest request,
            @RequestAttribute("adm.operatorId") String operatorId){
        return ResponseEntity.ok(service.decide(id,request,operatorId));
    }

    @PostMapping("/requests/{id}/execute")
    @CpfOnlineTransaction(id="OADMAP0107",name="AdmApprovedOwnerCommand")
    @Operation(operationId="admApprovalExecute",summary="승인 완료 Owner Command 실행",
            description="ADM이 Owner DB를 직접 수정하지 않고 Command Port로 실행하며 UNKNOWN을 실패로 단정하지 않습니다.")
    public ResponseEntity<Map<String,Object>> execute(@PathVariable long id,@RequestParam String reason,
            @RequestAttribute("adm.operatorId") String operatorId){
        return ResponseEntity.ok(service.execute(id,reason,operatorId));
    }
}
