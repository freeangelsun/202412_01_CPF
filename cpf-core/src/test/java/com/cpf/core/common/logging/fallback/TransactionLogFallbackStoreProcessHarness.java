package com.cpf.core.common.logging.fallback;

import com.cpf.core.common.logging.TransactionLogRecord;
import com.cpf.core.common.logging.file.CpfFileLogWriter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.env.Environment;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Child-process validation for duplicate idempotency and shared spool-capacity fencing.
 */
public final class TransactionLogFallbackStoreProcessHarness {
    private static final int BODY_BYTES = 512;
    private static final long MAX_BYTES = BODY_BYTES + 64L;

    private TransactionLogFallbackStoreProcessHarness() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length > 0 && "--worker".equals(args[0])) {
            worker(args);
            return;
        }
        duplicateRaceIsIdempotentAcrossProcesses();
        capacityRaceFailsClosedAcrossProcesses();
        System.out.println("CPF_LOG_SPOOL_PROCESS_HARNESS_PASS");
    }

    private static void duplicateRaceIsIdempotentAcrossProcesses() throws Exception {
        Path root = Files.createTempDirectory("cpf-spool-process-duplicate-");
        List<String> results = race(root, "tx-duplicate", "tx-duplicate");
        check(count(results, "ACCEPTED") == 1, "one duplicate process must create the journal");
        check(count(results, "DUPLICATE") == 1, "the competing duplicate must return idempotent false");
        check(count(results, "REJECTED") == 0, "duplicate race must not surface as capacity failure");
    }

    private static void capacityRaceFailsClosedAcrossProcesses() throws Exception {
        Path root = Files.createTempDirectory("cpf-spool-process-capacity-");
        List<String> results = race(root, "tx-capacity-left", "tx-capacity-right");
        check(count(results, "ACCEPTED") == 1, "only one distinct journal may fit");
        check(count(results, "REJECTED") == 1, "the competing process must fail closed at the capacity bound");
    }

    private static List<String> race(Path root, String leftTx, String rightTx) throws Exception {
        Path start = root.resolve("start.signal");
        Path leftReady = root.resolve("left.ready");
        Path rightReady = root.resolve("right.ready");
        Process left = startWorker(root, leftTx, leftReady, start);
        Process right = startWorker(root, rightTx, rightReady, start);
        awaitFile(leftReady);
        awaitFile(rightReady);
        Files.writeString(start, "start");
        List<String> results = new ArrayList<>();
        results.add(waitResult(left));
        results.add(waitResult(right));
        return results;
    }

    private static Process startWorker(Path root, String transactionId, Path ready, Path start) throws Exception {
        String java = Path.of(System.getProperty("java.home"), "bin", "java").toString();
        return new ProcessBuilder(
                java,
                "-cp",
                System.getProperty("java.class.path"),
                TransactionLogFallbackStoreProcessHarness.class.getName(),
                "--worker",
                root.toString(),
                transactionId,
                ready.toString(),
                start.toString())
                .redirectErrorStream(true)
                .start();
    }

    private static void worker(String[] args) throws Exception {
        Path root = Path.of(args[1]);
        String transactionId = args[2];
        Path ready = Path.of(args[3]);
        Path start = Path.of(args[4]);
        TestEnvironment environment = new TestEnvironment(root);
        Clock clock = Clock.fixed(Instant.parse("2026-08-05T00:00:00Z"), ZoneOffset.UTC);
        TransactionLogFallbackStore store = new TransactionLogFallbackStore(
                new FixedBodyObjectMapper(),
                new CpfFileLogWriter(environment, clock),
                environment,
                clock);
        Files.writeString(ready, "ready");
        awaitFile(start);
        try {
            boolean accepted = store.enqueue(
                    record(transactionId), Map.of("token", "secret"), null, new IllegalStateException("secret"));
            System.out.println(accepted ? "ACCEPTED" : "DUPLICATE");
        } catch (IllegalStateException capacityFailure) {
            System.out.println("REJECTED");
        }
    }

    private static String waitResult(Process process) throws Exception {
        if (!process.waitFor(20, TimeUnit.SECONDS)) {
            process.destroyForcibly();
            throw new AssertionError("spool worker process timed out");
        }
        String output = new String(process.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8).trim();
        check(process.exitValue() == 0, "spool worker failed: " + output);
        if (output.endsWith("ACCEPTED")) return "ACCEPTED";
        if (output.endsWith("DUPLICATE")) return "DUPLICATE";
        if (output.endsWith("REJECTED")) return "REJECTED";
        throw new AssertionError("unexpected spool worker result: " + output);
    }

    private static void awaitFile(Path file) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (!Files.exists(file)) {
            if (System.nanoTime() >= deadline) {
                throw new AssertionError("timed out waiting for " + file);
            }
            Thread.sleep(10L);
        }
    }

    private static long count(List<String> values, String expected) {
        return values.stream().filter(expected::equals).count();
    }

    private static TransactionLogRecord record(String transactionId) {
        TransactionLogRecord record = new TransactionLogRecord();
        record.setTransactionId(transactionId);
        record.setSpanId("span");
        record.setLogType("FINAL");
        record.setSequenceNo(1);
        return record;
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static final class FixedBodyObjectMapper extends ObjectMapper {
        @Override
        public byte[] writeValueAsBytes(Object value) {
            return new byte[BODY_BYTES];
        }
    }

    private static final class TestEnvironment implements Environment {
        private final Path root;

        private TestEnvironment(Path root) {
            this.root = root.toAbsolutePath().normalize();
        }

        @Override
        public String getProperty(String key) {
            return switch (key) {
                case "cpf.logging.file.base-path" -> root.toString();
                case "cpf.environment" -> "local";
                case "cpf.framework.module-id" -> "CPF";
                case "cpf.framework.instance-id" -> "spool-process";
                case "cpf.logging.file.enabled" -> "false";
                case "cpf.logging.file.timezone" -> "UTC";
                default -> null;
            };
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T getProperty(String key, Class<T> targetType, T defaultValue) {
            if ("cpf.logging.db-fallback.max-spool-bytes".equals(key) && targetType == Long.class) {
                return (T) Long.valueOf(MAX_BYTES);
            }
            String value = getProperty(key);
            if (value == null) return defaultValue;
            if (targetType == Boolean.class) return (T) Boolean.valueOf(value);
            if (targetType == Long.class) return (T) Long.valueOf(value);
            return (T) value;
        }

        @Override
        public String[] getActiveProfiles() {
            return new String[] {"test"};
        }
    }
}
