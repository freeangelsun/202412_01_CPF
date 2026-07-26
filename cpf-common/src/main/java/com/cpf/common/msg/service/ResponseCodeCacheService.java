package com.cpf.common.msg.service;

import com.cpf.common.msg.dto.CommonResponseCodeRequest;
import com.cpf.common.msg.mapper.ResponseCodeMapper;
import com.cpf.common.ref.service.CacheRefreshEventPublisher;
import com.cpf.core.api.util.CpfStrings;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * CPF 공통 응답코드의 조회·변경·캐시 동기화를 담당합니다.
 *
 * <p>전체 목록과 개별 응답코드의 cache key를 분리하고, DB snapshot을 먼저 정상 조회한 뒤에만
 * 기존 cache를 교체합니다. 업무 변경 중에는 cache를 건드리지 않고 commit 이후 교체하여 rollback된
 * 데이터가 cache에 노출되지 않게 합니다.</p>
 */
@Service
@ConditionalOnExpression("'${cpf.common.runtime-mode:product}'.toLowerCase() == 'product'")
public class ResponseCodeCacheService extends com.cpf.common.common.base.CmnBaseService {
    private static final Logger logger = LoggerFactory.getLogger(ResponseCodeCacheService.class);
    public static final String CACHE_NAME = "responseCodeCache";
    private static final String ALL_KEY = "ALL";
    private static final String CODE_PREFIX = "CODE:";

    private final ResponseCodeMapper responseCodeMapper;
    private final CacheManager cacheManager;
    private final CacheRefreshEventPublisher cacheRefreshEventPublisher;
    private final AtomicLong cacheVersion = new AtomicLong();

    private volatile Instant lastSynchronizedAt;
    private volatile String lastRefreshFailure;

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

    @Cacheable(value = CACHE_NAME, key = "'ALL'")
    public List<Map<String, Object>> getAllResponseCodes() {
        logger.info("Cache Miss: Fetching all response codes from database");
        return responseCodeMapper.findAllResponseCodes();
    }

    @Cacheable(value = CACHE_NAME, key = "'CODE:' + #p0")
    public Map<String, Object> getResponseCode(String responseCode) {
        String normalized = CpfStrings.normalizeCode(responseCode);
        logger.debug("Cache Miss: Fetching response code: {}", normalized);
        return responseCodeMapper.findResponseCode(normalized);
    }

    @Transactional(transactionManager = "cmnTransactionManager")
    public Map<String, Object> createResponseCode(CommonResponseCodeRequest request) {
        normalize(request);
        responseCodeMapper.insertResponseCode(request);
        Map<String, Object> created = responseCodeMapper.findResponseCode(request.getResponseCode());
        scheduleSnapshotAfterCommit(responseCodeMapper.findAllResponseCodes());
        publishRefreshEvent("CREATE", request.getResponseCode(), request.getRequestUser());
        return created;
    }

    @Transactional(transactionManager = "cmnTransactionManager")
    public Map<String, Object> updateResponseCode(String responseCode, CommonResponseCodeRequest request) {
        normalize(request);
        responseCodeMapper.updateResponseCode(CpfStrings.normalizeCode(responseCode), request);
        Map<String, Object> updated = responseCodeMapper.findResponseCode(request.getResponseCode());
        scheduleSnapshotAfterCommit(responseCodeMapper.findAllResponseCodes());
        publishRefreshEvent("UPDATE", request.getResponseCode(), request.getRequestUser());
        return updated;
    }

    @Transactional(transactionManager = "cmnTransactionManager")
    public List<Map<String, Object>> deleteResponseCode(String responseCode) {
        String normalized = CpfStrings.normalizeCode(responseCode);
        responseCodeMapper.deleteResponseCode(normalized);
        List<Map<String, Object>> latest = responseCodeMapper.findAllResponseCodes();
        scheduleSnapshotAfterCommit(latest);
        publishRefreshEvent("DELETE", normalized, "SYSTEM");
        return latest;
    }

    public List<Map<String, Object>> reloadResponseCodes() {
        return refreshResponseCodes();
    }

    /** DB 조회가 성공한 뒤에만 기존 cache를 교체합니다. */
    public List<Map<String, Object>> refreshResponseCodes() {
        List<Map<String, Object>> latest = responseCodeMapper.findAllResponseCodes();
        replaceSnapshot(latest);
        return latest;
    }

    public List<Map<String, Object>> refreshResponseCodesAndPublish() {
        List<Map<String, Object>> latest = refreshResponseCodes();
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
        if (!preloadEnabled) {
            logger.info("Response code cache preload skipped");
            return;
        }
        try {
            refreshResponseCodes();
        } catch (RuntimeException ex) {
            recordFailure(ex);
            if (failFastOnStartup) {
                throw ex;
            }
            logger.warn("Response code cache preload failed. Existing cache is preserved because fail-fast is disabled.", ex);
        }
    }

    @Scheduled(
            fixedRateString = "${cpf.cmn.cache.periodic-refresh-millis:1800000}",
            initialDelayString = "${cpf.cmn.cache.periodic-refresh-initial-delay-millis:1800000}")
    public void scheduledReloadResponseCodes() {
        try {
            refreshResponseCodes();
        } catch (RuntimeException ex) {
            recordFailure(ex);
            logger.warn("Scheduled response code cache reload failed. Existing cache is preserved.", ex);
        }
    }

    private void scheduleSnapshotAfterCommit(List<Map<String, Object>> latest) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            replaceSnapshot(latest);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                replaceSnapshot(latest);
            }
        });
    }

    private void replaceSnapshot(List<Map<String, Object>> latest) {
        Cache cache = requireCache();
        cache.clear();
        cache.put(ALL_KEY, latest);
        for (Map<String, Object> row : latest) {
            String code = mapText(row, "responseCode", "response_code");
            if (CpfStrings.hasText(code)) {
                cache.put(CODE_PREFIX + CpfStrings.normalizeCode(code), row);
            }
        }
        cacheVersion.incrementAndGet();
        lastSynchronizedAt = Instant.now();
        lastRefreshFailure = null;
    }

    private Cache requireCache() {
        Cache cache = cacheManager.getCache(CACHE_NAME);
        if (cache == null) {
            throw new IllegalStateException("Required cache is not configured: " + CACHE_NAME);
        }
        return cache;
    }

    private void recordFailure(RuntimeException ex) {
        lastRefreshFailure = ex.getClass().getSimpleName();
    }

    private String mapText(Map<String, Object> row, String camelKey, String snakeKey) {
        Object value = row.get(camelKey);
        if (value == null) {
            value = row.get(snakeKey);
        }
        return value == null ? null : String.valueOf(value);
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
