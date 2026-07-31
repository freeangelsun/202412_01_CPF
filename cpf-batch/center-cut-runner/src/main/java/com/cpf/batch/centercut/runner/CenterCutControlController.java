package com.cpf.batch.centercut.runner;

import com.cpf.batch.api.BatchExecutionControlPort;
import com.cpf.batch.api.BatchExecutionLink;
import com.cpf.batch.spi.BatchApprovedLaunchRequestResolver;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.security.Principal;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Center-Cut Control Plane. 실제 실행 생명주기는 Spring Batch JobOperator에 위임합니다. */
@RestController
@RequestMapping("/internal/v1/center-cut")
public class CenterCutControlController {
    private final BatchExecutionControlPort executions;
    private final BatchApprovedLaunchRequestResolver approvals;

    public CenterCutControlController(BatchExecutionControlPort executions, BatchApprovedLaunchRequestResolver approvals) {
        this.executions = executions;
        this.approvals = approvals;
    }

    @PostMapping("/executions")
    ResponseEntity<BatchExecutionLink> start(@Valid @RequestBody StartCommand command, Principal principal) {
        String operatorId = requiredPrincipal(principal);
        BatchExecutionLink link = executions.start(approvals.resolve(
                new BatchApprovedLaunchRequestResolver.ManualContext(
                        command.approvalId(), operatorId, command.reason(), command.idempotencyKey(),
                        command.fencingToken(), command.parameters())));
        return ResponseEntity.accepted().body(link);
    }

    @PostMapping("/executions/{jobExecutionId}/stop")
    ResponseEntity<Map<String,Object>> stop(@PathVariable long jobExecutionId, @Valid @RequestBody ReasonCommand command, Principal principal) {
        return ResponseEntity.accepted().body(Map.of("accepted", executions.stop(jobExecutionId, requiredPrincipal(principal), command.reason())));
    }

    @PostMapping("/executions/{jobExecutionId}/restart")
    ResponseEntity<BatchExecutionLink> restart(@PathVariable long jobExecutionId, @Valid @RequestBody RestartCommand command, Principal principal) {
        return ResponseEntity.accepted().body(executions.restart(jobExecutionId, requiredPrincipal(principal), command.reason(), command.fencingToken()));
    }

    @PostMapping("/executions/{jobExecutionId}/abandon")
    ResponseEntity<Void> abandon(@PathVariable long jobExecutionId, @Valid @RequestBody ReasonCommand command, Principal principal) {
        executions.abandon(jobExecutionId, requiredPrincipal(principal), command.reason());
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/executions/{cpfExecutionId}/reconcile")
    ResponseEntity<BatchExecutionLink> reconcile(@PathVariable String cpfExecutionId) {
        return ResponseEntity.ok(executions.reconcile(cpfExecutionId));
    }

    private static String requiredPrincipal(Principal principal) {
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            throw new SecurityException("AUTHENTICATED_OPERATOR_REQUIRED");
        }
        return principal.getName();
    }

    public record StartCommand(@NotBlank String approvalId, @NotBlank String reason,
                               @NotBlank String idempotencyKey, @Positive long fencingToken,
                               Map<String,Object> parameters) {
        public StartCommand { parameters = parameters == null ? Map.of() : Map.copyOf(parameters); }
    }
    public record ReasonCommand(@NotBlank String reason) { }
    public record RestartCommand(@NotBlank String reason, @Positive long fencingToken) { }
}
