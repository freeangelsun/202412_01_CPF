package com.cpf.common.parameter.service;

import com.cpf.foundation.api.CpfBaseService;

import com.cpf.common.parameter.dto.CommonConfigRequest;
import com.cpf.common.parameter.mapper.ConfigMapper;
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

/** CPF 공통 설정 cache를 commit-safe snapshot 방식으로 관리합니다. */
@Deprecated(forRemoval = false)
public class ConfigCacheService extends CpfBaseService {
    private static final Logger logger = LoggerFactory.getLogger(ConfigCacheService.class);
    private static final String CACHE_NAME = "configCache";
    private static final String ALL_KEY = "ALL";

    private final ConfigMapper configMapper;
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

    public ConfigCacheService(ConfigMapper configMapper, CacheManager cacheManager,
            CacheRefreshEventPublisher cacheRefreshEventPublisher) {
        this(configMapper, cacheManager, cacheRefreshEventPublisher, Clock.systemUTC());
    }

    @org.springframework.beans.factory.annotation.Autowired
    public ConfigCacheService(ConfigMapper configMapper, CacheManager cacheManager,
            CacheRefreshEventPublisher cacheRefreshEventPublisher, Clock clock) {
        this.configMapper = configMapper;
        this.cacheManager = cacheManager;
        this.cacheRefreshEventPublisher = cacheRefreshEventPublisher;
        this.clock = java.util.Objects.requireNonNull(clock, "clock");
    }

    @Cacheable(value = CACHE_NAME, key = "'ALL'")
    public List<Map<String, Object>> getAllConfigs() {
        return configMapper.findAllConfigs();
    }

    @Cacheable(value = CACHE_NAME, key = "'KEY:' + #p0")
    public Map<String, Object> getConfigByKey(String configKey) {
        return configMapper.findConfigByKey(configKey);
    }

    public Map<String, Object> getConfigById(Long configId) { return configMapper.findConfigById(configId); }

    @Transactional(transactionManager = "cpfCommonTransactionManager")
    public Map<String, Object> createConfig(CommonConfigRequest request) {
        configMapper.insertConfig(request);
        Map<String, Object> created = getConfigById(request.getConfigId());
        scheduleSnapshotAfterCommit(configMapper.findAllConfigs());
        publishRefreshEvent("CREATE", request.getConfigKey(), request.getRequestUser());
        return created;
    }

    @Transactional(transactionManager = "cpfCommonTransactionManager")
    public Map<String, Object> updateConfig(Long configId, CommonConfigRequest request) {
        configMapper.updateConfig(configId, request);
        Map<String, Object> updated = getConfigById(configId);
        scheduleSnapshotAfterCommit(configMapper.findAllConfigs());
        publishRefreshEvent("UPDATE", request.getConfigKey(), request.getRequestUser());
        return updated;
    }

    @Transactional(transactionManager = "cpfCommonTransactionManager")
    public List<Map<String, Object>> deleteConfig(Long configId) {
        Map<String, Object> beforeDelete = getConfigById(configId);
        String key = beforeDelete == null ? String.valueOf(configId) : mapValue(beforeDelete, "configKey", "config_key");
        configMapper.deleteConfig(configId);
        List<Map<String, Object>> latest = configMapper.findAllConfigs();
        scheduleSnapshotAfterCommit(latest);
        publishRefreshEvent("DELETE", key, "SYSTEM");
        return latest;
    }

    public List<Map<String, Object>> reloadConfigs() { return refreshConfigs(); }

    public List<Map<String, Object>> refreshConfigs() {
        List<Map<String, Object>> latest = configMapper.findAllConfigs();
        replaceSnapshot(latest);
        return latest;
    }

    public List<Map<String, Object>> refreshConfigsAndPublish() {
        List<Map<String, Object>> latest = refreshConfigs();
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
            refreshConfigs();
        } catch (RuntimeException ex) {
            recordFailure(ex);
            if (failFastOnStartup) throw ex;
            logger.warn("Config cache preload failed. Existing cache is preserved.", ex);
        }
    }

    @Scheduled(fixedRateString = "${cpf.cmn.cache.periodic-refresh-millis:1800000}",
            initialDelayString = "${cpf.cmn.cache.periodic-refresh-initial-delay-millis:1800000}")
    public void scheduledReloadConfigs() {
        try {
            refreshConfigs();
        } catch (RuntimeException ex) {
            recordFailure(ex);
            logger.warn("Scheduled config cache reload failed. Existing cache is preserved.", ex);
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
    private String mapValue(Map<String, Object> source, String camelKey, String snakeKey) {
        Object value = source.get(camelKey);
        if (value == null) value = source.get(snakeKey);
        return value == null ? "" : String.valueOf(value);
    }
}
