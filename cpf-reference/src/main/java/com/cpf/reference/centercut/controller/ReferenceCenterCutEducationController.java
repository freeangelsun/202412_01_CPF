package com.cpf.reference.centercut.controller;

import com.cpf.core.common.execution.CpfOnlineTransaction;
import com.cpf.reference.centercut.dto.ReferenceCenterCutExecutionResponse;
import com.cpf.reference.centercut.application.ReferenceCenterCutEducationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 업무 DB 기반 center-cut adapter 교육 API입니다.
 */
@RestController
@RequestMapping({"/api/reference/center-cut", "/reference/edu/center-cut"})
@Tag(name = "REF Reference 14. Center-Cut", description = "업무 DB 기반 center-cut target/provider/handler/result adapter 샘플")
public class ReferenceCenterCutEducationController extends com.cpf.reference.common.base.ReferenceBaseController {
    private final ReferenceCenterCutEducationService educationService;

    public ReferenceCenterCutEducationController(ReferenceCenterCutEducationService educationService) {
        this.educationService = educationService;
    }

    @PostMapping("/run")
    @CpfOnlineTransaction(id = "OREFAA0056", name = "REFCenterCut업무DBAdapter실행")
    @Operation(operationId = "refCenterCutEducationRun",
            summary = "업무 DB 기반 center-cut 샘플 실행",
            description = "ref_center_cut_sample_target을 조회하고 item별 성공/실패 결과를 ref_center_cut_sample_result에 기록합니다.")
    public ResponseEntity<ReferenceCenterCutExecutionResponse> run(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "false") boolean resetBeforeRun) {
        return ResponseEntity.ok(educationService.runSample(limit, resetBeforeRun));
    }
}
