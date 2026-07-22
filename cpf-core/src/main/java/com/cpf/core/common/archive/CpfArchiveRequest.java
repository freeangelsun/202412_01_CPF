package cpf.pfw.common.archive;

import java.nio.file.Path;
import java.util.List;

/**
 * 압축/해제 요청 표준 DTO입니다.
 */
public record CpfArchiveRequest(
        CpfArchiveFormat format,
        Path sourcePath,
        Path targetPath,
        List<CpfArchiveEntry> entries,
        CpfArchivePolicy policy) {

    public CpfArchiveRequest {
        if (format == null) {
            throw new IllegalArgumentException("압축 포맷은 필수입니다.");
        }
        if (targetPath == null) {
            throw new IllegalArgumentException("대상 파일 경로는 필수입니다.");
        }
        entries = entries == null ? List.of() : List.copyOf(entries);
        policy = policy == null
                ? CpfArchivePolicy.local(targetPath.toAbsolutePath().getParent())
                : policy;
    }

    public static CpfArchiveRequest zip(Path targetPath, List<CpfArchiveEntry> entries, CpfArchivePolicy policy) {
        return new CpfArchiveRequest(CpfArchiveFormat.ZIP, null, targetPath, entries, policy);
    }

    public static CpfArchiveRequest gzip(Path sourcePath, Path targetPath, CpfArchivePolicy policy) {
        return new CpfArchiveRequest(CpfArchiveFormat.GZIP, sourcePath, targetPath, List.of(), policy);
    }
}
