package com.cpf.file.objectstorage.api;

import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;

/** Provider-neutral object-storage contract. */
/** CpfObjectStorageOperations 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public interface CpfObjectStorageOperations {
    CpfObjectStorageMetadata put(CpfObjectStorageRequest request);
    Optional<CpfObjectStorageMetadata> head(String tenantId, String bucket, String objectKey);
    InputStream get(String tenantId, String bucket, String objectKey, long offset, long length);
    boolean delete(String tenantId, String bucket, String objectKey);
    URI presignGet(String tenantId, String bucket, String objectKey, Duration ttl);
    URI presignPut(String tenantId, String bucket, String objectKey, String contentType, Duration ttl);
    CpfMultipartUpload beginMultipart(String tenantId, String bucket, String objectKey, String contentType, Map<String,String> metadata);
    String uploadPart(CpfMultipartUpload upload, int partNumber, byte[] bytes);
    CpfObjectStorageMetadata completeMultipart(CpfMultipartUpload upload, Map<Integer,String> etags);
    void abortMultipart(CpfMultipartUpload upload);
    int abortMultipartOlderThan(String bucket, Duration age);
}
