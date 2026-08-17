package com.cpf.data.persistence.api.database;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * CPF JDBC execution contract used only by infrastructure adapters.
 *
 * <p>The contract hides JdbcTemplate, row mappers and transaction APIs from Generated Domain
 * source. SQL text must come from a CPF vendor catalog rather than business-source literals.</p>
 */
/** CpfNamedParameterJdbcOperations 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public interface CpfNamedParameterJdbcOperations {
    <T> List<T> query(String sql, Map<String, ?> parameters, Class<T> resultType);

    <T> T queryForObject(String sql, Map<String, ?> parameters, Class<T> resultType);

    int update(String sql, Map<String, ?> parameters);

    /** Executes work and always marks the enclosing transaction rollback-only. */
    <T> T inRollbackOnlyTransaction(Function<CpfNamedParameterJdbcOperations, T> callback);
}
