package com.cpf.common.ref.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 다중 WAS cache 동기화를 위한 DB refresh event를 업무 commit 이후 발행합니다.
 * DB 일시 장애는 업무 transaction을 되돌리지 않고 bounded retry queue에 보존하며 운영 상태로 노출합니다.
 */
@Service
@ConditionalOnExpression("'${cpf.common.runtime-mode:product}'.toLowerCase() == 'product'")
public class CacheRefreshEventPublisher {
    private static final Logger logger = LoggerFactory.getLogger(CacheRefreshEventPublisher.class);

    private final CacheRefreshEventStore store;
    private final Object retryLock = new Object();
    private final ArrayDeque<PendingEvent> retryQueue = new ArrayDeque<>();
    private final AtomicLong publishedCount = new AtomicLong();
    private final AtomicLong retriedCount = new AtomicLong();
    private final AtomicLong failedCount = new AtomicLong();
    private final AtomicLong droppedCount = new AtomicLong();

    private volatile String lastFailureType;
    private volatile Instant lastPublishedAt;

    @Value("${cpf.framework.was-id:local}")
    private String wasId;
    @Value("${cpf.cmn.cache.refresh-event-retry-capacity:1000}")
    private int retryCapacity;
    @Value("${cpf.cmn.cache.refresh-event-retry-batch:100}")
    private int retryBatchSize;

    public CacheRefreshEventPublisher(CacheRefreshEventStore store) {
        this.store = store;
    }

    /** 즉시 발행합니다. 실패 시 업무 예외로 전파하지 않고 retry queue에 보존합니다. */
    public void publish(String cacheName, String eventType, String eventKey, String requestUser) {
        publishOrQueue(new PendingEvent(cacheName, eventType, eventKey, normalizeUser(requestUser), 0));
    }

    /** 업무 transaction이 commit된 경우에만 event를 발행합니다. */
    public void publishAfterCommit(String cacheName, String eventType, String eventKey, String requestUser) {
        PendingEvent event = new PendingEvent(cacheName, eventType, eventKey, normalizeUser(requestUser), 0);
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    publishOrQueue(event);
                }
            });
            return;
        }
        publishOrQueue(event);
    }

    @Scheduled(fixedDelayString = "${cpf.cmn.cache.refresh-event-retry-millis:10000}")
    public void retryPendingEvents() {
        int limit = Math.max(1, retryBatchSize);
        for (int i = 0; i < limit; i++) {
            PendingEvent event;
            synchronized (retryLock) {
                event = retryQueue.pollFirst();
            }
            if (event == null) return;
            try {
                store.insert(event.cacheName(), event.eventType(), event.eventKey(), wasId, event.publishedBy());
                publishedCount.incrementAndGet();
                retriedCount.incrementAndGet();
                lastPublishedAt = Instant.now();
                lastFailureType = null;
            } catch (RuntimeException ex) {
                failedCount.incrementAndGet();
                lastFailureType = ex.getClass().getSimpleName();
                enqueue(event.nextAttempt());
                logger.warn("CMN cache refresh event retry failed. cacheName={}, eventType={}, eventKey={}, failureType={}",
                        event.cacheName(), event.eventType(), event.eventKey(), lastFailureType);
                return;
            }
        }
    }

    public Map<String, Object> status() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("publishedCount", publishedCount.get());
        status.put("retriedCount", retriedCount.get());
        status.put("failedCount", failedCount.get());
        status.put("droppedCount", droppedCount.get());
        synchronized (retryLock) {
            status.put("pendingCount", retryQueue.size());
        }
        status.put("lastPublishedAt", lastPublishedAt == null ? null : lastPublishedAt.toString());
        status.put("lastFailureType", lastFailureType);
        return status;
    }

    private void publishOrQueue(PendingEvent event) {
        try {
            store.insert(event.cacheName(), event.eventType(), event.eventKey(), wasId, event.publishedBy());
            publishedCount.incrementAndGet();
            lastPublishedAt = Instant.now();
            lastFailureType = null;
        } catch (RuntimeException ex) {
            failedCount.incrementAndGet();
            lastFailureType = ex.getClass().getSimpleName();
            enqueue(event.nextAttempt());
            logger.warn("CMN cache refresh event queued for retry. cacheName={}, eventType={}, eventKey={}, failureType={}",
                    event.cacheName(), event.eventType(), event.eventKey(), lastFailureType);
        }
    }

    private void enqueue(PendingEvent event) {
        synchronized (retryLock) {
            int capacity = Math.max(1, retryCapacity);
            if (retryQueue.size() >= capacity) {
                retryQueue.pollFirst();
                droppedCount.incrementAndGet();
            }
            retryQueue.offerLast(event);
        }
    }

    private String normalizeUser(String user) {
        return user == null || user.isBlank() ? "SYSTEM" : user;
    }

    private record PendingEvent(String cacheName, String eventType, String eventKey, String publishedBy, int attempt) {
        PendingEvent nextAttempt() {
            return new PendingEvent(cacheName, eventType, eventKey, publishedBy, attempt + 1);
        }
    }
}
