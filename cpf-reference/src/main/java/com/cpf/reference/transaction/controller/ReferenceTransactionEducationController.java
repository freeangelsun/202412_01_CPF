package com.cpf.reference.transaction.controller;

import com.cpf.core.common.execution.CpfOnlineTransaction;
import com.cpf.core.common.workflow.CpfWorkflow;
import com.cpf.core.common.workflow.CpfWorkflowFailurePolicy;
import com.cpf.core.common.workflow.CpfWorkflowStep;
import com.cpf.reference.crud.application.ReferenceCrudEducationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping({"/api/reference", "/reference/edu"})
@Tag(name = "REF Reference 09. Transaction", description = "단일 트랜잭션과 분리 감사 트랜잭션 교육")
public class ReferenceTransactionEducationController extends com.cpf.reference.common.base.ReferenceBaseController {
    private final ReferenceCrudEducationService crudEducationService;

    public ReferenceTransactionEducationController(ReferenceCrudEducationService crudEducationService) {
        this.crudEducationService = crudEducationService;
    }

    @PostMapping("/transaction/single")
    @CpfOnlineTransaction(id = "OREFAA0011", name = "REFSingleTransaction")
    @Operation(operationId = "refTransactionEducationRunSingleTransactionEducation", summary = "단일 트랜잭션 교육", description = "REF 교육 서비스에서 하나의 트랜잭션으로 등록 흐름을 실행합니다.")
    public ResponseEntity<String> runSingleTransactionEducation() {
        return ResponseEntity.ok(crudEducationService.runSingleTransactionEducation());
    }

    @PostMapping("/transaction/separated")
    @CpfOnlineTransaction(id = "OREFAA0012", name = "REFSeparatedTransaction")
    @CpfWorkflow(id = "OREFAA9001", name = "REFSeparatedTransactionWorkflow")
    @CpfWorkflowStep(name = "REFSeparatedTransactionStep", failurePolicy = CpfWorkflowFailurePolicy.MANUAL)
    @Operation(operationId = "refTransactionEducationRunSeparatedTransactionEducation", summary = "분리 트랜잭션 교육", description = "REQUIRES_NEW 감사 로직과 감사 이후 실패 흐름을 확인합니다.")
    public ResponseEntity<Map<String, Object>> runSeparatedTransactionEducation(
            @RequestParam(defaultValue = "false") boolean failAfterAudit) {

        String result = crudEducationService.runSeparatedTransactionEducation(failAfterAudit);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("result", result);
        response.put("auditMessages", crudEducationService.getAuditMessages());
        return ResponseEntity.ok(response);
    }
}
