package com.cpf.core.api.persistence;

import com.cpf.core.api.base.CpfRepositoryPort;
import java.util.Optional;

/**
 * 단순 aggregate/entity가 선택적으로 사용하는 공통 CRUD persistence 계약입니다.
 * <p>업무별 복잡 JOIN, 집계, vendor hint, 특수 SQL은 이 계약으로 강제하지 않고
 * Domain Repository Port와 provider native API로 확장합니다.</p>
 * @param <T> 저장 대상 형식
 * @param <ID> 식별자 형식
 */
public interface CpfCrudRepositoryPort<T, ID> extends CpfRepositoryPort<T, ID> {
    T create(T value);
    Optional<T> findById(ID id);
    T update(T value);
    boolean deleteById(ID id);
    boolean existsById(ID id);
    long count();

    /** Provider 고유 고급 API가 필요한 경우 사용할 escape hatch입니다. */
    default Object nativeRepository() { return this; }
}
