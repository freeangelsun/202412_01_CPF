package cpf.bat.edu.retry;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BatRetryEducationSampleTest {

    @Test
    void transientFailureRetriesUntilMaxAttempt() {
        BatRetryEducationSample.RetryDecision decision = new BatRetryEducationSample()
                .decide(1, 3, true);

        assertThat(decision.retry()).isTrue();
        assertThat(decision.action()).isEqualTo("RETRY");
    }
}
