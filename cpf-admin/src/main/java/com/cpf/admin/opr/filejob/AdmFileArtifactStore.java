package com.cpf.admin.opr.filejob;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermission;
import java.security.*;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/** File Job Artifact를 제한 권한 임시영역에 저장하고 traversal·무제한 Upload를 차단합니다. */
@Component
public class AdmFileArtifactStore {
    private static final Set<String> EXTENSIONS = Set.of("csv", "xlsx");
    private final Path root;
    private final long maxUploadBytes;

    public AdmFileArtifactStore(
            @Value("${cpf.admin.file-job.root:${java.io.tmpdir}/cpf-file-job}") String root,
            @Value("${cpf.admin.file-job.max-upload-bytes:104857600}") long maxUploadBytes) {
        if (maxUploadBytes < 1024 || maxUploadBytes > 2L * 1024 * 1024 * 1024) {
            throw new IllegalArgumentException("File Job Upload 상한은 1KB 이상 2GB 이하여야 합니다.");
        }
        this.maxUploadBytes = maxUploadBytes;
        try {
            this.root = Path.of(root).toAbsolutePath().normalize();
            Files.createDirectories(this.root);
            try { Files.setPosixFilePermissions(this.root, Set.of(PosixFilePermission.OWNER_READ,
                    PosixFilePermission.OWNER_WRITE, PosixFilePermission.OWNER_EXECUTE)); }
            catch (UnsupportedOperationException ignored) { }
        } catch (IOException error) {
            throw new IllegalStateException("File Job 저장 경로를 준비할 수 없습니다.", error);
        }
    }

    public Stored storeUpload(String jobId, MultipartFile file, String extension) {
        requiredId(jobId, "jobId");
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("빈 Upload 파일은 허용하지 않습니다.");
        String normalizedExtension = extension == null ? "" : extension.toLowerCase(Locale.ROOT).trim();
        if (!EXTENSIONS.contains(normalizedExtension)) throw new IllegalArgumentException("지원하지 않는 File 확장자입니다.");
        if (file.getSize() > maxUploadBytes) throw new IllegalArgumentException("Upload 파일이 허용 크기를 초과했습니다.");
        Path target = resolve(jobId + "-source." + normalizedExtension);
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            long copied;
            try (InputStream raw = file.getInputStream();
                 DigestInputStream in = new DigestInputStream(raw, digest);
                 OutputStream out = Files.newOutputStream(target, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
                copied = copyBounded(in, out, maxUploadBytes);
            } catch (Exception error) {
                Files.deleteIfExists(target);
                throw error;
            }
            if (copied == 0) { Files.deleteIfExists(target); throw new IllegalArgumentException("빈 Upload 파일은 허용하지 않습니다."); }
            restrict(target);
            return new Stored(target.toString(), java.util.HexFormat.of().formatHex(digest.digest()), copied);
        } catch (RuntimeException error) { throw error; }
        catch (Exception error) { throw new IllegalStateException("Upload 저장에 실패했습니다.", error); }
    }

    public Path createResult(String jobId, String suffix) {
        requiredId(jobId, "jobId");
        String safeSuffix = requiredId(suffix, "suffix");
        try {
            Path target = resolve(jobId + "-" + UUID.randomUUID() + "-" + safeSuffix);
            Files.createFile(target);
            restrict(target);
            return target;
        } catch (IOException error) { throw new IllegalStateException("Result Artifact 생성에 실패했습니다.", error); }
    }

    public Path require(String storedPath) {
        if (storedPath == null || storedPath.isBlank()) throw new IllegalArgumentException("Artifact 경로는 필수입니다.");
        Path path = Path.of(storedPath).toAbsolutePath().normalize();
        if (!path.startsWith(root) || !Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS) || Files.isSymbolicLink(path)) {
            throw new IllegalArgumentException("허용되지 않은 Artifact 경로입니다.");
        }
        return path;
    }

    public void delete(String storedPath) {
        if (storedPath == null || storedPath.isBlank()) return;
        try { Files.deleteIfExists(require(storedPath)); }
        catch (IOException error) { throw new IllegalStateException("Artifact 삭제에 실패했습니다.", error); }
    }

    private Path resolve(String name) {
        Path target = root.resolve(name.replaceAll("[^A-Za-z0-9._-]", "_")).normalize();
        if (!target.startsWith(root)) throw new IllegalArgumentException("Artifact path traversal이 감지되었습니다.");
        return target;
    }
    private void restrict(Path path) throws IOException {
        try { Files.setPosixFilePermissions(path, Set.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE)); }
        catch (UnsupportedOperationException ignored) { }
    }
    private static long copyBounded(InputStream in, OutputStream out, long max) throws IOException {
        byte[] buffer = new byte[64 * 1024];
        long total = 0;
        for (int read; (read = in.read(buffer)) >= 0;) {
            if (read == 0) continue;
            total += read;
            if (total > max) throw new IllegalArgumentException("Upload 파일이 허용 크기를 초과했습니다.");
            out.write(buffer, 0, read);
        }
        return total;
    }
    private static String requiredId(String value, String name) {
        if (value == null || value.isBlank() || value.length() > 160) throw new IllegalArgumentException(name + "가 올바르지 않습니다.");
        return value.trim();
    }
    public record Stored(String path, String sha256, long size) { }
}
