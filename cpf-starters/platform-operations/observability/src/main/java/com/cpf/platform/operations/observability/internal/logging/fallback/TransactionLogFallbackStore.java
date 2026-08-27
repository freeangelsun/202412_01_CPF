package com.cpf.platform.operations.observability.internal.logging.fallback;

import com.cpf.foundation.runtime.CpfInstanceIdentity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.cpf.platform.operations.observability.internal.logging.CpfTransactionLogIdentity;
import com.cpf.security.api.CpfMaskingRuntime;
import com.cpf.platform.operations.observability.spi.logging.TransactionLogRecord;
import com.cpf.platform.operations.observability.internal.logging.file.CpfFileLogWriter;
import com.cpf.platform.operations.observability.api.logging.policy.LogPolicyDecision;
import org.springframework.core.env.Environment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * CPF DB 거래 로그 저장 실패를 인스턴스별 durable journal에 보존합니다.
 *
 * <p>pending, processing, poison 디렉터리 간 원자적 이동으로 상태를 관리하고,
 * 민감정보를 마스킹한 JSON만 디스크에 기록합니다. 동일 이벤트는 전역 거래 ID,
 * segment ID, 이벤트 유형, 순번으로 만든 복구 ID를 사용해 한 번만 생성합니다.</p>
 */
@Component
public final class TransactionLogFallbackStore implements CpfTransactionLogFallbackPort {
    private static final Pattern SENSITIVE_KEY = Pattern.compile(
            "(?i).*(password|passwd|pwd|token|authorization|secret|credential|resident|account|card|pin|otp).*");
    private static final Pattern RECOVERY_EVENT_ID = Pattern.compile("[0-9a-f]{64}");
    private static final long DEFAULT_MAX_BYTES = 256L * 1024L * 1024L;
    private static final ConcurrentMap<Path, LocalCapacityLock> LOCAL_CAPACITY_LOCKS = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper;
    private final Clock clock;
    private final Path pendingDirectory;
    private final Path processingDirectory;
    private final Path poisonDirectory;
    private final Path capacityLockFile;
    private final String spoolRelativeDirectory;
    private final String workerId;
    private final long maxSpoolBytes;
    private final AtomicLong enqueueFailureCount = new AtomicLong();
    private final AtomicLong staleReclaimedCount = new AtomicLong();
    private final AtomicLong malformedPoisonCount = new AtomicLong();
    private final AtomicLong poisonRetryCount = new AtomicLong();
    private final AtomicLong staleClaimConflictCount = new AtomicLong();

    @Autowired
    public TransactionLogFallbackStore(
            ObjectMapper objectMapper,
            CpfFileLogWriter fileLogWriter,
            Environment environment) {
        this(objectMapper, fileLogWriter, environment, Clock.systemUTC());
    }

    TransactionLogFallbackStore(
            ObjectMapper objectMapper,
            CpfFileLogWriter fileLogWriter,
            Environment environment,
            Clock clock) {
        this.objectMapper = objectMapper;
        this.clock = clock;
        Path root = fileLogWriter.recoveryPath(Path.of("transaction-db"));
        this.pendingDirectory = root.resolve("pending");
        this.processingDirectory = root.resolve("processing");
        this.poisonDirectory = root.resolve("poison");
        this.capacityLockFile = root.resolve(".spool-capacity.lock").toAbsolutePath().normalize();
        this.spoolRelativeDirectory = fileLogWriter.relativeToLogRoot(root)
                .toString()
                .replace('\\', '/');
        this.workerId = CpfInstanceIdentity.current().instanceId();
        long configuredMaxSpoolBytes = environment.getProperty(
                "cpf.logging.db-fallback.max-spool-bytes",
                Long.class,
                DEFAULT_MAX_BYTES);
        if (configuredMaxSpoolBytes <= 0L) {
            throw new IllegalArgumentException("cpf.logging.db-fallback.max-spool-bytes must be positive");
        }
        this.maxSpoolBytes = configuredMaxSpoolBytes;
        initializeDirectories();
        // Claims are reclaimed by the worker only after the configured lease expires.
        // Constructor-time reclaim would steal active work from another instance.
    }

