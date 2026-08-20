package com.cpf.admin.opr.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * ADM 운영자 로그인 응답입니다.
 *
 * @param accessToken      BFF Credential Vault로 전달되는 내부 토큰. Browser 응답에서는 제거되며 OpenAPI에도 노출하지 않습니다.
 * @param tokenType        내부 토큰 유형. Browser 응답에서는 제거되며 OpenAPI에도 노출하지 않습니다.
 * @param expiresInSeconds 토큰 만료까지 남은 초
 * @param operator         로그인한 운영자 정보
 * @param menus            운영자 권한 기준으로 노출할 메뉴 목록
 * @param buttonIds        화면 Action Button 권한 ID 목록
 * @param allowedOperationIds Backend API Permission에서 계산한 OpenAPI operationId 허용 projection
 */
public record AdmLoginResponse(
        @Schema(hidden = true) String accessToken,
        @Schema(hidden = true) String tokenType,
        long expiresInSeconds,
        AdmOperator operator,
        List<AdmMenu> menus,
        List<String> buttonIds,
        List<String> allowedOperationIds) {
    public AdmLoginResponse {
        menus = menus == null ? List.of() : List.copyOf(menus);
        buttonIds = buttonIds == null ? List.of() : List.copyOf(buttonIds);
        allowedOperationIds = allowedOperationIds == null ? List.of() : List.copyOf(allowedOperationIds);
    }
}
