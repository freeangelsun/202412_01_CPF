package com.cpf.admin.opr.dto;

/**
 * ADM 운영자 로그인 요청입니다.
 *
 * @param operatorId 운영자 ID
 * @param password   로그인 비밀번호
 */
public record AdmLoginRequest(String operatorId, String password) {
}
