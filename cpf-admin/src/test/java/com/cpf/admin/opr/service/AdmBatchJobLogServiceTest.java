package com.cpf.admin.opr.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.env.MockEnvironment;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AdmBatchJobLogServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void listsAndParsesSafeJobInstanceLog() throws Exception {
        Path file = tempDir.resolve(
                "local/bat/jobs/20260713/CPF_JOB/cpf-bat-CPF_JOB-42-20260713.log");
        Files.createDirectories(file.getParent());
        Files.writeString(file, """
                {"businessDate":"20260713","jobName":"CPF_JOB","jobInstanceId":42,"status":"STARTED"}
                {"businessDate":"20260713","jobName":"CPF_JOB","jobInstanceId":42,"status":"COMPLETED"}
                """);
        AdmBatchJobLogService service = service();

        assertThat(service.findLogs("20260713", "CPF_JOB", 42L, 10))
                .singleElement()
                .satisfies(row -> assertThat(row.get("relativePath"))
                        .isEqualTo("local/bat/jobs/20260713/CPF_JOB/cpf-bat-CPF_JOB-42-20260713.log"));
        assertThat(service.findDetail("20260713", "CPF_JOB", 42L, 10))
                .containsEntry("totalRecordCount", 2)
                .containsEntry("returnedRecordCount", 2);
    }

    @Test
    void rejectsInvalidBusinessDateBeforeResolvingPath() {
        assertThatThrownBy(() -> service().findDetail("../outside", "CPF_JOB", 42L, 10))
                .hasMessageContaining("yyyyMMdd");
    }

    @Test
    void rejectsDirectoryNavigationJobName() {
        assertThatThrownBy(() -> service().findDetail("20260713", "..", 42L, 10))
                .hasMessageContaining("영문 또는 숫자로 시작");
    }

    private AdmBatchJobLogService service() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("cpf.logging.file.base-path", tempDir.toString())
                .withProperty("cpf.environment", "local")
                .withProperty("cpf.framework.module-id", "ADM")
                .withProperty("cpf.framework.instance-id", "adm-local-01");
        return new AdmBatchJobLogService(environment, new ObjectMapper());
    }
}
