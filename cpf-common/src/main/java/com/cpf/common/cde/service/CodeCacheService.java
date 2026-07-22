package cpf.cmn.cde.service;

import cpf.cmn.cde.dto.CommonCodeRequest;
import cpf.cmn.cde.mapper.CodeMapper;
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
 * PFW 공통 코드 캐시 서비스입니다.
 * 코드 조회, 등록, 수정, 삭제 후 캐시 초기화와 refresh 이벤트 발행을 함께 처리합니다.
 */
@Service
public class CodeCacheService extends cpf.cmn.common.base.CmnBaseService {

    private static final Logger logger = LoggerFactory.getLogger(CodeCacheService.class);
    private static final String CACHE_NAME = "codeCache";

    private final CodeMapper codeMapper;
    private final CacheManager cacheManager;
    private final CacheRefreshEventPublisher cacheRefreshEventPublisher;

    @Value("${cpf.cmn.cache.preload-enabled:true}")
    private boolean preloadEnabled;

    @Value("${cpf.cmn.cache.fail-fast-on-startup:false}")
    private boolean failFastOnStartup;

    public CodeCacheService(
            CodeMapper codeMapper,
            CacheManager cacheManager,
            CacheRefreshEventPublisher cacheRefreshEventPublisher) {
        this.codeMapper = codeMapper;
        this.cacheManager = cacheManager;
        this.cacheRefreshEventPublisher = cacheRefreshEventPublisher;
    }

    @Cacheable("codeCache")
    public List<Map<String, Object>> getAllCodes() {
        logger.info("Cache Miss: Fetching all codes from database");
        return codeMapper.findAllCodes();
    }

    @Cacheable(value = "codeCache", key = "#p0")
    public Map<String, Object> getCodeByKey(String codeKey) {
        logger.debug("Cache Miss: Fetching code for key: {}", codeKey);
        return codeMapper.findCodeByKey(codeKey);
    }

    public List<Map<String, Object>> getCodesByKey(String codeKey) {
        return codeMapper.findCodesByKey(codeKey);
    }

    public Map<String, Object> getCodeById(Long codeId) {
        return codeMapper.findCodeById(codeId);
    }

    @Transactional(transactionManager = "cmnTransactionManager")
    public Map<String, Object> createCode(CommonCodeRequest request) {
        codeMapper.insertCode(request);
        refreshCodes();
        publishRefreshEvent("CREATE", request.getCodeKey(), request.getRequestUser());
        return getCodeById(request.getCodeId());
    }

    @Transactional(transactionManager = "cmnTransactionManager")
    public Map<String, Object> updateCode(Long codeId, CommonCodeRequest request) {
        codeMapper.updateCode(codeId, request);
        refreshCodes();
        publishRefreshEvent("UPDATE", request.getCodeKey(), request.getRequestUser());
        return getCodeById(codeId);
    }

    @Transactional(transactionManager = "cmnTransactionManager")
    public List<Map<String, Object>> deleteCode(Long codeId) {
        Map<String, Object> beforeDelete = getCodeById(codeId);
        String eventKey = beforeDelete == null ? String.valueOf(codeId) : mapValue(beforeDelete, "codeKey", "code_key");
        codeMapper.deleteCode(codeId);
        List<Map<String, Object>> latestCodes = refreshCodes();
        publishRefreshEvent("DELETE", eventKey, "SYSTEM");
        return latestCodes;
    }

    @CachePut("codeCache")
    public List<Map<String, Object>> reloadCodes() {
        return refreshCodes();
    }

    public List<Map<String, Object>> refreshCodes() {
        logger.info("Cache Refresh: Clearing code cache and fetching updated codes from database");
        clearCache();
        return codeMapper.findAllCodes();
    }

    public List<Map<String, Object>> refreshCodesAndPublish() {
        List<Map<String, Object>> latestCodes = refreshCodes();
        publishRefreshEvent("MANUAL_REFRESH", "ALL", "SYSTEM");
        return latestCodes;
    }

    @PostConstruct
    public void loadCacheOnStartup() {
        if (!preloadEnabled) {
            logger.info("Code cache preload skipped");
            return;
        }

        logger.info("Initializing code cache at startup");
        try {
            getAllCodes();
        } catch (RuntimeException ex) {
            if (failFastOnStartup) {
                throw ex;
            }
            logger.warn("Code cache preload failed. Application will continue because fail-fast is disabled.", ex);
        }
    }

    @Scheduled(
            fixedRateString = "${cpf.cmn.cache.periodic-refresh-millis:1800000}",
            initialDelayString = "${cpf.cmn.cache.periodic-refresh-initial-delay-millis:1800000}")
    public void scheduledReloadCodes() {
        logger.info("Scheduled cache reload triggered");
        refreshCodes();
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
