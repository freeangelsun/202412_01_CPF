package fps.cmn.msg.service;

import fps.cmn.msg.dto.CommonMessageRequest;
import fps.cmn.msg.mapper.MessageMapper;
import fps.cmn.ref.service.CacheRefreshEventPublisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * MessageCacheService.java
 *
 * - 메시지 데이터를 캐싱하여 애플리케이션 성능을 향상시킵니다.
 * - 애플리케이션 시작 시 전체 메시지 데이터를 자동으로 캐싱합니다.
 */
@Service
public class MessageCacheService {

    private static final Logger logger = LoggerFactory.getLogger(MessageCacheService.class);
    private static final String CACHE_NAME = "messageCache";
    private final MessageMapper messageMapper;
    private final CacheManager cacheManager;
    private final CacheRefreshEventPublisher cacheRefreshEventPublisher;

    @Value("${fps.cmn.cache.preload-enabled:true}")
    private boolean preloadEnabled;

    public MessageCacheService(
            MessageMapper messageMapper,
            CacheManager cacheManager,
            CacheRefreshEventPublisher cacheRefreshEventPublisher) {
        this.messageMapper = messageMapper;
        this.cacheManager = cacheManager;
        this.cacheRefreshEventPublisher = cacheRefreshEventPublisher;
    }

    /**
     * 모든 메시지 데이터를 캐싱합니다.
     *
     * @return 메시지 데이터 목록
     */
    @Cacheable("messageCache")
    public List<Map<String, Object>> getAllMessages() {
        logger.info("Cache Miss: Fetching all messages from database");
        return messageMapper.findAllMessages();
    }

    /**
     * 특정 메시지 키에 해당하는 메시지 데이터를 반환합니다.
     *
     * @param messageKey 메시지 키
     * @return 메시지 데이터
     */
    @Cacheable(value = "messageCache", key = "#messageKey")
    public Map<String, Object> getMessageByKey(String messageKey) {
        logger.debug("Cache Miss: Fetching message for key: {}", messageKey);
        return messageMapper.findMessageByKey(messageKey);
    }

    /**
     * 메시지 키와 언어 코드로 메시지 한 건을 조회합니다.
     *
     * @param messageKey 메시지 키
     * @param locale 언어 코드
     * @return 메시지 데이터
     */
    public Map<String, Object> getMessageByKeyAndLocale(String messageKey, String locale) {
        return messageMapper.findMessageByKeyAndLocale(messageKey, locale);
    }

    /**
     * 메시지 키, 언어 코드, 메시지 유형으로 메시지 한 건을 조회합니다.
     *
     * <p>오류 메시지는 고객용 EXTERNAL과 내부 로그용 INTERNAL을 분리해 관리합니다.</p>
     *
     * @param messageKey 메시지 키
     * @param locale 언어 코드
     * @param messageType 메시지 유형
     * @return 메시지 데이터
     */
    @Cacheable(value = "messageCache", key = "#messageKey + ':' + #locale + ':' + #messageType")
    public Map<String, Object> getMessageByKeyLocaleType(String messageKey, String locale, String messageType) {
        return messageMapper.findMessageByKeyLocaleType(messageKey, locale, messageType);
    }

    /**
     * 메시지 ID로 메시지 한 건을 조회합니다.
     *
     * @param messageId 메시지 ID
     * @return 메시지 데이터
     */
    public Map<String, Object> getMessageById(Long messageId) {
        return messageMapper.findMessageById(messageId);
    }

    /**
     * 공통 메시지를 등록하고 메시지 캐시를 즉시 리프레시합니다.
     *
     * @param request 등록 요청
     * @return 등록된 메시지 데이터
     */
    @Transactional(transactionManager = "cmnTransactionManager")
    public Map<String, Object> createMessage(CommonMessageRequest request) {
        messageMapper.insertMessage(request);
        refreshMessages();
        publishRefreshEvent("CREATE", eventKey(request.getMessageKey(), request.getLocale(), request.getMessageType()), request.getRequestUser());
        return getMessageById(request.getMessageId());
    }

