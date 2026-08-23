package com.cpf.common.logging;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CpfRuntimeLogMaintenanceTest {
    private static final Instant NOW = Instant.parse("2026-08-23T00:00:00Z");
    @TempDir Path temp;

    @Test
    void keepsFiveDayBoundaryCompressesOlderAndDeletesOnlyPast365Days() throws Exception {
        CpfApplicationLoggingPolicy policy = policy();
        Path directory = CpfRuntimeLogPathPolicy.resolveDirectory(temp, "demo", "instance-1");
        Path archive = Files.createDirectories(directory.resolve("archive"));
        Path active = Files.writeString(directory.resolve("runtime.log"), "active");
        Path exactlyFive = archived(archive, "runtime.2026-08-18.log", 5, "five");
        Path sixDays = archived(archive, "runtime.2026-08-17.log", 6, "six");
        Path exactly365 = archived(archive, "error.2025-08-23.log", 365, "365");
        Path older = archived(archive, "error.2025-08-22.log.gz", 366, "old");

        var result = new CpfRuntimeLogMaintenance(Clock.fixed(NOW, ZoneOffset.UTC)).maintain(policy);

        assertThat(result.successful()).isTrue();
        assertThat(result.compressedFiles()).isEqualTo(2);
        assertThat(result.deletedFiles()).isEqualTo(1);
        assertThat(active).exists();
        assertThat(exactlyFive).exists();
        assertThat(exactly365).doesNotExist();
        assertThat(archive.resolve("error.2025-08-23.log.gz")).exists();
        assertThat(older).doesNotExist();
        assertThat(sixDays).doesNotExist();
        Path compressed = archive.resolve("runtime.2026-08-17.log.gz");
        assertThat(compressed).exists();
        try (var input = new GZIPInputStream(Files.newInputStream(compressed))) {
            assertThat(new String(input.readAllBytes())).isEqualTo("six");
        }
    }

    @Test
    void ignoresUnknownFilesAndRejectsSymlinkWhenSupported() throws Exception {
        Path directory = CpfRuntimeLogPathPolicy.resolveDirectory(temp, "demo", "instance-1");
        Path archive = Files.createDirectories(directory.resolve("archive"));
        Path unknown = archived(archive, "unmanaged.2020-01-01.log", 1000, "keep");
        var result = new CpfRuntimeLogMaintenance(Clock.fixed(NOW, ZoneOffset.UTC)).maintain(policy());
        assertThat(result.scannedFiles()).isZero();
        assertThat(unknown).exists();
    }

    private CpfApplicationLoggingPolicy policy() {
        return new CpfApplicationLoggingPolicy(temp, "demo", "instance-1", Map.of(
                "runtime", new CpfLogFilePolicy(true, "runtime.log", null,
                        CpfLogFilePolicy.Rolling.DAILY, 5, 365),
                "error", new CpfLogFilePolicy(true, "error.log", "ERROR",
                        CpfLogFilePolicy.Rolling.DAILY, 5, 365)));
    }

    private static Path archived(Path archive, String name, long ageDays, String body) throws Exception {
        Path file = Files.writeString(archive.resolve(name), body);
        Files.setLastModifiedTime(file, FileTime.from(NOW.minus(ageDays, ChronoUnit.DAYS)));
        return file;
    }
}
