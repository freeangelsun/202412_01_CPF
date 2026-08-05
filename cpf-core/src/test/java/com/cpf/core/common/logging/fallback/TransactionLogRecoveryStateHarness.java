package com.cpf.core.common.logging.fallback;

import com.cpf.core.api.logging.policy.LogPolicyDecision;
import com.cpf.core.api.logging.policy.LogPolicyTargetType;
import com.cpf.core.api.state.CpfOperationState;
import com.cpf.core.api.state.CpfStateSearchRequest;
import com.cpf.core.api.state.CpfStateSearchResult;
import com.cpf.core.common.logging.TransactionLogRecord;
import com.cpf.core.common.logging.file.CpfFileLogWriter;
import com.cpf.core.internal.state.InMemoryCpfStateStore;
import com.cpf.core.service.common.logging.TransactionLogService;
import com.cpf.core.service.state.DefaultCpfStateOperations;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import org.springframework.core.env.Environment;

/** Actual log-recovery consumer must persist RUNNING/UNKNOWN/terminal state through the common boundary. */
public final class TransactionLogRecoveryStateHarness {
    private static final Instant NOW = Instant.parse("2026-08-05T00:00:00Z");

    private TransactionLogRecoveryStateHarness() {}

    public static void main(String[] args) throws Exception {
        verifiesSuccessfulRecoveryState();
        verifiesPoisonFailureState();
        System.out.println("CPF_LOG_RECOVERY_STATE_HARNESS_PASS");
    }

    private static void verifiesSuccessfulRecoveryState() throws Exception {
        Fixture fixture = fixture(false);
        check(fixture.store.enqueue(record("tx-state-ok"), Map.of(),
                LogPolicyDecision.cpfDefault(LogPolicyTargetType.ONLINE_TRANSACTION, "*"), new IllegalStateException("db down")),
                "success fixture enqueue");
        TransactionLogRecoveryWorker.RecoveryResult result = fixture.worker.recoverPending();
        check(result.recoveredCount() == 1 && result.failedCount() == 0, "recovery must succeed");
        CpfStateSearchResult states = fixture.states.search(new CpfStateSearchRequest(
                "log-recovery:", java.util.Set.of(), null, 10));
        check(states.status() == CpfStateSearchResult.Status.SUCCESS && states.items().size() == 1,
                "recovery state must be queryable");
        check(states.items().getFirst().state() == CpfOperationState.SUCCEEDED,
                "successful recovery must reach SUCCEEDED");
    }

    private static void verifiesPoisonFailureState() throws Exception {
        Fixture fixture = fixture(true);
        check(fixture.store.enqueue(record("tx-state-fail"), Map.of(),
                LogPolicyDecision.cpfDefault(LogPolicyTargetType.ONLINE_TRANSACTION, "*"), new IllegalStateException("db down")),
                "failure fixture enqueue");
        TransactionLogRecoveryWorker.RecoveryResult result = fixture.worker.recoverPending();
        check(result.failedCount() == 1 && result.fallback().poisonCount() == 1,
                "max-attempt failure must be poisoned");
        CpfStateSearchResult states = fixture.states.search(new CpfStateSearchRequest(
                "log-recovery:", java.util.Set.of(), null, 10));
        check(states.items().size() == 1 && states.items().getFirst().state() == CpfOperationState.FAILED,
                "poisoned recovery must reach FAILED");
    }

    private static Fixture fixture(boolean failPersistence) throws Exception {
        Path root = Files.createTempDirectory("cpf-log-recovery-state-");
        TestEnvironment environment = new TestEnvironment(root);
        Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
        CpfFileLogWriter writer = new CpfFileLogWriter(environment, clock);
        MutableEnvelopeObjectMapper mapper = new MutableEnvelopeObjectMapper();
        TransactionLogFallbackStore store = new TransactionLogFallbackStore(mapper, writer, environment, clock);
        InMemoryCpfStateStore stateStore = new InMemoryCpfStateStore();
        DefaultCpfStateOperations states = new DefaultCpfStateOperations(stateStore, clock);
        TransactionLogService service = failPersistence ? new FailingTransactionLogService() : new TransactionLogService();
        TransactionLogRecoveryWorker worker = new TransactionLogRecoveryWorker(
                store, service, writer, environment, clock, states);
        return new Fixture(store, worker, states);
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

    private record Fixture(
            TransactionLogFallbackStore store,
            TransactionLogRecoveryWorker worker,
            DefaultCpfStateOperations states) {}

    private static final class FailingTransactionLogService extends TransactionLogService {
        @Override
        public void saveTransactionLog(
                TransactionLogRecord record,
                Map<String, String> details,
                LogPolicyDecision policy) {
            throw new IllegalStateException("simulated ambiguous persistence failure");
        }
    }

    private static final class MutableEnvelopeObjectMapper extends ObjectMapper {
        private TransactionLogFallbackEnvelope envelope;

        @Override
        public byte[] writeValueAsBytes(Object value) {
            if (value instanceof TransactionLogFallbackEnvelope next) envelope = next;
            return "journal".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        }

        @Override
        public <T> T readValue(File source, Class<T> valueType) {
            return valueType.cast(envelope);
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
                case "cpf.framework.instance-id" -> "state-harness";
                case "cpf.logging.file.enabled" -> "false";
                case "cpf.logging.file.timezone" -> "UTC";
                default -> null;
            };
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T getProperty(String key, Class<T> targetType, T defaultValue) {
            if ("cpf.logging.db-fallback.max-attempts".equals(key) && targetType == Integer.class) {
                return (T) Integer.valueOf(1);
            }
            String value = getProperty(key);
            if (value == null) return defaultValue;
            if (targetType == Boolean.class) return (T) Boolean.valueOf(value);
            if (targetType == Integer.class) return (T) Integer.valueOf(value);
            if (targetType == Long.class) return (T) Long.valueOf(value);
            return (T) value;
        }

        @Override
        public String[] getActiveProfiles() {
            return new String[] {"test"};
        }
    }
}
