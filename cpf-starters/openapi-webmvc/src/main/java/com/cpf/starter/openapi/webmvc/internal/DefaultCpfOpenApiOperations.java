package com.cpf.starter.openapi.webmvc.internal;

import com.cpf.starter.openapi.webmvc.api.CpfOpenApiOperations;
import com.cpf.starter.openapi.webmvc.api.CpfOpenApiSnapshot;
import com.cpf.starter.openapi.webmvc.api.CpfOpenApiStatus;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

final class DefaultCpfOpenApiOperations implements CpfOpenApiOperations {
    private final CpfOpenApiWebMvcProperties properties;
    private final RequestMappingHandlerMapping mappings;
    private final Clock clock;
    private final AtomicReference<CpfOpenApiSnapshot> snapshot = new AtomicReference<>();
    private final ReentrantLock refreshLock = new ReentrantLock();

    DefaultCpfOpenApiOperations(CpfOpenApiWebMvcProperties properties,
                                RequestMappingHandlerMapping mappings,
                                Clock clock) {
        this.properties = Objects.requireNonNull(properties, "properties");
        this.mappings = Objects.requireNonNull(mappings, "mappings");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    @Override
    public CpfOpenApiSnapshot snapshot() {
        CpfOpenApiSnapshot current = snapshot.get();
        return current == null ? refresh("initial-inventory") : current;
    }

    @Override
    public CpfOpenApiSnapshot refresh(String reason) {
        String auditedReason = requiredReason(reason);
        refreshLock.lock();
        try {
            CpfOpenApiSnapshot current = snapshot.get();
            Instant now = clock.instant();
            if (current != null && now.isBefore(current.refreshedAt().plus(properties.getMinimumRefreshInterval()))) {
                return current;
            }
            try {
                long operationCount = mappings.getHandlerMethods().entrySet().stream()
                        .filter(entry -> hasRoutePattern(entry.getKey()))
                        .filter(entry -> !entry.getValue().getBeanType().getName().startsWith("org.springframework.boot.actuate"))
                        .count();
                CpfOpenApiStatus status = operationCount > 0 ? CpfOpenApiStatus.UP : CpfOpenApiStatus.DEGRADED;
                CpfOpenApiSnapshot updated = snapshot(status, operationCount, now, auditedReason, "");
                snapshot.set(updated);
                return updated;
            } catch (RuntimeException failure) {
                CpfOpenApiSnapshot updated = snapshot(CpfOpenApiStatus.DOWN, 0, now, auditedReason,
                        failure.getClass().getSimpleName());
                snapshot.set(updated);
                return updated;
            }
        } finally {
            refreshLock.unlock();
        }
    }

    private static boolean hasRoutePattern(RequestMappingInfo mappingInfo) {
        return !mappingInfo.getPatternValues().isEmpty();
    }

    private CpfOpenApiSnapshot snapshot(CpfOpenApiStatus status, long count, Instant at, String reason, String failureCode) {
        return new CpfOpenApiSnapshot(status, properties.isEnabled(), properties.isApiDocsEnabled(),
                properties.getApiDocsPath(), properties.getInstanceId(), count, at, reason, failureCode);
    }

    private static String requiredReason(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("refresh reason is required");
        String trimmed = value.trim();
        if (trimmed.length() > 500) throw new IllegalArgumentException("refresh reason is too long");
        return trimmed;
    }
}
