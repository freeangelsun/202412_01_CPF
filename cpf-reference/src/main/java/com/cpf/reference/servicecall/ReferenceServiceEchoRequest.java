package com.cpf.reference.servicecall;

import com.cpf.core.common.base.CpfQuery;

/**
 * REF 중립 호출 샘플의 typed 입력입니다.
 *
 * @param requestKey 호출 결과를 식별하는 교육용 키
 */
public record ReferenceServiceEchoRequest(String requestKey) implements CpfQuery {

    /** 입력값을 검증합니다. */
    public ReferenceServiceEchoRequest {
        if (requestKey == null || requestKey.isBlank()) {
            throw new IllegalArgumentException("requestKey는 필수입니다.");
        }
        requestKey = requestKey.trim();
    }
}
