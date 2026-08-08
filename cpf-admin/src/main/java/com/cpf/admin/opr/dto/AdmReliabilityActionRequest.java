package com.cpf.admin.opr.dto;

/**
 * DLQ 재처리와 결과 미확정 수동 확정에 사용하는 ADM 운영 요청입니다.
 * 인증된 운영자 식별자는 HTTP Session/Security Context에서 서버가 결정합니다.
 */
public record AdmReliabilityActionRequest(
        String targetStatus,
        Long expectedVersion,
        String reason) {
}
