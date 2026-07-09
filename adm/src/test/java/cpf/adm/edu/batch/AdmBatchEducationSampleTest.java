package cpf.adm.edu.batch;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AdmBatchEducationSampleTest {

    @Test
    void executionQueryIncludesStepsAndFailureReason() {
        assertThat(new AdmBatchEducationSample().executionQuery("JOB", "20260708"))
                .containsEntry("includeSteps", "true")
                .containsEntry("includeFailureReason", "true");
    }
}
