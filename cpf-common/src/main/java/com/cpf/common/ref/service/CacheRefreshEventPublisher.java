package com.cpf.common.ref.service;

import com.cpf.common.ref.mapper.CacheRefreshEventMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/** 캐시 변경을 다른 WAS에 알리기 위한 DB fallback 이벤트를 발행합니다. */
@Service
public class CacheRefreshEventPublisher {
    private static final Logger logger = LoggerFactory.getLogger(CacheRefreshEventPublisher.class);

    private final CacheRefreshEventMapper cacheRefreshEventMapper;

    @Value("${cpf.framework.was-id:local}")
    private String wasId;

    public CacheRefreshEventPublisher(CacheRefreshEventMapper cacheRefreshEventMapper) {
        this.cacheRefreshEventMapper = cacheRefreshEventMapper;
    }

    /** 독립 트랜잭션으로 캐시 갱신 이벤트를 즉시 기록합니다. */
    @Transactional(transactionManager = "cmnTransactionManager", propagation = Propagation.REQUIRES_NEW)
    public void publish(String cacheName, String eventType, String eventKey, String requestUser) {
        publishNow(cacheName, eventType, eventKey, requestUser);
    }

    /** 업무 트랜잭션 커밋 이후 이벤트를 기록해 롤백된 변경이 전파되지 않게 합니다. */
    public void publishAfterCommit(String cacheName, String eventType, String eventKey, String requestUser) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    publishNow(cacheName, eventType, eventKey, requestUser);
                }
            });
            return;
        }

        publish(cacheName, eventType, eventKey, requestUser);
    }

    private void publishNow(String cacheName, String eventType, String eventKey, String requestUser) {
        String publishedBy = hasText(requestUser) ? requestUser : "SYSTEM";
        try {
            cacheRefreshEventMapper.insertEvent(cacheName, eventType, eventKey, wasId, publishedBy);
            logger.info("CMN cache refresh event published. cacheName={}, eventType={}, eventKey={}",
                    cacheName, eventType, eventKey);
        } catch (RuntimeException ex) {
            logger.warn("CMN cache refresh event publish skipped. cacheName={}, eventType={}, eventKey={}, reason={}",
                    cacheName, eventType, eventKey, ex.getMessage());
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}

