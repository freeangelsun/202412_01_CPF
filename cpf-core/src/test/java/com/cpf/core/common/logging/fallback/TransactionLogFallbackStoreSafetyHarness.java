package com.cpf.core.common.logging.fallback;

import com.cpf.core.api.logging.policy.LogPolicyDecision;
import com.cpf.core.common.logging.TransactionLogRecord;
import com.cpf.core.common.logging.file.CpfFileLogWriter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.env.Environment;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Durable DB-log spool path, capacity and identity boundaries.
 */
public final class TransactionLogFallbackStoreSafetyHarness {
    private TransactionLogFallbackStoreSafetyHarness() {
    }

    public static void main(String[] args) throws Exception {
        rejectsTraversalAndSymlink();
        validatesPoisonEnvelopeIdentity();
        rejectsInvalidLease();
        fencesStaleClaimsAndLegacyCompletion();
        serializesCapacityAcrossStoreInstances();
        releasesCapacityLocksAcrossUniqueRoots();
        System.out.println("CPF_LOG_SPOOL_SAFETY_HARNESS_PASS");
    }

    private static void rejectsTraversalAndSymlink() throws Exception {
        Path root = Files.createTempDirectory("cpf-spool-path-");
        TestEnvironment environment = new TestEnvironment(root, 1024L * 1024L);
        Clock clock = fixedClock();
        CpfFileLogWriter writer = new CpfFileLogWriter(environment, clock);
        TransactionLogFallbackStore store = new TransactionLogFallbackStore(
                new FixedBodyObjectMapper(128), writer, environment, clock);

        boolean traversalRejected = false;
        try {
            store.retryPoison("../../escaped");
        } catch (IllegalArgumentException expected) {
            traversalRejected = true;
        }
        check(traversalRejected, "path traversal must be rejected before filesystem access");

        Path outside = Files.createTempFile("cpf-spool-outside-", ".json");
        Path poisonDirectory = writer.recoveryPath(Path.of("transaction-db", "poison"));
        Files.createDirectories(poisonDirectory);
        String id = "a".repeat(64);
        Path symbolicLink = poisonDirectory.resolve(id + ".json");
        Files.createSymbolicLink(symbolicLink, outside);
        check(!store.retryPoison(id), "symbolic-link poison entries must not be followed");
    }

    private static void validatesPoisonEnvelopeIdentity() throws Exception {
        Path root = Files.createTempDirectory("cpf-spool-identity-");
        TestEnvironment environment = new TestEnvironment(root, 1024L * 1024L);
        Clock clock = fixedClock();
        CpfFileLogWriter writer = new CpfFileLogWriter(environment, clock);
        String fileId = "b".repeat(64);
        String envelopeId = "c".repeat(64);
        Path poison = writer.recoveryPath(Path.of("transaction-db", "poison", fileId + ".json"));
        Files.createDirectories(poison.getParent());
        Files.writeString(poison, "{}");
        TransactionLogFallbackEnvelope mismatched = new TransactionLogFallbackEnvelope(
                envelopeId,
                1,
                clock.instant(),
                clock.instant(),
                "TEST",
                null,
                null,
                new TransactionLogRecord(),
                Map.of(),
                null);
        TransactionLogFallbackStore store = new TransactionLogFallbackStore(
                new EnvelopeObjectMapper(mismatched), writer, environment, clock);
        boolean rejected = false;
        try {
            store.retryPoison(fileId);
        } catch (Exception expected) {
            rejected = expected instanceof IOException;
        }
        check(rejected, "poison filename and envelope identity must match");
    }

    private static void rejectsInvalidLease() throws Exception {
        Path root = Files.createTempDirectory("cpf-spool-lease-");
        TestEnvironment environment = new TestEnvironment(root, 1024L * 1024L);
        Clock clock = fixedClock();
        TransactionLogFallbackStore store = new TransactionLogFallbackStore(
                new FixedBodyObjectMapper(128),
                new CpfFileLogWriter(environment, clock),
                environment,
                clock);
        boolean rejected = false;
        try {
            store.reclaimStaleProcessing(clock.instant(), Duration.ZERO);
        } catch (IllegalArgumentException expected) {
            rejected = true;
        }
        check(rejected, "processing lease must be positive");
    }


