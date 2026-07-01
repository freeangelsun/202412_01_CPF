package cpf.adm.opr.dto;

/**
 * ADM 역할별 API 권한 변경 요청입니다.
 *
 * @param allowYn     허용 여부
 * @param requestUser 요청자 ID
 * @param reason      감사 사유
 */
public record AdmApiPermissionRoleUpdateRequest(
        String allowYn,
        String requestUser,
        String reason) {
}
