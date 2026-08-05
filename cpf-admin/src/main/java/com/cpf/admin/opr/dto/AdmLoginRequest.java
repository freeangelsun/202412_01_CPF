package com.cpf.admin.opr.dto;

/**
 * ADM 운영자 로그인 요청입니다.
 *
 * @param operatorId 운영자 ID
 * @param password   로그인 비밀번호
 * @param otpCode    MFA 활성 운영자의 6자리 TOTP 코드
 */
public record AdmLoginRequest(String operatorId, String password, String otpCode) {
    /** 기존 Password-only 호출자의 Source 호환을 유지합니다. */
    public AdmLoginRequest(String operatorId, String password) {
        this(operatorId, password, null);
    }
}

