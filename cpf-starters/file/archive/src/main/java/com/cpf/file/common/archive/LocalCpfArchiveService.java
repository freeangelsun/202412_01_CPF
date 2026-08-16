package com.cpf.file.common.archive;

import com.cpf.file.archive.api.CpfArchiveChecksum;
import com.cpf.file.archive.api.CpfArchiveEntry;
import com.cpf.file.archive.api.CpfArchiveFormat;
import com.cpf.file.archive.api.CpfArchivePolicy;
import com.cpf.file.archive.api.CpfArchiveRequest;
import com.cpf.file.archive.api.CpfArchiveResult;
import com.cpf.file.archive.api.CpfArchiveService;
import com.cpf.file.archive.api.CpfExtractedArchiveEntry;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;

/** ZIP/GZIP/TAR를 entry/total 예산과 symlink 경계 안에서 disk streaming으로 처리합니다. */
public class LocalCpfArchiveService implements CpfArchiveService {
    @Override
    public CpfArchiveResult create(CpfArchiveRequest request) {
        return switch (request.format()) {
            case ZIP -> zip(request);
            case GZIP -> gzip(request);
            case TAR -> tar(request);
        };
    }

    @Override
    public List<CpfExtractedArchiveEntry> extract(
            Path archive, CpfArchiveFormat format, Path target, CpfArchivePolicy policy) {
        requireRegularArchive(archive);
        return switch (format) {
            case ZIP -> unzip(archive, target, policy);
            case GZIP -> gunzip(archive, target, policy);
            case TAR -> untar(archive, target, policy);
        };
    }

    private CpfArchiveResult zip(CpfArchiveRequest request) {
        validateTarget(request);
        long total = 0;
        Path temporary = createTemporaryTarget(request);
        Set<String> names = new HashSet<>();
        try {
            try (ZipOutputStream output = new ZipOutputStream(Files.newOutputStream(
                    temporary, StandardOpenOption.TRUNCATE_EXISTING))) {
                for (CpfArchiveEntry entry : request.entries()) {
                    validateEntry(entry, request.policy(), total);
                    requireUniqueEntry(names, entry.name(), request.policy());
                    output.putNextEntry(new ZipEntry(entry.name()));
                    try (InputStream input = entry.openStream()) {
                        requireExactSize(entry, copy(input, output, entry.size()));
                    }
                    output.closeEntry();
                    total += entry.size();
                }
            }
            publish(temporary, request.targetPath(), request.policy().overwriteExisting());
            return result(request, total);
        } catch (IOException failure) {
            deleteQuietly(temporary);
            throw new IllegalStateException("ZIP_CREATE_FAILED", failure);
        } catch (RuntimeException failure) {
            deleteQuietly(temporary);
            throw failure;
        }
    }

