package com.cpf.starter.openapi.webmvc.internal;

import com.cpf.starter.openapi.webmvc.api.CpfOpenApiOperations;
import com.cpf.starter.openapi.webmvc.api.CpfOpenApiSnapshot;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

final class CpfOpenApiHealthIndicator implements HealthIndicator {
    private final CpfOpenApiOperations operations;
    CpfOpenApiHealthIndicator(CpfOpenApiOperations operations) { this.operations = operations; }

    @Override
    public Health health() {
        CpfOpenApiSnapshot value = operations.snapshot();
        Health.Builder builder = switch (value.status()) {
            case UP -> Health.up();
            case DEGRADED, UNKNOWN -> Health.unknown();
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
