package com.cpf.batch.edu.reconciliation;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BatReconciliationEducationSampleTest {

    @Test
    void reconciliationCandidatesMergeFailureAndUnknownIds() {
        assertThat(new BatReconciliationEducationSample().candidates(List.of("A", "B"), List.of("B", "C")))
                .containsExactly("A", "B", "C");
    }
}
