package com.cpf.data.persistence.api;

import com.cpf.core.api.context.CpfContext;
import com.cpf.core.api.context.CpfContextSnapshot;
import com.cpf.core.api.context.CpfContexts;
import com.cpf.foundation.validation.CpfValidation;

/**
 * JDBC/MyBatis 등 Class 기반 Repository 계층의 Framework 공통 확장점입니다.
 *
 * <p>Domain Common Base가 이 타입을 확장하고 Business Repository가 Domain Base를 확장합니다.
 * JPA에는 DAO 계층을 강제하지 않으며 Context, Validation, statement/paging guard를 제공합니다.</p>
 */
public abstract class CpfBaseRepository {
    protected CpfBaseRepository() { }

    protected final CpfContext requireCurrentContext() { return CpfContexts.requireCurrent(); }
    protected final CpfContextSnapshot requireContext() { return CpfContexts.requireSnapshot(); }
    protected final String requireText(String value, String fieldName) { return CpfValidation.requireText(value, fieldName); }
    /** requireValue 작업을 CPF 표준 계약에 따라 수행한다. */
    protected final <T> T requireValue(T value, String fieldName) { return CpfValidation.requireValue(value, fieldName); }
    protected final void requireRule(boolean condition, String message) { CpfValidation.require(condition, message); }

    protected final String statementId(String namespace, String statement) {
        return requireText(namespace, "namespace") + "." + requireText(statement, "statement");
    }

    /** boundedSize 작업을 CPF 표준 계약에 따라 수행한다. */
    protected final int boundedSize(int requested, int defaultValue, int maxValue) {
        requireRule(defaultValue > 0, "defaultValue must be greater than 0");
        requireRule(maxValue >= defaultValue, "maxValue must be greater than or equal to defaultValue");
        int value = requested <= 0 ? defaultValue : requested;
        return Math.min(value, maxValue);
    }

    /** executionFacts 작업을 CPF 표준 계약에 따라 수행한다. */
    protected final CpfPersistenceExecutionFacts executionFacts(String operation) {
        CpfContext context = requireCurrentContext();
        return new CpfPersistenceExecutionFacts(
                requireText(operation, "operation"),
                context.transactionId(), context.executionId(), context.actorId(), context.tenantId());
    }

    public record CpfPersistenceExecutionFacts(
            String operation, String transactionId, String executionId, String actorId, String tenantId) { }
}
