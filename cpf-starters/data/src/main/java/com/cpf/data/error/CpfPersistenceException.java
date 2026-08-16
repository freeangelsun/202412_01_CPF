package com.cpf.data.error;

import com.cpf.core.api.error.CpfErrorCode;
import com.cpf.core.api.error.CpfSystemException;
import java.util.Objects;

/** JDBC/MyBatis/JPA Provider 실패는 Data Owner에서 분류하고 Core 오류 의미로 정규화합니다. */
public class CpfPersistenceException extends CpfSystemException {
    private final CpfPersistenceFailureType failureType;
    private final String operation;

    public CpfPersistenceException(CpfPersistenceFailureType type, String operation, String detail, Throwable cause) {
        super(coreError(type), detail, cause);
        this.failureType = Objects.requireNonNull(type, "failureType");
        this.operation = operation == null ? "" : operation;
    }

    public CpfPersistenceFailureType failureType() { return failureType; }
    public String operation() { return operation; }
    public CpfErrorCode errorCode() { return coreError(failureType); }

    public static CpfErrorCode coreError(CpfPersistenceFailureType type) {
        if (type == null) return CpfErrorCode.DATABASE_ERROR;
        return switch (type) {
            case NOT_FOUND -> CpfErrorCode.NOT_FOUND;
            case OPTIMISTIC_LOCK, PESSIMISTIC_LOCK, CONSTRAINT -> CpfErrorCode.CONFLICT;
            case TIMEOUT, CONNECTION, TRANSIENT -> CpfErrorCode.INFRASTRUCTURE_UNAVAILABLE;
            case QUERY, UNKNOWN -> CpfErrorCode.DATABASE_ERROR;
        };
    }
}
