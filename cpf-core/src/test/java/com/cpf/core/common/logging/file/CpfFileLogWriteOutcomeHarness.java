package com.cpf.core.common.logging.file;

import com.cpf.core.api.logging.policy.LogPolicyDecision;
import com.cpf.core.api.logging.policy.LogPolicyTargetType;
import com.cpf.core.common.logging.TransactionLogRecord;
import org.springframework.core.env.Environment;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/** Verifies real filesystem write outcomes, symlink isolation and async terminal-loss accounting. */
public final class CpfFileLogWriteOutcomeHarness {
    private CpfFileLogWriteOutcomeHarness() { }

    public static void main(String[] args) throws Exception {
        TestEnvironment environment = new TestEnvironment();
        CpfFileLogWriter writer = new CpfFileLogWriter(environment, Clock.systemUTC());
        Path root = writer.logRoot();
        deleteTree(root);
        Files.createDirectories(root);
        try {
            directWriteRejectsSymlink(writer, root);
            retentionIgnoresSymlink(writer, root);
            asyncWriterConsumesActualFailure(writer, root);
            check(writer.fileWriteDiagnostics().writeFailureCount() >= 2L,
                    "direct and async filesystem failures must be counted");
            check(!"UP".equals(writer.fileLogRuntimeSnapshot().health()),
                    "write loss must prevent a healthy runtime status");
            System.out.println("CPF_FILE_LOG_WRITE_OUTCOME_HARNESS_PASS");
        } finally {
            deleteTree(root);
        }
    }

    private static void directWriteRejectsSymlink(CpfFileLogWriter writer, Path root) throws Exception {
        Path outside = Files.createTempFile("cpf-filelog-outside-", ".txt");
        Files.writeString(outside, "outside-original", StandardCharsets.UTF_8);
        Path relative = Path.of("outcome", "unsafe.log");
        Path managed = writer.batchJobLogPath(relative);
        Files.createDirectories(managed.getParent());
        Files.createSymbolicLink(managed, outside);
        boolean outcome = writer.writeEventAtRelativePathWithOutcome(relative, Map.of("token", "raw-secret"));
        check(!outcome, "symbolic log target must report a failed write outcome");
        check("outside-original".equals(Files.readString(outside, StandardCharsets.UTF_8)),
                "symbolic target must not modify an external file");
        check(writer.fileWriteDiagnostics().lastFailureType() != null,
                "direct write failure type must be bounded and visible");
        Files.deleteIfExists(managed);
        Files.deleteIfExists(outside);
    }

    private static void retentionIgnoresSymlink(CpfFileLogWriter writer, Path root) throws Exception {
        Path outside = Files.createTempFile("cpf-retention-outside-", ".log");
        Files.writeString(outside, "external-retention-secret", StandardCharsets.UTF_8);
        Path linked = writer.instanceRoot().resolve("audit-2020-01-01.log");
        Files.createDirectories(linked.getParent());
        Files.createSymbolicLink(linked, outside);
        check(writer.writeEventAtRelativePathWithOutcome(
                        Path.of("outcome", "retention-trigger.log"), Map.of("eventType", "TRIGGER")),
                "safe retention trigger write must succeed");
        check(Files.isSymbolicLink(linked), "retention must ignore symbolic log candidates");
        check(!Files.exists(linked.resolveSibling(linked.getFileName() + ".gz")),
                "retention must not archive external symbolic content into the managed root");
        check("external-retention-secret".equals(Files.readString(outside, StandardCharsets.UTF_8)),
                "retention must not read, truncate or delete the external target");
        Files.deleteIfExists(linked);
        Files.deleteIfExists(outside);
    }

    private static void asyncWriterConsumesActualFailure(CpfFileLogWriter writer, Path root) throws Exception {
        TransactionLogRecord record = new TransactionLogRecord();
        record.setTransactionId("tx-file-outcome");
        record.setStandardExecutionId("O-CORE-0001");
        record.setModuleId("CORE");
        record.setLogType("SUCCESS");
        record.setSequenceNo(1);
        LogPolicyDecision policy = LogPolicyDecision.cpfDefault(
                LogPolicyTargetType.ONLINE_TRANSACTION, "O-CORE-0001");
        check(writer.writePreparedTransactionWithOutcome(writer.prepareTransaction(record, Map.of(), policy)),
                "transaction path fixture must be created");
        Path transactionFile;
        try (var paths = Files.walk(root)) {
            transactionFile = paths.filter(path -> path.getFileName().toString().endsWith(".log"))
                    .filter(path -> {
                        try { return Files.readString(path).contains("tx-file-outcome"); }
                        catch (Exception ignored) { return false; }
                    })
                    .findFirst().orElseThrow();
        }
        Path outside = Files.createTempFile("cpf-async-outside-", ".txt");
        Files.writeString(outside, "async-outside-original", StandardCharsets.UTF_8);
        Files.delete(transactionFile);
        Files.createSymbolicLink(transactionFile, outside);

        CpfAsyncFileLogWriter async = new CpfAsyncFileLogWriter(
                writer, 4, CpfAsyncFileLogWriter.OverflowPolicy.CALLER_RUNS, Duration.ofSeconds(2));
        try {
            check(async.publish(record, Map.of(), policy) == CpfAsyncFileLogWriter.PublishResult.QUEUED,
                    "real writer failure fixture must be accepted by the bounded queue");
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(3);
            while (async.fileWriterSnapshot().failedCount() == 0L && System.nanoTime() < deadline) {
                Thread.sleep(10L);
            }
            var snapshot = async.fileWriterSnapshot();
            check(snapshot.failedCount() == 1L && snapshot.terminalLossCount() == 1L,
                    "actual filesystem failure must be counted as async terminal loss");
            check(snapshot.writtenCount() == 0L && "DOWN".equals(snapshot.health()),
                    "failed physical write must never be counted as written");
            check("async-outside-original".equals(Files.readString(outside, StandardCharsets.UTF_8)),
                    "async writer must not follow a symbolic transaction file");
        } finally {
            async.close();
            Files.deleteIfExists(transactionFile);
            Files.deleteIfExists(outside);
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static void deleteTree(Path root) throws Exception {
        if (!Files.exists(root)) return;
        try (var paths = Files.walk(root)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(path);
            }
        }
    }

    private static final class TestEnvironment implements Environment {
        @Override public String getProperty(String key) {
            return switch (key) {
                case "cpf.logging.file.enabled" -> "true";
                case "cpf.logging.file.archive-compress-enabled" -> "true";
                case "cpf.logging.file.retention-check-interval-ms" -> "0";
                case "cpf.logging.file.max-history-days" -> "30";
                case "cpf.logging.file.total-size-cap" -> "10MB";
                case "cpf.framework.module-id" -> "CORE";
                case "cpf.framework.instance-id" -> "write-outcome-harness";
                case "cpf.environment" -> "test";
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
            if (type == Integer.class) return (T) Integer.valueOf(value);
            return (T) value;
        }

        @Override public String[] getActiveProfiles() { return new String[] {"test"}; }
    }
}
