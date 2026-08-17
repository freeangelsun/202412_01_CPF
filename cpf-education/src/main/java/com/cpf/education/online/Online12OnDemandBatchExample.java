package com.cpf.education.online;

import com.cpf.batch.api.CpfBatchExecutionRequest;
import com.cpf.batch.api.CpfBatchExecutionResult;
import com.cpf.batch.api.CpfBatchOperations;
import com.cpf.foundation.execution.api.CpfOnlineTransaction;
import com.cpf.security.api.annotation.CpfPreAuthorize;
import com.cpf.web.api.CpfRestController;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/** 온라인-12 On-Demand Batch: 권한·멱등 실행 접수 후 executionRequestId로 상태를 조회합니다. */
@CpfRestController
@RequestMapping("/edu/online/12-batch")
public class Online12OnDemandBatchExample {
    private final CpfBatchOperations batches;

    public Online12OnDemandBatchExample(CpfBatchOperations batches) {
        this.batches = batches;
    }

    @PostMapping
    @CpfPreAuthorize("hasAuthority('BATCH_EXECUTE')")
    @Operation(operationId = "EDU-ONLINE-12", summary = "On-Demand Batch 호출")
    @CpfOnlineTransaction(
            operationId = "EDU-ONLINE-12",
            name = "On-Demand Batch 호출 거래",
            description = "CPF Batch Public API로 멱등 실행을 접수하고 executionRequestId로 현재 상태를 조회한다.")
    /** launch 단계는 CPF Public 계약을 사용해 이 EDU 시나리오의 업무 흐름을 수행합니다. */
    public LaunchView launch(@RequestBody Command command) {
        CpfBatchExecutionRequest request = CpfBatchExecutionRequest.onDemand(
                "EDU-BATCH-15", "EDU-BATCH-15", command.businessDate(), command.idempotencyKey(),
                command.jobParameters(), command.requestUser(), "교육 On-Demand Batch 실행");
        CpfBatchExecutionResult accepted = batches.launch(request);
        CpfBatchExecutionResult current = batches.status(accepted.executionRequestId());
        return new LaunchView(accepted.executionRequestId(), accepted.status(), current.status(), request.lockRequired());
    }

    /** 이 타입은 해당 EDU 시나리오의 입력·출력 또는 업무 경계를 명확히 표현합니다. */
    public record Command(String businessDate, String idempotencyKey, String jobParameters, String requestUser) { }
    /** 이 타입은 해당 EDU 시나리오의 입력·출력 또는 업무 경계를 명확히 표현합니다. */
    public record LaunchView(String executionRequestId, String acceptedStatus, String currentStatus, boolean duplicateLockEnabled) { }
}
