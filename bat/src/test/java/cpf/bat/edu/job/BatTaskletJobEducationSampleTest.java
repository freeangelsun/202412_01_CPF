package cpf.bat.edu.job;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BatTaskletJobEducationSampleTest {

    @Test
    void buildRunPlanContainsIdempotencyKey() {
        assertThat(new BatTaskletJobEducationSample().buildRunPlan("DAILY_JOB", "20260708"))
                .containsEntry("stepType", "TASKLET")
                .containsEntry("idempotencyKey", "DAILY_JOB:20260708");
    }
}
