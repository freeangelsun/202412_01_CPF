package com.cpf.batch.edu.idempotency;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BatIdempotencyEducationSampleTest {

    @Test
    void idempotencyKeyUsesJobDateAndParameterHash() {
        assertThat(new BatIdempotencyEducationSample().key("JOB", "20260708", "HASH"))
                .isEqualTo("JOB:20260708:HASH");
    }

    @Test
    void repeatedJobExecutionReplaysCpfStoredResult() {
        BatIdempotencyEducationSample sample = new BatIdempotencyEducationSample();

        assertThat(sample.runOnce("JOB", "20260713", "HASH").replayed()).isFalse();
        assertThat(sample.runOnce("JOB", "20260713", "HASH").replayed()).isTrue();
    }
}
