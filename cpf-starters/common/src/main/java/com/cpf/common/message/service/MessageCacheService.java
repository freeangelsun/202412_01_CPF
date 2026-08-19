package com.cpf.common.message.service;

import com.cpf.foundation.api.CpfBaseService;

import com.cpf.common.message.dto.CommonMessageRequest;
import com.cpf.common.message.mapper.MessageMapper;
import com.cpf.common.code.reference.service.CacheRefreshEventPublisher;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/** CPF 공통 메시지 cache를 commit-safe snapshot 방식으로 관리합니다. */
@Deprecated(forRemoval = false)
public class MessageCacheService extends CpfBaseService {
    private static final Logger logger = LoggerFactory.getLogger(MessageCacheService.class);
    private static final String CACHE_NAME = "messageCache";
    private static final String ALL_KEY = "ALL";
    private static final String KEY_PREFIX = "KEY:";
    private static final String KLT_PREFIX = "KLT:";

    private final MessageMapper messageMapper;
    private final CacheManager cacheManager;
    private final CacheRefreshEventPublisher cacheRefreshEventPublisher;
    private final Clock clock;
    private final AtomicLong cacheVersion = new AtomicLong();
    private volatile Instant lastSynchronizedAt;
    private volatile String lastRefreshFailure;

    @Value("${cpf.cmn.cache.preload-enabled:true}")
    private boolean preloadEnabled;
    @Value("${cpf.cmn.cache.fail-fast-on-startup:false}")
    private boolean failFastOnStartup;

    public MessageCacheService(MessageMapper messageMapper, CacheManager cacheManager,
            CacheRefreshEventPublisher cacheRefreshEventPublisher) {
        this(messageMapper, cacheManager, cacheRefreshEventPublisher, Clock.systemUTC());
    }

    @org.springframework.beans.factory.annotation.Autowired
    public MessageCacheService(MessageMapper messageMapper, CacheManager cacheManager,
            CacheRefreshEventPublisher cacheRefreshEventPublisher, Clock clock) {
        this.messageMapper = messageMapper;
        this.cacheManager = cacheManager;
        this.cacheRefreshEventPublisher = cacheRefreshEventPublisher;
        this.clock = java.util.Objects.requireNonNull(clock, "clock");
    }

    @Cacheable(value = CACHE_NAME, key = "'ALL'")
    public List<Map<String, Object>> getAllMessages() {
        logger.info("Cache Miss: Fetching all messages from database");
        return messageMapper.findAllMessages();
    }

    @Cacheable(value = CACHE_NAME, key = "'KEY:' + #p0")
    public Map<String, Object> getMessageByKey(String messageKey) {
        return messageMapper.findMessageByKey(messageKey);
    }

    public Map<String, Object> getMessageByKeyAndLocale(String messageKey, String locale) {
        return messageMapper.findMessageByKeyAndLocale(messageKey, locale);
    }

    @Cacheable(value = CACHE_NAME, key = "'KLT:' + #p0 + ':' + #p1 + ':' + #p2")
    public Map<String, Object> getMessageByKeyLocaleType(String messageKey, String locale, String messageType) {
        return messageMapper.findMessageByKeyLocaleType(messageKey, locale, messageType);
    }

    public Map<String, Object> getMessageById(Long messageId) {
        return messageMapper.findMessageById(messageId);
    }

    @Transactional(transactionManager = "cpfCommonTransactionManager")
    public Map<String, Object> createMessage(CommonMessageRequest request) {
        messageMapper.insertMessage(request);
        Map<String, Object> created = getMessageById(request.getMessageId());
        scheduleSnapshotAfterCommit(messageMapper.findAllMessages());
        publishRefreshEvent("CREATE", eventKey(request.getEffectiveMessageCode(), request.getLocale()), request.getRequestUser());
        return created;
    }

