package com.cpf.admin.opr.dto;

/**
 * ADM 운영자 본인 비밀번호 변경 요청입니다.
 *
 * @param currentPassword 현재 비밀번호
 * @param newPassword 새 비밀번호
 * @param newPasswordConfirm 새 비밀번호 확인
 * @param requestUser 요청 사용자. 서버 인증 정보가 있으면 서버 값이 우선합니다.
 * @param reason 감사 사유
 */
public record AdmPasswordChangeRequest(
        String currentPassword,
        String newPassword,
        String newPasswordConfirm,
        String requestUser,
        String reason) {
}
