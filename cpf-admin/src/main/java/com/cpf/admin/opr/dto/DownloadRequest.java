package com.cpf.admin.opr.dto;

/**
 * ADM 공통 다운로드 요청입니다.
 *
 * @param downloadType 다운로드 대상 유형입니다.
 * @param targetType 화면이나 업무 기준 대상 유형입니다.
 * @param reason 다운로드 감사 사유입니다.
 * @param requestUser 요청 운영자 ID입니다.
 * @param fromDate 조회 시작일시 또는 일자입니다.
 * @param toDate 조회 종료일시 또는 일자입니다.
 * @param transactionId 거래 ID 검색 조건입니다.
 * @param traceId trace ID 검색 조건입니다.
 * @param jobId 배치 Job ID 검색 조건입니다.
 * @param limit 최대 다운로드 건수입니다.
 * @param includeSensitive 민감정보 포함 요청 여부입니다.
 */
public record DownloadRequest(
        String downloadType,
        String targetType,
        String reason,
        String requestUser,
        String fromDate,
        String toDate,
        String transactionId,
        String traceId,
        String jobId,
        Integer limit,
        Boolean includeSensitive
) {
}
