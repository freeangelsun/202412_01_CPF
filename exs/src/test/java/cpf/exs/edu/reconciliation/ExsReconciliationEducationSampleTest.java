package cpf.exs.edu.reconciliation;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ExsReconciliationEducationSampleTest {

    @Test
    void mismatchReturnsInternalOnlyIds() {
        assertThat(new ExsReconciliationEducationSample().mismatch(List.of("A", "B"), List.of("B")))
                .containsExactly("A");
    }
}
