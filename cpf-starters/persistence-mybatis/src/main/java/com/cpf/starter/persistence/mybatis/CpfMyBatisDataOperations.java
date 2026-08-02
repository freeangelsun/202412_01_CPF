package com.cpf.starter.persistence.mybatis;

import com.cpf.core.api.database.CpfDataOperations;
import com.cpf.core.api.error.CpfSystemException;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.transaction.support.TransactionTemplate;

/** MyBatis internal adapter for the provider-neutral CPF named data operation contract. */
final class CpfMyBatisDataOperations implements CpfDataOperations {
    private final SqlSessionTemplate sessions;
    private final TransactionTemplate transactions;

    CpfMyBatisDataOperations(
            SqlSessionTemplate sessions,
            TransactionTemplate transactions) {
        this.sessions = Objects.requireNonNull(sessions, "sessions");
        this.transactions = Objects.requireNonNull(transactions, "transactions");
    }

    @Override
    public <T> List<T> selectList(String operationId, Object parameter) {
        String required = requireOperation(operationId);
        try {
            return List.copyOf(sessions.selectList(required, parameter));
        } catch (RuntimeException exception) {
            throw failure(required, exception);
        }
    }

    @Override
    public <T> T selectOne(String operationId, Object parameter) {
        String required = requireOperation(operationId);
        try {
            return sessions.selectOne(required, parameter);
        } catch (RuntimeException exception) {
            throw failure(required, exception);
        }
    }

    @Override
    public int insert(String operationId, Object parameter) {
        String required = requireOperation(operationId);
        try {
            return sessions.insert(required, parameter);
        } catch (RuntimeException exception) {
            throw failure(required, exception);
        }
    }

    @Override
    public int update(String operationId, Object parameter) {
        String required = requireOperation(operationId);
        try {
            return sessions.update(required, parameter);
        } catch (RuntimeException exception) {
            throw failure(required, exception);
        }
    }

    @Override
    public int delete(String operationId, Object parameter) {
        String required = requireOperation(operationId);
        try {
            return sessions.delete(required, parameter);
        } catch (RuntimeException exception) {
            throw failure(required, exception);
        }
    }

    @Override
    public <T> T inRollbackOnlyTransaction(Function<CpfDataOperations, T> callback) {
        Objects.requireNonNull(callback, "callback");
        return transactions.execute(status -> {
            T result = callback.apply(this);
            status.setRollbackOnly();
            return result;
        });
    }

    private static String requireOperation(String operationId) {
        if (operationId == null || operationId.isBlank()) {
            throw new IllegalArgumentException("CPF data operationId is required");
        }
        return operationId.trim();
    }

    private static CpfSystemException failure(String operationId, RuntimeException cause) {
        if (cause instanceof CpfSystemException cpfException) {
            return cpfException;
        }
        return new CpfSystemException("CPF MyBatis operation failed: " + operationId, cause);
    }
}