    /**
     * 실패 이벤트를 중복 없이 journal에 추가합니다.
     *
     * @return 새 파일을 만들었으면 {@code true}, 이미 같은 이벤트가 있으면 {@code false}
     */
    @Override
    public synchronized boolean enqueue(
            TransactionLogRecord sourceRecord,
            Map<String, String> sourceDetails,
            LogPolicyDecision logPolicy,
            Throwable failure) {
        if (sourceRecord == null) {
            enqueueFailureCount.incrementAndGet();
            throw new IllegalArgumentException("복구할 거래 로그 레코드는 필수입니다.");
        }
        String recoveryEventId = CpfTransactionLogIdentity.ensure(sourceRecord);
        TransactionLogRecord record = sanitizedCopy(sourceRecord);
        record.setRecoveryEventId(recoveryEventId);
        Map<String, String> details = sanitizeDetails(sourceDetails);
        Instant now = clock.instant();
        TransactionLogFallbackEnvelope envelope = new TransactionLogFallbackEnvelope(
                recoveryEventId,
                0,
                now,
                now,
                failure == null ? "UNKNOWN" : failure.getClass().getSimpleName(),
                null,
                null,
                record,
                details,
                logPolicy);
        Path target = pendingPath(recoveryEventId);
        if (Files.exists(target) || Files.exists(processingPath(recoveryEventId)) || Files.exists(poisonPath(recoveryEventId))) {
            return false;
        }
        try {
            byte[] body = objectMapper.writeValueAsBytes(envelope);
            return writePendingWithCapacityLock(target, body, recoveryEventId);
        } catch (IOException | RuntimeException ex) {
            enqueueFailureCount.incrementAndGet();
            throw new IllegalStateException("DB 거래 로그 복구 journal 저장에 실패했습니다.", ex);
        }
    }

    public synchronized List<Path> pendingFiles() {
        return listJsonFiles(pendingDirectory);
    }

    /**
     * 현재 실행 가능한 항목만 오래된 실패 순서로 반환합니다.
     * 아직 재시도 시각이 되지 않은 항목은 batch quota를 소비하지 않습니다.
     */
    public synchronized List<Path> eligiblePendingFiles(Instant now, int limit) {
        List<EligibleFile> eligible = new ArrayList<>();
        for (Path pending : listJsonFiles(pendingDirectory)) {
            try {
                TransactionLogFallbackEnvelope envelope = objectMapper.readValue(
                        pending.toFile(),
                        TransactionLogFallbackEnvelope.class);
                validateEnvelopeIdentity(envelope, pending.getFileName());
                if (envelope.nextAttemptAt() == null || !envelope.nextAttemptAt().isAfter(now)) {
                    eligible.add(new EligibleFile(pending, envelope.firstFailedAt(), envelope.recoveryEventId()));
                }
            } catch (IOException | RuntimeException ex) {
                moveMalformedToPoison(pending);
            }
        }
        return eligible.stream()
                .sorted(Comparator
                        .comparing(EligibleFile::firstFailedAt, Comparator.nullsFirst(Comparator.naturalOrder()))
                        .thenComparing(value -> value.recoveryEventId(), Comparator.nullsFirst((left, right) -> left.compareTo(right))))
                .limit(Math.max(1, limit))
                .map(value -> value.path())
                .toList();
    }

    public synchronized TransactionLogFallbackEnvelope claim(Path pendingFile) throws IOException {
        Path source = requireDirectChild(pendingDirectory, pendingFile);
        Path processing = processingDirectory.resolve(source.getFileName());
        moveAtomically(source, processing, false);
        try {
            TransactionLogFallbackEnvelope envelope = objectMapper
                    .readValue(processing.toFile(), TransactionLogFallbackEnvelope.class);
            validateEnvelopeIdentity(envelope, processing.getFileName());
            TransactionLogFallbackEnvelope claimed = envelope.claimed(workerId, clock.instant());
            writeAtomically(processing, objectMapper.writeValueAsBytes(claimed), true);
            return claimed;
        } catch (IOException | RuntimeException ex) {
            moveMalformedToPoison(processing);
            if (ex instanceof IOException ioException) throw ioException;
            throw new IOException("invalid recovery journal envelope", ex);
        }
    }

    /**
     * Deletes only the exact claim that completed persistence. A reclaimed/reclaimed-again claim
     * is never removed by a stale worker.
     */
    public synchronized boolean complete(TransactionLogFallbackEnvelope expectedClaim) throws IOException {
        Path processing = processingPath(expectedClaim.recoveryEventId());
        if (!Files.isRegularFile(processing, LinkOption.NOFOLLOW_LINKS) || Files.isSymbolicLink(processing)) {
            staleClaimConflictCount.incrementAndGet();
            return false;
        }
        TransactionLogFallbackEnvelope current = objectMapper.readValue(
                processing.toFile(), TransactionLogFallbackEnvelope.class);
        validateEnvelopeIdentity(current, processing.getFileName());
        if (!sameClaim(current, expectedClaim)) {
            staleClaimConflictCount.incrementAndGet();
            return false;
        }
        return Files.deleteIfExists(processing);
    }

