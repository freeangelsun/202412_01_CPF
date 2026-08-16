package com.cpf.admin.opr.dto;

/**
 * 다운로드 유형별 운영 정책입니다.
 *
 * @param downloadType 다운로드 유형입니다.
 * @param menuId 권한 기준 메뉴 ID입니다.
 * @param description 운영자 화면 표시 설명입니다.
 * @param includeSensitiveAllowed 민감정보 원문 포함 허용 여부입니다.
 */
public record DownloadPolicy(
        String downloadType,
        String menuId,
        String description,
        boolean includeSensitiveAllowed
) {
}
