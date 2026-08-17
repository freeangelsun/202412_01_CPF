package com.cpf.education.online;

import com.cpf.core.api.result.CpfRecoveryInfo;
import com.cpf.core.api.result.CpfResult;
import com.cpf.data.persistence.api.CpfCrudRepository;
import com.cpf.data.persistence.api.annotation.CpfTransactional;
import com.cpf.foundation.execution.api.CpfOnlineTransaction;
import com.cpf.integration.api.http.CpfRestClient;
import com.cpf.web.api.CpfRestController;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/** 온라인-11 외부 Side Effect: 로컬 상태와 외부 결과를 분리하고 timeout ambiguity를 UNKNOWN으로 저장합니다. */
@CpfRestController
@RequestMapping("/edu/online/11-side-effect")
public class Online11ExternalSideEffectTransactionExample {
    private final CpfRestClient rest;
    private final ObjectProvider<CpfCrudRepository<PaymentState, String>> repositories;

    public Online11ExternalSideEffectTransactionExample(
            CpfRestClient rest,
            ObjectProvider<CpfCrudRepository<PaymentState, String>> repositories) {
        this.rest = rest;
        this.repositories = repositories;
    }

    @PostMapping
    @CpfTransactional
    @Operation(operationId = "EDU-ONLINE-11", summary = "Transaction 경계 + 외부 Side Effect")
    @CpfOnlineTransaction(
            operationId = "EDU-ONLINE-11",
            name = "외부 Side Effect 거래",
            description = "로컬 PENDING을 저장한 뒤 외부 Side Effect를 호출하고 결과불명 시 UNKNOWN과 reconcile key를 보존한다.")
    /** execute 단계는 CPF Public 계약을 사용해 이 EDU 시나리오의 업무 흐름을 수행합니다. */
    public CpfResult<String> execute(@RequestBody SideEffectCommand command) {
        CpfCrudRepository<PaymentState, String> repository = repository();
        repository.save(new PaymentState(command.idempotencyKey(), "PENDING", null));
        try {
            String externalId = rest.post("payment-agency", "/payments", command, String.class);
            repository.save(new PaymentState(command.idempotencyKey(), "COMPLETED", externalId));
            return CpfResult.success(externalId);
        // 결과불명·재시도·복구 경계를 일반 실패로 축소하지 않고 상태와 복구 기준을 보존합니다.
        } catch (RuntimeException failure) {
            String recoveryId = "payment:" + command.idempotencyKey();
            repository.save(new PaymentState(command.idempotencyKey(), "UNKNOWN", recoveryId));
            return CpfResult.unknown(
                    "EXT-TIMEOUT",
                    "외부 결과를 확정할 수 없어 blind retry하지 않습니다.",
                    new CpfRecoveryInfo(recoveryId, "PROBE_OR_RECONCILE"));
        }
    }

    /** 이 타입은 해당 EDU 시나리오의 입력·출력 또는 업무 경계를 명확히 표현합니다. */
    public record SideEffectCommand(String idempotencyKey, long amount) { }
    /** 이 타입은 해당 EDU 시나리오의 입력·출력 또는 업무 경계를 명확히 표현합니다. */
    public record PaymentState(String idempotencyKey, String status, String externalReference) { }

    private CpfCrudRepository<PaymentState, String> repository() {
        CpfCrudRepository<PaymentState, String> repository = repositories.getIfAvailable();
        if (repository == null) throw new IllegalStateException("CPF payment repository is not configured");
        return repository;
    }
}
