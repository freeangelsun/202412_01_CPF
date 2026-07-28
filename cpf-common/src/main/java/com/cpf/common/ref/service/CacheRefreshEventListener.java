package com.cpf.common.ref.service;

import com.cpf.common.cde.service.CodeCacheService;
import com.cpf.common.cfg.service.ConfigCacheService;
import com.cpf.common.msg.service.MessageCacheService;
import com.cpf.common.msg.service.ResponseCodeCacheService;
import com.cpf.common.ref.mapper.CacheRefreshEventMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * DB durable cache event를 Runtime consumer checkpoint 기준으로 순서대로 재생합니다.
 *
 * <p>신규 인스턴스는 전체 Cache Snapshot을 만들기 <strong>전에</strong> Event high-water mark를
 * 고정합니다. Snapshot 생성 중 발생한 Event는 다음 Poll에서 반드시 재생되므로 Offline 복귀와
 * 초기 기동 Race에서도 Event가 유실되지 않습니다.</p>
 */
@Service
@ConditionalOnExpression("'${cpf.common.runtime-mode:product}'.toLowerCase() == 'product'")
public class CacheRefreshEventListener {
    private static final Logger log = LoggerFactory.getLogger(CacheRefreshEventListener.class);

    private final CacheRefreshEventMapper mapper;
    private final CodeCacheService code;
    private final MessageCacheService message;
    private final ResponseCodeCacheService response;
    private final ConfigCacheService config;

    private long lastEventId;
    private volatile String lastFailureType;
    private volatile Instant lastSuccessfulPollAt;
    private volatile Instant lastFullRefreshAt;

    @Value("${cpf.cmn.cache.event-poll-enabled:true}")
    private boolean enabled;

    @Value("${cpf.framework.was-id:local}")
    private String wasId;

    public CacheRefreshEventListener(
            CacheRefreshEventMapper mapper,
            CodeCacheService code,
            MessageCacheService message,
            ResponseCodeCacheService response,
            ConfigCacheService config) {
        this.mapper = mapper;
        this.code = code;
        this.message = message;
        this.response = response;
        this.config = config;
    }

    @PostConstruct
    public void initialize() {
        if (!enabled) {
            return;
        }

        String consumer = consumerId();
        try {
            Long checkpoint = mapper.findCheckpoint(consumer);
            if (checkpoint == null) {
                // Snapshot 이전의 tail을 고정해야 Snapshot 도중 발생한 Event를 다음 Poll에서 재생할 수 있다.
                Long highWaterMark = mapper.findMaxEventId();
                refreshAll();
                lastFullRefreshAt = Instant.now();
                lastEventId = highWaterMark == null ? 0L : highWaterMark;
                establishCheckpoint(consumer, lastEventId);
            } else {
                lastEventId = checkpoint;
            }
            lastFailureType = null;
            lastSuccessfulPollAt = Instant.now();
        } catch (RuntimeException ex) {
            lastFailureType = ex.getClass().getSimpleName();
            throw new IllegalStateException("Cache refresh durable checkpoint 초기화 실패", ex);
        }
    }

    @Scheduled(
            fixedDelayString = "${cpf.cmn.cache.refresh-poll-millis:5000}",
            initialDelayString = "${cpf.cmn.cache.refresh-initial-delay-millis:5000}")
    public void pollRefreshEvents() {
        if (!enabled) {
            return;
        }

        try {
            List<Map<String, Object>> events = mapper.findEventsAfter(lastEventId);
            long expectedAfter = lastEventId;
            for (Map<String, Object> event : events) {
                long eventId = asLong(event.get("eventId"));
                if (eventId <= expectedAfter) {
                    // Durable replay 중복은 무해하게 무시하되 Cursor를 역전시키지 않는다.
                    continue;
                }
                refreshCache(asString(event.get("cacheName")));
                if (mapper.updateCheckpoint(consumerId(), eventId) != 1) {
                    throw new IllegalStateException("Cache refresh checkpoint update 실패");
                }
                lastEventId = eventId;
                expectedAfter = eventId;
            }
            lastFailureType = null;
            lastSuccessfulPollAt = Instant.now();
        } catch (RuntimeException ex) {
            lastFailureType = ex.getClass().getSimpleName();
            log.warn(
                    "CMN durable cache event polling failed. consumerId={}, lastEventId={}",
                    consumerId(),
                    lastEventId,
                    ex);
        }
    }

    public Map<String, Object> status() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("enabled", enabled);
        status.put("consumerId", consumerId());
        status.put("lastEventId", lastEventId);
        status.put("lastSuccessfulPollAt", toText(lastSuccessfulPollAt));
        status.put("lastFullRefreshAt", toText(lastFullRefreshAt));
        status.put("lastFailureType", lastFailureType);
        status.put("durableCheckpoint", true);
        status.put("initialSnapshotHighWaterBeforeRefresh", true);
        return status;
    }

    private void establishCheckpoint(String consumer, long highWaterMark) {
        try {
            if (mapper.insertCheckpoint(consumer, highWaterMark) == 1) {
                return;
            }
        } catch (RuntimeException concurrentInsert) {
            Long current = mapper.findCheckpoint(consumer);
            if (current == null) {
                throw concurrentInsert;
            }
            lastEventId = current;
            return;
        }

        Long current = mapper.findCheckpoint(consumer);
        if (current == null) {
            throw new IllegalStateException("Cache refresh checkpoint insert 결과가 1이 아닙니다.");
        }
        lastEventId = current;
    }

    private void refreshAll() {
        code.refreshCodes();
        message.refreshMessages();
        response.refreshResponseCodes();
        config.refreshConfigs();
    }

    private void refreshCache(String name) {
        switch (name) {
            case "codeCache" -> code.refreshCodes();
            case "messageCache" -> message.refreshMessages();
            case "responseCodeCache" -> response.refreshResponseCodes();
            case "configCache" -> config.refreshConfigs();
            default -> throw new IllegalArgumentException("Unknown CMN cache refresh event: " + name);
        }
    }

    private String consumerId() {
        return "CMN_CACHE:" + (wasId == null || wasId.isBlank() ? "local" : wasId);
    }

    private long asLong(Object value) {
        return value instanceof Number number
                ? number.longValue()
                : Long.parseLong(String.valueOf(value));
    }

    private String asString(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private String toText(Instant value) {
        return value == null ? null : value.toString();
    }
}
