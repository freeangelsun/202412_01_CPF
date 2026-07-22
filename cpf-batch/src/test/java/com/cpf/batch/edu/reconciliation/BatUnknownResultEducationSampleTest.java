package com.cpf.batch.edu.reconciliation;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BatUnknownResultEducationSampleTest {

    @Test
    void timeoutResponseBecomesReconciliationCandidate() {
        BatUnknownResultEducationSample.UnknownDecision decision = new BatUnknownResultEducationSample()
                .decide("TIMEOUT");

        assertThat(decision.unknown()).isTrue();
        assertThat(decision.nextAction()).isEqualTo("RECONCILIATION_REQUIRED");
    }
}
