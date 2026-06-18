package cpf.cmn.cfg.service;

import cpf.cmn.cfg.dto.CommonConfigRequest;
import cpf.cmn.cfg.mapper.ConfigMapper;
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
 * PFW 공통 설정 캐시 서비스입니다.
 * 설정 조회, 등록, 수정, 삭제 후 캐시 초기화와 refresh 이벤트 발행을 함께 처리합니다.
 */
@Service
public class ConfigCacheService {
    private static final Logger logger = LoggerFactory.getLogger(ConfigCacheService.class);
    private static final String CACHE_NAME = "configCache";

    private final ConfigMapper configMapper;
    private final CacheManager cacheManager;
    private final CacheRefreshEventPublisher cacheRefreshEventPublisher;

    @Value("${cpf.cmn.cache.preload-enabled:true}")
    private boolean preloadEnabled;

    @Value("${cpf.cmn.cache.fail-fast-on-startup:false}")
    private boolean failFastOnStartup;

    public ConfigCacheService(
            ConfigMapper configMapper,
            CacheManager cacheManager,
            CacheRefreshEventPublisher cacheRefreshEventPublisher) {
        this.configMapper = configMapper;
        this.cacheManager = cacheManager;
        this.cacheRefreshEventPublisher = cacheRefreshEventPublisher;
    }

    @Cacheable("configCache")
    public List<Map<String, Object>> getAllConfigs() {
        logger.info("Cache Miss: Fetching all configs from database");
        return configMapper.findAllConfigs();
    }

    @Cacheable(value = "configCache", key = "#p0")
    public Map<String, Object> getConfigByKey(String configKey) {
        logger.debug("Cache Miss: Fetching config for key: {}", configKey);
        return configMapper.findConfigByKey(configKey);
    }

    public Map<String, Object> getConfigById(Long configId) {
        return configMapper.findConfigById(configId);
    }

    @Transactional(transactionManager = "cmnTransactionManager")
    public Map<String, Object> createConfig(CommonConfigRequest request) {
        configMapper.insertConfig(request);
        refreshConfigs();
        publishRefreshEvent("CREATE", request.getConfigKey(), request.getRequestUser());
        return getConfigById(request.getConfigId());
    }

    @Transactional(transactionManager = "cmnTransactionManager")
    public Map<String, Object> updateConfig(Long configId, CommonConfigRequest request) {
        configMapper.updateConfig(configId, request);
        refreshConfigs();
        publishRefreshEvent("UPDATE", request.getConfigKey(), request.getRequestUser());
        return getConfigById(configId);
    }

    @Transactional(transactionManager = "cmnTransactionManager")
    public List<Map<String, Object>> deleteConfig(Long configId) {
        Map<String, Object> beforeDelete = getConfigById(configId);
        String eventKey = beforeDelete == null ? String.valueOf(configId) : mapValue(beforeDelete, "configKey", "config_key");
        configMapper.deleteConfig(configId);
        List<Map<String, Object>> latestConfigs = refreshConfigs();
        publishRefreshEvent("DELETE", eventKey, "SYSTEM");
        return latestConfigs;
    }

    @CachePut("configCache")
    public List<Map<String, Object>> reloadConfigs() {
        return refreshConfigs();
    }

    public List<Map<String, Object>> refreshConfigs() {
        logger.info("Cache Refresh: Clearing config cache and fetching updated configs from database");
        clearCache();
        return configMapper.findAllConfigs();
    }

    public List<Map<String, Object>> refreshConfigsAndPublish() {
        List<Map<String, Object>> latestConfigs = refreshConfigs();
        publishRefreshEvent("MANUAL_REFRESH", "ALL", "SYSTEM");
        return latestConfigs;
    }

    @PostConstruct
    public void loadCacheOnStartup() {
        if (!preloadEnabled) {
            logger.info("Config cache preload skipped");
            return;
        }

        logger.info("Initializing config cache at startup");
        try {
            getAllConfigs();
        } catch (RuntimeException ex) {
            if (failFastOnStartup) {
                throw ex;
            }
            logger.warn("Config cache preload failed. Application will continue because fail-fast is disabled.", ex);
        }
    }

    @Scheduled(
            fixedRateString = "${cpf.cmn.cache.periodic-refresh-millis:1800000}",
            initialDelayString = "${cpf.cmn.cache.periodic-refresh-initial-delay-millis:1800000}")
    public void scheduledReloadConfigs() {
        logger.info("Scheduled config cache reload triggered");
        refreshConfigs();
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

    private String mapValue(Map<String, Object> source, String camelKey, String snakeKey) {
        Object value = source.get(camelKey);
        if (value == null) {
            value = source.get(snakeKey);
        }
        return value == null ? "" : String.valueOf(value);
    }
}
