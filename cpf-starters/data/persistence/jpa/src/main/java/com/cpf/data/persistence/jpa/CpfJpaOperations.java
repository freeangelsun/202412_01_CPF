package com.cpf.data.persistence.jpa;

import com.cpf.data.persistence.api.CpfLockMode;
import com.cpf.data.persistence.api.CpfPersistenceContext;
import com.cpf.data.persistence.api.CpfPersistencePolicy;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * EntityManager 반복 코드를 CPF 정책(query timeout, exception, slow-query, transactionId)과 함께 제공하는 편의 API입니다.
 */
public class CpfJpaOperations implements CpfJpaNativeAccess {
    private final EntityManager entityManager;
    private final CpfJpaProperties properties;
    private final CpfJpaQueryObserver observer;
    private final CpfJpaExecutionContextSupplier contexts;
    private final CpfJpaAuditHook auditHook;

    public CpfJpaOperations(EntityManager entityManager, CpfJpaProperties properties,
            CpfJpaQueryObserver observer, CpfJpaExecutionContextSupplier contexts, CpfJpaAuditHook auditHook) {
        this.entityManager = Objects.requireNonNull(entityManager, "entityManager");
        this.properties = Objects.requireNonNull(properties, "properties");
        this.observer = Objects.requireNonNull(observer, "observer");
        this.contexts = Objects.requireNonNull(contexts, "contexts");
        this.auditHook = Objects.requireNonNull(auditHook, "auditHook");
    }

    @Override public EntityManager entityManager() { return entityManager; }

    public <T> T create(T value) { return observed("create", value.getClass(), () -> { var c=context(); auditHook.before("create", value, c); entityManager.persist(value); auditHook.after("create", value, c); return value; }); }
    public <T> T update(T value) { return observed("update", value.getClass(), () -> { var c=context(); auditHook.before("update", value, c); T merged=entityManager.merge(value); auditHook.after("update", merged, c); return merged; }); }
    public <T, ID> Optional<T> findById(Class<T> type, ID id) { return observed("findById", type, () -> Optional.ofNullable(entityManager.find(type, id))); }
    public <T, ID> Optional<T> findById(Class<T> type, ID id, CpfLockMode lockMode) {
        return observed("findByIdLocked", type, () -> Optional.ofNullable(entityManager.find(type, id, CpfJpaLockModes.toJpa(lockMode))));
    }
    public <T, ID> boolean deleteById(Class<T> type, ID id) {
        return observed("deleteById", type, () -> {
            T value = entityManager.find(type, id);
            if (value == null) return false;
            var c=context(); auditHook.before("delete", value, c); entityManager.remove(value); auditHook.after("delete", value, c);
            return true;
        });
    }
    public <T> long count(Class<T> type) {
        return observed("count", type, () -> entityManager.createQuery("select count(e) from " + entityName(type) + " e", Long.class)
                .setHint("jakarta.persistence.query.timeout", properties.getQueryTimeoutMs())
                .getSingleResult());
    }
    public boolean exists(Class<?> type, Object id) { return findById(type, id).isPresent(); }

    public int bulkPersist(Collection<?> values) {
        Objects.requireNonNull(values, "values");
        CpfPersistencePolicy.requireBulkSize(values.size(), properties.getMaxBulkSize());
        int limit = properties.getBulkFlushSize();
        int count = 0;
        for (Object value : values) {
            entityManager.persist(Objects.requireNonNull(value, "bulk value"));
            count++;
            if (count % limit == 0) { entityManager.flush(); entityManager.clear(); }
        }
        if (count > 0 && count % limit != 0) entityManager.flush();
        return count;
    }

    public int bulkMerge(Collection<?> values) {
        Objects.requireNonNull(values, "values");
        CpfPersistencePolicy.requireBulkSize(values.size(), properties.getMaxBulkSize());
        int count=0;
        for (Object value : values) { update(Objects.requireNonNull(value, "bulk value")); count++; if (count % properties.getBulkFlushSize()==0) { entityManager.flush(); entityManager.clear(); } }
        if (count>0 && count % properties.getBulkFlushSize()!=0) entityManager.flush();
        return count;
    }

    public <T, ID> int bulkDeleteById(Class<T> type, Collection<ID> ids) {
        Objects.requireNonNull(ids, "ids");
        CpfPersistencePolicy.requireBulkSize(ids.size(), properties.getMaxBulkSize());
        int count=0; for (ID id:ids) if (deleteById(type,id)) count++; return count;
    }

    public Query applyTimeout(Query query) {
        return query.setHint("jakarta.persistence.query.timeout", properties.getQueryTimeoutMs());
    }

    private <R> R observed(String operation, Class<?> entityType, Supplier<R> work) {
        long start = System.nanoTime();
        try {
            return work.get();
        } catch (RuntimeException exception) {
            throw CpfJpaExceptionTranslator.translate(operation, exception);
        } finally {
            long elapsed = Math.max(0L, (System.nanoTime() - start) / 1_000_000L);
            CpfPersistenceContext c=context();
            observer.observe(new CpfJpaQueryObservation(operation, entityType.getSimpleName(), elapsed,
                    elapsed >= properties.getSlowQueryThresholdMs(), c.transactionId(), c.tenantId(), c.actorId()));
        }
    }

    private CpfPersistenceContext context() { return contexts.current(); }

    private static String entityName(Class<?> type) {
        jakarta.persistence.Entity annotation = type.getAnnotation(jakarta.persistence.Entity.class);
        return annotation != null && !annotation.name().isBlank() ? annotation.name() : type.getSimpleName();
    }
}
