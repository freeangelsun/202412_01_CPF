package com.cpf.core.api.database;

import java.util.List;
import java.util.function.Function;

/**
 * Provider-neutral named data operation contract for Generated Domain infrastructure.
 *
 * <p>Business services depend on domain ports. Generated repository adapters may use this
 * contract without exposing MyBatis or JDBC types. Operation identifiers and parameters are
 * CPF-owned values resolved by the selected provider.</p>
 */
public interface CpfDataOperations {
    <T> List<T> selectList(String operationId, Object parameter);

    <T> T selectOne(String operationId, Object parameter);

    int insert(String operationId, Object parameter);

    int update(String operationId, Object parameter);

    int delete(String operationId, Object parameter);

    /** Executes work and always marks the enclosing transaction rollback-only. */
    <T> T inRollbackOnlyTransaction(Function<CpfDataOperations, T> callback);
}
