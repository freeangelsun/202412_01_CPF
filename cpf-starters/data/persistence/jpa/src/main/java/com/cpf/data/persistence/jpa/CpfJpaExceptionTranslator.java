package com.cpf.data.persistence.jpa;

import com.cpf.data.error.CpfPersistenceException;
import com.cpf.data.error.CpfPersistenceFailureType;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.LockTimeoutException;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.PessimisticLockException;
import jakarta.persistence.QueryTimeoutException;

/** JPA provider별 RuntimeException을 CPF persistence 실패 분류로 정규화합니다. */
public final class CpfJpaExceptionTranslator {
    private CpfJpaExceptionTranslator() { }

    public static RuntimeException translate(String operation, RuntimeException cause) {
        if (cause instanceof CpfPersistenceException) return cause;
        CpfPersistenceFailureType type = switch (cause) {
            case EntityNotFoundException _ -> CpfPersistenceFailureType.NOT_FOUND;
            case OptimisticLockException _ -> CpfPersistenceFailureType.OPTIMISTIC_LOCK;
            case PessimisticLockException _ -> CpfPersistenceFailureType.PESSIMISTIC_LOCK;
            case LockTimeoutException _ -> CpfPersistenceFailureType.TIMEOUT;
            case QueryTimeoutException _ -> CpfPersistenceFailureType.TIMEOUT;
            case PersistenceException _ -> CpfPersistenceFailureType.QUERY;
            default -> CpfPersistenceFailureType.UNKNOWN;
        };
        return new CpfPersistenceException(type, operation, "CPF JPA operation failed: " + operation, cause);
    }
}