    /** Legacy id-only completion is intentionally fail-closed. */
    public synchronized void complete(String recoveryEventId) throws IOException {
        requireRecoveryEventId(recoveryEventId);
        throw new IOException("claim-scoped completion is required");
    }

    public synchronized void retry(TransactionLogFallbackEnvelope envelope) throws IOException {
        Path processing = requireOwnedProcessing(envelope);
        TransactionLogFallbackEnvelope released = envelope.released(
                envelope.nextAttemptAt(), envelope.lastFailureType());
        writeAtomically(processing, objectMapper.writeValueAsBytes(released), true);
        moveAtomically(processing, pendingPath(envelope.recoveryEventId()), false);
    }

    public synchronized void poison(TransactionLogFallbackEnvelope envelope) throws IOException {
        Path processing = requireOwnedProcessing(envelope);
        TransactionLogFallbackEnvelope released = envelope.released(
                envelope.nextAttemptAt(), envelope.lastFailureType());
        writeAtomically(processing, objectMapper.writeValueAsBytes(released), true);
        moveAtomically(processing, poisonPath(envelope.recoveryEventId()), false);
    }

    /**
     * 승인 시점에 관찰한 attempt count와 현재 poison 레코드가 같은 경우에만 원자적으로 재시도합니다.
     */
    public synchronized PoisonRetryStoreResult retryPoison(
            String recoveryEventId,
            int expectedAttemptCount) throws IOException {
        Path poison = poisonPath(recoveryEventId);
        if (!Files.isRegularFile(poison, LinkOption.NOFOLLOW_LINKS) || Files.isSymbolicLink(poison)) {
            return PoisonRetryStoreResult.NOT_FOUND;
        }
        TransactionLogFallbackEnvelope envelope = objectMapper.readValue(
                poison.toFile(),
                TransactionLogFallbackEnvelope.class);
        validateEnvelopeIdentity(envelope, poison.getFileName());
        if (envelope.attemptCount() != expectedAttemptCount) {
            return PoisonRetryStoreResult.STALE_ATTEMPT;
        }
        TransactionLogFallbackEnvelope approved = envelope.released(clock.instant(), "POISON_RETRY_APPROVED");
        writeAtomically(poison, objectMapper.writeValueAsBytes(approved), true);
        moveAtomically(poison, pendingPath(recoveryEventId), false);
        poisonRetryCount.incrementAndGet();
        return PoisonRetryStoreResult.RETRIED;
    }

    /**
     * 승인 없는 기존 호출은 fail-closed 처리합니다.
     *
     * 호환 안내: {@link com.cpf.platform.operations.observability.api.logging.CpfLogRecoveryOperations}를 통해 승인된 명령을 사용하십시오.
     */
    public synchronized boolean retryPoison(String recoveryEventId) throws IOException {
        Path poison = poisonPath(recoveryEventId);
        if (!Files.isRegularFile(poison, LinkOption.NOFOLLOW_LINKS) || Files.isSymbolicLink(poison)) {
            return false;
        }
        TransactionLogFallbackEnvelope envelope = objectMapper.readValue(
                poison.toFile(),
                TransactionLogFallbackEnvelope.class);
        validateEnvelopeIdentity(envelope, poison.getFileName());
        return false;
    }

    /**
     * lease 시간이 지난 processing 파일만 회수해 정상 처리 중인 claim을 보호합니다.
     */
    public synchronized int reclaimStaleProcessing(Instant now, Duration leaseTimeout) {
        if (now == null || leaseTimeout == null || leaseTimeout.isNegative() || leaseTimeout.isZero()) {
            throw new IllegalArgumentException("positive processing lease and current time are required");
        }
        int reclaimed = 0;
        Instant staleBefore;
        try {
            staleBefore = now.minus(leaseTimeout);
        } catch (RuntimeException underflow) {
            staleBefore = Instant.MIN;
        }
        for (Path processing : listJsonFiles(processingDirectory)) {
            try {
                TransactionLogFallbackEnvelope envelope = objectMapper.readValue(
                        processing.toFile(),
                        TransactionLogFallbackEnvelope.class);
                validateEnvelopeIdentity(envelope, processing.getFileName());
                if (envelope.claimedAt() != null && envelope.claimedAt().isAfter(staleBefore)) {
                    continue;
                }
                TransactionLogFallbackEnvelope released = envelope.released(now, "STALE_PROCESSING_RECLAIMED");
                writeAtomically(processing, objectMapper.writeValueAsBytes(released), true);
                moveAtomically(processing, pendingPath(envelope.recoveryEventId()), false);
                reclaimed++;
                staleReclaimedCount.incrementAndGet();
            } catch (IOException | RuntimeException ex) {
                moveMalformedToPoison(processing);
            }
        }
        return reclaimed;
    }

