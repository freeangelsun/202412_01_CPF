package com.cpf.data.error;

import java.sql.SQLTransientException;
import java.sql.SQLTimeoutException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.SQLRecoverableException;

/** Provider 세부 예외를 Data Owner의 안정적인 실패 분류로 변환합니다. */
public final class CpfPersistenceErrorMapper {
    private CpfPersistenceErrorMapper() { }
    public static CpfPersistenceFailureType classify(Throwable error) {
        Throwable current = error;
        while (current != null) {
            if (current instanceof SQLIntegrityConstraintViolationException) return CpfPersistenceFailureType.CONSTRAINT;
            if (current instanceof SQLTimeoutException) return CpfPersistenceFailureType.TIMEOUT;
            if (current instanceof SQLRecoverableException) return CpfPersistenceFailureType.CONNECTION;
            if (current instanceof SQLTransientException) return CpfPersistenceFailureType.TRANSIENT;
            String name = current.getClass().getSimpleName();
            if (name.contains("OptimisticLock") || name.contains("OptimisticLocking")) return CpfPersistenceFailureType.OPTIMISTIC_LOCK;
            current = current.getCause();
        }
        return CpfPersistenceFailureType.UNKNOWN;
    }
    public static CpfPersistenceException wrap(String operation, Throwable error) {
        return new CpfPersistenceException(classify(error), operation,
                "Persistence operation failed: " + (operation == null ? "unknown" : operation), error);
    }
}
