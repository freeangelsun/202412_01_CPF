package com.cpf.reference.idempotency;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReferenceIdempotencyEducationSampleTest {

    @Test
    void duplicateKeyReplaysStoredResultThroughCpfEngine() {
        ReferenceIdempotencyEducationSample sample = new ReferenceIdempotencyEducationSample();

        assertThat(sample.handle("K")).isEqualTo("PROCESSED");
        assertThat(sample.handle("K")).isEqualTo("REPLAYED");
    }
}
