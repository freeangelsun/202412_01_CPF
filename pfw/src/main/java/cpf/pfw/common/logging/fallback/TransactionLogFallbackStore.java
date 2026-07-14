package cpf.pfw.common.logging.fallback;

import com.fasterxml.jackson.databind.ObjectMapper;
import cpf.pfw.common.logging.SensitiveDataMasker;
import cpf.pfw.common.logging.TransactionLogRecord;
import cpf.pfw.common.logging.file.CpfFileLogWriter;
import cpf.pfw.common.logging.policy.LogPolicyDecision;
import org.springframework.core.env.Environment;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.time.Instant;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * PFW DB 거래 로그 저장 실패를 인스턴스별 durable journal에 보존합니다.
 *
 * <p>pending, processing, poison 디렉터리 간 원자적 이동으로 상태를 관리하고,
 * 민감정보를 마스킹한 JSON만 디스크에 기록합니다. 동일 이벤트는 전역 거래 ID,
 * segment ID, 이벤트 유형, 순번으로 만든 복구 ID를 사용해 한 번만 생성합니다.</p>
 */
@Component
public class TransactionLogFallbackStore {
    private static final Pattern SENSITIVE_KEY = Pattern.compile(
            "(?i).*(password|passwd|pwd|token|authorization|secret|credential|resident|account|card|pin|otp).*");
    private static final long DEFAULT_MAX_BYTES = 256L * 1024L * 1024L;

