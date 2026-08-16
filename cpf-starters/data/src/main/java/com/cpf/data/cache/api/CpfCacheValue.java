package com.cpf.data.cache.api;

import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;

/** Immutable cache value envelope with version and expiry. */
/** CpfCacheValue 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public record CpfCacheValue(boolean found, boolean negative, byte[] payload, String contentType, long version, Instant expiresAt) {
    public CpfCacheValue {
        payload = payload == null ? new byte[0] : Arrays.copyOf(payload, payload.length);
        contentType = Objects.requireNonNullElse(contentType, "application/octet-stream");
        if (version < 0) throw new IllegalArgumentException("version must not be negative");
    }
    @Override public byte[] payload() { return Arrays.copyOf(payload, payload.length); }
    /** miss 작업을 CPF 표준 계약에 따라 수행한다. */
    public static CpfCacheValue miss() { return new CpfCacheValue(false, false, new byte[0], "application/octet-stream", 0, null); }
    /** Creates a negative-cache hit without retaining an origin payload. */
    public static CpfCacheValue negative(long version, Instant expiresAt) {
        return new CpfCacheValue(true, true, new byte[0], "application/octet-stream", version,
                Objects.requireNonNull(expiresAt, "expiresAt"));
    }
}
