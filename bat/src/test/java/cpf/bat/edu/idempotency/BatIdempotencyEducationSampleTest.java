package cpf.bat.edu.idempotency;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BatIdempotencyEducationSampleTest {

    @Test
    void idempotencyKeyUsesJobDateAndParameterHash() {
        assertThat(new BatIdempotencyEducationSample().key("JOB", "20260708", "HASH"))
                .isEqualTo("JOB:20260708:HASH");
    }
}