    /**
     * 공통 메시지를 수정하고 메시지 캐시를 즉시 리프레시합니다.
     *
     * @param messageId 수정할 메시지 ID
     * @param request 수정 요청
     * @return 수정된 메시지 데이터
     */
    @Transactional(transactionManager = "cmnTransactionManager")
    public Map<String, Object> updateMessage(Long messageId, CommonMessageRequest request) {
        messageMapper.updateMessage(messageId, request);
        refreshMessages();
        publishRefreshEvent("UPDATE", eventKey(request.getMessageKey(), request.getLocale(), request.getMessageType()), request.getRequestUser());
        return getMessageById(messageId);
    }

    /**
     * 공통 메시지를 삭제하고 메시지 캐시를 즉시 리프레시합니다.
     *
     * @param messageId 삭제할 메시지 ID
     * @return 최신 메시지 목록
     */
    @Transactional(transactionManager = "cmnTransactionManager")
    public List<Map<String, Object>> deleteMessage(Long messageId) {
        Map<String, Object> beforeDelete = getMessageById(messageId);
        String eventKey = beforeDelete == null
                ? String.valueOf(messageId)
                : eventKey(
                        mapValue(beforeDelete, "messageKey", "message_key"),
                        mapValue(beforeDelete, "locale", "locale"),
                        mapValue(beforeDelete, "messageType", "message_type"));
        messageMapper.deleteMessage(messageId);
        List<Map<String, Object>> latestMessages = refreshMessages();
        publishRefreshEvent("DELETE", eventKey, "SYSTEM");
        return latestMessages;
    }

    /**
     * 캐시를 리로딩합니다.
     *
     * @return 최신 메시지 데이터 목록
     */
    @CachePut("messageCache")
    public List<Map<String, Object>> reloadMessages() {
        return refreshMessages();
    }

    /**
     * 메시지 캐시를 즉시 비우고 최신 DB 값을 다시 조회합니다.
     *
     * @return 최신 메시지 데이터 목록
     */
    public List<Map<String, Object>> refreshMessages() {
        logger.info("Cache Refresh: Clearing message cache and fetching updated messages from database");
        clearCache();
        return messageMapper.findAllMessages();
    }

    /**
     * 메시지 캐시를 즉시 리프레시하고 다른 WAS에도 리프레시 이벤트를 전파합니다.
     *
     * @return 최신 메시지 데이터 목록
     */
    public List<Map<String, Object>> refreshMessagesAndPublish() {
        List<Map<String, Object>> latestMessages = refreshMessages();
        publishRefreshEvent("MANUAL_REFRESH", "ALL", "SYSTEM");
        return latestMessages;
    }

    /**
     * 애플리케이션 시작 시 전체 메시지 데이터를 자동으로 캐싱합니다.
     */
    @PostConstruct
    public void loadCacheOnStartup() {
        if (!preloadEnabled) {
            logger.info("Message cache preload skipped");
            return;
        }

        logger.info("Initializing message cache at startup");
        getAllMessages();
    }

    /**
     * 주기적으로 캐시를 리로딩합니다.
     * - 30분마다 자동으로 실행됩니다.
     */
    @Scheduled(
            fixedRateString = "${fps.cmn.cache.periodic-refresh-millis:1800000}",
            initialDelayString = "${fps.cmn.cache.periodic-refresh-initial-delay-millis:1800000}")
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

    private String eventKey(String messageKey, String locale, String messageType) {
        return messageKey + ":" + locale + ":" + messageType;
    }

    private String mapValue(Map<String, Object> source, String camelKey, String snakeKey) {
        Object value = source.get(camelKey);
        if (value == null) {
            value = source.get(snakeKey);
        }
        return value == null ? "" : String.valueOf(value);
    }
}