    private static void fencesStaleClaimsAndLegacyCompletion() throws Exception {
        Path root = Files.createTempDirectory("cpf-spool-claim-");
        TestEnvironment environment = new TestEnvironment(root, 1024L * 1024L);
        MutableClock clock = new MutableClock(Instant.parse("2026-08-05T00:00:00Z"));
        TrackingObjectMapper mapper = new TrackingObjectMapper();
        TransactionLogFallbackStore store = new TransactionLogFallbackStore(
                mapper, new CpfFileLogWriter(environment, clock), environment, clock);

        check(store.enqueue(record("tx-claim"), Map.of(), null, new IllegalStateException("failure")),
                "claim fixture must be enqueued");
        TransactionLogFallbackEnvelope first = store.claim(store.pendingFiles().get(0));
        check(first.claimToken() != null && !first.claimToken().isBlank(),
                "claim must carry an unguessable fencing token");

        clock.advance(Duration.ofMinutes(2));
        check(store.reclaimStaleProcessing(clock.instant(), Duration.ofMinutes(1)) == 1,
                "expired claim must be reclaimed");
        TransactionLogFallbackEnvelope second = store.claim(store.pendingFiles().get(0));
        check(!first.claimToken().equals(second.claimToken()),
                "reclaimed work must receive a new claim token");

        boolean staleRetryRejected = false;
        try {
            store.retry(first.nextAttempt(1, clock.instant(), "STALE"));
        } catch (IOException expected) {
            staleRetryRejected = true;
        }
        check(staleRetryRejected, "stale worker must not rewrite a replacement claim");
        check(!store.complete(first), "stale worker must not delete a replacement claim");
        check(store.snapshot().processingCount() == 1,
                "replacement claim must remain after stale completion");

        boolean legacyRejected = false;
        try {
            store.complete(first.recoveryEventId());
        } catch (IOException expected) {
            legacyRejected = true;
        }
        check(legacyRejected, "id-only completion must fail closed");
        check(store.complete(second), "current claim must complete successfully");
        check(store.snapshot().processingCount() == 0, "completed current claim must be removed");
        check(store.snapshot().staleClaimConflictCount() >= 2,
                "stale claim conflicts must be observable");
    }

