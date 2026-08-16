package com.cpf.education.integration.servicecall;
import com.cpf.foundation.api.contract.CpfResponse;

/**
 * EDU 중립 호출 샘플의 typed 응답입니다.
 *
 * @param requestKey 요청 식별 키
 * @param statusCode HTTP 응답 상태
 * @param processedAt 시뮬레이터 처리 시각
 */
public record EducationServiceEchoResponse(
        String requestKey,
        String statusCode,
        String processedAt) implements CpfResponse {
}
