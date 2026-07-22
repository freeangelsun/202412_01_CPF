package com.cpf.reference.transaction;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReferenceTransactionEducationSampleTest {

    @Test
    void transactionStepKeepsGlobalIdAndStatusTransition() {
        ReferenceTransactionEducationSample.TransactionStep step = new ReferenceTransactionEducationSample()
                .changeStatus("T-001", "READY", "DONE");

        assertThat(step.transactionGlobalId()).isEqualTo("T-001");
        assertThat(step.action()).isEqualTo("COMMIT");
    }
}
