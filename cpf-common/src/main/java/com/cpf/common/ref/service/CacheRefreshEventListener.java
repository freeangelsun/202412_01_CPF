package cpf.cmn.ref.service;

import cpf.cmn.cde.service.CodeCacheService;
import cpf.cmn.cfg.service.ConfigCacheService;
import cpf.cmn.msg.service.MessageCacheService;
import cpf.cmn.msg.service.ResponseCodeCacheService;
import cpf.cmn.ref.mapper.CacheRefreshEventMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * CPF 기능 설명입니다.
 *
 * CPF 기능 설명입니다.
 * CPF 기능 설명입니다.
 */
@Service
public class CacheRefreshEventListener {
    private static final Logger logger = LoggerFactory.getLogger(CacheRefreshEventListener.class);

    private final CacheRefreshEventMapper cacheRefreshEventMapper;
    private final CodeCacheService codeCacheService;
    private final MessageCacheService messageCacheService;
    private final ResponseCodeCacheService responseCodeCacheService;
    private final ConfigCacheService configCacheService;

    private long lastEventId;

    @Value("${cpf.cmn.cache.event-poll-enabled:true}")
    private boolean eventPollEnabled;

    public CacheRefreshEventListener(
            CacheRefreshEventMapper cacheRefreshEventMapper,
            CodeCacheService codeCacheService,
            MessageCacheService messageCacheService,
            ResponseCodeCacheService responseCodeCacheService,
            ConfigCacheService configCacheService) {
        this.cacheRefreshEventMapper = cacheRefreshEventMapper;
        this.codeCacheService = codeCacheService;
        this.messageCacheService = messageCacheService;
        this.responseCodeCacheService = responseCodeCacheService;
        this.configCacheService = configCacheService;
    }

    /**
     * CPF 기능 설명입니다.
     */
    @PostConstruct
    public void initializeLastEventId() {
        if (!eventPollEnabled) {
            logger.info("CMN cache refresh event polling disabled");
            return;
        }

        try {
            Long maxEventId = cacheRefreshEventMapper.findMaxEventId();
            lastEventId = maxEventId == null ? 0L : maxEventId;
            logger.info("CMN cache refresh event listener started. lastEventId={}", lastEventId);
        } catch (RuntimeException ex) {
            logger.warn("CMN cache refresh event listener start skipped. reason={}", ex.getMessage());
        }
    }

    /**
     * CPF 기능 설명입니다.
     */
    @Scheduled(
            fixedDelayString = "${cpf.cmn.cache.refresh-poll-millis:5000}",
            initialDelayString = "${cpf.cmn.cache.refresh-initial-delay-millis:5000}")
    public void pollRefreshEvents() {
        if (!eventPollEnabled) {
            return;
        }

        try {
            List<Map<String, Object>> events = cacheRefreshEventMapper.findEventsAfter(lastEventId);
            for (Map<String, Object> event : events) {
                long eventId = asLong(event.get("eventId"));
                String cacheName = asString(event.get("cacheName"));
                refreshCache(cacheName);
                lastEventId = Math.max(lastEventId, eventId);
            }
        } catch (RuntimeException ex) {
            logger.warn("CMN cache refresh event polling skipped. reason={}", ex.getMessage());
        }
    }

    private void refreshCache(String cacheName) {
        if ("codeCache".equals(cacheName)) {
            codeCacheService.refreshCodes();
        } else if ("messageCache".equals(cacheName)) {
            messageCacheService.refreshMessages();
        } else if ("responseCodeCache".equals(cacheName)) {
            responseCodeCacheService.refreshResponseCodes();
        } else if ("configCache".equals(cacheName)) {
            configCacheService.refreshConfigs();
        } else {
            logger.warn("Unknown CMN cache refresh event received. cacheName={}", cacheName);
        }
    }

    private long asLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }

    private String asString(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}

