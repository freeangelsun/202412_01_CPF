
package com.cpf.common.data.quality;

import com.cpf.core.api.data.quality.CpfDataQualityDecision;
import com.cpf.core.api.data.quality.CpfDataQualityOperations;
import com.cpf.core.api.data.quality.CpfDataQualityRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ConcurrentModificationException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InMemoryCpfDataQualityOperationsTest {
    private InMemoryCpfDataQualityOperations operations;
    private String quarantineId;

    @BeforeEach
    void setUp() {
        operations = new InMemoryCpfDataQualityOperations();
        operations.register(new CpfDataQualityRule(
                "NAME_REQUIRED",
                1,
                "name",
                "NOT_BLANK",
                CpfDataQualityRule.Severity.ERROR,
                CpfDataQualityRule.State.ACTIVE,
                Map.of()), "policy-admin", "activate");
        CpfDataQualityDecision decision = operations.validate("REC-1", Map.of("name", ""));
        quarantineId = decision.quarantineId();
    }

    @Test
    @SuppressWarnings("removal")
    void legacyClientBooleanCanNeverAuthorizeCorrection() {
        assertThatThrownBy(() -> operations.correct(
                quarantineId, 1, Map.of("name", "Kim"), "operator", "reason", true))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Client approval flags");
    }

    @Test
    void serverAuthorizationIsBoundToActorAndAudited() {
        CpfDataQualityOperations.QuarantineItem corrected = operations.correctAuthorized(
                quarantineId,
                1,
                Map.of("name", "Kim"),
                "checker",
                "approved correction",
                new CpfDataQualityOperations.CorrectionAuthorization(
                        "ADM-APPROVAL:77:CMD-77",
                        "checker",
                        Instant.now()));

        assertThat(corrected.state()).isEqualTo("CORRECTED");
        assertThat(corrected.version()).isEqualTo(2);
        assertThat(operations.audit()).anySatisfy(event -> {
            assertThat(event.action()).isEqualTo("CORRECT");
            assertThat(event.approvalReference()).isEqualTo("ADM-APPROVAL:77:CMD-77");
        });
    }

    @Test
    void authorizationCannotBeUsedByAnotherActor() {
        assertThatThrownBy(() -> operations.correctAuthorized(
                quarantineId,
                1,
                Map.of("name", "Kim"),
                "other",
                "reason",
                new CpfDataQualityOperations.CorrectionAuthorization(
                        "ADM-APPROVAL:77:CMD-77",
                        "checker",
                        Instant.now())))
                .isInstanceOf(SecurityException.class);
    }

    @Test
    void optimisticVersionAndStatePreventReplay() {
        CpfDataQualityOperations.CorrectionAuthorization authorization =
                new CpfDataQualityOperations.CorrectionAuthorization(
                        "ADM-APPROVAL:77:CMD-77",
                        "checker",
                        Instant.now());
        operations.correctAuthorized(
                quarantineId, 1, Map.of("name", "Kim"), "checker", "reason", authorization);

        assertThatThrownBy(() -> operations.correctAuthorized(
                quarantineId, 1, Map.of("name", "Lee"), "checker", "reason", authorization))
                .isInstanceOf(IllegalStateException.class);

        String second = operations.validate("REC-2", Map.of("name", "")).quarantineId();
        assertThatThrownBy(() -> operations.correctAuthorized(
                second, 9, Map.of("name", "Lee"), "checker", "reason", authorization))
                .isInstanceOf(ConcurrentModificationException.class);
    }
}
