package com.cpf.admin.opr.service;

import com.cpf.admin.opr.dto.AdmCacheControlResponse;
import com.cpf.admin.opr.dto.AdmCacheSummaryResponse;
import com.cpf.data.cache.rediscommon.CpfCacheInvalidationCoordinator;
import com.cpf.common.code.service.CodeCacheService;
import com.cpf.common.parameter.service.ConfigCacheService;
import com.cpf.common.message.service.MessageCacheService;
import com.cpf.common.message.service.ResponseCodeCacheService;
import com.cpf.common.code.reference.service.CacheRefreshEventListener;
import com.cpf.common.runtime.cache.CpfCommonCacheRefreshPublisher;
import com.cpf.data.cache.api.CpfCacheHealth;
import com.cpf.data.cache.api.CpfCacheInvalidationEvent;
import com.cpf.data.cache.api.CpfCacheInvalidationPort;
import com.cpf.data.cache.api.CpfCacheMetricsSnapshot;
import com.cpf.data.cache.api.CpfCache;
import com.cpf.data.cache.api.CpfCacheKey;
import com.cpf.foundation.util.CpfStrings;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.ObjectProvider;
import com.cpf.foundation.annotation.CpfService;

/** Business Cache와 Provider Cache를 하나의 운영 모델로 조회·제어합니다. */
@CpfService
public class AdmCacheOperationService extends com.cpf.admin.common.base.AdmBaseService {
    private final CodeCacheService code;
    private final MessageCacheService message;
    private final ResponseCodeCacheService responseCode;
    private final ConfigCacheService config;
    private final CpfCommonCacheRefreshPublisher refreshPublisher;
    private final CacheRefreshEventListener refreshListener;
    private final CpfCache provider;
    private final CpfCacheInvalidationPort invalidations;
    private final CpfCacheInvalidationCoordinator coordinator;

    public AdmCacheOperationService(
            CodeCacheService code,
            MessageCacheService message,
            ResponseCodeCacheService responseCode,
            ConfigCacheService config,
            CpfCommonCacheRefreshPublisher refreshPublisher,
            CacheRefreshEventListener refreshListener,
            ObjectProvider<CpfCache> provider,
            ObjectProvider<CpfCacheInvalidationPort> invalidations,
            ObjectProvider<CpfCacheInvalidationCoordinator> coordinator) {
        this.code = code;
        this.message = message;
        this.responseCode = responseCode;
        this.config = config;
        this.refreshPublisher = refreshPublisher;
        this.refreshListener = refreshListener;
        this.provider = provider.getIfAvailable();
        this.invalidations = invalidations.getIfAvailable();
        this.coordinator = coordinator.getIfAvailable();
    }

    public AdmCacheSummaryResponse summary() {
        List<AdmCacheSummaryResponse.DomainStatus> domains = List.of(
                domain("CODE", code.cacheStatus(), code.getCodesByKey("USER_STATUS")),
                domain("MESSAGE", message.cacheStatus(), message.getMessageByKeyAndLocale("MCMN000001", "ko")),
                domain("RESPONSE_CODE", responseCode.cacheStatus(), responseCode.getResponseCode("ECPF010004")),
                domain("CONFIG", config.cacheStatus(), config.getConfigByKey("cpf.LOGIN.MAX_FAIL_COUNT")),
                domain("DURABLE_REFRESH", refreshPublisher.status(), refreshListener.status()));
        CpfCacheHealth health = provider == null
                ? new CpfCacheHealth(false, "NONE", "NONE", false, invalidations != null, 0,
                        List.of("CACHE_PROVIDER_NOT_CONFIGURED"), Instant.now())
                : provider.health();
        CpfCacheMetricsSnapshot metrics = provider == null
                ? new CpfCacheMetricsSnapshot("NONE", 0, 0, 0, 0, 0, 0, backlog(), Instant.now())
                : provider.metrics();
        String messageText = health.ready() && coordinator != null
                ? "정상"
                : "Cache Provider와 Durable Coordinator 상태를 확인하세요.";
        return new AdmCacheSummaryResponse(true, health, metrics, backlog(), domains, messageText);
    }

