package com.cpf.core.api.archive;

import java.nio.file.Path;

/**
 * 압축/해제 시 적용할 보안 정책입니다.
 */
public record CpfArchivePolicy(
        Path allowedBaseDirectory,
        long maxEntrySizeBytes,
        long maxTotalSizeBytes,
        boolean overwriteExisting,
        String tempSuffix,
        String archivedSuffix) {

    public CpfArchivePolicy {
        if (allowedBaseDirectory == null) {
            throw new IllegalArgumentException("허용 base directory는 필수입니다.");
        }
        if (maxEntrySizeBytes < 1) {
            throw new IllegalArgumentException("entry 최대 크기는 1 이상이어야 합니다.");
        }
        if (maxTotalSizeBytes < maxEntrySizeBytes) {
            throw new IllegalArgumentException("전체 최대 크기는 entry 최대 크기 이상이어야 합니다.");
        }
        tempSuffix = tempSuffix == null || tempSuffix.isBlank() ? ".tmp" : tempSuffix;
        archivedSuffix = archivedSuffix == null || archivedSuffix.isBlank() ? ".archived" : archivedSuffix;
    }

    /** 압축 폭탄 방지를 위한 archive entry 수 상한입니다. */
    public int maxEntries() {
        return 10_000;
    }

    public static CpfArchivePolicy local(Path allowedBaseDirectory) {
        return new CpfArchivePolicy(
                allowedBaseDirectory,
                10 * 1024 * 1024L,
                100 * 1024 * 1024L,
                false,
                ".tmp",
                ".archived");
    }
}
