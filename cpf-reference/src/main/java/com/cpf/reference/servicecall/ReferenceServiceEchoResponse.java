package com.cpf.reference.servicecall;

import com.cpf.core.api.base.CpfResponse;

/**
 * REF 중립 호출 샘플의 typed 응답입니다.
 *
 * @param requestKey 요청 식별 키
 * @param statusCode HTTP 응답 상태
 * @param processedAt 시뮬레이터 처리 시각
 */
public record ReferenceServiceEchoResponse(
        String requestKey,
        String statusCode,
        String processedAt) implements CpfResponse {
}
