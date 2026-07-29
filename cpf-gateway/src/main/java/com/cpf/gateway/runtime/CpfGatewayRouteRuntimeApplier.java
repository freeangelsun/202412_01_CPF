package com.cpf.gateway.runtime;

import com.cpf.core.api.gateway.CpfGatewayRoute;
import com.cpf.core.api.runtimecontrol.CpfRuntimeApplyResult;
import com.cpf.core.api.runtimecontrol.CpfRuntimeChangeApplier;
import com.cpf.core.api.runtimecontrol.CpfRuntimeDelivery;
import com.cpf.gateway.route.CpfGatewayRouteSnapshot;

public final class CpfGatewayRouteRuntimeApplier implements CpfRuntimeChangeApplier {
    private final CpfGatewayRouteSnapshot snapshot;

    public CpfGatewayRouteRuntimeApplier(CpfGatewayRouteSnapshot snapshot) {
        this.snapshot = snapshot;
    }

    public String changeType() {
        return "GATEWAY_ROUTE";
    }

    public boolean supportsIdempotentReplay() {
        return true;
    }

    public boolean snapshotCapable() {
        return true;
    }

    public CpfRuntimeApplyResult apply(CpfRuntimeDelivery delivery) {
        try {
            var current = snapshot.refreshNow();
            String executionId = delivery.payload().text("standardExecutionId", "");
            String expectedVersion = delivery.payload().text("expectedRouteVersion", "");
            if (!executionId.isBlank()) {
                CpfGatewayRoute route = current.routes().get(executionId);
                if (route == null) {
                    throw new IllegalArgumentException("route missing");
                }
                if (!expectedVersion.isBlank() && !expectedVersion.equals(route.routeVersion())) {
                    throw new IllegalArgumentException("route version mismatch");
                }
            }
            if (delivery.payload().contains("expectedRouteCount")
                    && current.routes().size()
                    != delivery.payload().longValue("expectedRouteCount", -1)) {
                throw new IllegalArgumentException("route count mismatch");
            }
            return CpfRuntimeApplyResult.success(delivery.payloadHash());
        } catch (RuntimeException ex) {
            return CpfRuntimeApplyResult.failure(
                    "GATEWAY_ROUTE_REFRESH_FAILED",
                    "Gateway route snapshot refresh/검증 실패");
        }
    }
}
