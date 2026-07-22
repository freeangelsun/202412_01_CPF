package com.cpf.admin.opr.dto;

/**
 * ADM 회원 권한 부여/회수 요청입니다.
 *
 * @param roleCode    회원 역할 코드
 * @param roleName    회원 역할명
 * @param roleType    회원 역할 유형
 * @param serviceCode 서비스 코드
 * @param temporaryYn 임시 권한 여부
 * @param expireAt    권한 만료 일시 문자열
 * @param useYn       사용 여부
 * @param requestUser 요청자
 * @param reason      감사 사유
 */
public record AdmMemberRoleRequest(
        String roleCode,
        String roleName,
        String roleType,
        String serviceCode,
        String temporaryYn,
        String expireAt,
        String useYn,
        String requestUser,
        String reason) {
}
