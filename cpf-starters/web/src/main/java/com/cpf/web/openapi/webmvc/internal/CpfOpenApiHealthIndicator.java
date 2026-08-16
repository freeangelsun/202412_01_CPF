package com.cpf.web.openapi.webmvc.internal;

import com.cpf.web.api.openapi.CpfOpenApiOperations;
import com.cpf.web.api.openapi.CpfOpenApiSnapshot;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;

final class CpfOpenApiHealthIndicator implements HealthIndicator {
    private final CpfOpenApiOperations operations;
    CpfOpenApiHealthIndicator(CpfOpenApiOperations operations) { this.operations = operations; }

    @Override
    public Health health() {
        CpfOpenApiSnapshot value = operations.snapshot();
        Health.Builder builder = switch (value.status()) {
            case UP -> Health.up();
            case DEGRADED -> Health.unknown();
            case DOWN -> Health.down();
        };
        return builder.withDetail("enabled", value.enabled())
                .withDetail("apiDocsEnabled", value.apiDocsEnabled())
                .withDetail("operationCount", value.operationCount())
                .withDetail("instanceId", value.instanceId())
                .withDetail("failureCode", value.failureCode())
                .build();
    }
}
