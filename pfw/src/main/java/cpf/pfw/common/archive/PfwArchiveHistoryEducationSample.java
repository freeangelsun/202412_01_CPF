package cpf.pfw.common.archive;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Map;

/**
 * 압축 처리 결과를 ADM 이력이나 운영 로그에 남길 때 사용하는 metadata 샘플입니다.
 */
public class PfwArchiveHistoryEducationSample {

    /**
     * 압축 결과에서 운영자가 검색할 수 있는 필드만 추려 감사 안전한 이력 DTO를 만듭니다.
     */
    public ArchiveOperationHistory history(String archiveJobId, CpfArchiveResult result) {
        return new ArchiveOperationHistory(
                archiveJobId,
                result.status(),
                result.format(),
                result.outputPath(),
                result.entryCount(),
                result.totalBytes(),
                result.checksum(),
                result.completedAt(),
                Map.of(
                        "checksumAlgorithm", "SHA-256",
                        "rawPayloadStored", "false",
                        "downloadPermissionRequired", "true"));
    }

    public record ArchiveOperationHistory(
            String archiveJobId,
            String status,
            CpfArchiveFormat format,
            Path outputPath,
            int entryCount,
            long totalBytes,
            String checksum,
            Instant completedAt,
            Map<String, String> attributes) {

        public ArchiveOperationHistory {
            attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
        }
    }
}
