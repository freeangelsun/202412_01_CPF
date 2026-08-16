package com.cpf.admin.opr.dto;

/**
 * ADM API 권한 관리 응답입니다.
 *
 * @param apiPermissionId API 권한 ID
 * @param apiGroupCode    API 그룹 코드
 * @param httpMethod      HTTP 메서드
 * @param apiPath         API 경로 패턴
 * @param apiName         API명
 * @param permissionCode  권한 코드
 * @param menuId          연결 메뉴 ID
 * @param buttonId        연결 버튼/행위 ID
 * @param useYn           사용 여부
 * @param createdAt       등록일시
 * @param updatedAt       수정일시
 */
public record AdmApiPermission(
        String apiPermissionId,
        String apiGroupCode,
        String httpMethod,
        String apiPath,
        String apiName,
        String permissionCode,
        String menuId,
        String buttonId,
        String useYn,
        String createdAt,
        String updatedAt) {
}
