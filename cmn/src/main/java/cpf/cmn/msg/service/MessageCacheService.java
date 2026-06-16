package cpf.cmn.msg.service;

import cpf.cmn.msg.dto.CommonMessageRequest;
import cpf.cmn.msg.mapper.MessageMapper;
import cpf.cmn.ref.service.CacheRefreshEventPublisher;
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
 * - 硫붿떆吏 ?곗씠?곕? 罹먯떛?섏뿬 ?좏뵆由ъ??댁뀡 ?깅뒫???μ긽?쒗궢?덈떎.
 * - ?좏뵆由ъ??댁뀡 ?쒖옉 ???꾩껜 硫붿떆吏 ?곗씠?곕? ?먮룞?쇰줈 罹먯떛?⑸땲??
 */
@Service
public class MessageCacheService {

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

    /**
     * 紐⑤뱺 硫붿떆吏 ?곗씠?곕? 罹먯떛?⑸땲??
     *
     * @return 硫붿떆吏 ?곗씠??紐⑸줉
     */
    @Cacheable("messageCache")
    public List<Map<String, Object>> getAllMessages() {
        logger.info("Cache Miss: Fetching all messages from database");
        return messageMapper.findAllMessages();
    }

    /**
     * ?뱀젙 硫붿떆吏 ?ㅼ뿉 ?대떦?섎뒗 硫붿떆吏 ?곗씠?곕? 諛섑솚?⑸땲??
     *
     * @param messageKey 硫붿떆吏 ??
     * @return 硫붿떆吏 ?곗씠??
     */
    @Cacheable(value = "messageCache", key = "#p0")
    public Map<String, Object> getMessageByKey(String messageKey) {
        logger.debug("Cache Miss: Fetching message for key: {}", messageKey);
        return messageMapper.findMessageByKey(messageKey);
    }

    /**
     * 硫붿떆吏 ?ㅼ? ?몄뼱 肄붾뱶濡?硫붿떆吏 ??嫄댁쓣 議고쉶?⑸땲??
     *
     * @param messageKey 硫붿떆吏 ??     * @param locale ?몄뼱 肄붾뱶
     * @return 硫붿떆吏 ?곗씠??     */
    public Map<String, Object> getMessageByKeyAndLocale(String messageKey, String locale) {
        return messageMapper.findMessageByKeyAndLocale(messageKey, locale);
    }

    /**
     * 硫붿떆吏 ?? ?몄뼱 肄붾뱶, 硫붿떆吏 ?좏삎?쇰줈 硫붿떆吏 ??嫄댁쓣 議고쉶?⑸땲??
     *
     * <p>?ㅻ쪟 硫붿떆吏??怨좉컼??EXTERNAL怨??대? 濡쒓렇??INTERNAL??遺꾨━??愿由ы빀?덈떎.</p>
     *
     * @param messageKey 硫붿떆吏 ??     * @param locale ?몄뼱 肄붾뱶
     * @param messageType 硫붿떆吏 ?좏삎
     * @return 硫붿떆吏 ?곗씠??     */
    @Cacheable(value = "messageCache", key = "#p0 + ':' + #p1 + ':' + #p2")
    public Map<String, Object> getMessageByKeyLocaleType(String messageKey, String locale, String messageType) {
        return messageMapper.findMessageByKeyLocaleType(messageKey, locale, messageType);
    }

    /**
     * 硫붿떆吏 ID濡?硫붿떆吏 ??嫄댁쓣 議고쉶?⑸땲??
     *
     * @param messageId 硫붿떆吏 ID
     * @return 硫붿떆吏 ?곗씠??     */
    public Map<String, Object> getMessageById(Long messageId) {
        return messageMapper.findMessageById(messageId);
    }

    /**
     * 怨듯넻 硫붿떆吏瑜??깅줉?섍퀬 硫붿떆吏 罹먯떆瑜?利됱떆 由ы봽?덉떆?⑸땲??
     *
     * @param request ?깅줉 ?붿껌
     * @return ?깅줉??硫붿떆吏 ?곗씠??     */
    @Transactional(transactionManager = "cmnTransactionManager")
    public Map<String, Object> createMessage(CommonMessageRequest request) {
        messageMapper.insertMessage(request);
        refreshMessages();
        publishRefreshEvent("CREATE", eventKey(request.getEffectiveMessageCode(), request.getLocale()), request.getRequestUser());
        return getMessageById(request.getMessageId());
    }

    /**
     * 怨듯넻 硫붿떆吏瑜??섏젙?섍퀬 硫붿떆吏 罹먯떆瑜?利됱떆 由ы봽?덉떆?⑸땲??
     *
     * @param messageId ?섏젙??硫붿떆吏 ID
     * @param request ?섏젙 ?붿껌
     * @return ?섏젙??硫붿떆吏 ?곗씠??     */
    @Transactional(transactionManager = "cmnTransactionManager")
    public Map<String, Object> updateMessage(Long messageId, CommonMessageRequest request) {
        messageMapper.updateMessage(messageId, request);
        refreshMessages();
        publishRefreshEvent("UPDATE", eventKey(request.getEffectiveMessageCode(), request.getLocale()), request.getRequestUser());
        return getMessageById(messageId);
    }

    /**
     * 怨듯넻 硫붿떆吏瑜???젣?섍퀬 硫붿떆吏 罹먯떆瑜?利됱떆 由ы봽?덉떆?⑸땲??
     *
     * @param messageId ??젣??硫붿떆吏 ID
     * @return 理쒖떊 硫붿떆吏 紐⑸줉
     */
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

    /**
     * 罹먯떆瑜?由щ줈?⑺빀?덈떎.
     *
     * @return 理쒖떊 硫붿떆吏 ?곗씠??紐⑸줉
     */
    @CachePut("messageCache")
    public List<Map<String, Object>> reloadMessages() {
        return refreshMessages();
    }

    /**
     * 硫붿떆吏 罹먯떆瑜?利됱떆 鍮꾩슦怨?理쒖떊 DB 媛믪쓣 ?ㅼ떆 議고쉶?⑸땲??
     *
     * @return 理쒖떊 硫붿떆吏 ?곗씠??紐⑸줉
     */
    public List<Map<String, Object>> refreshMessages() {
        logger.info("Cache Refresh: Clearing message cache and fetching updated messages from database");
        clearCache();
        return messageMapper.findAllMessages();
    }

    /**
     * 硫붿떆吏 罹먯떆瑜?利됱떆 由ы봽?덉떆?섍퀬 ?ㅻⅨ WAS?먮룄 由ы봽?덉떆 ?대깽?몃? ?꾪뙆?⑸땲??
     *
     * @return 理쒖떊 硫붿떆吏 ?곗씠??紐⑸줉
     */
    public List<Map<String, Object>> refreshMessagesAndPublish() {
        List<Map<String, Object>> latestMessages = refreshMessages();
        publishRefreshEvent("MANUAL_REFRESH", "ALL", "SYSTEM");
        return latestMessages;
    }

    /**
     * ?좏뵆由ъ??댁뀡 ?쒖옉 ???꾩껜 硫붿떆吏 ?곗씠?곕? ?먮룞?쇰줈 罹먯떛?⑸땲??
     */
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

    /**
     * 二쇨린?곸쑝濡?罹먯떆瑜?由щ줈?⑺빀?덈떎.
     * - 30遺꾨쭏???먮룞?쇰줈 ?ㅽ뻾?⑸땲??
     */
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