    public synchronized FallbackSnapshot snapshot() {
        return new FallbackSnapshot(
                pendingFiles().size(),
                listJsonFiles(processingDirectory).size(),
                listJsonFiles(poisonDirectory).size(),
                spoolSizeBytes(),
                maxSpoolBytes,
                enqueueFailureCount.get(),
                staleReclaimedCount.get(),
                malformedPoisonCount.get(),
                poisonRetryCount.get(),
                staleClaimConflictCount.get(),
                spoolRelativeDirectory);
    }

    private TransactionLogRecord sanitizedCopy(TransactionLogRecord source) {
        TransactionLogRecord record = objectMapper.convertValue(source, TransactionLogRecord.class);
        record.setLogIdx(null);
        record.setRecoveryEventId(null);
        record.setMemberNo(maskIdentifier(record.getMemberNo()));
        record.setCustomerNo(maskIdentifier(record.getCustomerNo()));
        record.setDeviceId(maskIdentifier(record.getDeviceId()));
        record.setClientIp(maskIp(record.getClientIp()));
        record.setParameters(CpfMaskingRuntime.mask(record.getParameters()));
        record.setRequestBody(CpfMaskingRuntime.mask(record.getRequestBody()));
        record.setResponse(CpfMaskingRuntime.mask(record.getResponse()));
        record.setMessageContent(CpfMaskingRuntime.mask(record.getMessageContent(), 1000));
        record.setErrorMessage(CpfMaskingRuntime.mask(record.getErrorMessage()));
        record.setExternalMessage(CpfMaskingRuntime.mask(record.getExternalMessage(), 1000));
        record.setInternalMessage(CpfMaskingRuntime.mask(record.getInternalMessage()));
        record.setUserAgent(CpfMaskingRuntime.mask(record.getUserAgent(), 500));
        return record;
    }

