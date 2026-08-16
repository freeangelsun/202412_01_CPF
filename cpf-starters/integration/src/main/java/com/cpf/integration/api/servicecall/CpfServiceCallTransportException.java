package com.cpf.integration.api.servicecall;

/**
 * Service Call Engine이 특정 HTTP Client 구현에 종속되지 않고 retry/unknown-result를 판정하도록 하는
 * topology-independent 전송 실패 계약입니다.
 */
public class CpfServiceCallTransportException extends RuntimeException {
    private final Integer httpStatus;
    private final boolean retryable;
    private final boolean unknownResult;

    public CpfServiceCallTransportException(
            String message,
            Integer httpStatus,
            boolean retryable,
            boolean unknownResult,
            Throwable cause) {
        super(message, cause);
        this.httpStatus = httpStatus;
        this.retryable = retryable;
        this.unknownResult = unknownResult;
    }

    /** CpfServiceCallTransportException 작업을 CPF 표준 계약에 따라 수행한다. */
    public CpfServiceCallTransportException(
            String message,
            Integer httpStatus,
            boolean retryable,
            boolean unknownResult) {
        this(message, httpStatus, retryable, unknownResult, null);
    }

    /** httpStatus 작업을 CPF 표준 계약에 따라 수행한다. */
    public Integer httpStatus() {
        return httpStatus;
    }

    public boolean retryable() {
        return retryable;
    }

    /** unknownResult 작업을 CPF 표준 계약에 따라 수행한다. */
    public boolean unknownResult() {
        return unknownResult;
    }
}
