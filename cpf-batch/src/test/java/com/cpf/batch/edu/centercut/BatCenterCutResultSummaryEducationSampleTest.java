package cpf.bat.edu.centercut;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BatCenterCutResultSummaryEducationSampleTest {

    @Test
    void summaryCalculatesFailedCount() {
        assertThat(new BatCenterCutResultSummaryEducationSample().summarize(5, 3).failed()).isEqualTo(2);
    }
}
