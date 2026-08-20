package com.cpf.common.runtime.cache;

import com.cpf.foundation.runtime.CpfInstanceIdentity;
import com.cpf.common.spi.CpfCommonCacheChangePublisher;
import com.cpf.common.spi.CpfCommonPersistenceNames;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Common 변경과 같은 transaction에 durable event를 기록하고 local invalidation은 commit 이후 수행합니다.
 */
@Service
public class CpfCommonCacheRefreshPublisher implements CpfCommonCacheChangePublisher {
    private final CpfCommonCacheRefreshEventRepository repository;
    private final CpfCommonCacheRefresher refresher;

    /** durable event Repository와 local cache refresher를 결합합니다. */
    public CpfCommonCacheRefreshPublisher(CpfCommonCacheRefreshEventRepository repository, CpfCommonCacheRefresher refresher) {
        this.repository = repository;
        this.refresher = refresher;
    }

    /** 업무 변경 transaction 안에서 event를 기록하고 commit 이후에만 local cache를 갱신합니다. */
    @Transactional(transactionManager = CpfCommonPersistenceNames.TX_MANAGER_BEAN, propagation = Propagation.MANDATORY)
    @Override
    public long publishRequired(String cacheName, String eventType, String eventKey, String actor) {
        long id = repository.insertEvent(cacheName, eventType, eventKey, normalizedInstance(), normalizeActor(actor));
        if (!TransactionSynchronizationManager.isSynchronizationActive()) throw new IllegalStateException("Common cache invalidation requires transaction synchronization");
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCommit() { refresher.refresh(cacheName); }
        });
        return id;
    }

    /** 복구/운영 경로는 별도 transaction으로 event를 남기되 commit 성공 후에만 local cache를 갱신합니다. */
    @Transactional(transactionManager = CpfCommonPersistenceNames.TX_MANAGER_BEAN, propagation = Propagation.REQUIRES_NEW)
    @Override
    public long publishOutOfBand(String cacheName, String eventType, String eventKey, String actor) {
        long id = repository.insertEvent(cacheName, eventType, eventKey, normalizedInstance(), normalizeActor(actor));
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCommit() { refresher.refresh(cacheName); }
            });
        }
        return id;
    }

    private String normalizedInstance() { return CpfInstanceIdentity.instanceId(); }
    private String normalizeActor(String actor) { return actor == null || actor.isBlank() ? "SYSTEM" : actor.trim(); }
}
