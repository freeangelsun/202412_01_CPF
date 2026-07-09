package cpf.bat.edu.logging;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class BatJobLogEducationSampleTest {

    @Test
    void logPathIsSeparatedByJobExecutionId() {
        assertThat(new BatJobLogEducationSample().logPath(Path.of("logs"), "JOB", 100L).toString())
                .contains("JOB")
                .contains("100");
    }
}
