package com.cpf.core.api.servicecall;

/** 표준 호출 엔진의 성공/실패/결과불명 공개 결과입니다. */
public record CpfServiceCallOutcome<T>(
        String status, CpfServiceCallTarget target, T responseBody, Integer httpStatus, Long durationMillis,
        Integer attemptCount, String failureCode, String failureMessage) {
    public boolean success(){ return "SUCCESS".equals(status); }
}
