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
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Bounded durable recovery spool for CPF file logs.
 *
 * <p>The spool is deliberately independent from the active file-log root so a disk/read-only
 * failure on that root cannot recursively call the same writer. Records are masked before they
 * are persisted, checksum protected, replayed idempotently, and malformed/poison records are
 * quarantined. No raw request payload is accepted by this component.</p>
 */
final class CpfFileLogRecoverySpool {
    private static final String VERSION = "CPFLOGSPOOL1";
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

    CpfFileLogRecoverySpool(Environment environment, Clock clock) {
        this.clock = clock;
        String configured = environment.getProperty("cpf.logging.file.recovery-spool-root");
        this.root = Paths.get(configured == null || configured.isBlank()
                ? System.getProperty("java.io.tmpdir", ".") + "/cpf-log-recovery-spool"
                : configured).toAbsolutePath().normalize();
        this.quarantine = root.resolve("quarantine");
        this.maxEntries = Math.max(32L, environment.getProperty(
                "cpf.logging.file.recovery-spool-max-entries", Long.class, 10_000L));
        this.baseBackoffMillis = Math.max(100L, environment.getProperty(
                "cpf.logging.file.recovery-spool-backoff-millis", Long.class, 1_000L));
        try {
            createSecureDirectory(root);
            createSecureDirectory(quarantine);
        } catch (IOException failure) {
            terminalLoss.incrementAndGet();
        }
    }

    boolean enqueue(Path target, String serializedMaskedRecord) {
        try {
            createSecureDirectory(root);
            if (pendingCount() >= maxEntries) {
                terminalLoss.incrementAndGet();
                return false;
            }
            String safe = redactSecrets(SensitiveDataMasker.mask(serializedMaskedRecord, 1024 * 1024));
            String checksum = sha256(target.toString() + "\n" + safe);
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
            String envelope = VERSION + "\n" + checksum + "\n" + target.toAbsolutePath().normalize() + "\n" + safe;
            Files.writeString(temp, envelope, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
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
        for (Path item : pending()) {
            try {
                Instant modifiedAt = Files.getLastModifiedTime(item).toInstant();
                if (modifiedAt.plusMillis(baseBackoffMillis).isAfter(clock.instant())) {
                    continue;
                }
                String envelope = Files.readString(item, StandardCharsets.UTF_8);
                String[] parts = envelope.split("\\n", 4);
                if (parts.length != 4 || !VERSION.equals(parts[0])) {
                    quarantine(item, "bad-envelope");
                    continue;
                }
                String checksum = parts[1];
                Path target = Paths.get(parts[2]).toAbsolutePath().normalize();
                String record = parts[3];
                if (!checksum.equals(sha256(target.toString() + "\n" + record))) {
                    quarantine(item, "checksum");
                    continue;
                }
                Files.createDirectories(target.getParent());
                String recoveredRecord = annotateRecoveryChecksum(record, checksum);
                String marker = "\"cpfRecoveryChecksum\":\"" + checksum + "\"";
                boolean alreadyApplied = false;
                if (Files.exists(target) && Files.size(target) <= 8L * 1024 * 1024) {
                    alreadyApplied = Files.readString(target, StandardCharsets.UTF_8).contains(marker);
                }
                if (!alreadyApplied) {
                    Files.writeString(target, recoveredRecord + System.lineSeparator(),
                            StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                } else {
                    deduplicated.incrementAndGet();
                }
                Files.deleteIfExists(item);
                replayed.incrementAndGet();
            } catch (FileSystemException transientFailure) {
                // Durable retry with backoff: keep the record and defer the next replay attempt.
                try {
                    Files.setLastModifiedTime(item, FileTime.from(clock.instant()));
                } catch (Exception ignored) {
                    // The original failure remains authoritative.
                }
                break;
            } catch (Exception poison) {
                quarantine(item, poison.getClass().getSimpleName());
            }
        }
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
            Files.move(item, quarantine.resolve(item.getFileName().toString() + "." + reason),
                    StandardCopyOption.REPLACE_EXISTING);
            quarantined.incrementAndGet();
        } catch (Exception ignored) {
            terminalLoss.incrementAndGet();
        }
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
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    private static void createSecureDirectory(Path path) throws IOException {
        Files.createDirectories(path);
        try {
            Files.setPosixFilePermissions(path, EnumSet.of(
                    PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE, PosixFilePermission.OWNER_EXECUTE));
        } catch (UnsupportedOperationException ignored) {
            // Windows ACL hardening is inherited from the configured spool root.
        }
    }

    private static void secureFile(Path path) throws IOException {
        try {
            Files.setPosixFilePermissions(path, EnumSet.of(
                    PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE));
        } catch (UnsupportedOperationException ignored) {
            // Windows ACL hardening is inherited from the configured spool root.
        }
    }

    private static String redactSecrets(String value) {
        if (value == null || value.isBlank()) return "";
        return value.replaceAll(
                "(?i)(\\\"?(?:password|passwd|secret|token|apiKey|api_key|authorization)\\\"?\\s*[:=]\\s*\\\"?)([^\\\",}\\s]+)",
                "$1***");
    }

    private static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception impossible) {
            throw new IllegalStateException(impossible);
        }
    }

    record Diagnostics(long pending, long enqueued, long replayed, long deduplicated,
                       long quarantined, long terminalLoss, Instant capturedAt) { }
}
