package com.cpf.gateway.runtime;

import static org.assertj.core.api.Assertions.assertThat;

import com.cpf.gateway.api.CpfGatewayEntryPolicyPort;
import com.cpf.platform.operations.runtimecontrol.CpfRuntimeDelivery;
import com.cpf.platform.operations.runtimecontrol.CpfRuntimePayload;
import com.cpf.gateway.config.CpfGatewaySafetyProperties;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class CpfGatewayEntryRuntimeApplierTest {
    @Test
    void approvedMaintenancePayloadAppliesWithCasAndReplayIsIdempotent() {
        DefaultCpfGatewayEntryPolicy policy = new DefaultCpfGatewayEntryPolicy(
                new CpfGatewaySafetyProperties());
        CpfGatewayEntryRuntimeApplier applier = new CpfGatewayEntryRuntimeApplier(policy);
        CpfRuntimeDelivery delivery = delivery(
                1L, "{\"expectedVersion\":0,\"retryAfterSeconds\":90,\"state\":\"MAINTENANCE\"}");

        assertThat(applier.apply(delivery).applied()).isTrue();
        assertThat(applier.apply(delivery).applied()).isTrue();
        assertThat(policy.snapshot().state()).isEqualTo(CpfGatewayEntryPolicyPort.State.MAINTENANCE);
        assertThat(policy.snapshot().retryAfter().toSeconds()).isEqualTo(90L);
    }

    @Test
    void staleExpectedVersionAndUnsupportedStateAreRejectedWithoutMutation() {
        DefaultCpfGatewayEntryPolicy policy = new DefaultCpfGatewayEntryPolicy(
                new CpfGatewaySafetyProperties());
        CpfGatewayEntryRuntimeApplier applier = new CpfGatewayEntryRuntimeApplier(policy);

        assertThat(applier.apply(delivery(
                2L, "{\"expectedVersion\":1,\"state\":\"ACTIVE\"}")).applied()).isFalse();
        assertThat(applier.apply(delivery(
                1L, "{\"expectedVersion\":0,\"state\":\"BROKEN\"}")).applied()).isFalse();
        assertThat(policy.snapshot().version()).isZero();
    }

    private static CpfRuntimeDelivery delivery(long version, String payload) {
        return new CpfRuntimeDelivery(
                "delivery-" + version, "change-" + version, "GATEWAY_ENTRY", "gwy-1",
                version, version, "request-hash", "payload-hash-" + version,
                CpfRuntimePayload.parse(payload), 1, Instant.now().plusSeconds(60));
    }
}
