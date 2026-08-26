package com.cpf.data.persistence.api;

import java.util.Optional;

/** 실제 invariant 보호가 필요한 Domain Repository가 선택하는 잠금 계약입니다. */
public interface CpfLockingRepositoryPort<T, ID> extends CpfRepositoryPort<T, ID> {
    Optional<T> findById(ID id, CpfLockMode lockMode);
}
