package com.cpf.bizadmin.auth.dto;

/** 본인 비밀번호 변경과 기존 refresh session 폐기 결과입니다. */
public record BzaPasswordChangeResponse(
        boolean changed,
        String loginId,
        boolean refreshTokensRevoked) {
}
