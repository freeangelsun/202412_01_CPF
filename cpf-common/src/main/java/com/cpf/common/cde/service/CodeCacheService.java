package com.cpf.common.cde.service;

import com.cpf.common.cde.dto.CommonCodeRequest;
import com.cpf.common.cde.mapper.CodeMapper;
import com.cpf.common.ref.service.CacheRefreshEventPublisher;
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
 * CPF 공통 코드 Cache 서비스입니다.
 *
 * <p>Startup/Periodic refresh는 Spring self-invocation에 의존하지 않고 CacheManager에 명시적으로 Snapshot을 적재합니다.
 * 변경 Transaction이 rollback 되면 기존 Cache를 유지하며, commit 이후에만 무효화·재적재합니다.</p>
 */
@Service
@ConditionalOnExpression("'${cpf.common.runtime-mode:product}'.toLowerCase() == 'product'")
public class CodeCacheService extends com.cpf.common.common.base.CmnBaseService {
    private static final Logger logger = LoggerFactory.getLogger(CodeCacheService.class);
    private static final String CACHE_NAME = "codeCache";
    private static final String ALL_KEY = "ALL";
    private static final String CODE_PREFIX = "CODE:";

    private final CodeMapper codeMapper;
    private final CacheManager cacheManager;
    private final CacheRefreshEventPublisher cacheRefreshEventPublisher;
    private final AtomicLong cacheVersion = new AtomicLong();
    private volatile Instant lastSynchronizedAt;
    private volatile String lastRefreshFailure;

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

    @Cacheable(value = CACHE_NAME, key = "'" + ALL_KEY + "'")
    public List<Map<String, Object>> getAllCodes() {
        logger.info("Cache Miss: Fetching all codes from database");
        return codeMapper.findAllCodes();
    }

    @Cacheable(value = CACHE_NAME, key = "'" + CODE_PREFIX + "' + #p0")
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
        scheduleReloadAfterCommit();
        publishRefreshEvent("CREATE", request.getCodeKey(), request.getRequestUser());
        return getCodeById(request.getCodeId());
    }

    @Transactional(transactionManager = "cmnTransactionManager")
    public Map<String, Object> updateCode(Long codeId, CommonCodeRequest request) {
        codeMapper.updateCode(codeId, request);
        scheduleReloadAfterCommit();
        publishRefreshEvent("UPDATE", request.getCodeKey(), request.getRequestUser());
        return getCodeById(codeId);
    }

    @Transactional(transactionManager = "cmnTransactionManager")
    public List<Map<String, Object>> deleteCode(Long codeId) {
        Map<String, Object> beforeDelete = getCodeById(codeId);
        String eventKey = beforeDelete == null ? String.valueOf(codeId) : mapValue(beforeDelete, "codeKey", "code_key");
        codeMapper.deleteCode(codeId);
        scheduleReloadAfterCommit();
        publishRefreshEvent("DELETE", eventKey, "SYSTEM");
        return codeMapper.findAllCodes();
    }

    /** 운영 수동 Refresh. 실제 Cache 적재가 완료된 Snapshot을 반환합니다. */
    public List<Map<String, Object>> reloadCodes() {
        return reloadSnapshot();
    }

    public List<Map<String, Object>> refreshCodes() {
        return reloadSnapshot();
    }

    public List<Map<String, Object>> refreshCodesAndPublish() {
        List<Map<String, Object>> latestCodes = reloadSnapshot();
        publishRefreshEvent("MANUAL_REFRESH", "ALL", "SYSTEM");
        return latestCodes;
    }

    /** 운영자가 Cache Version과 마지막 동기화/실패를 구분할 수 있는 상태입니다. */
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
            logger.info("Code cache preload skipped");
            return;
        }
        logger.info("Initializing code cache at startup");
        try {
            reloadSnapshot();
        } catch (RuntimeException ex) {
            lastRefreshFailure = ex.getClass().getSimpleName();
            if (failFastOnStartup) throw ex;
            logger.warn("Code cache preload failed. Application will continue because fail-fast is disabled.", ex);
        }
    }

    @Scheduled(
            fixedRateString = "${cpf.cmn.cache.periodic-refresh-millis:1800000}",
            initialDelayString = "${cpf.cmn.cache.periodic-refresh-initial-delay-millis:1800000}")
    public void scheduledReloadCodes() {
        logger.info("Scheduled cache reload triggered");
        try {
            reloadSnapshot();
        } catch (RuntimeException ex) {
            lastRefreshFailure = ex.getClass().getSimpleName();
            logger.warn("Scheduled code cache reload failed. Existing cache is preserved.", ex);
        }
    }

    private List<Map<String, Object>> reloadSnapshot() {
        // DB Snapshot을 먼저 읽고 성공한 경우에만 기존 Cache를 교체하여 DB 장애가 정상 Cache를 지우지 않게 합니다.
        List<Map<String, Object>> snapshot = List.copyOf(codeMapper.findAllCodes());
        Cache cache = cacheManager.getCache(CACHE_NAME);
        if (cache == null) throw new IllegalStateException("Required cache is missing: " + CACHE_NAME);
        cache.clear();
        cache.put(ALL_KEY, snapshot);
        for (Map<String, Object> row : snapshot) {
            String key = mapValue(row, "codeKey", "code_key");
            if (key != null && !key.isBlank()) cache.put(CODE_PREFIX + key, row);
        }
        cacheVersion.incrementAndGet();
        lastSynchronizedAt = Instant.now();
        lastRefreshFailure = null;
        return snapshot;
    }

    private void scheduleReloadAfterCommit() {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            reloadAfterCommitSafely();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                reloadAfterCommitSafely();
            }
        });
    }

    private void reloadAfterCommitSafely() {
        try {
            reloadSnapshot();
        } catch (RuntimeException ex) {
            lastRefreshFailure = ex.getClass().getSimpleName();
            logger.error("Committed code mutation succeeded, but local cache reload failed. Existing cache state and remote refresh event must be inspected.", ex);
        }
    }

    private void publishRefreshEvent(String eventType, String eventKey, String requestUser) {
        cacheRefreshEventPublisher.publishAfterCommit(CACHE_NAME, eventType, eventKey, requestUser);
    }

    private String mapValue(Map<String, Object> source, String camelKey, String snakeKey) {
        Object value = source.get(camelKey);
        if (value == null) value = source.get(snakeKey);
        return value == null ? "" : String.valueOf(value);
    }
}
