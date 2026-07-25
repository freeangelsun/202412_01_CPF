package com.cpf.core.common.saga;

/**
 * Saga 업무 Adapter 계약. execute와 compensate 모두 멱등하게 구현해야 합니다.
 * CPF는 호출 순서/상태/재시도/수동복구를 책임지고 실제 업무 변경은 Owner Adapter가 수행합니다.
 */
public interface CpfSagaStep {
    String stepId();
    CpfSagaStepResult execute(CpfSagaContext context) throws Exception;
    void compensate(CpfSagaContext context, CpfSagaStepResult executedResult) throws Exception;
}
