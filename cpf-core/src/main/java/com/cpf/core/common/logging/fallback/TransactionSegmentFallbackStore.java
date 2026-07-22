package cpf.pfw.common.logging.fallback;

import com.fasterxml.jackson.databind.ObjectMapper;
import cpf.pfw.common.logging.SensitiveDataMasker;
import cpf.pfw.common.logging.file.CpfFileLogWriter;
import cpf.pfw.common.logging.segment.TransactionSegmentRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

/**
 * 거래 구간 시작·종료 이벤트를 인스턴스별 durable journal에 보존합니다.
 *
 * <p>START와 END를 각각 결정적 event ID로 저장하고, 거래 순번과 이벤트 유형으로 정렬해
 * 시작 이벤트가 종료 이벤트보다 먼저 복구되도록 보장합니다. pending, processing, poison
 * 디렉터리 사이의 원자적 이동으로 다중 worker의 중복 claim을 차단합니다.</p>
 */
@Component
public class TransactionSegmentFallbackStore {
    private static final int EVENT_VERSION = 1;
    private static final long DEFAULT_MAX_BYTES = 256L * 1024L * 1024L;

    private final ObjectMapper objectMapper;
    private final Clock clock;
    private final Path pendingDirectory;
    private final Path processingDirectory;
    private final Path poisonDirectory;
    private final String spoolRelativeDirectory;
    private final String workerId;
    private final long maxSpoolBytes;
    private final AtomicLong enqueueFailureCount = new AtomicLong();
    private final AtomicLong staleReclaimedCount = new AtomicLong();
    private final AtomicLong malformedPoisonCount = new AtomicLong();
    private final AtomicLong poisonRetryCount = new AtomicLong();

    @Autowired
    public TransactionSegmentFallbackStore(
            ObjectMapper objectMapper,
            CpfFileLogWriter fileLogWriter,
            Environment environment) {
        this(objectMapper, fileLogWriter, environment, Clock.systemUTC());
    }

    TransactionSegmentFallbackStore(
            ObjectMapper objectMapper,
            CpfFileLogWriter fileLogWriter,
            Environment environment,
            Clock clock) {
        this.objectMapper = objectMapper;
        this.clock = clock;
        Path root = fileLogWriter.recoveryPath(Path.of("segment-db"));
        this.pendingDirectory = root.resolve("pending");
        this.processingDirectory = root.resolve("processing");
        this.poisonDirectory = root.resolve("poison");
        this.spoolRelativeDirectory = fileLogWriter.relativeToLogRoot(root).toString().replace('\\', '/');
        this.workerId = environment.getProperty("cpf.framework.instance-id", "pfw-local");
        this.maxSpoolBytes = environment.getProperty(
                "cpf.logging.segment-fallback.max-spool-bytes",
                Long.class,
                DEFAULT_MAX_BYTES);
        initializeDirectories();
        reclaimStaleProcessing(clock.instant(), Duration.ZERO);
    }

    public boolean enqueueStart(TransactionSegmentRecord source, Throwable failure) {
        return enqueue("START", source, failure);
    }

    public boolean enqueueEnd(TransactionSegmentRecord source, Throwable failure) {
        return enqueue("END", source, failure);
    }

    private synchronized boolean enqueue(String eventType, TransactionSegmentRecord source, Throwable failure) {
        if (source == null || source.getTransactionSegmentId() == null || source.getTransactionSegmentId().isBlank()) {
            enqueueFailureCount.incrementAndGet();
            throw new IllegalArgumentException("복구할 거래 구간과 거래 구간 ID는 필수입니다.");
        }
        TransactionSegmentRecord record = sanitizedCopy(source);
        String eventId = eventId(eventType, record);
        Path pending = pendingPath(eventId);
        if (Files.exists(pending) || Files.exists(processingPath(eventId)) || Files.exists(poisonPath(eventId))) {
            return false;
        }
        Instant now = clock.instant();
        TransactionSegmentRecoveryEnvelope envelope = new TransactionSegmentRecoveryEnvelope(
                eventId,
                EVENT_VERSION,
                eventType,
                record.getSequenceNo(),
                record.getTransactionGlobalId(),
                record.getTransactionSegmentId(),
                record.getParentSegmentId(),
                0,
                now,
                now,
                failure == null ? "UNKNOWN" : failure.getClass().getSimpleName(),
                null,
                null,
                record);
        try {
            byte[] body = objectMapper.writeValueAsBytes(envelope);
            ensureCapacity(body.length);
            writeAtomically(pending, body, false);
            return true;
        } catch (IOException | RuntimeException ex) {
            enqueueFailureCount.incrementAndGet();
            throw new IllegalStateException("거래 구간 복구 journal 저장에 실패했습니다.", ex);
        }
    }

