package com.cpf.core.api.async;
/** cooperative cancellation을 확인한 Handler가 side effect 중단 후 던지는 표준 취소 신호입니다. */
public final class CpfAsyncCancelledException extends RuntimeException {
    public CpfAsyncCancelledException(String message) { super(message); }
}
