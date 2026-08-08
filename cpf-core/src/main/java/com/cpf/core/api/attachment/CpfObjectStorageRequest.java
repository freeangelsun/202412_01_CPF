package com.cpf.core.api.attachment; import java.io.InputStream; import java.util.Map;
public record CpfObjectStorageRequest(String tenantId,String bucket,String objectKey,InputStream content,long contentLength,String contentType,String checksumSha256,Map<String,String> metadata){public CpfObjectStorageRequest{metadata=metadata==null?Map.of():Map.copyOf(metadata);}}
