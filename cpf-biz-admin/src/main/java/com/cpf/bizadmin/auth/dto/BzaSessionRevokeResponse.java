package com.cpf.bizadmin.auth.dto;

/** 소유권과 낙관적 조건을 통과한 session 폐기 결과입니다. */
public record BzaSessionRevokeResponse(long sessionId, boolean revoked) {
}
