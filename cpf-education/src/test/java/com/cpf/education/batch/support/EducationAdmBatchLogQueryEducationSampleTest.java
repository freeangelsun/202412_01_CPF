package com.cpf.education.batch.support;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EducationAdmBatchLogQueryEducationSampleTest {

    @Test
    void buildsSafeListAndDetailUrls() {
        var urls = new EducationAdmBatchLogQueryEducationSample()
                .queryUrls(LocalDate.of(2026, 7, 13), "CPF_BAT_SMOKE_JOB", 42L, "batWK01");

        assertThat(urls.get("listUrl"))
                .isEqualTo("/adm/api/reliability/batch-job-logs?businessDate=20260713"
                        + "&jobName=CPF_BAT_SMOKE_JOB&jobInstanceId=42"
                        + "&serverInstanceId=batWK01&limit=100");
        assertThat(urls.get("detailUrl"))
                .isEqualTo("/adm/api/reliability/batch-job-logs/20260713/CPF_BAT_SMOKE_JOB/42"
                        + "?serverInstanceId=batWK01&maxRecords=500");
        assertThat(urls.get("requiredPermission")).isEqualTo("RELIABILITY_READ");
    }

    @Test
    void rejectsPathManipulationJobName() {
        assertThatThrownBy(() -> new EducationAdmBatchLogQueryEducationSample()
                .queryUrls(LocalDate.of(2026, 7, 13), "../secret", 42L, "batWK01"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsInvalidServerInstanceId() {
        assertThatThrownBy(() -> new EducationAdmBatchLogQueryEducationSample()
                .queryUrls(LocalDate.of(2026, 7, 13), "CPF_BAT_SMOKE_JOB", 42L, "../worker"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
