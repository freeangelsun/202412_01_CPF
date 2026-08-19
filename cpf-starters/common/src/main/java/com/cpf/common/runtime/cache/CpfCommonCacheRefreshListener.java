package com.cpf.common.runtime.cache;

import com.cpf.foundation.runtime.CpfInstanceIdentity;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/** Durable event + checkpoint 기반 Common multi-instance cache version fence consumer입니다. */
@Service
public class CpfCommonCacheRefreshListener {
    private static final Logger log = LoggerFactory.getLogger(CpfCommonCacheRefreshListener.class);
    private final CpfCommonCacheRefreshEventRepository repository;
    private final CpfCommonCacheRefresher refresher;
    private final Clock clock;
    @Value("${cpf.common.cache.event-poll-enabled:true}") private boolean enabled;
    @Value("${cpf.common.cache.event-poll-limit:256}") private int pollLimit;
    private volatile long lastEventId;
    private volatile Instant lastSuccess;
    private volatile String lastFailureType;

    /** durable event Repository와 실제 cache refresher를 주입받아 replay consumer를 구성합니다. */
    public CpfCommonCacheRefreshListener(CpfCommonCacheRefreshEventRepository repository, CpfCommonCacheRefresher refresher) {
        this(repository, refresher, Clock.systemUTC());
    }

    @org.springframework.beans.factory.annotation.Autowired
    public CpfCommonCacheRefreshListener(CpfCommonCacheRefreshEventRepository repository, CpfCommonCacheRefresher refresher, Clock clock) {
        this.repository = repository;
        this.refresher = refresher;
        this.clock = java.util.Objects.requireNonNull(clock, "clock");
    }

    /** 최초 기동 시 checkpoint가 없으면 full refresh 후 high-water를 설정하여 과거 event 중복 적용을 막습니다. */
    @PostConstruct
    public void initialize() {
        if (!enabled) return;
        String consumer = consumerId();
        try {
            Long checkpoint = repository.checkpoint(consumer);
            if (checkpoint == null) {
                long highWater = repository.maxEventId();
                refresher.refreshAll();
                repository.establishCheckpoint(consumer, highWater, "SYSTEM");
                checkpoint = repository.checkpoint(consumer);
                lastEventId = checkpoint == null ? highWater : checkpoint;
            } else lastEventId = checkpoint;
            success();
        // 초기 full refresh/checkpoint 실패는 불완전한 cache 상태로 서비스하지 않도록 기동 실패로 승격합니다.
        // replay 실패 시 checkpoint를 전진시키지 않아 다음 poll에서 같은 event부터 재시도합니다.
        } catch (RuntimeException failure) {
            lastFailureType = failure.getClass().getSimpleName();
            throw new IllegalStateException("CPF Common durable cache checkpoint initialization failed", failure);
        }
    }

    /** checkpoint 이후 event를 순서대로 replay하고 refresh 성공 뒤에만 checkpoint를 전진시킵니다. */
    @Scheduled(fixedDelayString = "${cpf.common.cache.refresh-poll-millis:5000}", initialDelayString = "${cpf.common.cache.refresh-initial-delay-millis:5000}")
    public void poll() {
        if (!enabled) return;
        try {
            List<Map<String, Object>> events = repository.findAfter(lastEventId, pollLimit);
            for (Map<String, Object> event : events) {
                long id = asLong(event.get("event_id"), event.get("EVENT_ID"));
                if (id <= lastEventId) continue;
                String cache = asString(event.get("cache_name"), event.get("CACHE_NAME"));
                refresher.refresh(cache);
                repository.advanceCheckpoint(consumerId(), id, "SYSTEM");
                lastEventId = id;
            }
            success();
        // replay 실패 시 checkpoint를 전진시키지 않아 다음 poll에서 같은 event부터 재시도합니다.
        } catch (RuntimeException failure) {
            lastFailureType = failure.getClass().getSimpleName();
            log.warn("CPF Common cache replay failed. consumerId={}, lastEventId={}, failure={}", consumerId(), lastEventId, lastFailureType);
        }
    }

    /** 운영 조회용으로 현재 consumer checkpoint와 최근 성공/실패 상태를 노출합니다. */
    public Status status() { return new Status(enabled, consumerId(), lastEventId, lastSuccess, lastFailureType); }
    private void success() { lastSuccess = clock.instant(); lastFailureType = null; }
    private String consumerId() { return "CMN_CACHE:" + CpfInstanceIdentity.instanceId(); }
    private static long asLong(Object first, Object second) { Object v=first!=null?first:second; return v instanceof Number n?n.longValue():Long.parseLong(String.valueOf(v)); }
    private static String asString(Object first, Object second) { Object v=first!=null?first:second; return v==null?"":String.valueOf(v); }
    /** Cache replay consumer의 운영 상태를 불변 값으로 전달합니다. */
    public record Status(boolean enabled, String consumerId, long lastEventId, Instant lastSuccessfulPollAt, String lastFailureType) {}
}
