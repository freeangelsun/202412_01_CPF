package com.cpf.backoffice.online.auth.dto;

/** Backend Filter가 검증을 마친 인증 주체와 허용 행위를 전달하는 내부 경계 DTO입니다. */
public record BackofficeAuthorizationResult(
        BackofficeOperatorResponse operator,
        String menuCode,
        String actionCode) {
}
