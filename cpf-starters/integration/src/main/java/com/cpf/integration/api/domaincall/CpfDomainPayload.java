package com.cpf.integration.api.domaincall;

import com.cpf.core.api.base.CpfRequest;
import com.cpf.core.api.base.CpfResponse;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 실행 시점에 Operation이 결정되는 Framework Consumer용 JSON Object 계약입니다.
 *
 * <p>일반 업무 Source는 Generated Typed Client를 사용합니다. Center-Cut처럼 DB에 보존된
 * SystemCode/OperationId를 실행하는 공통 Engine만 이 계약을 사용하며, Transport adapter가
 * {@link #values()}를 실제 업무 요청 JSON과 응답 JSON으로 투명하게 변환합니다.</p>
 */
public record CpfDomainPayload(Map<String, Object> values) implements CpfRequest, CpfResponse {
    public CpfDomainPayload {
        if (values == null) throw new IllegalArgumentException("Domain payload values are required");
        values = Map.copyOf(new LinkedHashMap<>(values));
    }
}
