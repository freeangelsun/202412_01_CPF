package cpf.acc.tst.controller;

import cpf.pfw.common.exception.CpfBusinessException;
import cpf.pfw.common.exception.CpfExternalServiceException;
import cpf.pfw.common.exception.CpfValidationException;
import cpf.pfw.common.logging.CpfTransaction;
import cpf.pfw.common.workflow.CpfWorkflow;
import cpf.pfw.common.workflow.CpfWorkflowFailurePolicy;
import cpf.pfw.common.workflow.CpfWorkflowStep;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/acc/tran")
@Tag(name = "ACC-TST Transaction", description = "Transaction log, standard exception, compensation, and workflow sample APIs")
public class TransactionController {

    @GetMapping("/success")
    @CpfTransaction(id = "ACC09TST0001", name = "ACCSuccessSample")
    @CpfWorkflow(id = "ACC09TST9001", name = "ACCSuccessWorkflow")
    @CpfWorkflowStep(name = "ACCSuccessStep")
    @Operation(summary = "Successful transaction sample", description = "Writes a SUCCESS/COMPLETED transaction log sample.")
    public ResponseEntity<String> handleSuccessfulTransaction(@RequestParam String menuId, @RequestParam String execUser) {
        return ResponseEntity.ok("Transaction processed successfully.");
    }

    @GetMapping("/failure")
    @CpfTransaction(id = "ACC09TST0002", name = "ACCFailureSample")
    @CpfWorkflow(id = "ACC09TST9002", name = "ACCCompensationWorkflow")
    @CpfWorkflowStep(
            name = "ACCFailureStep",
            failurePolicy = CpfWorkflowFailurePolicy.COMPENSATE,
            compensationTransactionId = "ACC09TST0005")
    @Operation(summary = "Failed transaction sample", description = "Triggers COMPENSATING workflow metadata for log verification.")
    public ResponseEntity<String> handleFailedTransaction(@RequestParam String menuId, @RequestParam String execUser) {
        throw new RuntimeException("Simulated transaction failure.");
    }

    @GetMapping("/compensate")
    @CpfTransaction(id = "ACC09TST0005", name = "ACCCompensationSample")
    @CpfWorkflow(id = "ACC09TST9002", name = "ACCCompensationWorkflow")
    @CpfWorkflowStep(
            name = "ACCCompensationStep",
            compensation = true,
            compensationTargetTransactionId = "ACC09TST0002")
    @Operation(summary = "Compensation transaction sample", description = "Writes a compensation transaction log sample.")
    public ResponseEntity<String> handleCompensationTransaction(@RequestParam String menuId, @RequestParam String execUser) {
        return ResponseEntity.ok("Compensation transaction processed successfully.");
    }

    @GetMapping("/standard-exception")
    @CpfTransaction(id = "ACC09TST0006", name = "PFWStandardExceptionSample")
    @Operation(summary = "Standard exception sample", description = "Throws validation, business, or external exceptions for handler/log tests.")
    public ResponseEntity<String> throwStandardException(@RequestParam(defaultValue = "validation") String type) {
        if ("business".equalsIgnoreCase(type)) {
            throw new CpfBusinessException("Business exception sample. type=" + type);
        }
        if ("external".equalsIgnoreCase(type)) {
            throw new CpfExternalServiceException("External service exception sample. target=MBR", new IllegalStateException("MBR timeout sample"));
        }

        throw new CpfValidationException("Validation exception sample. type=" + type);
    }
}
