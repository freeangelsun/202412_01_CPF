package com.cpf.core.api.transaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.util.Set;
import org.junit.jupiter.api.Test;

class CpfTransactionStrategyPolicyTest {
    @Test
    void emptySelectionDefaultsToLocal() {
        assertThat(CpfTransactionStrategyPolicy.of(Set.of()).strategies())
                .containsExactly(CpfTransactionStrategy.LOCAL);
    }

    @Test
    void localCanMixWithOutboxButNotXaPrimary() {
        assertThat(CpfTransactionStrategyPolicy.of(Set.of(
                CpfTransactionStrategy.LOCAL,
                CpfTransactionStrategy.OUTBOX)).enabled(CpfTransactionStrategy.OUTBOX)).isTrue();

        assertThatIllegalArgumentException().isThrownBy(() -> CpfTransactionStrategyPolicy.of(Set.of(
                CpfTransactionStrategy.LOCAL,
                CpfTransactionStrategy.XA_JTA)));
    }
}
