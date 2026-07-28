package com.cpf.gateway.route;

import com.cpf.core.api.gateway.CpfGatewayRoute;
import com.cpf.core.api.gateway.CpfGatewayRouteProvider;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 매 요청마다 ADM 또는 DB를 조회하지 않도록 마지막 정상 route snapshot을 메모리에 유지합니다.
 */
@Component
public class CpfGatewayRouteSnapshot {
    private static final Logger log = LoggerFactory.getLogger(CpfGatewayRouteSnapshot.class);

    private final CpfGatewayRouteProvider provider;
    private final AtomicReference<Snapshot> current = new AtomicReference<>(new Snapshot(Map.of(), Instant.EPOCH));
    @Value("${cpf.gateway.allow-empty-routes:false}")
    private boolean allowEmptyRoutes;

    public CpfGatewayRouteSnapshot(CpfGatewayRouteProvider provider) {
        this.provider = provider;
    }

    @PostConstruct
    public void initialize() {
        Map<String, CpfGatewayRoute> routes = provider.loadPublicRoutes();
        if ((routes == null || routes.isEmpty()) && !allowEmptyRoutes) {
            throw new IllegalStateException("Gateway route snapshot이 비어 있어 기동을 중단합니다. 운영에서는 빈 route로 기동할 수 없습니다.");
        }
        current.set(new Snapshot(routes == null ? Map.of() : Map.copyOf(routes), Instant.now()));
    }

    @Scheduled(fixedDelayString = "${cpf.gateway.route-refresh-millis:30000}")
    public void refresh() {
        try { refreshNow(); }
        catch (RuntimeException ex) { log.error("Gateway route snapshot 갱신에 실패해 마지막 정상본을 유지합니다.", ex); }
    }

    /** Runtime Control ACK 전에 실제 Provider 정본을 즉시 다시 읽습니다. 실패 시 마지막 정상본은 유지합니다. */
    public Snapshot refreshNow() {
        Map<String, CpfGatewayRoute> routes = provider.loadPublicRoutes();
        if (routes == null || (routes.isEmpty() && !allowEmptyRoutes)) {
            throw new IllegalStateException("Gateway route refresh 결과가 비어 있습니다.");
        }
        Snapshot replacement = new Snapshot(Map.copyOf(routes), Instant.now());
        current.set(replacement);
        return replacement;
    }

    public Snapshot current() {
        return current.get();
    }

    public CpfGatewayRoute resolve(String executionId) {
        return provider.resolve(current.get().routes(), executionId);
    }

    public record Snapshot(Map<String, CpfGatewayRoute> routes, Instant loadedAt) {
    }
}
