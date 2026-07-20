package cpf.bat.edu.tasklet.basic;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BatTaskletEducationSampleTest {

    @Test
    void buildExecutionGuideCreatesIdempotencyKey() {
        BatTaskletEducationSample sample = new BatTaskletEducationSample();

        Map<String, String> guide = sample.buildExecutionGuide("dailySettlementJob", "20260708", "operator01");

        assertThat(guide.get("idempotencyKey")).isEqualTo("dailySettlementJob:20260708");
        assertThat(guide.get("logPolicy")).contains("jobExecutionId");
    }

    @Test
    void buildExecutionGuideRejectsInvalidDate() {
        BatTaskletEducationSample sample = new BatTaskletEducationSample();

        assertThatThrownBy(() -> sample.buildExecutionGuide("dailySettlementJob", "2026-07-08", "operator01"))
                .isInstanceOf(RuntimeException.class);
    }
}
