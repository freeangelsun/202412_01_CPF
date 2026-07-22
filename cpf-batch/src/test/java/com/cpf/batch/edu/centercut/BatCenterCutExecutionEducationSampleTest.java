package cpf.bat.edu.centercut;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BatCenterCutExecutionEducationSampleTest {

    @Test
    void centerCutExecutionIdContainsJobAndDate() {
        assertThat(new BatCenterCutExecutionEducationSample().centerCutExecutionId("JOB", "20260708"))
                .isEqualTo("CC-JOB-20260708");
    }
}
