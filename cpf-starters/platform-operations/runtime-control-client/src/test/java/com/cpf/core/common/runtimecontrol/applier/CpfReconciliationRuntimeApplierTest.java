package com.cpf.core.common.runtimecontrol.applier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cpf.core.api.runtimecontrol.CpfRuntimeDelivery;
import com.cpf.core.api.runtimecontrol.CpfRuntimePayload;
import com.cpf.core.common.reconciliation.CpfReconciliationRuntimePolicy;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class CpfReconciliationRuntimeApplierTest {
    @Test
    void enabledPolicyWithoutAllowlistFailsClosed() {
        CpfReconciliationRuntimePolicy policy = new CpfReconciliationRuntimePolicy();
        CpfReconciliationRuntimeApplier applier = new CpfReconciliationRuntimeApplier(policy);

        var result = applier.apply(delivery("{\"enabled\":true}"));

        assertFalse(result.applied());
        assertEquals("RECONCILIATION_INVALID", result.errorCode());
        assertFalse(policy.current().enabled());
    }

    @Test
    void newSafetyFieldsAreAppliedToRuntimeSnapshot() {
        CpfReconciliationRuntimePolicy policy = new CpfReconciliationRuntimePolicy();
        CpfReconciliationRuntimeApplier applier = new CpfReconciliationRuntimeApplier(policy);

        var result =
                applier.apply(
                        delivery(
                                "{\"enabled\":true,\"unknownTypes\":[\"payment\"],"
                                        + "\"maxAttempts\":4,\"circuitFailureThreshold\":2,"
                                        + "\"circuitOpenMillis\":5000,\"batchSize\":7}"));

        assertTrue(result.applied());
        assertEquals(4, policy.current().maxAttempts());
        assertEquals(2, policy.current().circuitFailureThreshold());
        assertEquals(5_000L, policy.current().circuitOpenMillis());
        assertEquals(7, policy.current().batchSize());
        assertEquals(java.util.Set.of("PAYMENT"), policy.current().unknownTypes());
    }

    @Test
    void missingNewFieldsKeepBackwardCompatibleDefaults() {
        CpfReconciliationRuntimePolicy policy = new CpfReconciliationRuntimePolicy();
        CpfReconciliationRuntimeApplier applier = new CpfReconciliationRuntimeApplier(policy);

        var result =
                applier.apply(
                        delivery(
                                "{\"enabled\":true,\"unknownTypes\":[\"file\"],"
                                        + "\"manualResolutionRequired\":true}"));

        assertTrue(result.applied());
        assertEquals(8, policy.current().maxAttempts());
        assertEquals(3, policy.current().circuitFailureThreshold());
        assertEquals(30_000L, policy.current().circuitOpenMillis());
    }

    private CpfRuntimeDelivery delivery(String json) {
        return new CpfRuntimeDelivery(
                "delivery",
                "change",
                "RECONCILIATION",
                "instance",
                2L,
                3L,
                "request-hash",
                "payload-hash",
                2,
                CpfRuntimePayload.parse(json),
                1,
                Instant.now().plusSeconds(60));
    }
}