    private final ObjectMapper objectMapper;
    private final Clock clock;
    private final Path pendingDirectory;
    private final Path processingDirectory;
    private final Path poisonDirectory;
    private final String spoolRelativeDirectory;
    private final long maxSpoolBytes;
    private final AtomicLong enqueueFailureCount = new AtomicLong();

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
        this.spoolRelativeDirectory = fileLogWriter.relativeToLogRoot(root)
                .toString()
                .replace('\\', '/');
        this.maxSpoolBytes = environment.getProperty(
                "cpf.logging.db-fallback.max-spool-bytes",
                Long.class,
                DEFAULT_MAX_BYTES);
        initializeDirectories();
        restoreInterruptedClaims();
    }

    /**
     * 실패 이벤트를 중복 없이 journal에 추가합니다.
     *
     * @return 새 파일을 만들었으면 {@code true}, 이미 같은 이벤트가 있으면 {@code false}
     */
    public synchronized boolean enqueue(
            TransactionLogRecord sourceRecord,
            Map<String, String> sourceDetails,
            LogPolicyDecision logPolicy,
            Throwable failure) {
        if (sourceRecord == null) {
            enqueueFailureCount.incrementAndGet();
            throw new IllegalArgumentException("복구할 거래 로그 레코드는 필수입니다.");
        }
        TransactionLogRecord record = sanitizedCopy(sourceRecord);
        String recoveryEventId = recoveryEventId(record);
        record.setRecoveryEventId(recoveryEventId);
        Map<String, String> details = sanitizeDetails(sourceDetails);
        Instant now = clock.instant();
        TransactionLogFallbackEnvelope envelope = new TransactionLogFallbackEnvelope(
                recoveryEventId,
                0,
                now,
                now,
                failure == null ? "UNKNOWN" : failure.getClass().getSimpleName(),
                record,
                details,
                logPolicy);
        Path target = pendingPath(recoveryEventId);
        if (Files.exists(target) || Files.exists(processingPath(recoveryEventId)) || Files.exists(poisonPath(recoveryEventId))) {
            return false;
        }
        try {
            byte[] body = objectMapper.writeValueAsBytes(envelope);
            ensureCapacity(body.length);
            writeAtomically(target, body, false);
            return true;
        } catch (IOException | RuntimeException ex) {
            enqueueFailureCount.incrementAndGet();
            throw new IllegalStateException("DB 거래 로그 복구 journal 저장에 실패했습니다.", ex);
        }
    }

    public synchronized List<Path> pendingFiles() {
        return listJsonFiles(pendingDirectory);
    }

    public synchronized TransactionLogFallbackEnvelope claim(Path pendingFile) throws IOException {
        Path source = requireDirectChild(pendingDirectory, pendingFile);
        Path processing = processingDirectory.resolve(source.getFileName());
        moveAtomically(source, processing, false);
        return objectMapper.readValue(processing.toFile(), TransactionLogFallbackEnvelope.class);
    }

    public synchronized void complete(String recoveryEventId) throws IOException {
        Files.deleteIfExists(processingPath(recoveryEventId));
    }

    public synchronized void retry(TransactionLogFallbackEnvelope envelope) throws IOException {
        Path processing = processingPath(envelope.recoveryEventId());
        writeAtomically(pendingPath(envelope.recoveryEventId()), objectMapper.writeValueAsBytes(envelope), true);
        Files.deleteIfExists(processing);
    }

    public synchronized void poison(TransactionLogFallbackEnvelope envelope) throws IOException {
        Path processing = processingPath(envelope.recoveryEventId());
        Path target = poisonPath(envelope.recoveryEventId());
        writeAtomically(target, objectMapper.writeValueAsBytes(envelope), true);
        Files.deleteIfExists(processing);
    }

    public synchronized FallbackSnapshot snapshot() {
        return new FallbackSnapshot(
                pendingFiles().size(),
                listJsonFiles(processingDirectory).size(),
                listJsonFiles(poisonDirectory).size(),
                spoolSizeBytes(),
                maxSpoolBytes,
                enqueueFailureCount.get(),
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
        record.setParameters(SensitiveDataMasker.mask(record.getParameters()));
        record.setRequestBody(SensitiveDataMasker.mask(record.getRequestBody()));
        record.setResponse(SensitiveDataMasker.mask(record.getResponse()));
        record.setMessageContent(SensitiveDataMasker.mask(record.getMessageContent(), 1000));
        record.setErrorMessage(SensitiveDataMasker.mask(record.getErrorMessage()));
        record.setExternalMessage(SensitiveDataMasker.mask(record.getExternalMessage(), 1000));
        record.setInternalMessage(SensitiveDataMasker.mask(record.getInternalMessage()));
        record.setUserAgent(SensitiveDataMasker.mask(record.getUserAgent(), 500));
        return record;
    }

    private Map<String, String> sanitizeDetails(Map<String, String> source) {
        Map<String, String> result = new LinkedHashMap<>();
        if (source == null) {
            return result;
        }
        source.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.nullsFirst(String::compareTo)))
                .forEach(entry -> {
                    String key = entry.getKey() == null ? "N/A" : SensitiveDataMasker.truncate(entry.getKey(), 100);
                    String value = SENSITIVE_KEY.matcher(key).matches()
                            ? "***"
                            : SensitiveDataMasker.mask(entry.getValue());
                    result.put(key, value);
                });
        return result;
    }

    private String recoveryEventId(TransactionLogRecord record) {
        String source = text(record.getTransactionId(), "NO_GLOBAL_ID") + '|'
                + text(record.getSpanId(), "ROOT") + '|'
                + text(record.getLogType(), "TRANSACTION_FINAL") + '|'
                + (record.getSequenceNo() == null ? 1 : record.getSequenceNo());
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(source.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("복구 이벤트 중복 방지 ID를 만들 수 없습니다.", ex);
        }
    }

    private void ensureCapacity(long incomingBytes) {
        if (incomingBytes < 0 || spoolSizeBytes() + incomingBytes > maxSpoolBytes) {
            throw new IllegalStateException("DB 거래 로그 복구 journal 용량 제한을 초과했습니다.");
        }
    }

    private long spoolSizeBytes() {
        try (Stream<Path> stream = Files.walk(pendingDirectory.getParent())) {
            return stream.filter(Files::isRegularFile)
                    .mapToLong(path -> {
                        try {
                            return Files.size(path);
                        } catch (IOException ex) {
                            return 0L;
                        }
                    })
                    .sum();
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
            throw new IllegalStateException("DB 거래 로그 복구 디렉터리를 초기화할 수 없습니다.", ex);
        }
    }

    private void restoreInterruptedClaims() {
        for (Path processing : listJsonFiles(processingDirectory)) {
            Path pending = pendingDirectory.resolve(processing.getFileName());
            try {
                if (Files.exists(pending)) {
                    Files.deleteIfExists(processing);
                } else {
                    moveAtomically(processing, pending, false);
                }
            } catch (IOException ex) {
                enqueueFailureCount.incrementAndGet();
            }
        }
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

    private Path requireDirectChild(Path directory, Path file) {
        Path normalized = file.toAbsolutePath().normalize();
        if (!normalized.getParent().equals(directory.toAbsolutePath().normalize())) {
            throw new IllegalArgumentException("복구 journal 경로가 허용된 디렉터리를 벗어났습니다.");
        }
        return normalized;
    }

    private Path pendingPath(String id) {
        return pendingDirectory.resolve(id + ".json");
    }

    private Path processingPath(String id) {
        return processingDirectory.resolve(id + ".json");
    }

    private Path poisonPath(String id) {
        return poisonDirectory.resolve(id + ".json");
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

    private String text(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim().toUpperCase(Locale.ROOT);
    }

    public record FallbackSnapshot(
            int pendingCount,
            int processingCount,
            int poisonCount,
            long spoolBytes,
            long maxSpoolBytes,
            long enqueueFailureCount,
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
}
