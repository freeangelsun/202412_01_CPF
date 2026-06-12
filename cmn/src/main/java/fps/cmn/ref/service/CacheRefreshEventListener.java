package fps.cmn.ref.service;

import fps.cmn.cde.service.CodeCacheService;
import fps.cmn.cfg.service.ConfigCacheService;
import fps.cmn.msg.service.MessageCacheService;
import fps.cmn.ref.mapper.CacheRefreshEventMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * CMN 캐시 리프레시 이벤트를 감지해 현재 WAS의 로컬 캐시를 갱신하는 리스너입니다.
 *
 * <p>외부 메시지 브로커를 아직 도입하지 않은 단계에서도 다중 WAS 캐시를 맞출 수 있도록
 * DB 이벤트 테이블을 짧은 주기로 확인합니다. 운영에서 Kafka, Redis Pub/Sub,
 * Spring Cloud Bus 같은 도구를 도입하면 이 클래스의 조회 부분만 교체하면 됩니다.</p>
 */
@Service
public class CacheRefreshEventListener {
    private static final Logger logger = LoggerFactory.getLogger(CacheRefreshEventListener.class);

    private final CacheRefreshEventMapper cacheRefreshEventMapper;
    private final CodeCacheService codeCacheService;
    private final MessageCacheService messageCacheService;
    private final ConfigCacheService configCacheService;

    private long lastEventId;

    @Value("${fps.cmn.cache.event-poll-enabled:true}")
    private boolean eventPollEnabled;

    public CacheRefreshEventListener(
            CacheRefreshEventMapper cacheRefreshEventMapper,
            CodeCacheService codeCacheService,
            MessageCacheService messageCacheService,
            ConfigCacheService configCacheService) {
        this.cacheRefreshEventMapper = cacheRefreshEventMapper;
        this.codeCacheService = codeCacheService;
        this.messageCacheService = messageCacheService;
        this.configCacheService = configCacheService;
    }

    /**
     * 기동 시점 이전 이벤트는 이미 지난 변경으로 보고, 마지막 이벤트 ID부터 감시를 시작합니다.
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
     * 새 캐시 변경 이벤트를 조회하고, 이벤트에 맞는 로컬 캐시를 비웁니다.
     */
    @Scheduled(
            fixedDelayString = "${fps.cmn.cache.refresh-poll-millis:5000}",
            initialDelayString = "${fps.cmn.cache.refresh-initial-delay-millis:5000}")
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
