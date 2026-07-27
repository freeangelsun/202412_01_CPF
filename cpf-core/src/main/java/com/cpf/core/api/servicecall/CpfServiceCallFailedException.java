package com.cpf.core.api.servicecall;

/** 표준 호출 엔진이 최종 실패 또는 결과불명으로 판정한 경우 외부 Consumer에 전달하는 예외입니다. */
public class CpfServiceCallFailedException extends RuntimeException {
    private final transient CpfServiceCallOutcome<?> outcome;
    public CpfServiceCallFailedException(CpfServiceCallOutcome<?> outcome) {
        super(outcome == null || outcome.failureMessage() == null ? "서비스 호출이 실패했습니다." : outcome.failureMessage());
        this.outcome=outcome;
    }
    public CpfServiceCallOutcome<?> outcome(){ return outcome; }
}
