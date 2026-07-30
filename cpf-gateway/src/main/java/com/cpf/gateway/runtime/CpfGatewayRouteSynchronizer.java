package com.cpf.gateway.runtime;

import com.cpf.core.api.gateway.CpfGatewayRegistryPort;
import com.cpf.core.api.gateway.CpfGatewayRoute;
import com.cpf.core.api.runtime.CpfInstanceIdentity;
import com.cpf.core.api.runtime.CpfRuntimePolicyDistributionPort;
import com.cpf.gateway.config.CpfGatewaySafetyEnforcer;
import com.cpf.gateway.route.CpfGatewayRouteSnapshot;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** ACTIVE Binding Event를 Candidate 검증→Owner ACK→ACK 전용 Snapshot 재조회 순서로 적용합니다. */
@Component
public final class CpfGatewayRouteSynchronizer {
    private static final Logger log = LoggerFactory.getLogger(CpfGatewayRouteSynchronizer.class);
    private final CpfRuntimePolicyDistributionPort distribution;
    private final CpfGatewayRegistryPort registry;
    private final CpfGatewayRouteSnapshot snapshot;
    private final CpfGatewaySafetyEnforcer safety;

    public CpfGatewayRouteSynchronizer(
            CpfRuntimePolicyDistributionPort distribution,
            CpfGatewayRegistryPort registry,
            CpfGatewayRouteSnapshot snapshot,
            CpfGatewaySafetyEnforcer safety) {
        this.distribution = distribution;
        this.registry = registry;
        this.snapshot = snapshot;
        this.safety = safety;
    }

    @Scheduled(fixedDelayString = "${cpf.gateway.route-event.worker-millis:1000}")
    public void applyEvents() {
        String instanceId = CpfInstanceIdentity.current().serverInstanceId();
        for (CpfRuntimePolicyDistributionPort.DistributionEvent event
                : distribution.claimPending(instanceId, List.of("GATEWAY_ROUTE"), 50, 60)) {
            applyOne(instanceId, event);
        }
    }

    @Scheduled(fixedDelayString = "${cpf.gateway.route-reconcile-millis:30000}")
    public void reconcile() {
        String instanceId = CpfInstanceIdentity.current().serverInstanceId();
        try {
            CpfGatewayRouteSnapshot.Snapshot candidate = snapshot.prepareCandidate();
            validateCandidate(candidate);
            for (CpfGatewayRoute route : candidate.routes().values()) {
                registry.acknowledge(new CpfGatewayRegistryPort.ApplyAckCommand(
                        route.routeId(), instanceId, route.routeVersion(), route.routeVersion(), "APPLIED",
                        "", "", OffsetDateTime.now(ZoneOffset.UTC)));
            }
            CpfGatewayRouteSnapshot.Snapshot acknowledged = snapshot.refreshNow();
            validateCandidate(acknowledged);
            if (!same(candidate.routes(), acknowledged.routes())) {
                log.warn("Gateway Candidate와 ACK Snapshot이 다릅니다. ACK Snapshot만 공개합니다. candidate={}, acknowledged={}",
                        candidate.routes().size(), acknowledged.routes().size());
            }
        } catch (RuntimeException ex) {
            log.error("Gateway Route drift reconcile에 실패해 Last Known Good Snapshot을 유지합니다.", ex);
        }
    }

    private void applyOne(String instanceId, CpfRuntimePolicyDistributionPort.DistributionEvent event) {
        String routeVersion = event.metadata().getOrDefault("routeVersion", "");
        try {
            CpfGatewayRouteSnapshot.Snapshot candidate = snapshot.prepareCandidate();
            validateCandidate(candidate);
            CpfGatewayRoute target = candidate.routes().values().stream()
                    .filter(route -> event.aggregateId().equals(route.routeId()))
                    .findFirst().orElse(null);
            boolean shouldExist = "ACTIVE".equalsIgnoreCase(event.action());
            if (shouldExist && target == null) {
                throw new IllegalStateException("ACTIVE Binding이 Candidate Snapshot에 없습니다.");
            }
            if (!shouldExist && target != null) {
                throw new IllegalStateException("비활성 Binding이 Candidate Snapshot에 남아 있습니다.");
            }
            if (target != null) {
                if (routeVersion.isBlank()) routeVersion = target.routeVersion();
                if (!routeVersion.equals(target.routeVersion())) {
                    throw new IllegalStateException("Gateway Route version mismatch");
                }
            }
            registry.acknowledge(new CpfGatewayRegistryPort.ApplyAckCommand(
                    event.aggregateId(), instanceId, routeVersion, routeVersion,
                    shouldExist ? "APPLIED" : "REMOVED", "", "", OffsetDateTime.now(ZoneOffset.UTC)));
            CpfGatewayRouteSnapshot.Snapshot acknowledged = snapshot.refreshNow();
            validateCandidate(acknowledged);
            boolean exposed = acknowledged.routes().values().stream()
                    .anyMatch(route -> event.aggregateId().equals(route.routeId()));
            if (shouldExist != exposed) {
                throw new IllegalStateException("Gateway ACK Snapshot 반영 결과가 Event 기대 상태와 다릅니다.");
            }
            distribution.acknowledge(new CpfRuntimePolicyDistributionPort.AcknowledgeCommand(
                    event.eventId(), instanceId, event.fencingToken(), "APPLIED", "", "",
                    OffsetDateTime.now(ZoneOffset.UTC)));
        } catch (RuntimeException ex) {
            String message = sanitize(ex.getMessage());
            try {
                registry.acknowledge(new CpfGatewayRegistryPort.ApplyAckCommand(
                        event.aggregateId(), instanceId, routeVersion, "", "FAILED",
                        "GATEWAY_ROUTE_APPLY_FAILED", message, OffsetDateTime.now(ZoneOffset.UTC)));
            } catch (RuntimeException ackFailure) {
                ex.addSuppressed(ackFailure);
            }
            distribution.acknowledge(new CpfRuntimePolicyDistributionPort.AcknowledgeCommand(
                    event.eventId(), instanceId, event.fencingToken(), "FAILED",
                    "GATEWAY_ROUTE_APPLY_FAILED", message, OffsetDateTime.now(ZoneOffset.UTC)));
            log.error("Gateway Route Event 적용 실패. eventId={}, bindingId={}",
                    event.eventId(), event.aggregateId(), ex);
        }
    }


    private void validateCandidate(CpfGatewayRouteSnapshot.Snapshot candidate) {
        if (candidate == null) throw new IllegalStateException("Gateway Candidate Snapshot이 없습니다.");
        for (CpfGatewayRoute route : candidate.routes().values()) safety.validateRoute(route);
    }

    private static boolean same(Map<String, CpfGatewayRoute> left, Map<String, CpfGatewayRoute> right) {
        if (!left.keySet().equals(right.keySet())) return false;
        for (String key : left.keySet()) {
            CpfGatewayRoute a = left.get(key);
            CpfGatewayRoute b = right.get(key);
            if (!Objects.equals(a.routeId(), b.routeId())
                    || !Objects.equals(a.routeVersion(), b.routeVersion())
                    || a.expectedVersion() != b.expectedVersion()) return false;
        }
        return true;
    }

    private static String sanitize(String value) {
        String result = Objects.toString(value, "Gateway route apply failed")
                .replaceAll("(?i)(password|token|secret)=[^,\\s]+", "$1=***");
        return result.length() > 900 ? result.substring(0, 900) : result;
    }
}
