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
 * 怨듯넻 ?ㅼ젙媛믪쓣 罹먯떆?섍퀬 CRUD 蹂寃???利됱떆 由ы봽?덉떆?섎뒗 ?쒕퉬?ㅼ엯?덈떎.
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

    /**
     * ?꾩껜 ?ㅼ젙媛믪쓣 議고쉶?⑸땲??
     *
     * @return ?ㅼ젙媛?⑸줉
     */
    @Cacheable("configCache")
    public List<Map<String, Object>> getAllConfigs() {
        logger.info("Cache Miss: Fetching all configs from database");
        return configMapper.findAllConfigs();
    }

    /**
     * ?ㅼ젙 ?ㅻ줈 ?ㅼ젙媛믪쓣 議고쉶?⑸땲??
     *
     * @param configKey ?ㅼ젙 ??     * @return ?ㅼ젙媛??곗씠??     */
    @Cacheable(value = "configCache", key = "#p0")
    public Map<String, Object> getConfigByKey(String configKey) {
        logger.debug("Cache Miss: Fetching config for key: {}", configKey);
        return configMapper.findConfigByKey(configKey);
    }

    /**
     * ?ㅼ젙 ID濡??ㅼ젙媛믪쓣 議고쉶?⑸땲??
     *
     * @param configId ?ㅼ젙 ID
     * @return ?ㅼ젙媛??곗씠??     */
    public Map<String, Object> getConfigById(Long configId) {
        return configMapper.findConfigById(configId);
    }

    /**
     * 怨듯넻 ?ㅼ젙媛믪쓣 ?깅줉?섍퀬 ?ㅼ젙 罹먯떆瑜?利됱떆 由ы봽?덉떆?⑸땲??
     *
     * @param request ?깅줉 ?붿껌
     * @return ?깅줉???ㅼ젙媛??곗씠??     */
    @Transactional(transactionManager = "cmnTransactionManager")
    public Map<String, Object> createConfig(CommonConfigRequest request) {
        configMapper.insertConfig(request);
        refreshConfigs();
        publishRefreshEvent("CREATE", request.getConfigKey(), request.getRequestUser());
        return getConfigById(request.getConfigId());
    }

    /**
     * 怨듯넻 ?ㅼ젙媛믪쓣 ?섏젙?섍퀬 ?ㅼ젙 罹먯떆瑜?利됱떆 由ы봽?덉떆?⑸땲??
     *
     * @param configId ?섏젙???ㅼ젙 ID
     * @param request ?섏젙 ?붿껌
     * @return ?섏젙???ㅼ젙媛??곗씠??     */
    @Transactional(transactionManager = "cmnTransactionManager")
    public Map<String, Object> updateConfig(Long configId, CommonConfigRequest request) {
        configMapper.updateConfig(configId, request);
        refreshConfigs();
        publishRefreshEvent("UPDATE", request.getConfigKey(), request.getRequestUser());
        return getConfigById(configId);
    }

    /**
     * 怨듯넻 ?ㅼ젙媛믪쓣 ??젣?섍퀬 ?ㅼ젙 罹먯떆瑜?利됱떆 由ы봽?덉떆?⑸땲??
     *
     * @param configId ??젣???ㅼ젙 ID
     * @return 理쒖떊 ?ㅼ젙媛?⑸줉
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
     * ?ㅼ젙 罹먯떆瑜?낆떆?곸쑝濡?由щ줈?쒗빀?덈떎.
     *
     * @return 理쒖떊 ?ㅼ젙媛?⑸줉
     */
    @CachePut("configCache")
    public List<Map<String, Object>> reloadConfigs() {
        return refreshConfigs();
    }

    /**
     * ?ㅼ젙 罹먯떆瑜?利됱떆 鍮꾩슦怨?理쒖떊 DB 媛믪쓣 ?ㅼ떆 議고쉶?⑸땲??
     *
     * @return 理쒖떊 ?ㅼ젙媛?⑸줉
     */
    public List<Map<String, Object>> refreshConfigs() {
        logger.info("Cache Refresh: Clearing config cache and fetching updated configs from database");
        clearCache();
        return configMapper.findAllConfigs();
    }

    /**
     * ?ㅼ젙 罹먯떆瑜?利됱떆 由ы봽?덉떆?섍퀬 ?ㅻⅨ WAS?먮룄 由ы봽?덉떆 ?대깽?몃? ?꾪뙆?⑸땲??
     *
     * @return 理쒖떊 ?ㅼ젙媛?⑸줉
     */
    public List<Map<String, Object>> refreshConfigsAndPublish() {
        List<Map<String, Object>> latestConfigs = refreshConfigs();
        publishRefreshEvent("MANUAL_REFRESH", "ALL", "SYSTEM");
        return latestConfigs;
    }

    /**
     * ?좏뵆由ъ??댁뀡 ?쒖옉 ???ㅼ젙 罹먯떆瑜?誘몃━ ?곸옱?⑸땲??
     */
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

    /**
     * 30遺꾨쭏???ㅼ젙 罹먯떆瑜?二쇨린?곸쑝濡?由ы봽?덉떆?⑸땲??
     */
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

