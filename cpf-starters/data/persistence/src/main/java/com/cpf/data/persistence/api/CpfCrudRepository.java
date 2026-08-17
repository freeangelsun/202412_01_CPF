package com.cpf.data.persistence.api;

import java.util.Optional;
/** Spring Data CrudRepository naming을 따르는 CPF CRUD 계약입니다. */
public interface CpfCrudRepository<T,ID> extends CpfRepositoryPort<T,ID> {
    T save(T value);
    Optional<T> findById(ID id);
    boolean existsById(ID id);
    long count();
    void deleteById(ID id);
}
