package com.cpf.integration.tcp;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermission;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.EnumSet;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * TCP 요청 전송 후 결과 미확정 상태를 보존하는 bounded store입니다.
 *
 * <p>Journal에는 원문 요청을 저장하지 않고 SHA-256만 남깁니다. 변경은 별도 lock file로
 * 프로세스 간 직렬화하고 append 후 force하여 Process Kill 직전 상태를 복구합니다.</p>
 */
public final class CpfTcpUnknownResultStore {
    private static final String VERSION = "V1";
    private static final Base64.Encoder B64_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder B64_DECODER = Base64.getUrlDecoder();

    private final Map<String, StoredUnknown> values = new LinkedHashMap<>();
    private final List<ReconciliationAudit> audits = new ArrayList<>();
    private final Map<String, Long> lastVersions = new LinkedHashMap<>();
    private final int limit;
    private final Path journalPath;
    private final Path lockPath;

    public CpfTcpUnknownResultStore(int limit) {
        this(limit, null);
    }

    public CpfTcpUnknownResultStore(int limit, Path journalPath) {
        if (limit < 1) {
            throw new IllegalArgumentException("limit must be positive");
        }
        this.limit = limit;
        this.journalPath = journalPath == null ? null : journalPath.toAbsolutePath().normalize();
        this.lockPath = this.journalPath == null
                ? null
                : this.journalPath.resolveSibling(this.journalPath.getFileName() + ".lock");
        if (this.journalPath != null) {
            initializeJournal();
            withFileLock(() -> {
                recoverTruncatedTail();
                reload();
                return null;
            });
        }
    }

    public void record(CpfTcpUnknownResult value) {
        CpfTcpUnknownResult required = Objects.requireNonNull(value, "value must not be null");
        String requestHash = sha256(required.request());
        mutate(() -> {
            StoredUnknown existing = values.get(required.correlationId());
            if (existing != null) {
                if (!existing.requestHash().equals(requestHash)
                        || !existing.value().writtenAt().equals(required.writtenAt())
                        || !existing.originalDetail().equals(mask(required.detail()))) {
                    // 같은 correlationId로 내용이 다른 재기록은 프로세스 간 correlationId 충돌
                    // 가능성이 있어 항상 fail-closed로 거부한다. 정상적인 update 경로는
                    // reconcile()로 기존 항목을 명시적으로 제거한 뒤 다시 record()하는 것이다
                    // (reconciledCorrelationCannotBeRemovedByStaleVersionAfterRerecord 참조).
                    throw new IllegalStateException("UNKNOWN_RESULT correlation conflict: " + required.correlationId());
                }
                return null;
            }
            if (values.size() >= limit) {
                throw new IllegalStateException("UNKNOWN_RESULT store limit reached");
            }
            String detail = mask(required.detail());
            long version = Math.addExact(lastVersions.getOrDefault(required.correlationId(), -1L), 1L);
            String line = recordLine(required.correlationId(), required.writtenAt(), requestHash, detail, version);
            append(line);
            values.put(required.correlationId(), new StoredUnknown(copy(required, detail), requestHash, detail, version));
            lastVersions.put(required.correlationId(), version);
            return null;
        });
    }

    public Optional<CpfTcpUnknownResult> find(String correlationId) {
        synchronized (values) {
            StoredUnknown stored = values.get(normalizeCorrelation(correlationId));
            return stored == null ? Optional.empty() : Optional.of(stored.value());
        }
    }

    public Optional<VersionedUnknownResult> findVersioned(String correlationId) {
        synchronized (values) {
            StoredUnknown stored = values.get(normalizeCorrelation(correlationId));
            return stored == null
                    ? Optional.empty()
                    : Optional.of(new VersionedUnknownResult(stored.value(), stored.requestHash(), stored.version()));
        }
    }

    /** 기존 단일 JVM 호출 호환용이며 내부적으로 현재 version을 읽어 CAS를 적용합니다. */
    public boolean reconcile(String correlationId) {
        Optional<VersionedUnknownResult> current = findVersioned(correlationId);
        if (current.isEmpty()) {
            return false;
        }
        return reconcile(correlationId, current.get().version(), "CPF_LEGACY", "legacy reconcile");
    }

