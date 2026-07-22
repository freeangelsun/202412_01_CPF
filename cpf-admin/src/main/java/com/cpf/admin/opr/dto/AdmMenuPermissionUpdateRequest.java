package cpf.adm.opr.dto;

/**
 * ADM 메뉴 권한 변경 요청입니다.
 *
 * @param readYn      조회 권한 여부
 * @param writeYn     등록/수정 권한 여부
 * @param deleteYn    삭제 권한 여부
 * @param requestUser 요청자 ID
 * @param reason      감사 사유
 */
public record AdmMenuPermissionUpdateRequest(
        String readYn,
        String writeYn,
        String deleteYn,
        String requestUser,
        String reason) {
}
