package com.cpf.gateway.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.cpf.core.api.gateway.CpfGatewayHealthStatus;
import com.cpf.core.api.gateway.CpfGatewayLoadBalancePolicy;
import com.cpf.core.api.gateway.CpfGatewayTargetSelectionPort.SelectionRequest;
import com.cpf.core.api.gateway.CpfGatewayTargetSelectionPort.TargetCandidate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CpfGatewayTargetSelectorCanaryTest {
    private final CpfGatewayTargetSelector selector = new CpfGatewayTargetSelector();

    @Test
    void sameRequestKeyAlwaysSelectsSameCanaryCohort() {
        SelectionRequest request = request("TX-20260730-0001", List.of(
                candidate("stable", 0, true), candidate("canary", 20, true)));
        String first = selector.select(request).instanceId();
        for (int i = 0; i < 20; i++) assertEquals(first, selector.select(request).instanceId());
    }

    @Test
    void unavailableCanaryFallsBackToStablePool() {
        for (int i = 0; i < 500; i++) {
            SelectionRequest request = request("KEY-" + i, List.of(
                    candidate("stable", 0, true), candidate("canary", 100, false)));
            assertEquals("stable", selector.select(request).instanceId());
        }
    }

    @Test
    void rejectsCanaryPercentAboveGroupCeiling() {
        SelectionRequest request = request("KEY", List.of(
                candidate("stable", 0, true), candidate("c1", 60, true), candidate("c2", 50, true)));
        assertThrows(IllegalArgumentException.class, () -> selector.select(request));
    }

    private static SelectionRequest request(String key, List<TargetCandidate> candidates) {
        return new SelectionRequest("GROUP", CpfGatewayLoadBalancePolicy.ROUND_ROBIN, key,
                candidates, Map.of("transactionId", key), OffsetDateTime.now());
    }

    private static TargetCandidate candidate(String id, int canaryPercent, boolean routable) {
        return new TargetCandidate(id, "127.0.0.1", 8080, 100, 0,
                routable ? CpfGatewayHealthStatus.UP : CpfGatewayHealthStatus.DOWN,
                "CLOSED", true, false, false, 0, 1.0, canaryPercent, OffsetDateTime.now());
    }
}
