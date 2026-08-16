package com.cpf.file.objectstorage.api;

import java.io.InputStream;
import java.util.Map;

/** CpfObjectStorageRequest 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public record CpfObjectStorageRequest(String tenantId, String bucket, String objectKey, InputStream content,
        long contentLength, String contentType, String checksumSha256, Map<String,String> metadata) {
    public CpfObjectStorageRequest {
        if (tenantId == null || tenantId.isBlank()) throw new IllegalArgumentException("tenantId");
        if (objectKey == null || objectKey.isBlank()) throw new IllegalArgumentException("objectKey");
        if (content == null) throw new IllegalArgumentException("content");
        if (contentLength < 0) throw new IllegalArgumentException("contentLength");
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
