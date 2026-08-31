package com.cpf.education.batch.externalcall.service;

import com.cpf.education.online.common.base.EducationBaseService;

import com.cpf.batch.spi.BatchStepHandler.BatchStepCommand;
import com.cpf.batch.spi.BatchStepHandler.BatchStepResult;
import com.cpf.batch.spi.BatchStepHandler.Status;
import com.cpf.core.api.result.CpfResult;
import com.cpf.core.api.result.CpfRecoveryInfo;
import com.cpf.foundation.annotation.CpfService;
import com.cpf.integration.api.http.CpfRestClient;
import com.cpf.education.batch.externalcall.client.ExternalSettlementClient;
import com.cpf.education.batch.externalcall.state.ExternalSettlementStateService;
import java.util.Map;

/** 배치-14 외부 호출 + UNKNOWN: 기술실패와 결과불명을 분리하고 UNKNOWN만 Reconcile 경로로 보냅니다. */
@CpfService
public class ExternalSettlementJobService extends EducationBaseService {
    private final ExternalSettlementClient client;
    private final ExternalSettlementStateService stateService;

    public ExternalSettlementJobService(CpfRestClient rest, ExternalSettlementStateService stateService) {
        this.client = new ExternalSettlementClient(rest);
        this.stateService = stateService;
    }

    /** run 동작은 Batch 외부호출의 SUCCESS/업무실패/기술실패/UNKNOWN과 재시작 기준을 보여주는 Golden Path에서 필요한 공개 동작을 수행합니다. */
    public BatchStepResult run(BatchStepCommand command) {
        String requestKey = String.valueOf(command.jobParameters().getOrDefault("idempotencyKey", command.cpfExecutionId()));
        stateService.mark(requestKey, "PENDING", null);
        CpfResult<String> result = client.settle(command.jobParameters(), requestKey);
        return result.fold(
                externalReference -> {
                    stateService.mark(requestKey, "COMPLETED", externalReference);
                    return BatchStepResult.completed("external result confirmed", 1, 1,
                            Map.of("idempotencyKey", requestKey, "externalReference", externalReference));
                },
                business -> {
                    stateService.mark(requestKey, "BUSINESS_FAILURE", business.errorCode());
                    return new BatchStepResult(Status.FAILED, business.errorCode(), business.errorMessage(), 1, 0, 0,
                            Map.of("idempotencyKey", requestKey));
                },
                technical -> {
                    stateService.mark(requestKey, "TECHNICAL_FAILURE", technical.errorCode());
                    return new BatchStepResult(Status.FAILED, technical.errorCode(), technical.errorMessage(), 1, 0, 0,
                            Map.of("idempotencyKey", requestKey));
                },
                unknown -> unknownResult(command, requestKey, unknown.recoveryInfo()));
    }

    private BatchStepResult unknownResult(BatchStepCommand command, String requestKey, CpfRecoveryInfo recovery) {
        CpfRecoveryInfo effective = recovery == null ? new CpfRecoveryInfo("batch:" + requestKey, "PROBE_OR_RECONCILE") : recovery;
        stateService.mark(requestKey, "UNKNOWN", effective.recoveryId());
        return new BatchStepResult(
                Status.UNKNOWN_RESULT,
                "UNKNOWN_RESULT",
                "외부 결과가 불명하므로 blind retry하지 않고 Probe/Reconcile 후 재시작 여부를 결정합니다.",
                1, 0, 0,
                Map.of("idempotencyKey", requestKey, "recoveryId", effective.recoveryId(), "checkpoint", command.stepExecutionId()));
    }

}
