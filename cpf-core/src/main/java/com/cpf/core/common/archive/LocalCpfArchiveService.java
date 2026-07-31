package com.cpf.core.common.archive;

import com.cpf.core.api.archive.CpfArchiveChecksum;
import com.cpf.core.api.archive.CpfArchiveEntry;
import com.cpf.core.api.archive.CpfArchiveFormat;
import com.cpf.core.api.archive.CpfArchivePolicy;
import com.cpf.core.api.archive.CpfArchiveRequest;
import com.cpf.core.api.archive.CpfArchiveResult;
import com.cpf.core.api.archive.CpfArchiveService;
import com.cpf.core.api.archive.CpfExtractedArchiveEntry;
import com.cpf.core.api.archive.CpfZipSlipGuard;
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
import java.util.List;
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
        try {
            Files.createDirectories(request.targetPath().toAbsolutePath().getParent());
            try (ZipOutputStream output = new ZipOutputStream(Files.newOutputStream(
                    request.targetPath(), StandardOpenOption.CREATE_NEW))) {
                for (CpfArchiveEntry entry : request.entries()) {
                    validateEntry(entry, request.policy(), total);
                    output.putNextEntry(new ZipEntry(entry.name()));
                    output.write(entry.content());
                    output.closeEntry();
                    total += entry.size();
                }
            }
            return result(request, total);
        } catch (IOException failure) {
            throw new IllegalStateException("ZIP_CREATE_FAILED", failure);
        }
    }

    private CpfArchiveResult tar(CpfArchiveRequest request) {
        validateTarget(request);
        long total = 0;
        try {
            Files.createDirectories(request.targetPath().toAbsolutePath().getParent());
            try (TarArchiveOutputStream output = new TarArchiveOutputStream(Files.newOutputStream(
                    request.targetPath(), StandardOpenOption.CREATE_NEW))) {
                output.setLongFileMode(TarArchiveOutputStream.LONGFILE_ERROR);
                output.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_ERROR);
                for (CpfArchiveEntry entry : request.entries()) {
                    validateEntry(entry, request.policy(), total);
                    TarArchiveEntry tarEntry = new TarArchiveEntry(entry.name());
                    tarEntry.setSize(entry.size());
                    tarEntry.setMode(0640);
                    output.putArchiveEntry(tarEntry);
                    output.write(entry.content());
                    output.closeArchiveEntry();
                    total += entry.size();
                }
                output.finish();
            }
            return result(request, total);
        } catch (IOException failure) {
            throw new IllegalStateException("TAR_CREATE_FAILED", failure);
        }
    }

    private CpfArchiveResult gzip(CpfArchiveRequest request) {
        validateTarget(request);
        Path source = request.sourcePath();
        requireRegularArchive(source);
        try {
            long size = Files.size(source);
            if (size > request.policy().maxEntrySizeBytes() || size > request.policy().maxTotalSizeBytes()) {
                throw new IllegalArgumentException("GZIP_BUDGET_EXCEEDED");
            }
            Files.createDirectories(request.targetPath().toAbsolutePath().getParent());
            try (InputStream input = Files.newInputStream(source, LinkOption.NOFOLLOW_LINKS);
                    OutputStream output = new GZIPOutputStream(Files.newOutputStream(
                            request.targetPath(), StandardOpenOption.CREATE_NEW))) {
                copy(input, output, size);
            }
            return new CpfArchiveResult("SUCCESS", CpfArchiveFormat.GZIP, request.targetPath(), 1, size,
                    CpfArchiveChecksum.sha256(request.targetPath()), Instant.now(), List.of());
        } catch (IOException failure) {
            throw new IllegalStateException("GZIP_CREATE_FAILED", failure);
        }
    }

    private List<CpfExtractedArchiveEntry> unzip(Path archive, Path target, CpfArchivePolicy policy) {
        List<CpfExtractedArchiveEntry> result = new ArrayList<>();
        ExtractionBudget budget = new ExtractionBudget(policy);
        Path base = secureTarget(target, policy);
        try (ZipInputStream input = new ZipInputStream(Files.newInputStream(archive, LinkOption.NOFOLLOW_LINKS))) {
            for (ZipEntry entry; (entry = input.getNextEntry()) != null;) {
                if (entry.isDirectory()) continue;
                result.add(extractEntry(input, entry.getName(), base, policy, budget));
            }
        } catch (IOException failure) {
            throw new IllegalStateException("ZIP_EXTRACT_FAILED", failure);
        }
        return List.copyOf(result);
    }

    private List<CpfExtractedArchiveEntry> untar(Path archive, Path target, CpfArchivePolicy policy) {
        List<CpfExtractedArchiveEntry> result = new ArrayList<>();
        ExtractionBudget budget = new ExtractionBudget(policy);
        Path base = secureTarget(target, policy);
        try (TarArchiveInputStream input = new TarArchiveInputStream(
                Files.newInputStream(archive, LinkOption.NOFOLLOW_LINKS))) {
            for (TarArchiveEntry entry; (entry = input.getNextEntry()) != null;) {
                if (entry.isDirectory()) continue;
                if (entry.isSymbolicLink() || entry.isLink() || entry.isCharacterDevice()
                        || entry.isBlockDevice() || entry.isFIFO()) {
                    throw new SecurityException("TAR_SPECIAL_ENTRY_DENIED:" + entry.getName());
                }
                if (entry.getSize() < 0 || entry.getSize() > policy.maxEntrySizeBytes()) {
                    throw new IllegalArgumentException("TAR_ENTRY_BUDGET_EXCEEDED:" + entry.getName());
                }
                result.add(extractEntry(input, entry.getName(), base, policy, budget));
            }
        } catch (IOException failure) {
            throw new IllegalStateException("TAR_EXTRACT_FAILED", failure);
        }
        return List.copyOf(result);
    }

    private List<CpfExtractedArchiveEntry> gunzip(Path archive, Path target, CpfArchivePolicy policy) {
        Path base = secureTarget(target, policy);
        String name = archive.getFileName().toString().replaceFirst("(?i)\\.gz$", "");
        try (InputStream input = new GZIPInputStream(Files.newInputStream(archive, LinkOption.NOFOLLOW_LINKS))) {
            ExtractionBudget budget = new ExtractionBudget(policy);
            return List.of(extractEntry(input, name, base, policy, budget));
        } catch (IOException failure) {
            throw new IllegalStateException("GZIP_EXTRACT_FAILED", failure);
        }
    }

    private CpfExtractedArchiveEntry extractEntry(
            InputStream input, String entryName, Path base, CpfArchivePolicy policy, ExtractionBudget budget)
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
        try {
            copied = copyEntry(input, temporary, budget.remainingEntryBudget());
            budget.consume(copied.size());
            move(temporary, output);
        } catch (RuntimeException | IOException failure) {
            Files.deleteIfExists(temporary);
            throw failure;
        }
        return new CpfExtractedArchiveEntry(entryName, output, copied.size(), copied.sha256());
    }

    private static void validateEntry(CpfArchiveEntry entry, CpfArchivePolicy policy, long total) {
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
