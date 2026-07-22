package com.cpf.admin.opr.dto;

/**
 * ADM 버튼/행위 권한 변경 요청입니다.
 *
 * @param allowYn     허용 여부
 * @param requestUser 요청자 ID
 * @param reason      감사 사유
 */
public record AdmButtonPermissionUpdateRequest(
        String allowYn,
        String requestUser,
        String reason) {
}
