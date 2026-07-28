package com.cpf.core.api.runtimecontrol;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CpfRuntimeStateCatalogTest {
    @Test
    void canonicalStatesExposeAllPersistenceValues() {
        assertThat(CpfRuntimeStateCatalog.ackStates())
                .containsExactlyInAnyOrder("SUCCESS", "ACKED", "FAILED", "UNKNOWN_RESULT", "RESTART_REQUIRED");
        assertThat(CpfRuntimeStateCatalog.deliveryStates())
                .contains("PENDING", "CLAIMED", "ACKED", "FAILED", "POISONED", "UNKNOWN_RESULT",
                        "RESTART_REQUIRED", "EXPIRED", "CANCELLED", "SUPERSEDED");
        assertThat(CpfRuntimeStateCatalog.changeStates())
                .contains("SCHEDULED", "APPLYING", "PARTIAL", "SUCCESS", "FAILED", "UNKNOWN_RESULT",
                        "EXPIRED", "CANCELLED", "ROLLBACK_PENDING", "ROLLED_BACK", "SUPERSEDED", "RECOVERED");
    }

    @Test
    void invalidAckStateFailsClosed() {
        assertThatThrownBy(() -> CpfRuntimeAckState.parse("DONE"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