    @Transactional(transactionManager = "cpfCommonTransactionManager")
    public Map<String, Object> updateMessage(Long messageId, CommonMessageRequest request) {
        messageMapper.updateMessage(messageId, request);
        Map<String, Object> updated = getMessageById(messageId);
        scheduleSnapshotAfterCommit(messageMapper.findAllMessages());
        publishRefreshEvent("UPDATE", eventKey(request.getEffectiveMessageCode(), request.getLocale()), request.getRequestUser());
        return updated;
    }

    @Transactional(transactionManager = "cpfCommonTransactionManager")
    public List<Map<String, Object>> deleteMessage(Long messageId) {
        Map<String, Object> beforeDelete = getMessageById(messageId);
        String key = beforeDelete == null ? String.valueOf(messageId)
                : eventKey(mapValue(beforeDelete, "messageCode", "message_code"), mapValue(beforeDelete, "locale", "locale"));
        messageMapper.deleteMessage(messageId);
        List<Map<String, Object>> latest = messageMapper.findAllMessages();
        scheduleSnapshotAfterCommit(latest);
        publishRefreshEvent("DELETE", key, "SYSTEM");
        return latest;
    }

    public List<Map<String, Object>> reloadMessages() {
        return refreshMessages();
    }

    public List<Map<String, Object>> refreshMessages() {
        List<Map<String, Object>> latest = messageMapper.findAllMessages();
        replaceSnapshot(latest);
        return latest;
    }

    public List<Map<String, Object>> refreshMessagesAndPublish() {
        List<Map<String, Object>> latest = refreshMessages();
        publishRefreshEvent("MANUAL_REFRESH", "ALL", "SYSTEM");
        return latest;
    }

    public Map<String, Object> cacheStatus() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("cacheName", CACHE_NAME);
        status.put("version", cacheVersion.get());
        status.put("lastSynchronizedAt", lastSynchronizedAt == null ? null : lastSynchronizedAt.toString());
        status.put("lastRefreshFailure", lastRefreshFailure);
        return status;
    }

    @PostConstruct
    public void loadCacheOnStartup() {
        if (!preloadEnabled) return;
        try {
            refreshMessages();
        } catch (RuntimeException ex) {
            recordFailure(ex);
            if (failFastOnStartup) throw ex;
            logger.warn("Message cache preload failed. Existing cache is preserved.", ex);
        }
    }

    @Scheduled(fixedRateString = "${cpf.cmn.cache.periodic-refresh-millis:1800000}",
            initialDelayString = "${cpf.cmn.cache.periodic-refresh-initial-delay-millis:1800000}")
    public void scheduledReloadMessages() {
        try {
            refreshMessages();
        } catch (RuntimeException ex) {
            recordFailure(ex);
            logger.warn("Scheduled message cache reload failed. Existing cache is preserved.", ex);
        }
    }

    private void scheduleSnapshotAfterCommit(List<Map<String, Object>> latest) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            replaceSnapshot(latest);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCommit() { replaceSnapshot(latest); }
        });
    }

    private void replaceSnapshot(List<Map<String, Object>> latest) {
        Cache cache = requireCache();
        cache.clear();
        cache.put(ALL_KEY, latest);
        cacheVersion.incrementAndGet();
        lastSynchronizedAt = clock.instant();
        lastRefreshFailure = null;
    }

    private Cache requireCache() {
        Cache cache = cacheManager.getCache(CACHE_NAME);
        if (cache == null) throw new IllegalStateException("Required cache is not configured: " + CACHE_NAME);
        return cache;
    }

    private void recordFailure(RuntimeException ex) { lastRefreshFailure = ex.getClass().getSimpleName(); }
    private void publishRefreshEvent(String type, String key, String user) {
        cacheRefreshEventPublisher.publishRequired(CACHE_NAME, type, key, user);
    }
    private String eventKey(String messageKey, String locale) { return messageKey + ":" + locale; }
    private String mapValue(Map<String, Object> source, String camelKey, String snakeKey) {
        Object value = source.get(camelKey);
        if (value == null) value = source.get(snakeKey);
        return value == null ? "" : String.valueOf(value);
    }
}