    private static void serializesCapacityAcrossStoreInstances() throws Exception {
        Path root = Files.createTempDirectory("cpf-spool-capacity-");
        int fixedBodyBytes = 512;
        TestEnvironment environment = new TestEnvironment(root, fixedBodyBytes + 64L);
        Clock clock = fixedClock();
        TransactionLogFallbackStore left = new TransactionLogFallbackStore(
                new FixedBodyObjectMapper(fixedBodyBytes),
                new CpfFileLogWriter(environment, clock),
                environment,
                clock);
        TransactionLogFallbackStore right = new TransactionLogFallbackStore(
                new FixedBodyObjectMapper(fixedBodyBytes),
                new CpfFileLogWriter(environment, clock),
                environment,
                clock);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger accepted = new AtomicInteger();
        AtomicInteger rejected = new AtomicInteger();
        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            Future<?> first = executor.submit(() -> enqueueAfter(start, left, record("tx-left"), accepted, rejected));
            Future<?> second = executor.submit(() -> enqueueAfter(start, right, record("tx-right"), accepted, rejected));
            start.countDown();
            first.get();
            second.get();
        }
        check(accepted.get() == 1, "only one journal may fit within the shared capacity");
        check(rejected.get() == 1, "the competing store must fail closed at the shared capacity boundary");
        check(left.pendingFiles().size() == 1, "shared spool must contain exactly one accepted journal");
        check(TransactionLogFallbackStore.localCapacityLockCount() == 0,
                "local capacity lock registry must release idle paths");
    }

    private static void releasesCapacityLocksAcrossUniqueRoots() throws Exception {
        check(TransactionLogFallbackStore.localCapacityLockCount() == 0,
                "capacity lock registry must start empty");
        Clock clock = fixedClock();
        for (int index = 0; index < 64; index++) {
            Path root = Files.createTempDirectory("cpf-spool-lock-lifecycle-");
            TestEnvironment environment = new TestEnvironment(root, 1024L * 1024L);
            TransactionLogFallbackStore store = new TransactionLogFallbackStore(
                    new FixedBodyObjectMapper(64),
                    new CpfFileLogWriter(environment, clock),
                    environment,
                    clock);
            check(store.enqueue(record("tx-lock-" + index), Map.of(), null,
                            new IllegalStateException("failure")),
                    "unique-root enqueue must succeed");
            check(TransactionLogFallbackStore.localCapacityLockCount() == 0,
                    "completed unique-root operations must release local capacity locks");
        }
    }

    private static void enqueueAfter(
            CountDownLatch start,
            TransactionLogFallbackStore store,
            TransactionLogRecord record,
            AtomicInteger accepted,
            AtomicInteger rejected) {
        try {
            start.await();
            if (store.enqueue(record, Map.of("token", "secret"), null, new IllegalStateException("secret"))) {
                accepted.incrementAndGet();
            }
        } catch (IllegalStateException expected) {
            rejected.incrementAndGet();
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("capacity harness interrupted", interrupted);
        }
    }

    private static TransactionLogRecord record(String transactionId) {
        TransactionLogRecord record = new TransactionLogRecord();
        record.setTransactionId(transactionId);
        record.setSpanId("span");
        record.setLogType("FINAL");
        record.setSequenceNo(1);
        return record;
    }

    private static Clock fixedClock() {
        return Clock.fixed(Instant.parse("2026-08-05T00:00:00Z"), ZoneOffset.UTC);
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static final class FixedBodyObjectMapper extends ObjectMapper {
        private final byte[] body;

        private FixedBodyObjectMapper(int bodyBytes) {
            this.body = new byte[bodyBytes];
        }

        @Override
        public byte[] writeValueAsBytes(Object value) {
            return body.clone();
        }
    }

    private static final class EnvelopeObjectMapper extends ObjectMapper {
        private final TransactionLogFallbackEnvelope envelope;

        private EnvelopeObjectMapper(TransactionLogFallbackEnvelope envelope) {
            this.envelope = envelope;
        }

        @Override
        public <T> T readValue(File source, Class<T> valueType) {
            return valueType.cast(envelope);
        }
    }


    private static final class TrackingObjectMapper extends ObjectMapper {
        private final ConcurrentMap<String, TransactionLogFallbackEnvelope> envelopes = new ConcurrentHashMap<>();

        @Override
        public byte[] writeValueAsBytes(Object value) {
            if (value instanceof TransactionLogFallbackEnvelope envelope) {
                envelopes.put(envelope.recoveryEventId(), envelope);
            }
            return new byte[128];
        }

        @Override
        public <T> T readValue(File source, Class<T> valueType) throws IOException {
            String fileName = source.toPath().getFileName().toString();
            String id = fileName.endsWith(".json")
                    ? fileName.substring(0, fileName.length() - 5)
                    : fileName;
            TransactionLogFallbackEnvelope envelope = envelopes.get(id);
            if (envelope == null) {
                throw new IOException("missing tracked envelope: " + id);
            }
            return valueType.cast(envelope);
        }
    }

    private static final class MutableClock extends Clock {
        private volatile Instant current;

        private MutableClock(Instant current) {
            this.current = current;
        }

        private void advance(Duration duration) {
            current = current.plus(duration);
        }

        @Override
        public java.time.ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return current;
        }
    }

    private static final class TestEnvironment implements Environment {
        private final Path root;
        private final long maxSpoolBytes;

        private TestEnvironment(Path root, long maxSpoolBytes) {
            this.root = root.toAbsolutePath().normalize();
            this.maxSpoolBytes = maxSpoolBytes;
        }

        @Override
        public String getProperty(String key) {
            return switch (key) {
                case "cpf.logging.file.base-path" -> root.toString();
                case "cpf.environment" -> "local";
                case "cpf.framework.module-id" -> "CPF";
                case "cpf.framework.instance-id" -> "spool-harness";
                case "cpf.logging.file.enabled" -> "false";
                case "cpf.logging.file.timezone" -> "UTC";
                default -> null;
            };
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T getProperty(String key, Class<T> targetType, T defaultValue) {
            if ("cpf.logging.db-fallback.max-spool-bytes".equals(key) && targetType == Long.class) {
                return (T) Long.valueOf(maxSpoolBytes);
            }
            String value = getProperty(key);
            if (value == null) {
                return defaultValue;
            }
            if (targetType == Boolean.class) {
                return (T) Boolean.valueOf(value);
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
