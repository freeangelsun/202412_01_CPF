package com.cpf.admin.opr.dto;

/**
 * ADM 배치 실행·재수행·중지·스케줄 위험조치 요청입니다.
 *
 * @param jobParameters 배치 실행 파라미터 JSON 문자열
 * @param reason 감사 가능한 운영 사유
 * @param approvalRequestId 승인 요청 식별자
 * @param expectedVersion 선택 대상의 낙관적 잠금 버전. 버전이 없는 명령은 null
 * @param idempotencyKey 재전송 중복 차단 키
 */
public record AdmBatchOperationRequest(
        String jobParameters,
        String reason,
        String approvalRequestId,
        Long expectedVersion,
        String idempotencyKey) {
    /** 기존 내부 호출 호환 생성자. HTTP 위험조치에서는 승인·멱등 필드가 별도로 검증됩니다. */
    public AdmBatchOperationRequest(String jobParameters, String reason) {
        this(jobParameters, reason, null, null, null);
    }
}
