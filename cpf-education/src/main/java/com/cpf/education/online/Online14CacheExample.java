package com.cpf.education.online;

import com.cpf.data.cache.CpfCacheAsideService;
import com.cpf.data.cache.api.CpfCache;
import com.cpf.data.cache.api.CpfCacheKey;
import com.cpf.data.cache.api.CpfCacheOptions;
import com.cpf.data.cache.api.CpfCacheValue;
import com.cpf.foundation.execution.api.CpfOnlineTransaction;
import com.cpf.web.api.CpfRestController;
import io.swagger.v3.oas.annotations.Operation;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/** 온라인-14 Cache 거래: CPF Cache-aside, TTL, single-flight, get/put/evict를 Provider-neutral하게 사용합니다. */
@CpfRestController
@RequestMapping("/edu/online/14-cache")
public class Online14CacheExample {
    private final CpfCache cache;
    private final CpfCacheAsideService cacheAside;

    public Online14CacheExample(CpfCache cache, CpfCacheAsideService cacheAside) {
        this.cache = cache;
        this.cacheAside = cacheAside;
    }

    @PostMapping
    @Operation(operationId = "EDU-ONLINE-14", summary = "Cache 사용 거래")
    @CpfOnlineTransaction(
            operationId = "EDU-ONLINE-14",
            name = "Cache 사용 거래",
            description = "CPF Cache-aside와 distributed lock으로 Local/Redis/Valkey Provider 차이를 숨기고 TTL·miss·evict를 처리한다.")
    /** cache 단계는 CPF Public 계약을 사용해 이 EDU 시나리오의 업무 흐름을 수행합니다. */
    public Map<String, Object> cache(@RequestBody Command command) {
        CpfCacheKey key = new CpfCacheKey("member", command.key(), command.tenantId());
        Duration ttl = Duration.ofSeconds(command.ttlSeconds());
        CpfCacheOptions options = new CpfCacheOptions(
                ttl, Duration.ofSeconds(10), Duration.ofMillis(200), Duration.ofSeconds(3), true, false);

        CpfCacheValue value = cacheAside.getOrLoad(key, options, ignored -> value(command.value(), ttl));
        if (command.evictAfterRead()) cache.evict(key);

        return Map.of(
                "found", value.found(),
                "negative", value.negative(),
                "providerReady", cache.health().ready(),
                "evicted", command.evictAfterRead());
    }

    /** 이 타입은 해당 EDU 시나리오의 입력·출력 또는 업무 경계를 명확히 표현합니다. */
    public record Command(
            String tenantId,
            String key,
            String value,
            long ttlSeconds,
            boolean evictAfterRead) {
        public Command {
            if (ttlSeconds <= 0) throw new IllegalArgumentException("ttlSeconds must be positive");
        }
    }

    private static CpfCacheValue value(String value, Duration ttl) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        return new CpfCacheValue(true, false, bytes, "text/plain", 1, Instant.now().plus(ttl));
    }
}
