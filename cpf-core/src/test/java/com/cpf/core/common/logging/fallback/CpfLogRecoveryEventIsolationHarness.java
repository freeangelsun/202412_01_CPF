package com.cpf.core.common.logging.fallback;

import com.cpf.core.api.logging.CpfLogRecoveryOperations.PoisonRetryApproval;
import com.cpf.core.api.logging.CpfLogRecoveryOperations.PoisonRetryCommand;
import com.cpf.core.api.logging.CpfLogRecoveryOperations.PoisonRetryStatus;
import com.cpf.core.api.logging.policy.LogPolicyDecision;
import com.cpf.core.api.logging.policy.LogPolicyTargetType;
import com.cpf.core.common.logging.TransactionLogRecord;
import com.cpf.core.common.logging.file.CpfFileLogWriter;
import com.cpf.core.service.common.logging.TransactionLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.env.Environment;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/** Operational event failures must not corrupt recovery state or create false retry results. */
public final class CpfLogRecoveryEventIsolationHarness {
    private static final Instant NOW = Instant.parse("2026-08-05T00:00:00Z");

    private CpfLogRecoveryEventIsolationHarness() { }

    public static void main(String[] args) throws Exception {
        preMutationAuditFailureIsFailClosed();
        postMutationAuditFailureIsUnknown();
        summaryEventFailureDoesNotReverseRecovery();
        System.out.println("CPF_LOG_RECOVERY_EVENT_ISOLATION_HARNESS_PASS");
    }

    private static void preMutationAuditFailureIsFailClosed() throws Exception {
        try (Fixture fixture = new Fixture("pre-audit")) {
            String id = "c".repeat(64);
            fixture.poison(id, 2);
            SequencedSink sink = new SequencedSink(1);
            TransactionLogRecoveryWorker worker = fixture.worker(sink);
            PoisonRetryCommand command = new PoisonRetryCommand(
                    id, 2, "requester@example.com", "retry password=raw-secret");
            PoisonRetryApproval approval = PoisonRetryApproval.approve(
                    "approval-pre", "approver@example.com", command,
                    NOW.minusSeconds(1), Duration.ofMinutes(5));

            var result = worker.retryPoison(command, approval);
            check(result.status() == PoisonRetryStatus.FAILED,
                    "unavailable pre-mutation audit must fail closed");
            check("CPF_LOG_RECOVERY_AUDIT_UNAVAILABLE".equals(result.errorCode()),
                    "pre-mutation audit failure error code");
            check(fixture.store.snapshot().poisonCount() == 1
                            && fixture.store.snapshot().pendingCount() == 0,
                    "poison state must remain unchanged when authorization audit is unavailable");
            check(worker.recoveryOperationalDiagnostics().operationalEventFailureCount() == 1L,
                    "audit failure must be operationally visible");
        }
    }

    private static void postMutationAuditFailureIsUnknown() throws Exception {
        try (Fixture fixture = new Fixture("post-audit")) {
            String id = "d".repeat(64);
            fixture.poison(id, 3);
            SequencedSink sink = new SequencedSink(2);
            TransactionLogRecoveryWorker worker = fixture.worker(sink);
            PoisonRetryCommand command = new PoisonRetryCommand(
                    id, 3, "requester@example.com", "retry token=raw-secret");
            PoisonRetryApproval approval = PoisonRetryApproval.approve(
                    "approval-post", "approver@example.com", command,
                    NOW.minusSeconds(1), Duration.ofMinutes(5));

            var result = worker.retryPoison(command, approval);
            check(result.status() == PoisonRetryStatus.UNKNOWN_RESULT,
                    "post-mutation audit failure must report UNKNOWN_RESULT");
            check("CPF_LOG_RECOVERY_AUDIT_UNKNOWN_RESULT".equals(result.errorCode()),
                    "unknown result error code");
            check(fixture.store.snapshot().poisonCount() == 0
                            && fixture.store.snapshot().pendingCount() == 1,
                    "successful poison release must not be rolled back or replayed after audit failure");
            check(worker.recoveryOperationalDiagnostics().unknownResultCount() == 1L,
                    "post-side-effect audit loss must increment unknown result counter");
            String captured = sink.events().toString();
            check(!captured.contains("requester@example.com")
                            && !captured.contains("approver@example.com")
                            && !captured.contains("raw-secret"),
                    "operational audit payload must redact actor PII and reason secrets");
        }
    }

