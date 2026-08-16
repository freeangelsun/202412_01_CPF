package com.cpf.admin.opr.gateway;

/** ADM→Gateway Owner 호출의 확정 실패와 결과불명을 구분하는 Typed Exception입니다. */
public final class CpfGatewayRemoteCallException extends RuntimeException {
    public enum ResultState { REJECTED, FAILED, UNKNOWN_RESULT }

    private final ResultState resultState;
    private final Integer httpStatus;
    private final boolean reconcileRequired;

    public CpfGatewayRemoteCallException(
            ResultState resultState, Integer httpStatus, boolean reconcileRequired, String message, Throwable cause) {
        super(message, cause);
        this.resultState = resultState;
        this.httpStatus = httpStatus;
        this.reconcileRequired = reconcileRequired;
    }

    public ResultState resultState() { return resultState; }
    public Integer httpStatus() { return httpStatus; }
    public boolean reconcileRequired() { return reconcileRequired; }
}
