package com.cpf.core.common.saga;

/** Saga Step의 저장 가능한 최소 결과 Snapshot. */
public record CpfSagaStepResult(String resultCode, String resultSnapshot) {
    public static CpfSagaStepResult success(String snapshot){return new CpfSagaStepResult("SUCCESS",snapshot);}
}
