package cpf.adm.opr.dto;

/**
 * ADM 메뉴 등록/수정 요청입니다.
 *
 * @param menuId       메뉴 ID
 * @param parentMenuId 상위 메뉴 ID
 * @param menuName     메뉴명
 * @param menuPath     메뉴 경로
 * @param sortOrder    정렬 순서
 * @param useYn        사용 여부
 * @param requestUser  요청자 ID
 * @param reason       감사 사유
 */
public record AdmMenuSaveRequest(
        String menuId,
        String parentMenuId,
        String menuName,
        String menuPath,
        Integer sortOrder,
        String useYn,
        String requestUser,
        String reason) {
}
