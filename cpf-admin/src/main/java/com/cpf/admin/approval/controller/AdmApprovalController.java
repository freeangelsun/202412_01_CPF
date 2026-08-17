package com.cpf.admin.approval.controller;

import org.springframework.web.bind.annotation.RestController;
import com.cpf.admin.approval.service.AdmApprovalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;

/** ADM 위험조치 Approval Policy/Request/Execution API. */
@RestController
@RequestMapping("/adm/api/approvals")
@Tag(name="ADM-Approval",description="위험조치 정책·승인·Owner Command 실행·UNKNOWN Reconcile API")
@SecurityRequirement(name = "admSessionCookie")
@Validated
public class AdmApprovalController extends com.cpf.admin.common.base.AdmBaseController {
    private final AdmApprovalService service;
    public AdmApprovalController(AdmApprovalService service){this.service=service;}

    @GetMapping("/policies")    @Operation(operationId="admApprovalPolicies",summary="위험조치 승인 정책 목록")
    public ResponseEntity<List<Map<String,Object>>> policies(@RequestParam(required=false)String actionType){
        return ResponseEntity.ok(service.findPolicies(actionType));
    }

    @GetMapping("/policies/{policyCode}/versions/{version}")    @Operation(operationId="admApprovalPolicyDetail",summary="승인 정책/단계 상세")
    public ResponseEntity<Map<String,Object>> policy(@PathVariable String policyCode,@PathVariable int version){
        return ResponseEntity.ok(service.findPolicy(policyCode,version));
    }

    @PostMapping("/policies")    @Operation(operationId="admApprovalPolicySave",summary="Versioned 위험조치 승인 정책 저장")
    @ApiResponse(responseCode = "422", description = "Validation failed")
    public ResponseEntity<Map<String,Object>> savePolicy(@Valid @RequestBody AdmApprovalService.PolicyRequest request,
            @RequestAttribute("adm.operatorId") String operatorId){
        return ResponseEntity.status(HttpStatus.CREATED).body(service.savePolicy(request,operatorId));
    }

    @PostMapping("/requests")    @Operation(operationId="admApprovalRequest",summary="위험조치 승인 요청",
            description="동적 Target을 운영자 Snapshot으로 고정하고 요청 key와 payload hash로 승인 대상 변경을 방지합니다.")
    @ApiResponse(responseCode = "422", description = "Validation failed")
    public ResponseEntity<Map<String,Object>> request(@Valid @RequestBody AdmApprovalService.CreateRequest request,
            @RequestAttribute("adm.operatorId") String operatorId){
        AdmApprovalService.ApprovalMutationResult result=service.requestApprovalResult(request,operatorId);
        return ResponseEntity.status(result.created()?HttpStatus.CREATED:HttpStatus.OK).body(result.body());
    }

    @GetMapping("/requests/{id}")    @Operation(operationId="admApprovalRequestDetail",summary="승인 요청/참여자/실행 상세")
    public ResponseEntity<Map<String,Object>> detail(@PathVariable long id){return ResponseEntity.ok(service.detail(id));}

    @PostMapping("/requests/{id}/decisions")    @Operation(operationId="admApprovalDecision",summary="승인/반려 결정",
            description="Snapshot 참여자, ALL/ANY/N_OF_M, 자기승인 정책, 멱등키, optimistic version을 적용합니다.")
    @ApiResponse(responseCode = "422", description = "Validation failed")
    public ResponseEntity<Map<String,Object>> decide(@PathVariable long id,
            @Valid @RequestBody AdmApprovalService.DecisionRequest request,
            @RequestAttribute("adm.operatorId") String operatorId){
        return ResponseEntity.ok(service.decide(id,request,operatorId));
    }

    @PostMapping("/requests/{id}/reconcile")    @Operation(operationId="admApprovalReconcile",summary="UNKNOWN 승인 실행 상태 Reconcile",
            description="Owner 상태를 조회해 Side Effect를 확정하며 Mutation을 자동 재실행하지 않습니다.")
    @ApiResponse(responseCode = "422", description = "Validation failed")
    public ResponseEntity<Map<String,Object>> reconcile(@PathVariable long id,@RequestParam @Size(min=8,max=500) String reason,
            @RequestAttribute("adm.operatorId") String operatorId){
        return ResponseEntity.ok(service.reconcile(id,reason,operatorId));
    }

    @PostMapping("/requests/{id}/execute")    @Operation(operationId="admApprovalExecute",summary="승인 완료 Owner Command 실행",
            description="ADM이 Owner DB를 직접 수정하지 않고 Command Port로 실행하며 UNKNOWN을 실패로 단정하지 않습니다.")
    @ApiResponse(responseCode = "422", description = "Validation failed")
    @ApiResponse(responseCode = "503", description = "Owner command unavailable")
    public ResponseEntity<Map<String,Object>> execute(@PathVariable long id,@RequestParam @Size(min=8,max=500) String reason,
            @RequestAttribute("adm.operatorId") String operatorId){
        return ResponseEntity.ok(service.execute(id,reason,operatorId));
    }
}
