package com.cpf.integration.api.servicecall;

/**
 * 원격 호출은 정상적으로 도달했지만 업무 규칙에 의해 거절된 결과를 표준 4상태의 BUSINESS_FAILURE로 전달합니다.
 * 기술 장애나 결과불명과 혼용하지 않으며 자동 재시도 대상이 아닙니다.
 */
public class CpfServiceCallBusinessException extends RuntimeException {
    private final String failureCode;
    private final Integer httpStatus;

    /** 업무 실패 코드와 메시지, 선택적 HTTP 상태를 보존합니다. */
    public CpfServiceCallBusinessException(String failureCode, String message, Integer httpStatus) {
        super(message);
        this.failureCode = failureCode == null || failureCode.isBlank() ? "BUSINESS_FAILURE" : failureCode.trim();
        this.httpStatus = httpStatus;
    }

    /** 업무 실패 코드를 반환합니다. */
    public String failureCode() { return failureCode; }

    /** Transport가 제공한 HTTP 상태를 반환합니다. */
    public Integer httpStatus() { return httpStatus; }
}
