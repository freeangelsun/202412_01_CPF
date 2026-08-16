package com.cpf.admin.opr.dto;

/**
 * ADM 관리 대상 사용/중지 상태 변경 요청입니다.
 *
 * @param useYn       사용 여부
 * @param requestUser 요청자 ID
 * @param reason      감사 사유
 */
public record AdmStatusUpdateRequest(
        String useYn,
        String requestUser,
        String reason) {
}
