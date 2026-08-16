package com.cpf.file.objectstorage.api;

import java.time.Instant;
import java.util.Map;

/** CpfObjectStorageMetadata 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public record CpfObjectStorageMetadata(String tenantId, String bucket, String objectKey, long contentLength,
        String contentType, String etag, String checksumSha256, String versionId, Instant lastModified,
        Map<String,String> metadata) {
    public CpfObjectStorageMetadata { metadata = metadata == null ? Map.of() : Map.copyOf(metadata); }
}
