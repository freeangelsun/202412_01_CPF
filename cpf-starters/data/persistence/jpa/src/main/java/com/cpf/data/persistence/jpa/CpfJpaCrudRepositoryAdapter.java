package com.cpf.data.persistence.jpa;

import com.cpf.data.persistence.api.CpfCrudRepositoryPort;
import com.cpf.data.persistence.api.CpfLockMode;
import com.cpf.data.persistence.api.CpfLockingRepositoryPort;
import java.util.Objects;
import java.util.Optional;

/** Domain Repository가 상속 또는 위임해서 사용하는 실제 CPF CRUD Consumer입니다. */
public class CpfJpaCrudRepositoryAdapter<T, ID>
        implements CpfCrudRepositoryPort<T, ID>, CpfLockingRepositoryPort<T, ID>, CpfJpaNativeAccess {
    private final Class<T> entityType;
    private final CpfJpaOperations operations;

    public CpfJpaCrudRepositoryAdapter(Class<T> entityType, CpfJpaOperations operations) {
        this.entityType = Objects.requireNonNull(entityType, "entityType");
        this.operations = Objects.requireNonNull(operations, "operations");
    }

    @Override public T create(T value) { return operations.create(value); }
    @Override public Optional<T> findById(ID id) { return operations.findById(entityType, id); }
    @Override public Optional<T> findById(ID id, CpfLockMode lockMode) { return operations.findById(entityType, id, lockMode); }
    @Override public T update(T value) { return operations.update(value); }
    @Override public boolean deleteById(ID id) { return operations.deleteById(entityType, id); }
    @Override public boolean existsById(ID id) { return operations.exists(entityType, id); }
    @Override public long count() { return operations.count(entityType); }
    @Override public jakarta.persistence.EntityManager entityManager() { return operations.entityManager(); }
}
