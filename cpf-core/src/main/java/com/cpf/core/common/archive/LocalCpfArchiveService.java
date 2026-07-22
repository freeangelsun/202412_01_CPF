package cpf.pfw.common.archive;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * 로컬 파일 시스템 기준 PFW 압축/해제 구현체입니다.
 */
public class LocalCpfArchiveService implements CpfArchiveService {

    @Override
    public CpfArchiveResult create(CpfArchiveRequest request) {
        return switch (request.format()) {
            case ZIP -> createZip(request);
            case GZIP -> createGzip(request);
            case TAR -> new CpfArchiveResult(
                    "PARTIAL_IMPLEMENTATION",
                    CpfArchiveFormat.TAR,
                    request.targetPath(),
                    0,
                    0,
                    null,
                    Instant.now(),
                    List.of("TAR는 Java 표준 라이브러리만으로 구현하지 않고 후순위 후보로 둡니다."));
        };
    }

    @Override
    public List<CpfArchiveEntry> extract(Path archivePath, CpfArchiveFormat format, Path targetDirectory, CpfArchivePolicy policy) {
        if (format == CpfArchiveFormat.ZIP) {
            return extractZip(archivePath, targetDirectory, policy);
        }
        if (format == CpfArchiveFormat.GZIP) {
            return List.of(new CpfArchiveEntry(targetDirectory.getFileName().toString(), readGzip(archivePath)));
        }
        throw new IllegalArgumentException("TAR 해제는 후순위 후보입니다.");
    }

    private CpfArchiveResult createZip(CpfArchiveRequest request) {
        validateTarget(request);
        long totalBytes = 0;
        try {
            Files.createDirectories(request.targetPath().toAbsolutePath().getParent());
            try (ZipOutputStream zipOutputStream = new ZipOutputStream(Files.newOutputStream(request.targetPath()))) {
                for (CpfArchiveEntry entry : request.entries()) {
                    validateEntry(entry, request.policy(), totalBytes);
                    zipOutputStream.putNextEntry(new ZipEntry(entry.name()));
                    zipOutputStream.write(entry.content());
                    zipOutputStream.closeEntry();
                    totalBytes += entry.size();
                }
            }
            return new CpfArchiveResult(
                    "SUCCESS",
                    CpfArchiveFormat.ZIP,
                    request.targetPath(),
                    request.entries().size(),
                    totalBytes,
                    CpfArchiveChecksum.sha256(request.targetPath()),
                    Instant.now(),
                    List.of());
        } catch (IOException ex) {
            throw new IllegalStateException("ZIP 압축 생성에 실패했습니다. path=" + request.targetPath(), ex);
        }
    }

    private CpfArchiveResult createGzip(CpfArchiveRequest request) {
        validateTarget(request);
        if (request.sourcePath() == null || !Files.exists(request.sourcePath())) {
            throw new IllegalArgumentException("GZIP sourcePath는 존재하는 파일이어야 합니다.");
        }
        try {
            Files.createDirectories(request.targetPath().toAbsolutePath().getParent());
            long totalBytes = Files.size(request.sourcePath());
            if (totalBytes > request.policy().maxEntrySizeBytes()) {
                throw new IllegalArgumentException("GZIP 대상 파일이 최대 허용 크기를 초과했습니다.");
            }
            try (InputStream inputStream = Files.newInputStream(request.sourcePath());
                 OutputStream outputStream = new GZIPOutputStream(Files.newOutputStream(request.targetPath()))) {
                inputStream.transferTo(outputStream);
            }
            return new CpfArchiveResult(
                    "SUCCESS",
                    CpfArchiveFormat.GZIP,
                    request.targetPath(),
                    1,
                    totalBytes,
                    CpfArchiveChecksum.sha256(request.targetPath()),
                    Instant.now(),
                    List.of());
        } catch (IOException ex) {
            throw new IllegalStateException("GZIP 압축 생성에 실패했습니다. path=" + request.targetPath(), ex);
        }
    }

    private List<CpfArchiveEntry> extractZip(Path archivePath, Path targetDirectory, CpfArchivePolicy policy) {
        List<CpfArchiveEntry> entries = new ArrayList<>();
        long totalBytes = 0;
        try {
            Files.createDirectories(targetDirectory);
            try (ZipInputStream zipInputStream = new ZipInputStream(Files.newInputStream(archivePath))) {
                ZipEntry zipEntry;
                while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                    if (zipEntry.isDirectory()) {
                        continue;
                    }
                    Path target = CpfZipSlipGuard.safeResolve(targetDirectory, zipEntry.getName());
                    Files.createDirectories(target.getParent());
                    byte[] content = readBounded(zipInputStream, policy.maxEntrySizeBytes());
                    totalBytes += content.length;
                    if (totalBytes > policy.maxTotalSizeBytes()) {
                        throw new IllegalArgumentException("압축 해제 전체 크기가 최대 허용 크기를 초과했습니다.");
                    }
                    Files.write(target, content);
                    entries.add(new CpfArchiveEntry(zipEntry.getName(), content));
                }
            }
            return List.copyOf(entries);
        } catch (IOException ex) {
            throw new IllegalStateException("ZIP 압축 해제에 실패했습니다. path=" + archivePath, ex);
        }
    }

    private byte[] readGzip(Path archivePath) {
        try (GZIPInputStream gzipInputStream = new GZIPInputStream(Files.newInputStream(archivePath))) {
            return gzipInputStream.readAllBytes();
        } catch (IOException ex) {
            throw new IllegalStateException("GZIP 압축 해제에 실패했습니다. path=" + archivePath, ex);
        }
    }

    private byte[] readBounded(InputStream inputStream, long maxEntrySizeBytes) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        long total = 0;
        int read;
        while ((read = inputStream.read(buffer)) >= 0) {
            total += read;
            if (total > maxEntrySizeBytes) {
                throw new IllegalArgumentException("압축 entry가 최대 허용 크기를 초과했습니다.");
            }
            outputStream.write(buffer, 0, read);
        }
        return outputStream.toByteArray();
    }

    private void validateTarget(CpfArchiveRequest request) {
        Path target = request.targetPath().toAbsolutePath().normalize();
        Path base = request.policy().allowedBaseDirectory().toAbsolutePath().normalize();
        if (!target.startsWith(base)) {
            throw new IllegalArgumentException("압축 대상 파일이 허용 base directory 밖입니다. path=" + request.targetPath());
        }
        if (Files.exists(request.targetPath()) && !request.policy().overwriteExisting()) {
            throw new IllegalArgumentException("대상 파일이 이미 존재합니다. path=" + request.targetPath());
        }
    }

    private void validateEntry(CpfArchiveEntry entry, CpfArchivePolicy policy, long currentTotalBytes) {
        CpfZipSlipGuard.safeResolve(policy.allowedBaseDirectory(), entry.name());
        if (entry.size() > policy.maxEntrySizeBytes()) {
            throw new IllegalArgumentException("압축 entry가 최대 허용 크기를 초과했습니다. entry=" + entry.name());
        }
        if (currentTotalBytes + entry.size() > policy.maxTotalSizeBytes()) {
            throw new IllegalArgumentException("압축 전체 크기가 최대 허용 크기를 초과했습니다.");
        }
    }
}
