package com.cpf.admin.opr.dto;

/**
 * ADM 메뉴 관리 응답입니다.
 *
 * @param menuId       메뉴 ID
 * @param parentMenuId 상위 메뉴 ID
 * @param menuName     메뉴명
 * @param menuPath     메뉴 경로
 * @param sortOrder    정렬 순서
 * @param useYn        사용 여부
 * @param createdAt    등록일시
 * @param updatedAt    수정일시
 */
public record AdmMenuManagement(
        String menuId,
        String parentMenuId,
        String menuName,
        String menuPath,
        int sortOrder,
        String useYn,
        String createdAt,
        String updatedAt) {
}
