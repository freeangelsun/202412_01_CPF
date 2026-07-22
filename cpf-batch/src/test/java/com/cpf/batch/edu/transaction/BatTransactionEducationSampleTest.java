package com.cpf.batch.edu.transaction;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BatTransactionEducationSampleTest {

    @Test
    void failureMakesBatchUnitRollbackCandidate() {
        BatTransactionEducationSample.TransactionSummary summary = new BatTransactionEducationSample()
                .summarize(List.of(true, true, false));

        assertThat(summary.failure()).isEqualTo(1);
        assertThat(summary.action()).isEqualTo("ROLLBACK");
    }
}
