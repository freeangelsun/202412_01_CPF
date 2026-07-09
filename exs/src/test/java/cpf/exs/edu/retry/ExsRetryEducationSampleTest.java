package cpf.exs.edu.retry;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExsRetryEducationSampleTest {

    @Test
    void timeoutIsRetryable() {
        assertThat(new ExsRetryEducationSample().retryable("200", "TIMEOUT")).isTrue();
    }
}
