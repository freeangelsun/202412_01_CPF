package cpf.xyz.edu.controller;

import cpf.pfw.common.logging.CpfTransaction;
import cpf.pfw.common.workflow.CpfWorkflow;
import cpf.pfw.common.workflow.CpfWorkflowFailurePolicy;
import cpf.pfw.common.workflow.CpfWorkflowStep;
import cpf.xyz.edu.service.XyzSampleService;
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
@Tag(name = "XYZ-EDU 09. Transaction", description = "Single transaction and separated audit transaction samples")
public class XyzTransactionEducationController {
    private final XyzSampleService xyzSampleService;

    public XyzTransactionEducationController(XyzSampleService xyzSampleService) {
        this.xyzSampleService = xyzSampleService;
    }

    @PostMapping("/transaction/single")
    @CpfTransaction(id = "XYZ05EDU0001", name = "XYZSingleTransaction")
    @Operation(summary = "Single transaction sample", description = "Runs one sample transaction through the XYZ service.")
    public ResponseEntity<String> runSingleTransactionSample() {
        return ResponseEntity.ok(xyzSampleService.runSingleTransactionSample());
    }

    @PostMapping("/transaction/separated")
    @CpfTransaction(id = "XYZ05EDU0002", name = "XYZSeparatedTransaction")
    @CpfWorkflow(id = "XYZ05EDU9001", name = "XYZSeparatedTransactionWorkflow")
    @CpfWorkflowStep(name = "XYZSeparatedTransactionStep", failurePolicy = CpfWorkflowFailurePolicy.MANUAL)
    @Operation(summary = "Separated transaction sample", description = "Runs REQUIRES_NEW audit logic and optional failure after audit.")
    public ResponseEntity<Map<String, Object>> runSeparatedTransactionSample(
            @RequestParam(defaultValue = "false") boolean failAfterAudit) {

        String result = xyzSampleService.runSeparatedTransactionSample(failAfterAudit);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("result", result);
        response.put("auditMessages", xyzSampleService.getAuditMessages());
        return ResponseEntity.ok(response);
    }
}