    public boolean reconcile(
            String correlationId,
            long expectedVersion,
            String operator,
            String reason) {
        String normalizedCorrelation = normalizeCorrelation(correlationId);
        String normalizedOperator = requireText(operator, "operator");
        String maskedReason = mask(requireText(reason, "reason"));
        return mutate(() -> {
            StoredUnknown existing = values.get(normalizedCorrelation);
            Instant now = Instant.now();
            if (existing == null) {
                append(auditLine(normalizedCorrelation, now, normalizedOperator, maskedReason,
                        expectedVersion, expectedVersion, false));
                audits.add(new ReconciliationAudit(
                        normalizedCorrelation, normalizedOperator, maskedReason, false, expectedVersion, now));
                return false;
            }
            if (existing.version() != expectedVersion) {
                throw new ConcurrentModificationException(
                        "UNKNOWN_RESULT version changed: expected=" + expectedVersion
                                + ", actual=" + existing.version());
            }
            long nextVersion = Math.addExact(expectedVersion, 1L);
            append(auditLine(normalizedCorrelation, now, normalizedOperator, maskedReason,
                    expectedVersion, nextVersion, true));
            values.remove(normalizedCorrelation);
            lastVersions.put(normalizedCorrelation, nextVersion);
            audits.add(new ReconciliationAudit(
                    normalizedCorrelation, normalizedOperator, maskedReason, true, nextVersion, now));
            return true;
        });
    }

    /** 다른 프로세스가 기록한 Journal 이벤트를 현재 인스턴스에 반영합니다. */
    public void refresh() {
        if (journalPath == null) {
            return;
        }
        withFileLock(() -> {
            recoverTruncatedTail();
            reload();
            return null;
        });
    }

    public List<CpfTcpUnknownResult> snapshot() {
        synchronized (values) {
            return values.values().stream()
                    .map(StoredUnknown::value)
                    .sorted(Comparator.comparing(CpfTcpUnknownResult::writtenAt))
                    .toList();
        }
    }

    public List<ReconciliationAudit> auditSnapshot() {
        synchronized (values) {
            return List.copyOf(audits);
        }
    }

    public boolean durable() {
        return journalPath != null;
    }

    public Optional<Path> journalPath() {
        return Optional.ofNullable(journalPath);
    }

    private <T> T mutate(IoSupplier<T> action) {
        if (journalPath == null) {
            synchronized (values) {
                try {
                    return action.get();
                } catch (IOException ex) {
                    throw new IllegalStateException("UNKNOWN_RESULT mutation failed", ex);
                }
            }
        }
        return withFileLock(() -> {
            recoverTruncatedTail();
            reload();
            return action.get();
        });
    }

    private void initializeJournal() {
        try {
            Path parent = journalPath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            if (lockPath.getParent() != null) {
                Files.createDirectories(lockPath.getParent());
            }
            if (!Files.exists(journalPath)) {
                Files.createFile(journalPath);
            }
            if (!Files.exists(lockPath)) {
                Files.createFile(lockPath);
            }
            restrictToOwner(journalPath);
            restrictToOwner(lockPath);
        } catch (IOException ex) {
            throw new IllegalStateException("UNKNOWN_RESULT journal initialization failed: " + journalPath, ex);
        }
    }

