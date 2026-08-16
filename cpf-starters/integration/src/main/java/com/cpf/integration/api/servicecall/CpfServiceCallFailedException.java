package com.cpf.integration.api.servicecall;

/** 표준 호출 엔진이 최종 실패 또는 결과불명으로 판정한 경우 외부 Consumer에 전달하는 예외입니다. */
public class CpfServiceCallFailedException extends RuntimeException {
    private final transient CpfServiceCallOutcome<?> outcome;

    public CpfServiceCallFailedException(CpfServiceCallOutcome<?> outcome) {
        super(message(outcome));
        this.outcome = outcome;
    }

    /** 실패 원본 결과를 반환합니다. 호출자는 ResultStatus를 기준으로 복구·재조회·업무처리를 결정할 수 있습니다. */
    public CpfServiceCallOutcome<?> outcome() {
        return outcome;
    }

    private static String message(CpfServiceCallOutcome<?> outcome) {
        if (outcome == null) {
            return "서비스 호출이 실패했습니다.";
        }
        if (outcome.failureMessage() != null && !outcome.failureMessage().isBlank()) {
            return outcome.failureMessage();
        }
        return switch (outcome.resultStatus()) {
            case BUSINESS_FAILURE -> "서비스 호출이 업무 실패로 종료되었습니다.";
            case TECHNICAL_FAILURE -> "서비스 호출이 기술 실패로 종료되었습니다.";
            case UNKNOWN -> "서비스 호출 결과를 확정할 수 없습니다. 재조회 또는 조정이 필요합니다.";
            case SUCCESS -> "서비스 호출 결과 처리 중 예외가 발생했습니다.";
        };
    }
}
