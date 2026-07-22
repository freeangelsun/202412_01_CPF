package com.cpf.admin.opr.dto;

/**
 * ADM 세션 폐기 요청입니다.
 *
 * @param requestUser 요청자 ID
 * @param reason      감사 사유
 */
public record AdmSessionRevokeRequest(String requestUser, String reason) {
}
