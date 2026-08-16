package com.cpf.file.objectstorage.api;

import java.io.InputStream;

/** Streaming malware/content inspection hook owned by File, not a provider. */
@FunctionalInterface
/** CpfObjectStorageScanHook 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public interface CpfObjectStorageScanHook {
    InputStream inspect(String key, InputStream content, long contentLength, String contentType);
    CpfObjectStorageScanHook NOOP = (key, content, length, type) -> content;
}
