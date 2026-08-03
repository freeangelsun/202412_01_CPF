package com.cpf.core.common.servicecall;

/**
 * CPF 서비스 호출 엔진에서 최종 실패로 판정한 호출 예외입니다.
 */
public class CpfServiceCallException extends RuntimeException {
    private final transient ServiceCallResult<?> result;

    public CpfServiceCallException(ServiceCallResult<?> result) {
        super(result == null ? "서비스 호출이 실패했습니다." : result.failureMessage());
        this.result = result;
    }

    public ServiceCallResult<?> getResult() {
        return result;
    }
}
