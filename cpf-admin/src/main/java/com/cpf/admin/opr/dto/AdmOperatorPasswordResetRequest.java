package com.cpf.admin.opr.dto;

/**
 * ADM 운영자 비밀번호 초기화 요청입니다.
 *
 * @param newPassword 비밀번호 정책을 만족하는 신규 비밀번호
 * @param forceChange 다음 로그인 시 강제 변경 여부
 * @param requestUser 요청자 ID
 * @param reason      감사 사유
 */
public record AdmOperatorPasswordResetRequest(
        String newPassword,
        boolean forceChange,
        String requestUser,
        String reason) {
}
