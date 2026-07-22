package com.cpf.core.common.attachment;

import java.time.Instant;

/** 안전한 저장소에 기록된 첨부파일 메타 정보입니다. */
public record CpfStoredAttachment(
        String storageKey,
        String originalFileName,
        String storedFileName,
        String contentType,
        long fileSize,
        String checksumSha256,
        Instant storedAt) {
}
