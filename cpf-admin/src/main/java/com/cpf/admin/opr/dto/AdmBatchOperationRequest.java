package com.cpf.admin.opr.dto;

/**
 * ADM 배치 실행, 재수행, 중지, 스케줄 변경 요청입니다.
 *
 * @param jobParameters 배치 실행 파라미터 JSON 문자열
 * @param reason 감사 로그에 남길 운영 사유
 */
public record AdmBatchOperationRequest(
        String jobParameters,
        String reason) {
}
