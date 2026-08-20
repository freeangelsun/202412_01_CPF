package com.cpf.backoffice.online.auth.dto;

/** refresh session 폐기 결과입니다. */
public record BackofficeLogoutResponse(boolean loggedOut, String loginDomain) {
}