    private static void summaryEventFailureDoesNotReverseRecovery() throws Exception {
        try (Fixture fixture = new Fixture("summary")) {
            check(fixture.store.enqueue(
                    fixture.record("tx-summary"), Map.of("safe", "value"),
                    LogPolicyDecision.cpfDefault(LogPolicyTargetType.ONLINE_TRANSACTION, "*"), new IllegalStateException("db unavailable")),
                    "recovery fixture must be enqueued");
            SequencedSink sink = new SequencedSink(1);
            TransactionLogRecoveryWorker worker = fixture.worker(sink);

            TransactionLogRecoveryWorker.RecoveryResult result = worker.recoverPending();
            check(result.recoveredCount() == 1 && result.failedCount() == 0,
                    "summary event failure must not reverse a completed recovery");
            check(fixture.store.snapshot().pendingCount() == 0
                            && fixture.store.snapshot().processingCount() == 0,
                    "completed journal must remain completed when event sink fails");
            check(worker.recoveryOperationalDiagnostics().operationalEventFailureCount() == 1L,
                    "summary event failure must be counted");
            check("DEGRADED".equals(worker.recoveryRuntimeSnapshot().health()),
                    "operational event loss must degrade runtime health without failing recovery");
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static final class SequencedSink implements TransactionLogRecoveryWorker.RecoveryEventSink {
        private final int failOnCall;
        private int calls;
        private final List<Map<String, Object>> events = new ArrayList<>();

        private SequencedSink(int failOnCall) {
            this.failOnCall = failOnCall;
        }

        @Override
        public void write(String logType, Map<String, Object> event) {
            calls++;
            if (calls == failOnCall) throw new IllegalStateException("event sink unavailable token=secret");
            events.add(event);
        }

        private List<Map<String, Object>> events() {
            return List.copyOf(events);
        }
    }

    private static final class Fixture implements AutoCloseable {
        private final Path root;
        private final TestEnvironment environment;
        private final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
        private final MutableEnvelopeObjectMapper mapper = new MutableEnvelopeObjectMapper();
        private final CpfFileLogWriter writer;
        private final TransactionLogFallbackStore store;

        private Fixture(String name) throws Exception {
            root = Files.createTempDirectory("cpf-recovery-event-" + name + "-");
            environment = new TestEnvironment(root);
            writer = new CpfFileLogWriter(environment, clock);
            store = new TransactionLogFallbackStore(mapper, writer, environment, clock);
        }

        private TransactionLogRecoveryWorker worker(TransactionLogRecoveryWorker.RecoveryEventSink sink) {
            return new TransactionLogRecoveryWorker(
                    store, new TransactionLogService(), environment, clock, sink);
        }

        private void poison(String id, int attempts) throws Exception {
            TransactionLogFallbackEnvelope envelope = new TransactionLogFallbackEnvelope(
                    id, attempts, NOW.minusSeconds(60), NOW,
                    "TEST_FAILURE", null, null, record("tx-" + id.substring(0, 4)),
                    Map.of("safe", "value"), LogPolicyDecision.cpfDefault(LogPolicyTargetType.ONLINE_TRANSACTION, "*"))
                    .claimed("event-harness", NOW);
            mapper.envelope = envelope;
            Path processing = writer.recoveryPath(Path.of(
                    "transaction-db", "processing", id + ".json"));
            Files.createDirectories(processing.getParent());
            Files.writeString(processing, "journal");
            store.poison(envelope);
        }

        private TransactionLogRecord record(String transactionId) {
            TransactionLogRecord record = new TransactionLogRecord();
            record.setTransactionId(transactionId);
            record.setSpanId("span");
            record.setLogType("FINAL");
            record.setSequenceNo(1);
            return record;
        }

        @Override
        public void close() {
            try (var paths = Files.walk(root)) {
                paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                    try { Files.deleteIfExists(path); } catch (Exception ignored) { }
                });
            } catch (Exception ignored) {
                // Test cleanup must not hide the product assertion result.
            }
        }
    }

    private static final class MutableEnvelopeObjectMapper extends ObjectMapper {
        private TransactionLogFallbackEnvelope envelope;

        @Override public byte[] writeValueAsBytes(Object value) {
            if (value instanceof TransactionLogFallbackEnvelope next) envelope = next;
            return "journal".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        }

        @Override public <T> T readValue(File source, Class<T> valueType) {
            return valueType.cast(envelope);
        }
    }

    private static final class TestEnvironment implements Environment {
        private final Path root;

        private TestEnvironment(Path root) {
            this.root = root.toAbsolutePath().normalize();
        }

        @Override public String getProperty(String key) {
            return switch (key) {
                case "cpf.logging.file.base-path" -> root.toString();
                case "cpf.environment" -> "local";
                case "cpf.framework.module-id" -> "CPF";
                case "cpf.framework.instance-id" -> "event-harness";
                case "cpf.logging.file.enabled" -> "false";
                case "cpf.logging.file.timezone" -> "UTC";
                default -> null;
            };
        }

        @SuppressWarnings("unchecked")
        @Override public <T> T getProperty(String key, Class<T> targetType, T defaultValue) {
            String value = getProperty(key);
            if (value == null) return defaultValue;
            if (targetType == Boolean.class) return (T) Boolean.valueOf(value);
            if (targetType == Integer.class) return (T) Integer.valueOf(value);
            if (targetType == Long.class) return (T) Long.valueOf(value);
            return defaultValue;
        }
    }
}
