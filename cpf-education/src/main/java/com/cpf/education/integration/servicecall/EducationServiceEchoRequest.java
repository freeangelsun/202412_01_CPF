package com.cpf.education.integration.servicecall;
import com.cpf.foundation.api.contract.CpfQuery;

/**
 * EDU 중립 호출 샘플의 typed 입력입니다.
 *
 * @param requestKey 호출 결과를 식별하는 교육용 키
 */
public record EducationServiceEchoRequest(String requestKey) implements CpfQuery {

    /** 입력값을 검증합니다. */
    public EducationServiceEchoRequest {
        if (requestKey == null || requestKey.isBlank()) {
            throw new IllegalArgumentException("requestKey는 필수입니다.");
        }
        requestKey = requestKey.trim();
    }
}
