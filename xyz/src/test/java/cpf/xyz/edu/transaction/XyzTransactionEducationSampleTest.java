package cpf.xyz.edu.transaction;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class XyzTransactionEducationSampleTest {

    @Test
    void transactionStepKeepsGlobalIdAndStatusTransition() {
        XyzTransactionEducationSample.TransactionStep step = new XyzTransactionEducationSample()
                .changeStatus("T-001", "READY", "DONE");

        assertThat(step.transactionGlobalId()).isEqualTo("T-001");
        assertThat(step.action()).isEqualTo("COMMIT");
    }
}
