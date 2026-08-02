package com.cpf.batch.centercut.runner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.cpf.batch.api.ActualState;
import com.cpf.batch.runtime.BatchRuntimePolicy;
import org.junit.jupiter.api.Test;

class SpringBatchCenterCutRuntimeStateTest {
    @Test
    void observesOnlySpringBatchStepInvocationsAndItemClaims() {
        SpringBatchCenterCutRuntimeState state =
                new SpringBatchCenterCutRuntimeState(new BatchRuntimePolicy(), 2);

        assertThat(state.ready()).isTrue();
        assertThat(state.availableCapacity()).isEqualTo(2);
        try (SpringBatchCenterCutRuntimeState.Scope scope = state.begin("cpf-1", 31L, 7L)) {
            scope.claim("claim-1");
            assertThat(state.actualState()).isEqualTo(ActualState.BUSY);
            assertThat(state.currentExecutions()).containsExactly("cpf-1");
            assertThat(state.activeLeases()).containsExactly("claim-1");
            assertThat(state.availableCapacity()).isEqualTo(1);
            assertThat(state.fencingToken()).isEqualTo(7L);
        }

        assertThat(state.actualState()).isEqualTo(ActualState.READY);
        assertThat(state.currentExecutions()).isEmpty();
        assertThat(state.activeLeases()).isEmpty();
    }

    @Test
    void policyAndCapacityFailClosedAtStepBoundary() {
        BatchRuntimePolicy policy = new BatchRuntimePolicy();
        SpringBatchCenterCutRuntimeState state = new SpringBatchCenterCutRuntimeState(policy, 1);

        try (SpringBatchCenterCutRuntimeState.Scope ignored = state.begin("cpf-1", 31L, 7L)) {
            assertThatThrownBy(() -> state.begin("cpf-2", 32L, 8L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage(SpringBatchCenterCutRuntimeState.CAPACITY_EXHAUSTED);
        }

        policy.replaceCenterCut(1L, false);
        assertThat(state.draining()).isTrue();
        assertThat(state.ready()).isFalse();
        assertThat(state.availableCapacity()).isZero();
        assertThatThrownBy(() -> state.begin("cpf-3", 33L, 9L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(SpringBatchCenterCutRuntimeState.DISABLED);
    }
}
