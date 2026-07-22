package com.cpf.batch.edu.restart.checkpoint;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BatJobStatusTransitionEducationSampleTest {

    @Test
    void failedAndStoppedStatusesAreRetryable() {
        BatJobStatusTransitionEducationSample sample = new BatJobStatusTransitionEducationSample();

        assertThat(sample.canRetry("FAILED")).isTrue();
        assertThat(sample.canRetry("COMPLETED")).isFalse();
        assertThat(sample.nextStatus(true)).isEqualTo("COMPLETED");
    }
}
