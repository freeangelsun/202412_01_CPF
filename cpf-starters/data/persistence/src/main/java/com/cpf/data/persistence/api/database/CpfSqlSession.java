package com.cpf.data.persistence.api.database;

import java.util.List;
import java.util.function.Function;

/**
 * Provider-neutral named data operation contract for Generated Domain infrastructure.
 *
 * <p>Business services depend on domain ports. Generated repository adapters may use this
 * contract without exposing MyBatis or JDBC types. Operation identifiers and parameters are
 * CPF-owned values resolved by the selected provider.</p>
 */
/** CpfSqlSession 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public interface CpfSqlSession {
    <T> List<T> selectList(String operationId, Object parameter);

    <T> T selectOne(String operationId, Object parameter);

    int insert(String operationId, Object parameter);

    int update(String operationId, Object parameter);

    int delete(String operationId, Object parameter);

    /** Executes work and always marks the enclosing transaction rollback-only. */
    // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
    <T> T inRollbackOnlyTransaction(Function<CpfSqlSession, T> callback);
}
