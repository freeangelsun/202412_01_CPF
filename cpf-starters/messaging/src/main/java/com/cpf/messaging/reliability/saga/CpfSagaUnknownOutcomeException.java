package com.cpf.messaging.reliability.saga;

/** 외부 side effect 결과를 확정할 수 없어 자동 재시도/보상을 금지해야 하는 실패입니다. */
public final class CpfSagaUnknownOutcomeException extends RuntimeException {
    public CpfSagaUnknownOutcomeException(String message) {
        super(message);
    }

    public CpfSagaUnknownOutcomeException(String message, Throwable cause) {
        super(message, cause);
    }
}
