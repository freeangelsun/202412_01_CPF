package cpf.xyz.edu.idempotency;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class XyzIdempotencyEducationSampleTest {

    @Test
    void duplicateKeyReturnsDuplicateResult() {
        XyzIdempotencyEducationSample sample = new XyzIdempotencyEducationSample();

        assertThat(sample.handle("K")).isEqualTo("PROCESSED");
        assertThat(sample.handle("K")).isEqualTo("DUPLICATE");
    }
}
