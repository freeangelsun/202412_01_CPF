package com.cpf.core.api.servicecall;

/** 표준 서비스 호출 결과의 공개 View. 결과불명은 status=UNKNOWN으로 명시됩니다. */
public record CpfServiceResult<T>(
        String status,
        CpfServiceTarget target,
        T responseBody,
        Integer httpStatus,
        Long durationMillis,
        Integer attemptCount,
        String failureCode,
        String failureMessage) {
    public boolean success(){ return "SUCCESS".equals(status); }
    public boolean unknown(){ return "UNKNOWN".equals(status); }
}
