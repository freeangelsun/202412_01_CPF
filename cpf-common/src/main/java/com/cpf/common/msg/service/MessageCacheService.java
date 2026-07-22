package cpf.cmn.msg.service;

import cpf.cmn.msg.dto.CommonMessageRequest;
import cpf.cmn.msg.mapper.MessageMapper;
import cpf.cmn.ref.service.CacheRefreshEventPublisher;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * PFW 공통 메시지 캐시 서비스입니다.
 * 메시지 조회, 등록, 수정, 삭제 후 캐시 초기화와 refresh 이벤트 발행을 함께 처리합니다.
 */
@Service
public class MessageCacheService extends cpf.cmn.common.base.CmnBaseService {

    private static final Logger logger = LoggerFactory.getLogger(MessageCacheService.class);
    private static final String CACHE_NAME = "messageCache";

    private final MessageMapper messageMapper;
    private final CacheManager cacheManager;
    private final CacheRefreshEventPublisher cacheRefreshEventPublisher;

    @Value("${cpf.cmn.cache.preload-enabled:true}")
    private boolean preloadEnabled;

    @Value("${cpf.cmn.cache.fail-fast-on-startup:false}")
    private boolean failFastOnStartup;

    public MessageCacheService(
            MessageMapper messageMapper,
            CacheManager cacheManager,
            CacheRefreshEventPublisher cacheRefreshEventPublisher) {
        this.messageMapper = messageMapper;
        this.cacheManager = cacheManager;
        this.cacheRefreshEventPublisher = cacheRefreshEventPublisher;
    }

    @Cacheable("messageCache")
    public List<Map<String, Object>> getAllMessages() {
        logger.info("Cache Miss: Fetching all messages from database");
        return messageMapper.findAllMessages();
    }

    @Cacheable(value = "messageCache", key = "#p0")
    public Map<String, Object> getMessageByKey(String messageKey) {
        logger.debug("Cache Miss: Fetching message for key: {}", messageKey);
        return messageMapper.findMessageByKey(messageKey);
    }

    public Map<String, Object> getMessageByKeyAndLocale(String messageKey, String locale) {
        return messageMapper.findMessageByKeyAndLocale(messageKey, locale);
    }

    @Cacheable(value = "messageCache", key = "#p0 + ':' + #p1 + ':' + #p2")
    public Map<String, Object> getMessageByKeyLocaleType(String messageKey, String locale, String messageType) {
        return messageMapper.findMessageByKeyLocaleType(messageKey, locale, messageType);
    }

    public Map<String, Object> getMessageById(Long messageId) {
        return messageMapper.findMessageById(messageId);
    }

    @Transactional(transactionManager = "cmnTransactionManager")
    public Map<String, Object> createMessage(CommonMessageRequest request) {
        messageMapper.insertMessage(request);
        refreshMessages();
        publishRefreshEvent("CREATE", eventKey(request.getEffectiveMessageCode(), request.getLocale()), request.getRequestUser());
        return getMessageById(request.getMessageId());
    }

    @Transactional(transactionManager = "cmnTransactionManager")
    public Map<String, Object> updateMessage(Long messageId, CommonMessageRequest request) {
        messageMapper.updateMessage(messageId, request);
        refreshMessages();
        publishRefreshEvent("UPDATE", eventKey(request.getEffectiveMessageCode(), request.getLocale()), request.getRequestUser());
        return getMessageById(messageId);
    }

    @Transactional(transactionManager = "cmnTransactionManager")
    public List<Map<String, Object>> deleteMessage(Long messageId) {
        Map<String, Object> beforeDelete = getMessageById(messageId);
        String eventKey = beforeDelete == null
                ? String.valueOf(messageId)
                : eventKey(
                        mapValue(beforeDelete, "messageCode", "message_code"),
                        mapValue(beforeDelete, "locale", "locale"));
        messageMapper.deleteMessage(messageId);
        List<Map<String, Object>> latestMessages = refreshMessages();
        publishRefreshEvent("DELETE", eventKey, "SYSTEM");
        return latestMessages;
    }

    @CachePut("messageCache")
    public List<Map<String, Object>> reloadMessages() {
        return refreshMessages();
    }

    public List<Map<String, Object>> refreshMessages() {
        logger.info("Cache Refresh: Clearing message cache and fetching updated messages from database");
        clearCache();
        return messageMapper.findAllMessages();
    }

    public List<Map<String, Object>> refreshMessagesAndPublish() {
        List<Map<String, Object>> latestMessages = refreshMessages();
        publishRefreshEvent("MANUAL_REFRESH", "ALL", "SYSTEM");
        return latestMessages;
    }

    @PostConstruct
    public void loadCacheOnStartup() {
        if (!preloadEnabled) {
            logger.info("Message cache preload skipped");
            return;
        }

        logger.info("Initializing message cache at startup");
        try {
            getAllMessages();
        } catch (RuntimeException ex) {
            if (failFastOnStartup) {
                throw ex;
            }
            logger.warn("Message cache preload failed. Application will continue because fail-fast is disabled.", ex);
        }
    }

    @Scheduled(
            fixedRateString = "${cpf.cmn.cache.periodic-refresh-millis:1800000}",
            initialDelayString = "${cpf.cmn.cache.periodic-refresh-initial-delay-millis:1800000}")
    public void scheduledReloadMessages() {
        logger.info("Scheduled cache reload triggered");
        refreshMessages();
    }

    private void clearCache() {
        Cache cache = cacheManager.getCache(CACHE_NAME);
        if (cache != null) {
            cache.clear();
        }
    }

    private void publishRefreshEvent(String eventType, String eventKey, String requestUser) {
        cacheRefreshEventPublisher.publishAfterCommit(CACHE_NAME, eventType, eventKey, requestUser);
    }

    private String eventKey(String messageKey, String locale) {
        return messageKey + ":" + locale;
    }

    private String mapValue(Map<String, Object> source, String camelKey, String snakeKey) {
        Object value = source.get(camelKey);
        if (value == null) {
            value = source.get(snakeKey);
        }
        return value == null ? "" : String.valueOf(value);
    }
}
