package com.cpf.batch.edu.logging;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class BatJobLogEducationSampleTest {

    @Test
    void logPathUsesCpfJobInstancePolicy() {
        String path = new BatJobLogEducationSample()
                .logPath(Path.of("logs"), "CPF_EDU_JOB", 100L, LocalDate.of(2026, 7, 13))
                .toString()
                .replace('\\', '/');

        assertThat(path)
                .endsWith("/logs/local/bat/jobs/20260713/CPF_EDU_JOB/cpf-bat-CPF_EDU_JOB-100-20260713.log");
    }

    @Test
    void restartUsesSameJobInstanceFile() {
        assertThat(new BatJobLogEducationSample().restartUsesSameFile(
                Path.of("logs"),
                "CPF_EDU_JOB",
                100L,
                LocalDate.of(2026, 7, 13)))
                .isTrue();
    }

    @Test
    void trackingFieldsExplainJobInstanceAndExecutionRelationship() {
        assertThat(new BatJobLogEducationSample().trackingFields(
                "20260713120000000BATbatWK010000001",
                "BAT-JOB-100",
                100L,
                200L))
                .containsEntry("transactionGlobalId", "20260713120000000BATbatWK010000001")
                .containsEntry("segmentId", "BAT-JOB-100")
                .containsEntry("jobInstanceId", 100L)
                .containsEntry("jobExecutionId", 200L);
    }
}
