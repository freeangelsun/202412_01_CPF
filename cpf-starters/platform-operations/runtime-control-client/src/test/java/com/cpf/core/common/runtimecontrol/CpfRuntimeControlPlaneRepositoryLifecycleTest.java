package com.cpf.core.common.runtimecontrol;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CpfRuntimeControlPlaneRepositoryLifecycleTest {

    @Test
    void expiryPreservesInFlightOrRestartSideEffectsAsUnknown() {
        assertEquals("EXPIRED", CpfRuntimeControlPlaneRepository.deriveExpiredChangeState(0, 0));
        assertEquals("UNKNOWN_RESULT", CpfRuntimeControlPlaneRepository.deriveExpiredChangeState(1, 0));
        assertEquals("UNKNOWN_RESULT", CpfRuntimeControlPlaneRepository.deriveExpiredChangeState(0, 1));
        assertEquals("EXPIRED", CpfRuntimeControlPlaneRepository.deriveExpiredChangeState(1, 0, 0));
        assertEquals("UNKNOWN_RESULT", CpfRuntimeControlPlaneRepository.deriveExpiredChangeState(1, 1, 0));
    }

    @Test
    void auditTimestampUsesVendorPortableSecondPrecision() {
        Instant value = Instant.parse("2026-08-05T01:02:03.987654321Z");
        assertEquals(Instant.parse("2026-08-05T01:02:03Z"),
                CpfRuntimeControlPlaneRepository.canonicalAuditInstant(value));
    }

    @Test
    void poisonIsTerminalFailedAndAckTimeoutUnknownWins() {
        assertEquals("FAILED", CpfRuntimeControlPlaneRepository.deriveChangeState(2, 1, 0, 1, 0, 0));
        assertEquals("UNKNOWN_RESULT", CpfRuntimeControlPlaneRepository.deriveChangeState(2, 1, 0, 1, 1, 0));
        assertEquals("PARTIAL", CpfRuntimeControlPlaneRepository.deriveChangeState(2, 1, 1, 0, 0, 0));
        assertEquals("SUCCESS", CpfRuntimeControlPlaneRepository.deriveChangeState(2, 2, 0, 0, 0, 0));
    }

    @Test
    void findOperationLazilyExpiresStaleProcessingLedgerBeforeReplay() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        CpfRuntimeControlPlaneRepository repository =
                new CpfRuntimeControlPlaneRepository(jdbc, new ObjectMapper());
        when(jdbc.queryForList(anyString(), eq("operation-1"))).thenReturn(List.of(Map.of(
                "operation_id", "operation-1",
                "request_hash", "hash-1",
                "result_state", "EXPIRED",
                "expires_at", Instant.now().minusSeconds(1))));

        assertEquals("EXPIRED", repository.findOperation("operation-1").orElseThrow().get("result_state"));

        var order = inOrder(jdbc);
        order.verify(jdbc).update(anyString(), eq("operation-1"));
        order.verify(jdbc).queryForList(anyString(), eq("operation-1"));
    }

    @Test
    void terminalOperationCannotBeOverwrittenByDifferentResult() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        CpfRuntimeControlPlaneRepository repository =
                new CpfRuntimeControlPlaneRepository(jdbc, new ObjectMapper());
        when(jdbc.update(anyString(), any(), any(), any(), anyString())).thenReturn(0);
        when(jdbc.queryForList(anyString(), eq("operation-1"))).thenReturn(List.of(Map.of(
                "operation_id", "operation-1",
                "request_hash", "hash-1",
                "entity_id", "change-1",
                "result_state", "SUCCESS")));

        assertThrows(IllegalStateException.class, () ->
                repository.completeOperation("operation-1", "change-2", "FAILED", "{}"));
    }

    @Test
    void sameTerminalOperationCompletionIsIdempotent() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        CpfRuntimeControlPlaneRepository repository =
                new CpfRuntimeControlPlaneRepository(jdbc, new ObjectMapper());
        when(jdbc.update(anyString(), any(), any(), any(), anyString())).thenReturn(0);
        when(jdbc.queryForList(anyString(), eq("operation-1"))).thenReturn(List.of(Map.of(
                "operation_id", "operation-1",
                "request_hash", "hash-1",
                "entity_id", "change-1",
                "result_state", "SUCCESS")));

        repository.completeOperation("operation-1", "change-1", "SUCCESS", "{}");
        verify(jdbc).queryForList(anyString(), eq("operation-1"));
    }
    @Test
    void cancellationPreservesPartialAndUnknownSideEffectEvidence() {
        assertEquals("CANCELLED",CpfRuntimeControlPlaneRepository.deriveCancellationState(0,0));
        assertEquals("PARTIAL",CpfRuntimeControlPlaneRepository.deriveCancellationState(1,0));
        assertEquals("UNKNOWN_RESULT",CpfRuntimeControlPlaneRepository.deriveCancellationState(0,1));
        assertEquals("UNKNOWN_RESULT",CpfRuntimeControlPlaneRepository.deriveCancellationState(1,1));
    }



    @Test
    void deterministicValidationFailuresArePoisonedWithoutBlindRetry() {
        assertTrue(CpfRuntimeControlPlaneRepository.isPermanentFailure("RECONCILIATION_INVALID"));
        assertTrue(CpfRuntimeControlPlaneRepository.isPermanentFailure("PAYLOAD_SCHEMA_UNSUPPORTED"));
        assertTrue(CpfRuntimeControlPlaneRepository.isPermanentFailure("DELIVERY_EXPIRED"));
        org.junit.jupiter.api.Assertions.assertFalse(
                CpfRuntimeControlPlaneRepository.isPermanentFailure("TRANSIENT_FAILURE"));
    }

    @Test
    void desiredStateUpdatesAreMonotonicAcrossScheduledActivation() {
        assertTrue(CpfRuntimeControlPlaneRepository.APPLY_INSTANCE_DESIRED_SQL.contains("desired_version<=?"));
        assertTrue(CpfRuntimeControlPlaneRepository.APPLY_FEATURE_DESIRED_SQL.contains("desired_version<=?"));
    }

    @Test
    void clockSkewValidationFailsClosedWithoutDurationOverflow() {
        java.time.Instant now=java.time.Instant.parse("2026-08-05T00:00:00Z");
        assertEquals(1000L, CpfRuntimeControlPlaneRepository.validatedClockSkewMillis(
                now.minusSeconds(1), now, false, "instance-1"));
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> CpfRuntimeControlPlaneRepository.validatedClockSkewMillis(
                        java.time.Instant.MIN, java.time.Instant.MAX, false, "instance-1"));
        org.junit.jupiter.api.Assertions.assertThrows(com.cpf.core.api.runtimecontrol.CpfRuntimeFenceException.class,
                () -> CpfRuntimeControlPlaneRepository.validatedClockSkewMillis(
                        java.time.Instant.MIN, java.time.Instant.MAX, true, "instance-1"));
    }
}
