package cpf.bat.edu.centercut;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BatCenterCutRetryEducationSampleTest {

    @Test
    void temporaryFailureIsRetryable() {
        assertThat(new BatCenterCutRetryEducationSample().retryable("TIMEOUT")).isTrue();
    }
}