    public synchronized List<Path> eligiblePendingFiles(Instant now, int limit) {
        List<EligibleFile> eligible = new ArrayList<>();
        for (Path pending : listJsonFiles(pendingDirectory)) {
            try {
                TransactionSegmentRecoveryEnvelope envelope = read(pending);
                if (envelope.nextAttemptAt() == null || !envelope.nextAttemptAt().isAfter(now)) {
                    eligible.add(new EligibleFile(pending, envelope));
                }
            } catch (IOException ex) {
                moveMalformedToPoison(pending);
            }
        }
        return eligible.stream()
                .sorted(Comparator
                        .comparing((EligibleFile item) -> text(item.envelope().transactionGlobalId()))
                        .thenComparingInt(item -> item.envelope().sequenceNo())
                        .thenComparingInt(item -> "START".equals(item.envelope().eventType()) ? 0 : 1)
                        .thenComparing(item -> item.envelope().firstFailedAt(), Comparator.nullsFirst(Comparator.naturalOrder()))
                        .thenComparing(item -> item.envelope().recoveryEventId()))
                .limit(Math.max(1, limit))
                .map(EligibleFile::path)
                .toList();
    }

    public synchronized TransactionSegmentRecoveryEnvelope claim(Path pendingFile) throws IOException {
        Path source = requireDirectChild(pendingDirectory, pendingFile);
        Path processing = processingDirectory.resolve(source.getFileName());
        moveAtomically(source, processing, false);
        try {
            TransactionSegmentRecoveryEnvelope claimed = read(processing).claimed(workerId, clock.instant());
            writeAtomically(processing, objectMapper.writeValueAsBytes(claimed), true);
            return claimed;
        } catch (IOException ex) {
            restoreClaimAfterReadFailure(processing);
            throw ex;
        }
    }

    public synchronized void complete(String eventId) throws IOException {
        Files.deleteIfExists(processingPath(eventId));
    }

    public synchronized void retry(TransactionSegmentRecoveryEnvelope envelope) throws IOException {
        writeAtomically(pendingPath(envelope.recoveryEventId()), objectMapper.writeValueAsBytes(envelope), true);
        Files.deleteIfExists(processingPath(envelope.recoveryEventId()));
    }

    public synchronized void poison(TransactionSegmentRecoveryEnvelope envelope) throws IOException {
        writeAtomically(poisonPath(envelope.recoveryEventId()), objectMapper.writeValueAsBytes(envelope), true);
        Files.deleteIfExists(processingPath(envelope.recoveryEventId()));
    }

    public synchronized boolean retryPoison(String eventId) throws IOException {
        Path poison = poisonPath(eventId);
        if (!Files.isRegularFile(poison)) {
            return false;
        }
        TransactionSegmentRecoveryEnvelope approved = read(poison)
                .retry(0, clock.instant(), "POISON_RETRY_APPROVED");
        writeAtomically(pendingPath(eventId), objectMapper.writeValueAsBytes(approved), false);
        Files.delete(poison);
        poisonRetryCount.incrementAndGet();
        return true;
    }

    public synchronized int reclaimStaleProcessing(Instant now, Duration leaseTimeout) {
        int reclaimed = 0;
        Instant staleBefore = now.minus(leaseTimeout);
        for (Path processing : listJsonFiles(processingDirectory)) {
            try {
                TransactionSegmentRecoveryEnvelope envelope = read(processing);
                if (envelope.claimedAt() != null && envelope.claimedAt().isAfter(staleBefore)) {
                    continue;
                }
                TransactionSegmentRecoveryEnvelope released = envelope.retry(
                        envelope.attemptCount(),
                        now,
                        "STALE_PROCESSING_RECLAIMED");
                writeAtomically(pendingPath(envelope.recoveryEventId()), objectMapper.writeValueAsBytes(released), false);
                Files.deleteIfExists(processing);
                reclaimed++;
                staleReclaimedCount.incrementAndGet();
            } catch (IOException ex) {
                moveMalformedToPoison(processing);
            }
        }
        return reclaimed;
    }

    public synchronized SegmentFallbackSnapshot snapshot() {
        return new SegmentFallbackSnapshot(
                listJsonFiles(pendingDirectory).size(),
                listJsonFiles(processingDirectory).size(),
                listJsonFiles(poisonDirectory).size(),
                spoolSizeBytes(),
                maxSpoolBytes,
                enqueueFailureCount.get(),
                staleReclaimedCount.get(),
                malformedPoisonCount.get(),
                poisonRetryCount.get(),
                spoolRelativeDirectory);
    }

    private TransactionSegmentRecord sanitizedCopy(TransactionSegmentRecord source) {
        TransactionSegmentRecord record = objectMapper.convertValue(source, TransactionSegmentRecord.class);
        record.setFailureMessageMasked(SensitiveDataMasker.mask(record.getFailureMessageMasked(), 1000));
        record.setRequestHeaderSnapshotMasked(SensitiveDataMasker.mask(record.getRequestHeaderSnapshotMasked(), 4000));
        record.setResponseHeaderSnapshotMasked(SensitiveDataMasker.mask(record.getResponseHeaderSnapshotMasked(), 4000));
        record.setExtensionHeaderSnapshotMasked(SensitiveDataMasker.mask(record.getExtensionHeaderSnapshotMasked(), 4000));
        record.setCustomerNoMasked(maskIdentifier(record.getCustomerNoMasked()));
        record.setMemberNoMasked(maskIdentifier(record.getMemberNoMasked()));
        record.setUserIdMasked(maskIdentifier(record.getUserIdMasked()));
        record.setOperatorIdMasked(maskIdentifier(record.getOperatorIdMasked()));
        return record;
    }

