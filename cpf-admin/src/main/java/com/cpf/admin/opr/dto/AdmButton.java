package com.cpf.admin.opr.dto;

/**
 * ADM 버튼/행위 관리 응답입니다.
 *
 * @param buttonId   버튼/행위 ID
 * @param menuId     메뉴 ID
 * @param actionCode 행위 코드
 * @param buttonName 버튼/행위명
 * @param httpMethod 대상 HTTP 메서드
 * @param apiPattern 대상 API 경로 패턴
 * @param sortOrder  정렬 순서
 * @param useYn      사용 여부
 * @param createdAt  등록일시
 * @param updatedAt  수정일시
 */
public record AdmButton(
        String buttonId,
        String menuId,
        String actionCode,
        String buttonName,
        String httpMethod,
        String apiPattern,
        int sortOrder,
        String useYn,
        String createdAt,
        String updatedAt) {
}
