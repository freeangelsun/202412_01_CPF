package cpf.xyz.idempotency;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class XyzIdempotencyEducationSampleTest {

    @Test
    void duplicateKeyReplaysStoredResultThroughPfwEngine() {
        XyzIdempotencyEducationSample sample = new XyzIdempotencyEducationSample();

        assertThat(sample.handle("K")).isEqualTo("PROCESSED");
        assertThat(sample.handle("K")).isEqualTo("REPLAYED");
    }
}
