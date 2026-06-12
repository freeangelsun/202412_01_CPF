package fps.cmn.cde.service;

import fps.cmn.cde.dto.CommonCodeRequest;
import fps.cmn.cde.mapper.CodeMapper;
import fps.cmn.ref.service.CacheRefreshEventPublisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * CodeCacheService.java
 *
 * - 코드 데이터를 캐싱하여 애플리케이션 성능을 향상시킵니다.
 * - 애플리케이션 시작 시 전체 코드 데이터를 자동으로 캐싱합니다.
 */
@Service
public class CodeCacheService {

    private static final Logger logger = LoggerFactory.getLogger(CodeCacheService.class);
    private static final String CACHE_NAME = "codeCache";
    private final CodeMapper codeMapper;
    private final CacheManager cacheManager;
    private final CacheRefreshEventPublisher cacheRefreshEventPublisher;

    @Value("${fps.cmn.cache.preload-enabled:true}")
    private boolean preloadEnabled;

    public CodeCacheService(
            CodeMapper codeMapper,
            CacheManager cacheManager,
            CacheRefreshEventPublisher cacheRefreshEventPublisher) {
        this.codeMapper = codeMapper;
        this.cacheManager = cacheManager;
        this.cacheRefreshEventPublisher = cacheRefreshEventPublisher;
    }

    /**
     * 모든 코드 데이터를 캐싱합니다.
     *
     * @return 코드 데이터 목록
     */
    @Cacheable("codeCache")
    public List<Map<String, Object>> getAllCodes() {
        logger.info("Cache Miss: Fetching all codes from database");
        return codeMapper.findAllCodes();
    }

    /**
     * 특정 코드 키에 해당하는 코드 데이터를 반환합니다.
     *
     * @param codeKey 코드 키
     * @return 코드 데이터
     */
    @Cacheable(value = "codeCache", key = "#codeKey")
    public Map<String, Object> getCodeByKey(String codeKey) {
        logger.debug("Cache Miss: Fetching code for key: {}", codeKey);
        return codeMapper.findCodeByKey(codeKey);
    }

    /**
     * 특정 코드 키에 해당하는 코드 목록을 조회합니다.
     *
     * @param codeKey 코드 키
     * @return 같은 코드 키를 가진 코드 목록
     */
    public List<Map<String, Object>> getCodesByKey(String codeKey) {
        return codeMapper.findCodesByKey(codeKey);
    }

    /**
     * 코드 ID로 코드 한 건을 조회합니다.
     *
     * @param codeId 코드 ID
     * @return 코드 데이터
     */
    public Map<String, Object> getCodeById(Long codeId) {
        return codeMapper.findCodeById(codeId);
    }

    /**
     * 공통 코드를 등록하고 코드 캐시를 즉시 리프레시합니다.
     *
     * @param request 등록 요청
     * @return 등록된 코드 데이터
     */
    @Transactional(transactionManager = "cmnTransactionManager")
    public Map<String, Object> createCode(CommonCodeRequest request) {
        codeMapper.insertCode(request);
        refreshCodes();
        publishRefreshEvent("CREATE", request.getCodeKey(), request.getRequestUser());
        return getCodeById(request.getCodeId());
    }

    /**
     * 공통 코드를 수정하고 코드 캐시를 즉시 리프레시합니다.
     *
     * @param codeId 수정할 코드 ID
     * @param request 수정 요청
     * @return 수정된 코드 데이터
     */
    @Transactional(transactionManager = "cmnTransactionManager")
    public Map<String, Object> updateCode(Long codeId, CommonCodeRequest request) {
        codeMapper.updateCode(codeId, request);
        refreshCodes();
        publishRefreshEvent("UPDATE", request.getCodeKey(), request.getRequestUser());
        return getCodeById(codeId);
    }

    /**
     * 공통 코드를 삭제하고 코드 캐시를 즉시 리프레시합니다.
     *
     * @param codeId 삭제할 코드 ID
     * @return 최신 코드 목록
     */
    @Transactional(transactionManager = "cmnTransactionManager")
    public List<Map<String, Object>> deleteCode(Long codeId) {
        Map<String, Object> beforeDelete = getCodeById(codeId);
        String eventKey = beforeDelete == null ? String.valueOf(codeId) : mapValue(beforeDelete, "codeKey", "code_key");
        codeMapper.deleteCode(codeId);
        List<Map<String, Object>> latestCodes = refreshCodes();
        publishRefreshEvent("DELETE", eventKey, "SYSTEM");
        return latestCodes;
    }

    /**
     * 캐시를 리로딩합니다.
     *
     * @return 최신 코드 데이터 목록
     */
    @CachePut("codeCache")
    public List<Map<String, Object>> reloadCodes() {
        return refreshCodes();
    }

    /**
     * 코드 캐시를 즉시 비우고 최신 DB 값을 다시 조회합니다.
     *
     * @return 최신 코드 데이터 목록
     */
    public List<Map<String, Object>> refreshCodes() {
        logger.info("Cache Refresh: Clearing code cache and fetching updated codes from database");
        clearCache();
        return codeMapper.findAllCodes();
    }

    /**
     * 코드 캐시를 즉시 리프레시하고 다른 WAS에도 리프레시 이벤트를 전파합니다.
     *
     * @return 최신 코드 데이터 목록
     */
    public List<Map<String, Object>> refreshCodesAndPublish() {
        List<Map<String, Object>> latestCodes = refreshCodes();
        publishRefreshEvent("MANUAL_REFRESH", "ALL", "SYSTEM");
        return latestCodes;
    }

    /**
     * 애플리케이션 시작 시 전체 코드 데이터를 자동으로 캐싱합니다.
     */
    @PostConstruct
    public void loadCacheOnStartup() {
        if (!preloadEnabled) {
            logger.info("Code cache preload skipped");
            return;
        }

        logger.info("Initializing code cache at startup");
        getAllCodes();
    }

    /**
     * 주기적으로 캐시를 리로딩합니다.
     * - 30분마다 자동으로 실행됩니다.
     */
    @Scheduled(
            fixedRateString = "${fps.cmn.cache.periodic-refresh-millis:1800000}",
            initialDelayString = "${fps.cmn.cache.periodic-refresh-initial-delay-millis:1800000}")
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