    private String eventId(String eventType, TransactionSegmentRecord record) {
        String source = text(record.getTransactionGlobalId()) + '|'
                + text(record.getTransactionSegmentId()) + '|'
                + eventType + '|'
                + record.getSequenceNo() + '|'
                + EVENT_VERSION;
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(source.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("거래 구간 복구 event ID를 만들 수 없습니다.", ex);
        }
    }

    private void ensureCapacity(long incomingBytes) {
        if (incomingBytes < 0 || spoolSizeBytes() + incomingBytes > maxSpoolBytes) {
            throw new IllegalStateException("거래 구간 복구 journal 용량 제한을 초과했습니다.");
        }
    }

    private long spoolSizeBytes() {
        try (Stream<Path> stream = Files.walk(pendingDirectory.getParent())) {
            return stream.filter(Files::isRegularFile).mapToLong(path -> {
                try {
                    return Files.size(path);
                } catch (IOException ex) {
                    return 0L;
                }
            }).sum();
        } catch (IOException ex) {
            return 0L;
        }
    }

    private void initializeDirectories() {
        try {
            Files.createDirectories(pendingDirectory);
            Files.createDirectories(processingDirectory);
            Files.createDirectories(poisonDirectory);
        } catch (IOException ex) {
            throw new IllegalStateException("거래 구간 복구 디렉터리를 초기화할 수 없습니다.", ex);
        }
    }

    private TransactionSegmentRecoveryEnvelope read(Path path) throws IOException {
        return objectMapper.readValue(path.toFile(), TransactionSegmentRecoveryEnvelope.class);
    }

    private List<Path> listJsonFiles(Path directory) {
        try (Stream<Path> stream = Files.list(directory)) {
            return stream.filter(path -> Files.isRegularFile(path) && path.getFileName().toString().endsWith(".json"))
                    .sorted()
                    .toList();
        } catch (IOException ex) {
            return List.of();
        }
    }

    private void restoreClaimAfterReadFailure(Path processing) {
        try {
            moveAtomically(processing, pendingDirectory.resolve(processing.getFileName()), false);
        } catch (IOException restoreFailure) {
            enqueueFailureCount.incrementAndGet();
        }
    }

    private void moveMalformedToPoison(Path source) {
        try {
            moveAtomically(source, poisonDirectory.resolve(source.getFileName()), true);
            malformedPoisonCount.incrementAndGet();
        } catch (IOException moveFailure) {
            enqueueFailureCount.incrementAndGet();
        }
    }

    private Path requireDirectChild(Path directory, Path file) {
        Path normalized = file.toAbsolutePath().normalize();
        if (!normalized.getParent().equals(directory.toAbsolutePath().normalize())) {
            throw new IllegalArgumentException("거래 구간 복구 journal 경로가 허용된 디렉터리를 벗어났습니다.");
        }
        return normalized;
    }

    private Path pendingPath(String eventId) {
        return pendingDirectory.resolve(eventId + ".json");
    }

    private Path processingPath(String eventId) {
        return processingDirectory.resolve(eventId + ".json");
    }

    private Path poisonPath(String eventId) {
        return poisonDirectory.resolve(eventId + ".json");
    }

    private void writeAtomically(Path target, byte[] body, boolean replace) throws IOException {
        Files.createDirectories(target.getParent());
        Path temporary = target.resolveSibling(target.getFileName() + ".tmp-" + Thread.currentThread().threadId());
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
        if (value == null || value.isBlank() || value.contains("***")) {
            return value;
        }
        String normalized = value.trim();
        return normalized.length() <= 4
                ? "***"
                : normalized.substring(0, 2) + "***" + normalized.substring(normalized.length() - 2);
    }

    private String text(String value) {
        return value == null || value.isBlank() ? "N/A" : value.trim().toUpperCase(Locale.ROOT);
    }

    public record SegmentFallbackSnapshot(
            int pendingCount,
            int processingCount,
            int poisonCount,
            long spoolBytes,
            long maxSpoolBytes,
            long enqueueFailureCount,
            long staleReclaimedCount,
            long malformedPoisonCount,
            long poisonRetryCount,
            String spoolDirectory) {
        public String health() {
            if (enqueueFailureCount > 0 || spoolBytes >= maxSpoolBytes) {
                return "DOWN";
            }
            if (poisonCount > 0 || pendingCount > 0 || processingCount > 0) {
                return "DEGRADED";
            }
            return "UP";
        }
    }

    private record EligibleFile(Path path, TransactionSegmentRecoveryEnvelope envelope) {
    }
}
