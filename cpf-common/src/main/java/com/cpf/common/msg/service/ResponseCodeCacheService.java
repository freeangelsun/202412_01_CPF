package com.cpf.common.msg.service;

import com.cpf.common.msg.dto.CommonResponseCodeRequest;
import com.cpf.common.msg.mapper.ResponseCodeMapper;
import com.cpf.common.ref.service.CacheRefreshEventPublisher;
import com.cpf.core.api.util.CpfStrings;
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
 * CPF 공통 응답코드의 조회·변경·캐시 동기화를 담당하는 CMN 서비스입니다.
 *
 * <p>응답코드 변경은 CMN 소유 Transaction에서 처리하고, 성공 후 로컬 캐시를 무효화한 뒤
 * refresh event를 발행하여 다중 인스턴스가 동일한 응답코드 기준을 사용하도록 합니다.
 * 기동 시 preload 실패 정책은 설정으로 통제하여 업무 WAS의 가용성과 fail-fast 요구를 분리합니다.</p>
 */
@Service
public class ResponseCodeCacheService extends com.cpf.common.common.base.CmnBaseService {
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
        responseCodeMapper.updateResponseCode(CpfStrings.normalizeCode(responseCode), request);
        refreshResponseCodes();
        publishRefreshEvent("UPDATE", request.getResponseCode(), request.getRequestUser());
        return responseCodeMapper.findResponseCode(request.getResponseCode());
    }

    @Transactional(transactionManager = "cmnTransactionManager")
    public List<Map<String, Object>> deleteResponseCode(String responseCode) {
        String normalized = CpfStrings.normalizeCode(responseCode);
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
        request.setResponseCode(CpfStrings.normalizeCode(request.getResponseCode()));
        request.setMessageCode(CpfStrings.normalizeCode(request.getMessageCode()));
        request.setResultType(CpfStrings.normalizeCode(request.getResultType()));
        request.setModuleId(CpfStrings.normalizeCode(request.getModuleId()));
        request.setResponseGroup(CpfStrings.normalizeCode(request.getResponseGroup()));
        request.setSequenceNo(CpfStrings.normalizeCode(request.getSequenceNo()));
        request.setUseYn(CpfStrings.normalizeCode(request.getUseYn()));
        request.setRequestUser(CpfStrings.hasText(request.getRequestUser()) ? request.getRequestUser() : "SYSTEM");
        requireFormat(request);
    }

    private void requireFormat(CommonResponseCodeRequest request) {
        String responseCode = request.getResponseCode();
        if (responseCode == null || !responseCode.matches("[SE][A-Z]{3}[0-9]{2}[0-9]{4}")) {
            throw new IllegalArgumentException("responseCode 형식은 {S|E}{MODULE}{GROUP}{SEQ}입니다. 예: EREF010001");
        }
        if (request.getMessageCode() == null || !request.getMessageCode().matches("M[A-Z]{3}[0-9]{2}[0-9]{4}")) {
            throw new IllegalArgumentException("messageCode 형식은 M{MODULE}{GROUP}{SEQ}입니다. 예: MREF010001");
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
