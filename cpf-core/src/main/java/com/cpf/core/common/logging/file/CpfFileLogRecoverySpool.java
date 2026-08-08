package com.cpf.core.common.logging.file;

import com.cpf.core.common.logging.SensitiveDataMasker;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFilePermission;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/** Bounded durable recovery spool for CPF file logs. */
final class CpfFileLogRecoverySpool implements AutoCloseable {
    private static final String VERSION = "CPFLOGSPOOL2";
    private final Path root;
    private final Path quarantine;
    private final long maxEntries;
    private final AtomicLong sequence = new AtomicLong();
    private final AtomicLong enqueued = new AtomicLong();
    private final AtomicLong replayed = new AtomicLong();
    private final AtomicLong deduplicated = new AtomicLong();
    private final AtomicLong quarantined = new AtomicLong();
    private final AtomicLong terminalLoss = new AtomicLong();
    private final Clock clock;
    private final long baseBackoffMillis;
    private final RecoveryAppender appender;
    private final ScheduledExecutorService retryExecutor;
    private final AtomicBoolean replaying = new AtomicBoolean();

    CpfFileLogRecoverySpool(Environment environment, Clock clock, RecoveryAppender appender) {
        this.clock = clock;
        this.appender = appender;
        String configured = environment.getProperty("cpf.logging.file.recovery-spool-root");
        String envCode = environment.getProperty("cpf.environment", "local").trim().toLowerCase(Locale.ROOT);
        if ((configured == null || configured.isBlank()) && ("dev".equals(envCode) || "stg".equals(envCode) || "prod".equals(envCode))) {
            throw new IllegalStateException("dev/stg/prod에서는 cpf.logging.file.recovery-spool-root가 필수입니다.");
        }
        CpfLogPathPolicy logPathPolicy = new CpfLogPathPolicy(environment);
        Path localDurableFallback = logPathPolicy.logRoot().resolveSibling(".cpf-file-log-recovery")
                .resolve(logPathPolicy.runtimeModuleCode().toLowerCase(Locale.ROOT))
                .resolve(logPathPolicy.instanceId());
        this.root = Paths.get(configured == null || configured.isBlank()
                ? localDurableFallback.toString() : configured).toAbsolutePath().normalize();
        this.quarantine = root.resolve("quarantine");
        this.maxEntries = Math.max(32L, environment.getProperty(
                "cpf.logging.file.recovery-spool-max-entries", Long.class, 10_000L));
        this.baseBackoffMillis = Math.max(100L, environment.getProperty(
                "cpf.logging.file.recovery-spool-backoff-millis", Long.class, 1_000L));
        try {
            createSecureDirectory(root);
            createSecureDirectory(quarantine);
        } catch (IOException failure) {
            throw new IllegalStateException("CPF file-log recovery spool 초기화 실패", failure);
        }
        this.retryExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "cpf-file-log-recovery");
            t.setDaemon(true);
            return t;
        });
        this.retryExecutor.scheduleWithFixedDelay(this::safeReplay,
                baseBackoffMillis, baseBackoffMillis, TimeUnit.MILLISECONDS);
    }

    /** Compatibility constructor used only by focused spool tests. */
    CpfFileLogRecoverySpool(Environment environment, Clock clock) {
        this(environment, clock, (target, record, checksum) -> {
            throw new IOException("RecoveryAppender is required for product replay");
        });
    }

    boolean enqueue(Path target, String serializedMaskedRecord) {
        try {
            createSecureDirectory(root);
            if (pendingCount() >= maxEntries) {
                terminalLoss.incrementAndGet();
                return false;
            }
            String safe = redactSecrets(SensitiveDataMasker.mask(serializedMaskedRecord, 1024 * 1024));
            String normalizedTarget = target.toAbsolutePath().normalize().toString();
            String checksum = sha256(normalizedTarget + "\n" + safe);
            for (Path existing : pending()) {
                if (existing.getFileName().toString().contains(checksum.substring(0, 16))) {
                    deduplicated.incrementAndGet();
                    return true;
                }
            }
            long seq = Math.max(sequence.incrementAndGet(), clock.millis());
            String fileName = String.format("%020d-%s.spool", seq, checksum.substring(0, 16));
            Path temp = root.resolve(fileName + ".tmp");
            Path destination = root.resolve(fileName);
            String envelope = VERSION + "\n" + checksum + "\n" + normalizedTarget + "\n" + safe;
            Files.writeString(temp, envelope, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE, LinkOption.NOFOLLOW_LINKS);
            secureFile(temp);
            try {
                Files.move(temp, destination, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(temp, destination, StandardCopyOption.REPLACE_EXISTING);
            }
            enqueued.incrementAndGet();
            return true;
        } catch (Exception failure) {
            terminalLoss.incrementAndGet();
            return false;
        }
    }

    void replayAvailable() {
        if (!replaying.compareAndSet(false, true)) return;
        try {
            for (Path item : pending()) {
                try {
                    Instant modifiedAt = Files.getLastModifiedTime(item).toInstant();
                    if (modifiedAt.plusMillis(baseBackoffMillis).isAfter(clock.instant())) continue;
                    Envelope envelope = readEnvelope(item);
                    boolean applied = appender.append(envelope.target(), envelope.record(), envelope.checksum());
                    if (!applied) {
                        Files.setLastModifiedTime(item, FileTime.from(clock.instant()));
                        break;
                    }
                    Files.deleteIfExists(item);
                    replayed.incrementAndGet();
                } catch (DuplicateRecoveryRecord duplicate) {
                    Files.deleteIfExists(item);
                    deduplicated.incrementAndGet();
                    replayed.incrementAndGet();
                } catch (FileSystemException transientFailure) {
                    touch(item);
                    break;
                } catch (IOException transientFailure) {
                    touch(item);
                    break;
                } catch (Exception poison) {
                    quarantine(item, poison.getClass().getSimpleName());
                }
            }
        } finally {
            replaying.set(false);
        }
    }

    private Envelope readEnvelope(Path item) throws IOException {
        String envelope = Files.readString(item, StandardCharsets.UTF_8);
        String[] parts = envelope.split("\\n", 4);
        if (parts.length != 4 || !VERSION.equals(parts[0])) throw new IllegalArgumentException("bad-envelope");
        String checksum = parts[1];
        Path target = Paths.get(parts[2]).toAbsolutePath().normalize();
        String record = parts[3];
        if (!checksum.equals(sha256(target + "\n" + record))) throw new IllegalArgumentException("checksum");
        return new Envelope(target, annotateRecoveryChecksum(record, checksum), checksum);
    }

    private void safeReplay() {
        try { replayAvailable(); } catch (RuntimeException ignored) { }
    }

    Diagnostics diagnostics() {
        return new Diagnostics(pendingCount(), enqueued.get(), replayed.get(), deduplicated.get(),
                quarantined.get(), terminalLoss.get(), clock.instant());
    }

    private List<Path> pending() {
        try (var stream = Files.list(root)) {
            return stream.filter(p -> p.getFileName().toString().endsWith(".spool"))
                    .sorted(Comparator.comparing(Path::toString)).toList();
        } catch (Exception ignored) {
            return List.of();
        }
    }

    private long pendingCount() { return pending().size(); }

    private void quarantine(Path item, String reason) {
        try {
            createSecureDirectory(quarantine);
            Files.move(item, quarantine.resolve(item.getFileName().toString() + "." + safeReason(reason)),
                    StandardCopyOption.REPLACE_EXISTING);
            quarantined.incrementAndGet();
        } catch (Exception ignored) {
            terminalLoss.incrementAndGet();
        }
    }

    private void touch(Path item) {
        try { Files.setLastModifiedTime(item, FileTime.from(clock.instant())); } catch (Exception ignored) { }
    }

    private static String annotateRecoveryChecksum(String record, String checksum) {
        String trimmed = record == null ? "" : record.trim();
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            String body = trimmed.substring(0, trimmed.length() - 1);
            String separator = body.length() > 1 ? "," : "";
            return body + separator + "\"cpfRecoveryChecksum\":\"" + checksum + "\"}";
        }
        return "{\"message\":\"" + jsonEscape(trimmed) + "\",\"cpfRecoveryChecksum\":\"" + checksum + "\"}";
    }

    private static String jsonEscape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }

    private static void createSecureDirectory(Path path) throws IOException {
        Files.createDirectories(path);
        try {
            Files.setPosixFilePermissions(path, EnumSet.of(
                    PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE, PosixFilePermission.OWNER_EXECUTE));
        } catch (UnsupportedOperationException ignored) { }
    }

    private static void secureFile(Path path) throws IOException {
        try {
            Files.setPosixFilePermissions(path, EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE));
        } catch (UnsupportedOperationException ignored) { }
    }

    private static String redactSecrets(String value) {
        if (value == null || value.isBlank()) return "";
        return value.replaceAll(
                "(?i)(\\\"?(?:password|passwd|secret|token|apiKey|api_key|authorization)\\\"?\\s*[:=]\\s*\\\"?)([^\\\",}\\s]+)",
                "$1***");
    }

    private static String safeReason(String value) {
        String safe = value == null ? "unknown" : value.replaceAll("[^A-Za-z0-9_.-]", "_");
        return safe.length() > 80 ? safe.substring(0, 80) : safe;
    }

    private static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception impossible) { throw new IllegalStateException(impossible); }
    }

    @Override
    public void close() {
        retryExecutor.shutdown();
        replayAvailable();
    }

    @FunctionalInterface
    interface RecoveryAppender {
        boolean append(Path target, String record, String checksum) throws Exception;
    }

    static final class DuplicateRecoveryRecord extends Exception {
        DuplicateRecoveryRecord() { super("recovery record already applied"); }
    }

    private record Envelope(Path target, String record, String checksum) { }
    record Diagnostics(long pending, long enqueued, long replayed, long deduplicated,
                       long quarantined, long terminalLoss, Instant capturedAt) { }
}
