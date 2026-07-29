package com.cpf.batch.runtime;

import com.cpf.core.api.runtimecontrol.CpfRuntimeDelivery;
import com.cpf.core.api.runtimecontrol.CpfRuntimePayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BatchRuntimePolicyApplierTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    void appliesAllSupportedPoliciesToSingleSharedPolicy() {
        BatchRuntimePolicy policy = new BatchRuntimePolicy();

        assertTrue(apply(BatchRuntimePolicyApplier.SCHEDULE, policy, 1L, Map.of("enabled", false)).applied());
        assertTrue(apply(BatchRuntimePolicyApplier.CONCURRENCY, policy, 2L,
                Map.of("enabled", true, "maxConcurrency", 7)).applied());
        assertTrue(apply(BatchRuntimePolicyApplier.CALENDAR, policy, 3L, Map.of("enabled", false)).applied());
        assertTrue(apply(BatchRuntimePolicyApplier.CENTER_CUT, policy, 4L, Map.of("enabled", false)).applied());
        assertTrue(apply(BatchRuntimePolicyApplier.AGENT_POLICY, policy, 5L,
                Map.of("commandsEnabled", false, "logCollectionEnabled", false)).applied());

        BatchRuntimePolicy.Snapshot snapshot = policy.current();
        assertFalse(snapshot.schedulerEnabled());
        assertEquals(7, snapshot.workerConcurrencyLimit());
        assertFalse(snapshot.calendarEnabled());
        assertFalse(snapshot.centerCutEnabled());
        assertFalse(snapshot.agentCommandsEnabled());
        assertFalse(snapshot.agentLogCollectionEnabled());
        assertEquals(5L, snapshot.version());
    }

    @Test
    void enabledOnlyConcurrencyChangeUsesValidCanonicalDefault() {
        BatchRuntimePolicy policy = new BatchRuntimePolicy();
        assertTrue(apply(BatchRuntimePolicyApplier.CONCURRENCY, policy, 1L,
                Map.of("enabled", false)).applied());
        assertFalse(policy.current().workerEnabled());
        assertEquals(10_000, policy.current().workerConcurrencyLimit());
    }

    @Test
    void lowerVersionCannotOverwriteNewerVersion() {
        BatchRuntimePolicy policy = new BatchRuntimePolicy();
        policy.replaceAgentPolicy(20L, false, false);
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> policy.replaceSchedule(19L, false));
        assertTrue(error.getMessage().contains("과거 Batch runtime version"));
        assertEquals(20L, policy.current().version());
        assertFalse(policy.current().agentCommandsEnabled());
    }

    @Test
    void sameVersionReplayMustBeIdentical() {
        BatchRuntimePolicy policy = new BatchRuntimePolicy();
        policy.replaceSchedule(1L, false);
        assertEquals(policy.current(), policy.replaceSchedule(1L, false));
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> policy.replaceSchedule(1L, true));
        assertTrue(error.getMessage().contains("동일 Batch runtime version"));
        assertFalse(policy.current().schedulerEnabled());
    }

    @Test
    void unsupportedPartitionPolicyIsRejectedInsteadOfCreatingFakeConsumer() {
        BatchRuntimePolicy policy = new BatchRuntimePolicy();
        assertThrows(IllegalArgumentException.class,
                () -> new BatchRuntimePolicyApplier("BATCH_PARTITION", policy));
    }

    private com.cpf.core.api.runtimecontrol.CpfRuntimeApplyResult apply(
            String type, BatchRuntimePolicy policy, long version, Map<String, Object> payload) {
        return new BatchRuntimePolicyApplier(type, policy).apply(delivery(type, version, payload));
    }

    private CpfRuntimeDelivery delivery(String type, long version, Map<String, Object> payload) {
        return new CpfRuntimeDelivery("D-" + version, "C-" + version, type, "BAT-01",
                version, version, "request-" + version, "payload-" + version,
                CpfRuntimePayload.parse(OBJECT_MAPPER.valueToTree(payload).toString()),
                1, Instant.now().plusSeconds(60));
    }
}
