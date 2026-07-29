package com.cpf.core.api.cache;

import java.time.Instant;
import java.util.Arrays;

/** 직렬화 형식에 종속되지 않는 Cache 값입니다. */
public record CpfCacheValue(boolean found, boolean negative, byte[] payload, String contentType,
                            long version, Instant expiresAt) {
    public CpfCacheValue {
        payload = payload == null ? new byte[0] : Arrays.copyOf(payload, payload.length);
        contentType = contentType == null || contentType.isBlank() ? "application/octet-stream" : contentType.trim();
        if (!found && (negative || payload.length > 0 || version != 0)) {
            throw new IllegalArgumentException("Cache miss는 negative/payload/version을 포함할 수 없습니다.");
        }
        if (negative && (!found || payload.length > 0)) {
            throw new IllegalArgumentException("Negative cache 값은 found=true이며 payload를 포함할 수 없습니다.");
        }
        if (version < 0) throw new IllegalArgumentException("Cache version은 음수일 수 없습니다.");
        if (contentType.length() > 1024 || contentType.indexOf('\r') >= 0 || contentType.indexOf('\n') >= 0) {
            throw new IllegalArgumentException("Cache contentType 형식이 올바르지 않습니다.");
        }
    }
    @Override
    public byte[] payload() { return Arrays.copyOf(payload, payload.length); }
    public static CpfCacheValue miss() { return new CpfCacheValue(false, false, new byte[0], "application/octet-stream", 0, null); }
    public static CpfCacheValue negative(long version, Instant expiresAt) {
        return new CpfCacheValue(true, true, new byte[0], "application/x-cpf-negative", version, expiresAt);
    }
}
