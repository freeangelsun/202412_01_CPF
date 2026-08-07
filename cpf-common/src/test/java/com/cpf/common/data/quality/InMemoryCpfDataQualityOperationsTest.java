package com.cpf.common.data.quality;

import com.cpf.core.api.data.quality.CpfDataQualityDecision;
import com.cpf.core.api.data.quality.CpfDataQualityOperations;
import com.cpf.core.api.data.quality.CpfDataQualityRule;
import com.cpf.core.spi.data.quality.CpfDataQualityCorrectionPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ConcurrentModificationException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InMemoryCpfDataQualityOperationsTest {
    private static final String VALID_PROOF = "v".repeat(64);
    private InMemoryCpfDataQualityOperations operations;
    private String quarantineId;

    @BeforeEach
    void setUp() {
        operations = new InMemoryCpfDataQualityOperations(command -> VALID_PROOF.equals(command.proof()));
        operations.register(new CpfDataQualityRule(
                "NAME_REQUIRED", 1, "name", "NOT_BLANK",
                CpfDataQualityRule.Severity.ERROR, CpfDataQualityRule.State.ACTIVE,
                Map.of()), "policy-admin", "activate rule");
        CpfDataQualityDecision decision = operations.validate("REC-1", Map.of("name", ""));
        quarantineId = decision.quarantineId();
    }

    @Test
    void callerForgedApprovalProofCannotAuthorizeCorrection() {
        assertThatThrownBy(() -> operations.correctApproved(command(
                quarantineId, 1, Map.of("name", "Kim"), "f".repeat(64))))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("proof");
    }

    @Test
    void serverProofIsBoundToCommandAndAudited() {
        CpfDataQualityOperations.QuarantineItem corrected = operations.correctApproved(command(
                quarantineId, 1, Map.of("name", "Kim"), VALID_PROOF));

        assertThat(corrected.state()).isEqualTo("CORRECTED");
        assertThat(corrected.version()).isEqualTo(2);
        assertThat(operations.audit()).anySatisfy(event -> {
            assertThat(event.action()).isEqualTo("CORRECT");
            assertThat(event.approvalReference()).isEqualTo("ADM-APPROVAL:77:CMD-77");
        });
    }

    @Test
    void optimisticVersionAndStatePreventReplay() {
        operations.correctApproved(command(quarantineId, 1, Map.of("name", "Kim"), VALID_PROOF));

        assertThatThrownBy(() -> operations.correctApproved(command(
                quarantineId, 1, Map.of("name", "Lee"), VALID_PROOF)))
                .isInstanceOf(IllegalStateException.class);

        String second = operations.validate("REC-2", Map.of("name", "")).quarantineId();
        assertThatThrownBy(() -> operations.correctApproved(command(
                second, 9, Map.of("name", "Lee"), VALID_PROOF)))
                .isInstanceOf(ConcurrentModificationException.class);
    }

    @Test
    void replayIsVersionedAndIdempotent() {
        operations.correctApproved(command(quarantineId, 1, Map.of("name", "Kim"), VALID_PROOF));
        CpfDataQualityOperations.ReplayCommand replay = new CpfDataQualityOperations.ReplayCommand(
                quarantineId, 2, "REPLAY-0001", "operator", "replay corrected record");
        assertThat(operations.replay(replay).accepted()).isTrue();
        assertThat(operations.replay(replay).accepted()).isTrue();
        assertThatThrownBy(() -> operations.replay(new CpfDataQualityOperations.ReplayCommand(
                quarantineId, 2, "REPLAY-0002", "operator", "stale replay")))
                .isInstanceOf(ConcurrentModificationException.class);
    }

    private static CpfDataQualityCorrectionPort.ApprovedCorrection command(
            String id, long version, Map<String,Object> corrected, String proof) {
        return new CpfDataQualityCorrectionPort.ApprovedCorrection(
                id, version, corrected, "checker", "approved correction",
                "ADM-APPROVAL:77:CMD-77", "a".repeat(64),
                "nonce-0123456789abcdef", proof, Instant.parse("2026-08-07T00:00:00Z"));
    }
}