    public AdmCacheControlResponse refresh(String target, String operator, String reason) {
        String normalized = CpfStrings.normalizeCode(target);
        if (!CpfStrings.hasText(normalized)) normalized = "ALL";
        long affected = 0;
        if ("ALL".equals(normalized) || "CODE".equals(normalized)) { code.refreshCodesAndPublish(); affected++; }
        if ("ALL".equals(normalized) || "MESSAGE".equals(normalized)) { message.refreshMessagesAndPublish(); affected++; }
        if ("ALL".equals(normalized) || "RESPONSE_CODE".equals(normalized)) { responseCode.refreshResponseCodesAndPublish(); affected++; }
        if ("ALL".equals(normalized) || "CONFIG".equals(normalized)) { config.refreshConfigsAndPublish(); affected++; }
        return result("REFRESH", normalized, affected, null, "업무 Cache의 Durable Refresh Event를 발행했습니다.");
    }

    public AdmCacheControlResponse evictKey(
            String tenant, String namespace, String key, long version, String operator, String reason) {
        CpfCacheInvalidationCoordinator active = requireCoordinator();
        CpfCacheKey cacheKey = new CpfCacheKey(namespace, key, tenant);
        CpfCacheInvalidationEvent event = active.request(
                UUID.randomUUID().toString(), cacheKey, Math.max(0, version), reason, operator);
        return result("EVICT_KEY", cacheKey.canonical(), 1, event,
                "Durable 원장 기록과 현재 Instance 무효화를 완료했습니다.");
    }

    public AdmCacheControlResponse evictNamespace(
            String tenant, String namespace, long version, String operator, String reason) {
        CpfCacheInvalidationCoordinator active = requireCoordinator();
        CpfCacheInvalidationEvent event = active.requestNamespace(
                UUID.randomUUID().toString(), tenant, namespace, Math.max(0, version), reason, operator);
        return result("EVICT_NAMESPACE", event.tenantId() + ":" + event.namespace(), 1, event,
                "Namespace 무효화를 Durable 원장에 기록하고 현재 Instance에 적용했습니다.");
    }

    public AdmCacheControlResponse reconcile(String operator, String reason) {
        CpfCacheInvalidationCoordinator active = requireCoordinator();
        long before = backlog();
        int applied = active.reconcileNow();
        long after = backlog();
        return new AdmCacheControlResponse(
                "RECONCILE", active.consumerId(), true, applied, null, after, Instant.now(),
                before == 0 ? "재조정 대상이 없습니다." : "Durable Event 재조정을 수행했습니다.");
    }

    private long backlog() {
        return invalidations == null || coordinator == null
                ? 0
                : invalidations.backlog(coordinator.consumerId());
    }

    private AdmCacheControlResponse result(
            String operation, String target, long affected, CpfCacheInvalidationEvent event, String messageText) {
        return new AdmCacheControlResponse(
                operation, target, true, affected, event, backlog(), Instant.now(), messageText);
    }

    private CpfCacheInvalidationCoordinator requireCoordinator() {
        if (provider == null) throw new IllegalStateException("CPF Cache Provider가 구성되지 않았습니다.");
        if (invalidations == null || coordinator == null) {
            throw new IllegalStateException("CPF Durable Cache Invalidation Coordinator가 구성되지 않았습니다.");
        }
        return coordinator;
    }

    private AdmCacheSummaryResponse.DomainStatus domain(String name, Object status, Object sample) {
        return new AdmCacheSummaryResponse.DomainStatus(name, String.valueOf(status), safe(sample));
    }

    private String safe(Object value) {
        String text = String.valueOf(value)
                .replaceAll("(?i)(password|token|secret)[=:][^, }]+", "$1=[REDACTED]");
        return text.substring(0, Math.min(text.length(), 300));
    }
}
