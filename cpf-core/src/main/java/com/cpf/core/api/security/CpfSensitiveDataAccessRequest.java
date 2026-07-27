package com.cpf.core.api.security;

/**
 * 민감정보 원문 조회 사유를 요청 본문으로 전달하는 공통 계약입니다.
 *
 * <p>사유를 URL query string에 두지 않아 access log, proxy log, browser history에
 * 운영 사유가 불필요하게 노출되는 것을 방지합니다.</p>
 */
public record CpfSensitiveDataAccessRequest(String reason) {
}
