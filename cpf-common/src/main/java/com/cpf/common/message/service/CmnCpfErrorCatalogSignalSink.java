package com.cpf.common.message.service;

import com.cpf.common.message.api.CpfErrorCatalogSignalSink;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** 민감 원문 없이 Error Catalog fallback을 audit log와 metric으로 남깁니다. */
@Component
public final class CmnCpfErrorCatalogSignalSink implements CpfErrorCatalogSignalSink {
    private static final Logger log = LoggerFactory.getLogger(CmnCpfErrorCatalogSignalSink.class);
    private final MeterRegistry meterRegistry;

    public CmnCpfErrorCatalogSignalSink(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public void catalogFallback(String reason, String errorReference) {
        String safeReason = safe(reason, "UNKNOWN");
        String safeReference = safe(errorReference, "UNSPECIFIED");
        Counter.builder("cpf.common.error_catalog.fallback")
                .tag("reason", safeReason)
                .register(meterRegistry)
                .increment();
        // Exception message/SQL/secret를 기록하지 않고 식별 가능한 code/reference만 기록합니다.
        log.warn("CPF_ERROR_CATALOG_FALLBACK reason={} reference={}", safeReason, safeReference);
    }

    private String safe(String value, String fallback) {
        if (value == null || value.isBlank()) return fallback;
        return value.replaceAll("[^A-Za-z0-9_.:-]", "_");
    }
}