    private <T> T withFileLock(IoSupplier<T> action) {
        try (FileChannel lockChannel = FileChannel.open(
                lockPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
             FileLock fileLock = lockChannel.lock()) {
            if (!fileLock.isValid()) {
                throw new IllegalStateException("UNKNOWN_RESULT journal lock is not valid: " + lockPath);
            }
            synchronized (values) {
                return action.get();
            }
        } catch (IOException ex) {
            throw new IllegalStateException("UNKNOWN_RESULT journal lock failed: " + lockPath, ex);
        }
    }

    private void reload() throws IOException {
        values.clear();
        audits.clear();
        lastVersions.clear();
        if (!Files.exists(journalPath) || Files.size(journalPath) == 0L) {
            return;
        }
        String content = Files.readString(journalPath, StandardCharsets.UTF_8);
        String[] lines = content.split("\\n", -1);
        int completeLines = Math.max(0, lines.length - 1);
        for (int i = 0; i < completeLines; i++) {
            String line = trimCr(lines[i]);
            if (line.isBlank()) {
                continue;
            }
            applyLine(line, i + 1);
        }
        if (content.endsWith("\n") && lines.length > 0) {
            String lastComplete = trimCr(lines[lines.length - 2]);
            if (!lastComplete.isBlank() && completeLines == 0) {
                applyLine(lastComplete, 1);
            }
        }
        if (values.size() > limit) {
            throw new IllegalStateException("UNKNOWN_RESULT journal exceeds configured limit");
        }
    }

    private void recoverTruncatedTail() throws IOException {
        if (!Files.exists(journalPath)) {
            return;
        }
        byte[] bytes = Files.readAllBytes(journalPath);
        if (bytes.length == 0 || bytes[bytes.length - 1] == '\n') {
            return;
        }
        int lastNewline = -1;
        for (int i = bytes.length - 1; i >= 0; i--) {
            if (bytes[i] == '\n') {
                lastNewline = i;
                break;
            }
        }
        try (FileChannel channel = FileChannel.open(journalPath, StandardOpenOption.WRITE)) {
            channel.truncate(lastNewline < 0 ? 0L : lastNewline + 1L);
            channel.force(true);
        }
    }

    private void applyLine(String line, int lineNo) {
        String[] fields = line.split("\\|", -1);
        if (fields.length < 3 || !VERSION.equals(fields[0])) {
            throw corrupt(lineNo, "unsupported version/event");
        }
        String expectedChecksum = fields[fields.length - 1];
        String body = line.substring(0, line.lastIndexOf('|'));
        if (!sameHash(expectedChecksum, sha256(body.getBytes(StandardCharsets.UTF_8)))) {
            throw corrupt(lineNo, "checksum mismatch");
        }
        try {
            switch (fields[1]) {
                case "R" -> applyRecord(fields);
                case "A" -> applyAudit(fields);
                default -> throw corrupt(lineNo, "unknown event type");
            }
        } catch (RuntimeException ex) {
            if (ex instanceof IllegalStateException state && state.getMessage() != null
                    && state.getMessage().startsWith("UNKNOWN_RESULT journal corruption")) {
                throw state;
            }
            throw corrupt(lineNo, ex.getClass().getSimpleName());
        }
    }

    private void applyRecord(String[] fields) {
        if (fields.length != 8) {
            throw new IllegalArgumentException("record field count");
        }
        String correlation = decode(fields[2]);
        Instant writtenAt = Instant.ofEpochMilli(Long.parseLong(fields[3]));
        String requestHash = fields[4];
        String detail = decode(fields[5]);
        long version = Long.parseLong(fields[6]);
        StoredUnknown existing = values.get(correlation);
        if (existing != null && (!existing.requestHash().equals(requestHash)
                || !existing.value().writtenAt().equals(writtenAt))) {
            throw new IllegalStateException("duplicate correlation conflict");
        }
        CpfTcpUnknownResult value = new CpfTcpUnknownResult(
                correlation, writtenAt, new byte[0], detail + " [requestSha256=" + requestHash + ']');
        values.put(correlation, new StoredUnknown(value, requestHash, detail, version));
        lastVersions.merge(correlation, version, Math::max);
    }

    private void applyAudit(String[] fields) {
        if (fields.length != 10) {
            throw new IllegalArgumentException("audit field count");
        }
        String correlation = decode(fields[2]);
        Instant at = Instant.ofEpochMilli(Long.parseLong(fields[3]));
        String operator = decode(fields[4]);
        String reason = decode(fields[5]);
        long expectedVersion = Long.parseLong(fields[6]);
        long resultingVersion = Long.parseLong(fields[7]);
        boolean reconciled = "1".equals(fields[8]);
        if (reconciled) {
            StoredUnknown existing = values.get(correlation);
            if (existing == null || existing.version() != expectedVersion) {
                throw new IllegalStateException("audit version conflict");
            }
            values.remove(correlation);
            lastVersions.merge(correlation, resultingVersion, Math::max);
        } else {
            lastVersions.merge(correlation, resultingVersion, Math::max);
        }
        audits.add(new ReconciliationAudit(
                correlation, operator, reason, reconciled, resultingVersion, at));
    }

    private String recordLine(
            String correlation,
            Instant writtenAt,
            String requestHash,
            String detail,
            long version) {
        String body = String.join("|", VERSION, "R", encode(correlation),
                Long.toString(writtenAt.toEpochMilli()), requestHash, encode(detail), Long.toString(version));
        return body + '|' + sha256(body.getBytes(StandardCharsets.UTF_8));
    }

    private String auditLine(
            String correlation,
            Instant at,
            String operator,
            String reason,
            long expectedVersion,
            long resultingVersion,
            boolean reconciled) {
        String body = String.join("|", VERSION, "A", encode(correlation),
                Long.toString(at.toEpochMilli()), encode(operator), encode(reason),
                Long.toString(expectedVersion), Long.toString(resultingVersion), reconciled ? "1" : "0");
        return body + '|' + sha256(body.getBytes(StandardCharsets.UTF_8));
    }

    private void append(String line) throws IOException {
        if (journalPath == null) {
            return;
        }
        byte[] bytes = (line + '\n').getBytes(StandardCharsets.UTF_8);
        try (FileChannel channel = FileChannel.open(
                journalPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND)) {
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            while (buffer.hasRemaining()) {
                channel.write(buffer);
            }
            channel.force(true);
        }
    }

    private static CpfTcpUnknownResult copy(CpfTcpUnknownResult value, String detail) {
        return new CpfTcpUnknownResult(
                value.correlationId(), value.writtenAt(), value.request(), detail);
    }

    private static String normalizeCorrelation(String value) {
        return requireText(value, "correlationId");
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value.trim();
    }

    private static String mask(String value) {
        String normalized = value == null || value.isBlank() ? "UNKNOWN" : value.trim();
        return normalized
                .replaceAll("(?i)(password|passwd|secret|token|authorization)\\s*[:=]\\s*[^,;\\s]+", "$1=***")
                .replaceAll("(?i)bearer\\s+[A-Za-z0-9._~+/-]+=*", "Bearer ***");
    }

    private static String encode(String value) {
        return B64_ENCODER.encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private static String decode(String value) {
        return new String(B64_DECODER.decode(value), StandardCharsets.UTF_8);
    }

    private static String sha256(byte[] value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value));
        } catch (Exception ex) {
            throw new IllegalStateException("SHA-256 unavailable", ex);
        }
    }

    private static boolean sameHash(String left, String right) {
        try {
            return MessageDigest.isEqual(HexFormat.of().parseHex(left), HexFormat.of().parseHex(right));
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private static String trimCr(String value) {
        return value.endsWith("\r") ? value.substring(0, value.length() - 1) : value;
    }

    private static IllegalStateException corrupt(int lineNo, String reason) {
        return new IllegalStateException(
                "UNKNOWN_RESULT journal corruption at line " + lineNo + ": " + reason);
    }

    private static void restrictToOwner(Path path) {
        try {
            Files.setPosixFilePermissions(path, EnumSet.of(
                    PosixFilePermission.OWNER_READ,
                    PosixFilePermission.OWNER_WRITE));
        } catch (UnsupportedOperationException | IOException ignored) {
            // Windows/비-POSIX 파일시스템에서는 OS ACL 정책을 사용합니다.
        }
    }

    private record StoredUnknown(
            CpfTcpUnknownResult value,
            String requestHash,
            String originalDetail,
            long version) {
    }

    public record VersionedUnknownResult(
            CpfTcpUnknownResult value,
            String requestHash,
            long version) {
    }

    public record ReconciliationAudit(
            String correlationId,
            String operator,
            String reason,
            boolean reconciled,
            long resultingVersion,
            Instant at) {
    }

    @FunctionalInterface
    private interface IoSupplier<T> {
        T get() throws IOException;
    }
}
