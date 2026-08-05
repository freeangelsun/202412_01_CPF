package com.cpf.core.common.logging.file;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPOutputStream;
import org.springframework.core.env.Environment;

/** Exercises .log/.log.gz restoration, concurrent append, lock cleanup, and nested masking. */
public final class CpfFileLogArchiveRaceHarness {
    private CpfFileLogArchiveRaceHarness() {}

    public static void main(String[] args) throws Exception {
        Environment environment = new TestEnvironment();
        CpfFileLogWriter writer = new CpfFileLogWriter(environment, Clock.systemUTC());
        Path relative = Path.of("archive-race", "shared.log");
        Path log = writer.batchJobLogPath(relative);
        Path gzip = log.resolveSibling(log.getFileName() + ".gz");
        deleteTree(writer.logRoot());
        Files.createDirectories(log.getParent());
        try (OutputStream output = new GZIPOutputStream(Files.newOutputStream(gzip))) {
            output.write("legacy-line\n".getBytes(StandardCharsets.UTF_8));
        }

        int writes = 240;
        ExecutorService pool = Executors.newFixedThreadPool(16);
        List<Future<?>> futures = new ArrayList<>();
        for (int index = 0; index < writes; index++) {
            int sequence = index;
            futures.add(pool.submit(() -> writer.writeEventAtRelativePath(relative, Map.of(
                    "sequence", sequence,
                    "nested", Map.of(
                            "accessToken", "secret-" + sequence,
                            "payload", "{\"password\":\"pw-" + sequence + "\"}",
                            "auth", "Bearer aaa.bbb.ccc",
                            "xml", "<password>xml-secret</password>")))));
        }
        for (Future<?> future : futures) future.get(20, TimeUnit.SECONDS);
        pool.shutdownNow();

        if (!Files.isRegularFile(log)) throw new AssertionError("restored log missing");
        if (Files.exists(gzip)) throw new AssertionError("gzip archive not consumed during restore");
        String content = Files.readString(log, StandardCharsets.UTF_8);
        if (!content.contains("legacy-line")) throw new AssertionError("legacy gzip content lost");
        long eventCount = content.lines().filter(line -> line.contains("sequence=")).count();
        if (eventCount != writes) throw new AssertionError("append loss: " + eventCount + "/" + writes);
        for (String leaked : List.of("secret-", "pw-", "aaa.bbb.ccc", "xml-secret")) {
            if (content.contains(leaked)) throw new AssertionError("sensitive value leaked: " + leaked);
        }
        if (writer.retainedLockEntryCount() != 0) {
            throw new AssertionError("logical path lock registry leak: " + writer.retainedLockEntryCount());
        }
        if (writer.retainedRetentionScheduleCount() > 1) {
            throw new AssertionError("retention schedule registry leak: "
                    + writer.retainedRetentionScheduleCount());
        }
        var runtime = writer.fileLogRuntimeSnapshot();
        if (runtime.retentionRunCount() < 1L || runtime.retentionSkipCount() < 1L) {
            throw new AssertionError("retention run/skip metrics are not visible: " + runtime);
        }
        if (!"UP".equals(runtime.health())) {
            throw new AssertionError("successful retention must be healthy: " + runtime);
        }
        System.out.println("CPF_FILE_LOG_ARCHIVE_RACE_HARNESS_PASS");
    }

    private static void deleteTree(Path root) throws IOException {
        if (!Files.exists(root)) return;
        try (var paths = Files.walk(root)) {
            for (Path path : paths.sorted((left, right) -> right.compareTo(left)).toList()) {
                Files.deleteIfExists(path);
            }
        }
    }

    private static final class TestEnvironment implements Environment {
        @Override public String getProperty(String key) {
            return switch (key) {
                case "cpf.logging.file.enabled" -> "true";
                case "cpf.logging.file.archive-compress-enabled" -> "false";
                case "cpf.logging.file.retention-check-interval-ms" -> "60000";
                case "cpf.logging.file.total-size-cap" -> "0B";
                case "cpf.framework.module-id" -> "CORE";
                case "cpf.logging.file.timezone" -> "UTC";
                default -> null;
            };
        }

        @SuppressWarnings("unchecked")
        @Override public <T> T getProperty(String key, Class<T> type, T defaultValue) {
            String value = getProperty(key);
            if (value == null) return defaultValue;
            if (type == Boolean.class) return (T) Boolean.valueOf(value);
            if (type == Long.class) return (T) Long.valueOf(value);
            return (T) value;
        }

        @Override public String[] getActiveProfiles() { return new String[] {"test"}; }
    }
}
