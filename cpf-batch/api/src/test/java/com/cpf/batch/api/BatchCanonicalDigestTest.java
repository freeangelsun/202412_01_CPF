package com.cpf.batch.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class BatchCanonicalDigestTest {
    @Test
    void requestHashUsesApprovedBusinessPayloadNotOperationalAuditFields() {
        BatchApprovedLaunchRequest first = request(Map.of("businessDate", "2026-07-31"), "operator-a", "first approved run", 11L);
        BatchApprovedLaunchRequest replay = request(Map.of("businessDate", "2026-07-31"), "operator-b", "reconciliation retry", 12L);
        BatchApprovedLaunchRequest changed = request(Map.of("businessDate", "2026-08-01"), "operator-a", "first approved run", 11L);

        assertEquals(first.idempotencyScope(), replay.idempotencyScope());
        assertEquals(first.requestHash(), replay.requestHash());
        assertNotEquals(first.requestHash(), changed.requestHash());
    }

    @Test
    void planChecksumIsRecomputedAtConstructionBoundary() {
        BatchStepDefinition step = step();
        String checksum = BatchCanonicalDigest.planHash(
                "BAT.TEST", 1L, BatchExecutionTopology.LOCAL, List.of(step));

        new BatchExecutionPlan("BAT.TEST", 1L, BatchExecutionTopology.LOCAL, List.of(step), checksum);
        assertThrows(IllegalArgumentException.class, () ->
                new BatchExecutionPlan("BAT.TEST", 1L, BatchExecutionTopology.LOCAL, List.of(step), "0".repeat(64)));
    }

    private static BatchApprovedLaunchRequest request(
            Map<String, Object> parameters, String operatorId, String reason, long fencingToken) {
        BatchStepDefinition step = step();
        String checksum = BatchCanonicalDigest.planHash(
                "BAT.TEST", 1L, BatchExecutionTopology.LOCAL, List.of(step));
        BatchExecutionPlan plan = new BatchExecutionPlan(
                "BAT.TEST", 1L, BatchExecutionTopology.LOCAL, List.of(step), checksum);
        return new BatchApprovedLaunchRequest(
                definition(), plan, parameters, "APR-20260731-0001", operatorId, reason,
                "idem-20260731-0001", fencingToken);
    }

    private static BatchStepDefinition step() {
        return new BatchStepDefinition(
                "execute", BatchJobDefinition.ExecutorType.SPRING_BATCH, "JOB:BAT.TEST",
                Map.of("retryMaxAttempts", 2), 1, "", "", true);
    }

    private static BatchJobDefinition definition() {
        return new BatchJobDefinition(
                "BAT.TEST", 1L, "Batch test", BatchJobDefinition.ExecutorType.SPRING_BATCH,
                BatchJobDefinition.State.PUBLISHED, "BAT", "test",
                new BatchJobDefinition.Trigger(
                        BatchJobDefinition.TriggerType.MANUAL, "", "Asia/Seoul",
                        BatchJobDefinition.MisfirePolicy.FAIL_CLOSED, true),
                List.of(), List.of(), BatchJobDefinition.ResourcePolicy.defaults(),
                BatchJobDefinition.RecoveryPolicy.defaults(), BatchJobDefinition.AlertPolicy.defaults(),
                "JOB:BAT.TEST", "a".repeat(64), "maker", "approved definition",
                OffsetDateTime.parse("2026-07-31T00:00:00+09:00"), null, 1L);
    }

    @Test
    void rejectsNonPositivePartitionCount() {
        assertThrows(IllegalArgumentException.class, () -> new BatchStepDefinition(
                "step-1",
                BatchJobDefinition.ExecutorType.SPRING_BATCH,
                "JOB:BAT.TEST",
                Map.of(),
                0,
                "",
                "",
                true));
    }

}
