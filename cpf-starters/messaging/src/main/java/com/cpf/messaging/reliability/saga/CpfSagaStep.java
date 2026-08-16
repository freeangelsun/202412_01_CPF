package com.cpf.messaging.reliability.saga;

/** 하나의 Saga 정방향 실행과 대응 보상을 함께 정의하는 Step 계약입니다. */
public interface CpfSagaStep {
    String stepId();
    CpfSagaStepResult execute(CpfSagaContext context) throws Exception;
    void compensate(CpfSagaContext context, CpfSagaStepResult priorResult) throws Exception;
}
