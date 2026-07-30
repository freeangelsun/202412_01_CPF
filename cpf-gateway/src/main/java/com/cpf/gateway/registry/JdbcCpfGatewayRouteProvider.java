package com.cpf.gateway.registry;

import com.cpf.core.api.gateway.CpfGatewayRegistryPort;
import com.cpf.core.api.gateway.CpfGatewayRoute;
import com.cpf.core.api.gateway.CpfGatewayRouteProvider;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Gateway Registry의 승인·적용 완료 Binding을 실제 Proxy Snapshot으로 투영합니다.
 *
 * <p>등록만 된 Route는 외부로 공개하지 않습니다. ACTIVE 상태, 유효기간, Gateway 허용,
 * 대상별 Apply ACK가 모두 정상인 Binding만 Last Known Good Snapshot 후보가 됩니다.</p>
 */
@Component
@Primary
public final class JdbcCpfGatewayRouteProvider implements CpfGatewayRouteProvider {
    private final CpfGatewayRegistryPort registry;

    public JdbcCpfGatewayRouteProvider(CpfGatewayRegistryPort registry) {
        this.registry = registry;
    }

    @Override
    public Map<String, CpfGatewayRoute> loadPublicRoutes() {
        return loadRoutes(true);
    }

    @Override
    public Map<String, CpfGatewayRoute> loadCandidateRoutes() {
        return loadRoutes(false);
    }

    private Map<String, CpfGatewayRoute> loadRoutes(boolean requireCurrentInstanceAck) {
        OffsetDateTime now = OffsetDateTime.now();
        String currentInstanceId = com.cpf.core.api.runtime.CpfInstanceIdentity.current().serverInstanceId();
        Map<String, CpfGatewayRoute> result = new LinkedHashMap<>();
        for (CpfGatewayRegistryPort.GatewayBinding binding
                : registry.findBindings(null, null, "ACTIVE", 10_000)) {
            if (!binding.gatewayAllowed()
                    || binding.effectiveFrom() != null && binding.effectiveFrom().isAfter(now)
                    || binding.effectiveTo() != null && !binding.effectiveTo().isAfter(now)) {
                continue;
            }
            if (requireCurrentInstanceAck) {
                List<CpfGatewayRegistryPort.ApplyStatus> applies = registry.findApplyStatuses(binding.bindingId(), 10_000);
                boolean currentApplied = applies.stream()
                        .filter(status -> currentInstanceId.equals(status.gatewayInstanceId()))
                        .anyMatch(status -> isApplied(status, binding.routeVersion()));
                if (!currentApplied) continue;
            }
            CpfGatewayRoute route = toRoute(binding);
            CpfGatewayRoute previous = result.putIfAbsent(route.standardExecutionId(), route);
            if (previous != null) {
                throw new IllegalStateException("Gateway standardExecutionId 중복: " + route.standardExecutionId());
            }
            for (CpfGatewayRoute existing : result.values()) {
                if (existing != route && existing.routeKey().equals(route.routeKey())) {
                    throw new IllegalStateException("Gateway Route match 충돌: " + route.routeKey());
                }
            }
        }
        return Map.copyOf(result);
    }

    @Override
    public CpfGatewayRoute resolve(Map<String, CpfGatewayRoute> snapshot, String executionId) {
        CpfGatewayRoute route = snapshot.get(executionId);
        if (route == null || !route.enabled()) {
            throw new IllegalArgumentException("승인·적용 완료된 Gateway Route가 없습니다. id=" + executionId);
        }
        return route;
    }

    private static boolean isApplied(CpfGatewayRegistryPort.ApplyStatus status, String expectedVersion) {
        return "APPLIED".equalsIgnoreCase(status.status())
                && expectedVersion.equals(status.expectedVersion())
                && expectedVersion.equals(status.appliedVersion())
                && status.acknowledgedAt() != null;
    }

    private static String blankTo(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private static CpfGatewayRoute toRoute(CpfGatewayRegistryPort.GatewayBinding b) {
        String executionId = b.routeId();
        return new CpfGatewayRoute(
                executionId,
                b.serviceId(),
                b.httpMethod(),
                blankTo(b.targetPath(), b.pathPattern()),
                b.routeId(),
                b.authorizationPolicyId(),
                b.approvalId() != null && !b.approvalId().isBlank(),
                b.routeVersion(),
                b.bindingId(),
                b.environmentCode(),
                b.hostPattern(),
                b.pathPattern(),
                b.apiVersion(),
                b.serverGroupId(),
                b.ingressProtocol(),
                b.targetProtocol(),
                b.tlsPolicyId(),
                b.authenticationPolicyId(),
                b.authorizationPolicyId(),
                b.headerPolicyId(),
                b.rateLimitPolicyId(),
                b.healthPolicyId(),
                b.connectTimeoutMs(),
                b.responseTimeoutMs(),
                b.overallTimeoutMs(),
                b.maxRetryCount(),
                b.idempotent(),
                b.failoverGroupId(),
                true,
                b.version());
    }
}
