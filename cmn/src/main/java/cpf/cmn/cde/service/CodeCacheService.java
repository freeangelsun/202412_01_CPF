package cpf.cmn.cde.service;

import cpf.cmn.cde.dto.CommonCodeRequest;
import cpf.cmn.cde.mapper.CodeMapper;
import cpf.cmn.ref.service.CacheRefreshEventPublisher;
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
 * - 肄붾뱶 ?곗씠?곕? 罹먯떛?섏뿬 ?좏뵆由ъ??댁뀡 ?깅뒫???μ긽?쒗궢?덈떎.
 * - ?좏뵆由ъ??댁뀡 ?쒖옉 ???꾩껜 肄붾뱶 ?곗씠?곕? ?먮룞?쇰줈 罹먯떛?⑸땲??
 */
@Service
public class CodeCacheService {

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

    /**
     * 紐⑤뱺 肄붾뱶 ?곗씠?곕? 罹먯떛?⑸땲??
     *
     * @return 肄붾뱶 ?곗씠??紐⑸줉
     */
    @Cacheable("codeCache")
    public List<Map<String, Object>> getAllCodes() {
        logger.info("Cache Miss: Fetching all codes from database");
        return codeMapper.findAllCodes();
    }

    /**
     * ?뱀젙 肄붾뱶 ?ㅼ뿉 ?대떦?섎뒗 肄붾뱶 ?곗씠?곕? 諛섑솚?⑸땲??
     *
     * @param codeKey 肄붾뱶 ??
     * @return 肄붾뱶 ?곗씠??
     */
    @Cacheable(value = "codeCache", key = "#p0")
    public Map<String, Object> getCodeByKey(String codeKey) {
        logger.debug("Cache Miss: Fetching code for key: {}", codeKey);
        return codeMapper.findCodeByKey(codeKey);
    }

    /**
     * ?뱀젙 肄붾뱶 ?ㅼ뿉 ?대떦?섎뒗 肄붾뱶 紐⑸줉??議고쉶?⑸땲??
     *
     * @param codeKey 肄붾뱶 ??     * @return 媛숈? 肄붾뱶 ?ㅻ? 媛吏?肄붾뱶 紐⑸줉
     */
    public List<Map<String, Object>> getCodesByKey(String codeKey) {
        return codeMapper.findCodesByKey(codeKey);
    }

    /**
     * 肄붾뱶 ID濡?肄붾뱶 ??嫄댁쓣 議고쉶?⑸땲??
     *
     * @param codeId 肄붾뱶 ID
     * @return 肄붾뱶 ?곗씠??     */
    public Map<String, Object> getCodeById(Long codeId) {
        return codeMapper.findCodeById(codeId);
    }

    /**
     * 怨듯넻 肄붾뱶瑜??깅줉?섍퀬 肄붾뱶 罹먯떆瑜?利됱떆 由ы봽?덉떆?⑸땲??
     *
     * @param request ?깅줉 ?붿껌
     * @return ?깅줉??肄붾뱶 ?곗씠??     */
    @Transactional(transactionManager = "cmnTransactionManager")
    public Map<String, Object> createCode(CommonCodeRequest request) {
        codeMapper.insertCode(request);
        refreshCodes();
        publishRefreshEvent("CREATE", request.getCodeKey(), request.getRequestUser());
        return getCodeById(request.getCodeId());
    }

    /**
     * 怨듯넻 肄붾뱶瑜??섏젙?섍퀬 肄붾뱶 罹먯떆瑜?利됱떆 由ы봽?덉떆?⑸땲??
     *
     * @param codeId ?섏젙??肄붾뱶 ID
     * @param request ?섏젙 ?붿껌
     * @return ?섏젙??肄붾뱶 ?곗씠??     */
    @Transactional(transactionManager = "cmnTransactionManager")
    public Map<String, Object> updateCode(Long codeId, CommonCodeRequest request) {
        codeMapper.updateCode(codeId, request);
        refreshCodes();
        publishRefreshEvent("UPDATE", request.getCodeKey(), request.getRequestUser());
        return getCodeById(codeId);
    }

    /**
     * 怨듯넻 肄붾뱶瑜???젣?섍퀬 肄붾뱶 罹먯떆瑜?利됱떆 由ы봽?덉떆?⑸땲??
     *
     * @param codeId ??젣??肄붾뱶 ID
     * @return 理쒖떊 肄붾뱶 紐⑸줉
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
     * 罹먯떆瑜?由щ줈?⑺빀?덈떎.
     *
     * @return 理쒖떊 肄붾뱶 ?곗씠??紐⑸줉
     */
    @CachePut("codeCache")
    public List<Map<String, Object>> reloadCodes() {
        return refreshCodes();
    }

    /**
     * 肄붾뱶 罹먯떆瑜?利됱떆 鍮꾩슦怨?理쒖떊 DB 媛믪쓣 ?ㅼ떆 議고쉶?⑸땲??
     *
     * @return 理쒖떊 肄붾뱶 ?곗씠??紐⑸줉
     */
    public List<Map<String, Object>> refreshCodes() {
        logger.info("Cache Refresh: Clearing code cache and fetching updated codes from database");
        clearCache();
        return codeMapper.findAllCodes();
    }

    /**
     * 肄붾뱶 罹먯떆瑜?利됱떆 由ы봽?덉떆?섍퀬 ?ㅻⅨ WAS?먮룄 由ы봽?덉떆 ?대깽?몃? ?꾪뙆?⑸땲??
     *
     * @return 理쒖떊 肄붾뱶 ?곗씠??紐⑸줉
     */
    public List<Map<String, Object>> refreshCodesAndPublish() {
        List<Map<String, Object>> latestCodes = refreshCodes();
        publishRefreshEvent("MANUAL_REFRESH", "ALL", "SYSTEM");
        return latestCodes;
    }

    /**
     * ?좏뵆由ъ??댁뀡 ?쒖옉 ???꾩껜 肄붾뱶 ?곗씠?곕? ?먮룞?쇰줈 罹먯떛?⑸땲??
     */
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

    /**
     * 二쇨린?곸쑝濡?罹먯떆瑜?由щ줈?⑺빀?덈떎.
     * - 30遺꾨쭏???먮룞?쇰줈 ?ㅽ뻾?⑸땲??
     */
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

