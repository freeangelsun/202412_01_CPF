package cpf.cmn.ref.service;

import cpf.cmn.ref.mapper.CacheRefreshEventMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * CPF 기능 설명입니다.
 *
 * CPF 기능 설명입니다.
 * CPF 기능 설명입니다.
 */
@Service
public class CacheRefreshEventPublisher {
    private static final Logger logger = LoggerFactory.getLogger(CacheRefreshEventPublisher.class);

    private final CacheRefreshEventMapper cacheRefreshEventMapper;

    @Value("${cpf.framework.was-id:local}")
    private String wasId;

    public CacheRefreshEventPublisher(CacheRefreshEventMapper cacheRefreshEventMapper) {
        this.cacheRefreshEventMapper = cacheRefreshEventMapper;
    }

    /**
     * CPF 기능 설명입니다.
     *
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
     *
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
     */
    @Transactional(transactionManager = "cmnTransactionManager", propagation = Propagation.REQUIRES_NEW)
    public void publish(String cacheName, String eventType, String eventKey, String requestUser) {
        publishNow(cacheName, eventType, eventKey, requestUser);
    }

    /**
     * CPF 기능 설명입니다.
     *
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
     *
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
     */
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

