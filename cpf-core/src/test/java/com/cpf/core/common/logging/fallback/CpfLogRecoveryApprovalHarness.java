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
import java.util.Map;

/** 승인 범위, 업무분리, 만료, stale attempt와 one-shot poison replay를 검증합니다. */
public final class CpfLogRecoveryApprovalHarness {
    private static final Instant NOW = Instant.parse("2026-08-05T00:00:00Z");

    private CpfLogRecoveryApprovalHarness() {
    }

    public static void main(String[] args) throws Exception {
        Path root = Files.createTempDirectory("cpf-log-recovery-approval-");
        TestEnvironment environment = new TestEnvironment(root);
        Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
        CpfFileLogWriter writer = new CpfFileLogWriter(environment, clock);
        MutableEnvelopeObjectMapper mapper = new MutableEnvelopeObjectMapper();
        TransactionLogFallbackStore store = new TransactionLogFallbackStore(mapper, writer, environment, clock);
        TransactionLogRecoveryWorker worker = new TransactionLogRecoveryWorker(
                store, new TransactionLogService(), writer, environment, clock);

        String id = "a".repeat(64);
        PoisonRetryCommand command = new PoisonRetryCommand(id, 3, "operator-a", "재처리 token=secret");
        check(!command.toString().contains("token=secret"), "poison command toString must redact reason");
        check(worker.retryPoison(command, null).status() == PoisonRetryStatus.APPROVAL_REQUIRED,
                "approval must be mandatory");

        PoisonRetryApproval sameActor = PoisonRetryApproval.approve(
                "approval-sod", "operator-a", command, NOW.minusSeconds(1), Duration.ofMinutes(5));
        check(worker.retryPoison(command, sameActor).status() == PoisonRetryStatus.SEPARATION_OF_DUTIES,
                "requester and approver must differ");

        PoisonRetryApproval expired = PoisonRetryApproval.approve(
                "approval-expired", "approver-b", command, NOW.minus(Duration.ofHours(2)), Duration.ofMinutes(30));
        check(worker.retryPoison(command, expired).status() == PoisonRetryStatus.APPROVAL_EXPIRED,
                "expired approval must fail closed");

        PoisonRetryCommand differentScope = new PoisonRetryCommand(id, 4, "operator-a", "다른 상태");
        PoisonRetryApproval wrongScope = PoisonRetryApproval.approve(
                "approval-scope", "approver-b", differentScope, NOW.minusSeconds(1), Duration.ofMinutes(5));
        check(worker.retryPoison(command, wrongScope).status() == PoisonRetryStatus.APPROVAL_SCOPE_MISMATCH,
                "approval must be bound to immutable command hash");

        TransactionLogFallbackEnvelope envelope = envelope(id, 3, clock)
                .claimed("approval-harness", clock.instant());
        mapper.envelope = envelope;
        createProcessingJournal(writer, envelope.recoveryEventId());
        store.poison(envelope);
        PoisonRetryApproval valid = PoisonRetryApproval.approve(
                "approval-valid", "approver-b", command, NOW.minusSeconds(1), Duration.ofMinutes(5));
        check(worker.retryPoison(command, valid).status() == PoisonRetryStatus.RETRIED,
                "matching approval must move poison to pending");
        check(worker.retryPoison(command, valid).status() == PoisonRetryStatus.NOT_FOUND,
                "one approval must not replay an already released poison item");

        String staleId = "b".repeat(64);
        TransactionLogFallbackEnvelope staleEnvelope = envelope(staleId, 5, clock)
                .claimed("approval-harness", clock.instant());
        mapper.envelope = staleEnvelope;
        createProcessingJournal(writer, staleEnvelope.recoveryEventId());
        store.poison(staleEnvelope);
        PoisonRetryCommand staleCommand = new PoisonRetryCommand(staleId, 4, "operator-a", "stale attempt");
        PoisonRetryApproval staleApproval = PoisonRetryApproval.approve(
                "approval-stale", "approver-b", staleCommand, NOW.minusSeconds(1), Duration.ofMinutes(5));
        check(worker.retryPoison(staleCommand, staleApproval).status() == PoisonRetryStatus.STALE_ATTEMPT,
                "changed poison state must invalidate the approval");

        boolean longLifetimeRejected = false;
        try {
            PoisonRetryApproval.approve(
                    "approval-too-long", "approver-b", command, NOW, Duration.ofHours(25));
        } catch (IllegalArgumentException expected) {
            longLifetimeRejected = true;
        }
        check(longLifetimeRejected, "approval lifetime must be bounded");
        check(!worker.retryPoison(id), "legacy unapproved replay must fail closed");
        var runtime = worker.recoveryRuntimeSnapshot();
        check(runtime.scheduledRecoveryEnabled(), "scheduled recovery status is visible");
        check(runtime.pendingCount() >= 1 && "DEGRADED".equals(runtime.health()),
                "pending recovery backlog must be operationally visible");
        check(!runtime.spoolDirectory().startsWith("/"), "runtime status must not expose an absolute spool path");

        System.out.println("CPF_LOG_RECOVERY_APPROVAL_HARNESS_PASS");
    }

    private static void createProcessingJournal(CpfFileLogWriter writer, String recoveryEventId) throws Exception {
        Path processing = writer.recoveryPath(Path.of(
                "transaction-db", "processing", recoveryEventId + ".json"));
        Files.createDirectories(processing.getParent());
        Files.writeString(processing, "journal");
    }

    private static TransactionLogFallbackEnvelope envelope(String id, int attemptCount, Clock clock) {
        TransactionLogRecord record = new TransactionLogRecord();
        record.setTransactionId("tx-" + id.substring(0, 4));
        record.setSpanId("span");
        record.setLogType("FINAL");
        record.setSequenceNo(1);
        return new TransactionLogFallbackEnvelope(
                id,
                attemptCount,
                clock.instant().minusSeconds(60),
                clock.instant(),
                "TEST_FAILURE",
                null,
                null,
                record,
                Map.of("safe", "value"),
                LogPolicyDecision.cpfDefault(LogPolicyTargetType.ONLINE_TRANSACTION, "*"));
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static final class MutableEnvelopeObjectMapper extends ObjectMapper {
        private TransactionLogFallbackEnvelope envelope;

        @Override
        public byte[] writeValueAsBytes(Object value) {
            if (value instanceof TransactionLogFallbackEnvelope next) {
                envelope = next;
            }
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
                case "cpf.framework.instance-id" -> "approval-harness";
                case "cpf.logging.file.enabled" -> "false";
                case "cpf.logging.file.timezone" -> "UTC";
                default -> null;
            };
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T getProperty(String key, Class<T> targetType, T defaultValue) {
            String value = getProperty(key);
            if (value == null) {
                return defaultValue;
            }
            if (targetType == Boolean.class) {
                return (T) Boolean.valueOf(value);
            }
            if (targetType == Integer.class) {
                return (T) Integer.valueOf(value);
            }
            if (targetType == Long.class) {
                return (T) Long.valueOf(value);
            }
            return (T) value;
        }

        @Override
        public String[] getActiveProfiles() {
            return new String[] {"test"};
        }
    }
}
