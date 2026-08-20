package com.cpf.common.message.service;

import com.cpf.common.message.api.CpfErrorCatalogSignalSink;
import com.cpf.common.message.api.CpfMessageRecord;
import com.cpf.common.message.api.CpfResponseCodeRecord;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/** DB outage에서도 이미 적재된 catalog hit를 유지하고 DB version fence로 다중 인스턴스를 수렴시킵니다. */
@Component
final class CmnErrorCatalogCache implements CmnErrorCatalogStore {
    private final CmnErrorCatalogRepository repository;
    private final CpfErrorCatalogSignalSink signals;
    private final Map<String, CpfResponseCodeRecord> responses = new ConcurrentHashMap<>();
    private final Map<String, CpfMessageRecord> messages = new ConcurrentHashMap<>();
    private final AtomicReference<CmnErrorCatalogRepository.CatalogFence> fence = new AtomicReference<>();

    @Value("${cpf.common.error-catalog.preload:true}")
    private boolean preload;
    @Value("${cpf.common.error-catalog.fail-fast-on-preload:false}")
    private boolean failFastOnPreload;

    CmnErrorCatalogCache(CmnErrorCatalogRepository repository, CpfErrorCatalogSignalSink signals) {
        this.repository = repository;
        this.signals = signals;
    }

    @PostConstruct
    void initialize() {
        try {
            fence.set(repository.readFence());
            if (preload) {
                for (CpfResponseCodeRecord row : repository.searchResponseCodes(null)) responses.put(key(row.responseCode()), row);
                for (CpfMessageRecord row : repository.searchMessages(null, null)) messages.put(messageKey(row.messageCode(), row.locale()), row);
            }
        } catch (RuntimeException ex) {
            signals.catalogFallback("PRELOAD_FAILURE_" + ex.getClass().getSimpleName(), "CATALOG");
            if (failFastOnPreload) throw new IllegalStateException("CPF Common Error Catalog preload failed", null);
        }
    }

    @Override
    public CpfResponseCodeRecord response(String code) {
        String key = key(code);
        CpfResponseCodeRecord cached = responses.get(key);
        if (cached != null) return cached;
        try {
            CpfResponseCodeRecord loaded = repository.findResponseCode(key);
            if (loaded != null) responses.put(key, loaded);
            return loaded;
        } catch (RuntimeException ex) {
            signals.catalogFallback("RESPONSE_DB_FAILURE_" + ex.getClass().getSimpleName(), key);
            return responses.get(key);
        }
    }

    @Override
    public CpfMessageRecord message(String code, Locale locale) {
        String language = locale == null || locale.getLanguage().isBlank() ? "ko" : locale.getLanguage();
        String key = messageKey(code, language);
        CpfMessageRecord cached = messages.get(key);
        if (cached != null) return cached;
        try {
            CpfMessageRecord loaded = repository.findMessage(code, language);
            if (loaded != null) messages.put(key, loaded);
            return loaded;
        } catch (RuntimeException ex) {
            signals.catalogFallback("MESSAGE_DB_FAILURE_" + ex.getClass().getSimpleName(), key);
            return messages.get(key);
        }
    }

    void invalidateAll() {
        responses.clear();
        messages.clear();
        try { fence.set(repository.readFence()); }
        catch (RuntimeException ex) { signals.catalogFallback("FENCE_READ_FAILURE", "CATALOG"); }
    }

    @Scheduled(fixedDelayString = "${cpf.common.error-catalog.fence-interval-ms:5000}")
    void reconcileFence() {
        try {
            var current = repository.readFence();
            var previous = fence.getAndSet(current);
            if (previous != null && !previous.equals(current)) {
                responses.clear();
                messages.clear();
            }
        } catch (RuntimeException ex) {
            // 기존 cache는 유지하여 runtime DB outage 시 hit를 살립니다.
            signals.catalogFallback("FENCE_POLL_FAILURE_" + ex.getClass().getSimpleName(), "CATALOG");
        }
    }

    int responseCacheSize() { return responses.size(); }
    int messageCacheSize() { return messages.size(); }

    private String key(String code) {
        if (code == null) return "";
        return code.trim().toUpperCase(Locale.ROOT);
    }
    private String messageKey(String code, String locale) {
        return key(code) + "|" + (locale == null ? "ko" : locale.trim().toLowerCase(Locale.ROOT));
    }
}
