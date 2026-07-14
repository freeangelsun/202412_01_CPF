package cpf.xyz.edu.logging;

import cpf.pfw.common.logging.file.CpfFileLogWriter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.env.MockEnvironment;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

class XyzFileLogEducationSampleTest {
    @TempDir
    Path tempDir;

    @Test
    void writesApplicationTransactionErrorAndMasksSensitiveValues() throws Exception {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("cpf.framework.module-id", "xyz")
                .withProperty("cpf.logging.file.base-path", tempDir.toString())
                .withProperty("cpf.environment", "local")
                .withProperty("cpf.framework.instance-id", "xyz-edu-01")
                .withProperty("cpf.logging.file.timezone", "Asia/Seoul")
                .withProperty("cpf.logging.file.archive-compress-enabled", "false");
        Clock clock = Clock.fixed(Instant.parse("2026-07-13T07:30:00Z"), ZoneId.of("Asia/Seoul"));
        XyzFileLogEducationSample sample = new XyzFileLogEducationSample(
                new CpfFileLogWriter(environment, clock));

        // 각 로그 유형을 호출해 하나의 파일에 서로 다른 유형이 섞이지 않는지 확인합니다.
        sample.writeApplicationLog("교육 애플리케이션 준비 완료");
        sample.writeTransactionLog(
                "XYZ_EDU_FILE_LOG",
                "20260713163000000XYZedu0010000001",
                "SEG-XYZ-1");
        sample.writeErrorLog("XYZ-EDU-001", "password=should-not-remain");
        sample.writeMaskingGuardExample("plain-password", "Bearer plain-token");

        Path moduleRoot = tempDir.resolve("local/xyz/xyz-edu-01");
        assertThat(moduleRoot).isDirectory();
        java.util.List<String> fileNames;
        try (var paths = Files.walk(moduleRoot)) {
            fileNames = paths.filter(Files::isRegularFile)
                    .map(path -> path.getFileName().toString())
                    .toList();
        }
        assertThat(fileNames)
                .anyMatch(name -> name.matches("cpf-xyz-application-.+\\.2026-07-13\\.log"))
                .anyMatch(name -> name.matches("cpf-xyz-error-.+\\.2026-07-13\\.log"))
                .contains("XYZ_EDU_FILE_LOG_20260713.log");

        String allLogs;
        try (var paths = Files.walk(moduleRoot)) {
            allLogs = paths.filter(Files::isRegularFile).map(path -> {
                        try {
                            return Files.readString(path);
                        } catch (Exception ex) {
                            throw new IllegalStateException(ex);
                        }
                    })
                    .reduce("", String::concat);
        }
        assertThat(allLogs)
                .contains("APPLICATION_STATE", "ONLINE_TRANSACTION", "EDU_FAILURE", "MASKING_GUARD")
                .contains("\"logType\":\"application\"", "\"logType\":\"transaction\"", "\"logType\":\"error\"")
                .contains("\"transactionId\":\"XYZ_EDU_FILE_LOG\"")
                .contains("\"transactionGlobalId\":\"20260713163000000XYZedu0010000001\"")
                .contains("\"password\":\"***\"", "\"authorization\":\"***\"")
                .doesNotContain("plain-password", "plain-token", "should-not-remain");
    }
}
