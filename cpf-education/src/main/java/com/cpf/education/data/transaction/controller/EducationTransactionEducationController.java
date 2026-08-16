package com.cpf.education.data.transaction.controller;
import com.cpf.foundation.annotation.CpfOnlineTransaction;
import com.cpf.foundation.workflow.CpfWorkflow;
import com.cpf.foundation.workflow.CpfWorkflowFailurePolicy;
import com.cpf.foundation.workflow.CpfWorkflowStep;
import com.cpf.education.web.crud.application.EducationCrudEducationService;
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
@RequestMapping({"/api/education", "/education/edu"})
@Tag(name = "EDU Education 09. Transaction", description = "단일 트랜잭션과 분리 감사 트랜잭션 교육")
/** EducationTransactionEducationController 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public class EducationTransactionEducationController extends com.cpf.education.base.EducationBaseController {
    private final EducationCrudEducationService crudEducationService;

    public EducationTransactionEducationController(EducationCrudEducationService crudEducationService) {
        this.crudEducationService = crudEducationService;
    }

    @PostMapping("/transaction/single")
    @CpfOnlineTransaction(id = "OEDUAA0011", name = "EDUSingleTransaction", ownerDomain="EDU")
    @Operation(operationId = "refTransactionEducationRunSingleTransactionEducation", summary = "단일 트랜잭션 교육", description = "EDU 교육 서비스에서 하나의 트랜잭션으로 등록 흐름을 실행합니다.")
    /** runSingleTransactionEducation 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<String> runSingleTransactionEducation() {
        return ResponseEntity.ok(crudEducationService.runSingleTransactionEducation());
    }

    @PostMapping("/transaction/separated")
    @CpfOnlineTransaction(id = "OEDUAA0012", name = "EDUSeparatedTransaction", ownerDomain="EDU")
    @CpfWorkflow(id = "OEDUAA9001", name = "EDUSeparatedTransactionWorkflow")
    @CpfWorkflowStep(name = "EDUSeparatedTransactionStep", failurePolicy = CpfWorkflowFailurePolicy.MANUAL)
    @Operation(operationId = "refTransactionEducationRunSeparatedTransactionEducation", summary = "분리 트랜잭션 교육", description = "REQUIRES_NEW 감사 로직과 감사 이후 실패 흐름을 확인합니다.")
    /** runSeparatedTransactionEducation 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<Map<String, Object>> runSeparatedTransactionEducation(
            @RequestParam(defaultValue = "false") boolean failAfterAudit) {

        String result = crudEducationService.runSeparatedTransactionEducation(failAfterAudit);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("result", result);
        response.put("auditMessages", crudEducationService.getAuditMessages());
        return ResponseEntity.ok(response);
    }
}
