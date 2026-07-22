package com.cpf.batch.edu.transaction;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BatItemTransactionEducationSampleTest {

    @Test
    void invalidItemIsSkippedWithReason() {
        BatItemTransactionEducationSample.ItemResult result = new BatItemTransactionEducationSample()
                .process(7, false);

        assertThat(result.status()).isEqualTo("SKIPPED");
        assertThat(result.reason()).isEqualTo("VALIDATION_FAILED");
    }
}
