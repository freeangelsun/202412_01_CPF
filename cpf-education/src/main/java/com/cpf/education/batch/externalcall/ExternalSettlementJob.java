package com.cpf.education.batch.externalcall;

import com.cpf.batch.api.annotation.CpfBatchJob;
import com.cpf.batch.api.annotation.CpfBatchStep;
import com.cpf.batch.spi.BatchStepHandler.BatchStepCommand;
import com.cpf.batch.spi.BatchStepHandler.BatchStepResult;
import com.cpf.core.api.result.CpfResult;
import com.cpf.core.api.result.CpfRecoveryInfo;
import com.cpf.data.persistence.api.CpfCrudRepository;
import com.cpf.data.persistence.api.annotation.CpfTransactional;
import com.cpf.foundation.annotation.CpfService;
import com.cpf.integration.api.http.CpfRestClient;
import java.util.Map;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.transaction.annotation.Propagation;

/** 배치-14 외부 호출 + UNKNOWN: 기술실패와 결과불명을 분리하고 UNKNOWN만 Reconcile 경로로 보냅니다. */
@CpfBatchJob(value = "EDU_EXTERNAL_SETTLEMENT_JOB")
public class ExternalSettlementJob {
    private final ExternalSettlementClient client;
    private final StateService stateService;

    public ExternalSettlementJob(CpfRestClient rest, StateService stateService) {
        this.client = new ExternalSettlementClient(rest);
        this.stateService = stateService;
    }

    @CpfBatchStep(value = "external-call", order = 1)
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
                    return new BatchStepResult(BatchStepResult.Status.FAILED, business.errorCode(), business.errorMessage(), 1, 0, 0,
                            Map.of("idempotencyKey", requestKey));
                },
                technical -> {
                    stateService.mark(requestKey, "TECHNICAL_FAILURE", technical.errorCode());
                    return new BatchStepResult(BatchStepResult.Status.FAILED, technical.errorCode(), technical.errorMessage(), 1, 0, 0,
                            Map.of("idempotencyKey", requestKey));
                },
                unknown -> unknownResult(command, requestKey, unknown.recoveryInfo()));
    }

    private BatchStepResult unknownResult(BatchStepCommand command, String requestKey, CpfRecoveryInfo recovery) {
        CpfRecoveryInfo effective = recovery == null ? new CpfRecoveryInfo("batch:" + requestKey, "PROBE_OR_RECONCILE") : recovery;
        stateService.mark(requestKey, "UNKNOWN", effective.recoveryId());
        return new BatchStepResult(
                BatchStepResult.Status.UNKNOWN_RESULT,
                "UNKNOWN_RESULT",
                "외부 결과가 불명하므로 blind retry하지 않고 Probe/Reconcile 후 재시작 여부를 결정합니다.",
                1, 0, 0,
                Map.of("idempotencyKey", requestKey, "recoveryId", effective.recoveryId(), "checkpoint", command.stepExecutionId()));
    }

    /** 기관 호출 Transport를 CPF Boundary Result로 보존하는 Batch Client입니다. */
    static final class ExternalSettlementClient {
        private final CpfRestClient rest;
        ExternalSettlementClient(CpfRestClient rest) { this.rest = rest; }
        CpfResult<String> settle(Map<String,Object> payload, String idempotencyKey) {
            return rest.exchangeResult("settlement-agency", "POST", b -> b.path("/batch").build(), payload,
                    Map.of("X-Idempotency-Key", idempotencyKey), String.class);
        }
    }

    /** ExternalState는 Batch 외부호출의 SUCCESS/업무실패/기술실패/UNKNOWN과 재시작 기준을 보여주는 Golden Path입니다. */
    public record ExternalState(String id, String status, String reference) { }

    @CpfService
    /** StateService는 Batch 외부호출의 SUCCESS/업무실패/기술실패/UNKNOWN과 재시작 기준을 보여주는 Golden Path입니다. */
    public static class StateService {
        private final ObjectProvider<CpfCrudRepository<ExternalState, String>> repositories;
        /** StateService 동작은 Batch 외부호출의 SUCCESS/업무실패/기술실패/UNKNOWN과 재시작 기준을 보여주는 Golden Path에서 필요한 공개 동작을 수행합니다. */
        public StateService(ObjectProvider<CpfCrudRepository<ExternalState, String>> repositories) { this.repositories = repositories; }
        @CpfTransactional(propagation = Propagation.REQUIRES_NEW)
        /** mark 동작은 Batch 외부호출의 SUCCESS/업무실패/기술실패/UNKNOWN과 재시작 기준을 보여주는 Golden Path에서 필요한 공개 동작을 수행합니다. */
        public void mark(String id, String status, String reference) {
            CpfCrudRepository<ExternalState, String> repository = repositories.getIfAvailable();
            if (repository == null) throw new IllegalStateException("CPF batch state repository is not configured");
            repository.save(new ExternalState(id, status, reference));
        }
    }
}
