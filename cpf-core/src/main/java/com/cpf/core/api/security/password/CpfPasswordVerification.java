package com.cpf.core.api.security.password;

/** 공개 비밀번호 검증 결과. */
public record CpfPasswordVerification(boolean matched, boolean rehashRequired) {
    public static CpfPasswordVerification rejected() { return new CpfPasswordVerification(false, false); }
}
