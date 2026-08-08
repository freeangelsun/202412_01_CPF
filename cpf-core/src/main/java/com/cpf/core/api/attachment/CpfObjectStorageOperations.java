package com.cpf.core.api.attachment;
import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
public interface CpfObjectStorageOperations {
    CpfObjectStorageMetadata put(CpfObjectStorageRequest request);
    Optional<CpfObjectStorageMetadata> head(String tenantId, String bucket, String key);
    InputStream get(String tenantId, String bucket, String key, long offset, long length);
    boolean delete(String tenantId, String bucket, String key);
    URI presignGet(String tenantId, String bucket, String key, Duration expiry);
    URI presignPut(String tenantId, String bucket, String key, String contentType, Duration expiry);
    CpfMultipartUpload beginMultipart(String tenantId, String bucket, String key, String contentType, Map<String,String> metadata);
    String uploadPart(CpfMultipartUpload upload, int partNumber, byte[] bytes);
    CpfObjectStorageMetadata completeMultipart(CpfMultipartUpload upload, Map<Integer,String> partEtags);
    void abortMultipart(CpfMultipartUpload upload);
    int abortMultipartOlderThan(String bucket, Duration age);
}
