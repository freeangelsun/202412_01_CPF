package com.cpf.data.cache.api;
import java.time.Instant;
/** CpfLockToken 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public record CpfLockToken(String lockName, String ownerId, long fencingToken, Instant acquiredAt, Instant expiresAt) { }
