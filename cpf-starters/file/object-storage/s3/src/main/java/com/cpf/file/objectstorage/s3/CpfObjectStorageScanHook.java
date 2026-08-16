package com.cpf.file.objectstorage.s3;

import java.io.InputStream;

/** Streaming inspection hook. Implementations may wrap the stream but must not consume it eagerly. */
@FunctionalInterface
public interface CpfObjectStorageScanHook {
    InputStream inspect(String key, InputStream content, long contentLength, String contentType);
    CpfObjectStorageScanHook NOOP = (key, content, length, type) -> content;
}
