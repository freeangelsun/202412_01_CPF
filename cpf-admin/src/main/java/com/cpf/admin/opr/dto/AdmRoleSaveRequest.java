package com.cpf.admin.opr.dto;

/**
 * ADM 역할 등록/수정 요청입니다.
 *
 * @param roleId      역할 ID
 * @param roleName    역할명
 * @param roleType    역할 유형
 * @param description 역할 설명
 * @param useYn       사용 여부
 * @param requestUser 요청자 ID
 * @param reason      감사 사유
 */
public record AdmRoleSaveRequest(
        String roleId,
        String roleName,
        String roleType,
        String description,
        String useYn,
        String requestUser,
        String reason) {
}
