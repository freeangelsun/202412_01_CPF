package com.cpf.core.common.archive;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

/**
 * 압축/해제 처리 결과입니다.
 */
public record CpfArchiveResult(
        String status,
        CpfArchiveFormat format,
        Path outputPath,
        int entryCount,
        long totalBytes,
        String checksum,
        Instant completedAt,
        List<String> warnings) {

    public CpfArchiveResult {
        status = status == null || status.isBlank() ? "UNKNOWN" : status;
        completedAt = completedAt == null ? Instant.now() : completedAt;
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
    }
}
