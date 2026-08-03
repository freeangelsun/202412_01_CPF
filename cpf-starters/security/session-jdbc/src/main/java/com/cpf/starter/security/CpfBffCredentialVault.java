package com.cpf.starter.security;

import java.time.Instant;
import java.util.Optional;

/** 다중 인스턴스 BFF Credential의 암호화 저장·회전·폐기 계약입니다. */
public interface CpfBffCredentialVault {
    String create(String accessToken, String refreshToken, Instant accessExpiresAt, Instant refreshExpiresAt);
    CpfBffCredential rotate(String handle, String accessToken, String refreshToken,
            Instant accessExpiresAt, Instant refreshExpiresAt, long expectedVersion);
    Optional<CpfBffCredential> find(String handle);
    void revoke(String handle);
    int purgeExpired(Instant now);
}
