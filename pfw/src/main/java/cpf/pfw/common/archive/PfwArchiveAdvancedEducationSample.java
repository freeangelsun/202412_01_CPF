package cpf.pfw.common.archive;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

/**
 * PFW archive/compression 운영 케이스를 학습하기 위한 고급 샘플입니다.
 *
 * <p>ZIP/GZIP/checksum/zip-slip 기본 기능 위에 재귀 디렉터리 ZIP, overwrite=false 중복 방지,
 * 깨진 압축 파일 처리, entry/total size guard, 처리 이력 metadata를 검증 가능한 형태로 제공합니다.</p>
 */
public class PfwArchiveAdvancedEducationSample {
    private final CpfArchiveService archiveService;

    public PfwArchiveAdvancedEducationSample() {
        this(new LocalCpfArchiveService());
    }

    public PfwArchiveAdvancedEducationSample(CpfArchiveService archiveService) {
        this.archiveService = archiveService;
    }

    /**
     * 디렉터리 경로 구조를 entry 이름으로 보존해 재귀 압축과 동일한 결과를 만듭니다.
     */
    public ArchiveHistoryRecord createRecursiveZip(Path baseDirectory) {
        CpfArchivePolicy policy = CpfArchivePolicy.local(baseDirectory);
        Path target = baseDirectory.resolve("daily-result.zip");
        CpfArchiveResult result = archiveService.create(CpfArchiveRequest.zip(target, List.of(
                new CpfArchiveEntry("2026/07/09/success.csv", "id,status\n1,SUCCESS\n".getBytes(StandardCharsets.UTF_8)),
                new CpfArchiveEntry("2026/07/09/failure.csv", "id,status\n2,FAILED\n".getBytes(StandardCharsets.UTF_8)),
                new CpfArchiveEntry("2026/07/09/meta/manifest.json", "{\"count\":2}".getBytes(StandardCharsets.UTF_8))
        ), policy));
        return history("PFW-ARCHIVE-RECURSIVE", result, "디렉터리 구조를 유지한 ZIP 압축을 완료했습니다.");
    }

    /**
     * overwrite=false 정책에서 동일 대상 파일을 다시 만들면 중복 방지 예외가 발생해야 합니다.
     */
    public DuplicatePreventionResult duplicatePrevention(Path baseDirectory) {
        CpfArchivePolicy policy = CpfArchivePolicy.local(baseDirectory);
        Path target = baseDirectory.resolve("duplicate.zip");
        List<CpfArchiveEntry> entries = List.of(new CpfArchiveEntry("a.txt", "A".getBytes(StandardCharsets.UTF_8)));
        archiveService.create(CpfArchiveRequest.zip(target, entries, policy));
        try {
            archiveService.create(CpfArchiveRequest.zip(target, entries, policy));
            return new DuplicatePreventionResult(false, "중복 대상 파일이 허용되었습니다.");
        } catch (IllegalArgumentException ex) {
            return new DuplicatePreventionResult(true, ex.getMessage());
        }
    }

    /**
     * entry/total size guard가 너무 큰 archive 생성을 차단하는지 확인합니다.
     */
    public GuardResult maxSizeGuard(Path baseDirectory) {
        CpfArchivePolicy policy = new CpfArchivePolicy(baseDirectory, 4, 8, false, ".tmp", ".archived");
        try {
            archiveService.create(CpfArchiveRequest.zip(
                    baseDirectory.resolve("too-large.zip"),
                    List.of(new CpfArchiveEntry("large.txt", "12345".getBytes(StandardCharsets.UTF_8))),
                    policy));
            return new GuardResult(false, "크기 제한을 초과한 entry가 허용되었습니다.");
        } catch (IllegalArgumentException ex) {
            return new GuardResult(true, ex.getMessage());
        }
    }

    /**
     * 깨진 ZIP 입력은 운영자가 후속 조치할 수 있도록 실패 사유를 명확히 분류합니다.
     */
    public CorruptedArchiveResult corruptedArchive(Path baseDirectory) {
        try {
            Files.createDirectories(baseDirectory);
            Path broken = baseDirectory.resolve("broken.zip");
            Files.writeString(broken, "not-a-zip", StandardCharsets.UTF_8);
            byte[] signature = Files.readAllBytes(broken);
            if (signature.length < 2 || signature[0] != 'P' || signature[1] != 'K') {
                return new CorruptedArchiveResult(true, "CORRUPTED_ARCHIVE", 0);
            }
            List<CpfArchiveEntry> entries = archiveService.extract(
                    broken,
                    CpfArchiveFormat.ZIP,
                    baseDirectory.resolve("extract"),
                    CpfArchivePolicy.local(baseDirectory));
            return new CorruptedArchiveResult(false, "BROKEN_ARCHIVE_ACCEPTED", entries.size());
        } catch (IOException ex) {
            throw new IllegalStateException("깨진 압축 파일 샘플 준비에 실패했습니다.", ex);
        } catch (IllegalStateException ex) {
            return new CorruptedArchiveResult(true, "CORRUPTED_ARCHIVE", 0);
        }
    }

    /**
     * Java 표준 라이브러리에 TAR 구현이 없어 TAR는 후보 상태로 분리해 기록합니다.
     */
    public CpfArchiveResult tarPlan(Path baseDirectory) {
        return archiveService.create(new CpfArchiveRequest(
                CpfArchiveFormat.TAR,
                null,
                baseDirectory.resolve("result.tar"),
                List.of(),
                CpfArchivePolicy.local(baseDirectory)));
    }

    private ArchiveHistoryRecord history(String archiveJobId, CpfArchiveResult result, String message) {
        return new ArchiveHistoryRecord(
                archiveJobId,
                result.status(),
                result.format(),
                result.outputPath(),
                result.entryCount(),
                result.totalBytes(),
                result.checksum(),
                message,
                Instant.parse("2026-07-09T03:00:00Z"));
    }

    public record ArchiveHistoryRecord(
            String archiveJobId,
            String status,
            CpfArchiveFormat format,
            Path outputPath,
            int entryCount,
            long totalBytes,
            String checksum,
            String detail,
            Instant loggedAt) {
    }

    public record DuplicatePreventionResult(boolean blocked, String detail) {
    }

    public record GuardResult(boolean blocked, String detail) {
    }

    public record CorruptedArchiveResult(boolean detected, String failureCode, int extractedEntryCount) {
    }
}
