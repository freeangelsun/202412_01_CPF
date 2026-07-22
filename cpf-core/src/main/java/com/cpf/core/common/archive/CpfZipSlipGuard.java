package com.cpf.core.common.archive;

import java.nio.file.Path;

/**
 * 압축 해제 시 zip-slip 경로 탈출을 차단합니다.
 */
public final class CpfZipSlipGuard {
    private CpfZipSlipGuard() {
    }

    public static Path safeResolve(Path baseDirectory, String entryName) {
        if (baseDirectory == null) {
            throw new IllegalArgumentException("baseDirectory는 필수입니다.");
        }
        if (entryName == null || entryName.isBlank()) {
            throw new IllegalArgumentException("entryName은 필수입니다.");
        }
        String sanitized = entryName.replace('\\', '/');
        if (sanitized.startsWith("/") || sanitized.contains("../")) {
            throw new IllegalArgumentException("압축 entry 경로가 허용 범위를 벗어납니다. entryName=" + entryName);
        }
        Path normalizedBase = baseDirectory.toAbsolutePath().normalize();
        Path target = normalizedBase.resolve(sanitized).normalize();
        if (!target.startsWith(normalizedBase)) {
            throw new IllegalArgumentException("압축 entry 경로가 base directory 밖을 가리킵니다. entryName=" + entryName);
        }
        return target;
    }
}
