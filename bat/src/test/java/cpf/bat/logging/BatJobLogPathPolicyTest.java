package cpf.bat.logging;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BatJobLogPathPolicyTest {

    @Test
    void buildJobInstanceLogPathUsesBusinessDateAndJobInstanceUnit() {
        BatJobLogPathPolicy policy = new BatJobLogPathPolicy();

        String path = policy.buildJobInstanceLogPath(
                "./logs",
                "dailySettlementJob",
                LocalDate.of(2026, 7, 8),
                12L
        );

        assertThat(path)
                .endsWith("/logs/bat/jobs/20260708/dailySettlementJob/cpf-bat-dailySettlementJob-12-20260708.log");
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

        assertThatThrownBy(() -> policy.buildJobInstanceLogPath("./logs", " ", LocalDate.now(), 1L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void buildJobInstanceLogPathRejectsDirectoryNavigationJobName() {
        BatJobLogPathPolicy policy = new BatJobLogPathPolicy();

        assertThatThrownBy(() -> policy.buildJobInstanceLogPath(
                "./logs",
                "..",
                LocalDate.of(2026, 7, 13),
                1L))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
