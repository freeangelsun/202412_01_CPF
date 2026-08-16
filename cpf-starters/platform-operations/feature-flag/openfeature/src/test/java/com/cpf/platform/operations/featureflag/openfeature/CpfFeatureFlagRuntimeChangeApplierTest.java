package com.cpf.platform.operations.featureflag.openfeature;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cpf.platform.operations.api.featureflag.CpfFeatureFlagContext;
import com.cpf.platform.operations.api.featureflag.CpfFeatureFlagOperations;
import com.cpf.platform.operations.api.featureflag.CpfFeatureFlagResult;
import com.cpf.platform.operations.api.featureflag.CpfFeatureFlagValue;
import com.cpf.platform.operations.runtimecontrol.CpfRuntimeApplyResult;
import com.cpf.platform.operations.runtimecontrol.CpfRuntimeDelivery;
import com.cpf.platform.operations.runtimecontrol.CpfRuntimePayload;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class CpfFeatureFlagRuntimeChangeApplierTest {

    @Test
    void appliesKillSwitchAndVerifiesActualState() {
        FakeOperations operations = new FakeOperations();
        CpfFeatureFlagRuntimeChangeApplier applier = new CpfFeatureFlagRuntimeChangeApplier(operations);

        CpfRuntimeApplyResult result = applier.apply(delivery(
                "{\"flagKey\":\"payments.write.enabled\",\"enabled\":false,\"reason\":\"incident\"}"));

        assertTrue(result.applied());
        assertEquals("payload-hash", result.actualHash());
        assertEquals(1, operations.setCalls);
        assertEquals("runtime-control:change-1", operations.lastOperator);
        assertEquals("incident", operations.lastReason);
    }

    @Test
    void replaySkipsDuplicateMutationAndAuditSideEffect() {
        FakeOperations operations = new FakeOperations();
        operations.flagKey = "payments.write.enabled";
        operations.enabled = false;
        CpfFeatureFlagRuntimeChangeApplier applier = new CpfFeatureFlagRuntimeChangeApplier(operations);

        CpfRuntimeApplyResult result = applier.apply(delivery(
                "{\"flagKey\":\"payments.write.enabled\",\"enabled\":false}"));

        assertTrue(result.applied());
        assertEquals(0, operations.setCalls);
        assertTrue(applier.supportsIdempotentReplay());
        assertFalse(applier.snapshotCapable());
    }

    @Test
    void rejectsUnknownFieldAndStringBooleanBeforeSideEffect() {
        FakeOperations operations = new FakeOperations();
        CpfFeatureFlagRuntimeChangeApplier applier = new CpfFeatureFlagRuntimeChangeApplier(operations);

        CpfRuntimeApplyResult unknownField = applier.apply(delivery(
                "{\"flagKey\":\"a\",\"enabled\":true,\"secret\":\"x\"}"));
        CpfRuntimeApplyResult stringBoolean = applier.apply(delivery(
                "{\"flagKey\":\"a\",\"enabled\":\"true\"}"));

        assertEquals("FEATURE_FLAG_INVALID", unknownField.errorCode());
        assertEquals("FEATURE_FLAG_INVALID", stringBoolean.errorCode());
        assertEquals(0, operations.setCalls);
    }

    @Test
    void mutationExceptionIsUnknownBecauseSideEffectMayHaveOccurred() {
        FakeOperations operations = new FakeOperations();
        operations.throwAfterMutation = true;
        CpfFeatureFlagRuntimeChangeApplier applier = new CpfFeatureFlagRuntimeChangeApplier(operations);

        CpfRuntimeApplyResult result = applier.apply(delivery(
                "{\"flagKey\":\"payments.write.enabled\",\"enabled\":true}"));

        assertTrue(result.unknownResult());
        assertEquals("FEATURE_FLAG_APPLY_UNKNOWN", result.errorCode());
        assertEquals(1, operations.setCalls);
    }

    private CpfRuntimeDelivery delivery(String payload) {
        return new CpfRuntimeDelivery(
                "delivery-1", "change-1", "CONFIG_PARAMETER_FEATURE_FLAG", "instance-1",
                1L, 1L, "request-hash", "payload-hash", 1,
                CpfRuntimePayload.parse(payload), 0, Instant.now().plusSeconds(60));
    }

    private static final class FakeOperations implements CpfFeatureFlagOperations {
        private String flagKey;
        private Boolean enabled;
        private int setCalls;
        private String lastOperator;
        private String lastReason;
        private boolean throwAfterMutation;

        @Override
        public CpfFeatureFlagResult<CpfFeatureFlagValue> find(String requestedFlagKey) {
            if (flagKey == null || !flagKey.equals(requestedFlagKey)) {
                throw new IllegalArgumentException("flag not found");
            }
            return new CpfFeatureFlagResult<>(
                    flagKey,
                    new CpfFeatureFlagValue.BooleanValue(enabled),
                    null,
                    "KILL_SWITCH",
                    CpfFeatureFlagResult.Source.KILL_SWITCH,
                    1L,
                    Instant.now());
        }

        @Override
        public void setKillSwitch(
                String requestedFlagKey,
                boolean requestedEnabled,
                String operatorId,
                String reason) {
            setCalls++;
            flagKey = requestedFlagKey;
            enabled = requestedEnabled;
            lastOperator = operatorId;
            lastReason = reason;
            if (throwAfterMutation) {
                throw new IllegalStateException("response lost after commit");
            }
        }

        @Override
        public CpfFeatureFlagResult<CpfFeatureFlagValue> evaluate(
                String flagKey,
                CpfFeatureFlagValue fallback,
                CpfFeatureFlagContext context) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<CpfFeatureFlagResult<CpfFeatureFlagValue>> search(
                String flagKeyContains,
                int page,
                int size) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String requestOverride(
                String flagKey,
                CpfFeatureFlagValue value,
                Instant expiresAt,
                String requesterId,
                String reason) {
            throw new UnsupportedOperationException();
        }

        @Override
        public CpfFeatureFlagResult<CpfFeatureFlagValue> approveOverride(
                String requestId,
                String approverId,
                String reason) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void revokeOverride(String requestId, String operatorId, String reason) {
            throw new UnsupportedOperationException();
        }
    }
}
