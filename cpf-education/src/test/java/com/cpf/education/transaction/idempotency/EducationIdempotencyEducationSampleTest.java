package com.cpf.education.transaction.idempotency;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EducationIdempotencyEducationSampleTest {

    @Test
    void duplicateKeyReplaysStoredResultThroughCpfEngine() {
        EducationIdempotencyEducationSample sample = new EducationIdempotencyEducationSample();

        assertThat(sample.handle("K")).isEqualTo("PROCESSED");
        assertThat(sample.handle("K")).isEqualTo("REPLAYED");
    }
}
