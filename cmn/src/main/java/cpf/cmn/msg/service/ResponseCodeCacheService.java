package cpf.cmn.msg.service;

import cpf.cmn.msg.dto.CommonResponseCodeRequest;
import cpf.cmn.msg.mapper.ResponseCodeMapper;
import cpf.cmn.ref.service.CacheRefreshEventPublisher;
import cpf.cmn.utils.TextUtils;
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

@Service
public class ResponseCodeCacheService {
    private static final Logger logger = LoggerFactory.getLogger(ResponseCodeCacheService.class);
    public static final String CACHE_NAME = "responseCodeCache";

    private final ResponseCodeMapper responseCodeMapper;
    private final CacheManager cacheManager;
    private final CacheRefreshEventPublisher cacheRefreshEventPublisher;

    @Value("${cpf.cmn.cache.preload-enabled:true}")
    private boolean preloadEnabled;

    @Value("${cpf.cmn.cache.fail-fast-on-startup:false}")
    private boolean failFastOnStartup;

    public ResponseCodeCacheService(
            ResponseCodeMapper responseCodeMapper,
            CacheManager cacheManager,
            CacheRefreshEventPublisher cacheRefreshEventPublisher) {
        this.responseCodeMapper = responseCodeMapper;
        this.cacheManager = cacheManager;
        this.cacheRefreshEventPublisher = cacheRefreshEventPublisher;
    }

    @Cacheable(CACHE_NAME)
    public List<Map<String, Object>> getAllResponseCodes() {
        logger.info("Cache Miss: Fetching all response codes from database");
        return responseCodeMapper.findAllResponseCodes();
    }

    @Cacheable(value = CACHE_NAME, key = "#p0")
    public Map<String, Object> getResponseCode(String responseCode) {
        logger.debug("Cache Miss: Fetching response code: {}", responseCode);
        return responseCodeMapper.findResponseCode(responseCode);
    }

    @Transactional(transactionManager = "cmnTransactionManager")
    public Map<String, Object> createResponseCode(CommonResponseCodeRequest request) {
        normalize(request);
        responseCodeMapper.insertResponseCode(request);
        refreshResponseCodes();
        publishRefreshEvent("CREATE", request.getResponseCode(), request.getRequestUser());
        return responseCodeMapper.findResponseCode(request.getResponseCode());
    }

    @Transactional(transactionManager = "cmnTransactionManager")
    public Map<String, Object> updateResponseCode(String responseCode, CommonResponseCodeRequest request) {
        normalize(request);
        responseCodeMapper.updateResponseCode(TextUtils.normalizeCode(responseCode), request);
        refreshResponseCodes();
        publishRefreshEvent("UPDATE", request.getResponseCode(), request.getRequestUser());
        return responseCodeMapper.findResponseCode(request.getResponseCode());
    }

    @Transactional(transactionManager = "cmnTransactionManager")
    public List<Map<String, Object>> deleteResponseCode(String responseCode) {
        String normalized = TextUtils.normalizeCode(responseCode);
        responseCodeMapper.deleteResponseCode(normalized);
        List<Map<String, Object>> latest = refreshResponseCodes();
        publishRefreshEvent("DELETE", normalized, "SYSTEM");
        return latest;
    }

    @CachePut(CACHE_NAME)
    public List<Map<String, Object>> reloadResponseCodes() {
        return refreshResponseCodes();
    }

    public List<Map<String, Object>> refreshResponseCodes() {
        logger.info("Cache Refresh: Clearing response code cache and fetching updated values");
        clearCache();
        return responseCodeMapper.findAllResponseCodes();
    }

    public List<Map<String, Object>> refreshResponseCodesAndPublish() {
        List<Map<String, Object>> latest = refreshResponseCodes();
        publishRefreshEvent("MANUAL_REFRESH", "ALL", "SYSTEM");
        return latest;
    }

    @PostConstruct
    public void loadCacheOnStartup() {
        if (!preloadEnabled) {
            logger.info("Response code cache preload skipped");
            return;
        }
        try {
            getAllResponseCodes();
        } catch (RuntimeException ex) {
            if (failFastOnStartup) {
                throw ex;
            }
            logger.warn("Response code cache preload failed. Application will continue because fail-fast is disabled.", ex);
        }
    }

    @Scheduled(
            fixedRateString = "${cpf.cmn.cache.periodic-refresh-millis:1800000}",
            initialDelayString = "${cpf.cmn.cache.periodic-refresh-initial-delay-millis:1800000}")
    public void scheduledReloadResponseCodes() {
        refreshResponseCodes();
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

    private void normalize(CommonResponseCodeRequest request) {
        request.setResponseCode(TextUtils.normalizeCode(request.getResponseCode()));
        request.setMessageCode(TextUtils.normalizeCode(request.getMessageCode()));
        request.setResultType(TextUtils.normalizeCode(request.getResultType()));
        request.setModuleId(TextUtils.normalizeCode(request.getModuleId()));
        request.setResponseGroup(TextUtils.normalizeCode(request.getResponseGroup()));
        request.setSequenceNo(TextUtils.normalizeCode(request.getSequenceNo()));
        request.setUseYn(TextUtils.normalizeCode(request.getUseYn()));
        request.setRequestUser(TextUtils.hasText(request.getRequestUser()) ? request.getRequestUser() : "SYSTEM");
        requireFormat(request);
    }

    private void requireFormat(CommonResponseCodeRequest request) {
        String responseCode = request.getResponseCode();
        if (responseCode == null || !responseCode.matches("[SE][A-Z]{3}[0-9]{2}[0-9]{4}")) {
            throw new IllegalArgumentException("responseCode 형식은 {S|E}{MODULE}{GROUP}{SEQ}입니다. 예: EXYZ010001");
        }
        if (request.getMessageCode() == null || !request.getMessageCode().matches("M[A-Z]{3}[0-9]{2}[0-9]{4}")) {
            throw new IllegalArgumentException("messageCode 형식은 M{MODULE}{GROUP}{SEQ}입니다. 예: MXYZ010001");
        }
        if (!String.valueOf(responseCode.charAt(0)).equals(request.getResultType())) {
            throw new IllegalArgumentException("resultType must match the first character of responseCode.");
        }
        if (!responseCode.substring(1, 4).equals(request.getModuleId())) {
            throw new IllegalArgumentException("moduleId must match responseCode positions 2-4.");
        }
        if (!responseCode.substring(4, 6).equals(request.getResponseGroup())) {
            throw new IllegalArgumentException("responseGroup must match responseCode positions 5-6.");
        }
        if (!responseCode.substring(6, 10).equals(request.getSequenceNo())) {
            throw new IllegalArgumentException("sequenceNo must match responseCode positions 7-10.");
        }
    }
}
