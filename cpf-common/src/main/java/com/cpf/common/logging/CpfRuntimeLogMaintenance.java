package com.cpf.common.logging;

import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.CREATE_NEW;
import static java.nio.file.StandardOpenOption.WRITE;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

/**
 * Logback이 일별로 전환한 일반 Runtime 로그를 5일 이후 압축하고 365일 이후 삭제합니다.
 * 현재 active 파일과 Transaction Evidence 파일은 대상에 포함하지 않습니다.
 */
public final class CpfRuntimeLogMaintenance {
    private final Clock clock;
    private final CpfApplicationLoggingPolicyValidator validator;

    public CpfRuntimeLogMaintenance(Clock clock) {
        this(clock, new CpfApplicationLoggingPolicyValidator());
    }

    CpfRuntimeLogMaintenance(Clock clock, CpfApplicationLoggingPolicyValidator validator) {
        this.clock = Objects.requireNonNull(clock, "clock");
        this.validator = Objects.requireNonNull(validator, "validator");
    }

    public MaintenanceResult maintain(CpfApplicationLoggingPolicy unvalidated) throws IOException {
        CpfApplicationLoggingPolicy policy = validator.validate(unvalidated);
        Path configuredRoot = policy.root().toAbsolutePath().normalize();
        Files.createDirectories(configuredRoot);
        Path root = configuredRoot.toRealPath();
        Path directory = CpfRuntimeLogPathPolicy.resolveDirectory(
                root, policy.applicationName(), policy.instanceId());
        Files.createDirectories(directory);
        Path actualDirectory = directory.toRealPath();
        requireInside(root, actualDirectory, "Application log directory");
        Path archive = actualDirectory.resolve("archive");
        Files.createDirectories(archive);
        Path actualArchive = archive.toRealPath();
        requireInside(root, actualArchive, "Archive directory");

        Path lockPath = actualDirectory.resolve(".cpf-runtime-log-maintenance.lock");
        try (FileChannel channel = FileChannel.open(lockPath, CREATE, WRITE);
             FileLock lock = tryLock(channel)) {
            if (lock == null) return new MaintenanceResult(0, 0, 0, true, List.of());
            return maintainLocked(policy, root, actualArchive);
        }
    }

    private MaintenanceResult maintainLocked(
            CpfApplicationLoggingPolicy policy, Path root, Path archive) throws IOException {
        int scanned = 0;
        int compressed = 0;
        int deleted = 0;
        List<String> failures = new ArrayList<>();
        Instant now = clock.instant();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(archive)) {
            for (Path candidate : stream) {
                if (!Files.isRegularFile(candidate, LinkOption.NOFOLLOW_LINKS)) continue;
                if (Files.isSymbolicLink(candidate)) {
                    failures.add(candidate.getFileName() + ": symbolic link is not allowed");
                    continue;
                }
                CpfLogFilePolicy filePolicy = matchingPolicy(policy, candidate.getFileName().toString());
                if (filePolicy == null) continue;
                scanned++;
                try {
                    Path safe = candidate.toRealPath(LinkOption.NOFOLLOW_LINKS);
                    requireInside(root, safe, "Archived log");
                    Instant modified = Files.getLastModifiedTime(
                            candidate, LinkOption.NOFOLLOW_LINKS).toInstant();
                    if (isPastBoundary(modified, filePolicy.deleteAfterDays(), now)) {
                        Files.delete(candidate);
                        deleted++;
                    } else if (!candidate.getFileName().toString().endsWith(".gz")
                            && isPastBoundary(modified, filePolicy.compressAfterDays(), now)) {
                        compress(candidate);
                        compressed++;
                    }
                } catch (IOException | RuntimeException failure) {
                    failures.add(candidate.getFileName() + ": " + failure.getMessage());
                }
            }
        }
        return new MaintenanceResult(scanned, compressed, deleted, false, List.copyOf(failures));
    }

    private static CpfLogFilePolicy matchingPolicy(CpfApplicationLoggingPolicy policy, String name) {
        for (CpfLogFilePolicy file : policy.files().values()) {
            if (!file.enabled()) continue;
            String base = file.fileName().substring(0, file.fileName().length() - ".log".length());
            Pattern pattern = Pattern.compile(Pattern.quote(base)
                    + "\\.\\d{4}-\\d{2}-\\d{2}\\.log(?:\\.gz)?");
            if (pattern.matcher(name).matches()) return file;
        }
        return null;
    }

    private static boolean isPastBoundary(Instant modified, int days, Instant now) {
        return modified.plus(days, ChronoUnit.DAYS).isBefore(now);
    }

    private static void compress(Path source) throws IOException {
        Path target = source.resolveSibling(source.getFileName() + ".gz");
        if (Files.exists(target, LinkOption.NOFOLLOW_LINKS)) {
            throw new IOException("compression target already exists: " + target.getFileName());
        }
        Path temporary = source.resolveSibling(source.getFileName() + ".tmp-" + UUID.randomUUID());
        try {
            try (OutputStream output = new GZIPOutputStream(Files.newOutputStream(temporary, CREATE_NEW, WRITE))) {
                Files.copy(source, output);
            }
            try {
                Files.move(temporary, target, ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException unsupported) {
                Files.move(temporary, target, REPLACE_EXISTING);
            }
            Files.delete(source);
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private static FileLock tryLock(FileChannel channel) throws IOException {
        try {
            return channel.tryLock();
        } catch (OverlappingFileLockException locked) {
            return null;
        }
    }

    private static void requireInside(Path root, Path candidate, String label) {
        if (!candidate.normalize().startsWith(root.normalize())) {
            throw new IllegalArgumentException(label + "가 cpf.logging.root 밖을 가리킵니다: " + candidate);
        }
    }

    /** Result of one bounded maintenance pass, including lock skips and per-file failures. */
    public record MaintenanceResult(
            int scannedFiles,
            int compressedFiles,
            int deletedFiles,
            boolean skippedBecauseLocked,
            List<String> failures) {
        public boolean successful() { return failures.isEmpty(); }
    }
}
