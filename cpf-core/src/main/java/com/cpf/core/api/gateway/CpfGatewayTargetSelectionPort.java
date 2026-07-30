package com.cpf.core.api.gateway;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * Gateway와 내부 Remote Service Call이 공유하는 Target 선택 계약입니다.
 *
 * <p>구현은 {@code cpf-gateway} 또는 Service Call Adapter가 소유하며 Core는
 * topology-independent 입력·결과만 정의합니다.</p>
 */
public interface CpfGatewayTargetSelectionPort {
    SelectionResult select(SelectionRequest request);

    record SelectionRequest(
            String serverGroupId,
            CpfGatewayLoadBalancePolicy policy,
            String affinityKey,
            List<TargetCandidate> candidates,
            Map<String, String> attributes,
            OffsetDateTime requestedAt) {
        public SelectionRequest {
            candidates = candidates == null ? List.of() : List.copyOf(candidates);
            attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
            requestedAt = requestedAt == null ? OffsetDateTime.now() : requestedAt;
        }
    }

    record TargetCandidate(
            String instanceId,
            String host,
            int port,
            int weight,
            int priority,
            CpfGatewayHealthStatus healthStatus,
            String circuitState,
            boolean enabled,
            boolean draining,
            boolean maintenance,
            long activeRequests,
            double ewmaLatencyMs,
            int canaryPercent,
            OffsetDateTime lastProbeAt) {
        public boolean routable() {
            if (!enabled || draining || maintenance) return false;
            if (healthStatus == null || !healthStatus.routable()) return false;
            return !"OPEN".equalsIgnoreCase(circuitState) && !"FORCED_OPEN".equalsIgnoreCase(circuitState);
        }
    }

    record SelectionResult(
            String serverGroupId,
            String instanceId,
            String host,
            int port,
            CpfGatewayLoadBalancePolicy policy,
            String reason,
            int eligibleCount,
            OffsetDateTime selectedAt) {
        public static SelectionResult unavailable(String groupId, CpfGatewayLoadBalancePolicy policy, String reason) {
            return new SelectionResult(groupId, "", "", 0, policy, reason, 0, OffsetDateTime.now());
        }
        public boolean selected() { return instanceId != null && !instanceId.isBlank(); }
    }
}
