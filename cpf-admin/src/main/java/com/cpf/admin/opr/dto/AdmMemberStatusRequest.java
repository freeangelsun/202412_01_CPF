package com.cpf.admin.opr.dto;

/**
 * ADM 회원 상태 변경 요청입니다.
 *
 * @param memberStatus 변경할 회원 상태
 * @param lockYn       잠금 여부
 * @param withdrawYn   탈퇴 여부
 * @param requestUser  요청자
 * @param reason       감사 사유
 */
public record AdmMemberStatusRequest(
        String memberStatus,
        String lockYn,
        String withdrawYn,
        String requestUser,
        String reason) {
}
