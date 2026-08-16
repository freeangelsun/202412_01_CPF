package com.cpf.education.data.transaction;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EducationTransactionEducationSampleTest {

    @Test
    void transactionStepKeepsGlobalIdAndStatusTransition() {
        EducationTransactionEducationSample.TransactionStep step = new EducationTransactionEducationSample()
                .changeStatus("T-001", "READY", "DONE");

        assertThat(step.transactionId()).isEqualTo("T-001");
        assertThat(step.action()).isEqualTo("COMMIT");
    }
}
