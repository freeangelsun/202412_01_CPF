package com.cpf.core.common.runtimecontrol;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CpfRuntimeAutoRollbackPolicyTest {

    @Test
    void emptyAllowlistDisablesAutomaticRollback() {
        var decision = policy(Set.of()).decide("CONFIG", "approval-1", null, 0, 0);

        assertFalse(decision.allowed());
        assertEquals("TYPE_NOT_ALLOWLISTED", decision.reason());
    }

    @Test
    void allowlistSupportsExactAndBoundedPrefixMatching() {
        var policy = policy(Set.of("CONFIG"));

        assertTrue(policy.decide("CONFIG", "approval-1", null, 0, 0).allowed());
        assertTrue(policy.decide("CONFIG_FEATURE", "approval-1", null, 0, 0).allowed());
        assertTrue(policy.decide("CONFIG:FEATURE", "approval-1", null, 0, 0).allowed());
        assertFalse(policy.decide("CONFIGURATION", "approval-1", null, 0, 0).allowed());
    }

    @Test
    void approvalOrBreakGlassIsRequired() {
        var policy = policy(Set.of("CONFIG"));

        assertEquals(
                "APPROVAL_REQUIRED",
                policy.decide("CONFIG", null, " ", 0, 0).reason());
        assertTrue(policy.decide("CONFIG", null, "break-glass-1", 0, 0).allowed());
    }

    @Test
    void persistentAttemptLimitStopsFurtherDispatch() {
        var decision = policy(Set.of("CONFIG")).decide("CONFIG", "approval-1", null, 3, 0);

        assertFalse(decision.allowed());
        assertEquals("ATTEMPT_LIMIT_EXCEEDED", decision.reason());
    }

    @Test
    void sharedFailureWindowOpensCircuit() {
        var decision = policy(Set.of("CONFIG")).decide("CONFIG", "approval-1", null, 0, 3);

        assertFalse(decision.allowed());
        assertEquals("CIRCUIT_OPEN", decision.reason());
    }

    @Test
    void invalidSafetyBoundsFailClosed() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new CpfRuntimeAutoRollbackPolicy(Set.of("CONFIG"), 0, 1, 3, 30_000L));
        assertThrows(
                IllegalArgumentException.class,
                () -> new CpfRuntimeAutoRollbackPolicy(Set.of("CONFIG"), 3, 1, 3, 999L));
    }


    @Test
    void dynamicBlockedAuditEventUsesStableDatabaseLength() {
        String event=CpfRuntimeControlReconciler.boundedAuditEventType(
                "AUTO_ROLLBACK_BLOCKED_OPERATION_LEDGER_CONFLICT_UNKNOWN_RESULT_WITH_EXTRA_CONTEXT");
        assertTrue(event.length()<=60);
        assertEquals(event,CpfRuntimeControlReconciler.boundedAuditEventType(
                "AUTO_ROLLBACK_BLOCKED_OPERATION_LEDGER_CONFLICT_UNKNOWN_RESULT_WITH_EXTRA_CONTEXT"));
        String distinct=CpfRuntimeControlReconciler.boundedAuditEventType(
                "AUTO_ROLLBACK_BLOCKED_OPERATION_LEDGER_CONFLICT_UNKNOWN_RESULT_WITH_OTHER_CONTEXT");
        assertFalse(event.equals(distinct));
    }

    private static CpfRuntimeAutoRollbackPolicy policy(Set<String> allowlist) {
        return new CpfRuntimeAutoRollbackPolicy(allowlist, 3, 1, 3, 30_000L);
    }
}