    private Map<String, String> sanitizeDetails(Map<String, String> source) {
        Map<String, String> result = new LinkedHashMap<>();
        if (source == null) {
            return result;
        }
        source.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.nullsFirst((left, right) -> left.compareTo(right))))
                .forEach(entry -> {
                    String key = entry.getKey() == null ? "N/A" : CpfMaskingRuntime.truncate(entry.getKey(), 100);
                    String value = SENSITIVE_KEY.matcher(key).matches()
                            ? "***"
                            : CpfMaskingRuntime.mask(entry.getValue());
                    result.put(key, value);
                });
        return result;
    }

    private boolean writePendingWithCapacityLock(
            Path target, byte[] body, String recoveryEventId) throws IOException {
        LocalCapacityLock localLock = retainLocalCapacityLock(capacityLockFile);
        try {
            synchronized (localLock.monitor()) {
                Files.createDirectories(capacityLockFile.getParent());
                try (FileChannel channel = FileChannel.open(
                        capacityLockFile,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.WRITE);
                     FileLock lock = channel.lock()) {
                    if (!lock.isValid()) {
                        throw new IOException("capacity lock acquisition failed");
                    }
                    if (Files.exists(target, LinkOption.NOFOLLOW_LINKS)
                            || Files.exists(processingPath(recoveryEventId), LinkOption.NOFOLLOW_LINKS)
                            || Files.exists(poisonPath(recoveryEventId), LinkOption.NOFOLLOW_LINKS)) {
                        return false;
                    }
                    ensureCapacity(body.length);
                    writeAtomically(target, body, false);
                    return true;
                }
            }
        } finally {
            releaseLocalCapacityLock(capacityLockFile, localLock);
        }
    }

    static int localCapacityLockCount() {
        return LOCAL_CAPACITY_LOCKS.size();
    }

    private static LocalCapacityLock retainLocalCapacityLock(Path key) {
        return LOCAL_CAPACITY_LOCKS.compute(key, (ignored, current) -> {
            LocalCapacityLock selected = current == null ? new LocalCapacityLock() : current;
            selected.retain();
            return selected;
        });
    }

    private static void releaseLocalCapacityLock(Path key, LocalCapacityLock expected) {
        LOCAL_CAPACITY_LOCKS.computeIfPresent(key, (ignored, current) -> {
            if (current != expected) return current;
            return current.release() == 0 ? null : current;
        });
    }

    private void ensureCapacity(long incomingBytes) {
        if (incomingBytes < 0L) {
            throw new IllegalArgumentException("incoming journal size must be non-negative");
        }
        long currentBytes = spoolSizeBytes();
        if (currentBytes > maxSpoolBytes || incomingBytes > maxSpoolBytes - currentBytes) {
            throw new IllegalStateException("DB 거래 로그 복구 journal 용량 제한을 초과했습니다.");
        }
    }

    private long spoolSizeBytes() {
        try (Stream<Path> stream = Files.walk(pendingDirectory.getParent())) {
            long total = 0L;
            for (Path path : stream.filter(Files::isRegularFile).toList()) {
                total = Math.addExact(total, Files.size(path));
            }
            return total;
        } catch (IOException | ArithmeticException ex) {
            enqueueFailureCount.incrementAndGet();
            throw new IllegalStateException("DB 거래 로그 복구 journal 용량을 안전하게 계산할 수 없습니다.", ex);
        }
    }

    private void initializeDirectories() {
        try {
            Files.createDirectories(pendingDirectory);
            Files.createDirectories(processingDirectory);
            Files.createDirectories(poisonDirectory);
        } catch (IOException ex) {
            throw new IllegalStateException("DB 거래 로그 복구 디렉터리를 초기화할 수 없습니다.", ex);
        }
    }


    private void moveMalformedToPoison(Path source) {
        try {
            Path target = poisonDirectory.resolve(source.getFileName());
            moveAtomically(source, target, true);
            malformedPoisonCount.incrementAndGet();
        } catch (IOException moveFailure) {
            enqueueFailureCount.incrementAndGet();
        }
    }

    private List<Path> listJsonFiles(Path directory) {
        try (Stream<Path> stream = Files.list(directory)) {
            return stream.filter(path -> Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)
                            && !Files.isSymbolicLink(path)
                            && path.getFileName().toString().endsWith(".json"))
                    .sorted()
                    .toList();
        } catch (IOException ex) {
            enqueueFailureCount.incrementAndGet();
            return List.of();
        }
    }

    private Path requireDirectChild(Path directory, Path file) {
        if (file == null) {
            throw new IllegalArgumentException("recovery journal path is required");
        }
        Path normalizedDirectory = directory.toAbsolutePath().normalize();
        Path normalized = file.toAbsolutePath().normalize();
        if (!normalized.getParent().equals(normalizedDirectory)) {
            throw new IllegalArgumentException("복구 journal 경로가 허용된 디렉터리를 벗어났습니다.");
        }
        if (Files.isSymbolicLink(normalized)
                || !Files.isRegularFile(normalized, LinkOption.NOFOLLOW_LINKS)) {
            throw new IllegalArgumentException("복구 journal은 일반 파일이어야 합니다.");
        }
        return normalized;
    }

    private Path requireOwnedProcessing(TransactionLogFallbackEnvelope expected) throws IOException {
        if (expected == null) {
            throw new IllegalArgumentException("claimed recovery envelope is required");
        }
        Path processing = processingPath(expected.recoveryEventId());
        if (!Files.isRegularFile(processing, LinkOption.NOFOLLOW_LINKS) || Files.isSymbolicLink(processing)) {
            staleClaimConflictCount.incrementAndGet();
            throw new IOException("claimed recovery journal is no longer processing");
        }
        TransactionLogFallbackEnvelope current = objectMapper.readValue(
                processing.toFile(), TransactionLogFallbackEnvelope.class);
        validateEnvelopeIdentity(current, processing.getFileName());
        if (!sameClaimOwner(current, expected)) {
            staleClaimConflictCount.incrementAndGet();
            throw new IOException("recovery journal claim was replaced or reclaimed");
        }
        return processing;
    }

    private static boolean sameClaim(
            TransactionLogFallbackEnvelope current, TransactionLogFallbackEnvelope expected) {
        return sameClaimOwner(current, expected)
                && current.attemptCount() == expected.attemptCount();
    }

    /**
     * Ownership fencing is based on the unguessable claim token and claimant identity. Retry and
     * poison envelopes intentionally carry the incremented attempt count, so attemptCount cannot
     * participate in ownership validation for those transitions.
     */
    private static boolean sameClaimOwner(
            TransactionLogFallbackEnvelope current, TransactionLogFallbackEnvelope expected) {
        if (current == null || expected == null
                || current.claimToken() == null || expected.claimToken() == null) {
            return false;
        }
        return java.security.MessageDigest.isEqual(
                        current.claimToken().getBytes(java.nio.charset.StandardCharsets.US_ASCII),
                        expected.claimToken().getBytes(java.nio.charset.StandardCharsets.US_ASCII))
                && java.util.Objects.equals(current.claimedBy(), expected.claimedBy())
                && java.util.Objects.equals(current.claimedAt(), expected.claimedAt());
    }

    private void validateEnvelopeIdentity(
            TransactionLogFallbackEnvelope envelope, Path fileName) throws IOException {
        if (envelope == null) throw new IOException("recovery envelope is required");
        String id = requireRecoveryEventId(envelope.recoveryEventId());
        if (fileName == null || !fileName.toString().equals(id + ".json")) {
            throw new IOException("recovery envelope id does not match journal file");
        }
    }

    private String requireRecoveryEventId(String id) {
        String normalized = id == null ? "" : id.trim().toLowerCase(Locale.ROOT);
        if (!RECOVERY_EVENT_ID.matcher(normalized).matches()) {
            throw new IllegalArgumentException("invalid recovery event id");
        }
        return normalized;
    }

    private Path pendingPath(String id) {
        return journalPath(pendingDirectory, id);
    }

    private Path processingPath(String id) {
        return journalPath(processingDirectory, id);
    }

    private Path poisonPath(String id) {
        return journalPath(poisonDirectory, id);
    }

    private Path journalPath(Path directory, String id) {
        Path normalizedDirectory = directory.toAbsolutePath().normalize();
        Path target = normalizedDirectory.resolve(requireRecoveryEventId(id) + ".json").normalize();
        if (!target.getParent().equals(normalizedDirectory)) {
            throw new IllegalArgumentException("recovery journal path escapes its directory");
        }
        return target;
    }

    private void writeAtomically(Path target, byte[] body, boolean replace) throws IOException {
        Files.createDirectories(target.getParent());
        Path temporary = target.resolveSibling(target.getFileName() + ".tmp-"
                + Thread.currentThread().threadId() + "-" + UUID.randomUUID());
        try {
            Files.write(temporary, body, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
            moveAtomically(temporary, target, replace);
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private void moveAtomically(Path source, Path target, boolean replace) throws IOException {
        try {
            if (replace) {
                Files.move(source, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } else {
                Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);
            }
        } catch (AtomicMoveNotSupportedException ex) {
            if (replace) {
                Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
            } else {
                Files.move(source, target);
            }
        }
    }

    private String maskIdentifier(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        String normalized = value.trim();
        return normalized.length() <= 4
                ? "***"
                : normalized.substring(0, 2) + "***" + normalized.substring(normalized.length() - 2);
    }

    private String maskIp(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        int separator = value.lastIndexOf('.');
        return separator > 0 ? value.substring(0, separator + 1) + "***" : "***";
    }


    public enum PoisonRetryStoreResult {
        RETRIED,
        NOT_FOUND,
        STALE_ATTEMPT
    }

    public record FallbackSnapshot(
            int pendingCount,
            int processingCount,
            int poisonCount,
            long spoolBytes,
            long maxSpoolBytes,
            long enqueueFailureCount,
            long staleReclaimedCount,
            long malformedPoisonCount,
            long poisonRetryCount,
            long staleClaimConflictCount,
            String spoolDirectory) {
        public String health() {
            if (enqueueFailureCount > 0 || staleClaimConflictCount > 0 || spoolBytes >= maxSpoolBytes) {
                return "DOWN";
            }
            if (poisonCount > 0 || pendingCount > 0 || processingCount > 0) {
                return "DEGRADED";
            }
            return "UP";
        }
    }

    private record EligibleFile(Path path, Instant firstFailedAt, String recoveryEventId) {
    }

    private static final class LocalCapacityLock {
        private final Object monitor = new Object();
        private int users;

        synchronized void retain() {
            if (users == Integer.MAX_VALUE) {
                throw new IllegalStateException("local capacity lock user count exhausted");
            }
            users++;
        }

        synchronized int release() {
            if (users < 1) {
                throw new IllegalStateException("local capacity lock user count underflow");
            }
            return --users;
        }

        Object monitor() {
            return monitor;
        }
    }

}
