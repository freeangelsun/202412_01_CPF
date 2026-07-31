package com.cpf.starter.security;

import java.time.Instant;

/** BFF가 내부 호출에만 사용하는 복호화된 단기 Credential입니다. */
public record CpfBffCredential(
        String handle,
        String accessToken,
        String refreshToken,
        Instant accessExpiresAt,
        Instant refreshExpiresAt,
        long version) {
    public CpfBffCredential {
        if (handle == null || handle.isBlank()) throw new IllegalArgumentException("handle is required");
        if (accessToken == null || accessToken.isBlank()) throw new IllegalArgumentException("accessToken is required");
        if (accessExpiresAt == null) throw new IllegalArgumentException("accessExpiresAt is required");
        if (refreshExpiresAt == null) refreshExpiresAt = accessExpiresAt;
    }

    public boolean accessExpired(Instant now) { return !accessExpiresAt.isAfter(now); }
    public boolean refreshExpired(Instant now) { return !refreshExpiresAt.isAfter(now); }
}
