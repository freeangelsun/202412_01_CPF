package cpf.pfw.common.logging.file;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.env.MockEnvironment;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

class CpfFileLogWriterTest {

    @TempDir
    Path tempDir;

    @Test
    void writeEventMasksSensitiveValuesByKeyAndContent() throws Exception {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("cpf.logging.file.base-path", tempDir.toString())
                .withProperty("cpf.framework.module-id", "ACC")
                .withProperty("server.port", "8080");
        CpfFileLogWriter writer = new CpfFileLogWriter(environment);

        writer.writeEvent("ACC", "integration", Map.of(
                "eventType", "OUTBOUND_REQUEST",
                "password", "plainSecret",
                "Authorization", "Bearer token-raw-value",
                "X-Api-Key", "api-key-raw",
                "nested", Map.of("credential", "credential-raw")));

        Path logFile = singleLogFile(tempDir.resolve("acc"), "cpf-acc-integration-*.log");
        String content = Files.readString(logFile);

        assertThat(content)
                .contains("\"password\":\"***\"")
                .contains("\"Authorization\":\"***\"")
                .contains("\"X-Api-Key\":\"***\"")
                .contains("credential=***")
                .doesNotContain("plainSecret")
                .doesNotContain("token-raw-value")
                .doesNotContain("api-key-raw")
                .doesNotContain("credential-raw");
    }

    @Test
    void writeEventUsesConfiguredModuleIdBeforeApplicationName() throws Exception {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("cpf.logging.file.base-path", tempDir.toString())
                .withProperty("cpf.framework.module-id", "ACC")
                .withProperty("spring.application.name", "cpf-cmn")
                .withProperty("server.port", "8080");
        CpfFileLogWriter writer = new CpfFileLogWriter(environment);

        writer.writeEvent(null, "transaction", Map.of("eventType", "MODULE_ID_PRIORITY_CHECK"));

        assertThat(logFiles(tempDir.resolve("acc"), "cpf-acc-transaction-*.log")).hasSize(1);
        assertThat(tempDir.resolve("cmn")).doesNotExist();
    }

    @Test
    void writeEventCreatesSeparateDateFilesWithInjectedClock() throws Exception {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("cpf.logging.file.base-path", tempDir.toString())
                .withProperty("cpf.logging.file.timezone", "Asia/Seoul")
                .withProperty("cpf.framework.module-id", "XYZ");

        CpfFileLogWriter firstDayWriter = new CpfFileLogWriter(
                environment,
                Clock.fixed(Instant.parse("2026-07-13T14:59:59Z"), ZoneId.of("Asia/Seoul")));
        CpfFileLogWriter secondDayWriter = new CpfFileLogWriter(
                environment,
                Clock.fixed(Instant.parse("2026-07-13T15:00:01Z"), ZoneId.of("Asia/Seoul")));

        firstDayWriter.writeEvent("XYZ", "application", firstDayWriter.newBaseEvent("XYZ", "application"));
        secondDayWriter.writeEvent("XYZ", "application", secondDayWriter.newBaseEvent("XYZ", "application"));

        List<Path> files;
        try (var stream = Files.list(tempDir.resolve("xyz"))) {
            files = stream.filter(Files::isRegularFile).toList();
        }
        assertThat(files).hasSize(2);
        assertThat(files.stream().map(path -> path.getFileName().toString()))
                .anyMatch(name -> name.contains(".2026-07-13.log.gz"))
                .anyMatch(name -> name.contains(".2026-07-14.log"));
        Path activeFile = files.stream()
                .filter(path -> path.getFileName().toString().contains(".2026-07-14.log"))
                .findFirst()
                .orElseThrow();
        assertThat(Files.readString(activeFile))
                .contains("\"timezone\":\"Asia/Seoul\"")
                .contains("\"businessDate\":");
    }

    @Test
    void writeEventCompressesPreviousDateAndRestoresItForLateWrite() throws Exception {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("cpf.logging.file.base-path", tempDir.toString())
                .withProperty("cpf.logging.file.timezone", "Asia/Seoul")
                .withProperty("cpf.logging.file.archive-compress-enabled", "true")
                .withProperty("cpf.framework.module-id", "ADM");
        CpfFileLogWriter firstDayWriter = new CpfFileLogWriter(
                environment,
                Clock.fixed(Instant.parse("2026-07-13T10:00:00Z"), ZoneId.of("Asia/Seoul")));
        CpfFileLogWriter secondDayWriter = new CpfFileLogWriter(
                environment,
                Clock.fixed(Instant.parse("2026-07-14T10:00:00Z"), ZoneId.of("Asia/Seoul")));

        firstDayWriter.writeEvent("ADM", "audit", Map.of("eventType", "FIRST_DAY"));
        Path firstDayLog = singleLogFile(tempDir.resolve("adm"), "*.2026-07-13.log");
        secondDayWriter.writeEvent("ADM", "audit", Map.of("eventType", "SECOND_DAY"));

        Path archived = firstDayLog.resolveSibling(firstDayLog.getFileName() + ".gz");
        assertThat(firstDayLog).doesNotExist();
        assertThat(archived).exists();
        try (GZIPInputStream input = new GZIPInputStream(Files.newInputStream(archived))) {
            assertThat(new String(input.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8))
                    .contains("FIRST_DAY");
        }

        firstDayWriter.writeEvent("ADM", "audit", Map.of("eventType", "LATE_FIRST_DAY"));

        assertThat(archived).doesNotExist();
        assertThat(Files.readString(firstDayLog))
                .contains("FIRST_DAY")
                .contains("LATE_FIRST_DAY");
    }

    @Test
    void constructorFailsFastWhenConfiguredLogRootCannotBeCreated() throws Exception {
        Path invalidRoot = tempDir.resolve("not-a-directory");
        Files.writeString(invalidRoot, "occupied");
        MockEnvironment environment = new MockEnvironment()
                .withProperty("cpf.logging.file.base-path", invalidRoot.toString())
                .withProperty("cpf.logging.file.initialization-fail-fast", "true")
                .withProperty("cpf.framework.module-id", "ADM");

        assertThatIllegalStateException()
                .isThrownBy(() -> new CpfFileLogWriter(environment))
                .withMessageContaining("로그 root 초기화");
    }

    private Path singleLogFile(Path directory, String glob) throws Exception {
        List<Path> files = logFiles(directory, glob);
        assertThat(files).hasSize(1);
        return files.getFirst();
    }

    private List<Path> logFiles(Path directory, String glob) throws Exception {
        if (!Files.isDirectory(directory)) {
            return List.of();
        }
        try (var stream = Files.newDirectoryStream(directory, glob)) {
            return java.util.stream.StreamSupport.stream(stream.spliterator(), false).toList();
        }
    }
}
