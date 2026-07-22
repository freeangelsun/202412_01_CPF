package com.cpf.core.common.security.password;

/**
 * 비밀번호 검증 결과입니다.
 *
 * @param matched 비밀번호 일치 여부
 * @param rehashRequired 로그인 성공 후 최신 정책으로 재해시해야 하는지 여부
 */
public record CpfPasswordVerification(boolean matched, boolean rehashRequired) {

    public static CpfPasswordVerification rejected() {
        return new CpfPasswordVerification(false, false);
    }
}
