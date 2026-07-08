package cpf.bat.logging;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BatJobLogPathPolicyTest {

    @Test
    void buildJobExecutionLogPathUsesJobExecutionUnit() {
        BatJobLogPathPolicy policy = new BatJobLogPathPolicy();

        String path = policy.buildJobExecutionLogPath(
                "./logs/bat",
                "dailySettlementJob",
                LocalDate.of(2026, 7, 8),
                12L,
                34L
        );

        assertThat(path).isEqualTo("./logs/bat/jobs/dailySettlementJob/20260708/jobInstance-12/execution-34.log");
    }

    @Test
    void buildCenterCutLogPathUsesCenterCutExecutionId() {
        BatJobLogPathPolicy policy = new BatJobLogPathPolicy();

        String path = policy.buildCenterCutLogPath("./logs/bat", "CUT-20260708-001", "summary.log");

        assertThat(path).isEqualTo("./logs/bat/centercut/CUT-20260708-001/summary.log");
    }

    @Test
    void buildJobExecutionLogPathRejectsBlankJobName() {
        BatJobLogPathPolicy policy = new BatJobLogPathPolicy();

        assertThatThrownBy(() -> policy.buildJobExecutionLogPath("./logs/bat", " ", LocalDate.now(), 1L, 1L))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
