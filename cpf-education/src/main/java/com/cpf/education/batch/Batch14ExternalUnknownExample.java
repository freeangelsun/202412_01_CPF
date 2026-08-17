package com.cpf.education.batch;

import com.cpf.batch.api.annotation.CpfBatchJob;
import com.cpf.batch.api.annotation.CpfBatchStep;
import com.cpf.batch.spi.BatchStepHandler.BatchStepCommand;
import com.cpf.batch.spi.BatchStepHandler.BatchStepResult;
import com.cpf.core.api.result.CpfRecoveryInfo;
import com.cpf.data.persistence.api.CpfCrudRepository;
import com.cpf.data.persistence.api.annotation.CpfTransactional;
import com.cpf.foundation.annotation.CpfService;
import com.cpf.integration.api.http.CpfRestClient;
import java.util.Map;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.transaction.annotation.Propagation;

/** 배치-14 외부 호출 + UNKNOWN: 로컬 Transaction을 외부 호출 앞/뒤로 분리하고 결과불명 시 reconcile checkpoint를 남깁니다. */
@CpfBatchJob(value = "EDU-BATCH-14")
public class Batch14ExternalUnknownExample {
    private final CpfRestClient rest;
    private final StateService stateService;

    public Batch14ExternalUnknownExample(CpfRestClient rest, StateService stateService) {
        this.rest = rest;
        this.stateService = stateService;
    }

    @CpfBatchStep(value = "external-call", order = 1)
    /** run 단계는 CPF Public 계약을 사용해 이 EDU 시나리오의 업무 흐름을 수행합니다. */
    public BatchStepResult run(BatchStepCommand command) {
        String requestKey = String.valueOf(command.jobParameters().getOrDefault("idempotencyKey", command.cpfExecutionId()));
        stateService.mark(requestKey, "PENDING", null);
        try {
            String externalReference = rest.post("settlement-agency", "/batch", command.jobParameters(), String.class);
            stateService.mark(requestKey, "COMPLETED", externalReference);
            return BatchStepResult.completed(
                    "external result confirmed",
                    1, 1,
                    Map.of("idempotencyKey", requestKey, "externalReference", externalReference));
        // 결과불명·재시도·복구 경계를 일반 실패로 축소하지 않고 상태와 복구 기준을 보존합니다.
        } catch (RuntimeException failure) {
            CpfRecoveryInfo recovery = new CpfRecoveryInfo("batch:" + requestKey, "RECONCILE");
            stateService.mark(requestKey, "UNKNOWN", recovery.recoveryId());
            return new BatchStepResult(
                    BatchStepResult.Status.UNKNOWN_RESULT,
                    "UNKNOWN_RESULT",
                    "외부 결과 확정 전 blind retry하지 않고 reconcile 후 재시작 여부를 결정합니다.",
                    1, 0, 0,
                    Map.of(
                            "idempotencyKey", requestKey,
                            "recoveryId", recovery.recoveryId(),
                            "checkpoint", command.stepExecutionId()));
        }
    }

    /** 이 타입은 해당 EDU 시나리오의 입력·출력 또는 업무 경계를 명확히 표현합니다. */
    public record ExternalState(String id, String status, String reference) { }

    @CpfService
    /** 이 타입은 해당 EDU 시나리오의 입력·출력 또는 업무 경계를 명확히 표현합니다. */
    public static class StateService {
        private final ObjectProvider<CpfCrudRepository<ExternalState, String>> repositories;

        /** StateService 단계는 CPF Public 계약을 사용해 이 EDU 시나리오의 업무 흐름을 수행합니다. */
        public StateService(ObjectProvider<CpfCrudRepository<ExternalState, String>> repositories) {
            this.repositories = repositories;
        }

        @CpfTransactional(propagation = Propagation.REQUIRES_NEW)
        /** mark 단계는 CPF Public 계약을 사용해 이 EDU 시나리오의 업무 흐름을 수행합니다. */
        public void mark(String id, String status, String reference) {
            CpfCrudRepository<ExternalState, String> repository = repositories.getIfAvailable();
            if (repository == null) throw new IllegalStateException("CPF batch state repository is not configured");
            repository.save(new ExternalState(id, status, reference));
        }
    }
}
