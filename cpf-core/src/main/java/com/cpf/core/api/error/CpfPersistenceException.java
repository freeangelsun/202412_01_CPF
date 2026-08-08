package com.cpf.core.api.error;

import java.util.Objects;

/** JDBC/MyBatis/JPA의 Provider 예외를 CPF 표준 실패 분류로 전달합니다. */
public class CpfPersistenceException extends CpfSystemException {
    private final CpfPersistenceFailureType failureType;
    private final String operation;

    public CpfPersistenceException(CpfPersistenceFailureType failureType, String operation, String detail, Throwable cause) {
        super(detail, cause);
        this.failureType = Objects.requireNonNull(failureType, "failureType");
        this.operation = operation == null ? "" : operation;
    }

    public CpfPersistenceFailureType failureType() { return failureType; }
    public String operation() { return operation; }
}
