package com.cpf.common.ref.service;

import com.cpf.common.ref.mapper.CacheRefreshEventMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** 캐시 refresh event 저장만 소유하여 실제 REQUIRES_NEW proxy 경계를 보장합니다. */
@Service
public class CacheRefreshEventStore {
    private final CacheRefreshEventMapper mapper;

    public CacheRefreshEventStore(CacheRefreshEventMapper mapper) {
        this.mapper = mapper;
    }

    @Transactional(transactionManager = "cmnTransactionManager", propagation = Propagation.REQUIRES_NEW)
    public void insert(String cacheName, String eventType, String eventKey, String wasId, String publishedBy) {
        mapper.insertEvent(cacheName, eventType, eventKey, wasId, publishedBy);
    }
}