    private CpfArchiveResult tar(CpfArchiveRequest request) {
        validateTarget(request);
        long total = 0;
        Path temporary = createTemporaryTarget(request);
        Set<String> names = new HashSet<>();
        try {
            try (TarArchiveOutputStream output = new TarArchiveOutputStream(Files.newOutputStream(
                    temporary, StandardOpenOption.TRUNCATE_EXISTING))) {
                output.setLongFileMode(TarArchiveOutputStream.LONGFILE_ERROR);
                output.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_ERROR);
                for (CpfArchiveEntry entry : request.entries()) {
                    validateEntry(entry, request.policy(), total);
                    requireUniqueEntry(names, entry.name(), request.policy());
                    TarArchiveEntry tarEntry = new TarArchiveEntry(entry.name());
                    tarEntry.setSize(entry.size());
                    tarEntry.setMode(0640);
                    output.putArchiveEntry(tarEntry);
                    try (InputStream input = entry.openStream()) {
                        requireExactSize(entry, copy(input, output, entry.size()));
                    }
                    output.closeArchiveEntry();
                    total += entry.size();
                }
                output.finish();
            }
            publish(temporary, request.targetPath(), request.policy().overwriteExisting());
            return result(request, total);
        } catch (IOException failure) {
            deleteQuietly(temporary);
            throw new IllegalStateException("TAR_CREATE_FAILED", failure);
        } catch (RuntimeException failure) {
            deleteQuietly(temporary);
            throw failure;
        }
    }

    private CpfArchiveResult gzip(CpfArchiveRequest request) {
        validateTarget(request);
        Path source = request.sourcePath();
        requireRegularArchive(source);
        Path temporary = createTemporaryTarget(request);
        try {
            long size = Files.size(source);
            if (size > request.policy().maxEntrySizeBytes() || size > request.policy().maxTotalSizeBytes()) {
                throw new IllegalArgumentException("GZIP_BUDGET_EXCEEDED");
            }
            try (InputStream input = Files.newInputStream(source, LinkOption.NOFOLLOW_LINKS);
                    OutputStream output = new GZIPOutputStream(Files.newOutputStream(
                            temporary, StandardOpenOption.TRUNCATE_EXISTING))) {
                long copied = copy(input, output, size);
                if (copied != size) throw new IllegalStateException("GZIP_SOURCE_SIZE_CHANGED");
            }
            publish(temporary, request.targetPath(), request.policy().overwriteExisting());
            return new CpfArchiveResult("SUCCESS", CpfArchiveFormat.GZIP, request.targetPath(), 1, size,
                    CpfArchiveChecksum.sha256(request.targetPath()), Instant.now(), List.of());
        } catch (IOException failure) {
            deleteQuietly(temporary);
            throw new IllegalStateException("GZIP_CREATE_FAILED", failure);
        } catch (RuntimeException failure) {
            deleteQuietly(temporary);
            throw failure;
        }
    }

    private List<CpfExtractedArchiveEntry> unzip(Path archive, Path target, CpfArchivePolicy policy) {
        List<CpfExtractedArchiveEntry> result = new ArrayList<>();
        ExtractionBudget budget = new ExtractionBudget(policy);
        Set<String> names = new HashSet<>();
        Path base = secureTarget(target, policy);
        ExtractionTransaction transaction = new ExtractionTransaction(base);
        try (ZipInputStream input = new ZipInputStream(Files.newInputStream(archive, LinkOption.NOFOLLOW_LINKS))) {
            for (ZipEntry entry; (entry = input.getNextEntry()) != null;) {
                if (entry.isDirectory()) continue;
                requireUniqueEntry(names, entry.getName(), policy);
                validateZipMetadata(entry, policy);
                result.add(extractEntry(input, entry.getName(), base, policy, budget, transaction));
            }
            transaction.commit();
        } catch (RuntimeException | IOException failure) {
            transaction.rollback(failure);
            if (failure instanceof IOException io) throw new IllegalStateException("ZIP_EXTRACT_FAILED", io);
            throw (RuntimeException) failure;
        }
        return remapResults(result, base, target);
    }

    private List<CpfExtractedArchiveEntry> untar(Path archive, Path target, CpfArchivePolicy policy) {
        List<CpfExtractedArchiveEntry> result = new ArrayList<>();
        ExtractionBudget budget = new ExtractionBudget(policy);
        Set<String> names = new HashSet<>();
        Path base = secureTarget(target, policy);
        ExtractionTransaction transaction = new ExtractionTransaction(base);
        try (TarArchiveInputStream input = new TarArchiveInputStream(
                Files.newInputStream(archive, LinkOption.NOFOLLOW_LINKS))) {
            for (TarArchiveEntry entry; (entry = input.getNextEntry()) != null;) {
                if (entry.isDirectory()) continue;
                requireUniqueEntry(names, entry.getName(), policy);
                if (entry.isSymbolicLink() || entry.isLink() || entry.isCharacterDevice()
                        || entry.isBlockDevice() || entry.isFIFO()) {
                    throw new SecurityException("TAR_SPECIAL_ENTRY_DENIED:" + entry.getName());
                }
                if (entry.getSize() < 0 || entry.getSize() > policy.maxEntrySizeBytes()) {
                    throw new IllegalArgumentException("TAR_ENTRY_BUDGET_EXCEEDED:" + entry.getName());
                }
                result.add(extractEntry(input, entry.getName(), base, policy, budget, transaction));
            }
            transaction.commit();
        } catch (RuntimeException | IOException failure) {
            transaction.rollback(failure);
            if (failure instanceof IOException io) throw new IllegalStateException("TAR_EXTRACT_FAILED", io);
            throw (RuntimeException) failure;
        }
        return remapResults(result, base, target);
    }

    private List<CpfExtractedArchiveEntry> gunzip(Path archive, Path target, CpfArchivePolicy policy) {
        Path base = secureTarget(target, policy);
        String name = archive.getFileName().toString().replaceFirst("(?i)\\.gz$", "");
        ExtractionTransaction transaction = new ExtractionTransaction(base);
        try (InputStream input = new GZIPInputStream(Files.newInputStream(archive, LinkOption.NOFOLLOW_LINKS))) {
            ExtractionBudget budget = new ExtractionBudget(policy);
            CpfExtractedArchiveEntry result = extractEntry(input, name, base, policy, budget, transaction);
            transaction.commit();
            return remapResults(List.of(result), base, target);
        } catch (RuntimeException | IOException failure) {
            transaction.rollback(failure);
            if (failure instanceof IOException io) throw new IllegalStateException("GZIP_EXTRACT_FAILED", io);
            throw (RuntimeException) failure;
        }
    }

    private CpfExtractedArchiveEntry extractEntry(
            InputStream input, String entryName, Path base, CpfArchivePolicy policy,
            ExtractionBudget budget, ExtractionTransaction transaction)
            throws IOException {
        Path output = CpfZipSlipGuard.safeResolve(base, entryName).toAbsolutePath().normalize();
        if (!output.startsWith(base)) throw new SecurityException("ARCHIVE_PATH_ESCAPE:" + entryName);
        ensureNoSymlinkAncestor(base, output.getParent());
        Files.createDirectories(output.getParent());
        if (Files.exists(output, LinkOption.NOFOLLOW_LINKS)) {
            if (Files.isSymbolicLink(output)) throw new SecurityException("ARCHIVE_TARGET_SYMLINK:" + entryName);
            if (!policy.overwriteExisting()) throw new FileAlreadyExistsException(output.toString());
        }
        Path temporary = output.resolveSibling(output.getFileName() + policy.tempSuffix());
        Files.deleteIfExists(temporary);
        DigestCopy copied;
        Path backup = null;
        try {
            copied = copyEntry(input, temporary, budget.remainingEntryBudget());
            budget.consume(copied.size());
            if (Files.exists(output, LinkOption.NOFOLLOW_LINKS)) {
                if (!Files.isRegularFile(output, LinkOption.NOFOLLOW_LINKS)) {
                    throw new SecurityException("ARCHIVE_TARGET_NOT_REGULAR_FILE:" + entryName);
                }
                backup = output.resolveSibling(output.getFileName() + ".cpf-backup-" + UUID.randomUUID());
                moveNoReplace(output, backup);
            }
            try {
                move(temporary, output);
            } catch (RuntimeException | IOException publishFailure) {
                if (backup != null && Files.exists(backup, LinkOption.NOFOLLOW_LINKS)) {
                    move(backup, output);
                }
                throw publishFailure;
            }
            transaction.record(output, backup);
        } catch (RuntimeException | IOException failure) {
            Files.deleteIfExists(temporary);
            throw failure;
        }
        return new CpfExtractedArchiveEntry(entryName, output, copied.size(), copied.sha256());
    }

    private static Path createTemporaryTarget(CpfArchiveRequest request) {
        Path target = request.targetPath().toAbsolutePath().normalize();
        Path parent = target.getParent();
        if (parent == null) throw new IllegalArgumentException("ARCHIVE_TARGET_PARENT_MISSING");
        try {
            Files.createDirectories(parent);
            return Files.createTempFile(parent, target.getFileName() + ".", request.policy().tempSuffix());
        } catch (IOException failure) {
            throw new IllegalStateException("ARCHIVE_TEMP_TARGET_CREATE_FAILED", failure);
        }
    }

    private static void publish(Path temporary, Path target, boolean overwrite) throws IOException {
        Path normalized = target.toAbsolutePath().normalize();
        StandardCopyOption[] options = overwrite
                ? new StandardCopyOption[] {StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING}
                : new StandardCopyOption[] {StandardCopyOption.ATOMIC_MOVE};
        try {
            Files.move(temporary, normalized, options);
        } catch (AtomicMoveNotSupportedException ignored) {
            if (overwrite) Files.move(temporary, normalized, StandardCopyOption.REPLACE_EXISTING);
            else Files.move(temporary, normalized);
        }
    }

    private static void deleteQuietly(Path path) {
        if (path == null) return;
        try { Files.deleteIfExists(path); }
        catch (IOException ignored) { /* recovery cleanup is best-effort; original failure remains primary */ }
    }

    private static void requireExactSize(CpfArchiveEntry entry, long copied) {
        if (copied != entry.size()) {
            throw new IllegalStateException("ARCHIVE_ENTRY_SIZE_CHANGED:" + entry.name());
        }
    }

    private static void requireUniqueEntry(Set<String> names, String entryName, CpfArchivePolicy policy) {
        String normalized = entryName.replace('\\', '/');
        if (normalized.indexOf('\0') >= 0 || normalized.chars().anyMatch(Character::isISOControl)) {
            throw new SecurityException("ARCHIVE_CONTROL_CHARACTER_ENTRY:" + entryName);
        }
        String[] segments = normalized.split("/");
        if (segments.length > policy.maxPathDepth()) {
            throw new SecurityException("ARCHIVE_PATH_DEPTH_EXCEEDED:" + entryName);
        }
        for (String segment : segments) validatePortableSegment(segment, entryName);
        String canonical = normalized.toLowerCase(Locale.ROOT);
        if (!names.add(canonical)) throw new SecurityException("ARCHIVE_DUPLICATE_CANONICAL_ENTRY:" + entryName);
        if (policy.maxNestedArchiveDepth() == 0 && isArchiveName(canonical)) {
            throw new SecurityException("ARCHIVE_NESTED_ENTRY_DENIED:" + entryName);
        }
    }

    private static void validatePortableSegment(String segment, String entryName) {
        if (segment.isBlank() || segment.endsWith(".") || segment.endsWith(" ")) {
            throw new SecurityException("ARCHIVE_NON_PORTABLE_ENTRY:" + entryName);
        }
        String base = segment.toLowerCase(Locale.ROOT).split("\\.", 2)[0];
        if (base.matches("con|prn|aux|nul|com[1-9]|lpt[1-9]")) {
            throw new SecurityException("ARCHIVE_RESERVED_NAME:" + entryName);
        }
    }

    private static boolean isArchiveName(String name) {
        return name.endsWith(".zip") || name.endsWith(".jar") || name.endsWith(".war")
                || name.endsWith(".tar") || name.endsWith(".tgz") || name.endsWith(".gz")
                || name.endsWith(".7z") || name.endsWith(".rar");
    }

    private static void validateZipMetadata(ZipEntry entry, CpfArchivePolicy policy) {
        long size = entry.getSize();
        long compressed = entry.getCompressedSize();
        if (size > policy.maxEntrySizeBytes()) {
            throw new IllegalArgumentException("ZIP_ENTRY_BUDGET_EXCEEDED:" + entry.getName());
        }
        if (size > 0 && compressed > 0 && ((double) size / (double) compressed) > policy.maxCompressionRatio()) {
            throw new SecurityException("ARCHIVE_COMPRESSION_RATIO_EXCEEDED:" + entry.getName());
        }
    }

    private static void validateEntry(CpfArchiveEntry entry, CpfArchivePolicy policy, long total) throws IOException {
        CpfZipSlipGuard.safeResolve(policy.allowedBaseDirectory(), entry.name());
        if (entry.size() > policy.maxEntrySizeBytes() || total + entry.size() > policy.maxTotalSizeBytes()) {
            throw new IllegalArgumentException("ARCHIVE_BUDGET_EXCEEDED:" + entry.name());
        }
    }

    private static CpfArchiveResult result(CpfArchiveRequest request, long total) {
        return new CpfArchiveResult("SUCCESS", request.format(), request.targetPath(), request.entries().size(), total,
                CpfArchiveChecksum.sha256(request.targetPath()), Instant.now(), List.of());
    }

    private static DigestCopy copyEntry(InputStream input, Path target, long maximum) throws IOException {
        MessageDigest digest = digest();
        long count = 0;
        try (OutputStream output = Files.newOutputStream(target, StandardOpenOption.CREATE_NEW)) {
            byte[] buffer = new byte[8192];
            for (int read; (read = input.read(buffer)) >= 0;) {
                if (read == 0) continue;
                count += read;
                if (count > maximum) throw new IllegalArgumentException("ARCHIVE_EXTRACTION_BUDGET_EXCEEDED");
                digest.update(buffer, 0, read);
                output.write(buffer, 0, read);
            }
        }
        return new DigestCopy(count, HexFormat.of().formatHex(digest.digest()));
    }

    private static long copy(InputStream input, OutputStream output, long maximum) throws IOException {
        long count = 0;
        byte[] buffer = new byte[8192];
        for (int read; (read = input.read(buffer)) >= 0;) {
            if (read == 0) continue;
            count += read;
            if (count > maximum) throw new IllegalArgumentException("STREAM_BUDGET_EXCEEDED");
            output.write(buffer, 0, read);
        }
        return count;
    }

    private static Path secureTarget(Path target, CpfArchivePolicy policy) {
        Path allowed = policy.allowedBaseDirectory().toAbsolutePath().normalize();
        Path candidate = target.toAbsolutePath().normalize();
        if (!candidate.startsWith(allowed)) throw new SecurityException("ARCHIVE_TARGET_OUTSIDE_ALLOWED_BASE");
        try {
            ensureNoSymlinkAncestor(allowed, candidate.getParent());
            Files.createDirectories(candidate);
            if (Files.isSymbolicLink(candidate)) throw new SecurityException("ARCHIVE_TARGET_SYMLINK");
            return candidate.toRealPath(LinkOption.NOFOLLOW_LINKS);
        } catch (IOException failure) {
            throw new IllegalStateException("ARCHIVE_TARGET_PREPARE_FAILED", failure);
        }
    }

    private static void validateTarget(CpfArchiveRequest request) {
        Path target = request.targetPath().toAbsolutePath().normalize();
        Path allowed = request.policy().allowedBaseDirectory().toAbsolutePath().normalize();
        if (!target.startsWith(allowed)) throw new SecurityException("ARCHIVE_TARGET_OUTSIDE_ALLOWED_BASE");
        if (Files.exists(target, LinkOption.NOFOLLOW_LINKS) && !request.policy().overwriteExisting()) {
            throw new IllegalArgumentException("ARCHIVE_TARGET_EXISTS");
        }
        ensureNoSymlinkAncestor(allowed, target.getParent());
    }

    private static void requireRegularArchive(Path archive) {
        if (archive == null || !Files.isRegularFile(archive, LinkOption.NOFOLLOW_LINKS)
                || Files.isSymbolicLink(archive)) {
            throw new IllegalArgumentException("ARCHIVE_SOURCE_INVALID");
        }
    }

    private static void ensureNoSymlinkAncestor(Path allowed, Path candidate) {
        if (candidate == null) throw new SecurityException("ARCHIVE_PARENT_MISSING");
        Path absoluteAllowed = allowed.toAbsolutePath().normalize();
        Path current = candidate.toAbsolutePath().normalize();
        if (!current.startsWith(absoluteAllowed)) throw new SecurityException("ARCHIVE_PARENT_ESCAPE");
        while (current != null && current.startsWith(absoluteAllowed)) {
            if (Files.exists(current, LinkOption.NOFOLLOW_LINKS) && Files.isSymbolicLink(current)) {
                throw new SecurityException("ARCHIVE_PARENT_SYMLINK:" + current);
            }
            if (current.equals(absoluteAllowed)) return;
            current = current.getParent();
        }
        throw new SecurityException("ARCHIVE_PARENT_OUTSIDE_ALLOWED_BASE");
    }

    private static List<CpfExtractedArchiveEntry> remapResults(
            List<CpfExtractedArchiveEntry> entries, Path base, Path requestedTarget) {
        Path target = requestedTarget.toAbsolutePath().normalize();
        return entries.stream()
                .map(entry -> new CpfExtractedArchiveEntry(
                        entry.name(),
                        target.resolve(base.relativize(entry.path())).normalize(),
                        entry.size(),
                        entry.checksumSha256()))
                .toList();
    }

    private static void moveNoReplace(Path source, Path target) throws IOException {
        try {
            Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException ignored) {
            Files.move(source, target);
        }
    }

    private static void move(Path source, Path target) throws IOException {
        try {
            Files.move(source, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException ignored) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static MessageDigest digest() {
        try { return MessageDigest.getInstance("SHA-256"); }
        catch (Exception failure) { throw new IllegalStateException("SHA256_UNAVAILABLE", failure); }
    }

    private record DigestCopy(long size, String sha256) {}

    private static final class ExtractionTransaction {
        private final Path base;
        private final List<PublishedEntry> published = new ArrayList<>();
        private boolean committed;

        private ExtractionTransaction(Path base) { this.base = base; }
        private void record(Path output, Path backup) { published.add(new PublishedEntry(output, backup)); }
        private void commit() {
            committed = true;
            for (PublishedEntry entry : published) {
                if (entry.backup() != null) deleteQuietly(entry.backup());
            }
        }
        private void rollback(Throwable primary) {
            if (committed) return;
            for (int index = published.size() - 1; index >= 0; index--) {
                PublishedEntry entry = published.get(index);
                try {
                    Files.deleteIfExists(entry.output());
                    if (entry.backup() != null && Files.exists(entry.backup(), LinkOption.NOFOLLOW_LINKS)) {
                        move(entry.backup(), entry.output());
                    }
                } catch (IOException rollbackFailure) {
                    primary.addSuppressed(rollbackFailure);
                }
            }
            cleanupEmptyDirectories(base, primary);
        }
        private static void cleanupEmptyDirectories(Path base, Throwable primary) {
            try (var paths = Files.walk(base)) {
                paths.filter(Files::isDirectory)
                        .sorted(java.util.Comparator.reverseOrder())
                        .filter(path -> !path.equals(base))
                        .forEach(path -> {
                            try (var children = Files.list(path)) {
                                if (children.findAny().isEmpty()) Files.deleteIfExists(path);
                            } catch (IOException failure) {
                                primary.addSuppressed(failure);
                            }
                        });
            } catch (IOException failure) {
                primary.addSuppressed(failure);
            }
        }
    }

    private record PublishedEntry(Path output, Path backup) {}

    private static final class ExtractionBudget {
        private final CpfArchivePolicy policy;
        private long total;
        private int entries;

        private ExtractionBudget(CpfArchivePolicy policy) { this.policy = policy; }
        private long remainingEntryBudget() {
            if (++entries > policy.maxEntries()) throw new IllegalArgumentException("ARCHIVE_ENTRY_COUNT_EXCEEDED");
            return Math.min(policy.maxEntrySizeBytes(), policy.maxTotalSizeBytes() - total);
        }
        private void consume(long size) {
            total += size;
            if (total > policy.maxTotalSizeBytes()) throw new IllegalArgumentException("ARCHIVE_TOTAL_BUDGET_EXCEEDED");
        }
    }
}
