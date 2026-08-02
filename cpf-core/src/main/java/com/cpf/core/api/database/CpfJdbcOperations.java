package com.cpf.core.api.database;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * CPF JDBC execution contract used only by infrastructure adapters.
 *
 * <p>The contract hides JdbcTemplate, row mappers and transaction APIs from Generated Domain
 * source. SQL text must come from a CPF vendor catalog rather than business-source literals.</p>
 */
public interface CpfJdbcOperations {
    <T> List<T> queryList(String sql, Map<String, ?> parameters, Class<T> resultType);

    <T> T queryOne(String sql, Map<String, ?> parameters, Class<T> resultType);

    int update(String sql, Map<String, ?> parameters);

    /** Executes work and always marks the enclosing transaction rollback-only. */
    <T> T inRollbackOnlyTransaction(Function<CpfJdbcOperations, T> callback);
}
