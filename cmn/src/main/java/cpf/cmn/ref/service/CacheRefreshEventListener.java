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
 * CMN 罹먯떆 由ы봽?덉떆 ?대깽?몃? 媛먯????꾩옱 WAS??濡쒖뺄 罹먯떆瑜?媛깆떊?섎뒗 由ъ뒪?덉엯?덈떎.
 *
 * <p>?몃? 硫붿떆吏 釉뚮줈而ㅻ? ?꾩쭅 ?꾩엯?섏? ?딆? ?④퀎?먯꽌???ㅼ쨷 WAS 罹먯떆瑜?留욎텧 ???덈룄濡? * DB ?대깽???뚯씠釉붿쓣 吏㏃? 二쇨린濡??뺤씤?⑸땲?? ?댁쁺?먯꽌 Kafka, Redis Pub/Sub,
 * Spring Cloud Bus 媛숈? ?꾧뎄瑜??꾩엯?섎㈃ ???대옒?ㅼ쓽 議고쉶 遺遺꾨쭔 援먯껜?섎㈃ ?⑸땲??</p>
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
     * 湲곕룞 ?쒖젏 ?댁쟾 ?대깽?몃뒗 ?대? 吏??蹂寃쎌쑝濡?蹂닿퀬, 留덉?留??대깽??ID遺??媛먯떆瑜??쒖옉?⑸땲??
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
     * ??罹먯떆 蹂寃??대깽?몃? 議고쉶?섍퀬, ?대깽?몄뿉 留욌뒗 濡쒖뺄 罹먯떆瑜?鍮꾩썎?덈떎.
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

