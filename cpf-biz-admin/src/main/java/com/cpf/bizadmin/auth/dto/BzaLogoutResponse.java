package com.cpf.bizadmin.auth.dto;

/** refresh session 폐기 결과입니다. */
public record BzaLogoutResponse(boolean loggedOut, String loginDomain) {
}
