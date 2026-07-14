package cpf.xyz.edu.controller;

import cpf.pfw.common.logging.CpfTransaction;
import cpf.pfw.common.workflow.CpfWorkflow;
import cpf.pfw.common.workflow.CpfWorkflowFailurePolicy;
import cpf.pfw.common.workflow.CpfWorkflowStep;
import cpf.xyz.edu.service.XyzCrudEducationService;
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
@RequestMapping("/xyz/edu")
@Tag(name = "XYZ-EDU 09. Transaction", description = "단일 트랜잭션과 분리 감사 트랜잭션 교육")
public class XyzTransactionEducationController {
    private final XyzCrudEducationService crudEducationService;

    public XyzTransactionEducationController(XyzCrudEducationService crudEducationService) {
        this.crudEducationService = crudEducationService;
    }

    @PostMapping("/transaction/single")
    @CpfTransaction(id = "XYZ05EDU0001", name = "XYZSingleTransaction")
    @Operation(operationId = "xyzTransactionEducationRunSingleTransactionEducation", summary = "단일 트랜잭션 교육", description = "XYZ 교육 서비스에서 하나의 트랜잭션으로 등록 흐름을 실행합니다.")
    public ResponseEntity<String> runSingleTransactionEducation() {
        return ResponseEntity.ok(crudEducationService.runSingleTransactionEducation());
    }

    @PostMapping("/transaction/separated")
    @CpfTransaction(id = "XYZ05EDU0002", name = "XYZSeparatedTransaction")
    @CpfWorkflow(id = "XYZ05EDU9001", name = "XYZSeparatedTransactionWorkflow")
    @CpfWorkflowStep(name = "XYZSeparatedTransactionStep", failurePolicy = CpfWorkflowFailurePolicy.MANUAL)
    @Operation(operationId = "xyzTransactionEducationRunSeparatedTransactionEducation", summary = "분리 트랜잭션 교육", description = "REQUIRES_NEW 감사 로직과 감사 이후 실패 흐름을 확인합니다.")
    public ResponseEntity<Map<String, Object>> runSeparatedTransactionEducation(
            @RequestParam(defaultValue = "false") boolean failAfterAudit) {

        String result = crudEducationService.runSeparatedTransactionEducation(failAfterAudit);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("result", result);
        response.put("auditMessages", crudEducationService.getAuditMessages());
        return ResponseEntity.ok(response);
    }
}
