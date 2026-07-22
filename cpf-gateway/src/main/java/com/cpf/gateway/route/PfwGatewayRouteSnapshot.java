package cpf.pfw.gateway.route;

import cpf.pfw.common.gateway.CpfGatewayRoute;
import cpf.pfw.common.gateway.CpfGatewayRouteCatalog;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 매 요청마다 ADM 또는 DB를 조회하지 않도록 마지막 정상 route snapshot을 메모리에 유지합니다.
 */
@Component
public class PfwGatewayRouteSnapshot {
    private static final Logger log = LoggerFactory.getLogger(PfwGatewayRouteSnapshot.class);

    private final CpfGatewayRouteCatalog catalog;
    private final AtomicReference<Snapshot> current = new AtomicReference<>(new Snapshot(Map.of(), Instant.EPOCH));

    public PfwGatewayRouteSnapshot(CpfGatewayRouteCatalog catalog) {
        this.catalog = catalog;
    }

    @PostConstruct
    public void initialize() {
        refresh();
    }

    @Scheduled(fixedDelayString = "${cpf.gateway.route-refresh-millis:30000}")
    public void refresh() {
        try {
            current.set(new Snapshot(catalog.loadPublicRoutes(), Instant.now()));
        } catch (RuntimeException ex) {
            log.error("Gateway route snapshot 갱신에 실패해 마지막 정상본을 유지합니다.", ex);
        }
    }

    public Snapshot current() {
        return current.get();
    }

    public CpfGatewayRoute resolve(String executionId) {
        return catalog.resolve(current.get().routes(), executionId);
    }

    public record Snapshot(Map<String, CpfGatewayRoute> routes, Instant loadedAt) {
    }
}
