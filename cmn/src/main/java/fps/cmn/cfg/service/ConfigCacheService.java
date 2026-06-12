package fps.cmn.cfg.service;

import fps.cmn.cfg.dto.CommonConfigRequest;
import fps.cmn.cfg.mapper.ConfigMapper;
import fps.cmn.ref.service.CacheRefreshEventPublisher;
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
 * 공통 설정값을 캐시하고 CRUD 변경 시 즉시 리프레시하는 서비스입니다.
 */
@Service
public class ConfigCacheService {
    private static final Logger logger = LoggerFactory.getLogger(ConfigCacheService.class);
    private static final String CACHE_NAME = "configCache";

    private final ConfigMapper configMapper;
    private final CacheManager cacheManager;
    private final CacheRefreshEventPublisher cacheRefreshEventPublisher;

    @Value("${fps.cmn.cache.preload-enabled:true}")
    private boolean preloadEnabled;

    public ConfigCacheService(
            ConfigMapper configMapper,
            CacheManager cacheManager,
            CacheRefreshEventPublisher cacheRefreshEventPublisher) {
        this.configMapper = configMapper;
        this.cacheManager = cacheManager;
        this.cacheRefreshEventPublisher = cacheRefreshEventPublisher;
    }

    /**
     * 전체 설정값을 조회합니다.
     *
     * @return 설정값 목록
     */
    @Cacheable("configCache")
    public List<Map<String, Object>> getAllConfigs() {
        logger.info("Cache Miss: Fetching all configs from database");
        return configMapper.findAllConfigs();
    }

    /**
     * 설정 키로 설정값을 조회합니다.
     *
     * @param configKey 설정 키
     * @return 설정값 데이터
     */
    @Cacheable(value = "configCache", key = "#configKey")
    public Map<String, Object> getConfigByKey(String configKey) {
        logger.debug("Cache Miss: Fetching config for key: {}", configKey);
        return configMapper.findConfigByKey(configKey);
    }

    /**
     * 설정 ID로 설정값을 조회합니다.
     *
     * @param configId 설정 ID
     * @return 설정값 데이터
     */
    public Map<String, Object> getConfigById(Long configId) {
        return configMapper.findConfigById(configId);
    }

    /**
     * 공통 설정값을 등록하고 설정 캐시를 즉시 리프레시합니다.
     *
     * @param request 등록 요청
     * @return 등록된 설정값 데이터
     */
    @Transactional(transactionManager = "cmnTransactionManager")
    public Map<String, Object> createConfig(CommonConfigRequest request) {
        configMapper.insertConfig(request);
        refreshConfigs();
        publishRefreshEvent("CREATE", request.getConfigKey(), request.getRequestUser());
        return getConfigById(request.getConfigId());
    }

    /**
     * 공통 설정값을 수정하고 설정 캐시를 즉시 리프레시합니다.
     *
     * @param configId 수정할 설정 ID
     * @param request 수정 요청
     * @return 수정된 설정값 데이터
     */
    @Transactional(transactionManager = "cmnTransactionManager")
    public Map<String, Object> updateConfig(Long configId, CommonConfigRequest request) {
        configMapper.updateConfig(configId, request);
        refreshConfigs();
        publishRefreshEvent("UPDATE", request.getConfigKey(), request.getRequestUser());
        return getConfigById(configId);
    }

    /**
     * 공통 설정값을 삭제하고 설정 캐시를 즉시 리프레시합니다.
     *
     * @param configId 삭제할 설정 ID
     * @return 최신 설정값 목록
     */
    @Transactional(transactionManager = "cmnTransactionManager")
    public List<Map<String, Object>> deleteConfig(Long configId) {
        Map<String, Object> beforeDelete = getConfigById(configId);
        String eventKey = beforeDelete == null ? String.valueOf(configId) : mapValue(beforeDelete, "configKey", "config_key");
        configMapper.deleteConfig(configId);
        List<Map<String, Object>> latestConfigs = refreshConfigs();
        publishRefreshEvent("DELETE", eventKey, "SYSTEM");
        return latestConfigs;
    }

    /**
     * 설정 캐시를 명시적으로 리로드합니다.
     *
     * @return 최신 설정값 목록
     */
    @CachePut("configCache")
    public List<Map<String, Object>> reloadConfigs() {
        return refreshConfigs();
    }

    /**
     * 설정 캐시를 즉시 비우고 최신 DB 값을 다시 조회합니다.
     *
     * @return 최신 설정값 목록
     */
    public List<Map<String, Object>> refreshConfigs() {
        logger.info("Cache Refresh: Clearing config cache and fetching updated configs from database");
        clearCache();
        return configMapper.findAllConfigs();
    }

    /**
     * 설정 캐시를 즉시 리프레시하고 다른 WAS에도 리프레시 이벤트를 전파합니다.
     *
     * @return 최신 설정값 목록
     */
    public List<Map<String, Object>> refreshConfigsAndPublish() {
        List<Map<String, Object>> latestConfigs = refreshConfigs();
        publishRefreshEvent("MANUAL_REFRESH", "ALL", "SYSTEM");
        return latestConfigs;
    }

    /**
     * 애플리케이션 시작 시 설정 캐시를 미리 적재합니다.
     */
    @PostConstruct
    public void loadCacheOnStartup() {
        if (!preloadEnabled) {
            logger.info("Config cache preload skipped");
            return;
        }

        logger.info("Initializing config cache at startup");
        getAllConfigs();
    }

    /**
     * 30분마다 설정 캐시를 주기적으로 리프레시합니다.
     */
    @Scheduled(
            fixedRateString = "${fps.cmn.cache.periodic-refresh-millis:1800000}",
            initialDelayString = "${fps.cmn.cache.periodic-refresh-initial-delay-millis:1800000}")
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
