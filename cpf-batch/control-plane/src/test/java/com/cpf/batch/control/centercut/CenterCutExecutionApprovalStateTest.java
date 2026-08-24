package com.cpf.batch.control.centercut;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class CenterCutExecutionApprovalStateTest {
    @Test
    void startApprovalIsRememberedWhileTargetsAreStillBeingPrepared() {
        assertThat(CenterCutExecutionService.nextState("START", "CREATED", false))
                .isEqualTo("STARTING");
        assertThat(CenterCutExecutionService.nextState("START", "TARGETING", false))
                .isEqualTo("STARTING");
    }

    @Test
    void onlyApprovedStartMovesACompleteTargetSetToRunning() {
        assertThat(CenterCutExecutionService.nextState("START", "TARGET_READY", true))
                .isEqualTo("RUNNING");
        assertThatThrownBy(() -> CenterCutExecutionService.nextState(
                "RESUME", "TARGET_READY", true))
                .isInstanceOf(IllegalStateException.class);
    }
}
